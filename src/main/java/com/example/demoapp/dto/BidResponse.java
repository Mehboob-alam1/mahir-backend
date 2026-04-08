package com.example.demoapp.dto;

import com.example.demoapp.entity.BidStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidResponse {

    private Long id;
    private Long jobId;
    private Long mahirId;
    private String mahirName;
    private String mahirAvatarUrl;
    private Double mahirRating;
    private Long mahirReviewCount;
    private String message;
    private BigDecimal proposedPrice;
    private LocalDateTime proposedAt;
    private Integer estimatedDurationHours;
    private BidStatus status;
    private Instant createdAt;
    /** Chat thread for this bid (poster ↔ Mahir), created when the bid is placed. */
    private Long chatThreadId;
}
