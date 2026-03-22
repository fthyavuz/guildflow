package com.guildflow.backend.service;

import com.guildflow.backend.dto.*;
import com.guildflow.backend.exception.EntityNotFoundException;
import com.guildflow.backend.exception.ForbiddenException;
import com.guildflow.backend.exception.ValidationException;
import com.guildflow.backend.model.*;
import com.guildflow.backend.model.enums.Role;
import com.guildflow.backend.repository.*;
import com.guildflow.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles goal type management and core goal CRUD operations.
 * Template operations → GoalTemplateService
 * Student progress tracking → GoalProgressService
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class GoalService {

    private final GoalTypeRepository goalTypeRepository;
    private final GoalRepository goalRepository;
    private final GoalTaskRepository goalTaskRepository;
    private final GoalStudentRepository goalStudentRepository;
    private final MentorClassRepository classRepository;
    private final UserRepository userRepository;
    private final ClassStudentRepository classStudentRepository;
    private final SourceRepository sourceRepository;
    private final SecurityUtils securityUtils;

    // --- Goal Types ---

    public List<GoalTypeResponse> getAllGoalTypes() {
        return goalTypeRepository.findByActiveTrue().stream()
                .map(GoalTypeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public GoalTypeResponse createGoalType(String name, String description) {
        GoalType type = GoalType.builder()
                .name(name)
                .description(description)
                .build();
        return GoalTypeResponse.fromEntity(goalTypeRepository.save(type));
    }

    // --- Goal CRUD ---

    @Transactional
    public GoalResponse createGoal(GoalRequest request, User creator) {
        securityUtils.validateUserState(creator);

        MentorClass mentorClass = null;
        if (!request.isTemplate()) {
            if (request.getClassId() == null) {
                throw new ValidationException("Class ID is required for non-template goals");
            }
            mentorClass = classRepository.findById(request.getClassId())
                    .orElseThrow(() -> new EntityNotFoundException("Class not found"));

            securityUtils.requireAdminOrOwner(creator, mentorClass.getMentor(),
                    "Access denied: You are not the mentor of this class");
        }

        GoalType goalType = goalTypeRepository.findById(request.getGoalTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Goal type not found"));

        Goal goal = Goal.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .mentorClass(mentorClass)
                .goalType(goalType)
                .applyToAll(request.isApplyToAll())
                .isTemplate(request.isTemplate())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdBy(creator)
                .build();

        Goal savedGoal = goalRepository.save(goal);

        List<GoalTask> tasks = request.getTasks().stream()
                .map(tr -> {
                    Source source = tr.getSourceId() != null
                            ? sourceRepository.findById(tr.getSourceId()).orElse(null)
                            : null;
                    return GoalTask.builder()
                            .goal(savedGoal)
                            .title(tr.getTitle())
                            .description(tr.getDescription())
                            .taskType(tr.getTaskType())
                            .targetValue(tr.getTargetValue())
                            .sortOrder(tr.getSortOrder() != null ? tr.getSortOrder() : 0)
                            .source(source)
                            .build();
                })
                .collect(Collectors.toList());

        goalTaskRepository.saveAll(tasks);
        savedGoal.setTasks(tasks);

        if (!request.isTemplate() && !request.isApplyToAll() && request.getStudentIds() != null) {
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

    @Transactional
    public GoalResponse updateGoal(Long id, GoalRequest request, User user) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Goal not found"));

        if (user.getRole() != Role.ADMIN && !goal.getCreatedBy().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied: You cannot update this goal");
        }

        GoalType goalType = goalTypeRepository.findById(request.getGoalTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Goal type not found"));

        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());
        goal.setGoalType(goalType);
        goal.setApplyToAll(request.isApplyToAll());
        goal.setStartDate(request.getStartDate());
        goal.setEndDate(request.getEndDate());

        goal.getTasks().clear();
        List<GoalTask> newTasks = request.getTasks().stream()
                .map(tr -> {
                    Source source = tr.getSourceId() != null
                            ? sourceRepository.findById(tr.getSourceId()).orElse(null)
                            : null;
                    return GoalTask.builder()
                            .goal(goal)
                            .title(tr.getTitle())
                            .description(tr.getDescription())
                            .taskType(tr.getTaskType())
                            .targetValue(tr.getTargetValue())
                            .sortOrder(tr.getSortOrder() != null ? tr.getSortOrder() : 0)
                            .source(source)
                            .build();
                })
                .collect(Collectors.toList());
        goal.getTasks().addAll(newTasks);

        if (!goal.getIsTemplate()) {
            goalStudentRepository.deleteByGoal(goal);
            if (!request.isApplyToAll() && request.getStudentIds() != null) {
                List<GoalStudent> studentMappings = request.getStudentIds().stream()
                        .map(sid -> {
                            User student = userRepository.findById(sid)
                                    .orElseThrow(() -> new EntityNotFoundException("Student not found: " + sid));
                            return GoalStudent.builder().goal(goal).student(student).build();
                        })
                        .collect(Collectors.toList());
                goalStudentRepository.saveAll(studentMappings);
            }
        }

        return GoalResponse.fromEntity(goalRepository.save(goal));
    }

    public GoalResponse getGoalById(Long id) {
        return goalRepository.findById(id)
                .map(GoalResponse::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("Goal not found"));
    }

    public Page<GoalResponse> getGoalsForClass(Long classId, User user, Pageable pageable) {
        MentorClass mentorClass = classRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException("Class not found"));

        return goalRepository.findByMentorClassAndActiveTrue(mentorClass, pageable)
                .map(GoalResponse::fromEntity);
    }

    @Transactional
    public void deleteGoal(Long id, User user) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Goal not found"));

        if (user.getRole() != Role.ADMIN && !goal.getCreatedBy().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied: You cannot delete this goal");
        }

        goal.setActive(false);
        goalRepository.save(goal);
    }

    // --- Student View ---

    public List<GoalResponse> getGoalsForStudent(User student) {
        ClassStudent activeEnrollment = classStudentRepository.findByStudentAndActiveTrue(student)
                .orElse(null);

        if (activeEnrollment == null || activeEnrollment.getMentorClass() == null)
            return Collections.emptyList();

        List<Goal> classGoals = goalRepository.findByMentorClassAndActiveTrue(activeEnrollment.getMentorClass())
                .stream().filter(Goal::getApplyToAll).collect(Collectors.toList());

        List<Goal> privateGoals = goalStudentRepository.findByStudent(student)
                .stream().map(GoalStudent::getGoal).collect(Collectors.toList());

        classGoals.addAll(privateGoals);
        return classGoals.stream().distinct().map(GoalResponse::fromEntity).collect(Collectors.toList());
    }
}
