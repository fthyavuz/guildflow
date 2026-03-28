package com.guildflow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String educationLevel;

    /** Tasks with progressPercentage < 100, grouped by resource category */
    private List<CategorySection> inProgress;

    /** Tasks with progressPercentage >= 100, grouped by resource category */
    private List<CategorySection> finished;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySection {
        private Long categoryId;     // null for "General"
        private String categoryName; // e.g. "Book", "Podcast", "General"
        private List<TaskItem> tasks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskItem {
        private Long taskId;
        private Long assignmentId;
        private String taskTitle;
        private String assignmentTitle;
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
