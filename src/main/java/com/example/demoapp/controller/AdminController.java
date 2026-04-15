package com.example.demoapp.controller;

import com.example.demoapp.dto.*;
import com.example.demoapp.entity.Role;
import com.example.demoapp.service.AdminService;
import com.example.demoapp.service.BannerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "ADMIN role only. Use JWT from an admin account login.")
public class AdminController {

    private final AdminService adminService;
    private final BannerService bannerService;

    @GetMapping("/dashboard/summary")
    public ResponseEntity<AdminDashboardSummaryResponse> dashboardSummary() {
        return ResponseEntity.ok(adminService.getDashboardSummary());
    }

    @GetMapping("/dashboard/engagement")
    public ResponseEntity<AdminEngagementResponse> dashboardEngagement(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(adminService.getDashboardEngagement(from, to));
    }

    @GetMapping("/banners")
    public ResponseEntity<Page<BannerResponse>> listBanners(
            @PageableDefault(size = 20, sort = "sortOrder", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(bannerService.listAdmin(pageable));
    }

    /**
     * Multipart field name: {@code file}. Returns {@code imageUrl} / {@code url} for {@code POST /api/admin/banners} JSON body.
     */
    @PostMapping(value = "/banners/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdminBannerImageUploadResponse> uploadBannerImage(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(bannerService.uploadBannerImage(file));
    }

    @PostMapping("/banners")
    public ResponseEntity<BannerResponse> createBanner(@Valid @RequestBody BannerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bannerService.create(request));
    }

    @PutMapping("/banners/{id}")
    public ResponseEntity<BannerResponse> putBanner(@PathVariable Long id, @Valid @RequestBody BannerRequest request) {
        return ResponseEntity.ok(bannerService.update(id, request));
    }

    @PatchMapping("/banners/{id}")
    public ResponseEntity<BannerResponse> patchBanner(@PathVariable Long id, @Valid @RequestBody BannerRequest request) {
        return ResponseEntity.ok(bannerService.update(id, request));
    }

    @DeleteMapping("/banners/{id}")
    public ResponseEntity<Void> deleteBanner(@PathVariable Long id) {
        bannerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean blocked,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminService.listUsers(search, role, blocked, pageable));
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

    @PatchMapping("/users/{id}")
    public ResponseEntity<UserResponse> patchUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminPatchUserRequest request) {
        return ResponseEntity.ok(adminService.patchUser(id, request));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUserByAdmin(request));
    }

    @GetMapping("/users/{id}/membership")
    public ResponseEntity<AdminUserMembershipDetailResponse> getUserMembership(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserMembershipAdmin(id));
    }

    @PatchMapping("/users/{id}/membership")
    public ResponseEntity<AdminUserMembershipDetailResponse> patchUserMembership(
            @PathVariable Long id,
            @Valid @RequestBody AdminPatchMembershipRequest request) {
        return ResponseEntity.ok(adminService.patchUserMembershipAdmin(id, request));
    }

    @PostMapping("/users/{id}/membership/revoke")
    public ResponseEntity<Void> revokeUserMembership(@PathVariable Long id) {
        adminService.cancelActiveMembership(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/jobs")
    public ResponseEntity<Page<JobResponse>> listJobs(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminService.listAllJobs(pageable));
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<JobResponse> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getJobByIdForAdmin(id));
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        adminService.deleteJobByAdmin(id);
        return ResponseEntity.noContent().build();
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

    @PatchMapping("/reviews/{id}")
    public ResponseEntity<ReviewResponse> patchReview(
            @PathVariable Long id,
            @Valid @RequestBody AdminReviewPatchRequest request) {
        return ResponseEntity.ok(adminService.patchReview(id, request));
    }

    @PatchMapping("/jobs/{id}")
    public ResponseEntity<JobResponse> patchJob(
            @PathVariable Long id,
            @Valid @RequestBody AdminJobPatchRequest request) {
        return ResponseEntity.ok(adminService.patchJob(id, request));
    }

    @GetMapping("/support/chats")
    public ResponseEntity<Page<AdminChatThreadResponse>> listSupportChats(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminService.listSupportThreads(pageable));
    }

    @GetMapping("/support/chats/{threadId}")
    public ResponseEntity<AdminChatThreadResponse> getSupportChat(@PathVariable Long threadId) {
        return ResponseEntity.ok(adminService.getSupportChatThread(threadId));
    }

    @GetMapping("/support/chats/{threadId}/messages")
    public ResponseEntity<Page<ChatMessageResponse>> listChatMessages(
            @PathVariable Long threadId,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(adminService.listThreadMessagesForSupport(threadId, pageable));
    }

    @PostMapping("/support/chats/{threadId}/messages")
    public ResponseEntity<ChatMessageResponse> postSupportMessage(
            @AuthenticationPrincipal com.example.demoapp.security.UserPrincipal principal,
            @PathVariable Long threadId,
            @Valid @RequestBody ChatMessageRequest request) {
        if (principal == null) {
            throw new com.example.demoapp.exception.UnauthorizedException("Authentication required");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminService.postSupportAdminMessage(threadId, principal.getUserId(), request));
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

    @PutMapping("/faqs/{id}")
    public ResponseEntity<FaqResponse> putFaq(@PathVariable Long id, @Valid @RequestBody FaqRequest request) {
        return ResponseEntity.ok(adminService.updateFaq(id, request));
    }

    @DeleteMapping("/faqs/{id}")
    public ResponseEntity<Void> deleteFaq(@PathVariable Long id) {
        adminService.deleteFaq(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/membership-plans")
    public ResponseEntity<Map<String, Object>> listPlans() {
        List<MembershipPlanResponse> list = adminService.listMembershipPlans();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("content", list);
        body.put("plans", list);
        body.put("items", list);
        body.put("results", list);
        body.put("data", Map.of("content", list));
        return ResponseEntity.ok(body);
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

    @PutMapping("/membership-plans/{id}")
    public ResponseEntity<MembershipPlanResponse> putPlan(
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
