package com.guildflow.backend.dto;

import com.guildflow.backend.model.MentorClass;
import com.guildflow.backend.model.enums.EducationLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassResponse {

    private Long id;
    private String name;
    private String description;
    private EducationLevel educationLevel;
    private Long mentorId;
    private String mentorName;
    private int studentCount;
    private boolean active;
    private LocalDateTime createdAt;

    public static ClassResponse fromEntity(MentorClass mentorClass) {
        return ClassResponse.builder()
                .id(mentorClass.getId())
                .name(mentorClass.getName())
                .description(mentorClass.getDescription())
                .educationLevel(mentorClass.getEducationLevel())
                .mentorId(mentorClass.getMentor().getId())
                .mentorName(mentorClass.getMentor().getFirstName() + " " + mentorClass.getMentor().getLastName())
                .studentCount(mentorClass.getStudents() != null
                        ? (int) mentorClass.getStudents().stream().filter(s -> s.getActive()).count()
                        : 0)
                .active(mentorClass.getActive())
                .createdAt(mentorClass.getCreatedAt())
                .build();
    }
}
