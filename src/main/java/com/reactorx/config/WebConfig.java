package com.reactorx.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Apply to your API path
                // FIX: Add your deployment URL if necessary, keeping localhost for development
                .allowedOrigins("http://localhost:5173", "https://reactorx-frontend-live.onrender.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 🛑 CRITICAL FIX: Explicitly list all required headers
                .allowedHeaders("Authorization", "Content-Type", "X-User-Email")
                .allowCredentials(true);
    }
}