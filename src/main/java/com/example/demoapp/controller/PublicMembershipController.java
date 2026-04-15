package com.example.demoapp.controller;

import com.example.demoapp.dto.MembershipPlanResponse;
import com.example.demoapp.entity.MembershipPlan;
import com.example.demoapp.repository.MembershipPlanRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.example.demoapp.entity.PlanAudience;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/membership-plans")
@RequiredArgsConstructor
@Tag(name = "Membership plans (public)", description = "Active plans for customers / Mahirs to view in-app")
public class PublicMembershipController {

    private final MembershipPlanRepository membershipPlanRepository;

    /**
     * Active plans ordered by sortOrder.
     *
     * @param audience optional: {@code MAHIR} → only plans with audience MAHIR or BOTH (for Mahir app pricing).
     */
    @GetMapping
    public List<MembershipPlanResponse> listActive(
            @RequestParam(required = false) String audience) {
        return membershipPlanRepository.findByActiveTrueOrderBySortOrderAsc().stream()
                .filter(p -> passesAudienceFilter(p, audience))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private boolean passesAudienceFilter(MembershipPlan p, String audienceParam) {
        if (audienceParam == null || audienceParam.isBlank()) {
            return true;
        }
        if ("MAHIR".equalsIgnoreCase(audienceParam.trim())) {
            return p.getAudience() == PlanAudience.MAHIR || p.getAudience() == PlanAudience.BOTH;
        }
        if ("USER".equalsIgnoreCase(audienceParam.trim())) {
            return p.getAudience() == PlanAudience.USER || p.getAudience() == PlanAudience.BOTH;
        }
        return true;
    }

    private MembershipPlanResponse toResponse(MembershipPlan p) {
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
}
