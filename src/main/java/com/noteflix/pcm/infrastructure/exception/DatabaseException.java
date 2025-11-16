package com.noteflix.pcm.infrastructure.exception;

/**
 * Custom exception for database operations
 *
 * <p>Provides more context than generic SQLException Follows Clean Code principle of meaningful
 * exception names
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create exception for entity not found
     */
    public static DatabaseException entityNotFound(String entityType, Object id) {
        return new DatabaseException(String.format("%s with id '%s' not found", entityType, id));
    }

    /**
     * Create exception for duplicate entity
     */
    public static DatabaseException duplicateEntity(String entityType, String field, Object value) {
        return new DatabaseException(
                String.format("%s with %s '%s' already exists", entityType, field, value));
    }

    /**
     * Create exception for constraint violation
     */
    public static DatabaseException constraintViolation(String constraint, String details) {
        return new DatabaseException(
                String.format("Constraint violation '%s': %s", constraint, details));
    }
}
