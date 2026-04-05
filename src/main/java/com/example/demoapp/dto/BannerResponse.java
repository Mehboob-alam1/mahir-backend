package com.example.demoapp.dto;

import com.example.demoapp.entity.PlanAudience;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerResponse {

    private Long id;
    private String title;
    private String imageUrl;
    private String linkUrl;
    private int sortOrder;
    private boolean active;
    private PlanAudience audience;
    private Instant startsAt;
    private Instant endsAt;
    private Instant createdAt;
    private Instant updatedAt;
}
