package com.guildflow.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentProgressSummary {
    private Long studentId;
    private String firstName;
    private String lastName;
    private String email;
    private Double averageProgress;
    private Integer activeGoalsCount;
}
