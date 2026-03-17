package com.guildflow.backend.dto;

import com.guildflow.backend.model.EventParticipant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventParticipantResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String role;
    private Boolean isGoing;
    private LocalDateTime respondedAt;

    public static EventParticipantResponse fromEntity(EventParticipant participant) {
        return EventParticipantResponse.builder()
                .id(participant.getId())
                .userId(participant.getUser().getId())
                .userName(participant.getUser().getFirstName() + " " + participant.getUser().getLastName())
                .role(participant.getUser().getRole().name())
                .isGoing(participant.getIsGoing())
                .respondedAt(participant.getRespondedAt())
                .build();
    }
}
