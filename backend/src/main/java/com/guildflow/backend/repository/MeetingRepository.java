package com.guildflow.backend.repository;

import com.guildflow.backend.model.Meeting;
import com.guildflow.backend.model.MentorClass;
import com.guildflow.backend.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByMentorClassOrderByStartTimeDesc(MentorClass mentorClass);

    List<Meeting> findByMentorClassInOrderByStartTimeDesc(List<MentorClass> classes);

    List<Meeting> findByMentorClassMentorOrderByStartTimeDesc(User mentor);

    List<Meeting> findByMentorClassAndStartTimeBetween(MentorClass mentorClass, LocalDateTime start, LocalDateTime end);

    List<Meeting> findByRecurrenceGroupId(String recurrenceGroupId);

    List<Meeting> findAllByOrderByStartTimeDesc();

    Page<Meeting> findByMentorClassOrderByStartTimeDesc(MentorClass mentorClass, Pageable pageable);

    Page<Meeting> findByMentorClassMentorOrderByStartTimeDesc(User mentor, Pageable pageable);

    Page<Meeting> findAllByOrderByStartTimeDesc(Pageable pageable);

    long countByMentorClassAndStartTimeBefore(MentorClass mentorClass, java.time.LocalDateTime cutoff);
}
