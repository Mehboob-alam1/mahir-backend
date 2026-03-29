package com.example.demoapp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaqResponse {

    private Long id;
    private String question;
    private String answer;
    private int sortOrder;
    private Boolean active;
}
