package com.reactorx.config;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.context.annotation.Configuration;

@EnableJpaRepositories(basePackages = "com.reactorx.repository")
@EntityScan(basePackages = "com.reactorx.entity")
@EnableTransactionManagement
@Configuration
public class DatabaseConfig {
    // Let Spring Boot handle all database configuration automatically
}
