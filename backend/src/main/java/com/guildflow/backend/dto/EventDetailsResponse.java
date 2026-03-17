package com.guildflow.backend.dto;

import com.guildflow.backend.model.Event;
import com.guildflow.backend.model.EventAssignment;
import com.guildflow.backend.model.EventParticipant;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class EventDetailsResponse extends EventResponse {

    private List<EventParticipantResponse> participants;
    private List<EventAssignmentResponse> assignments;
    private Boolean userGoingStatus; // null if not responded, true if going, false if not

    public static EventDetailsResponse fromEntity(Event event, List<EventParticipant> participants, List<EventAssignment> assignments, Boolean currentUserGoingStatus) {
        EventDetailsResponse response = new EventDetailsResponse();
        
        // Copy base fields
        response.setId(event.getId());
        response.setTitle(event.getTitle());
        response.setDescription(event.getDescription());
        response.setStartTime(event.getStartTime());
        response.setEndTime(event.getEndTime());
        response.setCreatedById(event.getCreatedBy().getId());
        response.setCreatedByName(event.getCreatedBy().getFirstName() + " " + event.getCreatedBy().getLastName());
        
        // Add detailed fields
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
