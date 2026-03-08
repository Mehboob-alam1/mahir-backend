package com.example.demoapp.controller;

import com.example.demoapp.dto.CreateReviewRequest;
import com.example.demoapp.dto.ReviewResponse;
import com.example.demoapp.security.UserPrincipal;
import com.example.demoapp.service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Reviews", description = "Create review for completed booking; list reviews by Mahir")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/reviews")
    public ResponseEntity<ReviewResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateReviewRequest request) {
        if (principal == null) {
            throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        }
        ReviewResponse response = reviewService.create(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/mahirs/{mahirId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getByMahirId(
            @PathVariable Long mahirId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<ReviewResponse> page = reviewService.getByMahirId(mahirId, pageable);
        return ResponseEntity.ok(page);
    }
}
