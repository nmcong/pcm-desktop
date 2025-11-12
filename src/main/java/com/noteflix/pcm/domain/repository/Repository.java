package com.noteflix.pcm.domain.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface following Repository Pattern
 * <p>
 * Benefits:
 * - Abstracts data access logic from business logic
 * - Enables easy testing with mock implementations
 * - Follows Interface Segregation Principle
 * - Follows Dependency Inversion Principle
 *
 * @param <T>  Entity type
 * @param <ID> ID type
 */
public interface Repository<T, ID> {

    /**
     * Save a new entity or update existing one
     *
     * @param entity Entity to save
     * @return Saved entity with generated ID
     */
    T save(T entity);

    /**
     * Find entity by ID
     *
     * @param id Entity ID
     * @return Optional containing entity if found
     */
    Optional<T> findById(ID id);

    /**
     * Find all entities
     *
     * @return List of all entities
     */
    List<T> findAll();

    /**
     * Update existing entity
     *
     * @param entity Entity to update
     */
    void update(T entity);

    /**
     * Delete entity by ID
     *
     * @param id Entity ID to delete
     */
    void delete(ID id);

    /**
     * Check if entity exists
     *
     * @param id Entity ID
     * @return true if entity exists
     */
    boolean exists(ID id);

    /**
     * Count all entities
     *
     * @return Total count of entities
     */
    long count();
}

