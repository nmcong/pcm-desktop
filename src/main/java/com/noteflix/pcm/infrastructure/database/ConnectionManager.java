package com.noteflix.pcm.infrastructure.database;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection manager following Singleton Pattern
 * <p>
 * Responsibilities:
 * - Manage database connections
 * - Ensure single instance (Singleton)
 * - Handle connection lifecycle
 * <p>
 * Thread-safe implementation using enum singleton pattern
 */
@Slf4j
public enum ConnectionManager {
    INSTANCE;

    private static final String DB_URL = "jdbc:sqlite:pcm-desktop.db";
    private static final String DB_DRIVER = "org.sqlite.JDBC";

    /**
     * Initialize database driver
     */
    static {
        try {
            Class.forName(DB_DRIVER);
            log.info("✅ SQLite JDBC driver loaded successfully");
        } catch (ClassNotFoundException e) {
            log.error("❌ Failed to load SQLite JDBC driver", e);
            throw new RuntimeException("Database driver not found", e);
        }
    }

    private Connection connection;

    /**
     * Get database connection
     * Creates new connection if not exists or closed
     *
     * @return Database connection
     * @throws SQLException if connection fails
     */
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            log.debug("Creating new database connection...");
            connection = DriverManager.getConnection(DB_URL);

            // Enable foreign keys for SQLite
            connection.createStatement().execute("PRAGMA foreign_keys = ON");

            log.info("✅ Database connection established: {}", DB_URL);
        }
        return connection;
    }

    /**
     * Close database connection
     */
    public synchronized void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                log.info("✅ Database connection closed");
            } catch (SQLException e) {
                log.error("❌ Failed to close database connection", e);
            } finally {
                connection = null;
            }
        }
    }

    /**
     * Test database connection
     *
     * @return true if connection is valid
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            log.error("❌ Database connection test failed", e);
            return false;
        }
    }

    /**
     * Get database URL
     */
    public String getDatabaseUrl() {
        return DB_URL;
    }
}

