package com.noteflix.pcm.application.service.provider;

import com.noteflix.pcm.domain.provider.ProviderConfiguration;
import com.noteflix.pcm.infrastructure.repository.provider.ProviderConfigurationRepository;
import com.noteflix.pcm.infrastructure.repository.provider.ProviderConfigurationRepositoryImpl;
import com.noteflix.pcm.llm.api.LLMProvider;
import com.noteflix.pcm.llm.registry.ProviderRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for synchronizing LLM providers between Registry and Database
 *
 * <p>Automatically creates/updates database records for providers registered in ProviderRegistry.
 * This eliminates the need to manually insert provider configurations in migrations.
 *
 * <p>Key features:
 * - Auto-detect new providers from registry
 * - Create database records for new providers
 * - Preserve existing configurations
 * - Set sensible defaults
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class ProviderSyncService {

    private final ProviderRegistry providerRegistry;
    private final ProviderConfigurationRepository repository;

    // Provider metadata (display names and default settings)
    private static final Map<String, ProviderMetadata> PROVIDER_METADATA = new HashMap<>();

    static {
        PROVIDER_METADATA.put("openai", new ProviderMetadata(
                "OpenAI",
                "https://api.openai.com/v1",
                "gpt-4-turbo-preview",
                true
        ));
        PROVIDER_METADATA.put("anthropic", new ProviderMetadata(
                "Anthropic Claude",
                "https://api.anthropic.com/v1",
                "claude-3-opus-20240229",
                true
        ));
        PROVIDER_METADATA.put("ollama", new ProviderMetadata(
                "Ollama (Local)",
                "http://localhost:11434",
                "llama2",
                false
        ));
        PROVIDER_METADATA.put("custom", new ProviderMetadata(
                "Custom API",
                null,
                "default",
                true
        ));
    }

    public ProviderSyncService() {
        this.providerRegistry = ProviderRegistry.getInstance();
        this.repository = new ProviderConfigurationRepositoryImpl();
    }

    public ProviderSyncService(ProviderRegistry providerRegistry, ProviderConfigurationRepository repository) {
        this.providerRegistry = providerRegistry;
        this.repository = repository;
    }

    /**
     * Synchronize all registered providers to database
     *
     * @return Number of providers synced
     */
    public int syncProvidersToDatabase() {
        log.info("🔄 Syncing providers from registry to database...");

        Map<String, LLMProvider> registeredProviders = providerRegistry.getAllProviders();
        int syncedCount = 0;

        for (Map.Entry<String, LLMProvider> entry : registeredProviders.entrySet()) {
            String providerName = entry.getKey();
            LLMProvider provider = entry.getValue();

            try {
                if (syncProvider(providerName, provider)) {
                    syncedCount++;
                }
            } catch (Exception e) {
                log.error("Failed to sync provider: {}", providerName, e);
            }
        }

        log.info("✅ Synced {} provider(s) to database", syncedCount);
        return syncedCount;
    }

    /**
     * Sync a single provider to database
     *
     * @param providerName Provider name
     * @param provider     Provider instance
     * @return true if synced (created or updated), false if already exists
     */
    private boolean syncProvider(String providerName, LLMProvider provider) {
        Optional<ProviderConfiguration> existingOpt = repository.findByName(providerName);

        if (existingOpt.isPresent()) {
            // Provider already exists in DB - don't overwrite user configurations
            log.debug("Provider already configured in DB: {}", providerName);
            return false;
        }

        // Create new provider configuration
        ProviderMetadata metadata = PROVIDER_METADATA.getOrDefault(
                providerName,
                new ProviderMetadata(
                        formatDisplayName(providerName),
                        null,
                        null,
                        true
                )
        );

        ProviderConfiguration config = ProviderConfiguration.builder()
                .providerName(providerName)
                .displayName(metadata.displayName)
                .apiBaseUrl(metadata.baseUrl)
                .defaultModel(metadata.defaultModel)
                .requiresApiKey(metadata.requiresApiKey)
                .enabled(true)
                .active(false) // Will be set later if this is the active provider
                .connectionTimeout(30000)
                .maxRetries(3)
                .build();

        // Get API key from provider's current config if available
        try {
            if (provider.getConfig() != null && provider.getConfig().getApiKey() != null) {
                config.setApiKey(provider.getConfig().getApiKey());
            }
        } catch (Exception e) {
            log.debug("Could not get API key from provider: {}", providerName);
        }

        repository.save(config);
        log.info("✅ Created database configuration for provider: {}", providerName);

        return true;
    }

    /**
     * Sync active provider from registry to database
     */
    public void syncActiveProvider() {
        String activeProviderName = providerRegistry.getActiveProviderName();
        if (activeProviderName != null) {
            repository.setActive(activeProviderName);
            log.info("✅ Set active provider in database: {}", activeProviderName);
        }
    }

    /**
     * Format provider name to display name
     */
    private String formatDisplayName(String providerName) {
        if (providerName == null || providerName.isEmpty()) {
            return "Unknown Provider";
        }
        // Capitalize first letter
        return providerName.substring(0, 1).toUpperCase() + providerName.substring(1);
    }

    /**
     * Provider metadata for default configurations
     */
    private record ProviderMetadata(
            String displayName,
            String baseUrl,
            String defaultModel,
            boolean requiresApiKey
    ) {}
}

