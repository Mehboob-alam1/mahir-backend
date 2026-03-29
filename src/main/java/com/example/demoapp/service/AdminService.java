package com.example.demoapp.service;

import com.example.demoapp.dto.*;
import com.example.demoapp.entity.*;
import com.example.demoapp.exception.BadRequestException;
import com.example.demoapp.exception.ResourceNotFoundException;
import com.example.demoapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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

    public Page<UserResponse> listUsers(Role role, Boolean blocked, Pageable pageable) {
        Page<User> page;
        if (role != null && blocked != null) {
            page = userRepository.findByRoleAndBlocked(role, blocked, pageable);
        } else if (role != null) {
            page = userRepository.findByRole(role, pageable);
        } else if (Boolean.TRUE.equals(blocked)) {
            page = userRepository.findByBlockedTrue(pageable);
        } else if (Boolean.FALSE.equals(blocked)) {
            page = userRepository.findByBlockedFalse(pageable);
        } else {
            page = userRepository.findAll(pageable);
        }
        return page.map(this::toAdminUserResponse);
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
                .bio(user.getBio())
                .credits(user.getCredits())
                .createdAt(user.getCreatedAt())
                .blocked(user.isBlocked())
                .blockedReason(user.getBlockedReason())
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
