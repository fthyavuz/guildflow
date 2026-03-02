package com.guildflow.backend.repository;

import com.guildflow.backend.model.Goal;
import com.guildflow.backend.model.MentorClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByMentorClassAndActiveTrue(MentorClass mentorClass);

    long countByActiveTrue();
}
