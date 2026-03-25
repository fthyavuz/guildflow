package com.guildflow.backend.controller;

import com.guildflow.backend.dto.PendingProgressResponse;
import com.guildflow.backend.dto.ProgressApprovalRequest;
import com.guildflow.backend.dto.ProgressRequest;
import com.guildflow.backend.model.User;
import com.guildflow.backend.service.GoalProgressService;
import com.guildflow.backend.service.ProgressApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final GoalProgressService goalProgressService;
    private final ProgressApprovalService progressApprovalService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> submitProgress(
            @Valid @RequestBody ProgressRequest request,
            @AuthenticationPrincipal User student) {
        goalProgressService.submitProgress(request, student);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<List<PendingProgressResponse>> getPendingEntries() {
        return ResponseEntity.ok(progressApprovalService.getPendingEntries());
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<Void> approve(
            @PathVariable Long id,
            @RequestBody(required = false) ProgressApprovalRequest body,
            @AuthenticationPrincipal User mentor) {
        ProgressApprovalRequest req = body != null ? body : new ProgressApprovalRequest();
        req.setEntryId(id);
        progressApprovalService.approve(req, mentor);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<Void> reject(
            @PathVariable Long id,
            @RequestBody(required = false) ProgressApprovalRequest body,
            @AuthenticationPrincipal User mentor) {
        ProgressApprovalRequest req = body != null ? body : new ProgressApprovalRequest();
        req.setEntryId(id);
        progressApprovalService.reject(req, mentor);
        return ResponseEntity.ok().build();
    }
}
