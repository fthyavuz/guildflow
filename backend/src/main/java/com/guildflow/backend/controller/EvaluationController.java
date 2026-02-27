package com.guildflow.backend.controller;

import com.guildflow.backend.dto.EvaluationRequest;
import com.guildflow.backend.dto.EvaluationResponse;
import com.guildflow.backend.model.User;
import com.guildflow.backend.service.EvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluations")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<EvaluationResponse> createEvaluation(
            @Valid @RequestBody EvaluationRequest request,
            @AuthenticationPrincipal User mentor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evaluationService.createEvaluation(request, mentor));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<EvaluationResponse>> getStudentEvaluations(
            @PathVariable Long studentId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(evaluationService.getStudentEvaluations(studentId, user));
    }
}
