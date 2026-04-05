package com.example.demoapp.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminEngagementResponse {

    private List<DailyCount> chatMessagesByDay;
    private List<DailyCount> newRegistrationsByDay;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyCount {
        private String date;
        private long count;
    }
}
