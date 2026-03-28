package com.guildflow.backend.controller;

import com.guildflow.backend.dto.ApproveTaskRequest;
import com.guildflow.backend.dto.DailyProgressEntry;
import com.guildflow.backend.dto.StudentReportResponse;
import com.guildflow.backend.model.User;
import com.guildflow.backend.service.StudentReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class StudentReportController {

    private final StudentReportService studentReportService;

    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<List<StudentReportResponse>> getStudentList() {
        return ResponseEntity.ok(studentReportService.getStudentList());
    }

    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<StudentReportResponse> getStudentReport(@PathVariable Long studentId) {
        return ResponseEntity.ok(studentReportService.getStudentReport(studentId));
    }

    @PostMapping("/students/{studentId}/assignments/{assignmentId}/tasks/{taskId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<Void> approveTask(
            @PathVariable Long studentId,
            @PathVariable Long assignmentId,
            @PathVariable Long taskId,
            @RequestBody(required = false) ApproveTaskRequest request,
            @AuthenticationPrincipal User approver) {
        studentReportService.approveTask(assignmentId, taskId, studentId, request, approver);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/students/{studentId}/chart")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<List<DailyProgressEntry>> getCategoryChart(
            @PathVariable Long studentId,
            @RequestParam String category,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        return ResponseEntity.ok(studentReportService.getCategoryChart(studentId, category, startDate, endDate));
    }

    @DeleteMapping("/students/{studentId}/assignments/{assignmentId}/tasks/{taskId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<Void> revokeApproval(
            @PathVariable Long studentId,
            @PathVariable Long assignmentId,
            @PathVariable Long taskId) {
        studentReportService.revokeApproval(assignmentId, taskId, studentId);
        return ResponseEntity.noContent().build();
    }
}
