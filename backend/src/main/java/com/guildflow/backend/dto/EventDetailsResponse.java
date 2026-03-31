package com.guildflow.backend.dto;

import com.guildflow.backend.model.Event;
import com.guildflow.backend.model.EventAssignment;
import com.guildflow.backend.model.EventParticipant;
import com.guildflow.backend.model.MentorClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class EventDetailsResponse extends EventResponse {

    private List<EventParticipantResponse> participants;
    private List<EventAssignmentResponse> assignments;
    private Boolean userGoingStatus;

    public static EventDetailsResponse fromEntity(Event event, List<EventParticipant> participants, List<EventAssignment> assignments, Boolean currentUserGoingStatus) {
        EventDetailsResponse response = new EventDetailsResponse();

        response.setId(event.getId());
        response.setTitle(event.getTitle());
        response.setDescription(event.getDescription());
        response.setStartTime(event.getStartTime());
        response.setEndTime(event.getEndTime());
        response.setCreatedById(event.getCreatedBy().getId());
        response.setCreatedByName(event.getCreatedBy().getFirstName() + " " + event.getCreatedBy().getLastName());
        response.setEducationLevel(event.getEducationLevel() != null ? event.getEducationLevel().name() : null);
        response.setTargetClassIds(event.getTargetClasses().stream().map(MentorClass::getId).collect(Collectors.toList()));
        response.setTargetClassNames(event.getTargetClasses().stream().map(MentorClass::getName).collect(Collectors.toList()));

        response.setParticipants(participants.stream()
                .map(EventParticipantResponse::fromEntity)
                .collect(Collectors.toList()));

        response.setAssignments(assignments.stream()
                .map(EventAssignmentResponse::fromEntity)
                .collect(Collectors.toList()));

        response.setUserGoingStatus(currentUserGoingStatus);

        return response;
    }
}
