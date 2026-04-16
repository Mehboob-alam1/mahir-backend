package com.example.demoapp.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @Size(max = 2000)
    private String description;

    @NotNull(message = "Category ID is required")
    @JsonProperty("categoryId")
    @JsonAlias({"category_id"})
    private Long categoryId;

    @Valid
    @NotNull(message = "Location is required")
    private LocationDto location;

    private LocalDateTime scheduledAt;

    @DecimalMin("0")
    private BigDecimal budgetMin;

    @DecimalMin("0")
    private BigDecimal budgetMax;

    @Min(1)
    @Max(500)
    private Integer durationHours;
}
