package com.guildflow.backend.controller;

import com.guildflow.backend.dto.AttendanceRequest;
import com.guildflow.backend.dto.AttendanceResponse;
import com.guildflow.backend.dto.AttendanceSummaryResponse;
import com.guildflow.backend.dto.MeetingRequest;
import com.guildflow.backend.dto.MeetingResponse;
import com.guildflow.backend.model.User;
import com.guildflow.backend.service.MeetingService;
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
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<List<MeetingResponse>> createMeeting(
            @Valid @RequestBody MeetingRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(meetingService.createMeeting(request, user));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<MeetingResponse>> getMyMeetings(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(meetingService.getMyMeetings(user, pageable));
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<Page<MeetingResponse>> getMeetingsForClass(
            @PathVariable Long classId,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(meetingService.getMeetingsForClass(classId, user, pageable));
    }

    @PostMapping("/{id}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<Void> markAttendance(
            @PathVariable Long id,
            @RequestBody List<AttendanceRequest> requests,
            @AuthenticationPrincipal User user) {
        meetingService.markAttendance(id, requests, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<List<AttendanceResponse>> getMeetingAttendance(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(meetingService.getMeetingAttendance(id, user));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MeetingResponse> getMeetingById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(meetingService.getMeetingById(id, user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<MeetingResponse> updateMeeting(
            @PathVariable Long id,
            @RequestBody MeetingRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(meetingService.updateMeeting(id, request, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<Void> deleteMeeting(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        meetingService.deleteMeeting(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/student/{studentId}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<AttendanceResponse>> getStudentAttendanceHistory(
            @PathVariable Long studentId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(meetingService.getStudentAttendanceHistory(studentId, user));
    }

    @GetMapping("/student/{studentId}/attendance-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR', 'STUDENT', 'PARENT')")
    public ResponseEntity<AttendanceSummaryResponse> getStudentAttendanceSummary(
            @PathVariable Long studentId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(meetingService.getStudentAttendanceSummary(studentId, user));
    }
}
