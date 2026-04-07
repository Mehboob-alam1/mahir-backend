package com.example.demoapp.service;

import com.example.demoapp.dto.AdminBannerImageUploadResponse;
import com.example.demoapp.dto.BannerRequest;
import com.example.demoapp.dto.BannerResponse;
import com.example.demoapp.entity.Banner;
import com.example.demoapp.entity.PlanAudience;
import com.example.demoapp.entity.Role;
import com.example.demoapp.exception.BadRequestException;
import com.example.demoapp.exception.PayloadTooLargeException;
import com.example.demoapp.exception.ResourceNotFoundException;
import com.example.demoapp.exception.UnsupportedMediaTypeAppException;
import com.example.demoapp.repository.BannerRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );
    private static final long MAX_BANNER_IMAGE_BYTES = 5L * 1024 * 1024;

    private final BannerRepository bannerRepository;

    @Value("${app.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

    @Value("${app.files.upload-dir:uploads}")
    private String uploadDir;

    private Path bannerImageRoot;

    @PostConstruct
    void initBannerStorage() throws IOException {
        bannerImageRoot = Path.of(uploadDir).resolve("banners").toAbsolutePath().normalize();
        Files.createDirectories(bannerImageRoot);
    }

    public Path getBannerImageRoot() {
        return bannerImageRoot;
    }

    /**
     * Stores a banner image and returns public URLs for use in {@link BannerRequest#setImageUrl(String)}.
     * Multipart field name: {@code file}. Accepts JPEG, PNG, WebP up to 5 MB.
     */
    public AdminBannerImageUploadResponse uploadBannerImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required (multipart field name: file)");
        }
        String ct = file.getContentType();
        if (ct == null || !ALLOWED_IMAGE_TYPES.contains(ct.toLowerCase(Locale.ROOT))) {
            throw new UnsupportedMediaTypeAppException("Unsupported media type; use JPEG, PNG, or WebP");
        }
        if (file.getSize() > MAX_BANNER_IMAGE_BYTES) {
            throw new PayloadTooLargeException("File too large (max 5 MB)");
        }
        String ext = extensionForContentType(ct);
        String storedName = UUID.randomUUID() + "." + ext;
        Path target = bannerImageRoot.resolve(storedName);
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BadRequestException("Could not store file: " + e.getMessage());
        }
        String base = publicBaseUrl.replaceAll("/+$", "");
        String publicPath = "/api/files/banners/" + storedName;
        String fullUrl = base + publicPath;
        return AdminBannerImageUploadResponse.builder()
                .imageUrl(fullUrl)
                .url(fullUrl)
                .path(publicPath)
                .build();
    }

    private static String extensionForContentType(String ct) {
        String c = ct.toLowerCase(Locale.ROOT);
        if (c.contains("jpeg") || c.contains("jpg")) {
            return "jpg";
        }
        if (c.contains("png")) {
            return "png";
        }
        return "webp";
    }

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
