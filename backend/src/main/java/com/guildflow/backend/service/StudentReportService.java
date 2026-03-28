package com.guildflow.backend.service;

import com.guildflow.backend.dto.ApproveTaskRequest;
import com.guildflow.backend.dto.StudentReportResponse;
import com.guildflow.backend.exception.EntityNotFoundException;
import com.guildflow.backend.model.*;
import com.guildflow.backend.model.enums.Role;
import com.guildflow.backend.model.enums.TaskType;
import com.guildflow.backend.repository.*;
import com.guildflow.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class StudentReportService {

    private final UserRepository userRepository;
    private final ClassStudentRepository classStudentRepository;
    private final ClassHomeworkAssignmentRepository assignmentRepository;
    private final GoalTaskRepository goalTaskRepository;
    private final TaskProgressRepository taskProgressRepository;
    private final TaskCompletionRepository taskCompletionRepository;
    private final SecurityUtils securityUtils;

    public List<StudentReportResponse> getStudentList() {
        List<User> students = userRepository.findByRole(Role.STUDENT)
                .stream().filter(u -> Boolean.TRUE.equals(u.getActive()))
                .collect(Collectors.toList());

        return students.stream().map(s -> StudentReportResponse.builder()
                .studentId(s.getId())
                .firstName(s.getFirstName())
                .lastName(s.getLastName())
                .email(s.getEmail())
                .assignments(Collections.emptyList())
                .build()).collect(Collectors.toList());
    }

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

        // Pre-fetch all completions for this student in one query
        Map<String, TaskCompletion> completionMap = taskCompletionRepository
                .findByAssignments(assignments)
                .stream()
                .filter(tc -> tc.getStudent().getId().equals(studentId))
                .collect(Collectors.toMap(
                        tc -> tc.getAssignment().getId() + "_" + tc.getTask().getId(),
                        tc -> tc));

        List<StudentReportResponse.AssignmentReport> reportItems = assignments.stream().map(a -> {
            List<GoalTask> tasks = a.getGoal().getTasks();

            List<StudentReportResponse.TaskReport> taskReports = tasks.stream()
                    .sorted(Comparator.comparing(GoalTask::getSortOrder))
                    .map(task -> buildTaskReport(task, student, a, completionMap))
                    .collect(Collectors.toList());

            return StudentReportResponse.AssignmentReport.builder()
                    .assignmentId(a.getId())
                    .title(a.getGoal().getTitle())
                    .startDate(a.getStartDate())
                    .endDate(a.getEndDate())
                    .frequency(a.getFrequency() != null ? a.getFrequency().name() : null)
                    .tasks(taskReports)
                    .build();
        }).collect(Collectors.toList());

        return StudentReportResponse.builder()
                .studentId(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .assignments(reportItems)
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

    // ── Helpers ───────────────────────────────────────────────────────────────

    private StudentReportResponse.TaskReport buildTaskReport(GoalTask task, User student,
            ClassHomeworkAssignment assignment,
            Map<String, TaskCompletion> completionMap) {

        double currentValue = 0.0;
        if (task.getTaskType() == TaskType.NUMBER) {
            currentValue = taskProgressRepository.sumNumericValueByTaskAndStudent(task, student);
        } else if (task.getTaskType() == TaskType.CHECKBOX) {
            currentValue = taskProgressRepository
                    .existsByTaskAndStudentAndDonePermanentlyTrue(task, student) ? 1.0 : 0.0;
        }

        double target = task.getTargetValue() != null ? task.getTargetValue() : 1.0;
        double percentage = Math.min((currentValue / target) * 100.0, 100.0);

        TaskCompletion completion = completionMap.get(assignment.getId() + "_" + task.getId());
        boolean approved = completion != null;

        return StudentReportResponse.TaskReport.builder()
                .taskId(task.getId())
                .title(task.getTitle())
                .taskType(task.getTaskType().name())
                .targetValue(task.getTargetValue())
                .currentValue(currentValue)
                .progressPercentage(percentage)
                .approved(approved)
                .approvedAt(completion != null ? completion.getApprovedAt() : null)
                .approvedByName(completion != null
                        ? completion.getApprovedBy().getFirstName() + " " + completion.getApprovedBy().getLastName()
                        : null)
                .approverNotes(completion != null ? completion.getNotes() : null)
                .build();
    }

    private StudentReportResponse buildEmptyReport(User student) {
        return StudentReportResponse.builder()
                .studentId(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .assignments(Collections.emptyList())
                .build();
    }
}
