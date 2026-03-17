package com.guildflow.backend.repository;

import com.guildflow.backend.model.EventParticipant;
import com.guildflow.backend.model.Event;
import com.guildflow.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventParticipantRepository extends JpaRepository<EventParticipant, Long> {
    List<EventParticipant> findByEvent(Event event);
    List<EventParticipant> findByEventId(Long eventId);
    Optional<EventParticipant> findByEventAndUser(Event event, User user);
    Optional<EventParticipant> findByEventIdAndUserId(Long eventId, Long userId);
}
