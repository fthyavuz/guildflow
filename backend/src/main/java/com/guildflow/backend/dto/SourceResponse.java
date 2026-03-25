package com.guildflow.backend.dto;

import com.guildflow.backend.model.Source;
import com.guildflow.backend.model.enums.TrackingType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SourceResponse {

    private Long id;
    private String title;
    private Long categoryId;
    private String categoryName;
    private TrackingType trackingType;
    private Double totalCapacity;
    private Double dailyLimit;
    private String language;
    private String part;

    public static SourceResponse fromEntity(Source source) {
        if (source == null) return null;
        return SourceResponse.builder()
                .id(source.getId())
                .title(source.getTitle())
                .categoryId(source.getCategory() != null ? source.getCategory().getId() : null)
                .categoryName(source.getCategory() != null ? source.getCategory().getName() : null)
                .trackingType(source.getTrackingType())
                .totalCapacity(source.getTotalCapacity())
                .dailyLimit(source.getDailyLimit())
                .language(source.getLanguage())
                .part(source.getPart())
                .build();
    }
}
