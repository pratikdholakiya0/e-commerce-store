package org.akashbag.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. Allowed Origins (Frontend URLs)
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));

        // 2. Allowed Methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 3. Allowed Headers (Authorization is crucial for JWT)
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // 4. Allow Credentials (if you ever use cookies/sessions)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply to all paths
        return source;
    }
}
