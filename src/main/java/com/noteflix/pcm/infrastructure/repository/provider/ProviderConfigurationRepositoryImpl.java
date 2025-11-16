package com.noteflix.pcm.infrastructure.repository.provider;

import com.noteflix.pcm.domain.provider.ProviderConfiguration;
import com.noteflix.pcm.infrastructure.dao.ProviderConfigurationDAO;
import com.noteflix.pcm.infrastructure.database.ConnectionManager;
import com.noteflix.pcm.infrastructure.exception.DatabaseException;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ProviderConfigurationRepository using SQLite
 *
 * <p>Manages provider configuration persistence with:
 * - Transaction management
 * - Exception handling
 * - Connection pooling via ConnectionManager
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class ProviderConfigurationRepositoryImpl implements ProviderConfigurationRepository {

    private final ProviderConfigurationDAO dao;
    private final ConnectionManager connectionManager;

    public ProviderConfigurationRepositoryImpl() {
        this.dao = new ProviderConfigurationDAO();
        this.connectionManager = ConnectionManager.INSTANCE;
    }

    // Constructor for testing
    public ProviderConfigurationRepositoryImpl(
            ProviderConfigurationDAO dao, ConnectionManager connectionManager) {
        this.dao = dao;
        this.connectionManager = connectionManager;
    }

    @Override
    public ProviderConfiguration save(ProviderConfiguration config) {
        try (Connection conn = connectionManager.getConnection()) {
            config.validate();
            return dao.create(config, conn);
        } catch (SQLException e) {
            log.error("Failed to save provider configuration", e);
            throw new DatabaseException("Failed to save provider configuration", e);
        }
    }

    @Override
    public void update(ProviderConfiguration config) {
        try (Connection conn = connectionManager.getConnection()) {
            config.validate();
            dao.update(config, conn);
            log.info("Updated provider configuration: {}", config.getProviderName());
        } catch (SQLException e) {
            log.error("Failed to update provider configuration", e);
            throw new DatabaseException("Failed to update provider configuration", e);
        }
    }

    @Override
    public Optional<ProviderConfiguration> findById(Long id) {
        try (Connection conn = connectionManager.getConnection()) {
            return dao.readById(id, conn);
        } catch (SQLException e) {
            log.error("Failed to find provider configuration by ID", e);
            throw new DatabaseException("Failed to find provider configuration", e);
        }
    }

    @Override
    public Optional<ProviderConfiguration> findByName(String providerName) {
        try (Connection conn = connectionManager.getConnection()) {
            return dao.readByName(providerName, conn);
        } catch (SQLException e) {
            log.error("Failed to find provider configuration by name", e);
            throw new DatabaseException("Failed to find provider configuration", e);
        }
    }

    @Override
    public List<ProviderConfiguration> findAll() {
        try (Connection conn = connectionManager.getConnection()) {
            return dao.readAll(conn);
        } catch (SQLException e) {
            log.error("Failed to find all provider configurations", e);
            throw new DatabaseException("Failed to find provider configurations", e);
        }
    }

    @Override
    public List<ProviderConfiguration> findEnabled() {
        try (Connection conn = connectionManager.getConnection()) {
            return dao.readEnabled(conn);
        } catch (SQLException e) {
            log.error("Failed to find enabled provider configurations", e);
            throw new DatabaseException("Failed to find provider configurations", e);
        }
    }

    @Override
    public Optional<ProviderConfiguration> findActive() {
        try (Connection conn = connectionManager.getConnection()) {
            return dao.readActive(conn);
        } catch (SQLException e) {
            log.error("Failed to find active provider configuration", e);
            throw new DatabaseException("Failed to find active provider configuration", e);
        }
    }

    @Override
    public void setActive(String providerName) {
        try (Connection conn = connectionManager.getConnection()) {
            dao.setActive(providerName, conn);
            log.info("Set active provider: {}", providerName);
        } catch (SQLException e) {
            log.error("Failed to set active provider", e);
            throw new DatabaseException("Failed to set active provider", e);
        }
    }

    @Override
    public void delete(Long id) {
        try (Connection conn = connectionManager.getConnection()) {
            dao.delete(id, conn);
            log.info("Deleted provider configuration: {}", id);
        } catch (SQLException e) {
            log.error("Failed to delete provider configuration", e);
            throw new DatabaseException("Failed to delete provider configuration", e);
        }
    }

    @Override
    public boolean exists(String providerName) {
        return findByName(providerName).isPresent();
    }
}

