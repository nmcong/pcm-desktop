package com.noteflix.pcm.core.auth;

import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Security audit logging for token access
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class SecurityAuditLogger {

    private static final String AUDIT_LOG_FILE = "logs/security-audit.log";
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

    public SecurityAuditLogger() {
        // Ensure log directory exists
        try {
            Path logDir = Paths.get("logs");
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
        } catch (IOException e) {
            log.warn("Failed to create audit log directory: {}", e.getMessage());
        }
    }

    /**
     * Log token access attempts
     */
    public void logTokenAccess(String serviceName, String operation, boolean success) {
        String logEntry =
                String.format(
                        "TOKEN_ACCESS|service=%s|operation=%s|success=%s|timestamp=%s|user=%s|host=%s",
                        serviceName,
                        operation,
                        success,
                        formatter.format(Instant.now()),
                        System.getProperty("user.name"),
                        getHostname());

        if (success) {
            log.info("Security: {}", logEntry);
        } else {
            log.warn("Security: {}", logEntry);
        }

        writeToAuditFile(logEntry);
    }

    /**
     * Log suspicious security activities
     */
    public void logSuspiciousActivity(String activity, String details) {
        String logEntry =
                String.format(
                        "SECURITY_ALERT|activity=%s|details=%s|timestamp=%s|user=%s|host=%s",
                        activity,
                        sanitizeLogValue(details),
                        formatter.format(Instant.now()),
                        System.getProperty("user.name"),
                        getHostname());

        log.error("Security Alert: {}", logEntry);
        writeToAuditFile("ALERT|" + logEntry);
    }

    /**
     * Log authentication events
     */
    public void logAuthentication(
            String serviceName, String authMethod, boolean success, String reason) {
        String logEntry =
                String.format(
                        "AUTH_EVENT|service=%s|method=%s|success=%s|reason=%s|timestamp=%s|user=%s|host=%s",
                        serviceName,
                        authMethod,
                        success,
                        sanitizeLogValue(reason),
                        formatter.format(Instant.now()),
                        System.getProperty("user.name"),
                        getHostname());

        if (success) {
            log.info("Auth: {}", logEntry);
        } else {
            log.warn("Auth: {}", logEntry);
        }

        writeToAuditFile(logEntry);
    }

    /**
     * Log configuration changes
     */
    public void logConfigurationChange(
            String configType, String change, String oldValue, String newValue) {
        String logEntry =
                String.format(
                        "CONFIG_CHANGE|type=%s|change=%s|old=%s|new=%s|timestamp=%s|user=%s|host=%s",
                        configType,
                        sanitizeLogValue(change),
                        sanitizeLogValue(oldValue),
                        sanitizeLogValue(newValue),
                        formatter.format(Instant.now()),
                        System.getProperty("user.name"),
                        getHostname());

        log.info("Config: {}", logEntry);
        writeToAuditFile(logEntry);
    }

    /**
     * Log system events
     */
    public void logSystemEvent(String eventType, String description) {
        String logEntry =
                String.format(
                        "SYSTEM_EVENT|type=%s|description=%s|timestamp=%s|user=%s|host=%s",
                        eventType,
                        sanitizeLogValue(description),
                        formatter.format(Instant.now()),
                        System.getProperty("user.name"),
                        getHostname());

        log.info("System: {}", logEntry);
        writeToAuditFile(logEntry);
    }

    /**
     * Write log entry to audit file
     */
    private void writeToAuditFile(String logEntry) {
        try {
            Path auditFile = Paths.get(AUDIT_LOG_FILE);

            // Ensure parent directory exists
            if (auditFile.getParent() != null && !Files.exists(auditFile.getParent())) {
                Files.createDirectories(auditFile.getParent());
            }

            // Append to audit file
            try (FileWriter writer = new FileWriter(auditFile.toFile(), true)) {
                writer.write(logEntry + System.lineSeparator());
                writer.flush();
            }

        } catch (IOException e) {
            log.error("Failed to write to audit log file: {}", e.getMessage());
        }
    }

    /**
     * Sanitize log values to prevent log injection
     */
    private String sanitizeLogValue(String value) {
        if (value == null) {
            return "null";
        }

        return value
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("|", "\\|")
                .replace("=", "\\=");
    }

    /**
     * Get hostname safely
     */
    private String getHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
