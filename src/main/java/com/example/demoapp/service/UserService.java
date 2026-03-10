package com.example.demoapp.service;

import com.example.demoapp.dto.LocationDto;
import com.example.demoapp.dto.PublicProfileResponse;
import com.example.demoapp.dto.UpdateProfileRequest;
import com.example.demoapp.dto.UserRequest;
import com.example.demoapp.dto.CategoryResponse;
import com.example.demoapp.dto.UserResponse;
import com.example.demoapp.entity.Location;
import com.example.demoapp.entity.User;
import com.example.demoapp.exception.DuplicateResourceException;
import com.example.demoapp.exception.ResourceNotFoundException;
import com.example.demoapp.exception.UnauthorizedException;
import com.example.demoapp.repository.CategoryRepository;
import com.example.demoapp.repository.ReviewRepository;
import com.example.demoapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required for new users");
        }
        User user = User.builder()
                .fullName(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        user = userRepository.save(user);
        return mapToResponse(user);
    }

    public UserResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse updateMe(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getLocation() != null) {
            user.setLocation(Location.builder()
                    .streetAddress(request.getLocation().getStreetAddress())
                    .latitude(request.getLocation().getLatitude())
                    .longitude(request.getLocation().getLongitude())
                    .build());
        }
        if (request.getAccountType() != null) {
            user.setAccountType(request.getAccountType());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (user.getRole() == com.example.demoapp.entity.Role.MAHIR) {
            if (request.getServiceCategoryIds() != null) {
                user.setServiceCategories(request.getServiceCategoryIds().isEmpty()
                        ? new ArrayList<>()
                        : categoryRepository.findAllById(request.getServiceCategoryIds()));
            }
            if (request.getCustomServiceName() != null) {
                user.setCustomServiceName(request.getCustomServiceName());
            }
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        user = userRepository.save(user);
        return mapToResponse(user);
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::mapToResponse);
    }

    public List<UserResponse> getAllUsersList() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return mapToResponse(user);
    }

    public PublicProfileResponse getPublicProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        Location loc = user.getLocation();
        LocationDto locDto = loc == null ? null : LocationDto.builder()
                .streetAddress(loc.getStreetAddress())
                .latitude(loc.getLatitude())
                .longitude(loc.getLongitude())
                .build();
        java.util.List<CategoryResponse> categories = user.getServiceCategories() == null ? List.of() : user.getServiceCategories().stream()
                .map(c -> CategoryResponse.builder().id(c.getId()).name(c.getName()).description(c.getDescription()).build())
                .collect(Collectors.toList());
        Double avgRating = user.getRole() == com.example.demoapp.entity.Role.MAHIR ? reviewRepository.getAverageRatingByMahirId(user.getId()) : null;
        long reviewCount = user.getRole() == com.example.demoapp.entity.Role.MAHIR ? reviewRepository.countByMahir(user) : 0L;
        return PublicProfileResponse.builder()
                .id(user.getId())
                .role(user.getRole())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .location(locDto)
                .accountType(user.getAccountType())
                .serviceCategories(categories)
                .customServiceName(user.getCustomServiceName())
                .averageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : null)
                .reviewCount(reviewCount)
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional
    public UserResponse updateUser(Long id, Long currentUserId, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        if (!id.equals(currentUserId)) {
            throw new UnauthorizedException("You can only update your own profile. Use PUT /api/users/me for your profile.");
        }
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        user.setFullName(request.getName());
        user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user = userRepository.save(user);
        return mapToResponse(user);
    }

    @Transactional
    public void deleteUser(Long id, Long currentUserId) {
        if (!id.equals(currentUserId)) {
            throw new UnauthorizedException("You can only delete your own account.");
        }
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
    }

    private UserResponse mapToResponse(User user) {
        Location loc = user.getLocation();
        LocationDto locationDto = loc == null ? null : LocationDto.builder()
                .streetAddress(loc.getStreetAddress())
                .latitude(loc.getLatitude())
                .longitude(loc.getLongitude())
                .build();
        return UserResponse.builder()
                .id(user.getId())
                .role(user.getRole())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth())
                .location(locationDto)
                .accountType(user.getAccountType())
                .serviceCategories(UserResponse.fromCategoryList(user.getServiceCategories()))
                .customServiceName(user.getCustomServiceName())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .credits(user.getCredits())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
