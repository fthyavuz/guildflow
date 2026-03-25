package com.guildflow.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResourceCategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;
}
