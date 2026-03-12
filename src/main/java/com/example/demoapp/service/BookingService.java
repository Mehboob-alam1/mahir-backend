package com.example.demoapp.service;

import com.example.demoapp.dto.BookingRequest;
import com.example.demoapp.dto.BookingResponse;
import com.example.demoapp.entity.*;
import com.example.demoapp.exception.ResourceNotFoundException;
import com.example.demoapp.exception.UnauthorizedException;
import com.example.demoapp.repository.BookingRepository;
import com.example.demoapp.repository.ChatThreadRepository;
import com.example.demoapp.repository.JobRepository;
import com.example.demoapp.repository.UserRepository;
import com.example.demoapp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ChatThreadRepository chatThreadRepository;
    private final JobRepository jobRepository;
    private final NotificationService notificationService;

    /** Called when user accepts a bid: creates booking and chat thread. */
    @Transactional
    public BookingResponse createFromAcceptedBid(Job job, Bid bid) {
        Booking booking = Booking.builder()
                .customer(job.getPostedBy())
                .mahir(bid.getMahir())
                .job(job)
                .bid(bid)
                .agreedPrice(bid.getProposedPrice())
                .status(BookingStatus.ACCEPTED)
                .scheduledAt(bid.getProposedAt() != null ? bid.getProposedAt() : job.getScheduledAt())
                .message(bid.getMessage())
                .build();
        booking = bookingRepository.save(booking);
        ChatThread thread = ChatThread.builder().booking(booking).build();
        thread = chatThreadRepository.save(thread);
        return toResponse(booking);
    }

    @Transactional
    public BookingResponse create(Long customerId, BookingRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", customerId));
        if (customer.getRole() != Role.USER) {
            throw new UnauthorizedException("Only customers (USER role) can create bookings");
        }
        User mahir = userRepository.findById(request.getMahirId())
                .orElseThrow(() -> new ResourceNotFoundException("Mahir", request.getMahirId()));
        if (mahir.getRole() != Role.MAHIR) {
            throw new UnauthorizedException("Target user is not a Mahir");
        }
        if (customer.getId().equals(mahir.getId())) {
            throw new UnauthorizedException("Cannot book yourself");
        }
        Booking booking = Booking.builder()
                .customer(customer)
                .mahir(mahir)
                .status(BookingStatus.PENDING)
                .scheduledAt(request.getScheduledAt())
                .message(request.getMessage())
                .build();
        booking = bookingRepository.save(booking);
        return toResponse(booking);
    }

    public Page<BookingResponse> getMyBookings(Long userId, BookingStatus statusFilter, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Page<Booking> page;
        if (statusFilter != null) {
            page = user.getRole() == Role.MAHIR
                    ? bookingRepository.findByMahirAndStatusOrderByCreatedAtDesc(user, statusFilter, pageable)
                    : bookingRepository.findByCustomerAndStatusOrderByCreatedAtDesc(user, statusFilter, pageable);
        } else {
            page = user.getRole() == Role.MAHIR
                    ? bookingRepository.findByMahirOrderByCreatedAtDesc(user, pageable)
                    : bookingRepository.findByCustomerOrderByCreatedAtDesc(user, pageable);
        }
        return page.map(this::toResponse);
    }

    @Transactional
    public BookingResponse cancel(Long bookingId, Long userId, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        ensureCanAccess(booking, userId);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new UnauthorizedException("Booking is already cancelled");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new UnauthorizedException("Cannot cancel a completed booking");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelReason(reason);
        booking = bookingRepository.save(booking);
        User other = booking.getCustomer().getId().equals(userId) ? booking.getMahir() : booking.getCustomer();
        notificationService.create(other.getId(), "BOOKING_CANCELLED", "Booking cancelled",
                "A booking was cancelled." + (reason != null && !reason.isBlank() ? " Reason: " + reason : ""), bookingId);
        return toResponse(booking);
    }

    public BookingResponse getById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        ensureCanAccess(booking, userId);
        return toResponse(booking);
    }

    @Transactional
    public BookingResponse updateStatus(Long bookingId, Long userId, BookingStatus status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        ensureCanAccess(booking, userId);
        User user = userRepository.findById(userId).orElseThrow();
        if (user.getRole() == Role.MAHIR) {
            if (!booking.getMahir().getId().equals(userId)) {
                throw new UnauthorizedException("Only the assigned Mahir can update status");
            }
            if (status != BookingStatus.CANCELLED) {
                throw new UnauthorizedException("Mahir can only cancel the booking");
            }
        } else {
            if (!booking.getCustomer().getId().equals(userId)) {
                throw new UnauthorizedException("Only the job poster (customer) can update this booking");
            }
            if (status != BookingStatus.REACHED && status != BookingStatus.IN_PROGRESS && status != BookingStatus.COMPLETED && status != BookingStatus.CANCELLED) {
                throw new UnauthorizedException("Customer can set REACHED, IN_PROGRESS, COMPLETED, or CANCELLED");
            }
        }
        booking.setStatus(status);
        booking = bookingRepository.save(booking);
        if (status == BookingStatus.COMPLETED && booking.getJob() != null) {
            Job job = booking.getJob();
            job.setStatus(JobStatus.COMPLETED);
            jobRepository.save(job);
        }
        return toResponse(booking);
    }

    private void ensureCanAccess(Booking booking, Long userId) {
        if (!booking.getCustomer().getId().equals(userId) && !booking.getMahir().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this booking");
        }
    }

    private BookingResponse toResponse(Booking b) {
        Long threadId = chatThreadRepository.findByBookingId(b.getId()).map(ChatThread::getId).orElse(null);
        return BookingResponse.builder()
                .id(b.getId())
                .jobId(b.getJob() != null ? b.getJob().getId() : null)
                .bidId(b.getBid() != null ? b.getBid().getId() : null)
                .customerId(b.getCustomer().getId())
                .customerName(b.getCustomer().getFullName())
                .customerEmail(b.getCustomer().getEmail())
                .mahirId(b.getMahir().getId())
                .mahirName(b.getMahir().getFullName())
                .mahirEmail(b.getMahir().getEmail())
                .jobTitle(b.getJob() != null ? b.getJob().getTitle() : null)
                .agreedPrice(b.getAgreedPrice())
                .status(b.getStatus())
                .scheduledAt(b.getScheduledAt())
                .message(b.getMessage())
                .cancelReason(b.getCancelReason())
                .chatThreadId(threadId)
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
