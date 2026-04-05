package com.example.demoapp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminReviewPatchRequest {

    private Boolean hiddenFromPublic;
    private Integer rating;
    private String comment;
}
