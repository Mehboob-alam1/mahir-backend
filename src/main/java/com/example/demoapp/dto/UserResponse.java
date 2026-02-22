package com.example.demoapp.dto;

import com.example.demoapp.entity.AccountType;
import com.example.demoapp.entity.Role;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private Role role;
    private String fullName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private LocationDto location;
    private AccountType accountType;
    private List<CategoryResponse> serviceCategories;
    private String customServiceName;
    private LocalDateTime createdAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryResponse {
        private Long id;
        private String name;
        private String description;
    }

    public static List<CategoryResponse> fromCategoryList(List<com.example.demoapp.entity.Category> categories) {
        if (categories == null) return List.of();
        return categories.stream()
                .map(c -> CategoryResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .description(c.getDescription())
                        .build())
                .collect(Collectors.toList());
    }
}
