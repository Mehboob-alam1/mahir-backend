package com.example.demoapp.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private String type;
    private String title;
    private String body;
    private Long relatedId;
    /** Same as relatedId (e.g. bookingId, jobId). */
    private Long referenceId;
    private Boolean read;
    private Instant createdAt;
}
