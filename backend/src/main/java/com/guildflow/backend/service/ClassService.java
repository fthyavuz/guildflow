package com.guildflow.backend.service;

import com.guildflow.backend.dto.*;
import com.guildflow.backend.model.ClassStudent;
import com.guildflow.backend.model.MentorClass;
import com.guildflow.backend.model.User;
import com.guildflow.backend.model.enums.Role;
import com.guildflow.backend.repository.ClassStudentRepository;
import com.guildflow.backend.repository.GoalRepository;
import com.guildflow.backend.repository.MentorClassRepository;
import com.guildflow.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final MentorClassRepository classRepository;
    private final ClassStudentRepository classStudentRepository;
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final GoalService goalService;
    private final EvaluationService evaluationService;

    @Transactional
    public ClassResponse createClass(User mentor, CreateClassRequest request) {
        MentorClass mentorClass = MentorClass.builder()
                .name(request.getName())
                .description(request.getDescription())
                .educationLevel(request.getEducationLevel())
                .mentor(mentor)
                .build();

        MentorClass savedClass = classRepository.save(mentorClass);
        return ClassResponse.fromEntity(savedClass);
    }

    public List<ClassResponse> getClasses(User user) {
        if (user == null || user.getRole() == null) {
            return Collections.emptyList();
        }

        List<MentorClass> classes;
        if (user.getRole() == Role.ADMIN) {
            classes = classRepository.findByActiveTrue();
        } else {
            classes = classRepository.findByMentorAndActiveTrue(user);
        }
        return classes.stream()
                .map(ClassResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ClassResponse getClassById(Long id, User currentUser) {
        MentorClass mentorClass = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found with id: " + id));

        if (currentUser == null || currentUser.getRole() == null) {
            throw new RuntimeException("Access denied: Invalid user state");
        }

        User mentor = mentorClass.getMentor();
        if (mentor == null) {
            throw new RuntimeException("Class mentor not found");
        }

        // Security check: Only Admin or the Class's Mentor can view details
        if (currentUser.getRole() != Role.ADMIN && !mentor.getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied: You are not the mentor of this class");
        }

        return ClassResponse.fromEntity(mentorClass);
    }

    @Transactional
    public ClassResponse updateClass(Long id, CreateClassRequest request, User currentUser) {
        MentorClass mentorClass = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found with id: " + id));

        if (currentUser == null || currentUser.getRole() == null) {
            throw new RuntimeException("Access denied: Invalid user state");
        }

        User mentor = mentorClass.getMentor();
        if (mentor == null || (currentUser.getRole() != Role.ADMIN && !mentor.getId().equals(currentUser.getId()))) {
            throw new RuntimeException("Access denied: You cannot edit this class");
        }

        mentorClass.setName(request.getName());
        mentorClass.setDescription(request.getDescription());
        mentorClass.setEducationLevel(request.getEducationLevel());

        MentorClass updatedClass = classRepository.save(mentorClass);
        return ClassResponse.fromEntity(updatedClass);
    }

    @Transactional
    public void addStudentToClass(Long classId, Long studentId, User currentUser) {
        MentorClass mentorClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        if (currentUser.getRole() != Role.ADMIN && !mentorClass.getMentor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied: You cannot add students to this class");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getRole() != Role.STUDENT) {
            throw new RuntimeException("User is not a student");
        }

        // Deactivate old active enrollment if exists
        classStudentRepository.findByStudentAndActiveTrue(student).ifPresent(oldEnrollment -> {
            oldEnrollment.setActive(false);
            oldEnrollment.setLeftAt(LocalDateTime.now());
            classStudentRepository.save(oldEnrollment);
        });

        ClassStudent enrollment = ClassStudent.builder()
                .mentorClass(mentorClass)
                .student(student)
                .build();

        classStudentRepository.save(enrollment);
    }

    @Transactional
    public void removeStudentFromClass(Long classId, Long studentId, User currentUser) {
        MentorClass mentorClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        if (currentUser.getRole() != Role.ADMIN && !mentorClass.getMentor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied: You cannot remove students from this class");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        ClassStudent enrollment = classStudentRepository.findByStudentAndActiveTrue(student)
                .filter(e -> e.getMentorClass().getId().equals(classId))
                .orElseThrow(() -> new RuntimeException("Student not enrolled in this class"));

        enrollment.setActive(false);
        enrollment.setLeftAt(LocalDateTime.now());
        classStudentRepository.save(enrollment);
    }

    public List<StudentProgressSummary> getClassProgressSummary(Long classId, User currentUser) {
        MentorClass mentorClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        if (currentUser.getRole() != Role.ADMIN && !mentorClass.getMentor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        List<ClassStudent> activeEnrollments = classStudentRepository.findByMentorClassAndActiveTrue(mentorClass);

        return activeEnrollments.stream().map(e -> {
            User student = e.getStudent();
            List<GoalProgressResponse> goals = goalService.getStudentGoalsWithProgress(student);

            Double avgProgress = goals.isEmpty() ? 0.0
                    : goals.stream().mapToDouble(GoalProgressResponse::getOverallProgress).average().orElse(0.0);

            return StudentProgressSummary.builder()
                    .studentId(student.getId())
                    .firstName(student.getFirstName())
                    .lastName(student.getLastName())
                    .email(student.getEmail())
                    .averageProgress(avgProgress)
                    .activeGoalsCount(goals.size())
                    .build();
        }).collect(Collectors.toList());
    }

    public List<UserResponse> getClassStudents(Long classId, User currentUser) {
        MentorClass mentorClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        if (currentUser == null || currentUser.getRole() == null) {
            throw new RuntimeException("Access denied");
        }

        User mentor = mentorClass.getMentor();
        if (mentor == null || (currentUser.getRole() != Role.ADMIN && !mentor.getId().equals(currentUser.getId()))) {
            throw new RuntimeException("Access denied");
        }

        List<ClassStudent> activeEnrollments = classStudentRepository.findByMentorClassAndActiveTrue(mentorClass);
        return activeEnrollments.stream()
                .map(e -> UserResponse.fromEntity(e.getStudent()))
                .collect(Collectors.toList());
    }

    public StudentProfileResponse getStudentProfile(Long studentId, User currentUser) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getRole() != Role.STUDENT) {
            throw new RuntimeException("User is not a student");
        }

        // Security check: Only Admin, or Mentor who manages an active class of this
        // student
        ClassStudent activeEnrollment = classStudentRepository.findByStudentAndActiveTrue(student)
                .orElse(null);

        if (currentUser.getRole() != Role.ADMIN) {
            if (activeEnrollment == null
                    || !activeEnrollment.getMentorClass().getMentor().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Access denied: You are not this student's mentor");
            }
        }

        ClassResponse classInfo = activeEnrollment != null ? ClassResponse.fromEntity(activeEnrollment.getMentorClass())
                : null;

        List<EvaluationResponse> evaluations = evaluationService.getStudentEvaluations(studentId, currentUser);
        List<GoalProgressResponse> goals = goalService.getStudentGoalsWithProgress(student);

        return StudentProfileResponse.builder()
                .student(UserResponse.fromEntity(student))
                .currentClass(classInfo)
                .evaluations(evaluations)
                .goals(goals)
                .build();
    }

    public SystemStatsResponse getSystemStats(User currentUser) {
        if (currentUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("Access denied");
        }

        return SystemStatsResponse.builder()
                .totalClasses(classRepository.countByActiveTrue())
                .totalStudents(userRepository.countByRoleAndActiveTrue(Role.STUDENT))
                .totalMentors(userRepository.countByRoleAndActiveTrue(Role.MENTOR))
                .activeQuests(goalRepository.countByActiveTrue())
                .build();
    }
}
