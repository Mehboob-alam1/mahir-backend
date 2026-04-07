package com.example.demoapp.controller;

import com.example.demoapp.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Serves admin-uploaded banner images referenced by {@link com.example.demoapp.dto.BannerResponse#getImageUrl()}.
 */
@RestController
@RequestMapping("/api/files/banners")
@Tag(name = "Files", description = "Public banner image URLs")
@RequiredArgsConstructor
public class BannerFileController {

    private static final Pattern SAFE_FILE_NAME = Pattern.compile(
            "^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\.(jpg|png|webp)$",
            Pattern.CASE_INSENSITIVE);

    private final BannerService bannerService;

    @GetMapping("/{fileName}")
    @Operation(summary = "Get banner image file (JPEG, PNG, or WebP)")
    public ResponseEntity<Resource> getBannerImage(@PathVariable String fileName) {
        if (fileName == null || !SAFE_FILE_NAME.matcher(fileName).matches()) {
            return ResponseEntity.notFound().build();
        }
        Path root = bannerService.getBannerImageRoot();
        Path file = root.resolve(fileName).normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        MediaType mediaType;
        if (lower.endsWith(".jpg")) {
            mediaType = MediaType.IMAGE_JPEG;
        } else if (lower.endsWith(".png")) {
            mediaType = MediaType.IMAGE_PNG;
        } else {
            mediaType = MediaType.parseMediaType("image/webp");
        }
        FileSystemResource body = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .contentType(mediaType)
                .body(body);
    }
}
