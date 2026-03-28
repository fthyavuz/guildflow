package com.guildflow.backend.service;

import com.guildflow.backend.dto.*;
import com.guildflow.backend.exception.EntityNotFoundException;
import com.guildflow.backend.exception.ForbiddenException;
import com.guildflow.backend.exception.ValidationException;
import com.guildflow.backend.model.*;
import com.guildflow.backend.model.enums.ProgressEntryStatus;
import com.guildflow.backend.model.enums.TaskType;
import com.guildflow.backend.repository.*;
import com.guildflow.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    // ── Student homework list ─────────────────────────────────────────────────

    public List<HomeworkSummaryResponse> getStudentHomeworkList(User student) {
        List<ClassHomeworkAssignment> assignments = getAssignmentsForStudent(student);
        if (assignments.isEmpty()) return Collections.emptyList();

        return assignments.stream().map(a -> {
            List<GoalTask> tasks = a.getGoal().getTasks();
            double overall = computeOverallProgress(tasks, student);
            return HomeworkSummaryResponse.builder()
                    .assignmentId(a.getId())
                    .title(a.getGoal().getTitle())
                    .description(a.getGoal().getDescription())
                    .startDate(a.getStartDate())
                    .endDate(a.getEndDate())
                    .frequency(a.getFrequency() != null ? a.getFrequency().name() : null)
                    .taskCount(tasks.size())
                    .overallProgress(overall)
                    .build();
        }).collect(Collectors.toList());
    }

    // ── Day entries ────────────────────────────────────────────────────────────

    public List<DayEntryResponse> getDayEntries(Long assignmentId, LocalDate date, User student) {
        ClassHomeworkAssignment assignment = assignmentRepository(assignmentId);
        validateStudentAccess(assignment, student);

        List<GoalTask> tasks = assignment.getGoal().getTasks();
        List<Long> taskIds = tasks.stream().map(GoalTask::getId).collect(Collectors.toList());

        // All entries for this day
        Map<Long, TaskProgress> entriesByTaskId = taskProgressRepository
                .findByTaskIdsAndStudentAndDate(taskIds, student, date)
                .stream().collect(Collectors.toMap(tp -> tp.getTask().getId(), tp -> tp));

        // Check if day is locked (at least one locked entry)
        boolean dayLocked = entriesByTaskId.values().stream().anyMatch(tp -> Boolean.TRUE.equals(tp.getLocked()));

        return tasks.stream().sorted(Comparator.comparing(GoalTask::getSortOrder)).map(task -> {
            TaskProgress entry = entriesByTaskId.get(task.getId());

            // CHECKBOX permanent-done check (any entry ever)
            boolean donePermanently = task.getTaskType() == TaskType.CHECKBOX
                    && taskProgressRepository.existsByTaskAndStudentAndDonePermanentlyTrue(task, student);

            // Cumulative value for NUMBER tasks
            Double cumulative = 0.0;
            if (task.getTaskType() == TaskType.NUMBER) {
                cumulative = taskProgressRepository.sumNumericValueByTaskAndStudent(task, student);
            }

            Double dailyLimit = (task.getSource() != null) ? task.getSource().getDailyLimit() : null;

            return DayEntryResponse.builder()
                    .taskId(task.getId())
                    .title(task.getTitle())
                    .taskType(task.getTaskType())
                    .targetValue(task.getTargetValue())
                    .cumulativeValue(cumulative)
                    .entryId(entry != null ? entry.getId() : null)
                    .numericEntry(entry != null ? entry.getNumericValue() : null)
                    .booleanEntry(entry != null ? entry.getBooleanValue() : null)
                    .dayLocked(dayLocked)
                    .donePermanently(donePermanently)
                    .dailyLimit(dailyLimit)
                    .build();
        }).collect(Collectors.toList());
    }

    // ── Save & lock a day ─────────────────────────────────────────────────────

    @Transactional
    public List<DayEntryResponse> saveDayEntries(Long assignmentId, SaveDayRequest request, User student) {
        ClassHomeworkAssignment assignment = assignmentRepository(assignmentId);
        validateStudentAccess(assignment, student);

        // Validate date is within assignment range and not in the future
        LocalDate date = request.getDate();
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException("Cannot submit entries for a future date");
        }
        if (assignment.getStartDate() != null && date.isBefore(assignment.getStartDate())) {
            throw new ValidationException("Date is before assignment start date");
        }
        if (assignment.getEndDate() != null && date.isAfter(assignment.getEndDate())) {
            throw new ValidationException("Date is after assignment end date");
        }

        // Check if already locked
        boolean alreadyLocked = !taskProgressRepository
                .findByStudentAndEntryDateAndLockedTrue(student, date).isEmpty();
        if (alreadyLocked) {
            throw new ValidationException("This day's entries are already saved and locked");
        }

        List<Long> taskIds = assignment.getGoal().getTasks()
                .stream().map(GoalTask::getId).collect(Collectors.toList());

        for (SaveDayRequest.TaskEntry e : request.getEntries()) {
            if (!taskIds.contains(e.getTaskId())) continue;

            GoalTask task = goalTaskRepository.findById(e.getTaskId())
                    .orElseThrow(() -> new EntityNotFoundException("Task not found: " + e.getTaskId()));

            // CHECKBOX: if already permanently done, skip
            if (task.getTaskType() == TaskType.CHECKBOX
                    && taskProgressRepository.existsByTaskAndStudentAndDonePermanentlyTrue(task, student)) {
                continue;
            }

            TaskProgress progress = taskProgressRepository
                    .findByTaskAndStudentAndEntryDate(task, student, date)
                    .orElse(TaskProgress.builder()
                            .task(task).student(student).entryDate(date)
                            .status(ProgressEntryStatus.PENDING)
                            .build());

            // Enforce daily limit for NUMBER tasks
            if (task.getTaskType() == TaskType.NUMBER
                    && task.getSource() != null
                    && task.getSource().getDailyLimit() != null
                    && e.getNumericValue() != null
                    && e.getNumericValue() > task.getSource().getDailyLimit()) {
                throw new ValidationException("Value for task '" + task.getTitle()
                        + "' exceeds the daily limit of " + task.getSource().getDailyLimit());
            }

            progress.setNumericValue(e.getNumericValue());
            progress.setBooleanValue(e.getBooleanValue());
            progress.setLocked(true);

            // Mark CHECKBOX task as permanently done when student checks it
            if (task.getTaskType() == TaskType.CHECKBOX && Boolean.TRUE.equals(e.getBooleanValue())) {
                progress.setDonePermanently(true);
            }

            taskProgressRepository.save(progress);
        }

        return getDayEntries(assignmentId, date, student);
    }

    // ── Mentor unlock a single entry ──────────────────────────────────────────

    @Transactional
    public void unlockEntry(Long entryId, User mentor) {
        TaskProgress entry = taskProgressRepository.findById(entryId)
                .orElseThrow(() -> new EntityNotFoundException("Entry not found: " + entryId));
        entry.setLocked(false);
        entry.setDonePermanently(false);
        taskProgressRepository.save(entry);
    }

    // ── Legacy: review submission (kept for backwards compat) ─────────────────

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
                        .goal(goal).student(student).build());

        review.setCompleted(request.getCompleted());
        review.setComment(request.getComment());
        reviewRepository.save(review);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<ClassHomeworkAssignment> getAssignmentsForStudent(User student) {
        ClassStudent activeEnrollment = classStudentRepository.findByStudentAndActiveTrue(student)
                .orElse(null);
        if (activeEnrollment == null) return Collections.emptyList();

        return classHomeworkAssignmentRepository
                .findByMentorClassOrderByCreatedAtDesc(activeEnrollment.getMentorClass())
                .stream()
                .filter(a -> Boolean.TRUE.equals(a.getApplyToAll())
                          || a.getStudentIds().contains(student.getId()))
                .collect(Collectors.toList());
    }

    private ClassHomeworkAssignment assignmentRepository(Long assignmentId) {
        return classHomeworkAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found: " + assignmentId));
    }

    private void validateStudentAccess(ClassHomeworkAssignment assignment, User student) {
        boolean allowed = Boolean.TRUE.equals(assignment.getApplyToAll())
                || assignment.getStudentIds().contains(student.getId());
        if (!allowed) throw new ForbiddenException("You do not have access to this assignment");
    }

    private double computeOverallProgress(List<GoalTask> tasks, User student) {
        if (tasks.isEmpty()) return 0.0;
        double total = 0.0;
        for (GoalTask task : tasks) {
            if (task.getTaskType() == TaskType.NUMBER && task.getTargetValue() != null && task.getTargetValue() > 0) {
                Double sum = taskProgressRepository.sumNumericValueByTaskAndStudent(task, student);
                total += Math.min((sum / task.getTargetValue()) * 100.0, 100.0);
            } else if (task.getTaskType() == TaskType.CHECKBOX) {
                if (taskProgressRepository.existsByTaskAndStudentAndDonePermanentlyTrue(task, student)) {
                    total += 100.0;
                }
            }
        }
        return total / tasks.size();
    }
}
