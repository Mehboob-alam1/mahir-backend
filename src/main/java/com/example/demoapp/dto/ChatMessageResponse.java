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
public class ChatMessageResponse {

    private Long id;
    private Long threadId;
    private Long senderId;
    private String senderName;
    /** USER | MAHIR | ADMIN — support and booking chat. */
    private String senderRole;
    /** True when sender is an admin (support inbox). */
    private Boolean fromAdmin;
    private String content;
    private Instant createdAt;
    /** Alias for clients expecting `sentAt`. */
    private Instant sentAt;
    private Instant readAt;
}
