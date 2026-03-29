package com.example.demoapp.repository;

import com.example.demoapp.entity.ChatMessage;
import com.example.demoapp.entity.ChatThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByThreadOrderByCreatedAtDesc(ChatThread thread, Pageable pageable);

    Page<ChatMessage> findByThreadOrderByCreatedAtAsc(ChatThread thread, Pageable pageable);
}
