package com.example.demoapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {

    @NotNull(message = "Mahir ID is required")
    private Long mahirId;

    private LocalDateTime scheduledAt;

    @Size(max = 1000)
    private String message;
}
