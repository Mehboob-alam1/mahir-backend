package com.example.demoapp.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminBlockUserRequest {

    @NotNull
    private Boolean blocked;

    private String reason;

    @JsonAlias("blocked_reason")
    private String blockedReason;

    public String resolveReason() {
        if (reason != null && !reason.isBlank()) return reason.trim();
        if (blockedReason != null && !blockedReason.isBlank()) return blockedReason.trim();
        return null;
    }
}
