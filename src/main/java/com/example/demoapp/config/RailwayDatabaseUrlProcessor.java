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
        String databaseUrl = environment.getProperty(DATABASE_URL);
        if (databaseUrl == null || databaseUrl.isBlank()) {
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
}
