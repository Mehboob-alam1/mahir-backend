package com.example.demoapp.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatThreadSummaryResponse {

    private Long id;
    /** Same as id; use for API compatibility. */
    private Long threadId;
    private Long bookingId;
    private String otherPartyName;
    private String otherPartyAvatarUrl;
    private String lastMessagePreview;
    private Instant lastMessageAt;
    private long unreadCount;
}
