package com.portfolio.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${CORS_ALLOWED_ORIGINS:${portfolio.cors.allowed-origins:}}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            return;
        }

        String[] origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);

        if (origins.length > 0) {
            registry.addMapping("/api/**")
                    .allowedOrigins(origins)
                    .allowedMethods("POST", "OPTIONS")
                    .allowedHeaders("Content-Type", "Accept", "Origin", "X-Requested-With")
                    .allowCredentials(false)
                    .maxAge(3600);
        }
    }
}
