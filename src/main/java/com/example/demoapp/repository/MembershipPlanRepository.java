package com.example.demoapp.repository;

import com.example.demoapp.entity.MembershipPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {

    Optional<MembershipPlan> findByCode(String code);

    List<MembershipPlan> findByActiveTrueOrderBySortOrderAsc();
}
