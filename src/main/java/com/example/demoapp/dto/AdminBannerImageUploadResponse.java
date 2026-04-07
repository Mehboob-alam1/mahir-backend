package com.example.demoapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response for {@code POST /api/admin/banners/upload}. Clients may read {@link #imageUrl} or {@link #url} (same value).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminBannerImageUploadResponse {

    /** Use this string as {@code imageUrl} when creating/updating a banner. */
    private String imageUrl;

    /** Alias of {@code imageUrl} for mobile clients. */
    private String url;

    /** Stored file name (under {@code /api/files/banners/{path}}). */
    private String path;
}
