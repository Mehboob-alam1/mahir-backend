package com.example.demoapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignMembershipRequest {

    @NotNull
    private Long planId;

    /** If null, membership has no fixed end date until cancelled. */
    private Instant expiresAt;
}
