package com.guildflow.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ClassHomeworkAssignmentRequest {
    @NotNull(message = "Goal (template) ID is required")
    private Long goalId;
    private String frequency;   // DAILY | WEEKLY | null
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean applyToAll = true;
    private List<Long> studentIds;
}
