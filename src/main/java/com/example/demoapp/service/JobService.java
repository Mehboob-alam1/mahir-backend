package com.example.demoapp.service;

import com.example.demoapp.dto.JobRequest;
import com.example.demoapp.dto.JobResponse;
import com.example.demoapp.dto.JobUpdateRequest;
import com.example.demoapp.dto.LocationDto;
import com.example.demoapp.dto.WhatsAppContactResponse;
import com.example.demoapp.entity.*;
import com.example.demoapp.exception.ResourceNotFoundException;
import com.example.demoapp.exception.UnauthorizedException;
import com.example.demoapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BidRepository bidRepository;
    private final NotificationService notificationService;

    @Transactional
    public JobResponse create(Long userId, JobRequest request) {
        User poster = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (poster.getRole() != Role.USER) {
            throw new UnauthorizedException("Only customers (USER) can post jobs");
        }
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
        Location loc = null;
        if (request.getLocation() != null) {
            loc = Location.builder()
                    .streetAddress(request.getLocation().getStreetAddress())
                    .latitude(request.getLocation().getLatitude())
                    .longitude(request.getLocation().getLongitude())
                    .build();
        }
        Job job = Job.builder()
                .postedBy(poster)
                .category(category)
                .title(request.getTitle())
                .description(request.getDescription())
                .location(loc)
                .scheduledAt(request.getScheduledAt())
                .budgetMin(request.getBudgetMin())
                .budgetMax(request.getBudgetMax())
                .durationHours(request.getDurationHours())
                .status(JobStatus.OPEN)
                .build();
        job = jobRepository.save(job);
        // Notify Mahirs in this category about the new job (limit 100 to avoid spam)
        org.springframework.data.domain.Page<User> mahirsInCategory = userRepository.findByRoleAndServiceCategoriesId(
                Role.MAHIR, job.getCategory().getId(), Pageable.ofSize(100));
        String title = "New job";
        String body = "A new job in " + job.getCategory().getName() + ": " + job.getTitle();
        for (User mahir : mahirsInCategory.getContent()) {
            if (!mahir.getId().equals(poster.getId())) {
                notificationService.create(mahir.getId(), "NEW_JOB", title, body, job.getId());
            }
        }
        return toResponse(job);
    }

    public Page<JobResponse> listMyJobs(Long userId, JobStatus status, Pageable pageable) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Page<Job> page = status != null
                ? jobRepository.findByPostedByAndStatusOrderByCreatedAtDesc(user, status, pageable)
                : jobRepository.findByPostedByOrderByCreatedAtDesc(user, pageable);
        return page.map(this::toResponse);
    }

    public Page<JobResponse> listOpenJobs(Long categoryId, Pageable pageable) {
        Page<Job> page = categoryId != null
                ? jobRepository.findByStatusAndCategoryIdOrderByCreatedAtDesc(JobStatus.OPEN, categoryId, pageable)
                : jobRepository.findByStatusOrderByCreatedAtDesc(JobStatus.OPEN, pageable);
        return page.map(this::toResponse);
    }

    public JobResponse getById(Long jobId, Long userId) {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        return toResponse(job);
    }

    @Transactional
    public JobResponse update(Long jobId, Long userId, JobUpdateRequest request) {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        if (!job.getPostedBy().getId().equals(userId)) {
            throw new UnauthorizedException("Only the job poster can update it");
        }
        if (job.getStatus() != JobStatus.OPEN) {
            throw new UnauthorizedException("Can only update jobs with status OPEN");
        }
        if (bidRepository.existsByJobIdAndStatus(jobId, BidStatus.ACCEPTED)) {
            throw new UnauthorizedException("Cannot update job after a bid has been accepted");
        }
        if (request.getTitle() != null) job.setTitle(request.getTitle());
        if (request.getDescription() != null) job.setDescription(request.getDescription());
        if (request.getLocation() != null) {
            job.setLocation(Location.builder()
                    .streetAddress(request.getLocation().getStreetAddress())
                    .latitude(request.getLocation().getLatitude())
                    .longitude(request.getLocation().getLongitude())
                    .build());
        }
        if (request.getScheduledAt() != null) job.setScheduledAt(request.getScheduledAt());
        if (request.getBudgetMin() != null) job.setBudgetMin(request.getBudgetMin());
        if (request.getBudgetMax() != null) job.setBudgetMax(request.getBudgetMax());
        if (request.getDurationHours() != null) job.setDurationHours(request.getDurationHours());
        job = jobRepository.save(job);
        return toResponse(job);
    }

    @Transactional
    public void cancel(Long jobId, Long userId) {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        if (!job.getPostedBy().getId().equals(userId)) {
            throw new UnauthorizedException("Only the job poster can cancel the job");
        }
        if (job.getStatus() != JobStatus.OPEN) {
            throw new UnauthorizedException("Can only cancel jobs with status OPEN");
        }
        job.setStatus(JobStatus.CANCELLED);
        jobRepository.save(job);
        for (Bid bid : bidRepository.findByJobOrderByCreatedAtDesc(job, Pageable.unpaged()).getContent()) {
            notificationService.create(bid.getMahir().getId(), "JOB_CANCELLED", "Job cancelled",
                    "The job you applied to has been cancelled.", jobId);
        }
    }

    /** Mahir uses 1 credit to get job poster's phone for WhatsApp contact. */
    @Transactional
    public WhatsAppContactResponse whatsappContact(Long jobId, Long mahirId) {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        User mahir = userRepository.findById(mahirId).orElseThrow(() -> new ResourceNotFoundException("User", mahirId));
        if (mahir.getRole() != Role.MAHIR) {
            throw new UnauthorizedException("Only Mahirs can use WhatsApp contact");
        }
        int credits = mahir.getCredits() == null ? 0 : mahir.getCredits();
        if (credits < 1) {
            throw new UnauthorizedException("No credits left. Use Apply to send a request instead.");
        }
        mahir.setCredits(credits - 1);
        userRepository.save(mahir);
        String posterPhone = job.getPostedBy().getPhoneNumber() != null ? job.getPostedBy().getPhoneNumber() : "";
        return WhatsAppContactResponse.builder()
                .posterPhoneNumber(posterPhone)
                .remainingCredits(mahir.getCredits())
                .build();
    }

    private JobResponse toResponse(Job j) {
        Location loc = j.getLocation();
        LocationDto locDto = loc == null ? null : LocationDto.builder()
                .streetAddress(loc.getStreetAddress())
                .latitude(loc.getLatitude())
                .longitude(loc.getLongitude())
                .build();
        int bidCount = (int) bidRepository.countByJobId(j.getId());
        return JobResponse.builder()
                .id(j.getId())
                .postedById(j.getPostedBy().getId())
                .posterName(j.getPostedBy().getFullName())
                .posterEmail(j.getPostedBy().getEmail())
                .categoryId(j.getCategory().getId())
                .categoryName(j.getCategory().getName())
                .title(j.getTitle())
                .description(j.getDescription())
                .location(locDto)
                .scheduledAt(j.getScheduledAt())
                .budgetMin(j.getBudgetMin())
                .budgetMax(j.getBudgetMax())
                .durationHours(j.getDurationHours())
                .status(j.getStatus())
                .bidCount(bidCount)
                .createdAt(j.getCreatedAt())
                .updatedAt(j.getUpdatedAt())
                .build();
    }
}
