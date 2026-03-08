package com.example.demoapp.repository;

import com.example.demoapp.entity.Review;
import com.example.demoapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByMahirOrderByCreatedAtDesc(User mahir, Pageable pageable);

    Optional<Review> findByBookingId(Long bookingId);

    boolean existsByBookingId(Long bookingId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.mahir.id = :mahirId")
    Double getAverageRatingByMahirId(@Param("mahirId") Long mahirId);

    long countByMahir(User mahir);
}
