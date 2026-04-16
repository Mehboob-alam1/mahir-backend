package com.example.demoapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    /** Matches {@link com.example.demoapp.catalog.ServiceCategoryCatalog} display order (1–17). */
    private Integer sortOrder;
}
