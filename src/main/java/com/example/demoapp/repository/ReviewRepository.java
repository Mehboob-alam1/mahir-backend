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

    long count();

    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Review> findByMahirOrderByCreatedAtDesc(User mahir, Pageable pageable);

    Page<Review> findByMahirAndHiddenFromPublicFalseOrderByCreatedAtDesc(User mahir, Pageable pageable);

    Optional<Review> findByBookingId(Long bookingId);

    boolean existsByBookingId(Long bookingId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.mahir.id = :mahirId AND r.hiddenFromPublic = false")
    Double getAverageRatingByMahirId(@Param("mahirId") Long mahirId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.mahir.id = :mahirId AND r.hiddenFromPublic = false")
    long countPublicByMahirId(@Param("mahirId") Long mahirId);

    long countByMahir(User mahir);

    long countByReviewer(User reviewer);
}
