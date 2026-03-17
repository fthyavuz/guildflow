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
@SuppressWarnings("null")
@Transactional
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
        private final SourceRepository sourceRepository;

        // --- Goal Types ---

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

        @Transactional
        public GoalResponse createGoal(GoalRequest request, User creator) {
                if (creator == null || creator.getRole() == null) {
                        throw new RuntimeException("Access denied: Invalid user state");
                }

                MentorClass mentorClass = null;
                if (!request.isTemplate()) {
                        if (request.getClassId() == null) {
                                throw new RuntimeException("Class ID is required for non-template goals");
                        }
                        mentorClass = classRepository.findById(request.getClassId())
                                        .orElseThrow(() -> new RuntimeException("Class not found"));

                        User classMentor = mentorClass.getMentor();
                        if (creator.getRole() != Role.ADMIN && (classMentor == null || !classMentor.getId().equals(creator.getId()))) {
                                throw new RuntimeException("Access denied: You are not the mentor of this class");
                        }
                }

                GoalType goalType = goalTypeRepository.findById(request.getGoalTypeId())
                                .orElseThrow(() -> new RuntimeException("Goal type not found"));

                Goal goal = Goal.builder()
                                .title(request.getTitle())
                                .description(request.getDescription())
                                .mentorClass(mentorClass)
                                .goalType(goalType)
                                .applyToAll(request.isApplyToAll())
                                .isTemplate(request.isTemplate())
                                .startDate(request.getStartDate())
                                .endDate(request.getEndDate())
                                .createdBy(creator)
                                .build();

                // Save goal first to get ID
                Goal savedGoal = goalRepository.save(goal);

                // Add Tasks
                List<GoalTask> tasks = request.getTasks().stream()
                                .map(tr -> {
                                        Source source = null;
                                        if (tr.getSourceId() != null) {
                                                source = sourceRepository.findById(tr.getSourceId())
                                                                .orElse(null);
                                        }
                                        return GoalTask.builder()
                                                        .goal(savedGoal)
                                                        .title(tr.getTitle())
                                                        .description(tr.getDescription())
                                                        .taskType(tr.getTaskType())
                                                        .targetValue(tr.getTargetValue())
                                                        .sortOrder(tr.getSortOrder() != null ? tr.getSortOrder() : 0)
                                                        .source(source)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                goalTaskRepository.saveAll(tasks);
                savedGoal.setTasks(tasks);

                // Map Students if not applyToAll and not a template
                if (!request.isTemplate() && !request.isApplyToAll() && request.getStudentIds() != null) {
                        List<GoalStudent> studentMappings = request.getStudentIds().stream()
                                        .map(sid -> {
                                                User student = userRepository.findById(sid)
                                                                .orElseThrow(() -> new RuntimeException(
                                                                                "Student not found: " + sid));
                                                return GoalStudent.builder().goal(savedGoal).student(student).build();
                                        })
                                        .collect(Collectors.toList());
                        goalStudentRepository.saveAll(studentMappings);
                }

                return GoalResponse.fromEntity(savedGoal);
        }

        @Transactional
        public GoalResponse updateGoal(Long id, GoalRequest request, User user) {
                Goal goal = goalRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Goal not found"));

                // Only Admin or creator can update
                if (user.getRole() != Role.ADMIN && !goal.getCreatedBy().getId().equals(user.getId())) {
                        throw new RuntimeException("Access denied: You cannot update this goal");
                }

                GoalType goalType = goalTypeRepository.findById(request.getGoalTypeId())
                                .orElseThrow(() -> new RuntimeException("Goal type not found"));

                goal.setTitle(request.getTitle());
                goal.setDescription(request.getDescription());
                goal.setGoalType(goalType);
                goal.setApplyToAll(request.isApplyToAll());
                goal.setStartDate(request.getStartDate());
                goal.setEndDate(request.getEndDate());

                // Update tasks: Clear and add new ones (simpler for now)
                goal.getTasks().clear();
                List<GoalTask> newTasks = request.getTasks().stream()
                                .map(tr -> {
                                        Source source = null;
                                        if (tr.getSourceId() != null) {
                                                source = sourceRepository.findById(tr.getSourceId())
                                                                .orElse(null);
                                        }
                                        return GoalTask.builder()
                                                        .goal(goal)
                                                        .title(tr.getTitle())
                                                        .description(tr.getDescription())
                                                        .taskType(tr.getTaskType())
                                                        .targetValue(tr.getTargetValue())
                                                        .sortOrder(tr.getSortOrder() != null ? tr.getSortOrder() : 0)
                                                        .source(source)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                goal.getTasks().addAll(newTasks);

                // Update student mappings if not template
                if (!goal.getIsTemplate()) {
                        goalStudentRepository.deleteByGoal(goal);
                        if (!request.isApplyToAll() && request.getStudentIds() != null) {
                                List<GoalStudent> studentMappings = request.getStudentIds().stream()
                                                .map(sid -> {
                                                        User student = userRepository.findById(sid)
                                                                        .orElseThrow(() -> new RuntimeException(
                                                                                        "Student not found: " + sid));
                                                        return GoalStudent.builder().goal(goal).student(student).build();
                                                })
                                                .collect(Collectors.toList());
                                goalStudentRepository.saveAll(studentMappings);
                        }
                }

                return GoalResponse.fromEntity(goalRepository.save(goal));
        }

        public List<GoalResponse> getGoalsForClass(Long classId, User user) {
                MentorClass mentorClass = classRepository.findById(classId)
                                .orElseThrow(() -> new RuntimeException("Class not found"));

                return goalRepository.findByMentorClassAndActiveTrue(mentorClass).stream()
                                .map(GoalResponse::fromEntity)
                                .collect(Collectors.toList());
        }

        public List<GoalResponse> getTemplates() {
                return goalRepository.findByIsTemplateTrueAndActiveTrue().stream()
                                .map(GoalResponse::fromEntity)
                                .collect(Collectors.toList());
        }

        public GoalResponse getGoalById(Long id) {
                return goalRepository.findById(id)
                                .map(GoalResponse::fromEntity)
                                .orElseThrow(() -> new RuntimeException("Goal not found"));
        }

        // --- Student View ---

        public List<GoalResponse> getGoalsForStudent(User student) {
                // 1. Get student's current active class
                ClassStudent activeEnrollment = classStudentRepository.findByStudentAndActiveTrue(student)
                                .orElse(null);

                if (activeEnrollment == null || activeEnrollment.getMentorClass() == null)
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

        public List<GoalProgressResponse> getStudentGoalsWithProgress(User student) {
                List<GoalResponse> goals = getGoalsForStudent(student);

                return goals.stream().map(g -> {
                        List<TaskProgressResponse> taskProgresses = g.getTasks().stream().map(t -> {
                                GoalTask task = goalTaskRepository.findById(t.getId()).orElseThrow();
                                List<TaskProgress> entries = taskProgressRepository
                                                .findByTaskAndStudentOrderByEntryDateDesc(task, student);

                                Double currentVal = 0.0;
                                if (t.getTaskType() == com.guildflow.backend.model.enums.TaskType.NUMBER) {
                                        currentVal = entries.stream()
                                                        .mapToDouble(e -> e.getNumericValue() != null
                                                                        ? e.getNumericValue()
                                                                        : 0.0)
                                                        .sum();
                                } else if (t.getTaskType() == com.guildflow.backend.model.enums.TaskType.CHECKBOX) {
                                        currentVal = (double) entries.stream()
                                                        .filter(e -> Boolean.TRUE.equals(e.getBooleanValue()))
                                                        .count();
                                }

                                double percentage = t.getTargetValue() > 0 ? (currentVal / t.getTargetValue()) * 100
                                                : 0;
                                if (percentage > 100)
                                        percentage = 100;

                                return TaskProgressResponse.builder()
                                                .taskId(t.getId())
                                                .title(t.getTitle())
                                                .taskType(t.getTaskType())
                                                .targetValue(t.getTargetValue())
                                                .currentValue(currentVal)
                                                .progressPercentage(percentage)
                                                .build();
                        }).collect(Collectors.toList());

                        double overallProgress = taskProgresses.isEmpty() ? 0
                                        : taskProgresses.stream()
                                                        .mapToDouble(TaskProgressResponse::getProgressPercentage)
                                                        .average().orElse(0.0);

                        return GoalProgressResponse.builder()
                                        .goalId(g.getId())
                                        .title(g.getTitle())
                                        .tasks(taskProgresses)
                                        .overallProgress(overallProgress)
                                        .build();
                }).collect(Collectors.toList());
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

                if (mentor == null || mentor.getRole() == null) {
                        throw new RuntimeException("Access denied: Invalid user state");
                }

                User classMentor = goal.getMentorClass().getMentor();
                if (classMentor == null
                                || (mentor.getRole() != Role.ADMIN && !classMentor.getId().equals(mentor.getId()))) {
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

        @Transactional
        public void deleteGoal(Long id, User user) {
                Goal goal = goalRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Goal not found"));

                // Only Admin or the creator can delete templates
                if (user.getRole() != Role.ADMIN && !goal.getCreatedBy().getId().equals(user.getId())) {
                        throw new RuntimeException("Access denied: You cannot delete this goal");
                }

                // If it's a template, we can hard delete or deactivate
                // For simplicity and safety, let's just deactivate
                goal.setActive(false);
                goalRepository.save(goal);
        }

        @Transactional
        public GoalResponse assignGoalTemplate(GoalAssignmentRequest request, User creator) {
                Goal template = goalRepository.findById(request.getGoalId())
                                .orElseThrow(() -> new RuntimeException("Template not found"));

                if (!template.getIsTemplate()) {
                        throw new RuntimeException("Selected goal is not a template");
                }

                MentorClass mentorClass = null;
                if (request.getClassId() != null) {
                        mentorClass = classRepository.findById(request.getClassId())
                                        .orElseThrow(() -> new RuntimeException("Class not found"));
                }

                Goal newGoal = Goal.builder()
                                .title(template.getTitle())
                                .description(template.getDescription())
                                .mentorClass(mentorClass)
                                .goalType(template.getGoalType())
                                .applyToAll(request.isApplyToAll())
                                .isTemplate(false)
                                .startDate(request.getStartDate())
                                .endDate(request.getEndDate())
                                .createdBy(creator)
                                .build();

                Goal savedGoal = goalRepository.save(newGoal);

                // Clone tasks
                List<GoalTask> clonedTasks = template.getTasks().stream()
                                .map(t -> GoalTask.builder()
                                                .goal(savedGoal)
                                                .title(t.getTitle())
                                                .description(t.getDescription())
                                                .taskType(t.getTaskType())
                                                .targetValue(t.getTargetValue())
                                                .sortOrder(t.getSortOrder())
                                                .source(t.getSource())
                                                .build())
                                .collect(Collectors.toList());

                goalTaskRepository.saveAll(clonedTasks);
                savedGoal.setTasks(clonedTasks);

                // Assign to students if not applyToAll
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
}
