package com.example.demoapp.dto;

import com.example.demoapp.entity.AccountType;
import com.example.demoapp.entity.Role;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicProfileResponse {

    private Long id;
    private Role role;
    private String fullName;
    private String avatarUrl;
    private String bio;
    private LocationDto location;
    private AccountType accountType;
    private List<CategoryResponse> serviceCategories;
    private String customServiceName;
    private Double averageRating;
    private Long reviewCount;
    private LocalDateTime createdAt;
}
