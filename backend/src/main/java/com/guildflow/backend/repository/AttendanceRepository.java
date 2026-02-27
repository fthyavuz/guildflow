package com.guildflow.backend.repository;

import com.guildflow.backend.model.Attendance;
import com.guildflow.backend.model.Meeting;
import com.guildflow.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByMeeting(Meeting meeting);

    List<Attendance> findByStudent(User student);

    Optional<Attendance> findByMeetingAndStudent(Meeting meeting, User student);
}
