package com.noteflix.pcm.infrastructure.database;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Database Migration Manager
 * 
 * Automatically runs SQL migration scripts on application startup.
 * Follows Flyway-like convention: V<version>__<description>.sql
 * 
 * Features:
 * - Tracks applied migrations in schema_version table
 * - Runs migrations in order (V1, V2, V3...)
 * - Idempotent (safe to run multiple times)
 * - Transaction support
 * 
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class DatabaseMigrationManager {
    
    private static final String SCHEMA_VERSION_TABLE = "schema_version";
    private static final String MIGRATION_PATH = "/db/migration/";
    
    private final ConnectionManager connectionManager;
    
    public DatabaseMigrationManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    
    /**
     * Run all pending migrations
     */
    public void migrate() {
        log.info("🔄 Starting database migration...");
        
        try (Connection conn = connectionManager.getConnection()) {
            // 1. Create schema_version table if not exists
            createSchemaVersionTable(conn);
            
            // 2. Get list of applied migrations
            List<String> appliedMigrations = getAppliedMigrations(conn);
            log.info("Applied migrations: {}", appliedMigrations);
            
            // 3. Get list of available migration files
            List<String> availableMigrations = getAvailableMigrations();
            log.info("Available migrations: {}", availableMigrations);
            
            // 4. Run pending migrations
            int migrationsRun = 0;
            for (String migration : availableMigrations) {
                if (!appliedMigrations.contains(migration)) {
                    runMigration(conn, migration);
                    migrationsRun++;
                }
            }
            
            if (migrationsRun > 0) {
                log.info("✅ Applied {} migration(s) successfully", migrationsRun);
            } else {
                log.info("✅ Database is up to date (no migrations to run)");
            }
            
        } catch (Exception e) {
            log.error("❌ Database migration failed", e);
            throw new RuntimeException("Failed to run database migrations", e);
        }
    }
    
    /**
     * Create schema_version table to track migrations
     */
    private void createSchemaVersionTable(Connection conn) throws SQLException {
        String sql = 
            "CREATE TABLE IF NOT EXISTS " + SCHEMA_VERSION_TABLE + " (" +
            "    version TEXT PRIMARY KEY," +
            "    description TEXT," +
            "    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            log.debug("Schema version table created/verified");
        }
    }
    
    /**
     * Get list of already applied migrations
     */
    private List<String> getAppliedMigrations(Connection conn) throws SQLException {
        List<String> applied = new ArrayList<>();
        
        String sql = "SELECT version FROM " + SCHEMA_VERSION_TABLE + " ORDER BY version";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                applied.add(rs.getString("version"));
            }
        }
        
        return applied;
    }
    
    /**
     * Get list of available migration files from resources
     */
    private List<String> getAvailableMigrations() {
        List<String> migrations = new ArrayList<>();
        
        // Hardcoded migration files (in real app, would scan directory)
        migrations.add("V1__initial_schema.sql");
        migrations.add("V2__chat_tables.sql");
        
        return migrations;
    }
    
    /**
     * Run a single migration script
     */
    private void runMigration(Connection conn, String migrationFile) throws Exception {
        log.info("📝 Running migration: {}", migrationFile);
        
        // Read SQL file from resources
        String sql = readMigrationFile(migrationFile);
        
        // Extract version and description from filename
        String version = extractVersion(migrationFile);
        String description = extractDescription(migrationFile);
        
        // Run migration in transaction
        conn.setAutoCommit(false);
        
        try {
            // Execute all SQL statements
            executeSqlScript(conn, sql);
            
            // Record migration in schema_version table
            recordMigration(conn, version, description);
            
            // Commit transaction
            conn.commit();
            
            log.info("✅ Migration applied: {} - {}", version, description);
            
        } catch (Exception e) {
            // Rollback on error
            conn.rollback();
            log.error("❌ Migration failed: {} - {}", version, e.getMessage());
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
    
    /**
     * Read migration SQL file from resources
     */
    private String readMigrationFile(String filename) throws Exception {
        String path = MIGRATION_PATH + filename;
        
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("Migration file not found: " + path);
            }
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        }
    }
    
    /**
     * Execute SQL script (multiple statements)
     * 
     * Simple approach: Use ScriptRunner pattern
     * - Read line by line
     * - Build statement until semicolon
     * - Execute complete statement
     */
    private void executeSqlScript(Connection conn, String sql) throws SQLException {
        StringBuilder currentStatement = new StringBuilder();
        int stmtCount = 0;
        boolean inTrigger = false;  // Track if we're inside a CREATE TRIGGER
        
        try (Statement stmt = conn.createStatement()) {
            BufferedReader reader = new BufferedReader(new java.io.StringReader(sql));
            String line;
            boolean inComment = false;
            
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                String upperTrimmed = trimmed.toUpperCase();
                
                // Skip empty lines
                if (trimmed.isEmpty()) {
                    continue;
                }
                
                // Handle multi-line comments
                if (trimmed.contains("/*")) {
                    inComment = true;
                }
                if (inComment) {
                    if (trimmed.contains("*/")) {
                        inComment = false;
                    }
                    continue;
                }
                
                // Skip single-line comments
                if (trimmed.startsWith("--")) {
                    continue;
                }
                
                // Remove inline comments
                int commentIdx = line.indexOf("--");
                if (commentIdx >= 0) {
                    line = line.substring(0, commentIdx);
                    trimmed = line.trim();
                    upperTrimmed = trimmed.toUpperCase();
                }
                
                // Append to current statement
                currentStatement.append(line).append(" ");
                
                // Check if this is the start of a trigger
                if (upperTrimmed.startsWith("CREATE TRIGGER")) {
                    inTrigger = true;
                }
                
                // Determine if statement is complete
                boolean isComplete = false;
                if (inTrigger) {
                    // Trigger ends with "END;" on its own line or at end of line
                    if (upperTrimmed.equals("END;") || upperTrimmed.endsWith(" END;")) {
                        isComplete = true;
                        inTrigger = false;
                    }
                } else {
                    // Normal statement ends with semicolon
                    if (trimmed.endsWith(";")) {
                        isComplete = true;
                    }
                }
                
                if (isComplete) {
                    String statementToExecute = currentStatement.toString().trim();
                    
                    if (!statementToExecute.isEmpty()) {
                        stmtCount++;
                        
                        String preview = statementToExecute.length() > 80 
                            ? statementToExecute.substring(0, 77) + "..." 
                            : statementToExecute;
                        
                        log.info("[Statement {}] {}", stmtCount, preview.replace("\n", " "));
                        
                        try {
                            stmt.execute(statementToExecute);
                        } catch (SQLException e) {
                            log.error("Failed to execute: {}", preview);
                            throw e;
                        }
                    }
                    
                    // Reset for next statement
                    currentStatement = new StringBuilder();
                }
            }
            
            log.info("✅ Executed {} statements successfully", stmtCount);
        } catch (java.io.IOException e) {
            throw new SQLException("Failed to read SQL script", e);
        }
    }
    
    /**
     * Remove SQL comments from script
     */
    private String removeComments(String sql) {
        StringBuilder result = new StringBuilder();
        String[] lines = sql.split("\n");
        boolean inMultiLineComment = false;
        
        for (String line : lines) {
            // Handle multi-line comments
            if (line.contains("/*")) {
                inMultiLineComment = true;
            }
            
            if (inMultiLineComment) {
                if (line.contains("*/")) {
                    inMultiLineComment = false;
                }
                continue; // Skip this line
            }
            
            // Handle single-line comments
            int commentIndex = line.indexOf("--");
            if (commentIndex >= 0) {
                line = line.substring(0, commentIndex);
            }
            
            // Add non-empty lines
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                result.append(line).append("\n");
            }
        }
        
        return result.toString();
    }
    
    /**
     * Record migration in schema_version table
     */
    private void recordMigration(Connection conn, String version, String description) throws SQLException {
        String sql = "INSERT OR IGNORE INTO " + SCHEMA_VERSION_TABLE + " (version, description) VALUES (?, ?)";
        
        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, version);
            stmt.setString(2, description);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Extract version from filename: V1__description.sql -> V1
     */
    private String extractVersion(String filename) {
        int endIndex = filename.indexOf("__");
        if (endIndex == -1) {
            return filename.replace(".sql", "");
        }
        return filename.substring(0, endIndex);
    }
    
    /**
     * Extract description from filename: V1__initial_schema.sql -> initial_schema
     */
    private String extractDescription(String filename) {
        int startIndex = filename.indexOf("__");
        int endIndex = filename.lastIndexOf(".sql");
        
        if (startIndex == -1 || endIndex == -1) {
            return filename;
        }
        
        return filename.substring(startIndex + 2, endIndex).replace("_", " ");
    }
    
    /**
     * Check database status
     */
    public void printStatus() {
        try (Connection conn = connectionManager.getConnection()) {
            List<String> applied = getAppliedMigrations(conn);
            
            System.out.println("\n📊 Database Migration Status:");
            System.out.println("================================");
            
            if (applied.isEmpty()) {
                System.out.println("⚠️  No migrations applied yet");
            } else {
                System.out.println("✅ Applied migrations:");
                for (String version : applied) {
                    System.out.println("   - " + version);
                }
            }
            
            System.out.println("================================\n");
            
        } catch (Exception e) {
            log.error("Failed to get migration status", e);
        }
    }
}

