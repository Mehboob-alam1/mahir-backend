package com.example.demoapp.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserMembershipDetailResponse {

    private AdminMembershipRowResponse active;
    private List<AdminMembershipRowResponse> history;
}
