package com.guildflow.backend.repository;

import com.guildflow.backend.model.Goal;
import com.guildflow.backend.model.GoalStudent;
import com.guildflow.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalStudentRepository extends JpaRepository<GoalStudent, Long> {
    List<GoalStudent> findByGoal(Goal goal);

    List<GoalStudent> findByStudent(User student);

    Optional<GoalStudent> findByGoalAndStudent(Goal goal, User student);
}
