package com.example.demoapp.catalog;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Fixed service category names for Find Mahir (Mahir signup chips + Post job dropdown).
 * Must match Flutter {@code AppServiceCategories.names} exactly (case + spelling).
 */
public final class ServiceCategoryCatalog {

    private ServiceCategoryCatalog() {
    }

    /**
     * Display order: same as mobile app list (1 = first chip / first dropdown item).
     */
    public static final List<String> NAMES_IN_DISPLAY_ORDER = List.of(
            "Home Cleaning",
            "Electrician",
            "Plumbing",
            "AC Services",
            "Appliance Repair",
            "Carpentry",
            "Painting & Renovation",
            "Pest Control",
            "Home Shifting",
            "Handyman Services",
            "Delivery Boy",
            "Home Cheff",
            "Cleaner",
            "Teachers",
            "Makeup Artist",
            "Driver",
            "Other"
    );

    public static final int CANONICAL_COUNT = NAMES_IN_DISPLAY_ORDER.size();

    private static final Set<String> NAME_SET = Set.copyOf(NAMES_IN_DISPLAY_ORDER);

    public static boolean isCanonicalName(String name) {
        return name != null && NAME_SET.contains(name);
    }

    public static List<String> namesUnmodifiable() {
        return Collections.unmodifiableList(NAMES_IN_DISPLAY_ORDER);
    }
}
