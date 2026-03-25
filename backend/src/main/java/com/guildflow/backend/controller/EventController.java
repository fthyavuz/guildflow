package com.guildflow.backend.controller;

import com.guildflow.backend.dto.*;
import com.guildflow.backend.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
            @RequestParam(required = false) Long classId,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(eventService.getEvents(filter, educationLevel, classId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDetailsResponse> getEventDetails(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(eventService.getEventDetails(id, email));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(request, authentication.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/rsvp")
    public ResponseEntity<EventParticipantResponse> rsvpToEvent(
            @PathVariable Long id,
            @Valid @RequestBody RsvpRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(eventService.rsvpToEvent(id, request, authentication.getName()));
    }

    @PostMapping("/{id}/assignments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventAssignmentResponse> assignDuty(
            @PathVariable Long id,
            @Valid @RequestBody EventAssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.assignDuty(id, request));
    }

    @DeleteMapping("/assignments/{assignmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeAssignment(@PathVariable Long assignmentId) {
        eventService.removeAssignment(assignmentId);
        return ResponseEntity.noContent().build();
    }
}
