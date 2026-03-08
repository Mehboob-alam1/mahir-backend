package com.example.demoapp.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {

    private Long id;
    private Long threadId;
    private Long senderId;
    private String senderName;
    private String content;
    private Instant createdAt;
    private Instant readAt;
}
