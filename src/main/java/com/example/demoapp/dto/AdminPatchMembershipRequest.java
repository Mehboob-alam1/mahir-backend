package com.example.demoapp.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPatchMembershipRequest {

    private Long planId;
    private Instant expiresAt;
    /** Optional: set Mahir credit balance when patching membership. */
    private Integer credits;
}
