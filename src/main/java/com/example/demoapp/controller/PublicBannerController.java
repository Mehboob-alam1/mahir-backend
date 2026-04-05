package com.example.demoapp.controller;

import com.example.demoapp.dto.BannerResponse;
import com.example.demoapp.entity.Role;
import com.example.demoapp.security.UserPrincipal;
import com.example.demoapp.service.BannerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
@Tag(name = "Banners", description = "Home banners (optional JWT refines audience for USER/MAHIR)")
public class PublicBannerController {

    private final BannerService bannerService;

    @GetMapping
    public ResponseEntity<List<BannerResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
        Role role = null;
        if (principal != null && (principal.getRole() == Role.USER || principal.getRole() == Role.MAHIR)) {
            role = principal.getRole();
        }
        return ResponseEntity.ok(bannerService.listActiveForHome(role));
    }
}
