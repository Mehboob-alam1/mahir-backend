package com.example.demoapp.repository;

import com.example.demoapp.entity.SupportMessage;
import com.example.demoapp.entity.SupportThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {

    Page<SupportMessage> findByThreadOrderByCreatedAtAsc(SupportThread thread, Pageable pageable);
}
