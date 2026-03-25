package com.guildflow.backend.controller;

import com.guildflow.backend.dto.GoalReviewRequest;
import com.guildflow.backend.model.User;
import com.guildflow.backend.service.GoalProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goal-reviews")
@RequiredArgsConstructor
public class GoalReviewController {

    private final GoalProgressService goalProgressService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<Void> submitReview(
            @Valid @RequestBody GoalReviewRequest request,
            @AuthenticationPrincipal User mentor) {
        goalProgressService.submitReview(request, mentor);
        return ResponseEntity.ok().build();
    }
}
