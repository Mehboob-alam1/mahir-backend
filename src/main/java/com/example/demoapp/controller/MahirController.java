package com.example.demoapp.controller;

import com.example.demoapp.dto.MahirResponse;
import com.example.demoapp.service.MahirSearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mahirs")
@Tag(name = "Mahirs", description = "Search and get Mahirs (professionals)")
@RequiredArgsConstructor
public class MahirController {

    private final MahirSearchService mahirSearchService;

    @GetMapping
    public ResponseEntity<Page<MahirResponse>> searchMahirs(
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<MahirResponse> page = mahirSearchService.searchMahirs(categoryId, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MahirResponse> getMahirById(@PathVariable Long id) {
        MahirResponse mahir = mahirSearchService.getMahirById(id);
        return ResponseEntity.ok(mahir);
    }
}
