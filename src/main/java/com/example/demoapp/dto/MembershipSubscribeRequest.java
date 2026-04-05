package com.example.demoapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipSubscribeRequest {

    @NotNull
    private Long planId;
}
