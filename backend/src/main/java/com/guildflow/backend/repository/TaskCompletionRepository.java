package com.guildflow.backend.repository;

import com.guildflow.backend.model.ClassHomeworkAssignment;
import com.guildflow.backend.model.GoalTask;
import com.guildflow.backend.model.TaskCompletion;
import com.guildflow.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskCompletionRepository extends JpaRepository<TaskCompletion, Long> {

    Optional<TaskCompletion> findByAssignmentAndTaskAndStudent(
            ClassHomeworkAssignment assignment, GoalTask task, User student);

    List<TaskCompletion> findByStudent(User student);

    @Query("SELECT tc FROM TaskCompletion tc WHERE tc.assignment IN :assignments")
    List<TaskCompletion> findByAssignments(
            @Param("assignments") List<ClassHomeworkAssignment> assignments);
}
