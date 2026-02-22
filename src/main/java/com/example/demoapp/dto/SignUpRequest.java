package com.example.demoapp.dto;

import com.example.demoapp.entity.AccountType;
import com.example.demoapp.entity.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequest {

    @NotNull(message = "Role is required (USER or MAHIR)")
    private Role role;

    @NotBlank(message = "Full name is required")
    @Size(min = 1, max = 100)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be 6-100 characters")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Size(max = 20)
    private String phoneNumber;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @Valid
    @NotNull(message = "Location is required")
    private LocationDto location;

    @NotNull(message = "Account type is required (FREEMIUM or PREMIUM)")
    private AccountType accountType;

    /** For MAHIR only: category IDs from GET /api/categories. Can be empty if using customServiceName. */
    private List<Long> serviceCategoryIds;

    /** For MAHIR only: custom service name when not selecting from categories. */
    @Size(max = 200)
    private String customServiceName;
}
