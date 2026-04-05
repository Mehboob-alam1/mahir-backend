package com.example.demoapp.service;

import com.example.demoapp.dto.AdminMembershipRowResponse;
import com.example.demoapp.dto.MembershipPlanResponse;
import com.example.demoapp.dto.MembershipSubscribeRequest;
import com.example.demoapp.dto.MyMembershipResponse;
import com.example.demoapp.entity.*;
import com.example.demoapp.exception.BadRequestException;
import com.example.demoapp.exception.ResourceNotFoundException;
import com.example.demoapp.repository.MembershipPlanRepository;
import com.example.demoapp.repository.UserMembershipRepository;
import com.example.demoapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserMembershipSelfService {

    private final UserRepository userRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final UserMembershipRepository userMembershipRepository;

    public MyMembershipResponse getMyMembership(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (user.getRole() == Role.ADMIN) {
            return MyMembershipResponse.builder()
                    .plan(null)
                    .activeMembership(null)
                    .history(List.of())
                    .build();
        }
        List<UserMembership> rows = userMembershipRepository.findByUserOrderByCreatedAtDesc(user);
        UserMembership active = userMembershipRepository.findByUserAndStatus(user, UserMembershipStatus.ACTIVE)
                .orElse(null);
        MembershipPlanResponse planDto = active != null ? toPlanResponse(active.getPlan()) : null;
        return MyMembershipResponse.builder()
                .plan(planDto)
                .activeMembership(active != null ? toMembershipRow(active) : null)
                .history(rows.stream().map(this::toMembershipRow).collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public AdminMembershipRowResponse subscribe(Long userId, MembershipSubscribeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Memberships are not used for ADMIN accounts");
        }
        MembershipPlan plan = membershipPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan", request.getPlanId()));
        if (!plan.isActive()) {
            throw new BadRequestException("Plan is inactive");
        }
        validatePlanAudience(plan, user.getRole());
        userMembershipRepository.findByUserAndStatus(user, UserMembershipStatus.ACTIVE)
                .ifPresent(um -> {
                    um.setStatus(UserMembershipStatus.CANCELLED);
                    userMembershipRepository.save(um);
                });
        UserMembership um = UserMembership.builder()
                .user(user)
                .plan(plan)
                .startedAt(Instant.now())
                .expiresAt(null)
                .status(UserMembershipStatus.ACTIVE)
                .build();
        return toMembershipRow(userMembershipRepository.save(um));
    }

    @Transactional
    public void cancelMyMembership(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        UserMembership active = userMembershipRepository.findByUserAndStatus(user, UserMembershipStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("No active membership"));
        active.setStatus(UserMembershipStatus.CANCELLED);
        userMembershipRepository.save(active);
    }

    private void validatePlanAudience(MembershipPlan plan, Role userRole) {
        PlanAudience a = plan.getAudience();
        if (a == PlanAudience.BOTH) {
            return;
        }
        if (a == PlanAudience.USER && userRole != Role.USER) {
            throw new BadRequestException("This plan is only for customers (USER)");
        }
        if (a == PlanAudience.MAHIR && userRole != Role.MAHIR) {
            throw new BadRequestException("This plan is only for Mahirs");
        }
    }

    private MembershipPlanResponse toPlanResponse(MembershipPlan p) {
        return MembershipPlanResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .code(p.getCode())
                .description(p.getDescription())
                .audience(p.getAudience())
                .priceMonthly(p.getPriceMonthly())
                .currency(p.getCurrency())
                .featuresText(p.getFeaturesText())
                .active(p.isActive())
                .sortOrder(p.getSortOrder())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private AdminMembershipRowResponse toMembershipRow(UserMembership um) {
        User u = um.getUser();
        MembershipPlan p = um.getPlan();
        return AdminMembershipRowResponse.builder()
                .id(um.getId())
                .userId(u.getId())
                .userEmail(u.getEmail())
                .planId(p.getId())
                .planName(p.getName())
                .planCode(p.getCode())
                .status(um.getStatus())
                .startedAt(um.getStartedAt())
                .expiresAt(um.getExpiresAt())
                .build();
    }
}
