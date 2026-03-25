package com.guildflow.backend.dto;

import com.guildflow.backend.model.ResourceCategory;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResourceCategoryResponse {

    private Long id;
    private String name;
    private String description;
    private Boolean active;

    public static ResourceCategoryResponse fromEntity(ResourceCategory cat) {
        return ResourceCategoryResponse.builder()
                .id(cat.getId())
                .name(cat.getName())
                .description(cat.getDescription())
                .active(cat.getActive())
                .build();
    }
}
