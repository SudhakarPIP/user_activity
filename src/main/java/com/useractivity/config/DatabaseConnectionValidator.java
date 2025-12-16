package com.useractivity.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // Run early in startup sequence
public class DatabaseConnectionValidator implements CommandLineRunner {

    private final DataSource dataSource;

    @Override
    public void run(String... args) {
        log.info("Validating database connection...");
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            log.info("✅ Database connection successful!");
            log.info("Database: {} {}", 
                metaData.getDatabaseProductName(), 
                metaData.getDatabaseProductVersion());
            log.info("Driver: {} {}", 
                metaData.getDriverName(), 
                metaData.getDriverVersion());
            log.info("URL: {}", metaData.getURL());
            log.info("Catalog: {}", connection.getCatalog());
            
            // Test query execution on user_activities table
            testUserActivitiesTable(connection);
            
        } catch (Exception e) {
            log.error("❌ Database connection failed!", e);
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    private void testUserActivitiesTable(Connection connection) {
        log.info("----------------------------------------");
        log.info("Testing SELECT query on pip.user_activities table...");
        
        String query = "SELECT * FROM pip.user_activities ORDER BY created_at DESC LIMIT 10";
        
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int columnCount = resultSetMetaData.getColumnCount();
            
            // Display column headers
            log.info("Table Columns:");
            StringBuilder headers = new StringBuilder();
            for (int i = 1; i <= columnCount; i++) {
                headers.append(String.format("%-20s", resultSetMetaData.getColumnName(i)));
            }
            log.info(headers.toString());
            log.info("-".repeat(columnCount * 20));
            
            // Count rows
            int rowCount = 0;
            while (resultSet.next()) {
                rowCount++;
                
                // Display row data
                StringBuilder row = new StringBuilder();
                for (int i = 1; i <= columnCount; i++) {
                    Object value = resultSet.getObject(i);
                    String displayValue = (value == null) ? "NULL" : value.toString();
                    // Truncate long values for better display
                    if (displayValue.length() > 18) {
                        displayValue = displayValue.substring(0, 15) + "...";
                    }
                    row.append(String.format("%-20s", displayValue));
                }
                log.info(row.toString());
            }
            
            if (rowCount == 0) {
                log.info("⚠️  Table is empty - No records found in pip.user_activities");
            } else {
                log.info("-".repeat(columnCount * 20));
                log.info("✅ Query executed successfully!");
                log.info("Total rows retrieved: {}", rowCount);
                
                // Get total count
                try (PreparedStatement countStmt = connection.prepareStatement(
                        "SELECT COUNT(*) as total FROM pip.user_activities WHERE is_deleted = false")) {
                    ResultSet countRs = countStmt.executeQuery();
                    if (countRs.next()) {
                        long totalCount = countRs.getLong("total");
                        log.info("Total non-deleted records in table: {}", totalCount);
                    }
                }
            }
            
        } catch (SQLException e) {
            if (e.getErrorCode() == 1146) { // Table doesn't exist error code
                log.warn("⚠️  Table 'pip.user_activities' does not exist yet.");
                log.info("   The table will be created automatically by Hibernate on first entity save.");
            } else {
                log.error("❌ Error executing SELECT query on user_activities table", e);
                log.error("SQL Error Code: {}", e.getErrorCode());
                log.error("SQL State: {}", e.getSQLState());
                log.error("Error Message: {}", e.getMessage());
            }
        }
        
        log.info("----------------------------------------");
    }

}
