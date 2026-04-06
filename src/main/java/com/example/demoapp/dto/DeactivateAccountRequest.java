package com.example.demoapp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeactivateAccountRequest {

    private String reason;
}
