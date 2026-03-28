package com.guildflow.backend.controller;

import com.guildflow.backend.dto.*;
import com.guildflow.backend.model.User;
import com.guildflow.backend.service.GoalProgressService;
import com.guildflow.backend.service.GoalService;
import com.guildflow.backend.service.GoalTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;
    private final GoalTemplateService goalTemplateService;
    private final GoalProgressService goalProgressService;

    @GetMapping("/templates")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<GoalResponse>> getTemplates(@PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(goalTemplateService.getTemplates(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR', 'STUDENT')")
    public ResponseEntity<GoalResponse> getGoal(@PathVariable Long id) {
        return ResponseEntity.ok(goalService.getGoalById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<GoalResponse> createGoal(
            @Valid @RequestBody GoalRequest request,
            @AuthenticationPrincipal User mentor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goalService.createGoal(request, mentor));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<GoalResponse> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody GoalRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(goalService.updateGoal(id, request, user));
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<Page<GoalResponse>> getGoalsForClass(
            @PathVariable Long classId,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(goalService.getGoalsForClass(classId, user, pageable));
    }

    @GetMapping("/my-goals")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<HomeworkSummaryResponse>> getMyGoals(@AuthenticationPrincipal User student) {
        return ResponseEntity.ok(goalProgressService.getStudentHomeworkList(student));
    }

    @GetMapping("/my-goals/{assignmentId}/day")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<DayEntryResponse>> getDayEntries(
            @PathVariable Long assignmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal User student) {
        return ResponseEntity.ok(goalProgressService.getDayEntries(assignmentId, date, student));
    }

    @PostMapping("/my-goals/{assignmentId}/save-day")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<DayEntryResponse>> saveDayEntries(
            @PathVariable Long assignmentId,
            @Valid @RequestBody SaveDayRequest request,
            @AuthenticationPrincipal User student) {
        return ResponseEntity.ok(goalProgressService.saveDayEntries(assignmentId, request, student));
    }

    @DeleteMapping("/entries/{entryId}/unlock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<Void> unlockEntry(
            @PathVariable Long entryId,
            @AuthenticationPrincipal User mentor) {
        goalProgressService.unlockEntry(entryId, mentor);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assign-template")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<GoalResponse> assignTemplate(
            @Valid @RequestBody GoalAssignmentRequest request,
            @AuthenticationPrincipal User mentor) {
        return ResponseEntity.ok(goalTemplateService.assignGoalTemplate(request, mentor));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<Void> deleteGoal(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        goalService.deleteGoal(id, user);
        return ResponseEntity.noContent().build();
    }
}
