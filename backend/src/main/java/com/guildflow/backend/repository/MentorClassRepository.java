package com.guildflow.backend.repository;

import com.guildflow.backend.model.MentorClass;
import com.guildflow.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentorClassRepository extends JpaRepository<MentorClass, Long> {
    List<MentorClass> findByMentorAndActiveTrue(User mentor);

    List<MentorClass> findByActiveTrue();

    long countByActiveTrue();
}
