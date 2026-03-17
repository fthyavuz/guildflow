package com.guildflow.backend.dto;

import com.guildflow.backend.model.enums.SourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SourceRequest {
    @NotBlank
    private String title;

    @NotNull
    private SourceType type;

    private String language;
    private String part;
    private Integer totalPages;
    private Integer totalMinutes;
}
