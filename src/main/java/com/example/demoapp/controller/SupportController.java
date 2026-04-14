package com.example.demoapp.controller;

import com.example.demoapp.dto.ChatMessageRequest;
import com.example.demoapp.dto.ChatMessageResponse;
import com.example.demoapp.dto.SupportThreadSummaryResponse;
import com.example.demoapp.security.UserPrincipal;
import com.example.demoapp.service.SupportService;
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
@RequestMapping("/api/support/chats")
@RequiredArgsConstructor
@Tag(name = "Support", description = "In-app support chat (USER / MAHIR). Same thread ids as admin support inbox.")
public class SupportController {

    private final SupportService supportService;

    @GetMapping
    public ResponseEntity<Page<SupportThreadSummaryResponse>> listThreads(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20, sort = "updatedAt") Pageable pageable) {
        if (principal == null) throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        return ResponseEntity.ok(supportService.listMyThreads(principal.getUserId(), pageable));
    }

    @PostMapping
    public ResponseEntity<SupportThreadSummaryResponse> createThread(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        return ResponseEntity.status(HttpStatus.CREATED).body(supportService.createThread(principal.getUserId()));
    }

    @GetMapping("/{threadId}")
    public ResponseEntity<SupportThreadSummaryResponse> getThread(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long threadId) {
        if (principal == null) throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        return ResponseEntity.ok(supportService.getThread(threadId, principal.getUserId()));
    }

    @GetMapping("/{threadId}/messages")
    public ResponseEntity<Page<ChatMessageResponse>> listMessages(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long threadId,
            @PageableDefault(size = 50, sort = "createdAt") Pageable pageable) {
        if (principal == null) throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        return ResponseEntity.ok(supportService.listMessages(threadId, principal.getUserId(), pageable));
    }

    @PostMapping("/{threadId}/messages")
    public ResponseEntity<ChatMessageResponse> postMessage(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long threadId,
            @Valid @RequestBody ChatMessageRequest request) {
        if (principal == null) throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        return ResponseEntity.status(HttpStatus.CREATED).body(supportService.postMessage(threadId, principal.getUserId(), request));
    }
}
