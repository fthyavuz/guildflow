package com.guildflow.backend.repository;

import com.guildflow.backend.model.StudentEvaluation;
import com.guildflow.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentEvaluationRepository extends JpaRepository<StudentEvaluation, Long> {
    List<StudentEvaluation> findByStudentOrderByCreatedAtDesc(User student);

    List<StudentEvaluation> findByMentorAndStudentOrderByCreatedAtDesc(User mentor, User student);
}
