package com.guildflow.backend.dto;

import com.guildflow.backend.model.Goal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalResponse {
    private Long id;
    private String title;
    private String description;
    private Long classId;
    private String className;
    private Long goalTypeId;
    private String goalTypeName;
    private boolean applyToAll;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private List<TaskResponse> tasks;

    public static GoalResponse fromEntity(Goal goal) {
        return GoalResponse.builder()
                .id(goal.getId())
                .title(goal.getTitle())
                .description(goal.getDescription())
                .classId(goal.getMentorClass().getId())
                .className(goal.getMentorClass().getName())
                .goalTypeId(goal.getGoalType().getId())
                .goalTypeName(goal.getGoalType().getName())
                .applyToAll(goal.getApplyToAll())
                .startDate(goal.getStartDate())
                .endDate(goal.getEndDate())
                .createdAt(goal.getCreatedAt())
                .tasks(goal.getTasks().stream()
                        .map(TaskResponse::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }
}
