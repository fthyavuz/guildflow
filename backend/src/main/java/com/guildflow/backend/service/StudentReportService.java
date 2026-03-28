package com.guildflow.backend.service;

import com.guildflow.backend.dto.ApproveTaskRequest;
import com.guildflow.backend.dto.StudentReportResponse;
import com.guildflow.backend.dto.StudentReportResponse.CategorySection;
import com.guildflow.backend.dto.StudentReportResponse.TaskItem;
import com.guildflow.backend.exception.EntityNotFoundException;
import com.guildflow.backend.model.*;
import com.guildflow.backend.model.enums.Role;
import com.guildflow.backend.model.enums.TaskType;
import com.guildflow.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentReportService {

    private static final String GENERAL_CATEGORY = "General";

    private final UserRepository userRepository;
    private final ClassStudentRepository classStudentRepository;
    private final ClassHomeworkAssignmentRepository assignmentRepository;
    private final GoalTaskRepository goalTaskRepository;
    private final TaskProgressRepository taskProgressRepository;
    private final TaskCompletionRepository taskCompletionRepository;

    public List<StudentReportResponse> getStudentList() {
        return userRepository.findByRole(Role.STUDENT).stream()
                .filter(u -> Boolean.TRUE.equals(u.getActive()))
                .map(s -> StudentReportResponse.builder()
                        .studentId(s.getId())
                        .firstName(s.getFirstName())
                        .lastName(s.getLastName())
                        .email(s.getEmail())
                        .inProgress(Collections.emptyList())
                        .finished(Collections.emptyList())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentReportResponse getStudentReport(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + studentId));

        ClassStudent enrollment = classStudentRepository.findByStudentAndActiveTrue(student)
                .orElse(null);

        if (enrollment == null) {
            return buildEmptyReport(student);
        }

        List<ClassHomeworkAssignment> assignments = assignmentRepository
                .findByMentorClassOrderByCreatedAtDesc(enrollment.getMentorClass())
                .stream()
                .filter(a -> Boolean.TRUE.equals(a.getApplyToAll())
                          || a.getStudentIds().contains(student.getId()))
                .collect(Collectors.toList());

        if (assignments.isEmpty()) {
            return buildEmptyReport(student);
        }

        Map<String, TaskCompletion> completionMap = taskCompletionRepository
                .findByAssignments(assignments)
                .stream()
                .filter(tc -> tc.getStudent().getId().equals(studentId))
                .collect(Collectors.toMap(
                        tc -> tc.getAssignment().getId() + "_" + tc.getTask().getId(),
                        tc -> tc,
                        (a, b) -> a));

        Map<String, List<TaskItem>> inProgressByCategory = new TreeMap<>();
        Map<String, List<TaskItem>> finishedByCategory = new TreeMap<>();
        Map<String, Long> categoryIdByName = new HashMap<>();

        for (ClassHomeworkAssignment assignment : assignments) {
            assignment.getGoal().getTasks().stream()
                    .sorted(Comparator.comparing(GoalTask::getSortOrder))
                    .forEach(task -> {
                        double current = computeCurrentValue(task, student);
                        double target = task.getTargetValue() != null ? task.getTargetValue() : 1.0;
                        double pct = Math.min((current / target) * 100.0, 100.0);

                        String catName = resolveCategoryName(task);
                        Long catId = resolveCategoryId(task);
                        if (catId != null) categoryIdByName.put(catName, catId);

                        TaskCompletion completion =
                                completionMap.get(assignment.getId() + "_" + task.getId());

                        TaskItem item = TaskItem.builder()
                                .taskId(task.getId())
                                .assignmentId(assignment.getId())
                                .taskTitle(task.getTitle())
                                .assignmentTitle(assignment.getGoal().getTitle())
                                .taskType(task.getTaskType().name())
                                .targetValue(task.getTargetValue())
                                .currentValue(current)
                                .progressPercentage(pct)
                                .approved(completion != null)
                                .approvedAt(completion != null ? completion.getApprovedAt() : null)
                                .approvedByName(completion != null
                                        ? completion.getApprovedBy().getFirstName() + " "
                                          + completion.getApprovedBy().getLastName()
                                        : null)
                                .approverNotes(completion != null ? completion.getNotes() : null)
                                .build();

                        if (pct >= 100.0) {
                            finishedByCategory.computeIfAbsent(catName, k -> new ArrayList<>()).add(item);
                        } else {
                            inProgressByCategory.computeIfAbsent(catName, k -> new ArrayList<>()).add(item);
                        }
                    });
        }

        return StudentReportResponse.builder()
                .studentId(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .inProgress(buildSections(inProgressByCategory, categoryIdByName))
                .finished(buildSections(finishedByCategory, categoryIdByName))
                .build();
    }

    @Transactional
    public void approveTask(Long assignmentId, Long taskId, Long studentId,
                            ApproveTaskRequest request, User approver) {
        ClassHomeworkAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found: " + assignmentId));
        GoalTask task = goalTaskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found: " + taskId));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + studentId));

        TaskCompletion completion = taskCompletionRepository
                .findByAssignmentAndTaskAndStudent(assignment, task, student)
                .orElse(TaskCompletion.builder()
                        .assignment(assignment).task(task).student(student)
                        .build());

        completion.setApprovedBy(approver);
        completion.setNotes(request != null ? request.getNotes() : null);
        taskCompletionRepository.save(completion);
    }

    @Transactional
    public void revokeApproval(Long assignmentId, Long taskId, Long studentId) {
        ClassHomeworkAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found: " + assignmentId));
        GoalTask task = goalTaskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found: " + taskId));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + studentId));

        taskCompletionRepository.findByAssignmentAndTaskAndStudent(assignment, task, student)
                .ifPresent(taskCompletionRepository::delete);
    }

    private double computeCurrentValue(GoalTask task, User student) {
        if (task.getTaskType() == TaskType.NUMBER) {
            return taskProgressRepository.sumNumericValueByTaskAndStudent(task, student);
        }
        return taskProgressRepository.existsByTaskAndStudentAndDonePermanentlyTrue(task, student)
                ? 1.0 : 0.0;
    }

    private String resolveCategoryName(GoalTask task) {
        if (task.getSource() != null && task.getSource().getCategory() != null) {
            return task.getSource().getCategory().getName();
        }
        return GENERAL_CATEGORY;
    }

    private Long resolveCategoryId(GoalTask task) {
        if (task.getSource() != null && task.getSource().getCategory() != null) {
            return task.getSource().getCategory().getId();
        }
        return null;
    }

    private List<CategorySection> buildSections(Map<String, List<TaskItem>> byCategory,
                                                 Map<String, Long> categoryIdByName) {
        List<String> keys = new ArrayList<>(byCategory.keySet());
        keys.remove(GENERAL_CATEGORY);
        Collections.sort(keys);
        if (byCategory.containsKey(GENERAL_CATEGORY)) keys.add(GENERAL_CATEGORY);
        return keys.stream()
                .map(name -> CategorySection.builder()
                        .categoryId(categoryIdByName.get(name))
                        .categoryName(name)
                        .tasks(byCategory.get(name))
                        .build())
                .collect(Collectors.toList());
    }

    private StudentReportResponse buildEmptyReport(User student) {
        return StudentReportResponse.builder()
                .studentId(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .inProgress(Collections.emptyList())
                .finished(Collections.emptyList())
                .build();
    }
}
