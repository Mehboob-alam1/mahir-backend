package com.example.demoapp.dto;

import com.example.demoapp.entity.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long id;
    private Long jobId;
    private Long bidId;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private Long mahirId;
    private String mahirName;
    private String mahirEmail;
    private String jobTitle;
    private BigDecimal agreedPrice;
    private BookingStatus status;
    private LocalDateTime scheduledAt;
    private String message;
    private String cancelReason;
    private Long chatThreadId;
    private Instant createdAt;
    private Instant updatedAt;
}
