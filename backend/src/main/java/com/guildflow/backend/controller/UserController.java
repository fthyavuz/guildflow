package com.guildflow.backend.controller;

import com.guildflow.backend.dto.AdminResetPasswordRequest;
import com.guildflow.backend.dto.CreateUserRequest;
import com.guildflow.backend.dto.UserResponse;
import com.guildflow.backend.model.enums.Role;
import com.guildflow.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userService.getUsers(role, search, pageable));
    }

    @PutMapping("/{id}/admin-reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminResetPassword(
            @PathVariable Long id,
            @Valid @RequestBody AdminResetPasswordRequest request) {
        userService.adminResetPassword(id, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mentors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getMentors(@PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(userService.getUsers(Role.MENTOR, null, pageable));
    }

    @GetMapping("/students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getStudents(@PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(userService.getUsers(Role.STUDENT, null, pageable));
    }

    @GetMapping("/parents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getParents(@PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(userService.getUsers(Role.PARENT, null, pageable));
    }

    @GetMapping("/students-list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.List<UserResponse>> getStudentsList() {
        return ResponseEntity.ok(userService.getAllStudentsWithParent());
    }

    @GetMapping("/parents-list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.List<UserResponse>> getParentsList() {
        return ResponseEntity.ok(userService.getAllParentsWithStudentCount());
    }

    @PostMapping("/{parentId}/link-student/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> linkParentToStudent(@PathVariable Long parentId, @PathVariable Long studentId) {
        userService.linkParentToStudent(parentId, studentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{parentId}/link-student/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unlinkParentFromStudent(@PathVariable Long parentId, @PathVariable Long studentId) {
        userService.unlinkParentFromStudent(parentId, studentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{parentId}/students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.List<UserResponse>> getStudentsForParent(@PathVariable Long parentId) {
        return ResponseEntity.ok(userService.getStudentsForParent(parentId));
    }
}
