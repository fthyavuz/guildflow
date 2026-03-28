package com.guildflow.backend.service;

import com.guildflow.backend.dto.*;
import com.guildflow.backend.exception.EntityNotFoundException;
import com.guildflow.backend.exception.ForbiddenException;
import com.guildflow.backend.exception.ValidationException;
import com.guildflow.backend.model.*;
import com.guildflow.backend.model.enums.ProgressEntryStatus;
import com.guildflow.backend.model.enums.Role;
import com.guildflow.backend.repository.*;
import com.guildflow.backend.util.SecurityUtils;
import com.guildflow.backend.repository.ClassHomeworkAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles student-facing goal progress tracking and mentor reviews.
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class GoalProgressService {

    private final GoalRepository goalRepository;
    private final GoalTaskRepository goalTaskRepository;
    private final GoalStudentRepository goalStudentRepository;
    private final TaskProgressRepository taskProgressRepository;
    private final GoalStudentReviewRepository reviewRepository;
    private final ClassStudentRepository classStudentRepository;
    private final ClassHomeworkAssignmentRepository classHomeworkAssignmentRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    public List<GoalProgressResponse> getStudentGoalsWithProgress(User student) {
        List<Goal> goals = getGoalEntitiesForStudent(student);
        if (goals.isEmpty()) return Collections.emptyList();

        List<Long> taskIds = goals.stream()
                .flatMap(g -> g.getTasks().stream())
                .map(GoalTask::getId)
                .collect(Collectors.toList());

        if (taskIds.isEmpty()) return Collections.emptyList();

        Map<Long, List<TaskProgress>> progressByTaskId = taskProgressRepository
                .findByTaskIdsAndStudentAndStatus(taskIds, student, ProgressEntryStatus.APPROVED)
                .stream()
                .collect(Collectors.groupingBy(tp -> tp.getTask().getId()));

        return goals.stream().map(goal -> {
            List<TaskProgressResponse> taskProgresses = goal.getTasks().stream().map(task -> {
                List<TaskProgress> entries = progressByTaskId.getOrDefault(task.getId(), Collections.emptyList());

                Double currentVal = 0.0;
                if (task.getTaskType() == com.guildflow.backend.model.enums.TaskType.NUMBER) {
                    currentVal = entries.stream()
                            .mapToDouble(e -> e.getNumericValue() != null ? e.getNumericValue() : 0.0)
                            .sum();
                } else if (task.getTaskType() == com.guildflow.backend.model.enums.TaskType.CHECKBOX) {
                    currentVal = (double) entries.stream()
                            .filter(e -> Boolean.TRUE.equals(e.getBooleanValue()))
                            .count();
                }

                double percentage = task.getTargetValue() != null && task.getTargetValue() > 0
                        ? (currentVal / task.getTargetValue()) * 100
                        : 0;
                if (percentage > 100) percentage = 100;

                return TaskProgressResponse.builder()
                        .taskId(task.getId())
                        .title(task.getTitle())
                        .taskType(task.getTaskType())
                        .targetValue(task.getTargetValue())
                        .currentValue(currentVal)
                        .progressPercentage(percentage)
                        .build();
            }).collect(Collectors.toList());

            double overallProgress = taskProgresses.isEmpty() ? 0
                    : taskProgresses.stream()
                            .mapToDouble(TaskProgressResponse::getProgressPercentage)
                            .average().orElse(0.0);

            return GoalProgressResponse.builder()
                    .goalId(goal.getId())
                    .title(goal.getTitle())
                    .tasks(taskProgresses)
                    .overallProgress(overallProgress)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public void submitProgress(ProgressRequest request, User student) {
        GoalTask task = goalTaskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        TaskProgress progress = taskProgressRepository
                .findByTaskAndStudentAndEntryDate(task, student, request.getEntryDate())
                .orElse(TaskProgress.builder()
                        .task(task)
                        .student(student)
                        .entryDate(request.getEntryDate())
                        .status(ProgressEntryStatus.PENDING)
                        .build());

        // If already approved, do not allow re-submission
        if (ProgressEntryStatus.APPROVED.equals(progress.getStatus())) {
            return;
        }

        progress.setNumericValue(request.getNumericValue());
        progress.setBooleanValue(request.getBooleanValue());
        progress.setStatus(ProgressEntryStatus.PENDING);
        taskProgressRepository.save(progress);
    }

    @Transactional
    public void submitReview(GoalReviewRequest request, User mentor) {
        Goal goal = goalRepository.findById(request.getGoalId())
                .orElseThrow(() -> new EntityNotFoundException("Goal not found"));

        securityUtils.validateUserState(mentor);

        if (goal.getMentorClass() == null) {
            throw new ValidationException("Goal has no associated class and cannot be reviewed");
        }
        securityUtils.requireAdminOrOwner(mentor, goal.getMentorClass().getMentor(),
                "Access denied: You are not the mentor of this goal");

        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        GoalStudentReview review = reviewRepository
                .findByGoalAndStudent(goal, student)
                .orElse(GoalStudentReview.builder()
                        .goal(goal)
                        .student(student)
                        .build());

        review.setCompleted(request.getCompleted());
        review.setComment(request.getComment());
        reviewRepository.save(review);
    }

    private List<Goal> getGoalEntitiesForStudent(User student) {
        ClassStudent activeEnrollment = classStudentRepository.findByStudentAndActiveTrue(student)
                .orElse(null);

        if (activeEnrollment == null || activeEnrollment.getMentorClass() == null)
            return Collections.emptyList();

        // Homework assigned via the Assignments tab is stored in class_homework_assignments.
        // The linked goal is a template (mentorClass = null), so querying goals by mentorClass
        // never finds them. We must resolve the goals through assignments instead.
        List<Goal> assignedGoals = classHomeworkAssignmentRepository
                .findByMentorClassOrderByCreatedAtDesc(activeEnrollment.getMentorClass())
                .stream()
                .filter(a -> Boolean.TRUE.equals(a.getApplyToAll())
                          || a.getStudentIds().contains(student.getId()))
                .map(ClassHomeworkAssignment::getGoal)
                .collect(Collectors.toList());

        // Legacy path: goals directly attached to the class (old assignment model)
        List<Goal> legacyClassGoals = goalRepository
                .findByMentorClassAndActiveTrueWithTasks(activeEnrollment.getMentorClass())
                .stream().filter(Goal::getApplyToAll).collect(Collectors.toList());

        // Private goals explicitly assigned to this student
        List<Goal> privateGoals = goalStudentRepository.findByStudentWithGoalTasks(student)
                .stream().map(GoalStudent::getGoal).collect(Collectors.toList());

        assignedGoals.addAll(legacyClassGoals);
        assignedGoals.addAll(privateGoals);
        return assignedGoals.stream().distinct().collect(Collectors.toList());
    }
}
