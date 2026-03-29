package com.guildflow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSummaryResponse {
    private int total;
    private int present;
    private int absent;
    private int late;
    private int excused;
    private double attendanceRate;
}
