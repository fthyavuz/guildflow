package com.guildflow.backend.controller;

import com.guildflow.backend.dto.*;
import com.guildflow.backend.model.User;
import com.guildflow.backend.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<Page<EventResponse>> getEvents(
            @RequestParam(defaultValue = "UPCOMING") String filter,
            @RequestParam(required = false) String educationLevel,
            @PageableDefault(size = 50) Pageable pageable,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventService.getEvents(filter, educationLevel, pageable, user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDetailsResponse> getEventDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventService.getEventDetails(id, user));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(request, user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventService.updateEvent(id, request, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        eventService.deleteEvent(id, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/rsvp")
    public ResponseEntity<EventParticipantResponse> rsvpToEvent(
            @PathVariable Long id,
            @Valid @RequestBody RsvpRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(eventService.rsvpToEvent(id, request, user));
    }

    @PostMapping("/{id}/assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<EventAssignmentResponse> assignDuty(
            @PathVariable Long id,
            @Valid @RequestBody EventAssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.assignDuty(id, request));
    }

    @DeleteMapping("/assignments/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<Void> removeAssignment(@PathVariable Long assignmentId) {
        eventService.removeAssignment(assignmentId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/assignments/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<EventAssignmentResponse> updateAssignment(
            @PathVariable Long assignmentId,
            @Valid @RequestBody EventAssignmentRequest request) {
        return ResponseEntity.ok(eventService.updateAssignment(assignmentId, request));
    }

    @GetMapping("/{id}/eligible-students")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<List<UserResponse>> getEligibleStudents(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEligibleStudents(id));
    }

    @PostMapping("/{id}/participants/manual")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<EventParticipantResponse> addParticipantManually(
            @PathVariable Long id,
            @RequestParam Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.addParticipantManually(id, userId));
    }

    @DeleteMapping("/{id}/participants/{participantId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<Void> removeParticipant(
            @PathVariable Long id,
            @PathVariable Long participantId) {
        eventService.removeParticipant(participantId);
        return ResponseEntity.noContent().build();
    }
}
