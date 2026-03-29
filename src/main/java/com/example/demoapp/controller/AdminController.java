package com.example.demoapp.controller;

import com.example.demoapp.dto.*;
import com.example.demoapp.entity.Role;
import com.example.demoapp.service.AdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "ADMIN role only. Use JWT from an admin account login.")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> listUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean blocked,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminService.listUsers(role, blocked, pageable));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserDetailResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserDetail(id));
    }

    @PatchMapping("/users/{id}/block")
    public ResponseEntity<UserResponse> blockUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminBlockUserRequest request) {
        return ResponseEntity.ok(adminService.setUserBlocked(id, request));
    }

    @GetMapping("/jobs")
    public ResponseEntity<Page<JobResponse>> listJobs(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminService.listAllJobs(pageable));
    }

    @GetMapping("/reviews")
    public ResponseEntity<Page<ReviewResponse>> listReviews(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminService.listAllReviews(pageable));
    }

    @PatchMapping("/reviews/{id}/visibility")
    public ResponseEntity<ReviewResponse> setReviewVisibility(
            @PathVariable Long id,
            @Valid @RequestBody AdminReviewVisibilityRequest request) {
        return ResponseEntity.ok(adminService.setReviewVisibility(id, request));
    }

    @GetMapping("/support/chats")
    public ResponseEntity<Page<AdminChatThreadResponse>> listSupportChats(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminService.listSupportThreads(pageable));
    }

    @GetMapping("/support/chats/{threadId}/messages")
    public ResponseEntity<Page<ChatMessageResponse>> listChatMessages(
            @PathVariable Long threadId,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(adminService.listThreadMessagesForSupport(threadId, pageable));
    }

    @GetMapping("/faqs")
    public ResponseEntity<List<FaqResponse>> listFaqsAdmin() {
        return ResponseEntity.ok(adminService.listAllFaqs());
    }

    @PostMapping("/faqs")
    public ResponseEntity<FaqResponse> createFaq(@Valid @RequestBody FaqRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createFaq(request));
    }

    @PatchMapping("/faqs/{id}")
    public ResponseEntity<FaqResponse> updateFaq(@PathVariable Long id, @Valid @RequestBody FaqRequest request) {
        return ResponseEntity.ok(adminService.updateFaq(id, request));
    }

    @DeleteMapping("/faqs/{id}")
    public ResponseEntity<Void> deleteFaq(@PathVariable Long id) {
        adminService.deleteFaq(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/membership-plans")
    public ResponseEntity<List<MembershipPlanResponse>> listPlans() {
        return ResponseEntity.ok(adminService.listMembershipPlans());
    }

    @PostMapping("/membership-plans")
    public ResponseEntity<MembershipPlanResponse> createPlan(@Valid @RequestBody MembershipPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createMembershipPlan(request));
    }

    @PatchMapping("/membership-plans/{id}")
    public ResponseEntity<MembershipPlanResponse> updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody MembershipPlanRequest request) {
        return ResponseEntity.ok(adminService.updateMembershipPlan(id, request));
    }

    @DeleteMapping("/membership-plans/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        adminService.deleteMembershipPlan(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/memberships")
    public ResponseEntity<Page<AdminMembershipRowResponse>> listMemberships(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminService.listAllMemberships(pageable));
    }

    @PostMapping("/users/{userId}/membership")
    public ResponseEntity<AdminMembershipRowResponse> assignMembership(
            @PathVariable Long userId,
            @Valid @RequestBody AssignMembershipRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.assignMembership(userId, request));
    }

    @DeleteMapping("/users/{userId}/membership")
    public ResponseEntity<Void> cancelMembership(@PathVariable Long userId) {
        adminService.cancelActiveMembership(userId);
        return ResponseEntity.noContent().build();
    }
}
