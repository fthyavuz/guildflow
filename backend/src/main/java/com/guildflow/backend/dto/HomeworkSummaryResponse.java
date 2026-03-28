package com.guildflow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeworkSummaryResponse {
    private Long assignmentId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String frequency;
    private int taskCount;
    private double overallProgress;
}
