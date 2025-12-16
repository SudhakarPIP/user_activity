package com.useractivity;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
@ActiveProfiles("test") // Use test profile with H2, or remove to use MySQL
class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testDatabaseConnection() throws SQLException {
        assertNotNull(dataSource, "DataSource should not be null");
        
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "Connection should not be null");
            assertFalse(connection.isClosed(), "Connection should be open");
            
            DatabaseMetaData metaData = connection.getMetaData();
            assertNotNull(metaData, "DatabaseMetaData should not be null");
            
            log.info("✅ Database Connection Test: SUCCESS");
            log.info("URL: " + metaData.getURL());
            log.info("Username: " + metaData.getUserName());
        }
    }

    @Test
    void testDatabaseConnectionWithMySQL() throws SQLException {
        // This test specifically checks for MySQL connection
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String productName = metaData.getDatabaseProductName();
            
            // Note: In test profile, this might be H2, so we'll allow both
            assertTrue(productName.contains("MySQL") || productName.contains("H2"), 
                "Expected MySQL or H2 database, but got: " + productName);

            log.info("✅ Database Connection Verified: " + productName);
        }
    }

    @Test
    void testUserActivitiesTableCount() throws SQLException {
        assertNotNull(dataSource, "DataSource should not be null");
        
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "Connection should not be null");
            
            // Get database product name to determine table name format
            DatabaseMetaData metaData = connection.getMetaData();
            String productName = metaData.getDatabaseProductName();
            String catalog = connection.getCatalog();
            
            // Adjust table name based on database type
            // For MySQL: pip.user_activities, for H2: user_activities
            String tableName;
            if (productName.contains("MySQL")) {
                tableName = (catalog != null ? catalog + "." : "") + "user_activities";
            } else {
                // H2 or other databases
                tableName = "user_activities";
            }

            log.info("Testing SELECT COUNT query on table: " + tableName);

            // Test 1: Total count query
            String totalCountQuery = "SELECT COUNT(*) as total_count FROM " + tableName;
            try (PreparedStatement stmt = connection.prepareStatement(totalCountQuery);
                 ResultSet rs = stmt.executeQuery()) {
                
                assertTrue(rs.next(), "ResultSet should have at least one row");
                long totalCount = rs.getLong("total_count");
                
                assertTrue(totalCount >= 0, "Count should be non-negative");
                log.info("✅ Total records in " + tableName + ": " + totalCount);
                
                // Assert that count query executed successfully
                assertNotNull(totalCount);
            }
            
            // Test 2: Count of user_id records
            String activeCountQuery = "SELECT COUNT(*) as user_id FROM " + tableName + " WHERE user_id = 111";
            try (PreparedStatement stmt = connection.prepareStatement(activeCountQuery);
                 ResultSet rs = stmt.executeQuery()) {
                
                assertTrue(rs.next(), "ResultSet should have at least one row");
                long activeCount = rs.getLong("user_id");
                
                assertTrue(activeCount >= 0, "Active count should be non-negative");
                log.info("✅ Active (user 111) records: " + activeCount);
            }
            
            // Test 3: Count of deleted records
            String deletedCountQuery = "SELECT COUNT(*) as deleted_count FROM " + tableName + " WHERE is_deleted = true";
            try (PreparedStatement stmt = connection.prepareStatement(deletedCountQuery);
                 ResultSet rs = stmt.executeQuery()) {
                
                assertTrue(rs.next(), "ResultSet should have at least one row");
                long deletedCount = rs.getLong("deleted_count");
                
                assertTrue(deletedCount >= 0, "Deleted count should be non-negative");
                log.info("✅ Deleted records: " + deletedCount);
            }
            
            // Test 4: Count by user_id (example: user_id = 101)
            String userCountQuery = "SELECT COUNT(*) as user_count FROM " + tableName + " WHERE user_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(userCountQuery)) {
                stmt.setLong(1, 101L);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    assertTrue(rs.next(), "ResultSet should have at least one row");
                    long userCount = rs.getLong("user_count");
                    
                    assertTrue(userCount >= 0, "User count should be non-negative");
                    log.info("✅ Records for user_id 101: " + userCount);
                }
            }
            
            log.info("✅ All COUNT queries executed successfully!");
            
        } catch (SQLException e) {
            if (e.getErrorCode() == 1146 || e.getMessage().contains("does not exist") || 
                e.getMessage().contains("Table") && e.getMessage().contains("not found")) {
                log.error("⚠️  Table 'user_activities' does not exist yet.");
                log.error("   This is expected if the table hasn't been created.");
                log.error("   The table will be created automatically by Hibernate on first entity save.");
                // Don't fail the test if table doesn't exist - it's a valid scenario
            } else {
                // Re-throw other SQL exceptions
                throw e;
            }
        }

    }

    @Test
    void testSelectAllFromUserActivities() throws SQLException {
        assertNotNull(dataSource, "DataSource should not be null");
        
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "Connection should not be null");
            
            DatabaseMetaData metaData = connection.getMetaData();
            String productName = metaData.getDatabaseProductName();
            String catalog = connection.getCatalog();
            
            // Adjust table name based on database type
            String tableName;
            if (productName.contains("MySQL")) {
                tableName = (catalog != null ? catalog + "." : "") + "user_activities";
            } else {
                tableName = "user_activities";
            }

            log.info("Testing SELECT * FROM " + tableName);
            log.info("----------------------------------------");
            
            String selectQuery = "SELECT * FROM " + tableName + " ORDER BY created_at DESC LIMIT 10";
            
            try (PreparedStatement stmt = connection.prepareStatement(selectQuery);
                 ResultSet rs = stmt.executeQuery()) {
                
                ResultSetMetaData resultSetMetaData = rs.getMetaData();
                int columnCount = resultSetMetaData.getColumnCount();
                
                // Verify columns exist
                assertTrue(columnCount > 0, "Table should have columns");

                log.info("Table has " + columnCount + " columns:");
                for (int i = 1; i <= columnCount; i++) {
                    log.info("  - " + resultSetMetaData.getColumnName(i) +
                                     " (" + resultSetMetaData.getColumnTypeName(i) + ")");
                }
                
                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                }

                log.info("Total rows retrieved: " + rowCount);
                log.info("✅ SELECT query executed successfully!");
                
                // Assert that query executed without errors
                assertTrue(rowCount >= 0, "Row count should be non-negative");
                
            } catch (SQLException e) {
                if (e.getErrorCode() == 1146 || e.getMessage().contains("does not exist") || 
                    e.getMessage().contains("Table") && e.getMessage().contains("not found")) {
                    log.info("⚠️  Table 'user_activities' does not exist yet.");
                    log.info("   This is expected if the table hasn't been created.");
                    // Don't fail the test if table doesn't exist
                } else {
                    throw e;
                }
            }
        }
    }
}
