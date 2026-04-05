package com.example.demoapp.dto;

import com.example.demoapp.entity.PlanAudience;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerRequest {

    @Size(max = 200)
    private String title;

    @Size(max = 1000)
    private String imageUrl;

    @Size(max = 1000)
    private String linkUrl;

    private Integer sortOrder;

    private Boolean active;

    private PlanAudience audience;

    private Instant startsAt;
    private Instant endsAt;
}
