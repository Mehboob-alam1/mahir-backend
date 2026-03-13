package com.example.demoapp.service;

import com.example.demoapp.entity.User;
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
        if (FirebaseApp.getApps().isEmpty()) {
            log.info("FCM push skipped for user {}: Firebase not initialized (set APP_FIREBASE_SERVICE_ACCOUNT_JSON on server)", userId);
            return;
        }

        String token = userRepository.findById(userId)
                .map(u -> u.getFcmToken())
                .orElse(null);
        if (token == null || token.isBlank()) {
            log.info("FCM push skipped for user {}: no FCM token (app must call POST /api/users/me/fcm-token after login)", userId);
            return;
        }

        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title != null ? title : "")
                            .setBody(body != null ? body : "")
                            .build())
                    .build();
            String msgId = FirebaseMessaging.getInstance().send(message);
            log.info("FCM push sent to user {}: messageId={}", userId, msgId);
        } catch (FirebaseMessagingException e) {
            log.warn("FCM push failed for user {}: {} (token may be invalid or expired; app should send token again)", userId, e.getMessage());
        }
    }

    /** For debugging: is Firebase initialized (can we send FCM)? */
    public boolean isFirebaseInitialized() {
        return !FirebaseApp.getApps().isEmpty();
    }

    /** For debugging: does this user have an FCM token stored? */
    public boolean userHasFcmToken(Long userId) {
        if (userId == null) return false;
        String token = userRepository.findById(userId).map(User::getFcmToken).orElse(null);
        return token != null && !token.isBlank();
    }

    /**
     * Send a push notification to a specific FCM device token (e.g. for testing).
     * Uses FirebaseMessaging to send title and body. No-op if Firebase is not initialized.
     */
    public String sendToToken(String token, String title, String body) {
        if (token == null || token.isBlank()) {
            log.info("FCM sendToToken skipped: token is null or blank");
            return null;
        }
        if (FirebaseApp.getApps().isEmpty()) {
            log.info("FCM sendToToken skipped: Firebase not initialized (set APP_FIREBASE_SERVICE_ACCOUNT_JSON or firebase-service-account.json)");
            return null;
        }
        String t = token.trim();
        log.info("FCM sending to token (length={}): title='{}', body='{}'", t.length(), title, body);
        try {
            Message message = Message.builder()
                    .setToken(t)
                    .setNotification(Notification.builder()
                            .setTitle(title != null ? title : "")
                            .setBody(body != null ? body : "")
                            .build())
                    .build();
            String messageId = FirebaseMessaging.getInstance().send(message);
            log.info("FCM sendToToken success: messageId={}", messageId);
            return messageId;
        } catch (FirebaseMessagingException e) {
            log.warn("FCM sendToToken failed: {} (token may be invalid/expired)", e.getMessage());
            throw new com.example.demoapp.exception.BadRequestException("FCM send failed: " + e.getMessage());
        }
    }
}
