package com.example.demoapp.service;

import com.example.demoapp.dto.NotificationResponse;
import com.example.demoapp.entity.Notification;
import com.example.demoapp.entity.User;
import com.example.demoapp.exception.ResourceNotFoundException;
import com.example.demoapp.exception.UnauthorizedException;
import com.example.demoapp.repository.NotificationRepository;
import com.example.demoapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final PushNotificationService pushNotificationService;

    @Transactional
    public void create(Long userId, String type, String title, String body, Long relatedId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Notification n = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .body(body)
                .relatedId(relatedId)
                .build();
        notificationRepository.save(n);
        pushNotificationService.sendToUser(userId, title, body, type);
    }

    public Page<NotificationResponse> listMy(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable).map(this::toResponse);
    }

    /**
     * Send a push notification to a specific FCM device token (e.g. for testing).
     * Delegates to PushNotificationService. Returns FCM message ID or null if Firebase not initialized.
     */
    public String sendPushToToken(String token, String title, String body) {
        return pushNotificationService.sendToToken(token, title, body);
    }

    /** For debugging push: is Firebase ready and does this user have a token? */
    public java.util.Map<String, Object> pushStatus(Long userId) {
        return java.util.Map.of(
                "firebaseInitialized", pushNotificationService.isFirebaseInitialized(),
                "hasFcmToken", pushNotificationService.userHasFcmToken(userId)
        );
    }

    public long unreadCount(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return notificationRepository.countByUserAndReadAtIsNull(user);
    }

    @Transactional
    public void markRead(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId).orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        if (!n.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Not your notification");
        }
        n.setReadAt(java.time.Instant.now());
        notificationRepository.save(n);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .body(n.getBody())
                .relatedId(n.getRelatedId())
                .referenceId(n.getRelatedId())
                .read(n.getReadAt() != null)
                .createdAt(n.getCreatedAt())
                .build();
    }
}
