package com.noteflix.pcm.application.service.provider;

import com.noteflix.pcm.domain.provider.ProviderConfiguration;
import com.noteflix.pcm.infrastructure.repository.provider.ProviderConfigurationRepository;
import com.noteflix.pcm.infrastructure.repository.provider.ProviderConfigurationRepositoryImpl;
import com.noteflix.pcm.llm.api.LLMProvider;
import com.noteflix.pcm.llm.model.ModelInfo;
import com.noteflix.pcm.llm.model.ProviderConfig;
import com.noteflix.pcm.llm.registry.ProviderRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing LLM Provider Configurations
 *
 * <p>Business logic layer that coordinates between:
 * - Database persistence (via Repository)
 * - LLM Provider Registry
 * - Provider testing and model loading
 *
 * <p>Follows Clean Architecture principles:
 * - Single Responsibility
 * - Dependency Inversion
 * - Separation of Concerns
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class ProviderConfigurationService {

    private final ProviderConfigurationRepository repository;
    private final ProviderRegistry providerRegistry;

    public ProviderConfigurationService() {
        this.repository = new ProviderConfigurationRepositoryImpl();
        this.providerRegistry = ProviderRegistry.getInstance();
    }

    public ProviderConfigurationService(
            ProviderConfigurationRepository repository, ProviderRegistry providerRegistry) {
        this.repository = repository;
        this.providerRegistry = providerRegistry;
    }

    /**
     * Get all provider configurations
     */
    public List<ProviderConfiguration> getAllConfigurations() {
        return repository.findAll();
    }

    /**
     * Get enabled provider configurations
     */
    public List<ProviderConfiguration> getEnabledConfigurations() {
        return repository.findEnabled();
    }

    /**
     * Get configuration by provider name
     */
    public Optional<ProviderConfiguration> getConfiguration(String providerName) {
        return repository.findByName(providerName);
    }

    /**
     * Get active provider configuration
     */
    public Optional<ProviderConfiguration> getActiveConfiguration() {
        return repository.findActive();
    }

    /**
     * Save or update provider configuration
     */
    public ProviderConfiguration saveConfiguration(ProviderConfiguration config) {
        config.validate();

        if (config.getId() == null) {
            // New configuration
            ProviderConfiguration saved = repository.save(config);
            log.info("Saved new provider configuration: {}", config.getProviderName());

            // Apply to registry if enabled
            if (saved.isEnabled() && saved.isReady()) {
                applyConfigurationToRegistry(saved);
            }

            return saved;
        } else {
            // Update existing
            repository.update(config);
            log.info("Updated provider configuration: {}", config.getProviderName());

            // Apply to registry if enabled
            if (config.isEnabled() && config.isReady()) {
                applyConfigurationToRegistry(config);
            }

            return config;
        }
    }

    /**
     * Set a provider as active
     */
    public void setActiveProvider(String providerName) {
        repository.setActive(providerName);
        providerRegistry.setActive(providerName);
        log.info("Set active provider: {}", providerName);
    }

    /**
     * Test provider connection
     */
    public CompletableFuture<Boolean> testConnection(String providerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Optional<ProviderConfiguration> configOpt = repository.findByName(providerName);
                if (configOpt.isEmpty()) {
                    log.warn("Provider configuration not found: {}", providerName);
                    return false;
                }

                ProviderConfiguration config = configOpt.get();

                // Apply configuration temporarily
                applyConfigurationToRegistry(config);

                // Test connection
                LLMProvider provider = providerRegistry.get(providerName);
                if (provider == null) {
                    log.warn("Provider not found in registry: {}", providerName);
                    return false;
                }

                boolean success = provider.testConnection();

                // Update test status
                if (success) {
                    config.markTestSuccess();
                } else {
                    config.markTestFailed();
                }
                repository.update(config);

                return success;
            } catch (Exception e) {
                log.error("Failed to test provider connection: {}", providerName, e);
                return false;
            }
        });
    }

    /**
     * Load available models from provider
     */
    public CompletableFuture<List<ModelInfo>> loadModels(String providerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Optional<ProviderConfiguration> configOpt = repository.findByName(providerName);
                if (configOpt.isEmpty()) {
                    log.warn("Provider configuration not found: {}", providerName);
                    return List.of();
                }

                ProviderConfiguration config = configOpt.get();

                // Apply configuration
                applyConfigurationToRegistry(config);

                // Get models
                LLMProvider provider = providerRegistry.get(providerName);
                if (provider == null) {
                    log.warn("Provider not found in registry: {}", providerName);
                    return List.of();
                }

                return provider.getModels();
            } catch (Exception e) {
                log.error("Failed to load models from provider: {}", providerName, e);
                return List.of();
            }
        });
    }

    /**
     * Apply configuration to provider registry
     */
    private void applyConfigurationToRegistry(ProviderConfiguration config) {
        LLMProvider provider = providerRegistry.get(config.getProviderName());
        if (provider != null) {
            ProviderConfig providerConfig = ProviderConfig.builder()
                    .apiKey(config.getApiKey())
                    .baseUrl(config.getApiBaseUrl())
                    .model(config.getDefaultModel())
                    .timeoutMs(config.getConnectionTimeout() != null ? config.getConnectionTimeout() : 30000)
                    .maxRetries(config.getMaxRetries() != null ? config.getMaxRetries() : 3)
                    .build();

            provider.configure(providerConfig);
            log.debug("Applied configuration to provider: {}", config.getProviderName());
        }
    }

    /**
     * Initialize all enabled providers
     */
    public void initializeProviders() {
        List<ProviderConfiguration> enabled = repository.findEnabled();
        for (ProviderConfiguration config : enabled) {
            if (config.isReady()) {
                applyConfigurationToRegistry(config);
            }
        }

        // Set active provider
        Optional<ProviderConfiguration> active = repository.findActive();
        active.ifPresent(config -> providerRegistry.setActive(config.getProviderName()));

        log.info("Initialized {} provider configurations", enabled.size());
    }
}

