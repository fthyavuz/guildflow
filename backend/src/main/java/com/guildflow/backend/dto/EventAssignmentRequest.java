package com.guildflow.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventAssignmentRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Duty description is required")
    private String dutyDescription;
}
