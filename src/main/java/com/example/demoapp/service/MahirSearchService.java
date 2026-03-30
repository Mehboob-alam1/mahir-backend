package com.example.demoapp.service;

import com.example.demoapp.dto.CategoryResponse;
import com.example.demoapp.dto.LocationDto;
import com.example.demoapp.dto.MahirResponse;
import com.example.demoapp.entity.Role;
import com.example.demoapp.entity.User;
import com.example.demoapp.repository.ReviewRepository;
import com.example.demoapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MahirSearchService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public Page<MahirResponse> searchMahirs(Long categoryId, Pageable pageable, boolean maskEmailAndPhone) {
        Page<User> mahirs = categoryId != null
                ? userRepository.findActiveMahirsByCategory(Role.MAHIR, categoryId, pageable)
                : userRepository.findByRoleAndBlockedFalse(Role.MAHIR, pageable);
        return mahirs.map(u -> toMahirResponse(u, maskEmailAndPhone));
    }

    public MahirResponse getMahirById(Long id, boolean maskEmailAndPhone) {
        User mahir = userRepository.findById(id)
                .orElseThrow(() -> new com.example.demoapp.exception.ResourceNotFoundException("Mahir", id));
        if (mahir.getRole() != Role.MAHIR) {
            throw new com.example.demoapp.exception.ResourceNotFoundException("Mahir", id);
        }
        if (mahir.isBlocked()) {
            throw new com.example.demoapp.exception.ResourceNotFoundException("Mahir", id);
        }
        return toMahirResponse(mahir, maskEmailAndPhone);
    }

    private MahirResponse toMahirResponse(User user, boolean maskEmailAndPhone) {
        Double avgRating = reviewRepository.getAverageRatingByMahirId(user.getId());
        long reviewCount = reviewRepository.countPublicByMahirId(user.getId());
        LocationDto locDto = null;
        if (user.getLocation() != null) {
            locDto = LocationDto.builder()
                    .streetAddress(user.getLocation().getStreetAddress())
                    .latitude(user.getLocation().getLatitude())
                    .longitude(user.getLocation().getLongitude())
                    .build();
        }
        List<CategoryResponse> categories = user.getServiceCategories() == null ? List.of() : user.getServiceCategories().stream()
                .map(c -> CategoryResponse.builder().id(c.getId()).name(c.getName()).description(c.getDescription()).build())
                .collect(Collectors.toList());
        return MahirResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(maskEmailAndPhone ? null : user.getEmail())
                .phoneNumber(maskEmailAndPhone ? null : user.getPhoneNumber())
                .location(locDto)
                .accountType(user.getAccountType())
                .serviceCategories(categories)
                .customServiceName(user.getCustomServiceName())
                .averageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : null)
                .reviewCount(reviewCount)
                .role("MAHIR")
                .build();
    }
}
