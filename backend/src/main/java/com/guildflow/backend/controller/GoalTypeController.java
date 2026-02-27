package com.guildflow.backend.controller;

import com.guildflow.backend.dto.GoalTypeResponse;
import com.guildflow.backend.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goal-types")
@RequiredArgsConstructor
public class GoalTypeController {

    private final GoalService goalService;

    @GetMapping
    public ResponseEntity<List<GoalTypeResponse>> getAllTypes() {
        return ResponseEntity.ok(goalService.getAllGoalTypes());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GoalTypeResponse> createType(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String description = request.get("description");
        return ResponseEntity.ok(goalService.createGoalType(name, description));
    }
}
