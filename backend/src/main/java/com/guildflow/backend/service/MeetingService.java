package com.guildflow.backend.service;

import com.guildflow.backend.dto.AttendanceRequest;
import com.guildflow.backend.dto.AttendanceResponse;
import com.guildflow.backend.dto.AttendanceSummaryResponse;
import com.guildflow.backend.dto.MeetingRequest;
import com.guildflow.backend.dto.MeetingResponse;
import com.guildflow.backend.exception.ConflictException;
import com.guildflow.backend.exception.EntityNotFoundException;
import com.guildflow.backend.exception.ForbiddenException;
import com.guildflow.backend.exception.ValidationException;
import com.guildflow.backend.util.SecurityUtils;
import com.guildflow.backend.model.Attendance;
import com.guildflow.backend.model.Meeting;
import com.guildflow.backend.model.MentorClass;
import com.guildflow.backend.model.Room;
import com.guildflow.backend.model.RoomBooking;
import com.guildflow.backend.model.User;
import com.guildflow.backend.model.enums.AttendanceStatus;
import com.guildflow.backend.model.enums.Role;
import com.guildflow.backend.repository.AttendanceRepository;
import com.guildflow.backend.repository.ClassStudentRepository;
import com.guildflow.backend.repository.MeetingRepository;
import com.guildflow.backend.repository.MentorClassRepository;
import com.guildflow.backend.repository.RoomBookingRepository;
import com.guildflow.backend.repository.RoomRepository;
import com.guildflow.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    private final RoomRepository roomRepository;
    private final RoomBookingRepository roomBookingRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public List<MeetingResponse> createMeeting(MeetingRequest request, User currentUser) {
        MentorClass mentorClass = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new EntityNotFoundException("Class not found"));

        securityUtils.validateUserState(currentUser);
        securityUtils.requireAdminOrOwner(currentUser, mentorClass.getMentor(), "Access denied");

        Room room = null;
        if (request.getRoomId() != null) {
            room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new EntityNotFoundException("Room not found with id: " + request.getRoomId()));
        }

        List<Meeting> meetingsToSave = new ArrayList<>();

        if (request.isRecurring()) {
            // Pre-validate all slots before saving anything
            if (room != null) {
                for (int i = 0; i < request.getRecurrenceCount(); i++) {
                    var slotStart = request.getStartTime().plusWeeks(i);
                    var slotEnd = request.getEndTime().plusWeeks(i);
                    if (!roomBookingRepository.findOverlappingBookings(room.getId(), slotStart, slotEnd).isEmpty()) {
                        throw new ConflictException("Room '" + room.getTitle() + "' is already booked at occurrence " + (i + 1) + " (" + slotStart.toLocalDate() + ")");
                    }
                }
            }
            String recurrenceId = UUID.randomUUID().toString();
            for (int i = 0; i < request.getRecurrenceCount(); i++) {
                var slotStart = request.getStartTime().plusWeeks(i);
                var slotEnd = request.getEndTime().plusWeeks(i);
                RoomBooking booking = room != null ? createRoomBooking(room, currentUser, request.getTitle(), slotStart, slotEnd) : null;
                meetingsToSave.add(Meeting.builder()
                        .mentorClass(mentorClass)
                        .title(request.getTitle())
                        .description(request.getDescription())
                        .startTime(slotStart)
                        .endTime(slotEnd)
                        .location(request.getLocation())
                        .isRecurring(true)
                        .recurrenceGroupId(recurrenceId)
                        .room(room)
                        .roomBooking(booking)
                        .build());
            }
        } else {
            if (room != null && !roomBookingRepository.findOverlappingBookings(room.getId(), request.getStartTime(), request.getEndTime()).isEmpty()) {
                throw new ConflictException("Room '" + room.getTitle() + "' is already booked for this time period");
            }
            RoomBooking booking = room != null ? createRoomBooking(room, currentUser, request.getTitle(), request.getStartTime(), request.getEndTime()) : null;
            meetingsToSave.add(Meeting.builder()
                    .mentorClass(mentorClass)
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .location(request.getLocation())
                    .isRecurring(false)
                    .room(room)
                    .roomBooking(booking)
                    .build());
        }

        return meetingRepository.saveAll(meetingsToSave).stream()
                .map(MeetingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public MeetingResponse updateMeeting(Long id, MeetingRequest request, User currentUser) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found with id: " + id));

        securityUtils.validateUserState(currentUser);
        securityUtils.requireAdminOrOwner(currentUser, meeting.getMentorClass().getMentor(), "Access denied");

        // Release old room booking if any
        if (meeting.getRoomBooking() != null) {
            Long oldBookingId = meeting.getRoomBooking().getId();
            meeting.setRoomBooking(null);
            meeting.setRoom(null);
            meetingRepository.save(meeting);
            roomBookingRepository.deleteById(oldBookingId);
        }

        // Assign new room if requested
        Room newRoom = null;
        RoomBooking newBooking = null;
        if (request.getRoomId() != null) {
            newRoom = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new EntityNotFoundException("Room not found with id: " + request.getRoomId()));
            var start = request.getStartTime() != null ? request.getStartTime() : meeting.getStartTime();
            var end = request.getEndTime() != null ? request.getEndTime() : meeting.getEndTime();
            if (!roomBookingRepository.findOverlappingBookings(newRoom.getId(), start, end).isEmpty()) {
                throw new ConflictException("Room '" + newRoom.getTitle() + "' is already booked for this time period");
            }
            newBooking = createRoomBooking(newRoom, currentUser, request.getTitle() != null ? request.getTitle() : meeting.getTitle(), start, end);
        }

        if (request.getTitle() != null) meeting.setTitle(request.getTitle());
        if (request.getDescription() != null) meeting.setDescription(request.getDescription());
        if (request.getStartTime() != null) meeting.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) meeting.setEndTime(request.getEndTime());
        if (request.getLocation() != null) meeting.setLocation(request.getLocation());
        meeting.setRoom(newRoom);
        meeting.setRoomBooking(newBooking);

        return MeetingResponse.fromEntity(meetingRepository.save(meeting));
    }

    @Transactional
    public void deleteMeeting(Long id, User currentUser) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found with id: " + id));

        securityUtils.validateUserState(currentUser);
        securityUtils.requireAdminOrOwner(currentUser, meeting.getMentorClass().getMentor(), "Access denied");

        // Release room booking first to avoid FK constraint issues
        if (meeting.getRoomBooking() != null) {
            Long bookingId = meeting.getRoomBooking().getId();
            meeting.setRoomBooking(null);
            meeting.setRoom(null);
            meetingRepository.save(meeting);
            roomBookingRepository.deleteById(bookingId);
        }

        meetingRepository.delete(meeting);
    }

    private RoomBooking createRoomBooking(Room room, User bookedBy, String reason, java.time.LocalDateTime start, java.time.LocalDateTime end) {
        return roomBookingRepository.save(RoomBooking.builder()
                .room(room)
                .bookedBy(bookedBy)
                .reason("Meeting: " + reason)
                .startTime(start)
                .endTime(end)
                .build());
    }

    public MeetingResponse getMeetingById(Long id, User currentUser) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found with id: " + id));
        securityUtils.validateUserState(currentUser);
        return MeetingResponse.fromEntity(meeting);
    }

    public Page<MeetingResponse> getMeetingsForClass(Long classId, User user, Pageable pageable) {
        MentorClass mentorClass = classRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException("Class not found"));

        return meetingRepository.findByMentorClassOrderByStartTimeDesc(mentorClass, pageable)
                .map(MeetingResponse::fromEntity);
    }

    public Page<MeetingResponse> getMyMeetings(User user, Pageable pageable) {
        if (user.getRole() == Role.MENTOR) {
            return meetingRepository.findByMentorClassMentorOrderByStartTimeDesc(user, pageable)
                    .map(MeetingResponse::fromEntity);
        } else if (user.getRole() == Role.STUDENT) {
            return classStudentRepository.findByStudentAndActiveTrue(user)
                    .map(cs -> meetingRepository.findByMentorClassOrderByStartTimeDesc(cs.getMentorClass(), pageable)
                            .map(MeetingResponse::fromEntity))
                    .orElse(Page.empty(pageable));
        } else if (user.getRole() == Role.ADMIN) {
            return meetingRepository.findAllByOrderByStartTimeDesc(pageable)
                    .map(MeetingResponse::fromEntity);
        }
        return Page.empty(pageable);
    }

    @Transactional
    public void markAttendance(Long meetingId, List<AttendanceRequest> attendanceRequests, User currentUser) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found"));

        securityUtils.validateUserState(currentUser);
        securityUtils.requireAdminOrOwner(currentUser, meeting.getMentorClass().getMentor(), "Access denied");

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // Attendance can only be recorded for past meetings
        if (!meeting.getStartTime().isBefore(now)) {
            throw new ValidationException("Attendance can only be recorded after the meeting has started");
        }

        // Mentors are restricted to meetings within the last 30 days
        if (currentUser.getRole() == Role.MENTOR) {
            java.time.LocalDateTime cutoff = now.minusMonths(1);
            if (meeting.getStartTime().isBefore(cutoff)) {
                throw new ForbiddenException("Mentors can only record attendance for meetings within the last month");
            }
        }

        for (AttendanceRequest ar : attendanceRequests) {
            User student = userRepository.findById(ar.getStudentId())
                    .orElseThrow(() -> new EntityNotFoundException("Student not found: " + ar.getStudentId()));

            if (!classStudentRepository.existsByMentorClassAndStudentAndActiveTrue(meeting.getMentorClass(), student)) {
                throw new ValidationException("Student " + ar.getStudentId() + " is not enrolled in this class");
            }

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
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found"));

        securityUtils.validateUserState(currentUser);
        securityUtils.requireAdminOrOwner(currentUser, meeting.getMentorClass().getMentor(), "Access denied");

        return attendanceRepository.findByMeeting(meeting).stream()
                .map(AttendanceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<AttendanceResponse> getStudentAttendanceHistory(Long studentId, User currentUser) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        securityUtils.validateUserState(currentUser);

        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(studentId)) {
            throw new ForbiddenException("Access denied");
        }

        return attendanceRepository.findByStudent(student).stream()
                .map(AttendanceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public AttendanceSummaryResponse getStudentAttendanceSummary(Long studentId, User currentUser) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        securityUtils.validateUserState(currentUser);

        if (currentUser.getRole() != Role.ADMIN
                && currentUser.getRole() != Role.MENTOR
                && !currentUser.getId().equals(studentId)) {
            throw new ForbiddenException("Access denied");
        }

        var classStudentOpt = classStudentRepository.findByStudentAndActiveTrue(student);
        if (classStudentOpt.isEmpty()) {
            return AttendanceSummaryResponse.builder().build();
        }

        MentorClass mentorClass = classStudentOpt.get().getMentorClass();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        long total = meetingRepository.countByMentorClassAndStartTimeBefore(mentorClass, now);

        List<Attendance> attendances = attendanceRepository.findByStudent(student);

        int present = (int) attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        int absent  = (int) attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
        int late    = (int) attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
        int excused = (int) attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.EXCUSED).count();

        double attendanceRate = total > 0 ? Math.round((double) present / total * 1000.0) / 10.0 : 0.0;

        return AttendanceSummaryResponse.builder()
                .total((int) total)
                .present(present)
                .absent(absent)
                .late(late)
                .excused(excused)
                .attendanceRate(attendanceRate)
                .build();
    }
}
