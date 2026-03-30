package com.example.demoapp.controller;

import com.example.demoapp.dto.MahirResponse;
import com.example.demoapp.security.UserPrincipal;
import com.example.demoapp.service.MahirSearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mahirs")
@Tag(name = "Mahirs", description = "Search and get Mahirs (professionals). Public profile shape: docs/MAHIR_PUBLIC_PROFILE.md")
@RequiredArgsConstructor
public class MahirController {

    private final MahirSearchService mahirSearchService;

    /**
     * When false, email and phone are omitted unless the caller presents a valid JWT (any role).
     * Default true preserves previous behavior for anonymous map/profile callers.
     */
    @Value("${app.mahir-profile.expose-pii-without-auth:true}")
    private boolean exposePiiWithoutAuth;

    @GetMapping
    public ResponseEntity<Page<MahirResponse>> searchMahirs(
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 20, sort = "id") Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean mask = !exposePiiWithoutAuth && principal == null;
        Page<MahirResponse> page = mahirSearchService.searchMahirs(categoryId, pageable, mask);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MahirResponse> getMahirById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean mask = !exposePiiWithoutAuth && principal == null;
        MahirResponse mahir = mahirSearchService.getMahirById(id, mask);
        return ResponseEntity.ok(mahir);
    }
}
