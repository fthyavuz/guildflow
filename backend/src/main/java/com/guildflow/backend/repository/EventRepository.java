package com.guildflow.backend.repository;

import com.guildflow.backend.model.Event;
import com.guildflow.backend.model.enums.EducationLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE " +
           "(:startAfter IS NULL OR e.startTime >= :startAfter) AND " +
           "(:endBefore IS NULL OR e.startTime < :endBefore) AND " +
           "(:educationLevel IS NULL OR e.educationLevel = :educationLevel) AND " +
           "(:targetClassId IS NULL OR e.targetClass.id = :targetClassId) " +
           "ORDER BY e.startTime ASC")
    Page<Event> findWithFilters(
            @Param("startAfter") LocalDateTime startAfter,
            @Param("endBefore") LocalDateTime endBefore,
            @Param("educationLevel") EducationLevel educationLevel,
            @Param("targetClassId") Long targetClassId,
            Pageable pageable);
}
