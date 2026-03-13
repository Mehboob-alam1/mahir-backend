package com.example.demoapp.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Initializes Firebase Admin SDK for FCM push. Supports:
 * 1. app.firebase.service-account-json (or env APP_FIREBASE_SERVICE_ACCOUNT_JSON) – raw JSON string (works everywhere, e.g. Railway secret).
 * 2. app.firebase.service-account-json-base64 (or env APP_FIREBASE_SERVICE_ACCOUNT_JSON_BASE64) – base64-encoded JSON (single line).
 * 3. app.firebase.service-account-path – path to JSON file (e.g. local).
 * If none set, push is skipped (app still runs).
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${app.firebase.service-account-json:}")
    private String serviceAccountJson;

    @Value("${app.firebase.service-account-json-base64:}")
    private String serviceAccountJsonBase64;

    @Value("${app.firebase.service-account-path:./firebase-service-account.json}")
    private String serviceAccountPath;

    @PostConstruct
    public void init() {
        if (FirebaseApp.getApps().isEmpty() == false) return;

        String envJson = System.getenv("APP_FIREBASE_SERVICE_ACCOUNT_JSON");
        if (envJson != null && !envJson.isBlank()) {
            log.info("Firebase: APP_FIREBASE_SERVICE_ACCOUNT_JSON is set (length={})", envJson.length());
        } else {
            log.info("Firebase: APP_FIREBASE_SERVICE_ACCOUNT_JSON is not set or empty. Set it in Railway Variables (full JSON).");
        }

        InputStream stream = null;
        String source = null;

        // Prefer property, then env var (Railway: APP_FIREBASE_SERVICE_ACCOUNT_JSON)
        String json = (serviceAccountJson != null && !serviceAccountJson.isBlank())
                ? serviceAccountJson
                : envJson;
        if (json != null && !json.isBlank()) {
            stream = new ByteArrayInputStream(json.trim().getBytes(StandardCharsets.UTF_8));
            source = "APP_FIREBASE_SERVICE_ACCOUNT_JSON";
        } else {
            String base64 = (serviceAccountJsonBase64 != null && !serviceAccountJsonBase64.isBlank())
                    ? serviceAccountJsonBase64
                    : System.getenv("APP_FIREBASE_SERVICE_ACCOUNT_JSON_BASE64");
            if (base64 != null && !base64.isBlank()) {
                try {
                    byte[] decoded = Base64.getDecoder().decode(base64.trim());
                    stream = new ByteArrayInputStream(decoded);
                    source = "APP_FIREBASE_SERVICE_ACCOUNT_JSON_BASE64";
                } catch (IllegalArgumentException e) {
                    log.warn("Firebase: invalid base64 in APP_FIREBASE_SERVICE_ACCOUNT_JSON_BASE64. Push disabled.");
                    return;
                }
            }
        }
        if (stream == null && serviceAccountPath != null && !serviceAccountPath.isBlank()) {
            try {
                stream = new FileInputStream(serviceAccountPath.trim());
                source = "app.firebase.service-account-path";
            } catch (Exception e) {
                log.warn("Firebase: could not open file {}. Trying classpath.", serviceAccountPath);
                stream = null;
            }
        }
        // Fallback: load from classpath (file packaged in JAR – works on Railway)
        if (stream == null) {
            InputStream classpath = FirebaseConfig.class.getResourceAsStream("/firebase-service-account.json");
            if (classpath != null) {
                stream = classpath;
                source = "classpath:/firebase-service-account.json";
            }
        }

        if (stream == null) {
            log.info("Firebase: no credentials set (json, json-base64, or path). Push notifications disabled.");
            return;
        }

        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(stream))
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase initialized for FCM push (source: {}).", source);
        } catch (Exception e) {
            log.warn("Firebase initialization failed. Push notifications disabled: {}", e.getMessage());
        }
    }
}
