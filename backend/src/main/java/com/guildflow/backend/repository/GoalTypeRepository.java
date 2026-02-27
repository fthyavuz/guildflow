package com.guildflow.backend.repository;

import com.guildflow.backend.model.GoalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalTypeRepository extends JpaRepository<GoalType, Long> {
    List<GoalType> findByActiveTrue();

    Optional<GoalType> findByNameAndActiveTrue(String name);
}
