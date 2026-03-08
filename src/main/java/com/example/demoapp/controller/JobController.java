package com.example.demoapp.controller;

import com.example.demoapp.dto.JobRequest;
import com.example.demoapp.dto.JobResponse;
import com.example.demoapp.dto.JobUpdateRequest;
import com.example.demoapp.entity.JobStatus;
import com.example.demoapp.security.UserPrincipal;
import com.example.demoapp.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Jobs", description = "Job posts (USER creates), open for bids")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody JobRequest request) {
        if (principal == null) throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        JobResponse response = jobService.create(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<JobResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        if (principal == null) throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        Page<JobResponse> page;
        if ("my".equals(filter)) {
            page = jobService.listMyJobs(principal.getUserId(), status, pageable);
        } else {
            page = jobService.listOpenJobs(categoryId, pageable);
        }
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        if (principal == null) throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        return ResponseEntity.ok(jobService.getById(id, principal.getUserId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<JobResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody JobUpdateRequest request) {
        if (principal == null) throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        return ResponseEntity.ok(jobService.update(id, principal.getUserId(), request));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        if (principal == null) throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        jobService.cancel(id, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}
