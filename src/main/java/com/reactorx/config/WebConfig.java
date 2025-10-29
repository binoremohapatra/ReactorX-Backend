package com.reactorx.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // 👇 Add all your allowed frontend origins here
                .allowedOrigins(
                        "http://localhost:5173",            // local React (Vite)
                        "http://localhost:3000",            // alternate local React port
                        "https://reactorx.vercel.app",      // deployed frontend
                        "https://www.reactorx.vercel.app"   // optional www version
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
