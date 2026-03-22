package com.guildflow.backend.repository;

import com.guildflow.backend.model.Goal;
import com.guildflow.backend.model.GoalStudent;
import com.guildflow.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalStudentRepository extends JpaRepository<GoalStudent, Long> {
    List<GoalStudent> findByGoal(Goal goal);

    List<GoalStudent> findByStudent(User student);

    @Query("SELECT gs FROM GoalStudent gs JOIN FETCH gs.goal g LEFT JOIN FETCH g.tasks WHERE gs.student = :student")
    List<GoalStudent> findByStudentWithGoalTasks(@Param("student") User student);

    @Query("SELECT gs FROM GoalStudent gs JOIN FETCH gs.goal g LEFT JOIN FETCH g.tasks WHERE gs.student IN :students")
    List<GoalStudent> findByStudentsWithGoalTasks(@Param("students") List<User> students);

    Optional<GoalStudent> findByGoalAndStudent(Goal goal, User student);
    void deleteByGoal(Goal goal);
}
