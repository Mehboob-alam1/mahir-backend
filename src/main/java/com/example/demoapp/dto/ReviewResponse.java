package com.example.demoapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewResponse {

    private Long id;
    private Long bookingId;
    private Long reviewerId;
    private String reviewerName;
    private Long mahirId;
    private String mahirName;
    private Integer rating;
    private String comment;
    private Instant createdAt;
    /** Set only in admin APIs. */
    private Boolean hiddenFromPublic;
}
