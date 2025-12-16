package com.useractivity.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class DatabaseConnectionTester {
    
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/pip?useSSL=false&serverTimezone=UTC";
        String username = "root";
        String password = "Svmr12!@";
        
        System.out.println("Testing MySQL Database Connection...");
        System.out.println("URL: " + url);
        System.out.println("Username: " + username);
        System.out.println("----------------------------------------");
        
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ MySQL Driver loaded successfully");
            
            // Attempt connection
            Connection connection = DriverManager.getConnection(url, username, password);
            
            if (connection != null && !connection.isClosed()) {
                System.out.println("✅ Database connection successful!");
                
                var metaData = connection.getMetaData();
                System.out.println("Database Product: " + metaData.getDatabaseProductName());
                System.out.println("Database Version: " + metaData.getDatabaseProductVersion());
                System.out.println("Driver Name: " + metaData.getDriverName());
                System.out.println("Driver Version: " + metaData.getDriverVersion());
                System.out.println("Catalog: " + connection.getCatalog());
                System.out.println("----------------------------------------");
                
                // Test SELECT query on user_activities table
                testUserActivitiesTable(connection);
                
                connection.close();
                System.out.println("✅ Connection closed successfully");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL Driver not found: " + e.getMessage());
            System.err.println("Make sure mysql-connector-j is in the classpath");
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed!");
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testUserActivitiesTable(Connection connection) {
        System.out.println("Testing SELECT query on pip.user_activities table...");
        System.out.println("----------------------------------------");
        
        try {
            // Query to get total count
            String countQuery = "SELECT COUNT(*) as total_count FROM pip.user_activities";
            try (PreparedStatement countStmt = connection.prepareStatement(countQuery);
                 ResultSet countRs = countStmt.executeQuery()) {
                
                if (countRs.next()) {
                    long totalCount = countRs.getLong("total_count");
                    System.out.println("✅ Total records in pip.user_activities: " + totalCount);
                }
            }
            
            // Query to get count of non-deleted records
            String activeCountQuery = "SELECT COUNT(*) as active_count FROM pip.user_activities WHERE user_id = 101";
            try (PreparedStatement activeStmt = connection.prepareStatement(activeCountQuery);
                 ResultSet activeRs = activeStmt.executeQuery()) {
                
                if (activeRs.next()) {
                    long activeCount = activeRs.getLong("active_count");
                    System.out.println("✅ Active (101 user) records: " + activeCount);
                }
            }
            
            // Query to get count of deleted records
            String deletedCountQuery = "SELECT COUNT(*) as deleted_count FROM pip.user_activities WHERE is_deleted = true";
            try (PreparedStatement deletedStmt = connection.prepareStatement(deletedCountQuery);
                 ResultSet deletedRs = deletedStmt.executeQuery()) {
                
                if (deletedRs.next()) {
                    long deletedCount = deletedRs.getLong("deleted_count");
                    System.out.println("✅ Deleted records: " + deletedCount);
                }
            }
            
            // Query to get sample records (first 5)
            String selectQuery = "SELECT * FROM pip.user_activities ORDER BY created_at DESC LIMIT 5";
            try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
                 ResultSet resultSet = selectStmt.executeQuery()) {
                
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                int rowCount = 0;
                System.out.println("\nSample records (first 5):");
                System.out.println("----------------------------------------");
                
                // Display column headers
                StringBuilder headers = new StringBuilder();
                for (int i = 1; i <= columnCount; i++) {
                    headers.append(String.format("%-18s", metaData.getColumnName(i)));
                }
                System.out.println(headers.toString());
                System.out.println("-".repeat(columnCount * 18));
                
                // Display rows
                while (resultSet.next()) {
                    rowCount++;
                    StringBuilder row = new StringBuilder();
                    for (int i = 1; i <= columnCount; i++) {
                        Object value = resultSet.getObject(i);
                        String displayValue = (value == null) ? "NULL" : value.toString();
                        // Truncate long values
                        if (displayValue.length() > 15) {
                            displayValue = displayValue.substring(0, 12) + "...";
                        }
                        row.append(String.format("%-18s", displayValue));
                    }
                    System.out.println(row.toString());
                }
                
                if (rowCount == 0) {
                    System.out.println("⚠️  No records found in the table");
                } else {
                    System.out.println("-".repeat(columnCount * 18));
                    System.out.println("Displayed " + rowCount + " record(s)");
                }
            }
            
            System.out.println("----------------------------------------");
            System.out.println("✅ Query execution completed successfully!");
            
        } catch (SQLException e) {
            if (e.getErrorCode() == 1146) { // Table doesn't exist
                System.out.println("⚠️  Table 'pip.user_activities' does not exist yet.");
                System.out.println("   The table will be created automatically by Hibernate on first entity save.");
            } else {
                System.err.println("❌ Error executing SELECT query on user_activities table");
                System.err.println("Error Code: " + e.getErrorCode());
                System.err.println("SQL State: " + e.getSQLState());
                System.err.println("Message: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
