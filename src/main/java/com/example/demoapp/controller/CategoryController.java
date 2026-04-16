package com.example.demoapp.controller;

import com.example.demoapp.catalog.ServiceCategoryCatalog;
import com.example.demoapp.dto.CategoryResponse;
import com.example.demoapp.entity.Category;
import com.example.demoapp.repository.CategoryRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Fixed 17 service categories (Mahir signup + Post job); names match mobile exactly")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    /**
     * Returns exactly the 17 canonical categories in app order (not alphabetical).
     * Omits any DB row whose name is not in {@link ServiceCategoryCatalog}.
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {
        List<CategoryResponse> list = new ArrayList<>(ServiceCategoryCatalog.CANONICAL_COUNT);
        for (String name : ServiceCategoryCatalog.NAMES_IN_DISPLAY_ORDER) {
            categoryRepository.findByName(name)
                    .ifPresent(c -> list.add(toResponse(c)));
        }
        return ResponseEntity.ok(list);
    }

    private CategoryResponse toResponse(Category c) {
        int idx = ServiceCategoryCatalog.NAMES_IN_DISPLAY_ORDER.indexOf(c.getName());
        Integer sortOrder = idx >= 0 ? idx + 1 : null;
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .sortOrder(sortOrder)
                .build();
    }
}
