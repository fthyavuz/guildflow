package com.guildflow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomBookingResponse {
    private Long id;
    private Long roomId;
    private Long bookedById;
    private String bookedByName;
    private String reason;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
