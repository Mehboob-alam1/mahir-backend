package com.example.demoapp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferencesResponse {

    private boolean inboxMessages;
    private boolean ratingReminders;
    private boolean promotionsAndTips;
    private boolean yourAccount;
}
