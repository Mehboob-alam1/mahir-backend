package com.example.demoapp.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.example.demoapp.entity.AccountStatus;
import com.example.demoapp.entity.AccountType;
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

    @JsonAlias("account_type")
    private AccountType accountType;

    @JsonAlias("credit_balance")
    private Integer credits;

    private String fullName;
    private String email;
    private String phoneNumber;
}
