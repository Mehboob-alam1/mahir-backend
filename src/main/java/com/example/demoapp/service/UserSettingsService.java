package com.example.demoapp.service;

import com.example.demoapp.dto.*;
import com.example.demoapp.entity.AccountStatus;
import com.example.demoapp.entity.User;
import com.example.demoapp.entity.UserNotificationPreferences;
import com.example.demoapp.exception.BadRequestException;
import com.example.demoapp.exception.PayloadTooLargeException;
import com.example.demoapp.exception.UnsupportedMediaTypeAppException;
import com.example.demoapp.exception.ResourceNotFoundException;
import com.example.demoapp.repository.UserNotificationPreferencesRepository;
import com.example.demoapp.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserSettingsService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );
    private static final long MAX_AVATAR_BYTES = 5L * 1024 * 1024;

    private final UserRepository userRepository;
    private final UserNotificationPreferencesRepository preferencesRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

    @Value("${app.files.upload-dir:uploads}")
    private String uploadDir;

    private Path avatarRoot;

    @PostConstruct
    void init() throws IOException {
        avatarRoot = Path.of(uploadDir).resolve("avatars").toAbsolutePath().normalize();
        Files.createDirectories(avatarRoot);
    }

    public Path getAvatarRoot() {
        return avatarRoot;
    }

    @Transactional(readOnly = true)
    public NotificationPreferencesResponse getNotificationPreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        UserNotificationPreferences p = preferencesRepository.findByUser(user)
                .orElseGet(() -> UserNotificationPreferences.builder()
                        .inboxMessages(true)
                        .ratingReminders(true)
                        .promotionsAndTips(true)
                        .yourAccount(true)
                        .build());
        return toPrefsResponse(p);
    }

    @Transactional
    public NotificationPreferencesResponse patchNotificationPreferences(Long userId, NotificationPreferencesPatchRequest req) {
        if (req == null) {
            req = new NotificationPreferencesPatchRequest();
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        UserNotificationPreferences p = preferencesRepository.findByUser(user)
                .orElseGet(() -> {
                    UserNotificationPreferences n = UserNotificationPreferences.builder()
                            .user(user)
                            .inboxMessages(true)
                            .ratingReminders(true)
                            .promotionsAndTips(true)
                            .yourAccount(true)
                            .build();
                    return preferencesRepository.save(n);
                });
        if (req.getInboxMessages() != null) {
            p.setInboxMessages(req.getInboxMessages());
        }
        if (req.getRatingReminders() != null) {
            p.setRatingReminders(req.getRatingReminders());
        }
        if (req.getPromotionsAndTips() != null) {
            p.setPromotionsAndTips(req.getPromotionsAndTips());
        }
        if (req.getYourAccount() != null) {
            p.setYourAccount(req.getYourAccount());
        }
        preferencesRepository.save(p);
        return toPrefsResponse(p);
    }

    private NotificationPreferencesResponse toPrefsResponse(UserNotificationPreferences p) {
        return NotificationPreferencesResponse.builder()
                .inboxMessages(p.isInboxMessages())
                .ratingReminders(p.isRatingReminders())
                .promotionsAndTips(p.isPromotionsAndTips())
                .yourAccount(p.isYourAccount())
                .build();
    }

    @Transactional
    public ProfilePictureUploadResponse uploadProfilePicture(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required (multipart field name: file)");
        }
        String ct = file.getContentType();
        if (ct == null || !ALLOWED_IMAGE_TYPES.contains(ct.toLowerCase(Locale.ROOT))) {
            throw new UnsupportedMediaTypeAppException("Unsupported media type; use JPEG, PNG, or WebP");
        }
        if (file.getSize() > MAX_AVATAR_BYTES) {
            throw new PayloadTooLargeException("File too large (max 5 MB)");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        String ext = extensionForContentType(ct);
        Path target = avatarRoot.resolve(userId + "." + ext);
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BadRequestException("Could not store file: " + e.getMessage());
        }
        String base = publicBaseUrl.replaceAll("/+$", "");
        String url = base + "/api/files/avatars/" + userId;
        user.setAvatarUrl(url);
        userRepository.save(user);
        return ProfilePictureUploadResponse.builder()
                .profilePictureUrl(url)
                .build();
    }

    private String extensionForContentType(String ct) {
        String c = ct.toLowerCase(Locale.ROOT);
        if (c.contains("jpeg") || c.contains("jpg")) {
            return "jpg";
        }
        if (c.contains("png")) {
            return "png";
        }
        return "webp";
    }

    @Transactional
    public void removeProfilePicture(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        deleteAvatarFiles(userId);
        user.setAvatarUrl(null);
        userRepository.save(user);
    }

    public void deleteAvatarFiles(Long userId) {
        for (String ext : new String[] {"jpg", "png", "webp"}) {
            try {
                Files.deleteIfExists(avatarRoot.resolve(userId + "." + ext));
            } catch (IOException ignored) {
            }
        }
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deactivateAccount(Long userId, DeactivateAccountRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setAccountStatus(AccountStatus.DEACTIVATED);
        if (req != null && req.getReason() != null && !req.getReason().isBlank()) {
            user.setBlockedReason(req.getReason().trim());
        }
        userRepository.save(user);
    }

    @Transactional
    public void deleteMyAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        deleteAvatarFiles(userId);
        preferencesRepository.findByUser(user).ifPresent(preferencesRepository::delete);
        userRepository.delete(user);
    }
}
