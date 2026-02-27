package com.guildflow.backend.dto;

import com.guildflow.backend.model.enums.EducationLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateClassRequest {

    @NotBlank(message = "Class name is required")
    private String name;

    @NotNull(message = "Education level is required")
    private EducationLevel educationLevel;

    private String description;
}
