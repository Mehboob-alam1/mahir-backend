package com.example.demoapp.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminChatThreadResponse {

    private Long threadId;
    private Long bookingId;
    private Long jobId;
    private String jobTitle;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private Long mahirId;
    private String mahirName;
    private String mahirEmail;
    private Instant threadCreatedAt;
}
