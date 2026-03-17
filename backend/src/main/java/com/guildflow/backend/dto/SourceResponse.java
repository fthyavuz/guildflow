package com.guildflow.backend.dto;

import com.guildflow.backend.model.Source;
import com.guildflow.backend.model.enums.SourceType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SourceResponse {
    private Long id;
    private String title;
    private SourceType type;
    private String language;
    private String part;
    private Integer totalPages;
    private Integer totalMinutes;

    public static SourceResponse fromEntity(Source source) {
        if (source == null) return null;
        return SourceResponse.builder()
                .id(source.getId())
                .title(source.getTitle())
                .type(source.getType())
                .language(source.getLanguage())
                .part(source.getPart())
                .totalPages(source.getTotalPages())
                .totalMinutes(source.getTotalMinutes())
                .build();
    }
}
