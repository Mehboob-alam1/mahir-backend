package com.example.demoapp.service;

import com.example.demoapp.repository.UserRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Sends FCM push notifications to the user's device.
 * No-op if Firebase is not initialized or user has no fcmToken.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {

    private final UserRepository userRepository;

    /**
     * Send a push notification to the user's device(s). Called after saving an in-app notification.
     * If Firebase is not configured or user has no FCM token, this does nothing.
     */
    public void sendToUser(Long userId, String title, String body) {
        if (userId == null || (title == null && body == null)) return;
        if (FirebaseApp.getApps().isEmpty()) return;

        String token = userRepository.findById(userId)
                .map(u -> u.getFcmToken())
                .orElse(null);
        if (token == null || token.isBlank()) return;

        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title != null ? title : "")
                            .setBody(body != null ? body : "")
                            .build())
                    .build();
            String msgId = FirebaseMessaging.getInstance().send(message);
            log.debug("FCM push sent to user {}: {}", userId, msgId);
        } catch (FirebaseMessagingException e) {
            log.warn("FCM send failed for user {}: {}", userId, e.getMessage());
        }
    }
}
