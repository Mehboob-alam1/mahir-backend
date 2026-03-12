package com.example.demoapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FcmTokenRequest {

    @NotBlank(message = "FCM token is required")
    @Size(max = 500)
    private String fcmToken;
}
