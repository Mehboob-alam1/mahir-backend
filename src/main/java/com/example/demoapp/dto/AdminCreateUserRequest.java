package com.example.demoapp.dto;

import com.example.demoapp.entity.AccountType;
import com.example.demoapp.entity.Role;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCreateUserRequest {

    @NotNull
    private Role role;

    @NotBlank
    @Size(max = 100)
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    private String phoneNumber;
    private LocalDate dateOfBirth;

    @NotNull
    private AccountType accountType;

    private List<Long> serviceCategoryIds;
    private String customServiceName;
}
