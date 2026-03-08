package com.example.demoapp.service;

import com.example.demoapp.dto.CreateReviewRequest;
import com.example.demoapp.dto.ReviewResponse;
import com.example.demoapp.entity.Booking;
import com.example.demoapp.entity.BookingStatus;
import com.example.demoapp.entity.Review;
import com.example.demoapp.entity.Role;
import com.example.demoapp.entity.User;
import com.example.demoapp.exception.ResourceNotFoundException;
import com.example.demoapp.exception.UnauthorizedException;
import com.example.demoapp.repository.BookingRepository;
import com.example.demoapp.repository.ReviewRepository;
import com.example.demoapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponse create(Long userId, CreateReviewRequest request) {
        User reviewer = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (reviewer.getRole() != Role.USER) {
            throw new UnauthorizedException("Only customers (USER role) can leave reviews");
        }
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", request.getBookingId()));
        if (!booking.getCustomer().getId().equals(userId)) {
            throw new UnauthorizedException("Only the customer who made the booking can leave a review");
        }
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new UnauthorizedException("Can only review completed bookings");
        }
        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new UnauthorizedException("This booking has already been reviewed");
        }
        Review review = Review.builder()
                .booking(booking)
                .reviewer(reviewer)
                .mahir(booking.getMahir())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        review = reviewRepository.save(review);
        return toResponse(review);
    }

    public Page<ReviewResponse> getByMahirId(Long mahirId, Pageable pageable) {
        User mahir = userRepository.findById(mahirId)
                .orElseThrow(() -> new ResourceNotFoundException("Mahir", mahirId));
        if (mahir.getRole() != Role.MAHIR) {
            throw new ResourceNotFoundException("Mahir", mahirId);
        }
        return reviewRepository.findByMahirOrderByCreatedAtDesc(mahir, pageable)
                .map(this::toResponse);
    }

    private ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .bookingId(r.getBooking().getId())
                .reviewerId(r.getReviewer().getId())
                .reviewerName(r.getReviewer().getFullName())
                .mahirId(r.getMahir().getId())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
