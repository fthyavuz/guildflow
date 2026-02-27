package com.guildflow.backend.dto;

import com.guildflow.backend.model.StudentEvaluation;
import com.guildflow.backend.model.enums.EvaluationPeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResponse {
    private Long id;
    private Long mentorId;
    private String mentorName;
    private Long studentId;
    private String studentName;
    private EvaluationPeriod period;
    private String content;
    private String periodName;
    private LocalDateTime createdAt;

    public static EvaluationResponse fromEntity(StudentEvaluation evaluation) {
        return EvaluationResponse.builder()
                .id(evaluation.getId())
                .mentorId(evaluation.getMentor().getId())
                .mentorName(evaluation.getMentor().getFirstName() + " " + evaluation.getMentor().getLastName())
                .studentId(evaluation.getStudent().getId())
                .studentName(evaluation.getStudent().getFirstName() + " " + evaluation.getStudent().getLastName())
                .period(evaluation.getPeriod())
                .content(evaluation.getContent())
                .periodName(evaluation.getPeriodName())
                .createdAt(evaluation.getCreatedAt())
                .build();
    }
}
