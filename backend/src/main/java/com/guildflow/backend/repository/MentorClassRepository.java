package com.guildflow.backend.repository;

import com.guildflow.backend.model.MentorClass;
import com.guildflow.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentorClassRepository extends JpaRepository<MentorClass, Long> {
    Optional<MentorClass> findByName(String name);

    List<MentorClass> findByMentorAndActiveTrue(User mentor);

    List<MentorClass> findByActiveTrue();

    Page<MentorClass> findByActiveTrue(Pageable pageable);

    Page<MentorClass> findByMentorAndActiveTrue(User mentor, Pageable pageable);

    long countByActiveTrue();
}
