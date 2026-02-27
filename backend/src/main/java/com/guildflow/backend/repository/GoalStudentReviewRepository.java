package com.guildflow.backend.repository;

import com.guildflow.backend.model.Goal;
import com.guildflow.backend.model.GoalStudentReview;
import com.guildflow.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalStudentReviewRepository extends JpaRepository<GoalStudentReview, Long> {
    Optional<GoalStudentReview> findByGoalAndStudent(Goal goal, User student);

    List<GoalStudentReview> findByGoal(Goal goal);
}
