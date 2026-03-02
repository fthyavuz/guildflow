package com.guildflow.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileResponse {
    private UserResponse student;
    private ClassResponse currentClass;
    private List<EvaluationResponse> evaluations;
    private List<GoalProgressResponse> goals;
}
