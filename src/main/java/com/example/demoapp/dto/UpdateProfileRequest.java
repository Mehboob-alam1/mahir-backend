package com.example.demoapp.dto;

import com.example.demoapp.entity.AccountType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    @Size(min = 1, max = 100)
    private String fullName;

    @Size(max = 20)
    private String phoneNumber;

    private LocalDate dateOfBirth;

    @Valid
    private LocationDto location;

    private AccountType accountType;

    private List<Long> serviceCategoryIds;

    @Size(max = 200)
    private String customServiceName;

    @Size(max = 500)
    private String avatarUrl;

    @Size(max = 1000)
    private String bio;

    @Size(min = 6, max = 100, message = "Password must be 6-100 characters")
    private String password;
}
