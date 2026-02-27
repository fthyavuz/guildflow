package com.guildflow.backend.controller;

import com.guildflow.backend.dto.GoalRequest;
import com.guildflow.backend.dto.GoalResponse;
import com.guildflow.backend.model.User;
import com.guildflow.backend.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<GoalResponse> createGoal(
            @Valid @RequestBody GoalRequest request,
            @AuthenticationPrincipal User mentor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goalService.createGoal(request, mentor));
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<List<GoalResponse>> getGoalsForClass(
            @PathVariable Long classId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(goalService.getGoalsForClass(classId, user));
    }

    @GetMapping("/my-goals")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<GoalResponse>> getMyGoals(@AuthenticationPrincipal User student) {
        return ResponseEntity.ok(goalService.getGoalsForStudent(student));
    }
}
