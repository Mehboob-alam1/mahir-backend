package com.example.demoapp.dto;

import com.example.demoapp.entity.JobStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobResponse {

    private Long id;
    private Long postedById;
    private String posterName;
    private String posterEmail;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String description;
    private LocationDto location;
    private LocalDateTime scheduledAt;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private Integer durationHours;
    private JobStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
