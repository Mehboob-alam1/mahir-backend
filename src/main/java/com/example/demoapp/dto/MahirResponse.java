package com.example.demoapp.dto;

import com.example.demoapp.entity.AccountType;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * Public Mahir profile for {@code GET /api/mahirs/{id}} and search. See docs/MAHIR_PUBLIC_PROFILE.md
 * for contract, PII policy, and {@code accountType} values (FREEMIUM | PREMIUM).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MahirResponse {

    private Long id;

    @JsonProperty("fullName")
    @JsonAlias("full_name")
    private String fullName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phoneNumber")
    @JsonAlias("phone_number")
    private String phoneNumber;

    private LocationDto location;

    @JsonProperty("accountType")
    @JsonAlias("account_type")
    private AccountType accountType;

    @JsonProperty("serviceCategories")
    @JsonAlias("service_categories")
    private List<CategoryResponse> serviceCategories;

    @JsonProperty("customServiceName")
    @JsonAlias("custom_service_name")
    private String customServiceName;

    @JsonProperty("averageRating")
    @JsonAlias("average_rating")
    private Double averageRating;

    @JsonProperty("reviewCount")
    @JsonAlias("review_count")
    private Long reviewCount;

    /** Stable role label for clients (always MAHIR for this resource). */
    @JsonProperty("role")
    private String role;
}
