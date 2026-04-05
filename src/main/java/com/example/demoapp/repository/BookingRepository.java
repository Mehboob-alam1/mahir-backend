package com.example.demoapp.repository;

import com.example.demoapp.entity.Booking;
import com.example.demoapp.entity.BookingStatus;
import com.example.demoapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
