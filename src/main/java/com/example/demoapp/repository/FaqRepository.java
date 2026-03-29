package com.example.demoapp.repository;

import com.example.demoapp.entity.Faq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {

    List<Faq> findByActiveTrueOrderBySortOrderAsc();

    Page<Faq> findAllByOrderBySortOrderAsc(Pageable pageable);
}
