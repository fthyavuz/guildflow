package com.guildflow.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SystemStatsResponse {
    private long totalClasses;
    private long totalStudents;
    private long totalMentors;
    private long activeQuests;
}
