package com.guildflow.backend.dto;

import com.guildflow.backend.model.GoalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalTypeResponse {
    private Long id;
    private String name;
    private String description;

    public static GoalTypeResponse fromEntity(GoalType type) {
        return GoalTypeResponse.builder()
                .id(type.getId())
                .name(type.getName())
                .description(type.getDescription())
                .build();
    }
}
