package com.guildflow.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MeetingRequest {
    @NotNull(message = "Class ID is required")
    private Long classId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    private String location;

    private boolean recurring = false;

    /** Number of weekly occurrences to create when recurring is true. Defaults to 13 (~3 months). */
    @Min(value = 1, message = "Recurrence count must be at least 1")
    @Max(value = 52, message = "Recurrence count cannot exceed 52 weeks")
    private int recurrenceCount = 13;
}
