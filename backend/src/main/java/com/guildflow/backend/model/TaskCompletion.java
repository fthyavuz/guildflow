package com.guildflow.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_completion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private ClassHomeworkAssignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private GoalTask task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", nullable = false)
    private User approvedBy;

    @CreationTimestamp
    @Column(name = "approved_at", nullable = false, updatable = false)
    private LocalDateTime approvedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
