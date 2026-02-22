package com.example.demoapp.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * When DATABASE_URL is set (e.g. on Railway), parses it and sets Spring datasource properties
 * so you don't need to configure URL/username/password separately.
 * Supports: postgresql://user:password@host:port/database
 */
public class RailwayDatabaseUrlProcessor implements EnvironmentPostProcessor {

    private static final String DATABASE_URL = "DATABASE_URL";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // Railway can expose DATABASE_URL, DATABASE_PRIVATE_URL, or POSTGRES_URL
        String databaseUrl = firstNonBlank(
                environment.getProperty("DATABASE_URL"),
                environment.getProperty("DATABASE_PRIVATE_URL"),
                environment.getProperty("DATABASE_PUBLIC_URL"),
                environment.getProperty("POSTGRES_URL")
        );
        boolean railwayProfile = isRailwayProfileActive(environment);
        if (databaseUrl == null || databaseUrl.isBlank()) {
            if (railwayProfile) {
                throw new IllegalStateException(
                    "Railway profile is active but DATABASE_URL is not set. "
                    + "In Railway Dashboard: open your APP service (not Postgres) → Variables → Add variable: "
                    + "Name = DATABASE_URL, Value = click 'Add reference' and select your Postgres service → DATABASE_URL. "
                    + "Then redeploy."
                );
            }
            return;
        }
        try {
            URI uri = URI.create(databaseUrl);
            String scheme = uri.getScheme();
            if (scheme == null || !scheme.startsWith("postgres")) {
                return;
            }
            String username = uri.getUserInfo() != null ? uri.getUserInfo().split(":")[0] : "";
            String password = uri.getUserInfo() != null && uri.getUserInfo().contains(":")
                    ? uri.getUserInfo().substring(uri.getUserInfo().indexOf(':') + 1) : "";
            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String path = uri.getPath();
            if (path == null || path.isEmpty()) path = "/demoapp";
            String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;
            String query = uri.getQuery();
            if (query != null && !query.isEmpty()) {
                jdbcUrl += "?" + query;
            } else if (databaseUrl.contains("sslmode=") || databaseUrl.contains("ssl=true")) {
                jdbcUrl += "?sslmode=require";
            }

            Map<String, Object> props = new HashMap<>();
            props.put("spring.datasource.url", jdbcUrl);
            props.put("spring.datasource.username", username);
            props.put("spring.datasource.password", password);
            props.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
            props.put("spring.jpa.database-platform", "org.hibernate.dialect.PostgreSQLDialect");

            environment.getPropertySources().addFirst(
                    new MapPropertySource("railwayDatabaseUrl", props)
            );
        } catch (Exception ignored) {
            // If parsing fails, skip so normal config is used
        }
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }

    private static boolean isRailwayProfileActive(ConfigurableEnvironment environment) {
        String active = environment.getProperty("spring.profiles.active");
        if (active != null && active.contains("railway")) return true;
        if (System.getenv("SPRING_PROFILES_ACTIVE") != null && System.getenv("SPRING_PROFILES_ACTIVE").contains("railway")) return true;
        return false;
    }
}
