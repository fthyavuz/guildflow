package com.guildflow.backend.repository;

import com.guildflow.backend.model.RoomBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RoomBookingRepository extends JpaRepository<RoomBooking, Long> {
    
    List<RoomBooking> findByRoomId(Long roomId);

    @Query("SELECT rb FROM RoomBooking rb WHERE rb.room.id = :roomId AND " +
           "((rb.startTime < :endTime AND rb.endTime > :startTime))")
    List<RoomBooking> findOverlappingBookings(@Param("roomId") Long roomId, 
                                            @Param("startTime") LocalDateTime startTime, 
                                            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT rb FROM RoomBooking rb WHERE " +
           "rb.startTime >= :startDate AND rb.endTime <= :endDate")
    List<RoomBooking> findBookingsInDateRange(@Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);
}
