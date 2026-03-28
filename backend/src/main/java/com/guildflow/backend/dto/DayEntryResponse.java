package com.guildflow.backend.dto;

import com.guildflow.backend.model.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayEntryResponse {
    private Long taskId;
    private String title;
    private TaskType taskType;
    private Double targetValue;

    /** Cumulative total across all saved entries (NUMBER tasks only). */
    private Double cumulativeValue;

    /** The entry id for this specific date (null if not yet entered). */
    private Long entryId;

    /** The value entered for this specific date (null if not yet entered). */
    private Double numericEntry;
    private Boolean booleanEntry;

    /** True when this day's entries have been saved and locked. */
    private boolean dayLocked;

    /** True for CHECKBOX tasks that have been permanently marked done. */
    private boolean donePermanently;

    /** Daily limit from the linked resource (null if task has no source). */
    private Double dailyLimit;
}
