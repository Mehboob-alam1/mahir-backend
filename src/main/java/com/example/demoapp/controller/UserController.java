package com.example.demoapp.controller;

import com.example.demoapp.dto.UpdateProfileRequest;
import com.example.demoapp.dto.UserRequest;
import com.example.demoapp.dto.UserResponse;
import com.example.demoapp.security.UserPrincipal;
import com.example.demoapp.service.UserService;
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

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "My profile (me), list users, get/update/delete by ID")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

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
