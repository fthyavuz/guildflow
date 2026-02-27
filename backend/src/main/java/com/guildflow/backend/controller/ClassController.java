package com.guildflow.backend.controller;

import com.guildflow.backend.dto.ClassResponse;
import com.guildflow.backend.dto.CreateClassRequest;
import com.guildflow.backend.dto.UserResponse;
import com.guildflow.backend.model.User;
import com.guildflow.backend.service.ClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<ClassResponse> createClass(
            @AuthenticationPrincipal User mentor,
            @Valid @RequestBody CreateClassRequest request) {
        ClassResponse response = classService.createClass(mentor, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<List<ClassResponse>> getClasses(@AuthenticationPrincipal User user) {
        List<ClassResponse> classes = classService.getClasses(user);
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<ClassResponse> getClassById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        ClassResponse response = classService.getClassById(id, user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<ClassResponse> updateClass(
            @PathVariable Long id,
            @Valid @RequestBody CreateClassRequest request,
            @AuthenticationPrincipal User user) {
        ClassResponse response = classService.updateClass(id, request, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/students/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<Void> addStudentToClass(
            @PathVariable Long id,
            @PathVariable Long studentId,
            @AuthenticationPrincipal User user) {
        classService.addStudentToClass(id, studentId, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/students/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<Void> removeStudentFromClass(
            @PathVariable Long id,
            @PathVariable Long studentId,
            @AuthenticationPrincipal User user) {
        classService.removeStudentFromClass(id, studentId, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<List<UserResponse>> getClassStudents(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        List<UserResponse> students = classService.getClassStudents(id, user);
        return ResponseEntity.ok(students);
    }
}
