package com.guildflow.backend.service;

import com.guildflow.backend.dto.ClassResponse;
import com.guildflow.backend.dto.CreateClassRequest;
import com.guildflow.backend.dto.UserResponse;
import com.guildflow.backend.model.ClassStudent;
import com.guildflow.backend.model.MentorClass;
import com.guildflow.backend.model.User;
import com.guildflow.backend.model.enums.Role;
import com.guildflow.backend.repository.ClassStudentRepository;
import com.guildflow.backend.repository.MentorClassRepository;
import com.guildflow.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final MentorClassRepository classRepository;
    private final ClassStudentRepository classStudentRepository;
    private final UserRepository userRepository;

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

        // Security check: Only Admin or the Class's Mentor can view details
        if (currentUser.getRole() != Role.ADMIN && !mentorClass.getMentor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied: You are not the mentor of this class");
        }

        return ClassResponse.fromEntity(mentorClass);
    }

    @Transactional
    public ClassResponse updateClass(Long id, CreateClassRequest request, User currentUser) {
        MentorClass mentorClass = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found with id: " + id));

        if (currentUser.getRole() != Role.ADMIN && !mentorClass.getMentor().getId().equals(currentUser.getId())) {
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

    public List<UserResponse> getClassStudents(Long classId, User currentUser) {
        MentorClass mentorClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        if (currentUser.getRole() != Role.ADMIN && !mentorClass.getMentor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        List<ClassStudent> activeEnrollments = classStudentRepository.findByMentorClassAndActiveTrue(mentorClass);
        return activeEnrollments.stream()
                .map(e -> UserResponse.fromEntity(e.getStudent()))
                .collect(Collectors.toList());
    }
}
