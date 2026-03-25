package com.guildflow.backend.dto;

import com.guildflow.backend.model.TaskProgress;
import com.guildflow.backend.model.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingProgressResponse {
    private Long entryId;
    private Long taskId;
    private String taskTitle;
    private TaskType taskType;
    private Long goalId;
    private String goalTitle;
    private Long studentId;
    private String studentName;
    private LocalDate entryDate;
    private Double numericValue;
    private Boolean booleanValue;
    private LocalDateTime submittedAt;

    public static PendingProgressResponse fromEntity(TaskProgress tp) {
        return PendingProgressResponse.builder()
                .entryId(tp.getId())
                .taskId(tp.getTask().getId())
                .taskTitle(tp.getTask().getTitle())
                .taskType(tp.getTask().getTaskType())
                .goalId(tp.getTask().getGoal().getId())
                .goalTitle(tp.getTask().getGoal().getTitle())
                .studentId(tp.getStudent().getId())
                .studentName(tp.getStudent().getFirstName() + " " + tp.getStudent().getLastName())
                .entryDate(tp.getEntryDate())
                .numericValue(tp.getNumericValue())
                .booleanValue(tp.getBooleanValue())
                .submittedAt(tp.getCreatedAt())
                .build();
    }
}
