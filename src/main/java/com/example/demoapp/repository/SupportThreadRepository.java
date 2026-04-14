package com.example.demoapp.repository;

import com.example.demoapp.entity.SupportThread;
import com.example.demoapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportThreadRepository extends JpaRepository<SupportThread, Long> {

    Page<SupportThread> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    Page<SupportThread> findByUserOrderByUpdatedAtDesc(User user, Pageable pageable);
}
