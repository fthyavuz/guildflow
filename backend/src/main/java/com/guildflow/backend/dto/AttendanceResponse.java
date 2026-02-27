package com.guildflow.backend.dto;

import com.guildflow.backend.model.Attendance;
import com.guildflow.backend.model.enums.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private AttendanceStatus status;
    private String note;
    private LocalDateTime recordedAt;

    public static AttendanceResponse fromEntity(Attendance attendance) {
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .studentId(attendance.getStudent().getId())
                .studentName(attendance.getStudent().getFirstName() + " " + attendance.getStudent().getLastName())
                .status(attendance.getStatus())
                .note(attendance.getNote())
                .recordedAt(attendance.getRecordedAt())
                .build();
    }
}
