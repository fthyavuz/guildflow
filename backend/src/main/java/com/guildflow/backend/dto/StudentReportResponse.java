package com.guildflow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentReportResponse {

    private Long studentId;
    private String firstName;
    private String lastName;
    private String email;
    private List<AssignmentReport> assignments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignmentReport {
        private Long assignmentId;
        private String title;
        private LocalDate startDate;
        private LocalDate endDate;
        private String frequency;
        private List<TaskReport> tasks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskReport {
        private Long taskId;
        private String title;
        private String taskType;
        private Double targetValue;
        private Double currentValue;
        private Double progressPercentage;
        private boolean approved;
        private LocalDateTime approvedAt;
        private String approvedByName;
        private String approverNotes;
    }
}
