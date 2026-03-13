package com.example.demoapp.controller;

import com.example.demoapp.dto.TestNotificationRequest;
import com.example.demoapp.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Test endpoint to send a push notification to a specific FCM token.
 * Use for verifying Firebase setup and that the device receives push.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Test", description = "Test notification (FCM)")
@RequiredArgsConstructor
@Slf4j
public class TestNotificationController {

    private final NotificationService notificationService;

    @PostMapping("/test-notification")
    @Operation(summary = "Send test push to FCM token", description = "Sends a push with title and body to the given FCM device token. Requires Firebase to be initialized (service account).")
    public ResponseEntity<Map<String, String>> sendTestNotification(@Valid @RequestBody TestNotificationRequest request) {
        String token = request.getToken();
        String title = request.getTitle() != null ? request.getTitle() : "Test Notification";
        String body = request.getBody() != null ? request.getBody() : "This is a test notification";
        String tokenPreview = token != null && token.length() > 12
                ? token.substring(0, 8) + "..." + token.substring(token.length() - 4) : "(short)";
        log.info("Test notification request: token={}, title='{}', body='{}'", tokenPreview, title, body);
        String messageId = notificationService.sendPushToToken(token, title, body);
        if (messageId == null) {
            log.warn("Test notification failed: Firebase not initialized");
            return ResponseEntity.status(503).body(Map.of(
                    "error", "Firebase not initialized",
                    "hint", "Set APP_FIREBASE_SERVICE_ACCOUNT_JSON or place firebase-service-account.json in working directory"
            ));
        }
        log.info("Test notification sent successfully: messageId={}", messageId);
        return ResponseEntity.ok(Map.of("messageId", messageId, "status", "sent"));
    }
}
