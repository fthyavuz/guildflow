package com.guildflow.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GoalAssignmentRequest {
    @NotNull(message = "Goal ID is required")
    private Long goalId; // The template ID

    private Long classId; // Optional if assigned to specific students
    private List<Long> studentIds; // Optional if assigned to whole class
    
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    
    @NotNull(message = "End date is required")
    private LocalDateTime endDate;
    
    private boolean applyToAll = true;
}
