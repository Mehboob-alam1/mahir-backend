package com.example.demoapp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardSummaryResponse {

    private long totalUsers;
    private long totalCustomers;
    private long totalMahirs;
    private long totalAdmins;
    private long totalJobs;
    private long openJobs;
    private long totalBookings;
    private long completedBookings;
    private long totalReviews;
    private long chatMessagesLast7Days;
    private long activeBanners;
}
