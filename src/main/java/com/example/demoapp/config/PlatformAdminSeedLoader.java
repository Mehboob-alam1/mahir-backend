package com.example.demoapp.config;

import com.example.demoapp.entity.AccountStatus;
import com.example.demoapp.entity.AccountType;
import com.example.demoapp.entity.Location;
import com.example.demoapp.entity.Role;
import com.example.demoapp.entity.User;
import com.example.demoapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Ensures the platform ADMIN exists on every startup (not tied to {@code APP_SAMPLE_DATA}).
 * Idempotent: skips if the email is already registered.
 */
@Component
@Order(150)
@RequiredArgsConstructor
@Slf4j
public class PlatformAdminSeedLoader implements ApplicationRunner {

    public static final String SEED_ADMIN_EMAIL = "admin.portal@findmahir.app";
    public static final String SEED_ADMIN_INITIAL_PASSWORD = "Password123!";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(SEED_ADMIN_EMAIL)) {
            return;
        }
        userRepository.save(User.builder()
                .fullName("Platform Admin")
                .email(SEED_ADMIN_EMAIL)
                .password(passwordEncoder.encode(SEED_ADMIN_INITIAL_PASSWORD))
                .phoneNumber("+216 00 000 001")
                .dateOfBirth(LocalDate.of(1985, 1, 1))
                .location(Location.builder()
                        .streetAddress("Operations")
                        .latitude(36.8065)
                        .longitude(10.1815)
                        .build())
                .accountType(AccountType.PREMIUM)
                .role(Role.ADMIN)
                .accountStatus(AccountStatus.ACTIVE)
                .blocked(false)
                .build());
        log.info("Created platform ADMIN user: {} (change password in production)", SEED_ADMIN_EMAIL);
    }
}
