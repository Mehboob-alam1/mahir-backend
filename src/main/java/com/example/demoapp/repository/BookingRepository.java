package com.example.demoapp.repository;

import com.example.demoapp.entity.Booking;
import com.example.demoapp.entity.BookingStatus;
import com.example.demoapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    long count();

    long countByStatus(BookingStatus status);

    long countByCustomer(User customer);

    long countByMahir(User mahir);

    Page<Booking> findByCustomerOrderByCreatedAtDesc(User customer, Pageable pageable);

    Page<Booking> findByMahirOrderByCreatedAtDesc(User mahir, Pageable pageable);

    Page<Booking> findByCustomerAndStatusOrderByCreatedAtDesc(User customer, BookingStatus status, Pageable pageable);

    Page<Booking> findByMahirAndStatusOrderByCreatedAtDesc(User mahir, BookingStatus status, Pageable pageable);

    Page<Booking> findByCustomerOrMahirOrderByCreatedAtDesc(User customer, User mahir, Pageable pageable);

    Optional<Booking> findByBid_Id(Long bidId);

    List<Booking> findByJob_IdAndStatus(Long jobId, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.customer.id = :customerId AND b.mahir.id = :mahirId AND b.job IS NULL AND b.status NOT IN ('CANCELLED', 'COMPLETED') ORDER BY b.createdAt DESC")
    List<Booking> findActiveDirectBookingsBetween(@Param("customerId") Long customerId, @Param("mahirId") Long mahirId, Pageable pageable);

    List<Booking> findByJob_Id(Long jobId);
}
