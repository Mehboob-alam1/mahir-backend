package com.example.demoapp.service;

import com.example.demoapp.dto.ChatMessageRequest;
import com.example.demoapp.dto.ChatMessageResponse;
import com.example.demoapp.dto.SupportThreadSummaryResponse;
import com.example.demoapp.entity.Role;
import com.example.demoapp.entity.SupportMessage;
import com.example.demoapp.entity.SupportThread;
import com.example.demoapp.entity.User;
import com.example.demoapp.exception.ResourceNotFoundException;
import com.example.demoapp.exception.UnauthorizedException;
import com.example.demoapp.repository.SupportMessageRepository;
import com.example.demoapp.repository.SupportThreadRepository;
import com.example.demoapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SupportService {

    private final SupportThreadRepository supportThreadRepository;
    private final SupportMessageRepository supportMessageRepository;
    private final UserRepository userRepository;

    public Page<SupportThreadSummaryResponse> listMyThreads(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        requireCustomerOrMahir(user);
        return supportThreadRepository.findByUserOrderByUpdatedAtDesc(user, pageable).map(this::toSummary);
    }

    @Transactional
    public SupportThreadSummaryResponse createThread(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        requireCustomerOrMahir(user);
        SupportThread t = SupportThread.builder().user(user).build();
        t = supportThreadRepository.save(t);
        return toSummary(t);
    }

    public SupportThreadSummaryResponse getThread(Long threadId, Long userId) {
        SupportThread t = supportThreadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Support thread", threadId));
        ensureOwner(t, userId);
        return toSummary(t);
    }

    public Page<ChatMessageResponse> listMessages(Long threadId, Long userId, Pageable pageable) {
        SupportThread t = supportThreadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Support thread", threadId));
        ensureOwner(t, userId);
        return supportMessageRepository.findByThreadOrderByCreatedAtAsc(t, pageable).map(this::toMessageResponse);
    }

    @Transactional
    public ChatMessageResponse postMessage(Long threadId, Long userId, ChatMessageRequest request) {
        SupportThread t = supportThreadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Support thread", threadId));
        ensureOwner(t, userId);
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new com.example.demoapp.exception.BadRequestException("content is required");
        }
        User sender = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        SupportMessage msg = SupportMessage.builder()
                .thread(t)
                .sender(sender)
                .content(request.getContent().trim())
                .build();
        msg = supportMessageRepository.save(msg);
        t.setUpdatedAt(Instant.now());
        supportThreadRepository.save(t);
        return toMessageResponse(msg);
    }

    private void requireCustomerOrMahir(User user) {
        if (user.getRole() != Role.USER && user.getRole() != Role.MAHIR) {
            throw new UnauthorizedException("Support chat is only for customers and Mahirs");
        }
    }

    private void ensureOwner(SupportThread t, Long userId) {
        if (!t.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this support thread");
        }
    }

    private SupportThreadSummaryResponse toSummary(SupportThread t) {
        return SupportThreadSummaryResponse.builder()
                .id(t.getId())
                .threadId(t.getId())
                .userId(t.getUser().getId())
                .userName(t.getUser().getFullName())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    private ChatMessageResponse toMessageResponse(SupportMessage m) {
        User s = m.getSender();
        boolean admin = s.getRole() == Role.ADMIN;
        return ChatMessageResponse.builder()
                .id(m.getId())
                .threadId(m.getThread().getId())
                .senderId(s.getId())
                .senderName(s.getFullName())
                .senderRole(s.getRole().name())
                .fromAdmin(admin)
                .content(m.getContent())
                .createdAt(m.getCreatedAt())
                .sentAt(m.getCreatedAt())
                .readAt(null)
                .build();
    }
}
