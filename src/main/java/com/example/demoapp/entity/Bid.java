package com.example.demoapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids", uniqueConstraints = @UniqueConstraint(columnNames = { "job_id", "mahir_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mahir_id", nullable = false)
    private User mahir;

    @Column(length = 2000)
    private String message;

    @Column(name = "proposed_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal proposedPrice;

    @Column(name = "proposed_at")
    private LocalDateTime proposedAt;

    @Column(name = "estimated_duration_hours")
    private Integer estimatedDurationHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BidStatus status = BidStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
