package com.guildflow.backend.service;

import com.guildflow.backend.dto.*;
import com.guildflow.backend.exception.EntityNotFoundException;
import com.guildflow.backend.exception.ForbiddenException;
import com.guildflow.backend.exception.ValidationException;
import com.guildflow.backend.model.*;
import com.guildflow.backend.model.enums.Role;
import com.guildflow.backend.model.enums.TaskType;
import com.guildflow.backend.repository.*;
import com.guildflow.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ClassService {

    private final MentorClassRepository classRepository;
    private final ClassStudentRepository classStudentRepository;
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final GoalStudentRepository goalStudentRepository;
    private final TaskProgressRepository taskProgressRepository;
    private final GoalProgressService goalProgressService;
    private final EvaluationService evaluationService;
    private final SecurityUtils securityUtils;

    @Transactional
    public ClassResponse createClass(User currentUser, CreateClassRequest request) {
        User assignedMentor = currentUser;

        if (currentUser.getRole() == Role.ADMIN && request.getMentorId() != null) {
            assignedMentor = userRepository.findById(request.getMentorId())
                    .orElseThrow(() -> new EntityNotFoundException("Mentor not found with id: " + request.getMentorId()));
            if (assignedMentor.getRole() != Role.MENTOR) {
                throw new ValidationException("Assigned user is not a mentor");
            }
        }

        MentorClass mentorClass = MentorClass.builder()
                .name(request.getName())
                .description(request.getDescription())
                .educationLevel(request.getEducationLevel())
                .mentor(assignedMentor)
                .build();

        MentorClass savedClass = classRepository.save(mentorClass);
        return ClassResponse.fromEntity(savedClass);
    }

    public List<ClassResponse> getAllActiveClasses() {
        return classRepository.findByActiveTrue()
                .stream()
                .map(ClassResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public Page<ClassResponse> getClasses(User user, Pageable pageable) {
        if (user == null || user.getRole() == null) {
            return Page.empty(pageable);
        }

        if (user.getRole() == Role.ADMIN) {
            return classRepository.findByActiveTrue(pageable).map(ClassResponse::fromEntity);
        }
        return classRepository.findByMentorAndActiveTrue(user, pageable).map(ClassResponse::fromEntity);
    }

    public ClassResponse getClassById(Long id, User currentUser) {
        MentorClass mentorClass = classRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Class not found with id: " + id));

        securityUtils.validateUserState(currentUser);

        User mentor = mentorClass.getMentor();
        if (mentor == null) {
            throw new EntityNotFoundException("Class mentor not found");
        }

        securityUtils.requireAdminOrOwner(currentUser, mentor,
                "Access denied: You are not the mentor of this class");

        return ClassResponse.fromEntity(mentorClass);
    }

    @Transactional
    public ClassResponse updateClass(Long id, CreateClassRequest request, User currentUser) {
        MentorClass mentorClass = classRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Class not found with id: " + id));

        securityUtils.validateUserState(currentUser);
        securityUtils.requireAdminOrOwner(currentUser, mentorClass.getMentor(),
                "Access denied: You cannot edit this class");

        mentorClass.setName(request.getName());
        mentorClass.setDescription(request.getDescription());
        mentorClass.setEducationLevel(request.getEducationLevel());

        if (securityUtils.isAdmin(currentUser) && request.getMentorId() != null) {
            User newMentor = userRepository.findById(request.getMentorId())
                    .orElseThrow(() -> new EntityNotFoundException("Mentor not found with id: " + request.getMentorId()));
            if (newMentor.getRole() != Role.MENTOR) {
                throw new ValidationException("Assigned user is not a mentor");
            }
            mentorClass.setMentor(newMentor);
        }

        MentorClass updatedClass = classRepository.save(mentorClass);
        return ClassResponse.fromEntity(updatedClass);
    }

    @Transactional
    public void addStudentToClass(Long classId, Long studentId, User currentUser) {
        MentorClass mentorClass = classRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException("Class not found"));

        securityUtils.requireAdminOrOwner(currentUser, mentorClass.getMentor(),
                "Access denied: You cannot add students to this class");

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        if (student.getRole() != Role.STUDENT) {
            throw new ValidationException("User is not a student");
        }

        // Deactivate old active enrollment in OTHER classes if exists
        classStudentRepository.findByStudentAndActiveTrue(student).ifPresent(oldEnrollment -> {
            if (!oldEnrollment.getMentorClass().getId().equals(classId)) {
                oldEnrollment.setActive(false);
                oldEnrollment.setLeftAt(LocalDateTime.now());
                classStudentRepository.save(oldEnrollment);
            }
        });

        // Pessimistic write lock prevents concurrent duplicate inserts
        Optional<ClassStudent> existingEnrollment = classStudentRepository.findByMentorClassAndStudentForUpdate(mentorClass,
                student);

        if (existingEnrollment.isPresent()) {
            ClassStudent enrollment = existingEnrollment.get();
            if (!enrollment.getActive()) {
                enrollment.setActive(true);
                enrollment.setLeftAt(null);
                classStudentRepository.save(enrollment);
            }
        } else {
            ClassStudent enrollment = ClassStudent.builder()
                    .mentorClass(mentorClass)
                    .student(student)
                    .build();
            classStudentRepository.save(enrollment);
        }
    }

    @Transactional
    public void removeStudentFromClass(Long classId, Long studentId, User currentUser) {
        MentorClass mentorClass = classRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException("Class not found"));

        securityUtils.requireAdminOrOwner(currentUser, mentorClass.getMentor(),
                "Access denied: You cannot remove students from this class");

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        ClassStudent enrollment = classStudentRepository.findByStudentAndActiveTrue(student)
                .filter(e -> e.getMentorClass().getId().equals(classId))
                .orElseThrow(() -> new EntityNotFoundException("Student not enrolled in this class"));

        enrollment.setActive(false);
        enrollment.setLeftAt(LocalDateTime.now());
        classStudentRepository.save(enrollment);
    }

    public List<StudentProgressSummary> getClassProgressSummary(Long classId, User currentUser) {
        MentorClass mentorClass = classRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException("Class not found"));

        securityUtils.requireAdminOrOwner(currentUser, mentorClass.getMentor(), "Access denied");

        List<ClassStudent> activeEnrollments = classStudentRepository.findByMentorClassAndActiveTrue(mentorClass);
        if (activeEnrollments.isEmpty()) return Collections.emptyList();

        List<User> students = activeEnrollments.stream().map(ClassStudent::getStudent).collect(Collectors.toList());

        // 1. Class-wide goals with tasks — one query, shared across all students
        List<Goal> classGoals = goalRepository.findByMentorClassAndActiveTrueWithTasks(mentorClass)
                .stream().filter(Goal::getApplyToAll).collect(Collectors.toList());

        // 2. Private goal assignments for all students — one query
        List<GoalStudent> privateAssignments = goalStudentRepository.findByStudentsWithGoalTasks(students);

        // 3. Build per-student goal sets in memory
        Map<Long, Set<Goal>> goalsByStudentId = new HashMap<>();
        for (User student : students) {
            goalsByStudentId.put(student.getId(), new HashSet<>(classGoals));
        }
        for (GoalStudent gs : privateAssignments) {
            goalsByStudentId.computeIfAbsent(gs.getStudent().getId(), k -> new HashSet<>()).add(gs.getGoal());
        }

        // 4. Collect all unique task IDs across all goals
        List<Long> allTaskIds = goalsByStudentId.values().stream()
                .flatMap(Collection::stream)
                .flatMap(g -> g.getTasks().stream())
                .map(GoalTask::getId)
                .distinct()
                .collect(Collectors.toList());

        // 5. All progress for all students and all tasks — one query
        Map<Long, Map<Long, List<TaskProgress>>> progressByStudentAndTask = allTaskIds.isEmpty()
                ? Collections.emptyMap()
                : taskProgressRepository.findByTaskIdsAndStudents(allTaskIds, students).stream()
                        .collect(Collectors.groupingBy(
                                tp -> tp.getStudent().getId(),
                                Collectors.groupingBy(tp -> tp.getTask().getId())));

        // 6. Compute per-student summaries in memory — no more DB calls
        return students.stream().map(student -> {
            Set<Goal> studentGoals = goalsByStudentId.getOrDefault(student.getId(), Collections.emptySet());
            Map<Long, List<TaskProgress>> studentProgress = progressByStudentAndTask.getOrDefault(student.getId(),
                    Collections.emptyMap());

            List<Double> goalProgresses = studentGoals.stream().map(goal -> {
                List<Double> taskPercentages = goal.getTasks().stream().map(task -> {
                    List<TaskProgress> entries = studentProgress.getOrDefault(task.getId(), Collections.emptyList());
                    double currentVal = 0.0;
                    if (task.getTaskType() == TaskType.NUMBER) {
                        currentVal = entries.stream()
                                .mapToDouble(e -> e.getNumericValue() != null ? e.getNumericValue() : 0.0).sum();
                    } else if (task.getTaskType() == TaskType.CHECKBOX) {
                        currentVal = entries.stream().filter(e -> Boolean.TRUE.equals(e.getBooleanValue())).count();
                    }
                    double pct = task.getTargetValue() != null && task.getTargetValue() > 0
                            ? Math.min((currentVal / task.getTargetValue()) * 100, 100)
                            : 0;
                    return pct;
                }).collect(Collectors.toList());
                return taskPercentages.isEmpty() ? 0.0
                        : taskPercentages.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            }).collect(Collectors.toList());

            double avgProgress = goalProgresses.isEmpty() ? 0.0
                    : goalProgresses.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

            return StudentProgressSummary.builder()
                    .studentId(student.getId())
                    .firstName(student.getFirstName())
                    .lastName(student.getLastName())
                    .email(student.getEmail())
                    .averageProgress(avgProgress)
                    .activeGoalsCount(studentGoals.size())
                    .build();
        }).collect(Collectors.toList());
    }

    public List<UserResponse> getClassStudents(Long classId, User currentUser) {
        MentorClass mentorClass = classRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException("Class not found"));

        securityUtils.validateUserState(currentUser);
        securityUtils.requireAdminOrOwner(currentUser, mentorClass.getMentor(), "Access denied");

        List<ClassStudent> activeEnrollments = classStudentRepository.findByMentorClassAndActiveTrue(mentorClass);
        return activeEnrollments.stream()
                .map(e -> UserResponse.fromEntity(e.getStudent()))
                .collect(Collectors.toList());
    }

    public StudentProfileResponse getStudentProfile(Long studentId, User currentUser) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        if (student.getRole() != Role.STUDENT) {
            throw new ValidationException("User is not a student");
        }

        ClassStudent activeEnrollment = classStudentRepository.findByStudentAndActiveTrue(student)
                .orElse(null);

        if (!securityUtils.isAdmin(currentUser)) {
            if (activeEnrollment == null
                    || !activeEnrollment.getMentorClass().getMentor().getId().equals(currentUser.getId())) {
                throw new ForbiddenException("Access denied: You are not this student's mentor");
            }
        }

        ClassResponse classInfo = activeEnrollment != null
                ? ClassResponse.fromEntity(activeEnrollment.getMentorClass())
                : null;

        List<EvaluationResponse> evaluations = evaluationService.getStudentEvaluations(studentId, currentUser);
        List<HomeworkSummaryResponse> goals = goalProgressService.getStudentHomeworkList(student);

        return StudentProfileResponse.builder()
                .student(UserResponse.fromEntity(student))
                .currentClass(classInfo)
                .evaluations(evaluations)
                .goals(goals)
                .build();
    }

    public SystemStatsResponse getSystemStats(User currentUser) {
        if (!securityUtils.isAdmin(currentUser)) {
            throw new ForbiddenException("Access denied");
        }

        return SystemStatsResponse.builder()
                .totalClasses(classRepository.countByActiveTrue())
                .totalStudents(userRepository.countByRoleAndActiveTrue(Role.STUDENT))
                .totalMentors(userRepository.countByRoleAndActiveTrue(Role.MENTOR))
                .activeQuests(goalRepository.countByActiveTrue())
                .build();
    }
}
