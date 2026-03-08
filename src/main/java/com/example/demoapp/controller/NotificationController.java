package com.example.demoapp.controller;

import com.example.demoapp.dto.NotificationResponse;
import com.example.demoapp.security.UserPrincipal;
import com.example.demoapp.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "List and mark read")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> listMy(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        if (principal == null) throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        return ResponseEntity.ok(notificationService.listMy(principal.getUserId(), pageable));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        return ResponseEntity.ok(Map.of("count", notificationService.unreadCount(principal.getUserId())));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        if (principal == null) throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        notificationService.markRead(id, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}
