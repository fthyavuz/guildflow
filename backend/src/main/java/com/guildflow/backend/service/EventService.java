package com.guildflow.backend.service;

import com.guildflow.backend.dto.*;
import com.guildflow.backend.exception.EntityNotFoundException;
import com.guildflow.backend.model.Event;
import com.guildflow.backend.model.EventAssignment;
import com.guildflow.backend.model.EventParticipant;
import com.guildflow.backend.model.MentorClass;
import com.guildflow.backend.model.User;
import com.guildflow.backend.model.enums.EducationLevel;
import com.guildflow.backend.repository.EventAssignmentRepository;
import com.guildflow.backend.repository.EventParticipantRepository;
import com.guildflow.backend.repository.EventRepository;
import com.guildflow.backend.repository.MentorClassRepository;
import com.guildflow.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class EventService {

    private final EventRepository eventRepository;
    private final EventParticipantRepository participantRepository;
    private final EventAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final MentorClassRepository classRepository;

    /**
     * Get events with optional filters:
     *   filter: UPCOMING (default), PAST, ALL
     *   educationLevel: PRIMARY, SECONDARY, HIGH_SCHOOL, UNIVERSITY (or null for all)
     *   targetClassId: specific class ID (or null for all)
     */
    public Page<EventResponse> getEvents(String filter, String educationLevel, Long targetClassId, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startAfter = null;
        LocalDateTime endBefore = null;

        if ("PAST".equalsIgnoreCase(filter)) {
            endBefore = now.minusDays(1);
        } else if (!"ALL".equalsIgnoreCase(filter)) {
            // Default: UPCOMING
            startAfter = now.minusDays(1);
        }

        EducationLevel level = null;
        if (educationLevel != null && !educationLevel.isBlank()) {
            try {
                level = EducationLevel.valueOf(educationLevel.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        return eventRepository.findWithFilters(startAfter, endBefore, level, targetClassId, pageable)
                .map(EventResponse::fromEntity);
    }

    /**
     * Get detailed event information including participants and assignments.
     */
    public EventDetailsResponse getEventDetails(Long eventId, String currentUserEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        List<EventParticipant> participants = participantRepository.findByEvent(event);
        List<EventAssignment> assignments = assignmentRepository.findByEvent(event);

        Boolean currentUserStatus = null;
        if (currentUserEmail != null) {
            User currentUser = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

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
                .orElseThrow(() -> new EntityNotFoundException("Admin user not found"));

        MentorClass targetClass = resolveTargetClass(request);
        EducationLevel level = resolveEducationLevel(request);

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .createdBy(admin)
                .educationLevel(level)
                .targetClass(targetClass)
                .build();

        return EventResponse.fromEntity(eventRepository.save(event));
    }

    /**
     * Update an event. Admin only.
     */
    @Transactional
    public EventResponse updateEvent(Long eventId, EventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setEducationLevel(resolveEducationLevel(request));
        event.setTargetClass(resolveTargetClass(request));

        return EventResponse.fromEntity(eventRepository.save(event));
    }

    /**
     * Delete an event. Admin only.
     */
    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        eventRepository.delete(event);
    }

    /**
     * RSVP to an event.
     */
    @Transactional
    public EventParticipantResponse rsvpToEvent(Long eventId, RsvpRequest request, String userEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

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

        return EventParticipantResponse.fromEntity(participantRepository.save(participant));
    }

    /**
     * Assign a duty for the event. Admin only.
     */
    @Transactional
    public EventAssignmentResponse assignDuty(Long eventId, EventAssignmentRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        User responsibleUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Assigned user not found"));

        EventAssignment assignment = EventAssignment.builder()
                .event(event)
                .responsibleUser(responsibleUser)
                .dutyDescription(request.getDutyDescription())
                .build();

        return EventAssignmentResponse.fromEntity(assignmentRepository.save(assignment));
    }

    /**
     * Remove a duty assignment. Admin only.
     */
    @Transactional
    public void removeAssignment(Long assignmentId) {
        EventAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));
        assignmentRepository.delete(assignment);
    }

    private MentorClass resolveTargetClass(EventRequest request) {
        if (request.getTargetClassId() == null) return null;
        return classRepository.findById(request.getTargetClassId())
                .orElseThrow(() -> new EntityNotFoundException("Target class not found"));
    }

    private EducationLevel resolveEducationLevel(EventRequest request) {
        if (request.getEducationLevel() == null || request.getEducationLevel().isBlank()) return null;
        try {
            return EducationLevel.valueOf(request.getEducationLevel().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
