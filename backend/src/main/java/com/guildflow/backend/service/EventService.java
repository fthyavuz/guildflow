package com.guildflow.backend.service;

import com.guildflow.backend.dto.*;
import com.guildflow.backend.exception.EntityNotFoundException;
import com.guildflow.backend.exception.ForbiddenException;
import com.guildflow.backend.model.Event;
import com.guildflow.backend.model.EventAssignment;
import com.guildflow.backend.model.EventParticipant;
import com.guildflow.backend.model.MentorClass;
import com.guildflow.backend.model.User;
import com.guildflow.backend.model.enums.EducationLevel;
import com.guildflow.backend.model.enums.Role;
import com.guildflow.backend.repository.ClassStudentRepository;
import com.guildflow.backend.repository.EventAssignmentRepository;
import com.guildflow.backend.repository.EventParticipantRepository;
import com.guildflow.backend.repository.EventRepository;
import com.guildflow.backend.repository.MentorClassRepository;
import com.guildflow.backend.repository.ParentStudentRepository;
import com.guildflow.backend.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final MentorClassRepository classRepository;
    private final ClassStudentRepository classStudentRepository;
    private final ParentStudentRepository parentStudentRepository;

    /**
     * Get events. Visibility:
     *   ADMIN/MENTOR: all events
     *   STUDENT: events with no target classes (open) OR events targeting their class
     *   PARENT: events with no target classes OR targeting any of their students' classes
     *   unauthenticated: only open events
     */
    public Page<EventResponse> getEvents(String filter, String educationLevel, Pageable pageable, User currentUser) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startAfter = null;
        LocalDateTime endBefore = null;

        if ("PAST".equalsIgnoreCase(filter)) {
            endBefore = now.minusDays(1);
        } else if (!"ALL".equalsIgnoreCase(filter)) {
            startAfter = now.minusDays(1);
        }

        EducationLevel level = null;
        if (educationLevel != null && !educationLevel.isBlank()) {
            try {
                level = EducationLevel.valueOf(educationLevel.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        final LocalDateTime finalStartAfter = startAfter;
        final LocalDateTime finalEndBefore = endBefore;
        final EducationLevel finalLevel = level;

        Specification<Event> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (finalStartAfter != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), finalStartAfter));
            if (finalEndBefore != null)
                predicates.add(cb.lessThan(root.get("startTime"), finalEndBefore));
            if (finalLevel != null)
                predicates.add(cb.equal(root.get("educationLevel"), finalLevel));
            if (query != null)
                query.orderBy(cb.asc(root.get("startTime")));

            // Visibility filtering for STUDENT and PARENT
            if (currentUser == null) {
                // Unauthenticated: only open events
                predicates.add(cb.equal(cb.size(root.get("targetClasses")), 0));
            } else {
                Role role = currentUser.getRole();
                if (role == Role.STUDENT || role == Role.PARENT) {
                    List<Long> visibleClassIds = getVisibleClassIds(currentUser, role);

                    if (visibleClassIds.isEmpty()) {
                        // No class membership → only open events
                        predicates.add(cb.equal(cb.size(root.get("targetClasses")), 0));
                    } else {
                        // Open events OR events targeting any of the visible classes
                        Subquery<Long> sq = query.subquery(Long.class);
                        Root<Event> subRoot = sq.correlate(root);
                        Join<Event, MentorClass> targetJoin = subRoot.join("targetClasses");
                        sq.select(cb.literal(1L)).where(targetJoin.get("id").in(visibleClassIds));

                        predicates.add(cb.or(
                                cb.equal(cb.size(root.get("targetClasses")), 0),
                                cb.exists(sq)
                        ));
                    }
                }
                // ADMIN and MENTOR see all events — no extra predicate
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return eventRepository.findAll(spec, pageable).map(EventResponse::fromEntity);
    }

    /**
     * Get detailed event information. Applies same visibility rules.
     */
    public EventDetailsResponse getEventDetails(Long eventId, User currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        // Visibility check for STUDENT/PARENT
        if (currentUser != null) {
            Role role = currentUser.getRole();
            if (role == Role.STUDENT || role == Role.PARENT) {
                if (!isEventVisible(event, currentUser, role)) {
                    throw new ForbiddenException("You do not have access to this event");
                }
            }
        }

        List<EventParticipant> participants = participantRepository.findByEvent(event);
        List<EventAssignment> assignments = assignmentRepository.findByEvent(event);

        Boolean currentUserStatus = null;
        if (currentUser != null) {
            Optional<EventParticipant> participantInfo = participantRepository.findByEventAndUser(event, currentUser);
            if (participantInfo.isPresent()) {
                currentUserStatus = participantInfo.get().getIsGoing();
            }
        }

        return EventDetailsResponse.fromEntity(event, participants, assignments, currentUserStatus);
    }

    /**
     * Create a new event. Admin or Mentor.
     */
    @Transactional
    public EventResponse createEvent(EventRequest request, User user) {
        List<MentorClass> targetClasses = resolveTargetClasses(request);

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .createdBy(user)
                .targetClasses(targetClasses)
                .build();

        return EventResponse.fromEntity(eventRepository.save(event));
    }

    /**
     * Update an event. Mentor can only edit their own events.
     */
    @Transactional
    public EventResponse updateEvent(Long eventId, EventRequest request, User user) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        if (user.getRole() == Role.MENTOR && !event.getCreatedBy().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only edit events you created");
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.getTargetClasses().clear();
        event.getTargetClasses().addAll(resolveTargetClasses(request));

        return EventResponse.fromEntity(eventRepository.save(event));
    }

    /**
     * Delete an event. Mentor can only delete their own events.
     */
    @Transactional
    public void deleteEvent(Long eventId, User user) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        if (user.getRole() == Role.MENTOR && !event.getCreatedBy().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only delete events you created");
        }

        eventRepository.delete(event);
    }

    /**
     * RSVP to an event.
     */
    @Transactional
    public EventParticipantResponse rsvpToEvent(Long eventId, RsvpRequest request, User user) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

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
     * Assign a duty for the event. Admin or Mentor.
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
     * Remove a duty assignment. Admin or Mentor.
     */
    @Transactional
    public void removeAssignment(Long assignmentId) {
        EventAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));
        assignmentRepository.delete(assignment);
    }

    private List<MentorClass> resolveTargetClasses(EventRequest request) {
        if (request.getTargetClassIds() == null || request.getTargetClassIds().isEmpty()) {
            return new ArrayList<>();
        }
        return request.getTargetClassIds().stream()
                .map(id -> classRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Class not found: " + id)))
                .collect(Collectors.toList());
    }

    private List<Long> getVisibleClassIds(User user, Role role) {
        if (role == Role.STUDENT) {
            return classStudentRepository.findByStudentAndActiveTrue(user)
                    .map(cs -> List.of(cs.getMentorClass().getId()))
                    .orElse(List.of());
        } else if (role == Role.PARENT) {
            return parentStudentRepository.findByParent(user).stream()
                    .flatMap(ps -> classStudentRepository.findByStudentAndActiveTrue(ps.getStudent()).stream())
                    .map(cs -> cs.getMentorClass().getId())
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private boolean isEventVisible(Event event, User user, Role role) {
        if (event.getTargetClasses().isEmpty()) return true; // open event
        List<Long> visibleClassIds = getVisibleClassIds(user, role);
        return event.getTargetClasses().stream()
                .anyMatch(tc -> visibleClassIds.contains(tc.getId()));
    }
}
