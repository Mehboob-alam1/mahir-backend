package com.example.demoapp.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private Long id;
    private Long bookingId;
    private Long reviewerId;
    private String reviewerName;
    private Long mahirId;
    private Integer rating;
    private String comment;
    private Instant createdAt;
}
