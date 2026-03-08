package com.example.demoapp.controller;

import com.example.demoapp.dto.BidResponse;
import com.example.demoapp.dto.BookingResponse;
import com.example.demoapp.entity.BidStatus;
import com.example.demoapp.security.UserPrincipal;
import com.example.demoapp.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.demoapp.dto.BidRequest;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Bids", description = "Mahir bids on jobs; User accepts/rejects")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    @PostMapping("/jobs/{jobId}/bids")
    public ResponseEntity<BidResponse> createBid(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long jobId,
            @Valid @RequestBody BidRequest request) {
        if (principal == null) throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        BidResponse response = bidService.create(jobId, principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/jobs/{jobId}/bids")
    public ResponseEntity<Page<BidResponse>> listBidsForJob(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long jobId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        if (principal == null) throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        return ResponseEntity.ok(bidService.listBidsForJob(jobId, principal.getUserId(), pageable));
    }

    @GetMapping("/bids")
    public ResponseEntity<Page<BidResponse>> listMyBids(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) BidStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        if (principal == null) throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        return ResponseEntity.ok(bidService.listMyBids(principal.getUserId(), status, pageable));
    }

    @PostMapping("/jobs/{jobId}/bids/{bidId}/accept")
    public ResponseEntity<BookingResponse> acceptBid(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long jobId,
            @PathVariable Long bidId) {
        if (principal == null) throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        BookingResponse booking = bidService.acceptBid(jobId, bidId, principal.getUserId());
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/jobs/{jobId}/bids/{bidId}/reject")
    public ResponseEntity<Void> rejectBid(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long jobId,
            @PathVariable Long bidId) {
        if (principal == null) throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        bidService.rejectBid(jobId, bidId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}
