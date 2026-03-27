package com.guildflow.backend.model;

import com.guildflow.backend.model.enums.Frequency;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "class_homework_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassHomeworkAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private MentorClass mentorClass;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", length = 10)
    private Frequency frequency;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "apply_to_all", nullable = false)
    @Builder.Default
    private Boolean applyToAll = true;

    @ElementCollection
    @CollectionTable(name = "class_homework_assignment_students",
            joinColumns = @JoinColumn(name = "assignment_id"))
    @Column(name = "student_id")
    @Builder.Default
    private List<Long> studentIds = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
