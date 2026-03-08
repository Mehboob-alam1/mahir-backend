package com.example.demoapp.service;

import com.example.demoapp.dto.JobRequest;
import com.example.demoapp.dto.JobResponse;
import com.example.demoapp.dto.JobUpdateRequest;
import com.example.demoapp.dto.LocationDto;
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
    }

    private JobResponse toResponse(Job j) {
        Location loc = j.getLocation();
        LocationDto locDto = loc == null ? null : LocationDto.builder()
                .streetAddress(loc.getStreetAddress())
                .latitude(loc.getLatitude())
                .longitude(loc.getLongitude())
                .build();
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
                .createdAt(j.getCreatedAt())
                .updatedAt(j.getUpdatedAt())
                .build();
    }
}
