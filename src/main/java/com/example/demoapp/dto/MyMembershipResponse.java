package com.example.demoapp.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyMembershipResponse {

    private MembershipPlanResponse plan;
    private AdminMembershipRowResponse activeMembership;
    private List<AdminMembershipRowResponse> history;
}
