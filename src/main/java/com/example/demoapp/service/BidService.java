package com.example.demoapp.service;

import com.example.demoapp.dto.BidRequest;
import com.example.demoapp.dto.BidResponse;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final BookingService bookingService;
    private final NotificationService notificationService;

    @Transactional
    public BidResponse create(Long jobId, Long mahirId, BidRequest request) {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        if (job.getStatus() != JobStatus.OPEN) {
            throw new UnauthorizedException("Job is not open for bids");
        }
        User mahir = userRepository.findById(mahirId).orElseThrow(() -> new ResourceNotFoundException("User", mahirId));
        if (mahir.getRole() != Role.MAHIR) {
            throw new UnauthorizedException("Only Mahirs can place bids");
        }
        if (bidRepository.existsByJobIdAndMahirId(jobId, mahirId)) {
            throw new UnauthorizedException("You have already bid on this job");
        }
        java.math.BigDecimal price = request.getProposedPrice() != null ? request.getProposedPrice() : java.math.BigDecimal.ZERO;
        Bid bid = Bid.builder()
                .job(job)
                .mahir(mahir)
                .message(request.getMessage())
                .proposedPrice(price)
                .proposedAt(request.getProposedAt())
                .estimatedDurationHours(request.getEstimatedDurationHours())
                .status(BidStatus.PENDING)
                .build();
        bid = bidRepository.save(bid);
        notificationService.create(job.getPostedBy().getId(), "BID_RECEIVED", "New bid",
                "You have a new bid on your job. Tap to view.", job.getId());
        return toBidResponse(bid);
    }

    public Page<BidResponse> listBidsForJob(Long jobId, Long userId, Pageable pageable) {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (job.getPostedBy().getId().equals(userId)) {
            return bidRepository.findByJobOrderByCreatedAtDesc(job, pageable).map(this::toBidResponse);
        }
        if (user.getRole() == Role.MAHIR) {
            return bidRepository.findByJobAndMahirOrderByCreatedAtDesc(job, user, pageable).map(this::toBidResponse);
        }
        throw new UnauthorizedException("Only the job poster or a bidding Mahir can list bids for this job");
    }

    public Page<BidResponse> listMyBids(Long mahirId, BidStatus status, Pageable pageable) {
        User mahir = userRepository.findById(mahirId).orElseThrow(() -> new ResourceNotFoundException("User", mahirId));
        if (mahir.getRole() != Role.MAHIR) {
            throw new UnauthorizedException("Only Mahirs can list their bids");
        }
        Page<Bid> page = status != null
                ? bidRepository.findByMahirAndStatusOrderByCreatedAtDesc(mahir, status, pageable)
                : bidRepository.findByMahirOrderByCreatedAtDesc(mahir, pageable);
        return page.map(this::toBidResponse);
    }

    @Transactional
    public com.example.demoapp.dto.BookingResponse acceptBid(Long jobId, Long bidId, Long userId) {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        if (!job.getPostedBy().getId().equals(userId)) {
            throw new UnauthorizedException("Only the job poster can accept a bid");
        }
        if (job.getStatus() != JobStatus.OPEN) {
            throw new UnauthorizedException("Job is not open for accepting bids");
        }
        Bid bid = bidRepository.findById(bidId).orElseThrow(() -> new ResourceNotFoundException("Bid", bidId));
        if (!bid.getJob().getId().equals(jobId)) {
            throw new UnauthorizedException("Bid does not belong to this job");
        }
        if (bid.getStatus() != BidStatus.PENDING) {
            throw new UnauthorizedException("Bid is not pending");
        }
        bid.setStatus(BidStatus.ACCEPTED);
        bidRepository.save(bid);
        List<Bid> others = bidRepository.findByJobOrderByCreatedAtDesc(job, Pageable.unpaged()).getContent();
        for (Bid b : others) {
            if (!b.getId().equals(bidId) && b.getStatus() == BidStatus.PENDING) {
                b.setStatus(BidStatus.REJECTED);
                bidRepository.save(b);
                notificationService.create(b.getMahir().getId(), "BID_REJECTED", "Bid not accepted",
                        "Your bid was not accepted for this job.", jobId);
            }
        }
        job.setStatus(JobStatus.ASSIGNED);
        jobRepository.save(job);
        com.example.demoapp.dto.BookingResponse booking = bookingService.createFromAcceptedBid(job, bid);
        notificationService.create(bid.getMahir().getId(), "BID_ACCEPTED", "Bid accepted",
                "Your bid was accepted. Tap to view the booking.", booking.getId());
        notificationService.create(job.getPostedBy().getId(), "BOOKING_CONFIRMED", "Booking confirmed",
                "You're booked with " + bid.getMahir().getFullName() + ". Tap to view.", booking.getId());
        return booking;
    }

    @Transactional
    public void rejectBid(Long jobId, Long bidId, Long userId) {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job", jobId));
        if (!job.getPostedBy().getId().equals(userId)) {
            throw new UnauthorizedException("Only the job poster can reject a bid");
        }
        Bid bid = bidRepository.findById(bidId).orElseThrow(() -> new ResourceNotFoundException("Bid", bidId));
        if (!bid.getJob().getId().equals(jobId) || bid.getStatus() != BidStatus.PENDING) {
            throw new UnauthorizedException("Invalid bid");
        }
        bid.setStatus(BidStatus.REJECTED);
        bidRepository.save(bid);
        notificationService.create(bid.getMahir().getId(), "BID_REJECTED", "Bid not accepted",
                "Your bid was not accepted for this job.", jobId);
    }

    private BidResponse toBidResponse(Bid b) {
        Double rating = reviewRepository.getAverageRatingByMahirId(b.getMahir().getId());
        long reviewCount = reviewRepository.countByMahir(b.getMahir());
        return BidResponse.builder()
                .id(b.getId())
                .jobId(b.getJob().getId())
                .mahirId(b.getMahir().getId())
                .mahirName(b.getMahir().getFullName())
                .mahirAvatarUrl(b.getMahir().getAvatarUrl())
                .mahirRating(rating != null ? Math.round(rating * 10.0) / 10.0 : null)
                .mahirReviewCount(reviewCount)
                .message(b.getMessage())
                .proposedPrice(b.getProposedPrice())
                .proposedAt(b.getProposedAt())
                .estimatedDurationHours(b.getEstimatedDurationHours())
                .status(b.getStatus())
                .createdAt(b.getCreatedAt())
                .build();
    }
}
