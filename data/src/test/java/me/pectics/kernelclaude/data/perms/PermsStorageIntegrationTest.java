/*
 * Integration test for PermsStorage
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.perms;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple integration test for SQLite database connection.
 */
class PermsStorageIntegrationTest {

    private static final String DB_URL = "jdbc:sqlite:F:/kernel-claude/.db/db.test.sqlite";

    @Test
    void testDatabaseConnection() throws SQLException {
        System.out.println("Testing database connection...");

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            assertNotNull(conn, "Connection should not be null");
            assertFalse(conn.isClosed(), "Connection should be open");

            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("Database: " + meta.getDatabaseProductName());
            System.out.println("Database Version: " + meta.getDatabaseProductVersion());
            System.out.println("Driver: " + meta.getDriverName());
            System.out.println("Driver Version: " + meta.getDriverVersion());
            System.out.println("URL: " + meta.getURL());
        }

        System.out.println("Database connection test passed!");
    }

    @Test
    void testSimpleQuery() throws SQLException {
        System.out.println("Testing simple query...");

        try (Connection conn = DriverManager.getConnection(DB_URL);
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT 1 AS value")) {

            assertTrue(rs.next(), "Result set should have at least one row");
            int result = rs.getInt("value");
            assertEquals(1, result, "Simple query should return 1");
            System.out.println("Simple query test passed! Result: " + result);
        }
    }
}
