package com.example.demoapp.config;

import com.example.demoapp.catalog.ServiceCategoryCatalog;
import com.example.demoapp.entity.Category;
import com.example.demoapp.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Ensures the 17 canonical categories exist with exact names and {@code sort_order} 1–17
 * (aligned with Flutter {@code AppServiceCategories.names}). Runs on every startup: upserts
 * missing rows and refreshes sort order / descriptions; does not delete extra DB rows.
 */
@Component
@Order(50)
@RequiredArgsConstructor
@Slf4j
public class CategoryDataLoader implements ApplicationRunner {

    private static final List<String> DESCRIPTIONS = List.of(
            "Home cleaning services",
            "Electrical repairs and installations",
            "Plumbing and pipe work",
            "Air conditioning and cooling",
            "Home appliance repair",
            "Woodwork and furniture",
            "Interior and exterior painting and renovation",
            "Pest control services",
            "Moving and relocation",
            "General handyman tasks",
            "Delivery and errands",
            "In-home cooking and chef services",
            "Cleaning services",
            "Tutoring and teaching",
            "Makeup and beauty",
            "Driving and chauffeur services",
            "Services that do not fit other categories"
    );

    private final CategoryRepository categoryRepository;

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<String> names = ServiceCategoryCatalog.NAMES_IN_DISPLAY_ORDER;
            if (DESCRIPTIONS.size() != names.size()) {
                throw new IllegalStateException("DESCRIPTIONS must match NAMES_IN_DISPLAY_ORDER size");
            }
            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i);
                int sortOrder = i + 1;
                String description = DESCRIPTIONS.get(i);
                categoryRepository.findByName(name).ifPresentOrElse(
                        existing -> {
                            existing.setSortOrder(sortOrder);
                            existing.setDescription(description);
                            categoryRepository.save(existing);
                        },
                        () -> categoryRepository.save(Category.builder()
                                .name(name)
                                .description(description)
                                .sortOrder(sortOrder)
                                .build())
                );
            }
            log.info("Synced {} canonical service categories (sort_order 1–{})", names.size(), names.size());
        } catch (Exception e) {
            log.warn("Could not sync default categories: {}", e.getMessage());
        }
    }
}
