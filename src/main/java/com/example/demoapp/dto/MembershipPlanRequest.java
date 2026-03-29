package com.example.demoapp.dto;

import com.example.demoapp.entity.PlanAudience;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipPlanRequest {

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotBlank
    @Size(max = 64)
    private String code;

    @Size(max = 2000)
    private String description;

    @NotNull
    private PlanAudience audience;

    private BigDecimal priceMonthly;

    @Size(max = 8)
    private String currency;

    @Size(max = 4000)
    private String featuresText;

    private Boolean active;

    private Integer sortOrder;
}
