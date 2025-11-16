package com.noteflix.pcm.infrastructure.repository.provider;

import com.noteflix.pcm.domain.provider.ProviderConfiguration;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ProviderConfiguration
 *
 * <p>Provides abstraction over data access for provider configurations.
 * Follows Repository Pattern from Domain-Driven Design.
 *
 * @author PCM Team
 * @version 1.0.0
 */
public interface ProviderConfigurationRepository {

    /**
     * Save a new provider configuration
     *
     * @param config Configuration to save
     * @return Saved configuration with generated ID
     */
    ProviderConfiguration save(ProviderConfiguration config);

    /**
     * Update existing provider configuration
     *
     * @param config Configuration to update
     */
    void update(ProviderConfiguration config);

    /**
     * Find configuration by ID
     *
     * @param id Configuration ID
     * @return Optional containing configuration if found
     */
    Optional<ProviderConfiguration> findById(Long id);

    /**
     * Find configuration by provider name
     *
     * @param providerName Provider name (e.g., "openai")
     * @return Optional containing configuration if found
     */
    Optional<ProviderConfiguration> findByName(String providerName);

    /**
     * Get all provider configurations
     *
     * @return List of all configurations
     */
    List<ProviderConfiguration> findAll();

    /**
     * Get all enabled provider configurations
     *
     * @return List of enabled configurations
     */
    List<ProviderConfiguration> findEnabled();

    /**
     * Get the active provider configuration
     *
     * @return Optional containing active configuration
     */
    Optional<ProviderConfiguration> findActive();

    /**
     * Set a provider as active (deactivates all others)
     *
     * @param providerName Provider name to activate
     */
    void setActive(String providerName);

    /**
     * Delete a provider configuration
     *
     * @param id Configuration ID
     */
    void delete(Long id);

    /**
     * Check if a provider exists
     *
     * @param providerName Provider name
     * @return true if exists, false otherwise
     */
    boolean exists(String providerName);
}

