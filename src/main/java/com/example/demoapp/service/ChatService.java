package com.example.demoapp.service;

import com.example.demoapp.dto.ChatMessageRequest;
import com.example.demoapp.dto.ChatMessageResponse;
import com.example.demoapp.dto.ChatThreadSummaryResponse;
import com.example.demoapp.entity.*;
import com.example.demoapp.exception.ResourceNotFoundException;
import com.example.demoapp.exception.UnauthorizedException;
import com.example.demoapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatThreadRepository threadRepository;
    private final ChatMessageRepository messageRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public Long getOrCreateThreadForBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        ensureParticipant(booking, userId);
        return threadRepository.findByBookingId(bookingId)
                .map(ChatThread::getId)
                .orElseGet(() -> {
                    ChatThread t = threadRepository.save(ChatThread.builder().booking(booking).build());
                    return t.getId();
                });
    }

    public List<ChatThreadSummaryResponse> listMyThreads(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        List<ChatThread> threads = threadRepository.findThreadsForUser(userId);
        return threads.stream().map(t -> toSummary(t, userId)).collect(Collectors.toList());
    }

    public Page<ChatMessageResponse> getMessages(Long threadId, Long userId, Pageable pageable) {
        ChatThread thread = threadRepository.findById(threadId).orElseThrow(() -> new ResourceNotFoundException("Chat thread", threadId));
        ensureParticipant(thread.getBooking(), userId);
        Pageable desc = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        return messageRepository.findByThreadOrderByCreatedAtDesc(thread, desc).map(this::toMessageResponse);
    }

    @Transactional
    public ChatMessageResponse sendMessage(Long threadId, Long userId, ChatMessageRequest request) {
        ChatThread thread = threadRepository.findById(threadId).orElseThrow(() -> new ResourceNotFoundException("Chat thread", threadId));
        ensureParticipant(thread.getBooking(), userId);
        User sender = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        ChatMessage msg = ChatMessage.builder()
                .thread(thread)
                .sender(sender)
                .content(request.getContent())
                .build();
        msg = messageRepository.save(msg);
        User other = thread.getBooking().getCustomer().getId().equals(userId) ? thread.getBooking().getMahir() : thread.getBooking().getCustomer();
        notificationService.create(other.getId(), "CHAT_MESSAGE", "New message", "You have a new message in your booking chat.", threadId);
        return toMessageResponse(msg);
    }

    private void ensureParticipant(Booking booking, Long userId) {
        if (!booking.getCustomer().getId().equals(userId) && !booking.getMahir().getId().equals(userId)) {
            throw new UnauthorizedException("You are not a participant in this booking");
        }
    }

    private ChatThreadSummaryResponse toSummary(ChatThread t, Long currentUserId) {
        Booking b = t.getBooking();
        User other = b.getCustomer().getId().equals(currentUserId) ? b.getMahir() : b.getCustomer();
        List<ChatMessage> messages = messageRepository.findByThreadOrderByCreatedAtDesc(t, PageRequest.of(0, 1)).getContent();
        ChatMessage last = messages.isEmpty() ? null : messages.get(0);
        String preview = last != null ? (last.getContent().length() > 80 ? last.getContent().substring(0, 80) + "..." : last.getContent()) : null;
        long unread = messageRepository.findByThreadOrderByCreatedAtDesc(t, Pageable.unpaged()).getContent().stream()
                .filter(m -> !m.getSender().getId().equals(currentUserId) && m.getReadAt() == null).count();
        return ChatThreadSummaryResponse.builder()
                .id(t.getId())
                .bookingId(b.getId())
                .otherPartyName(other.getFullName())
                .otherPartyAvatarUrl(other.getAvatarUrl())
                .lastMessagePreview(preview)
                .lastMessageAt(last != null ? last.getCreatedAt() : t.getCreatedAt())
                .unreadCount(unread)
                .build();
    }

    private ChatMessageResponse toMessageResponse(ChatMessage m) {
        return ChatMessageResponse.builder()
                .id(m.getId())
                .threadId(m.getThread().getId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getFullName())
                .content(m.getContent())
                .createdAt(m.getCreatedAt())
                .readAt(m.getReadAt())
                .build();
    }
}
