package com.noteflix.pcm.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Configuration for an LLM provider.
 *
 * <p>Contains authentication credentials, model selection, and provider-specific settings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderConfig {

    /**
     * API key for authentication.
     */
    private String apiKey;

    /**
     * Default model to use (can be overridden per request).
     */
    private String model;

    /**
     * Base URL for API (for custom endpoints or proxies).
     */
    private String baseUrl;

    /**
     * Organization ID (for providers that support it like OpenAI).
     */
    private String organizationId;

    /**
     * Request timeout in milliseconds.
     */
    @Builder.Default
    private long timeoutMs = 60000; // 60 seconds default

    /**
     * Maximum retries for failed requests.
     */
    @Builder.Default
    private int maxRetries = 3;

    /**
     * Additional HTTP headers.
     */
    private Map<String, String> headers;

    /**
     * Additional provider-specific configuration.
     */
    private Map<String, Object> metadata;
}
