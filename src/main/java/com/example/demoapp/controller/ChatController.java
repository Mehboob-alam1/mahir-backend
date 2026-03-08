package com.example.demoapp.controller;

import com.example.demoapp.dto.ChatMessageRequest;
import com.example.demoapp.dto.ChatMessageResponse;
import com.example.demoapp.dto.ChatThreadIdResponse;
import com.example.demoapp.dto.ChatThreadSummaryResponse;
import com.example.demoapp.service.ChatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.demoapp.security.UserPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Chat", description = "Chat per booking")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/bookings/{bookingId}/chat")
    public ResponseEntity<ChatThreadIdResponse> getOrCreateThread(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long bookingId) {
        if (principal == null) throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        Long threadId = chatService.getOrCreateThreadForBooking(bookingId, principal.getUserId());
        return ResponseEntity.ok(ChatThreadIdResponse.builder().threadId(threadId).build());
    }

    @GetMapping("/chats")
    public ResponseEntity<List<ChatThreadSummaryResponse>> listMyThreads(
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        return ResponseEntity.ok(chatService.listMyThreads(principal.getUserId()));
    }

    @GetMapping("/chats/{threadId}/messages")
    public ResponseEntity<Page<ChatMessageResponse>> getMessages(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long threadId,
            @PageableDefault(size = 50, sort = "createdAt") Pageable pageable) {
        if (principal == null) throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        return ResponseEntity.ok(chatService.getMessages(threadId, principal.getUserId(), pageable));
    }

    @PostMapping("/chats/{threadId}/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long threadId,
            @Valid @RequestBody ChatMessageRequest request) {
        if (principal == null) throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        return ResponseEntity.ok(chatService.sendMessage(threadId, principal.getUserId(), request));
    }
}
