package com.example.demoapp.config;

import com.example.demoapp.entity.Category;
import com.example.demoapp.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CategoryDataLoader implements ApplicationRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (categoryRepository.count() > 0) return;
            List<Category> defaultCategories = List.of(
                    Category.builder().name("Plumbing").description("Plumbing and pipe work").build(),
                    Category.builder().name("Electrical").description("Electrical repairs and installations").build(),
                    Category.builder().name("Cleaning").description("Home and office cleaning").build(),
                    Category.builder().name("Carpentry").description("Woodwork and furniture").build(),
                    Category.builder().name("Painting").description("Interior and exterior painting").build(),
                    Category.builder().name("AC & HVAC").description("Air conditioning and heating").build(),
                    Category.builder().name("Pest Control").description("Pest control services").build(),
                    Category.builder().name("Moving").description("Moving and relocation").build(),
                    Category.builder().name("Landscaping").description("Garden and landscaping").build(),
                    Category.builder().name("Other").description("Other home services").build()
            );
            categoryRepository.saveAll(defaultCategories);
            log.info("Loaded {} default service categories", defaultCategories.size());
        } catch (Exception e) {
            log.warn("Could not seed categories (table may not exist yet or already populated): {}", e.getMessage());
        }
    }
}
