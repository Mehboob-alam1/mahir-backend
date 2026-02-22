package com.example.demoapp.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

/**
 * When railway profile is active, create DataSource only from DATABASE_URL.
 * This ensures we never use the default MySQL config on Railway.
 */
@Configuration
@Profile("railway")
public class RailwayDataSourceConfig {

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Bean
    public DataSource dataSource() {
        if (databaseUrl == null || databaseUrl.isBlank()) {
            throw new IllegalStateException(
                "DATABASE_URL is not set. In Railway: open your APP service → Variables → "
                + "Add variable DATABASE_URL = reference to Postgres service DATABASE_URL (${{ Postgres.DATABASE_URL }}). "
                + "Then redeploy."
            );
        }
        try {
            URI uri = URI.create(databaseUrl);
            if (uri.getScheme() == null || !uri.getScheme().startsWith("postgres")) {
                throw new IllegalStateException("DATABASE_URL must be a PostgreSQL URL (postgresql://...). Got: " + (uri.getScheme() != null ? uri.getScheme() : "null"));
            }
            String username = uri.getUserInfo() != null ? uri.getUserInfo().split(":")[0] : "";
            String password = uri.getUserInfo() != null && uri.getUserInfo().contains(":")
                    ? uri.getUserInfo().substring(uri.getUserInfo().indexOf(':') + 1) : "";
            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String path = uri.getPath();
            if (path == null || path.isEmpty()) path = "/railway";
            String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;
            String query = uri.getQuery();
            if (query != null && !query.isEmpty()) {
                jdbcUrl += "?" + query;
            } else {
                // Internal host (e.g. postgres.railway.internal) often works without strict SSL
                boolean internal = host != null && host.contains("internal");
                jdbcUrl += "?" + (internal ? "sslmode=allow" : "sslmode=require");
            }

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("org.postgresql.Driver");
            return new HikariDataSource(config);
        } catch (Exception e) {
            if (e instanceof IllegalStateException) throw (IllegalStateException) e;
            throw new IllegalStateException("Invalid DATABASE_URL. Set it to your Postgres connection URL in Railway Variables.", e);
        }
    }
}
