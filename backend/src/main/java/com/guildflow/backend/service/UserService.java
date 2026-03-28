package com.guildflow.backend.service;

import com.guildflow.backend.dto.*;
import com.guildflow.backend.exception.ConflictException;
import com.guildflow.backend.exception.EntityNotFoundException;
import com.guildflow.backend.exception.ValidationException;
import com.guildflow.backend.model.ParentStudent;
import com.guildflow.backend.model.User;
import com.guildflow.backend.model.enums.Role;
import com.guildflow.backend.repository.ParentStudentRepository;
import com.guildflow.backend.repository.UserRepository;
import com.guildflow.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class UserService {

    private final UserRepository userRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Authenticate a user and return JWT tokens.
     */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        User user = (User) authentication.getPrincipal();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(UserResponse.fromEntity(user))
                .build();
    }

    /**
     * Refresh access token using a valid refresh token.
     */
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new ValidationException("Invalid refresh token");
        }

        String tokenType = jwtTokenProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new ValidationException("Token is not a refresh token");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String newAccessToken = jwtTokenProvider.generateAccessToken(email);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .user(UserResponse.fromEntity(user))
                .build();
    }

    /**
     * Create a new user (admin only).
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(request.getRole())
                .languagePref(request.getLanguagePref())
                .build();

        User savedUser = userRepository.save(user);

        // Every student must be linked to a parent
        if (request.getRole() == Role.STUDENT) {
            if (request.getParentId() == null) {
                throw new ValidationException("A parent must be assigned when creating a student");
            }
            User parent = userRepository.findById(request.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent not found: " + request.getParentId()));
            if (parent.getRole() != Role.PARENT) {
                throw new ValidationException("Selected user is not a parent");
            }
            parentStudentRepository.save(ParentStudent.builder().parent(parent).student(savedUser).build());
        }

        return UserResponse.fromEntity(savedUser);
    }

    /**
     * Get all users, optionally filtered by role and/or search term, with pagination.
     */
    public Page<UserResponse> getUsers(Role role, String search, Pageable pageable) {
        return userRepository.findByFilters(role, search, pageable).map(UserResponse::fromEntity);
    }

    /**
     * Admin-only password reset — no current password verification required.
     */
    @Transactional
    public void adminResetPassword(Long id, AdminResetPasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Get user by ID.
     */
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return UserResponse.fromEntity(user);
    }

    /**
     * Update an existing user.
     */
    @Transactional
    public UserResponse updateUser(Long id, CreateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setLanguagePref(request.getLanguagePref());

        // Only update email if it changed and isn't taken
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        // Only update password if provided
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    /**
     * Change the authenticated user's password after verifying the current one.
     */
    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ValidationException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new ValidationException("New password must be different from the current password");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Soft-delete a user by setting active = false.
     */
    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        user.setActive(false);
        userRepository.save(user);
    }

    // ── Parent–Student linking ────────────────────────────────────────────────

    @Transactional
    public void linkParentToStudent(Long parentId, Long studentId) {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + parentId));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + studentId));

        if (parent.getRole() != Role.PARENT) throw new ValidationException("User is not a parent");
        if (student.getRole() != Role.STUDENT) throw new ValidationException("User is not a student");
        if (parentStudentRepository.existsByParentAndStudent(parent, student))
            throw new ConflictException("Link already exists");

        parentStudentRepository.save(ParentStudent.builder().parent(parent).student(student).build());
    }

    @Transactional
    public void unlinkParentFromStudent(Long parentId, Long studentId) {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + parentId));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + studentId));
        parentStudentRepository.findByParentAndStudent(parent, student)
                .ifPresent(parentStudentRepository::delete);
    }

    public List<UserResponse> getStudentsForParent(Long parentId) {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + parentId));
        return parentStudentRepository.findByParent(parent).stream()
                .map(ps -> UserResponse.fromEntity(ps.getStudent()))
                .collect(Collectors.toList());
    }

    /** Returns all active students enriched with their linked parent's info. */
    public List<UserResponse> getAllStudentsWithParent() {
        return userRepository.findByRoleAndActiveTrue(Role.STUDENT).stream().map(student -> {
            List<ParentStudent> links = parentStudentRepository.findByStudent(student);
            UserResponse r = UserResponse.fromEntity(student);
            if (!links.isEmpty()) {
                User parent = links.get(0).getParent();
                r.setParentId(parent.getId());
                r.setParentName(parent.getFirstName() + " " + parent.getLastName());
            }
            return r;
        }).collect(Collectors.toList());
    }

    /** Returns all active parents enriched with how many students they have. */
    public List<UserResponse> getAllParentsWithStudentCount() {
        return userRepository.findByRoleAndActiveTrue(Role.PARENT).stream().map(parent -> {
            int count = parentStudentRepository.findByParent(parent).size();
            UserResponse r = UserResponse.fromEntity(parent);
            r.setStudentCount(count);
            return r;
        }).collect(Collectors.toList());
    }
}
