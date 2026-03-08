package com.example.demoapp.dto;

import com.example.demoapp.entity.AccountType;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MahirResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private LocationDto location;
    private AccountType accountType;
    private List<CategoryResponse> serviceCategories;
    private String customServiceName;
    private Double averageRating;
    private Long reviewCount;
}
