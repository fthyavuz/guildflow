package com.guildflow.backend.dto;

import com.guildflow.backend.model.GoalTask;
import com.guildflow.backend.model.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskType taskType;
    private Double targetValue;
    private Integer sortOrder;

    public static TaskResponse fromEntity(GoalTask task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .taskType(task.getTaskType())
                .targetValue(task.getTargetValue())
                .sortOrder(task.getSortOrder())
                .build();
    }
}
