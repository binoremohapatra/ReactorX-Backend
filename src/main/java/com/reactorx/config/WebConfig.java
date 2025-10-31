package com.reactorx.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // 🟢 CRITICAL FIX: Add the Vercel Domain to WebMvcConfigurer
                .allowedOrigins("http://localhost:5173",
                        "https://reactor-x-frontend-fhsz.vercel.app") // <=== NEW VERCEL DOMAIN
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // ✅ Ensure X-User-Email is allowed here as well
                .allowedHeaders("Authorization", "Content-Type", "X-User-Email")
                .allowCredentials(true);
    }
}