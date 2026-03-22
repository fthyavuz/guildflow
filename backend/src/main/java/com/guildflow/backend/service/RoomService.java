package com.guildflow.backend.service;

import com.guildflow.backend.dto.RoomBookingRequest;
import com.guildflow.backend.dto.RoomBookingResponse;
import com.guildflow.backend.dto.RoomRequest;
import com.guildflow.backend.dto.RoomResponse;
import com.guildflow.backend.exception.ConflictException;
import com.guildflow.backend.exception.EntityNotFoundException;
import com.guildflow.backend.exception.ForbiddenException;
import com.guildflow.backend.exception.ValidationException;
import com.guildflow.backend.model.Room;
import com.guildflow.backend.model.RoomBooking;
import com.guildflow.backend.model.User;
import com.guildflow.backend.repository.RoomBookingRepository;
import com.guildflow.backend.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomBookingRepository roomBookingRepository;

    @Transactional(readOnly = true)
    public Page<RoomResponse> getAllRooms(Pageable pageable) {
        return roomRepository.findAll(pageable).map(this::mapToRoomResponse);
    }

    @Transactional(readOnly = true)
    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Room not found with id: " + id));
        return mapToRoomResponse(room);
    }

    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        if (roomRepository.existsByTitle(request.getTitle())) {
            throw new ConflictException("Room with title already exists: " + request.getTitle());
        }

        Room room = Room.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .capacity(request.getCapacity())
                .canStayOvernight(request.isCanStayOvernight())
                .build();

        room = roomRepository.save(room);
        return mapToRoomResponse(room);
    }

    @Transactional
    public RoomResponse updateRoom(Long id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Room not found with id: " + id));

        if (!room.getTitle().equals(request.getTitle()) && roomRepository.existsByTitle(request.getTitle())) {
            throw new ConflictException("Room with title already exists: " + request.getTitle());
        }

        room.setTitle(request.getTitle());
        room.setDescription(request.getDescription());
        room.setCapacity(request.getCapacity());
        room.setCanStayOvernight(request.isCanStayOvernight());

        room = roomRepository.save(room);
        return mapToRoomResponse(room);
    }

    @Transactional
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new EntityNotFoundException("Room not found with id: " + id);
        }
        roomRepository.deleteById(id);
    }

    // Bookings

    @Transactional(readOnly = true)
    public List<RoomBookingResponse> getBookingsForDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        return roomBookingRepository.findBookingsInDateRange(startOfDay, endOfDay).stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomBookingResponse bookRoom(RoomBookingRequest request, User bookedBy) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().isEqual(request.getEndTime())) {
            throw new ValidationException("End time must be after start time");
        }

        // Check for overlaps
        List<RoomBooking> overlapping = roomBookingRepository.findOverlappingBookings(
                room.getId(), request.getStartTime(), request.getEndTime());

        if (!overlapping.isEmpty()) {
            throw new ConflictException("Room is already booked for this time period");
        }

        RoomBooking booking = RoomBooking.builder()
                .room(room)
                .bookedBy(bookedBy)
                .reason(request.getReason())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        booking = roomBookingRepository.save(booking);
        return mapToBookingResponse(booking);
    }

    @Transactional
    public void deleteBooking(Long bookingId, User user) {
        RoomBooking booking = roomBookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        // Only ADMIN or the person who booked it can delete
        if (!user.getRole().name().equals("ADMIN") && !booking.getBookedBy().getId().equals(user.getId())) {
            throw new ForbiddenException("Not authorized to delete this booking");
        }

        roomBookingRepository.delete(booking);
    }


    private RoomResponse mapToRoomResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .title(room.getTitle())
                .description(room.getDescription())
                .capacity(room.getCapacity())
                .canStayOvernight(room.isCanStayOvernight())
                .bookings(room.getBookings() == null ? List.of() :
                          room.getBookings().stream().map(this::mapToBookingResponse).collect(Collectors.toList()))
                .build();
    }

    private RoomBookingResponse mapToBookingResponse(RoomBooking booking) {
        return RoomBookingResponse.builder()
                .id(booking.getId())
                .roomId(booking.getRoom().getId())
                .bookedById(booking.getBookedBy().getId())
                .bookedByName(booking.getBookedBy().getFirstName() + " " + booking.getBookedBy().getLastName())
                .reason(booking.getReason())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .build();
    }
}
