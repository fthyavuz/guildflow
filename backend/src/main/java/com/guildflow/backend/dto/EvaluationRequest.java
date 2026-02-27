package com.guildflow.backend.dto;

import com.guildflow.backend.model.enums.EvaluationPeriod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EvaluationRequest {
    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Period is required")
    private EvaluationPeriod period;

    @NotBlank(message = "Content is required")
    private String content;

    private String periodName;
}
