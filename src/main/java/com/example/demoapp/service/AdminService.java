package com.example.demoapp.service;

import com.example.demoapp.dto.*;
import com.example.demoapp.entity.*;
import com.example.demoapp.exception.BadRequestException;
import com.example.demoapp.exception.ResourceNotFoundException;
import com.example.demoapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final BidRepository bidRepository;
    private final ReviewRepository reviewRepository;
    private final ChatThreadRepository chatThreadRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final FaqRepository faqRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final UserMembershipRepository userMembershipRepository;
    private final BannerRepository bannerRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;

    public Page<UserResponse> listUsers(String search, Role role, Boolean blocked, Pageable pageable) {
        String q = search != null ? search.trim() : "";
        return userRepository.adminSearch(q, role, blocked, pageable).map(this::toAdminUserResponse);
    }

    public AdminUserDetailResponse getUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        long jobsPosted = jobRepository.countByPostedBy(user);
        long reviewsAsMahir = user.getRole() == Role.MAHIR ? reviewRepository.countByMahir(user) : 0;
        long reviewsAsCustomer = user.getRole() == Role.USER ? reviewRepository.countByReviewer(user) : 0;
        List<AdminMembershipRowResponse> memberships = userMembershipRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toMembershipRow)
                .collect(Collectors.toList());
        return AdminUserDetailResponse.builder()
                .profile(toAdminUserResponse(user))
                .jobsPostedCount(jobsPosted)
                .reviewsReceivedAsMahirCount(reviewsAsMahir)
                .reviewsWrittenAsCustomerCount(reviewsAsCustomer)
                .memberships(memberships)
                .build();
    }

    @Transactional
    public UserResponse setUserBlocked(Long userId, AdminBlockUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Cannot block an ADMIN account");
        }
        user.setBlocked(Boolean.TRUE.equals(request.getBlocked()));
        user.setBlockedAt(user.isBlocked() ? Instant.now() : null);
        user.setBlockedReason(user.isBlocked() ? request.getReason() : null);
        userRepository.save(user);
        return toAdminUserResponse(user);
    }

    public Page<JobResponse> listAllJobs(Pageable pageable) {
        return jobRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toJobResponse);
    }

    public Page<ReviewResponse> listAllReviews(Pageable pageable) {
        return reviewRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toAdminReviewResponse);
    }

    @Transactional
    public ReviewResponse setReviewVisibility(Long reviewId, AdminReviewVisibilityRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));
        review.setHiddenFromPublic(Boolean.TRUE.equals(request.getHiddenFromPublic()));
        reviewRepository.save(review);
        return toAdminReviewResponse(review);
    }

    public Page<AdminChatThreadResponse> listSupportThreads(Pageable pageable) {
        return chatThreadRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toChatThreadResponse);
    }

    public Page<ChatMessageResponse> listThreadMessagesForSupport(Long threadId, Pageable pageable) {
        ChatThread thread = chatThreadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat thread", threadId));
        return chatMessageRepository.findByThreadOrderByCreatedAtAsc(thread, pageable)
                .map(this::toChatMessageResponse);
    }

    public List<FaqResponse> listAllFaqs() {
        return faqRepository.findAll(Sort.by(Sort.Direction.ASC, "sortOrder")).stream()
                .map(this::toFaqResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FaqResponse createFaq(FaqRequest request) {
        Faq faq = Faq.builder()
                .question(request.getQuestion().trim())
                .answer(request.getAnswer().trim())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .active(request.getActive() == null || request.getActive())
                .build();
        return toFaqResponse(faqRepository.save(faq));
    }

    @Transactional
    public FaqResponse updateFaq(Long id, FaqRequest request) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ", id));
        if (request.getQuestion() != null) {
            faq.setQuestion(request.getQuestion().trim());
        }
        if (request.getAnswer() != null) {
            faq.setAnswer(request.getAnswer().trim());
        }
        if (request.getSortOrder() != null) {
            faq.setSortOrder(request.getSortOrder());
        }
        if (request.getActive() != null) {
            faq.setActive(request.getActive());
        }
        return toFaqResponse(faqRepository.save(faq));
    }

    @Transactional
    public void deleteFaq(Long id) {
        if (!faqRepository.existsById(id)) {
            throw new ResourceNotFoundException("FAQ", id);
        }
        faqRepository.deleteById(id);
    }

    public List<MembershipPlanResponse> listMembershipPlans() {
        return membershipPlanRepository.findAll().stream()
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .map(this::toPlanResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MembershipPlanResponse createMembershipPlan(MembershipPlanRequest request) {
        if (membershipPlanRepository.findByCode(request.getCode().trim()).isPresent()) {
            throw new BadRequestException("Plan code already exists: " + request.getCode());
        }
        MembershipPlan plan = MembershipPlan.builder()
                .name(request.getName().trim())
                .code(request.getCode().trim())
                .description(request.getDescription())
                .audience(request.getAudience())
                .priceMonthly(request.getPriceMonthly())
                .currency(request.getCurrency() != null ? request.getCurrency() : "TND")
                .featuresText(request.getFeaturesText())
                .active(request.getActive() == null || request.getActive())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();
        return toPlanResponse(membershipPlanRepository.save(plan));
    }

    @Transactional
    public MembershipPlanResponse updateMembershipPlan(Long id, MembershipPlanRequest request) {
        MembershipPlan plan = membershipPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan", id));
        if (request.getName() != null) {
            plan.setName(request.getName().trim());
        }
        if (request.getCode() != null && !request.getCode().equals(plan.getCode())) {
            if (membershipPlanRepository.findByCode(request.getCode().trim()).isPresent()) {
                throw new BadRequestException("Plan code already in use");
            }
            plan.setCode(request.getCode().trim());
        }
        if (request.getDescription() != null) {
            plan.setDescription(request.getDescription());
        }
        if (request.getAudience() != null) {
            plan.setAudience(request.getAudience());
        }
        if (request.getPriceMonthly() != null) {
            plan.setPriceMonthly(request.getPriceMonthly());
        }
        if (request.getCurrency() != null) {
            plan.setCurrency(request.getCurrency());
        }
        if (request.getFeaturesText() != null) {
            plan.setFeaturesText(request.getFeaturesText());
        }
        if (request.getActive() != null) {
            plan.setActive(request.getActive());
        }
        if (request.getSortOrder() != null) {
            plan.setSortOrder(request.getSortOrder());
        }
        return toPlanResponse(membershipPlanRepository.save(plan));
    }

    @Transactional
    public void deleteMembershipPlan(Long id) {
        MembershipPlan plan = membershipPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan", id));
        plan.setActive(false);
        membershipPlanRepository.save(plan);
    }

    public Page<AdminMembershipRowResponse> listAllMemberships(Pageable pageable) {
        return userMembershipRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toMembershipRow);
    }

    @Transactional
    public AdminMembershipRowResponse assignMembership(Long userId, AssignMembershipRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Memberships are not used for ADMIN accounts");
        }
        MembershipPlan plan = membershipPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan", request.getPlanId()));
        if (!plan.isActive()) {
            throw new BadRequestException("Plan is inactive");
        }
        validatePlanAudience(plan, user.getRole());
        userMembershipRepository.findByUserAndStatus(user, UserMembershipStatus.ACTIVE)
                .ifPresent(um -> {
                    um.setStatus(UserMembershipStatus.CANCELLED);
                    userMembershipRepository.save(um);
                });
        UserMembership um = UserMembership.builder()
                .user(user)
                .plan(plan)
                .startedAt(Instant.now())
                .expiresAt(request.getExpiresAt())
                .status(UserMembershipStatus.ACTIVE)
                .build();
        return toMembershipRow(userMembershipRepository.save(um));
    }

    @Transactional
    public void cancelActiveMembership(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        UserMembership active = userMembershipRepository.findByUserAndStatus(user, UserMembershipStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("No active membership for this user"));
        active.setStatus(UserMembershipStatus.CANCELLED);
        userMembershipRepository.save(active);
    }

    public AdminDashboardSummaryResponse getDashboardSummary() {
        Instant weekAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        return AdminDashboardSummaryResponse.builder()
                .totalUsers(userRepository.count())
                .totalCustomers(userRepository.countByRole(Role.USER))
                .totalMahirs(userRepository.countByRole(Role.MAHIR))
                .totalAdmins(userRepository.countByRole(Role.ADMIN))
                .totalJobs(jobRepository.count())
                .openJobs(jobRepository.countByStatus(JobStatus.OPEN))
                .totalBookings(bookingRepository.count())
                .completedBookings(bookingRepository.countByStatus(BookingStatus.COMPLETED))
                .totalReviews(reviewRepository.count())
                .chatMessagesLast7Days(chatMessageRepository.countByCreatedAtAfter(weekAgo))
                .activeBanners(bannerRepository.countCurrentlyValid(Instant.now()))
                .build();
    }

    public AdminEngagementResponse getDashboardEngagement(LocalDate from, LocalDate to) {
        LocalDate end = to != null ? to : LocalDate.now(ZoneOffset.UTC);
        LocalDate start = from != null ? from : end.minusDays(13);
        if (start.isAfter(end)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }
        List<AdminEngagementResponse.DailyCount> chatByDay = new ArrayList<>();
        List<AdminEngagementResponse.DailyCount> regByDay = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            Instant dayStart = d.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant dayEnd = d.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            long msgs = chatMessageRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(dayStart, dayEnd);
            chatByDay.add(AdminEngagementResponse.DailyCount.builder()
                    .date(d.toString())
                    .count(msgs)
                    .build());
            LocalDateTime lStart = LocalDateTime.ofInstant(dayStart, ZoneOffset.UTC);
            LocalDateTime lEnd = LocalDateTime.ofInstant(dayEnd, ZoneOffset.UTC);
            long regs = userRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(lStart, lEnd);
            regByDay.add(AdminEngagementResponse.DailyCount.builder()
                    .date(d.toString())
                    .count(regs)
                    .build());
        }
        return AdminEngagementResponse.builder()
                .chatMessagesByDay(chatByDay)
                .newRegistrationsByDay(regByDay)
                .build();
    }

    @Transactional
    public UserResponse patchUser(Long userId, AdminPatchUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (user.getRole() == Role.ADMIN && Boolean.TRUE.equals(request.getBlocked())) {
            throw new BadRequestException("Cannot block an ADMIN account");
        }
        if (request.getBlocked() != null) {
            user.setBlocked(Boolean.TRUE.equals(request.getBlocked()));
            user.setBlockedAt(user.isBlocked() ? Instant.now() : null);
            if (!user.isBlocked()) {
                user.setBlockedReason(null);
            } else if (request.getBlockedReason() != null) {
                user.setBlockedReason(request.getBlockedReason());
            }
        } else if (request.getBlockedReason() != null && user.isBlocked()) {
            user.setBlockedReason(request.getBlockedReason());
        }
        if (request.getAccountStatus() != null) {
            if (user.getRole() == Role.ADMIN && request.getAccountStatus() != AccountStatus.ACTIVE) {
                throw new BadRequestException("Cannot change ADMIN account status away from ACTIVE");
            }
            user.setAccountStatus(request.getAccountStatus());
        }
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String email = request.getEmail().trim();
            if (!email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new BadRequestException("Email already in use: " + email);
            }
            user.setEmail(email);
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber().isBlank() ? null : request.getPhoneNumber().trim());
        }
        userRepository.save(user);
        return toAdminUserResponse(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Cannot delete an ADMIN account");
        }
        if (jobRepository.countByPostedBy(user) > 0
                || bookingRepository.countByCustomer(user) > 0
                || bookingRepository.countByMahir(user) > 0
                || bidRepository.countByMahir(user) > 0) {
            throw new BadRequestException("Cannot delete user with jobs, bids, or bookings");
        }
        try {
            userRepository.delete(user);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Cannot delete user: related data still exists");
        }
    }

    @Transactional
    public UserResponse createUserByAdmin(AdminCreateUserRequest request) {
        if (request.getRole() == Role.ADMIN) {
            throw new BadRequestException("Creating ADMIN via API is not allowed");
        }
        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }
        AccountType accountType = request.getRole() == Role.MAHIR
                ? AccountType.FREEMIUM
                : request.getAccountType();

        User user = User.builder()
                .role(request.getRole())
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth())
                .accountType(accountType)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
        if (request.getRole() == Role.MAHIR) {
            List<Category> categories = request.getServiceCategoryIds() == null || request.getServiceCategoryIds().isEmpty()
                    ? new ArrayList<>()
                    : categoryRepository.findAllById(request.getServiceCategoryIds());
            user.setServiceCategories(categories);
            user.setCustomServiceName(request.getCustomServiceName());
            user.setCredits(3);
        }
        user = userRepository.save(user);
        return toAdminUserResponse(user);
    }

    public AdminUserMembershipDetailResponse getUserMembershipAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        List<UserMembership> all = userMembershipRepository.findByUserOrderByCreatedAtDesc(user);
        UserMembership active = userMembershipRepository.findByUserAndStatus(user, UserMembershipStatus.ACTIVE)
                .orElse(null);
        return AdminUserMembershipDetailResponse.builder()
                .active(active != null ? toMembershipRow(active) : null)
                .history(all.stream().map(this::toMembershipRow).collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public AdminUserMembershipDetailResponse patchUserMembershipAdmin(Long userId, AdminPatchMembershipRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Memberships are not used for ADMIN accounts");
        }
        if (request.getPlanId() != null) {
            assignMembership(userId, AssignMembershipRequest.builder()
                    .planId(request.getPlanId())
                    .expiresAt(request.getExpiresAt())
                    .build());
        } else if (request.getExpiresAt() != null) {
            UserMembership active = userMembershipRepository.findByUserAndStatus(user, UserMembershipStatus.ACTIVE)
                    .orElseThrow(() -> new BadRequestException("No active membership for this user"));
            active.setExpiresAt(request.getExpiresAt());
            userMembershipRepository.save(active);
        }
        return getUserMembershipAdmin(userId);
    }

    @Transactional
    public ReviewResponse patchReview(Long reviewId, AdminReviewPatchRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));
        if (request.getHiddenFromPublic() != null) {
            review.setHiddenFromPublic(Boolean.TRUE.equals(request.getHiddenFromPublic()));
        }
        if (request.getRating() != null) {
            int r = request.getRating();
            if (r < 1 || r > 5) {
                throw new BadRequestException("Rating must be between 1 and 5");
            }
            review.setRating(r);
        }
        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }
        reviewRepository.save(review);
        return toAdminReviewResponse(review);
    }

    @Transactional
    public JobResponse patchJob(Long jobId, AdminJobPatchRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        if (request.getStatus() != null) {
            job.setStatus(request.getStatus());
        }
        jobRepository.save(job);
        return toJobResponse(job);
    }

    public AdminChatThreadResponse getSupportChatThread(Long threadId) {
        ChatThread thread = chatThreadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat thread", threadId));
        return toChatThreadResponse(thread);
    }

    private void validatePlanAudience(MembershipPlan plan, Role userRole) {
        PlanAudience a = plan.getAudience();
        if (a == PlanAudience.BOTH) {
            return;
        }
        if (a == PlanAudience.USER && userRole != Role.USER) {
            throw new BadRequestException("This plan is only for customers (USER)");
        }
        if (a == PlanAudience.MAHIR && userRole != Role.MAHIR) {
            throw new BadRequestException("This plan is only for Mahirs");
        }
    }

    private UserResponse toAdminUserResponse(User user) {
        Location loc = user.getLocation();
        LocationDto locationDto = loc == null ? null : LocationDto.builder()
                .streetAddress(loc.getStreetAddress())
                .latitude(loc.getLatitude())
                .longitude(loc.getLongitude())
                .build();
        return UserResponse.builder()
                .id(user.getId())
                .role(user.getRole())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth())
                .location(locationDto)
                .accountType(user.getAccountType())
                .serviceCategories(UserResponse.fromCategoryList(user.getServiceCategories()))
                .customServiceName(user.getCustomServiceName())
                .avatarUrl(user.getAvatarUrl())
                .profilePictureUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .credits(user.getCredits())
                .createdAt(user.getCreatedAt())
                .blocked(user.isBlocked())
                .blockedReason(user.getBlockedReason())
                .accountStatus(user.getAccountStatus())
                .build();
    }

    private JobResponse toJobResponse(Job job) {
        int bidCount = (int) bidRepository.countByJobId(job.getId());
        return JobResponse.builder()
                .id(job.getId())
                .postedById(job.getPostedBy().getId())
                .posterName(job.getPostedBy().getFullName())
                .posterEmail(job.getPostedBy().getEmail())
                .categoryId(job.getCategory().getId())
                .categoryName(job.getCategory().getName())
                .title(job.getTitle())
                .description(job.getDescription())
                .location(job.getLocation() == null ? null : LocationDto.builder()
                        .streetAddress(job.getLocation().getStreetAddress())
                        .latitude(job.getLocation().getLatitude())
                        .longitude(job.getLocation().getLongitude())
                        .build())
                .scheduledAt(job.getScheduledAt())
                .budgetMin(job.getBudgetMin())
                .budgetMax(job.getBudgetMax())
                .durationHours(job.getDurationHours())
                .status(job.getStatus())
                .bidCount(bidCount)
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    private ReviewResponse toAdminReviewResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .bookingId(r.getBooking().getId())
                .reviewerId(r.getReviewer().getId())
                .reviewerName(r.getReviewer().getFullName())
                .mahirId(r.getMahir().getId())
                .mahirName(r.getMahir().getFullName())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .hiddenFromPublic(r.isHiddenFromPublic())
                .build();
    }

    private AdminChatThreadResponse toChatThreadResponse(ChatThread t) {
        Booking b = t.getBooking();
        User c = b.getCustomer();
        User m = b.getMahir();
        Job j = b.getJob();
        return AdminChatThreadResponse.builder()
                .threadId(t.getId())
                .bookingId(b.getId())
                .jobId(j != null ? j.getId() : null)
                .jobTitle(j != null ? j.getTitle() : null)
                .customerId(c.getId())
                .customerName(c.getFullName())
                .customerEmail(c.getEmail())
                .mahirId(m.getId())
                .mahirName(m.getFullName())
                .mahirEmail(m.getEmail())
                .threadCreatedAt(t.getCreatedAt())
                .build();
    }

    private ChatMessageResponse toChatMessageResponse(com.example.demoapp.entity.ChatMessage msg) {
        return ChatMessageResponse.builder()
                .id(msg.getId())
                .threadId(msg.getThread().getId())
                .senderId(msg.getSender().getId())
                .senderName(msg.getSender().getFullName())
                .content(msg.getContent())
                .createdAt(msg.getCreatedAt())
                .readAt(msg.getReadAt())
                .build();
    }

    private FaqResponse toFaqResponse(Faq f) {
        return FaqResponse.builder()
                .id(f.getId())
                .question(f.getQuestion())
                .answer(f.getAnswer())
                .sortOrder(f.getSortOrder())
                .active(f.isActive())
                .build();
    }

    private MembershipPlanResponse toPlanResponse(MembershipPlan p) {
        return MembershipPlanResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .code(p.getCode())
                .description(p.getDescription())
                .audience(p.getAudience())
                .priceMonthly(p.getPriceMonthly())
                .currency(p.getCurrency())
                .featuresText(p.getFeaturesText())
                .active(p.isActive())
                .sortOrder(p.getSortOrder())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private AdminMembershipRowResponse toMembershipRow(UserMembership um) {
        User u = um.getUser();
        MembershipPlan p = um.getPlan();
        return AdminMembershipRowResponse.builder()
                .id(um.getId())
                .userId(u.getId())
                .userEmail(u.getEmail())
                .planId(p.getId())
                .planName(p.getName())
                .planCode(p.getCode())
                .status(um.getStatus())
                .startedAt(um.getStartedAt())
                .expiresAt(um.getExpiresAt())
                .build();
    }
}
