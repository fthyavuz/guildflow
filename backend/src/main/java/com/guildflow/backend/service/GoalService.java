package com.guildflow.backend.service;

import com.guildflow.backend.dto.*;
import com.guildflow.backend.model.*;
import com.guildflow.backend.model.enums.Role;
import com.guildflow.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalTypeRepository goalTypeRepository;
    private final GoalRepository goalRepository;
    private final GoalTaskRepository goalTaskRepository;
    private final GoalStudentRepository goalStudentRepository;
    private final TaskProgressRepository taskProgressRepository;
    private final GoalStudentReviewRepository reviewRepository;
    private final MentorClassRepository classRepository;
    private final UserRepository userRepository;
    private final ClassStudentRepository classStudentRepository;

    // --- Goal Types (Admin only managed via Controller) ---

    public List<GoalTypeResponse> getAllGoalTypes() {
        return goalTypeRepository.findByActiveTrue().stream()
                .map(GoalTypeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public GoalTypeResponse createGoalType(String name, String description) {
        GoalType type = GoalType.builder()
                .name(name)
                .description(description)
                .build();
        return GoalTypeResponse.fromEntity(goalTypeRepository.save(type));
    }

    // --- Goals (Mentor) ---

    @Transactional
    public GoalResponse createGoal(GoalRequest request, User mentor) {
        MentorClass mentorClass = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found"));

        // Security check
        if (mentor.getRole() != Role.ADMIN && !mentorClass.getMentor().getId().equals(mentor.getId())) {
            throw new RuntimeException("Access denied: You are not the mentor of this class");
        }

        GoalType goalType = goalTypeRepository.findById(request.getGoalTypeId())
                .orElseThrow(() -> new RuntimeException("Goal type not found"));

        Goal goal = Goal.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .mentorClass(mentorClass)
                .goalType(goalType)
                .applyToAll(request.isApplyToAll())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        // Save goal first to get ID
        Goal savedGoal = goalRepository.save(goal);

        // Add Tasks
        List<GoalTask> tasks = request.getTasks().stream()
                .map(tr -> GoalTask.builder()
                        .goal(savedGoal)
                        .title(tr.getTitle())
                        .description(tr.getDescription())
                        .taskType(tr.getTaskType())
                        .targetValue(tr.getTargetValue())
                        .sortOrder(tr.getSortOrder() != null ? tr.getSortOrder() : 0)
                        .build())
                .collect(Collectors.toList());

        goalTaskRepository.saveAll(tasks);
        savedGoal.setTasks(tasks);

        // Map Students if not applyToAll
        if (!request.isApplyToAll() && request.getStudentIds() != null) {
            List<GoalStudent> studentMappings = request.getStudentIds().stream()
                    .map(sid -> {
                        User student = userRepository.findById(sid)
                                .orElseThrow(() -> new RuntimeException("Student not found: " + sid));
                        return GoalStudent.builder().goal(savedGoal).student(student).build();
                    })
                    .collect(Collectors.toList());
            goalStudentRepository.saveAll(studentMappings);
        }

        return GoalResponse.fromEntity(savedGoal);
    }

    public List<GoalResponse> getGoalsForClass(Long classId, User user) {
        MentorClass mentorClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        return goalRepository.findByMentorClassAndActiveTrue(mentorClass).stream()
                .map(GoalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // --- Student View ---

    public List<GoalResponse> getGoalsForStudent(User student) {
        // 1. Get student's current active class
        ClassStudent activeEnrollment = classStudentRepository.findByStudentAndActiveTrue(student)
                .orElse(null);

        if (activeEnrollment == null)
            return Collections.emptyList();

        // 2. Fetch "applyToAll" goals for that class
        List<Goal> classGoals = goalRepository.findByMentorClassAndActiveTrue(activeEnrollment.getMentorClass())
                .stream().filter(Goal::getApplyToAll).collect(Collectors.toList());

        // 3. Fetch specific mappings (when NOT applyToAll)
        List<Goal> privateGoals = goalStudentRepository.findByStudent(student)
                .stream().map(GoalStudent::getGoal).collect(Collectors.toList());

        // 4. Combine and convert
        classGoals.addAll(privateGoals);
        return classGoals.stream().distinct().map(GoalResponse::fromEntity).collect(Collectors.toList());
    }

    // --- Progress (Student) ---

    @Transactional
    public void submitProgress(ProgressRequest request, User student) {
        GoalTask task = goalTaskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // Find or create daily progress
        TaskProgress progress = taskProgressRepository
                .findByTaskAndStudentAndEntryDate(task, student, request.getEntryDate())
                .orElse(TaskProgress.builder()
                        .task(task)
                        .student(student)
                        .entryDate(request.getEntryDate())
                        .build());

        progress.setNumericValue(request.getNumericValue());
        progress.setBooleanValue(request.getBooleanValue());

        taskProgressRepository.save(progress);
    }

    // --- Mentor Review ---

    @Transactional
    public void submitReview(GoalReviewRequest request, User mentor) {
        Goal goal = goalRepository.findById(request.getGoalId())
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        if (mentor.getRole() != Role.ADMIN && !goal.getMentorClass().getMentor().getId().equals(mentor.getId())) {
            throw new RuntimeException("Access denied: You are not the mentor of this goal");
        }

        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

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
}
