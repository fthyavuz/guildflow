package com.guildflow.backend.service;

import com.guildflow.backend.dto.ClassHomeworkAssignmentRequest;
import com.guildflow.backend.dto.ClassHomeworkAssignmentResponse;
import com.guildflow.backend.exception.EntityNotFoundException;
import com.guildflow.backend.exception.ForbiddenException;
import com.guildflow.backend.model.ClassHomeworkAssignment;
import com.guildflow.backend.model.Goal;
import com.guildflow.backend.model.MentorClass;
import com.guildflow.backend.model.User;
import com.guildflow.backend.model.enums.Frequency;
import com.guildflow.backend.repository.ClassHomeworkAssignmentRepository;
import com.guildflow.backend.repository.GoalRepository;
import com.guildflow.backend.repository.MentorClassRepository;
import com.guildflow.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final ClassHomeworkAssignmentRepository assignmentRepository;
    private final MentorClassRepository classRepository;
    private final GoalRepository goalRepository;
    private final SecurityUtils securityUtils;

    public List<ClassHomeworkAssignmentResponse> getClassAssignments(Long classId) {
        MentorClass mentorClass = classRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException("Class not found"));
        return assignmentRepository.findByMentorClassOrderByCreatedAtDesc(mentorClass)
                .stream()
                .map(ClassHomeworkAssignmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClassHomeworkAssignmentResponse createAssignment(Long classId,
            ClassHomeworkAssignmentRequest request, User creator) {
        securityUtils.validateUserState(creator);

        MentorClass mentorClass = classRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException("Class not found"));
        securityUtils.requireAdminOrOwner(creator, mentorClass.getMentor(),
                "Access denied: You are not the mentor of this class");

        Goal goal = goalRepository.findById(request.getGoalId())
                .orElseThrow(() -> new EntityNotFoundException("Homework template not found"));

        Frequency frequency = null;
        if (request.getFrequency() != null && !request.getFrequency().isBlank()) {
            frequency = Frequency.valueOf(request.getFrequency());
        }

        ClassHomeworkAssignment assignment = ClassHomeworkAssignment.builder()
                .goal(goal)
                .mentorClass(mentorClass)
                .frequency(frequency)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .applyToAll(request.isApplyToAll())
                .createdBy(creator)
                .build();

        if (!request.isApplyToAll() && request.getStudentIds() != null) {
            assignment.setStudentIds(request.getStudentIds());
        }

        return ClassHomeworkAssignmentResponse.fromEntity(assignmentRepository.save(assignment));
    }

    @Transactional
    public void deleteAssignment(Long classId, Long assignmentId, User user) {
        ClassHomeworkAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));

        if (!assignment.getMentorClass().getId().equals(classId)) {
            throw new ForbiddenException("Assignment does not belong to this class");
        }
        securityUtils.requireAdminOrOwner(user, assignment.getMentorClass().getMentor(),
                "Access denied: You are not the mentor of this class");

        assignmentRepository.delete(assignment);
    }
}
