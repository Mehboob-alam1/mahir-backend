package com.example.demoapp.controller;

import com.example.demoapp.service.UserSettingsService;
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

/**
 * Serves uploaded profile pictures. Public GET so the app can load images via stable HTTPS URLs.
 */
@RestController
@RequestMapping("/api/files")
@Tag(name = "Files", description = "Public avatar URLs")
@RequiredArgsConstructor
public class AvatarFileController {

    private final UserSettingsService userSettingsService;

    @GetMapping("/avatars/{userId}")
    @Operation(summary = "Get user avatar file (JPEG, PNG, or WebP)")
    public ResponseEntity<Resource> getAvatar(@PathVariable Long userId) {
        Path root = userSettingsService.getAvatarRoot();
        Path file = null;
        String ext = null;
        for (String e : new String[] {"jpg", "png", "webp"}) {
            Path candidate = root.resolve(userId + "." + e);
            if (Files.isRegularFile(candidate)) {
                file = candidate;
                ext = e;
                break;
            }
        }
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = switch (ext) {
            case "jpg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            default -> MediaType.parseMediaType("image/webp");
        };
        FileSystemResource body = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .contentType(mediaType)
                .body(body);
    }
}
