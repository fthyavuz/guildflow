package com.guildflow.backend.service;

import com.guildflow.backend.dto.*;
import com.guildflow.backend.model.Event;
import com.guildflow.backend.model.EventAssignment;
import com.guildflow.backend.model.EventParticipant;
import com.guildflow.backend.model.User;
import com.guildflow.backend.repository.EventAssignmentRepository;
import com.guildflow.backend.repository.EventParticipantRepository;
import com.guildflow.backend.repository.EventRepository;
import com.guildflow.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class EventService {

    private final EventRepository eventRepository;
    private final EventParticipantRepository participantRepository;
    private final EventAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    /**
     * Get upcoming events.
     */
    public List<EventResponse> getUpcomingEvents() {
        List<Event> events = eventRepository.findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime.now().minusDays(1));
        return events.stream()
                .map(EventResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get detailed event information including participants and assignments.
     */
    public EventDetailsResponse getEventDetails(Long eventId, String currentUserEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        List<EventParticipant> participants = participantRepository.findByEvent(event);
        List<EventAssignment> assignments = assignmentRepository.findByEvent(event);

        Boolean currentUserStatus = null;
        if (currentUserEmail != null) {
            User currentUser = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Optional<EventParticipant> participantInfo = participantRepository.findByEventAndUser(event, currentUser);
            if (participantInfo.isPresent()) {
                currentUserStatus = participantInfo.get().getIsGoing();
            }
        }

        return EventDetailsResponse.fromEntity(event, participants, assignments, currentUserStatus);
    }

    /**
     * Create a new event. Admin only.
     */
    @Transactional
    public EventResponse createEvent(EventRequest request, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .createdBy(admin)
                .build();

        Event savedEvent = eventRepository.save(event);
        return EventResponse.fromEntity(savedEvent);
    }

    /**
     * Update an event. Admin only.
     */
    @Transactional
    public EventResponse updateEvent(Long eventId, EventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());

        Event savedEvent = eventRepository.save(event);
        return EventResponse.fromEntity(savedEvent);
    }

    /**
     * Delete an event. Admin only.
     */
    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Delete associated records first
        participantRepository.deleteAll(participantRepository.findByEvent(event));
        assignmentRepository.deleteAll(assignmentRepository.findByEvent(event));

        eventRepository.delete(event);
    }

    /**
     * RSVP to an event. Open to Mentors and Students.
     */
    @Transactional
    public EventParticipantResponse rsvpToEvent(Long eventId, RsvpRequest request, String userEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<EventParticipant> existingParticipant = participantRepository.findByEventAndUser(event, user);
        EventParticipant participant;

        if (existingParticipant.isPresent()) {
            participant = existingParticipant.get();
            participant.setIsGoing(request.getIsGoing());
        } else {
            participant = EventParticipant.builder()
                    .event(event)
                    .user(user)
                    .isGoing(request.getIsGoing())
                    .build();
        }

        EventParticipant savedParticipant = participantRepository.save(participant);
        return EventParticipantResponse.fromEntity(savedParticipant);
    }

    /**
     * Assign a user a task for the event. Admin only.
     */
    @Transactional
    public EventAssignmentResponse assignDuty(Long eventId, EventAssignmentRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        
        User responsibleUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Assigned user not found"));

        EventAssignment assignment = EventAssignment.builder()
                .event(event)
                .responsibleUser(responsibleUser)
                .dutyDescription(request.getDutyDescription())
                .build();

        EventAssignment savedAssignment = assignmentRepository.save(assignment);
        return EventAssignmentResponse.fromEntity(savedAssignment);
    }

    /**
     * Remove a duty assignment. Admin only.
     */
    @Transactional
    public void removeAssignment(Long assignmentId) {
        EventAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        assignmentRepository.delete(assignment);
    }
}
