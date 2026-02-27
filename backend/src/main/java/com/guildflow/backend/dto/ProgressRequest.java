package com.guildflow.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProgressRequest {
    @NotNull(message = "Task ID is required")
    private Long taskId;

    @NotNull(message = "Entry date is required")
    private LocalDate entryDate;

    private Double numericValue;
    private Boolean booleanValue;
}
