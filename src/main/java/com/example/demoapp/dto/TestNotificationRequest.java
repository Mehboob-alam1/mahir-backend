package com.example.demoapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestNotificationRequest {

    @NotBlank(message = "FCM device token is required")
    @Size(max = 500)
    private String token;

    @Size(max = 200)
    private String title;

    @Size(max = 500)
    private String body;
}
