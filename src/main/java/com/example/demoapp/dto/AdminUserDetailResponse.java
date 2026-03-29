package com.example.demoapp.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserDetailResponse {

    private UserResponse profile;
    private long jobsPostedCount;
    private long reviewsReceivedAsMahirCount;
    private long reviewsWrittenAsCustomerCount;
    private List<AdminMembershipRowResponse> memberships;
}
