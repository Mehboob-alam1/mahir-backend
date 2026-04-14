package com.example.demoapp.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportThreadSummaryResponse {

    private Long id;
    private Long threadId;
    private Long userId;
    private String userName;
    private Instant updatedAt;
    private Instant createdAt;
}
