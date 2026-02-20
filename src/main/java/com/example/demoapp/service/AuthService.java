package com.example.demoapp.service;

import com.example.demoapp.dto.AuthResponse;
import com.example.demoapp.dto.SignInRequest;
import com.example.demoapp.dto.SignUpRequest;
import com.example.demoapp.dto.UserResponse;
import com.example.demoapp.entity.User;
import com.example.demoapp.exception.DuplicateResourceException;
import com.example.demoapp.exception.UnauthorizedException;
import com.example.demoapp.repository.UserRepository;
import com.example.demoapp.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        user = userRepository.save(user);
        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getId());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail(), user.getId());
        UserResponse userResponse = mapToUserResponse(user);
        return AuthResponse.builder()
                .success(true)
                .message("Registration successful")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getAccessExpirationSeconds())
                .user(userResponse)
                .build();
    }

    public AuthResponse signIn(SignInRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getId());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail(), user.getId());
        UserResponse userResponse = mapToUserResponse(user);
        return AuthResponse.builder()
                .success(true)
                .message("Login successful")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getAccessExpirationSeconds())
                .user(userResponse)
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token required");
        }
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        var claims = jwtService.parseToken(refreshToken);
        String email = claims.getSubject();
        Long userId = claims.get("userId", Long.class);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        String newAccessToken = jwtService.generateAccessToken(user.getEmail(), user.getId());
        String newRefreshToken = jwtService.generateRefreshToken(user.getEmail(), user.getId());
        return AuthResponse.builder()
                .success(true)
                .message("Token refreshed")
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtService.getAccessExpirationSeconds())
                .build();
    }

    public AuthResponse checkSession(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new UnauthorizedException("Session expired or invalid");
        }
        if (!jwtService.isAccessToken(accessToken)) {
            throw new UnauthorizedException("Session expired or invalid");
        }
        var claims = jwtService.parseToken(accessToken);
        String email = claims.getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        UserResponse userResponse = mapToUserResponse(user);
        return AuthResponse.builder()
                .success(true)
                .user(userResponse)
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
