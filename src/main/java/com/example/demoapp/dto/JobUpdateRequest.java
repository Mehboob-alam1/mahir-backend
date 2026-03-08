package com.example.demoapp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobUpdateRequest {

    @Size(max = 200)
    private String title;

    @Size(max = 2000)
    private String description;

    @Valid
    private LocationDto location;

    private LocalDateTime scheduledAt;

    private BigDecimal budgetMin;

    private BigDecimal budgetMax;

    @Min(1)
    @Max(500)
    private Integer durationHours;
}
