package com.guildflow.backend.repository;

import com.guildflow.backend.model.Goal;
import com.guildflow.backend.model.MentorClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByMentorClassAndActiveTrue(MentorClass mentorClass);

    @Query("SELECT DISTINCT g FROM Goal g LEFT JOIN FETCH g.tasks WHERE g.mentorClass = :mentorClass AND g.active = true")
    List<Goal> findByMentorClassAndActiveTrueWithTasks(@Param("mentorClass") MentorClass mentorClass);
    List<Goal> findByIsTemplateTrueAndActiveTrue();
    List<Goal> findByIsTemplateTrue();
    List<Goal> findByIsTemplateFalseAndMentorClassAndActiveTrue(MentorClass mentorClass);

    Page<Goal> findByIsTemplateTrueAndActiveTrue(Pageable pageable);
    Page<Goal> findByMentorClassAndActiveTrue(MentorClass mentorClass, Pageable pageable);
    List<Goal> findByTitle(String title);

    long countByActiveTrue();
}
