package com.noteflix.pcm.infrastructure.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Generic DAO interface following DAO Pattern
 *
 * <p>Lower-level data access interface compared to Repository Works directly with JDBC and SQL
 *
 * @param <T> Entity type
 * @param <ID> ID type
 */
public interface DAO<T, ID> {

  /**
   * Create new entity
   *
   * @param entity Entity to create
   * @param connection Database connection
   * @return Created entity with generated ID
   * @throws SQLException if database operation fails
   */
  T create(T entity, Connection connection) throws SQLException;

  /**
   * Read entity by ID
   *
   * @param id Entity ID
   * @param connection Database connection
   * @return Optional containing entity if found
   * @throws SQLException if database operation fails
   */
  Optional<T> read(ID id, Connection connection) throws SQLException;

  /**
   * Read all entities
   *
   * @param connection Database connection
   * @return List of all entities
   * @throws SQLException if database operation fails
   */
  List<T> readAll(Connection connection) throws SQLException;

  /**
   * Update existing entity
   *
   * @param entity Entity to update
   * @param connection Database connection
   * @throws SQLException if database operation fails
   */
  void update(T entity, Connection connection) throws SQLException;

  /**
   * Delete entity by ID
   *
   * @param id Entity ID to delete
   * @param connection Database connection
   * @throws SQLException if database operation fails
   */
  void delete(ID id, Connection connection) throws SQLException;

  /**
   * Check if entity exists
   *
   * @param id Entity ID
   * @param connection Database connection
   * @return true if entity exists
   * @throws SQLException if database operation fails
   */
  boolean exists(ID id, Connection connection) throws SQLException;

  /**
   * Count all entities
   *
   * @param connection Database connection
   * @return Total count
   * @throws SQLException if database operation fails
   */
  long count(Connection connection) throws SQLException;
}
