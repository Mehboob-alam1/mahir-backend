package com.example.demoapp.config;

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
 * Seeds canonical service categories aligned with Flutter {@code AppServiceCategories.names}
 * (exact spelling, including "Home Cheff" and "Other").
 */
@Component
@Order(100)
@RequiredArgsConstructor
@Slf4j
public class CategoryDataLoader implements ApplicationRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (categoryRepository.count() > 0) return;
            List<Category> defaultCategories = List.of(
                    cat("Home Cleaning", "Home cleaning services"),
                    cat("Electrician", "Electrical repairs and installations"),
                    cat("Plumbing", "Plumbing and pipe work"),
                    cat("AC Services", "Air conditioning and cooling"),
                    cat("Appliance Repair", "Home appliance repair"),
                    cat("Carpentry", "Woodwork and furniture"),
                    cat("Painting & Renovation", "Interior and exterior painting and renovation"),
                    cat("Pest Control", "Pest control services"),
                    cat("Home Shifting", "Moving and relocation"),
                    cat("Handyman Services", "General handyman tasks"),
                    cat("Delivery Boy", "Delivery and errands"),
                    cat("Home Cheff", "In-home cooking and chef services"),
                    cat("Cleaner", "Cleaning services"),
                    cat("Teachers", "Tutoring and teaching"),
                    cat("Makeup Artist", "Makeup and beauty"),
                    cat("Driver", "Driving and chauffeur services"),
                    cat("Other", "Services that do not fit other categories")
            );
            categoryRepository.saveAll(defaultCategories);
            log.info("Loaded {} canonical service categories", defaultCategories.size());
        } catch (Exception e) {
            log.warn("Could not load default categories: {}", e.getMessage());
        }
    }

    private static Category cat(String name, String description) {
        return Category.builder().name(name).description(description).build();
    }
}
