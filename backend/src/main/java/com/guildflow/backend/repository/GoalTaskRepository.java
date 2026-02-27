package com.guildflow.backend.repository;

import com.guildflow.backend.model.Goal;
import com.guildflow.backend.model.GoalTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalTaskRepository extends JpaRepository<GoalTask, Long> {
    List<GoalTask> findByGoal(Goal goal);
}
