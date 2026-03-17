package com.guildflow.backend.dto;

import com.guildflow.backend.model.EventAssignment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventAssignmentResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String dutyDescription;
    private LocalDateTime assignedAt;

    public static EventAssignmentResponse fromEntity(EventAssignment assignment) {
        return EventAssignmentResponse.builder()
                .id(assignment.getId())
                .userId(assignment.getResponsibleUser().getId())
                .userName(assignment.getResponsibleUser().getFirstName() + " " + assignment.getResponsibleUser().getLastName())
                .dutyDescription(assignment.getDutyDescription())
                .assignedAt(assignment.getAssignedAt())
                .build();
    }
}
