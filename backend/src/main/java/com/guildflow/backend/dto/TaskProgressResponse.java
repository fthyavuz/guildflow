package com.guildflow.backend.dto;

import com.guildflow.backend.model.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskProgressResponse {
    private Long taskId;
    private String title;
    private TaskType taskType;
    private Double targetValue;
    private Double currentValue;
    private Double progressPercentage;
}
