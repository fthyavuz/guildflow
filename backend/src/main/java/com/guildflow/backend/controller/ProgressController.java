package com.guildflow.backend.controller;

import com.guildflow.backend.dto.ProgressRequest;
import com.guildflow.backend.model.User;
import com.guildflow.backend.service.GoalService;
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
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final GoalService goalService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> submitProgress(
            @Valid @RequestBody ProgressRequest request,
            @AuthenticationPrincipal User student) {
        goalService.submitProgress(request, student);
        return ResponseEntity.ok().build();
    }
}
