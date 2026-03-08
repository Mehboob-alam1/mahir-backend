package com.example.demoapp.repository;

import com.example.demoapp.entity.Booking;
import com.example.demoapp.entity.ChatThread;
import com.example.demoapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatThreadRepository extends JpaRepository<ChatThread, Long> {

    Optional<ChatThread> findByBookingId(Long bookingId);

    @Query("SELECT t FROM ChatThread t WHERE t.booking.customer.id = :userId OR t.booking.mahir.id = :userId ORDER BY t.createdAt DESC")
    List<ChatThread> findThreadsForUser(@Param("userId") Long userId);
}
