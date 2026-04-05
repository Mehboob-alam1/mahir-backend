package com.example.demoapp.service;

import com.example.demoapp.dto.BannerRequest;
import com.example.demoapp.dto.BannerResponse;
import com.example.demoapp.entity.Banner;
import com.example.demoapp.entity.PlanAudience;
import com.example.demoapp.entity.Role;
import com.example.demoapp.exception.BadRequestException;
import com.example.demoapp.exception.ResourceNotFoundException;
import com.example.demoapp.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerService {

    private final BannerRepository bannerRepository;

    public List<BannerResponse> listActiveForHome(Role role) {
        Instant now = Instant.now();
        Set<PlanAudience> audiences = EnumSet.of(PlanAudience.BOTH);
        if (role == Role.USER) {
            audiences.add(PlanAudience.USER);
        } else if (role == Role.MAHIR) {
            audiences.add(PlanAudience.MAHIR);
        }
        return bannerRepository.findActiveByAudienceIn(audiences).stream()
                .filter(b -> b.isCurrentlyValid(now))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Page<BannerResponse> listAdmin(Pageable pageable) {
        return bannerRepository.findAllByOrderBySortOrderAscIdAsc(pageable).map(this::toResponse);
    }

    @Transactional
    public BannerResponse create(BannerRequest request) {
        if (request.getImageUrl() == null || request.getImageUrl().isBlank()) {
            throw new BadRequestException("imageUrl is required");
        }
        if (request.getAudience() == null) {
            throw new BadRequestException("audience is required");
        }
        Banner b = Banner.builder()
                .title(request.getTitle() != null ? request.getTitle().trim() : null)
                .imageUrl(request.getImageUrl().trim())
                .linkUrl(request.getLinkUrl() != null ? request.getLinkUrl().trim() : null)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .active(request.getActive() == null || request.getActive())
                .audience(request.getAudience())
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .build();
        return toResponse(bannerRepository.save(b));
    }

    @Transactional
    public BannerResponse update(Long id, BannerRequest request) {
        Banner b = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner", id));
        if (request.getTitle() != null) {
            b.setTitle(request.getTitle().trim());
        }
        if (request.getImageUrl() != null) {
            b.setImageUrl(request.getImageUrl().trim());
        }
        if (request.getLinkUrl() != null) {
            b.setLinkUrl(request.getLinkUrl().trim());
        }
        if (request.getSortOrder() != null) {
            b.setSortOrder(request.getSortOrder());
        }
        if (request.getActive() != null) {
            b.setActive(request.getActive());
        }
        if (request.getAudience() != null) {
            b.setAudience(request.getAudience());
        }
        if (request.getStartsAt() != null || request.getEndsAt() != null) {
            if (request.getStartsAt() != null) {
                b.setStartsAt(request.getStartsAt());
            }
            if (request.getEndsAt() != null) {
                b.setEndsAt(request.getEndsAt());
            }
        }
        return toResponse(bannerRepository.save(b));
    }

    @Transactional
    public void delete(Long id) {
        if (!bannerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Banner", id);
        }
        bannerRepository.deleteById(id);
    }

    private BannerResponse toResponse(Banner b) {
        return BannerResponse.builder()
                .id(b.getId())
                .title(b.getTitle())
                .imageUrl(b.getImageUrl())
                .linkUrl(b.getLinkUrl())
                .sortOrder(b.getSortOrder())
                .active(b.isActive())
                .audience(b.getAudience())
                .startsAt(b.getStartsAt())
                .endsAt(b.getEndsAt())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
