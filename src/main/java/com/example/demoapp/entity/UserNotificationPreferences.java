package com.example.demoapp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotificationPreferences {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "inbox_messages", nullable = false)
    @Builder.Default
    private boolean inboxMessages = true;

    @Column(name = "rating_reminders", nullable = false)
    @Builder.Default
    private boolean ratingReminders = true;

    @Column(name = "promotions_and_tips", nullable = false)
    @Builder.Default
    private boolean promotionsAndTips = true;

    @Column(name = "your_account", nullable = false)
    @Builder.Default
    private boolean yourAccount = true;
}
