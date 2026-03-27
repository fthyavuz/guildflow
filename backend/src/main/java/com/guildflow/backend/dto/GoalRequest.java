package com.guildflow.backend.dto;

import com.guildflow.backend.model.enums.Frequency;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private Long classId; // Optional for templates
    private Long goalTypeId; // Optional — category removed from template form
    private boolean applyToAll = true;
    @JsonProperty("isTemplate")
    private boolean isTemplate = false;
    private List<Long> studentIds; // Only used if applyToAll is false
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Frequency frequency;

    @Valid
    @NotNull(message = "Tasks are required")
    private List<TaskRequest> tasks;
}
