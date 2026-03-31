package com.guildflow.backend.dto;

import com.guildflow.backend.model.Event;
import com.guildflow.backend.model.MentorClass;
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
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long createdById;
    private String createdByName;
    private String educationLevel;
    private List<Long> targetClassIds;
    private List<String> targetClassNames;

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
                .targetClassIds(event.getTargetClasses().stream().map(MentorClass::getId).collect(Collectors.toList()))
                .targetClassNames(event.getTargetClasses().stream().map(MentorClass::getName).collect(Collectors.toList()))
                .build();
    }
}
