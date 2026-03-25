package com.guildflow.backend.dto;

import com.guildflow.backend.model.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long createdById;
    private String createdByName;
    private String educationLevel;
    private Long targetClassId;
    private String targetClassName;

    public static EventResponse fromEntity(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .createdById(event.getCreatedBy().getId())
                .createdByName(event.getCreatedBy().getFirstName() + " " + event.getCreatedBy().getLastName())
                .educationLevel(event.getEducationLevel() != null ? event.getEducationLevel().name() : null)
                .targetClassId(event.getTargetClass() != null ? event.getTargetClass().getId() : null)
                .targetClassName(event.getTargetClass() != null ? event.getTargetClass().getName() : null)
                .build();
    }
}
