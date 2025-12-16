package com.useractivity.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Health Check", description = "Database and application health check endpoints")
public class DatabaseHealthController {

    private final DataSource dataSource;

    @GetMapping("/db")
    @Operation(summary = "Test database connectivity", description = "Checks if the application can connect to the database")
    public ResponseEntity<Map<String, Object>> testDatabaseConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            if (connection != null && !connection.isClosed()) {
                DatabaseMetaData metaData = connection.getMetaData();
                
                response.put("status", "SUCCESS");
                response.put("connected", true);
                response.put("databaseProductName", metaData.getDatabaseProductName());
                response.put("databaseProductVersion", metaData.getDatabaseProductVersion());
                response.put("driverName", metaData.getDriverName());
                response.put("driverVersion", metaData.getDriverVersion());
                response.put("url", metaData.getURL());
                response.put("username", metaData.getUserName());
                response.put("catalogName", connection.getCatalog());
                
                log.info("Database connection test successful: {}", metaData.getURL());
                
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "FAILED");
                response.put("connected", false);
                response.put("message", "Connection is null or closed");
                return ResponseEntity.status(500).body(response);
            }
        } catch (SQLException e) {
            log.error("Database connection test failed", e);
            response.put("status", "FAILED");
            response.put("connected", false);
            response.put("error", e.getMessage());
            response.put("errorCode", e.getErrorCode());
            response.put("sqlState", e.getSQLState());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/ping")
    @Operation(summary = "Application health check", description = "Simple ping endpoint to check if application is running")
    public ResponseEntity<Map<String, String>> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Application is running");
        return ResponseEntity.ok(response);
    }
}
