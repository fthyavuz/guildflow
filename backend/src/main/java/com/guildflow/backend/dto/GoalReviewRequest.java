package com.guildflow.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GoalReviewRequest {
    @NotNull(message = "Goal ID is required")
    private Long goalId;

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Completion status is required")
    private Boolean completed;

    private String comment;
}
