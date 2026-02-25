package com.example.demoapp.service;

import com.example.demoapp.dto.*;
import com.example.demoapp.entity.*;
import com.example.demoapp.exception.DuplicateResourceException;
import com.example.demoapp.exception.ResourceNotFoundException;
import com.example.demoapp.exception.UnauthorizedException;
import com.example.demoapp.repository.CategoryRepository;
import com.example.demoapp.repository.PasswordResetTokenRepository;
import com.example.demoapp.repository.UserRepository;
import com.example.demoapp.security.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.reset-password.base-url:http://localhost:8080}")
    private String resetPasswordBaseUrl;

    @Value("${app.reset-password.token-valid-minutes:60}")
    private int tokenValidMinutes;

    @Transactional
    public AuthResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        Location location = null;
        if (request.getLocation() != null) {
            location = Location.builder()
                    .streetAddress(request.getLocation().getStreetAddress())
                    .latitude(request.getLocation().getLatitude())
                    .longitude(request.getLocation().getLongitude())
                    .build();
        }

        User user = User.builder()
                .role(request.getRole())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth())
                .location(location)
                .accountType(request.getAccountType())
                .build();

        if (request.getRole() == Role.MAHIR) {
            List<Category> categories = new ArrayList<>();
            if (request.getServiceCategoryIds() != null && !request.getServiceCategoryIds().isEmpty()) {
                categories = categoryRepository.findAllById(request.getServiceCategoryIds());
            }
            user.setServiceCategories(categories);
            user.setCustomServiceName(request.getCustomServiceName());
        }

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
        try {
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
        } catch (UnauthorizedException e) {
            throw e;
        } catch (JwtException e) {
            log.debug("Check session failed: invalid or expired token");
            throw new UnauthorizedException("Session expired or invalid");
        } catch (Exception e) {
            log.warn("Check session failed", e);
            throw new UnauthorizedException("Session expired or invalid");
        }
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            log.debug("Forgot password requested for unknown email: {}", request.getEmail());
            return;
        }
        passwordResetTokenRepository.deleteByUserId(user.getId());
        String token = UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plusSeconds(tokenValidMinutes * 60L);
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(expiresAt)
                .build();
        passwordResetTokenRepository.save(resetToken);

        String resetLink = resetPasswordBaseUrl + "/reset-password?token=" + token;
        if (mailSender != null) {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(user.getEmail());
            msg.setSubject("Reset your password");
            msg.setText("Use this link to reset your password (valid " + tokenValidMinutes + " minutes):\n\n" + resetLink);
            try {
                mailSender.send(msg);
                log.info("Password reset email sent to {}", user.getEmail());
            } catch (Exception e) {
                log.warn("Failed to send reset email: {}", e.getMessage());
            }
        } else {
            log.info("No mail sender configured. Reset link for {}: {}", user.getEmail(), resetLink);
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired reset token"));
        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new UnauthorizedException("Invalid or expired reset token");
        }
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);
        log.info("Password reset completed for user {}", user.getEmail());
    }

    private UserResponse mapToUserResponse(User user) {
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
                .serviceCategories(UserResponse.fromCategoryList(user.getServiceCategories() != null ? user.getServiceCategories() : List.of()))
                .customServiceName(user.getCustomServiceName())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
