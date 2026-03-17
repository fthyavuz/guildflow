package com.guildflow.backend.repository;

import com.guildflow.backend.model.EventAssignment;
import com.guildflow.backend.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventAssignmentRepository extends JpaRepository<EventAssignment, Long> {
    List<EventAssignment> findByEvent(Event event);
    List<EventAssignment> findByEventId(Long eventId);
}
