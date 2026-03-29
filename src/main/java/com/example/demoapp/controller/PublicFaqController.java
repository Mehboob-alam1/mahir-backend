package com.example.demoapp.controller;

import com.example.demoapp.dto.FaqResponse;
import com.example.demoapp.repository.FaqRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/faqs")
@RequiredArgsConstructor
@Tag(name = "FAQs", description = "Public FAQ list for the app")
public class PublicFaqController {

    private final FaqRepository faqRepository;

    @GetMapping
    public List<FaqResponse> listActive() {
        return faqRepository.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(f -> FaqResponse.builder()
                        .id(f.getId())
                        .question(f.getQuestion())
                        .answer(f.getAnswer())
                        .sortOrder(f.getSortOrder())
                        .build())
                .collect(Collectors.toList());
    }
}
