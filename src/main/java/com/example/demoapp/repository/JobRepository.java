package com.example.demoapp.repository;

import com.example.demoapp.entity.Job;
import com.example.demoapp.entity.JobStatus;
import com.example.demoapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    Page<Job> findByPostedByOrderByCreatedAtDesc(User postedBy, Pageable pageable);

    Page<Job> findByPostedByAndStatusOrderByCreatedAtDesc(User postedBy, JobStatus status, Pageable pageable);

    Page<Job> findByStatusOrderByCreatedAtDesc(JobStatus status, Pageable pageable);

    Page<Job> findByStatusAndCategoryIdOrderByCreatedAtDesc(JobStatus status, Long categoryId, Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.status = :status AND (:categoryId IS NULL OR j.category.id = :categoryId) ORDER BY j.createdAt DESC")
    Page<Job> findOpenJobs(@Param("status") JobStatus status, @Param("categoryId") Long categoryId, Pageable pageable);
}
