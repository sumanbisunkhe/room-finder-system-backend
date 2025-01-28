package com.roomfinder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Allow specific origins (frontend URLs)
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:5173")); // Add more origins if needed

        // Allow credentials (cookies, authorization headers)
        config.setAllowCredentials(true);

        // Allow specific headers
        config.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Accept", "X-Requested-With", "Cache-Control"
        ));

        // Expose specific headers to the frontend
        config.setExposedHeaders(List.of(
                "Authorization", "Content-Disposition"
        ));

        // Allow specific HTTP methods
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // Apply this configuration to all endpoints
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}