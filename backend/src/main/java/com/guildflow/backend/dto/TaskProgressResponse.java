package com.guildflow.backend.dto;

import com.guildflow.backend.model.enums.ProgressEntryStatus;
import com.guildflow.backend.model.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskProgressResponse {
    private Long taskId;
    private String title;
    private TaskType taskType;
    private Double targetValue;
    private Double currentValue;
    private Double progressPercentage;
    // Per-entry fields (populated when returning individual entries)
    private Long entryId;
    private LocalDate entryDate;
    private ProgressEntryStatus status;
    private String mentorNotes;
}
