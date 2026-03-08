package com.example.demoapp.dto;

import com.example.demoapp.entity.BookingStatus;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private Long mahirId;
    private String mahirName;
    private String mahirEmail;
    private BookingStatus status;
    private LocalDateTime scheduledAt;
    private String message;
    private Instant createdAt;
    private Instant updatedAt;
}
