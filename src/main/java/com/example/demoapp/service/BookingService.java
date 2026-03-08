package com.example.demoapp.service;

import com.example.demoapp.dto.BookingRequest;
import com.example.demoapp.dto.BookingResponse;
import com.example.demoapp.entity.Booking;
import com.example.demoapp.entity.BookingStatus;
import com.example.demoapp.entity.Role;
import com.example.demoapp.entity.User;
import com.example.demoapp.exception.ResourceNotFoundException;
import com.example.demoapp.exception.UnauthorizedException;
import com.example.demoapp.repository.BookingRepository;
import com.example.demoapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

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

    public Page<BookingResponse> getMyBookings(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Page<Booking> page = user.getRole() == Role.MAHIR
                ? bookingRepository.findByMahirOrderByCreatedAtDesc(user, pageable)
                : bookingRepository.findByCustomerOrderByCreatedAtDesc(user, pageable);
        return page.map(this::toResponse);
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
            if (status != BookingStatus.ACCEPTED && status != BookingStatus.REJECTED && status != BookingStatus.COMPLETED) {
                throw new UnauthorizedException("Mahir can set status to ACCEPTED, REJECTED, or COMPLETED");
            }
        } else {
            if (!booking.getCustomer().getId().equals(userId)) {
                throw new UnauthorizedException("Only the customer can update this booking");
            }
            if (status != BookingStatus.CANCELLED) {
                throw new UnauthorizedException("Customer can only cancel the booking");
            }
        }
        booking.setStatus(status);
        booking = bookingRepository.save(booking);
        return toResponse(booking);
    }

    private void ensureCanAccess(Booking booking, Long userId) {
        if (!booking.getCustomer().getId().equals(userId) && !booking.getMahir().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this booking");
        }
    }

    private BookingResponse toResponse(Booking b) {
        return BookingResponse.builder()
                .id(b.getId())
                .customerId(b.getCustomer().getId())
                .customerName(b.getCustomer().getFullName())
                .customerEmail(b.getCustomer().getEmail())
                .mahirId(b.getMahir().getId())
                .mahirName(b.getMahir().getFullName())
                .mahirEmail(b.getMahir().getEmail())
                .status(b.getStatus())
                .scheduledAt(b.getScheduledAt())
                .message(b.getMessage())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
