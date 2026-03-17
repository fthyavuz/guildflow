package com.guildflow.backend.repository;

import com.guildflow.backend.model.Source;
import com.guildflow.backend.model.enums.SourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {
    List<Source> findByType(SourceType type);
    List<Source> findByTitleContainingIgnoreCase(String title);
}
