package com.guildflow.backend.dto;

import com.guildflow.backend.model.ClassHomeworkAssignment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ClassHomeworkAssignmentResponse {
    private Long id;
    private Long goalId;
    private String goalTitle;
    private int taskCount;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean applyToAll;
    private List<Long> studentIds;
    private LocalDateTime createdAt;

    public static ClassHomeworkAssignmentResponse fromEntity(ClassHomeworkAssignment a) {
        return ClassHomeworkAssignmentResponse.builder()
                .id(a.getId())
                .goalId(a.getGoal().getId())
                .goalTitle(a.getGoal().getTitle())
                .taskCount(a.getGoal().getTasks().size())
                .frequency(a.getFrequency() != null ? a.getFrequency().name() : null)
                .startDate(a.getStartDate())
                .endDate(a.getEndDate())
                .applyToAll(a.getApplyToAll())
                .studentIds(a.getStudentIds())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
