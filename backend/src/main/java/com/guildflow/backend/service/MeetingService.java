package com.guildflow.backend.service;

import com.guildflow.backend.dto.AttendanceRequest;
import com.guildflow.backend.dto.AttendanceResponse;
import com.guildflow.backend.dto.MeetingRequest;
import com.guildflow.backend.dto.MeetingResponse;
import com.guildflow.backend.model.Attendance;
import com.guildflow.backend.model.Meeting;
import com.guildflow.backend.model.MentorClass;
import com.guildflow.backend.model.User;
import com.guildflow.backend.model.enums.Role;
import com.guildflow.backend.repository.AttendanceRepository;
import com.guildflow.backend.repository.ClassStudentRepository;
import com.guildflow.backend.repository.MeetingRepository;
import com.guildflow.backend.repository.MentorClassRepository;
import com.guildflow.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final AttendanceRepository attendanceRepository;
    private final MentorClassRepository classRepository;
    private final UserRepository userRepository;
    private final ClassStudentRepository classStudentRepository;

    @Transactional
    public List<MeetingResponse> createMeeting(MeetingRequest request, User currentUser) {
        MentorClass mentorClass = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found"));

        if (currentUser == null || currentUser.getRole() == null) {
            throw new RuntimeException("Access denied: Invalid user state");
        }

        User mentor = mentorClass.getMentor();
        if (mentor == null || (currentUser.getRole() != Role.ADMIN && !mentor.getId().equals(currentUser.getId()))) {
            throw new RuntimeException("Access denied");
        }

        List<Meeting> meetingsToSave = new ArrayList<>();

        if (request.isRecurring()) {
            String recurrenceId = UUID.randomUUID().toString();
            // Create weekly meetings for 3 months (approx 13 weeks)
            for (int i = 0; i < 13; i++) {
                meetingsToSave.add(Meeting.builder()
                        .mentorClass(mentorClass)
                        .title(request.getTitle())
                        .description(request.getDescription())
                        .startTime(request.getStartTime().plusWeeks(i))
                        .endTime(request.getEndTime().plusWeeks(i))
                        .location(request.getLocation())
                        .isRecurring(true)
                        .recurrenceGroupId(recurrenceId)
                        .build());
            }
        } else {
            meetingsToSave.add(Meeting.builder()
                    .mentorClass(mentorClass)
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .location(request.getLocation())
                    .isRecurring(false)
                    .build());
        }

        return meetingRepository.saveAll(meetingsToSave).stream()
                .map(MeetingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MeetingResponse> getMeetingsForClass(Long classId, User user) {
        MentorClass mentorClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        return meetingRepository.findByMentorClassOrderByStartTimeDesc(mentorClass).stream()
                .map(MeetingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MeetingResponse> getMyMeetings(User user) {
        if (user.getRole() == Role.MENTOR) {
            // Mentor sees meetings for all classes they lead
            return meetingRepository.findByMentorClassMentorOrderByStartTimeDesc(user).stream()
                    .map(MeetingResponse::fromEntity)
                    .collect(Collectors.toList());
        } else if (user.getRole() == Role.STUDENT) {
            // Student sees meetings for their active current class
            return classStudentRepository.findByStudentAndActiveTrue(user)
                    .map(cs -> meetingRepository.findByMentorClassOrderByStartTimeDesc(cs.getMentorClass())
                            .stream()
                            .map(MeetingResponse::fromEntity)
                            .collect(Collectors.toList()))
                    .orElse(new ArrayList<>());
        } else if (user.getRole() == Role.ADMIN) {
            return meetingRepository.findAllByOrderByStartTimeDesc().stream()
                    .map(MeetingResponse::fromEntity)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Transactional
    public void markAttendance(Long meetingId, List<AttendanceRequest> attendanceRequests, User currentUser) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));

        if (currentUser == null || currentUser.getRole() == null) {
            throw new RuntimeException("Access denied: Invalid user state");
        }
        if (currentUser.getRole() != Role.ADMIN
                && !meeting.getMentorClass().getMentor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        for (AttendanceRequest ar : attendanceRequests) {
            User student = userRepository.findById(ar.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student not found: " + ar.getStudentId()));

            Attendance attendance = attendanceRepository.findByMeetingAndStudent(meeting, student)
                    .orElse(Attendance.builder()
                            .meeting(meeting)
                            .student(student)
                            .build());

            attendance.setStatus(ar.getStatus());
            attendance.setNote(ar.getNote());
            attendanceRepository.save(attendance);
        }
    }

    public List<AttendanceResponse> getMeetingAttendance(Long meetingId, User currentUser) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));

        if (currentUser == null || currentUser.getRole() == null) {
            throw new RuntimeException("Access denied: Invalid user state");
        }
        if (currentUser.getRole() != Role.ADMIN
                && !meeting.getMentorClass().getMentor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        return attendanceRepository.findByMeeting(meeting).stream()
                .map(AttendanceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<AttendanceResponse> getStudentAttendanceHistory(Long studentId, User currentUser) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (currentUser == null || currentUser.getRole() == null) {
            throw new RuntimeException("Access denied: Invalid user state");
        }

        // Only Admin, the Parent of the student, or the student themselves can see this
        // For now, only Admin and the student
        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(studentId)) {
            throw new RuntimeException("Access denied");
        }

        return attendanceRepository.findByStudent(student).stream()
                .map(AttendanceResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
