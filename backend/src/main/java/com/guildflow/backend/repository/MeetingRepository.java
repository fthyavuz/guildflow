package com.guildflow.backend.repository;

import com.guildflow.backend.model.Meeting;
import com.guildflow.backend.model.MentorClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByMentorClassOrderByStartTimeDesc(MentorClass mentorClass);

    List<Meeting> findByMentorClassAndStartTimeBetween(MentorClass mentorClass, LocalDateTime start, LocalDateTime end);

    List<Meeting> findByRecurrenceGroupId(String recurrenceGroupId);
}
