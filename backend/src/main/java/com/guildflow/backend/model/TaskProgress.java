package com.guildflow.backend.model;

import com.guildflow.backend.model.enums.ProgressEntryStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private GoalTask task;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @NotNull
    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "numeric_value")
    private Double numericValue;

    @Column(name = "boolean_value")
    private Boolean booleanValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ProgressEntryStatus status = ProgressEntryStatus.PENDING;

    @Column(name = "mentor_notes", columnDefinition = "TEXT")
    private String mentorNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    /** True once the student saves the day — prevents further edits. */
    @Column(name = "locked", nullable = false)
    @Builder.Default
    private Boolean locked = false;

    /** True for CHECKBOX tasks once permanently marked done — only a mentor can reverse. */
    @Column(name = "done_permanently", nullable = false)
    @Builder.Default
    private Boolean donePermanently = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
