package com.example.demoapp.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminChatThreadResponse {

    /** Same as threadId for support threads (Flutter contract). */
    private Long id;
    private Long threadId;
    /** Participant app user (USER or MAHIR) — open profile in admin UI. */
    private Long userId;
    private String userRole;
    /** "SUPPORT" for in-app support threads. */
    private String type;

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
