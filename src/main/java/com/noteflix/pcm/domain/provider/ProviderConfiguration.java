package com.noteflix.pcm.domain.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain model for LLM Provider Configuration
 *
 * <p>Represents configuration settings for an LLM provider (OpenAI, Anthropic, Ollama, etc.)
 * including API keys, default model, and connection settings.
 *
 * <p>This is a domain entity that follows:
 * - Single Responsibility: Only holds provider configuration data
 * - Immutability: Uses Lombok's @Data for consistency
 * - Clean Architecture: Part of domain layer, no framework dependencies
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderConfiguration {

    /**
     * Unique identifier
     */
    private Long id;

    /**
     * Provider name (unique key, e.g., "openai", "anthropic")
     */
    private String providerName;

    /**
     * Display name (e.g., "OpenAI", "Anthropic Claude")
     */
    private String displayName;

    /**
     * API key for authentication
     */
    private String apiKey;

    /**
     * Base URL for API endpoints
     */
    private String apiBaseUrl;

    /**
     * Default model to use with this provider
     */
    private String defaultModel;

    /**
     * Whether this provider is currently active (default provider)
     */
    private boolean active;

    /**
     * Whether this provider is enabled
     */
    private boolean enabled;

    /**
     * Whether this provider requires an API key
     */
    private boolean requiresApiKey;

    /**
     * Connection timeout in milliseconds
     */
    private Integer connectionTimeout;

    /**
     * Maximum retry attempts for failed requests
     */
    private Integer maxRetries;

    /**
     * Additional configuration as JSON string
     */
    private String extraConfig;

    /**
     * Creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    private LocalDateTime updatedAt;

    /**
     * Last time connection was tested
     */
    private LocalDateTime lastTestedAt;

    /**
     * Test status ('success', 'failed', or null)
     */
    private String testStatus;

    /**
     * Validate configuration
     *
     * @throws IllegalStateException if configuration is invalid
     */
    public void validate() {
        if (providerName == null || providerName.trim().isEmpty()) {
            throw new IllegalStateException("Provider name cannot be empty");
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalStateException("Display name cannot be empty");
        }
        if (requiresApiKey && (apiKey == null || apiKey.trim().isEmpty())) {
            throw new IllegalStateException(
                    "API key is required for provider: " + providerName);
        }
    }

    /**
     * Check if provider is ready to use
     *
     * @return true if provider is configured and enabled
     */
    public boolean isReady() {
        return enabled && (!requiresApiKey || (apiKey != null && !apiKey.trim().isEmpty()));
    }

    /**
     * Mark provider as tested successfully
     */
    public void markTestSuccess() {
        this.testStatus = "success";
        this.lastTestedAt = LocalDateTime.now();
    }

    /**
     * Mark provider test as failed
     */
    public void markTestFailed() {
        this.testStatus = "failed";
        this.lastTestedAt = LocalDateTime.now();
    }
}

