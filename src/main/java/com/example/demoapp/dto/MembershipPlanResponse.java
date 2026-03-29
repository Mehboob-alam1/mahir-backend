package com.example.demoapp.dto;

import com.example.demoapp.entity.PlanAudience;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipPlanResponse {

    private Long id;
    private String name;
    private String code;
    private String description;
    private PlanAudience audience;
    private BigDecimal priceMonthly;
    private String currency;
    private String featuresText;
    private boolean active;
    private int sortOrder;
    private Instant createdAt;
    private Instant updatedAt;
}
