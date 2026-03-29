package com.example.demoapp.dto;

import com.example.demoapp.entity.UserMembershipStatus;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminMembershipRowResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private Long planId;
    private String planName;
    private String planCode;
    private UserMembershipStatus status;
    private Instant startedAt;
    private Instant expiresAt;
}
