package com.example.demoapp.repository;

import com.example.demoapp.entity.Bid;
import com.example.demoapp.entity.BidStatus;
import com.example.demoapp.entity.Job;
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
public interface BidRepository extends JpaRepository<Bid, Long> {

    @Query("SELECT COUNT(b) FROM Bid b WHERE b.job.id = :jobId")
    long countByJobId(@Param("jobId") Long jobId);

    long countByMahir(User mahir);

    Page<Bid> findByJobOrderByCreatedAtDesc(Job job, Pageable pageable);

    Page<Bid> findByJobAndStatusOrderByCreatedAtDesc(Job job, BidStatus status, Pageable pageable);

    Page<Bid> findByMahirOrderByCreatedAtDesc(User mahir, Pageable pageable);

    Page<Bid> findByMahirAndStatusOrderByCreatedAtDesc(User mahir, BidStatus status, Pageable pageable);

    Optional<Bid> findByJobIdAndMahirId(Long jobId, Long mahirId);

    List<Bid> findByJob_Id(Long jobId);

    Page<Bid> findByJobAndMahirOrderByCreatedAtDesc(Job job, User mahir, Pageable pageable);

    boolean existsByJobIdAndMahirId(Long jobId, Long mahirId);

    boolean existsByJobIdAndStatus(Long jobId, BidStatus status);
}
