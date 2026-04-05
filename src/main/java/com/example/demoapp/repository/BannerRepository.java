package com.example.demoapp.repository;

import com.example.demoapp.entity.Banner;
import com.example.demoapp.entity.PlanAudience;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {

    Page<Banner> findAllByOrderBySortOrderAscIdAsc(Pageable pageable);

    @Query("SELECT b FROM Banner b WHERE b.active = true AND b.audience IN :audiences ORDER BY b.sortOrder ASC, b.id ASC")
    List<Banner> findActiveByAudienceIn(@Param("audiences") Collection<PlanAudience> audiences);

    @Query("SELECT COUNT(b) FROM Banner b WHERE b.active = true AND (b.startsAt IS NULL OR b.startsAt <= :now) AND (b.endsAt IS NULL OR b.endsAt >= :now)")
    long countCurrentlyValid(@Param("now") Instant now);
}
