package com.reactorx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableConfigurationProperties
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableTransactionManagement
public class ReactorXApplication {
    
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ReactorXApplication.class);
        
        // Enable lazy initialization for faster startup
        app.setLazyInitialization(true);
        
        // Set default profile if none specified
        app.setAdditionalProfiles("dev");
        
        app.run(args);
    }
    
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> java.util.Optional.of("system");
    }
}