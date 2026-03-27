package com.guildflow.backend.repository;

import com.guildflow.backend.model.ClassHomeworkAssignment;
import com.guildflow.backend.model.MentorClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassHomeworkAssignmentRepository extends JpaRepository<ClassHomeworkAssignment, Long> {
    List<ClassHomeworkAssignment> findByMentorClassOrderByCreatedAtDesc(MentorClass mentorClass);
}
