package com.example.demoapp.config;

import com.example.demoapp.entity.MembershipPlan;
import com.example.demoapp.entity.PlanAudience;
import com.example.demoapp.repository.MembershipPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Ensures Mahir (and customer) membership plans exist with stable {@code code} and real DB ids
 * so GET /api/membership-plans and POST /api/admin/users/{id}/membership always have planId values.
 */
@Component
@Order(45)
@RequiredArgsConstructor
@Slf4j
public class MembershipPlanSeedLoader implements ApplicationRunner {

    private final MembershipPlanRepository membershipPlanRepository;

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (membershipPlanRepository.count() > 0) {
                return;
            }
            List<MembershipPlan> plans = List.of(
                    MembershipPlan.builder()
                            .name("Mahir Freemium")
                            .code("mahir_freemium")
                            .description("Default Mahir tier. Includes 3 WhatsApp contact credits.")
                            .audience(PlanAudience.MAHIR)
                            .priceMonthly(BigDecimal.ZERO)
                            .currency("TND")
                            .featuresText("3 credits for WhatsApp job contact; apply to jobs for free.")
                            .active(true)
                            .sortOrder(0)
                            .build(),
                    MembershipPlan.builder()
                            .name("Mahir Premium")
                            .code("mahir_premium")
                            .description("Premium Mahir subscription with higher contact allowance.")
                            .audience(PlanAudience.MAHIR)
                            .priceMonthly(new BigDecimal("29.99"))
                            .currency("TND")
                            .featuresText("High credit balance for WhatsApp contacts; featured placement (if enabled in app).")
                            .active(true)
                            .sortOrder(1)
                            .build(),
                    MembershipPlan.builder()
                            .name("Find Mahir Plus (Customer)")
                            .code("customer_plus")
                            .description("Optional customer subscription plan.")
                            .audience(PlanAudience.USER)
                            .priceMonthly(new BigDecimal("9.99"))
                            .currency("TND")
                            .featuresText("Priority support and offers (configure in app).")
                            .active(true)
                            .sortOrder(10)
                            .build()
            );
            membershipPlanRepository.saveAll(plans);
            log.info("Seeded {} default membership plans (Mahir Freemium/Premium + customer plan)", plans.size());
        } catch (Exception e) {
            log.warn("Could not seed membership plans: {}", e.getMessage());
        }
    }
}
