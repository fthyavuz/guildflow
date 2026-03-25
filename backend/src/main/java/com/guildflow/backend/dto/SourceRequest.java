package com.guildflow.backend.dto;

import com.guildflow.backend.model.enums.TrackingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SourceRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Tracking type is required")
    private TrackingType trackingType;

    private Double totalCapacity;
    private Double dailyLimit;
    private String language;
    private String part;
}
