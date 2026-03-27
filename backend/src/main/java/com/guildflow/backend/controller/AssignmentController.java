package com.guildflow.backend.controller;

import com.guildflow.backend.dto.ClassHomeworkAssignmentRequest;
import com.guildflow.backend.dto.ClassHomeworkAssignmentResponse;
import com.guildflow.backend.model.User;
import com.guildflow.backend.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes/{classId}/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<List<ClassHomeworkAssignmentResponse>> getAssignments(
            @PathVariable Long classId) {
        return ResponseEntity.ok(assignmentService.getClassAssignments(classId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<ClassHomeworkAssignmentResponse> createAssignment(
            @PathVariable Long classId,
            @Valid @RequestBody ClassHomeworkAssignmentRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assignmentService.createAssignment(classId, request, user));
    }

    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long classId,
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User user) {
        assignmentService.deleteAssignment(classId, assignmentId, user);
        return ResponseEntity.noContent().build();
    }
}
