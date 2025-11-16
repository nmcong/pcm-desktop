package com.noteflix.pcm.infrastructure.dao;

import com.noteflix.pcm.domain.provider.ProviderConfiguration;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for ProviderConfiguration
 *
 * <p>Handles all SQL operations for provider configurations including:
 * - CRUD operations
 * - Finding active provider
 * - Finding by name
 *
 * <p>This DAO follows:
 * - Single Responsibility: Only handles database operations
 * - Dependency Inversion: Works with Connection interface
 * - Clean separation from domain logic
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class ProviderConfigurationDAO {

    private static final String TABLE = "provider_configurations";

    /**
     * Create a new provider configuration
     */
    public ProviderConfiguration create(ProviderConfiguration config, Connection conn)
            throws SQLException {
        String sql = """
                INSERT INTO provider_configurations (
                    provider_name, display_name, api_key, api_base_url, default_model,
                    is_active, is_enabled, requires_api_key, connection_timeout, max_retries,
                    extra_config
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, config.getProviderName());
            stmt.setString(2, config.getDisplayName());
            stmt.setString(3, config.getApiKey());
            stmt.setString(4, config.getApiBaseUrl());
            stmt.setString(5, config.getDefaultModel());
            stmt.setBoolean(6, config.isActive());
            stmt.setBoolean(7, config.isEnabled());
            stmt.setBoolean(8, config.isRequiresApiKey());
            stmt.setInt(9, config.getConnectionTimeout() != null ? config.getConnectionTimeout() : 30000);
            stmt.setInt(10, config.getMaxRetries() != null ? config.getMaxRetries() : 3);
            stmt.setString(11, config.getExtraConfig());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating provider configuration failed, no rows affected");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    config.setId(rs.getLong(1));
                }
            }
        }

        return config;
    }

    /**
     * Read provider configuration by ID
     */
    public Optional<ProviderConfiguration> readById(Long id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM " + TABLE + " WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Read provider configuration by provider name
     */
    public Optional<ProviderConfiguration> readByName(String providerName, Connection conn)
            throws SQLException {
        String sql = "SELECT * FROM " + TABLE + " WHERE provider_name = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, providerName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Read all provider configurations
     */
    public List<ProviderConfiguration> readAll(Connection conn) throws SQLException {
        String sql = "SELECT * FROM " + TABLE + " ORDER BY display_name";
        List<ProviderConfiguration> configs = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                configs.add(mapRow(rs));
            }
        }

        return configs;
    }

    /**
     * Read enabled provider configurations
     */
    public List<ProviderConfiguration> readEnabled(Connection conn) throws SQLException {
        String sql = "SELECT * FROM " + TABLE + " WHERE is_enabled = 1 ORDER BY display_name";
        List<ProviderConfiguration> configs = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                configs.add(mapRow(rs));
            }
        }

        return configs;
    }

    /**
     * Read active provider configuration
     */
    public Optional<ProviderConfiguration> readActive(Connection conn) throws SQLException {
        String sql = "SELECT * FROM " + TABLE + " WHERE is_active = 1 LIMIT 1";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        }

        return Optional.empty();
    }

    /**
     * Update provider configuration
     */
    public void update(ProviderConfiguration config, Connection conn) throws SQLException {
        String sql = """
                UPDATE provider_configurations SET
                    display_name = ?, api_key = ?, api_base_url = ?, default_model = ?,
                    is_active = ?, is_enabled = ?, requires_api_key = ?,
                    connection_timeout = ?, max_retries = ?, extra_config = ?,
                    updated_at = CURRENT_TIMESTAMP, last_tested_at = ?, test_status = ?
                WHERE id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, config.getDisplayName());
            stmt.setString(2, config.getApiKey());
            stmt.setString(3, config.getApiBaseUrl());
            stmt.setString(4, config.getDefaultModel());
            stmt.setBoolean(5, config.isActive());
            stmt.setBoolean(6, config.isEnabled());
            stmt.setBoolean(7, config.isRequiresApiKey());
            stmt.setInt(8, config.getConnectionTimeout() != null ? config.getConnectionTimeout() : 30000);
            stmt.setInt(9, config.getMaxRetries() != null ? config.getMaxRetries() : 3);
            stmt.setString(10, config.getExtraConfig());

            if (config.getLastTestedAt() != null) {
                stmt.setTimestamp(11, Timestamp.valueOf(config.getLastTestedAt()));
            } else {
                stmt.setNull(11, Types.TIMESTAMP);
            }

            stmt.setString(12, config.getTestStatus());
            stmt.setLong(13, config.getId());

            stmt.executeUpdate();
        }
    }

    /**
     * Set active provider (deactivate all others)
     */
    public void setActive(String providerName, Connection conn) throws SQLException {
        // First, deactivate all
        String deactivateAll = "UPDATE " + TABLE + " SET is_active = 0";
        try (PreparedStatement stmt = conn.prepareStatement(deactivateAll)) {
            stmt.executeUpdate();
        }

        // Then activate the specified one
        String activate = "UPDATE " + TABLE + " SET is_active = 1 WHERE provider_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(activate)) {
            stmt.setString(1, providerName);
            stmt.executeUpdate();
        }
    }

    /**
     * Delete provider configuration
     */
    public void delete(Long id, Connection conn) throws SQLException {
        String sql = "DELETE FROM " + TABLE + " WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Map ResultSet row to ProviderConfiguration
     */
    private ProviderConfiguration mapRow(ResultSet rs) throws SQLException {
        return ProviderConfiguration.builder()
                .id(rs.getLong("id"))
                .providerName(rs.getString("provider_name"))
                .displayName(rs.getString("display_name"))
                .apiKey(rs.getString("api_key"))
                .apiBaseUrl(rs.getString("api_base_url"))
                .defaultModel(rs.getString("default_model"))
                .active(rs.getBoolean("is_active"))
                .enabled(rs.getBoolean("is_enabled"))
                .requiresApiKey(rs.getBoolean("requires_api_key"))
                .connectionTimeout(rs.getInt("connection_timeout"))
                .maxRetries(rs.getInt("max_retries"))
                .extraConfig(rs.getString("extra_config"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                .lastTestedAt(rs.getTimestamp("last_tested_at") != null
                        ? rs.getTimestamp("last_tested_at").toLocalDateTime()
                        : null)
                .testStatus(rs.getString("test_status"))
                .build();
    }
}

