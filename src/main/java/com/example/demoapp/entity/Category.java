package com.example.demoapp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    /** 1–17 for canonical app categories; higher values sort after (legacy / admin-only rows). */
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 999;
}
