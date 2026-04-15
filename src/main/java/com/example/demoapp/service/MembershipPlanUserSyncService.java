package com.example.demoapp.service;

import com.example.demoapp.entity.AccountType;
import com.example.demoapp.entity.MembershipPlan;
import com.example.demoapp.entity.Role;
import com.example.demoapp.entity.User;
import com.example.demoapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MembershipPlanUserSyncService {

    private final UserRepository userRepository;

    /**
     * After a Mahir is linked to a membership plan row, align {@link AccountType} and WhatsApp credits
     * from plan code/name (freemium → 3 credits, premium → high balance).
     */
    @Transactional
    public void onPlanAssigned(User user, MembershipPlan plan) {
        if (user.getRole() != Role.MAHIR) {
            return;
        }
        String code = plan.getCode() != null ? plan.getCode().toLowerCase() : "";
        String name = plan.getName() != null ? plan.getName().toLowerCase() : "";
        if (code.contains("freemium") || name.contains("freemium")) {
            user.setAccountType(AccountType.FREEMIUM);
            user.setCredits(3);
        } else if (code.contains("premium") || name.contains("premium")) {
            user.setAccountType(AccountType.PREMIUM);
            user.setCredits(9999);
        }
        userRepository.save(user);
    }

    @Transactional
    public void onMembershipCancelled(User user) {
        if (user.getRole() != Role.MAHIR) {
            return;
        }
        user.setAccountType(AccountType.FREEMIUM);
        user.setCredits(3);
        userRepository.save(user);
    }
}
