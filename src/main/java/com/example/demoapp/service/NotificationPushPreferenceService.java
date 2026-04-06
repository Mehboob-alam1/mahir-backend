package com.example.demoapp.service;

import com.example.demoapp.entity.User;
import com.example.demoapp.entity.UserNotificationPreferences;
import com.example.demoapp.repository.UserNotificationPreferencesRepository;
import com.example.demoapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Maps in-app notification {@code type} strings to user preference flags and decides if FCM push is allowed.
 */
@Service
@RequiredArgsConstructor
public class NotificationPushPreferenceService {

    private final UserRepository userRepository;
    private final UserNotificationPreferencesRepository preferencesRepository;

    /**
     * Returns true if a push notification may be sent for this user and notification type.
     */
    @Transactional(readOnly = true)
    public boolean isPushAllowed(Long userId, String notificationType) {
        if (userId == null || notificationType == null) {
            return true;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        return preferencesRepository.findByUser(user)
                .map(p -> switch (categoryForType(notificationType)) {
                    case INBOX -> p.isInboxMessages();
                    case RATING -> p.isRatingReminders();
                    case PROMO -> p.isPromotionsAndTips();
                    case ACCOUNT -> p.isYourAccount();
                })
                .orElse(true);
    }

    private enum Category {
        INBOX,
        RATING,
        PROMO,
        ACCOUNT
    }

    private Category categoryForType(String type) {
        if (type == null) {
            return Category.ACCOUNT;
        }
        return switch (type) {
            case "CHAT_MESSAGE" -> Category.INBOX;
            case "NEW_REVIEW", "JOB_COMPLETED" -> Category.RATING;
            case "NEW_JOB" -> Category.PROMO;
            default -> Category.ACCOUNT;
        };
    }
}
