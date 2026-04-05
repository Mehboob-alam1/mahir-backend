package com.example.demoapp.dto;

import com.example.demoapp.entity.AccountStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPatchUserRequest {

    private Boolean blocked;
    private String blockedReason;
    private AccountStatus accountStatus;
    private String fullName;
    private String email;
    private String phoneNumber;
}
