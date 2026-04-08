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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ChatThreadRepository chatThreadRepository;
    private final JobRepository jobRepository;
    private final NotificationService notificationService;

    /** Called when user accepts a bid: creates booking and chat thread (legacy bids without prior thread). */
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

    /**
     * When a Mahir places a bid, create a PENDING booking and chat thread so poster and Mahir can message before accept.
     * Idempotent if a booking for this bid already exists.
     */
    @Transactional
    public BookingResponse createFromBid(Bid bid) {
        Optional<Booking> existing = bookingRepository.findByBid_Id(bid.getId());
        if (existing.isPresent()) {
            Booking b = existing.get();
            chatThreadRepository.findByBookingId(b.getId()).orElseGet(() ->
                    chatThreadRepository.save(ChatThread.builder().booking(b).build()));
            return toResponse(b);
        }
        Job job = bid.getJob();
        Booking booking = Booking.builder()
                .customer(job.getPostedBy())
                .mahir(bid.getMahir())
                .job(job)
                .bid(bid)
                .agreedPrice(bid.getProposedPrice())
                .status(BookingStatus.PENDING)
                .scheduledAt(bid.getProposedAt() != null ? bid.getProposedAt() : job.getScheduledAt())
                .message(bid.getMessage())
                .build();
        booking = bookingRepository.save(booking);
        chatThreadRepository.save(ChatThread.builder().booking(booking).build());
        return toResponse(booking);
    }

    /**
     * After bid accept: upgrade existing PENDING bid booking to ACCEPTED, or create booking if missing (old data).
     */
    @Transactional
    public BookingResponse finalizeAcceptedBid(Job job, Bid bid) {
        Optional<Booking> opt = bookingRepository.findByBid_Id(bid.getId());
        if (opt.isEmpty()) {
            return createFromAcceptedBid(job, bid);
        }
        Booking booking = opt.get();
        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setAgreedPrice(bid.getProposedPrice());
        booking.setScheduledAt(bid.getProposedAt() != null ? bid.getProposedAt() : job.getScheduledAt());
        booking.setMessage(bid.getMessage());
        Booking saved = bookingRepository.save(booking);
        chatThreadRepository.findByBookingId(saved.getId()).orElseGet(() ->
                chatThreadRepository.save(ChatThread.builder().booking(saved).build()));
        return toResponse(saved);
    }

    @Transactional
    public void cancelBookingForRejectedBid(Bid bid) {
        bookingRepository.findByBid_Id(bid.getId()).ifPresent(booking -> {
            if (booking.getStatus() == BookingStatus.PENDING) {
                booking.setStatus(BookingStatus.CANCELLED);
                booking.setCancelReason("Bid was rejected");
                bookingRepository.save(booking);
            }
        });
    }

    /** When one bid is accepted, close other Mahirs' PENDING inquiry bookings on the same job. */
    @Transactional
    public void cancelPendingJobBookingsExceptBid(Job job, Long acceptedBidId) {
        List<Booking> pending = bookingRepository.findByJob_IdAndStatus(job.getId(), BookingStatus.PENDING);
        for (Booking b : pending) {
            if (b.getBid() == null || !b.getBid().getId().equals(acceptedBidId)) {
                b.setStatus(BookingStatus.CANCELLED);
                b.setCancelReason("Another bid was accepted for this job");
                bookingRepository.save(b);
            }
        }
    }

    /**
     * Customer (USER) invites a Mahir from profile: notification + booking without job + chat thread.
     * Reuses an existing non-terminal direct (no-job) booking between the same pair when present.
     */
    @Transactional
    public BookingResponse createDirectHireInvite(Long customerUserId, Long mahirUserId, String message) {
        User customer = userRepository.findById(customerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", customerUserId));
        if (customer.getRole() != Role.USER) {
            throw new UnauthorizedException("Only customers can send a hire invitation");
        }
        User mahir = userRepository.findById(mahirUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", mahirUserId));
        if (mahir.getRole() != Role.MAHIR) {
            throw new UnauthorizedException("Hire invitation target must be a Mahir");
        }
        if (mahir.isBlocked()) {
            throw new UnauthorizedException("This Mahir profile is not available");
        }
        if (customer.getId().equals(mahir.getId())) {
            throw new UnauthorizedException("Cannot invite yourself");
        }

        List<Booking> active = bookingRepository.findActiveDirectBookingsBetween(
                customer.getId(), mahir.getId(), PageRequest.of(0, 1));
        if (!active.isEmpty()) {
            Booking b = active.get(0);
            chatThreadRepository.findByBookingId(b.getId()).orElseGet(() ->
                    chatThreadRepository.save(ChatThread.builder().booking(b).build()));
            return toResponse(b);
        }

        String note = message != null && !message.isBlank() ? message.trim() : null;
        Booking booking = Booking.builder()
                .customer(customer)
                .mahir(mahir)
                .job(null)
                .bid(null)
                .agreedPrice(null)
                .status(BookingStatus.ACCEPTED)
                .message(note)
                .build();
        booking = bookingRepository.save(booking);
        chatThreadRepository.save(ChatThread.builder().booking(booking).build());
        notificationService.create(mahir.getId(), "HIRE_INVITE", "Hire invitation",
                customer.getFullName() + " wants to hire you. Open chat to connect.", booking.getId());
        return toResponse(booking);
    }

    public Long getChatThreadIdForBidId(Long bidId) {
        return bookingRepository.findByBid_Id(bidId)
                .flatMap(b -> chatThreadRepository.findByBookingId(b.getId()))
                .map(ChatThread::getId)
                .orElse(null);
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
                "The booking was cancelled." + (reason != null && !reason.isBlank() ? " Reason: " + reason : ""), bookingId);
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
        if (booking.getBid() != null && booking.getBid().getStatus() == BidStatus.PENDING
                && booking.getStatus() == BookingStatus.PENDING && status != BookingStatus.CANCELLED) {
            throw new UnauthorizedException("This chat is for a pending application. Accept the bid before updating job progress, or cancel to close.");
        }
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
        if (status == BookingStatus.REACHED) {
            notificationService.create(booking.getMahir().getId(), "BOOKING_STATUS_REACHED", "Customer marked you as reached",
                    "The customer marked that you have reached.", bookingId);
        } else if (status == BookingStatus.IN_PROGRESS) {
            notificationService.create(booking.getMahir().getId(), "BOOKING_STATUS_IN_PROGRESS", "Job in progress",
                    "The customer marked the job as in progress.", bookingId);
        } else if (status == BookingStatus.COMPLETED) {
            notificationService.create(booking.getMahir().getId(), "BOOKING_COMPLETED", "Job completed",
                    "The customer marked the job as completed. You may receive a review.", bookingId);
            notificationService.create(booking.getCustomer().getId(), "JOB_COMPLETED", "Job completed",
                    "Your job has been marked completed.", bookingId);
        }
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
