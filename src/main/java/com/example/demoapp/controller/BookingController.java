package com.example.demoapp.controller;

import com.example.demoapp.dto.BookingRequest;
import com.example.demoapp.dto.BookingResponse;
import com.example.demoapp.entity.BookingStatus;
import com.example.demoapp.security.UserPrincipal;
import com.example.demoapp.service.BookingService;
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
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Create and manage bookings (USER requests MAHIR)")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BookingRequest request) {
        if (principal == null) {
            throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        }
        BookingResponse response = bookingService.create(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) BookingStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        if (principal == null) {
            throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        }
        Page<BookingResponse> page = bookingService.getMyBookings(principal.getUserId(), status, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        if (principal == null) {
            throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        }
        BookingResponse booking = bookingService.getById(id, principal.getUserId());
        return ResponseEntity.ok(booking);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BookingResponse> updateStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestParam BookingStatus status) {
        if (principal == null) {
            throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        }
        BookingResponse response = bookingService.updateStatus(id, principal.getUserId(), status);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancel(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        if (principal == null) {
            throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        }
        return ResponseEntity.ok(bookingService.cancel(id, principal.getUserId(), reason));
    }
}
