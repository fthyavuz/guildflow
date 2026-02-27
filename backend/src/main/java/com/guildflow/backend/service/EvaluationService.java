package com.guildflow.backend.service;

import com.guildflow.backend.dto.EvaluationRequest;
import com.guildflow.backend.dto.EvaluationResponse;
import com.guildflow.backend.model.StudentEvaluation;
import com.guildflow.backend.model.User;
import com.guildflow.backend.model.enums.Role;
import com.guildflow.backend.repository.StudentEvaluationRepository;
import com.guildflow.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final StudentEvaluationRepository evaluationRepository;
    private final UserRepository userRepository;

    @Transactional
    public EvaluationResponse createEvaluation(EvaluationRequest request, User mentor) {
        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getRole() != Role.STUDENT) {
            throw new RuntimeException("User is not a student");
        }

        StudentEvaluation evaluation = StudentEvaluation.builder()
                .mentor(mentor)
                .student(student)
                .period(request.getPeriod())
                .content(request.getContent())
                .periodName(request.getPeriodName())
                .build();

        return EvaluationResponse.fromEntity(evaluationRepository.save(evaluation));
    }

    public List<EvaluationResponse> getStudentEvaluations(Long studentId, User currentUser) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Only Admin, the Mentor who wrote it, the Parent of the student, or the
        // student themselves
        if (currentUser.getRole() != Role.ADMIN &&
                currentUser.getRole() != Role.MENTOR &&
                !currentUser.getId().equals(studentId)) {
            // Further security check for Mentor
            // Ideally, we'd check if the mentor is managing the student's class
            throw new RuntimeException("Access denied");
        }

        return evaluationRepository.findByStudentOrderByCreatedAtDesc(student).stream()
                .map(EvaluationResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
