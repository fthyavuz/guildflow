package com.guildflow.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GoalRequest {
    @NotBlank(message = "Goal title is required")
    private String title;
    private String description;
    @NotNull(message = "Class ID is required")
    private Long classId;
    @NotNull(message = "Goal type ID is required")
    private Long goalTypeId;
    private boolean applyToAll = true;
    private List<Long> studentIds; // Only used if applyToAll is false
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Valid
    @NotNull(message = "Tasks are required")
    private List<TaskRequest> tasks;
}
