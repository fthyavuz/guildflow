package com.guildflow.backend.dto;

import com.guildflow.backend.model.GoalTask;
import com.guildflow.backend.model.enums.TaskType;
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
public class TaskRequest {
    @NotBlank(message = "Task title is required")
    private String title;
    private String description;
    @NotNull(message = "Task type is required")
    private TaskType taskType;
    private Double targetValue;
    private Integer sortOrder;
}
