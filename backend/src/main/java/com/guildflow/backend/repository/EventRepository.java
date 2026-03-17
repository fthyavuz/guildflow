package com.guildflow.backend.repository;

import com.guildflow.backend.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStartTimeBetweenOrderByStartTimeAsc(LocalDateTime start, LocalDateTime end);
    List<Event> findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime start);
}
