package com.example.demoapp.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HireInviteRequest {

    /** Optional note shown on the booking / context for the Mahir. */
    @Size(max = 1000)
    private String message;
}
