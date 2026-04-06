package com.example.demoapp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferencesPatchRequest {

    private Boolean inboxMessages;
    private Boolean ratingReminders;
    private Boolean promotionsAndTips;
    private Boolean yourAccount;
}
