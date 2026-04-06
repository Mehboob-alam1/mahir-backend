package com.example.demoapp.controller;

import com.example.demoapp.dto.ChangePasswordRequest;
import com.example.demoapp.dto.DeactivateAccountRequest;
import com.example.demoapp.dto.DeleteAccountRequest;
import com.example.demoapp.dto.FcmTokenRequest;
import com.example.demoapp.dto.MembershipSubscribeRequest;
import com.example.demoapp.dto.MyMembershipResponse;
import com.example.demoapp.dto.AdminMembershipRowResponse;
import com.example.demoapp.dto.NotificationPreferencesPatchRequest;
import com.example.demoapp.dto.NotificationPreferencesResponse;
import com.example.demoapp.dto.ProfilePictureUploadResponse;
import com.example.demoapp.dto.UpdateProfileRequest;
import com.example.demoapp.dto.UserRequest;
import com.example.demoapp.dto.UserResponse;
import com.example.demoapp.security.UserPrincipal;
import com.example.demoapp.service.UserMembershipSelfService;
import com.example.demoapp.service.UserService;
import com.example.demoapp.service.UserSettingsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "My profile (me), list users, get/update/delete by ID")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final UserMembershipSelfService userMembershipSelfService;
    private final UserSettingsService userSettingsService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        }
        UserResponse user = userService.getMe(principal.getUserId());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me/membership")
    public ResponseEntity<MyMembershipResponse> getMyMembership(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        }
        return ResponseEntity.ok(userMembershipSelfService.getMyMembership(principal.getUserId()));
    }

    @PostMapping("/me/membership/subscribe")
    public ResponseEntity<AdminMembershipRowResponse> subscribeMembership(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody MembershipSubscribeRequest request) {
        if (principal == null) {
            throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userMembershipSelfService.subscribe(principal.getUserId(), request));
    }

    @PostMapping("/me/membership/cancel")
    public ResponseEntity<Void> cancelMembership(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        }
        userMembershipSelfService.cancelMyMembership(principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        if (principal == null) {
            throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        }
        UserResponse response = userService.updateMe(principal.getUserId(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/me/fcm-token")
    public ResponseEntity<Void> saveFcmToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody FcmTokenRequest request) {
        if (principal == null) {
            throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        }
        userService.saveFcmToken(principal.getUserId(), request.getFcmToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/notification-preferences")
    public ResponseEntity<NotificationPreferencesResponse> getNotificationPreferences(
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        }
        return ResponseEntity.ok(userSettingsService.getNotificationPreferences(principal.getUserId()));
    }

    @PatchMapping("/me/notification-preferences")
    public ResponseEntity<NotificationPreferencesResponse> patchNotificationPreferences(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody(required = false) NotificationPreferencesPatchRequest request) {
        if (principal == null) {
            throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        }
        return ResponseEntity.ok(userSettingsService.patchNotificationPreferences(principal.getUserId(), request));
    }

    /**
     * Multipart field name: {@code file}. Accepts JPEG, PNG, WebP (max 5 MB).
     */
    @PostMapping(value = "/me/profile-picture", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfilePictureUploadResponse> uploadProfilePicture(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file) {
        if (principal == null) {
            throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        }
        return ResponseEntity.ok(userSettingsService.uploadProfilePicture(principal.getUserId(), file));
    }

    @DeleteMapping("/me/profile-picture")
    public ResponseEntity<Void> deleteProfilePicture(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        }
        userSettingsService.removeProfilePicture(principal.getUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        if (principal == null) {
            throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        }
        userSettingsService.changePassword(principal.getUserId(), request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/deactivate")
    public ResponseEntity<Void> deactivateAccount(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody(required = false) DeactivateAccountRequest request) {
        if (principal == null) {
            throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        }
        userSettingsService.deactivateAccount(principal.getUserId(), request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        }
        userSettingsService.deleteMyAccount(principal.getUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/delete")
    public ResponseEntity<Void> deleteMyAccountWithConfirm(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DeleteAccountRequest request) {
        if (principal == null) {
            throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        }
        if (request.getConfirm() == null || !"DELETE".equals(request.getConfirm())) {
            throw new com.example.demoapp.exception.BadRequestException("confirm must be DELETE");
        }
        userSettingsService.deleteMyAccount(principal.getUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<UserResponse> page = userService.getAllUsers(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}/public")
    public ResponseEntity<com.example.demoapp.dto.PublicProfileResponse> getPublicProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        if (principal == null) throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        return ResponseEntity.ok(userService.getPublicProfile(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        if (principal == null) {
            throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        }
        UserResponse response = userService.updateUser(id, principal.getUserId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        if (principal == null) {
            throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        }
        userService.deleteUser(id, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}
