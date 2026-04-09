package com.reactorx.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer {

    private final DataSource dataSource;
    private final Environment environment;

    @Bean
    public ApplicationRunner databaseInitializerRunner() {
        return args -> {
            try {
                log.info("Checking database connection and table creation...");
                
                // Test database connection
                try (Connection connection = dataSource.getConnection()) {
                    DatabaseMetaData metaData = connection.getMetaData();
                    log.info("Connected to database: {}", metaData.getURL());
                    
                    // Check if tables exist
                    boolean categoriesExist = tableExists(connection, "CATEGORY");
                    boolean productsExist = tableExists(connection, "PRODUCT");
                    boolean usersExist = tableExists(connection, "USER");
                    
                    log.info("Table status - Categories: {}, Products: {}, Users: {}", 
                            categoriesExist, productsExist, usersExist);
                    
                    if (!categoriesExist || !productsExist || !usersExist) {
                        log.warn("Some tables are missing. This is normal for H2 create-drop mode.");
                        log.info("Tables will be created by Hibernate on first access.");
                    }
                }
                
                log.info("Database initialization check completed successfully.");
                
            } catch (Exception e) {
                log.error("Error during database initialization: {}", e.getMessage(), e);
                // Don't fail the application startup
            }
        };
    }
    
    private boolean tableExists(Connection connection, String tableName) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, tableName.toUpperCase(), null);
            boolean exists = tables.next();
            tables.close();
            return exists;
        } catch (Exception e) {
            log.debug("Error checking table {}: {}", tableName, e.getMessage());
            return false;
        }
    }
}
