package com.guildflow.backend.model;

import com.guildflow.backend.model.enums.TrackingType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Source {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ResourceCategory category;

    @Column(name = "total_capacity")
    private Double totalCapacity;

    @Column(name = "daily_limit")
    private Double dailyLimit;

    @Enumerated(EnumType.STRING)
    @Column(name = "tracking_type", length = 20)
    private TrackingType trackingType;

    @Column(length = 50)
    private String language;

    @Column(length = 255)
    private String part;

    // Legacy columns — kept for migration safety, no longer populated
    @Column(name = "total_pages")
    private Integer totalPages;

    @Column(name = "total_minutes")
    private Integer totalMinutes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
