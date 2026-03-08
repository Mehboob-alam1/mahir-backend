package com.example.demoapp.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidRequest {

    @Size(max = 2000)
    private String message;

    @NotNull(message = "Proposed price is required")
    @DecimalMin("0")
    private BigDecimal proposedPrice;

    private LocalDateTime proposedAt;

    @Min(1)
    @Max(500)
    private Integer estimatedDurationHours;
}
