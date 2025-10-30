package com.reactorx.config;// File: com.reactorx.config.WebConfig.java

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173", "https://reactorx-frontend-live.onrender.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // ✅ FIX: Use correct casing for X-User-Email
                .allowedHeaders("Authorization", "Content-Type", "X-User-Email")
                .allowCredentials(true);
    }
}