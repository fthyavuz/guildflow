package com.guildflow.backend.service;

import com.guildflow.backend.dto.GoalAssignmentRequest;
import com.guildflow.backend.dto.GoalResponse;
import com.guildflow.backend.exception.EntityNotFoundException;
import com.guildflow.backend.exception.ValidationException;
import com.guildflow.backend.model.*;
import com.guildflow.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class GoalTemplateService {

    private final GoalRepository goalRepository;
    private final GoalTaskRepository goalTaskRepository;
    private final GoalStudentRepository goalStudentRepository;
    private final MentorClassRepository classRepository;
    private final UserRepository userRepository;

    public Page<GoalResponse> getTemplates(Pageable pageable) {
        return goalRepository.findByIsTemplateTrueAndActiveTrue(pageable)
                .map(GoalResponse::fromEntity);
    }

    @Transactional
    public GoalResponse assignGoalTemplate(GoalAssignmentRequest request, User creator) {
        Goal template = goalRepository.findById(request.getGoalId())
                .orElseThrow(() -> new EntityNotFoundException("Template not found"));

        if (!template.getIsTemplate()) {
            throw new ValidationException("Selected goal is not a template");
        }

        MentorClass mentorClass = null;
        if (request.getClassId() != null) {
            mentorClass = classRepository.findById(request.getClassId())
                    .orElseThrow(() -> new EntityNotFoundException("Class not found"));
        }

        Goal newGoal = Goal.builder()
                .title(template.getTitle())
                .description(template.getDescription())
                .mentorClass(mentorClass)
                .goalType(template.getGoalType())
                .applyToAll(request.isApplyToAll())
                .isTemplate(false)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdBy(creator)
                .build();

        Goal savedGoal = goalRepository.save(newGoal);

        // Clone tasks from template
        List<GoalTask> clonedTasks = template.getTasks().stream()
                .map(t -> GoalTask.builder()
                        .goal(savedGoal)
                        .title(t.getTitle())
                        .description(t.getDescription())
                        .taskType(t.getTaskType())
                        .targetValue(t.getTargetValue())
                        .sortOrder(t.getSortOrder())
                        .source(t.getSource())
                        .build())
                .collect(Collectors.toList());

        goalTaskRepository.saveAll(clonedTasks);
        savedGoal.setTasks(clonedTasks);

        // Assign to students if not applyToAll
        if (!request.isApplyToAll() && request.getStudentIds() != null) {
            List<GoalStudent> studentMappings = request.getStudentIds().stream()
                    .map(sid -> {
                        User student = userRepository.findById(sid)
                                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + sid));
                        return GoalStudent.builder().goal(savedGoal).student(student).build();
                    })
                    .collect(Collectors.toList());
            goalStudentRepository.saveAll(studentMappings);
        }

        return GoalResponse.fromEntity(savedGoal);
    }
}
