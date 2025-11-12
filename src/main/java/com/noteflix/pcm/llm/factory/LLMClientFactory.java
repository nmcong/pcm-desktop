package com.noteflix.pcm.llm.factory;

import com.noteflix.pcm.llm.api.LLMClient;
import com.noteflix.pcm.llm.client.anthropic.AnthropicClient;
import com.noteflix.pcm.llm.client.ollama.OllamaClient;
import com.noteflix.pcm.llm.client.openai.OpenAIClient;
import com.noteflix.pcm.llm.exception.LLMException;
import com.noteflix.pcm.llm.model.LLMProviderConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating LLM clients based on provider configuration
 * <p>
 * Follows Factory pattern to support multiple providers:
 * - OpenAI (GPT-4, GPT-3.5-turbo)
 * - Anthropic (Claude) - Coming soon
 * - Ollama (Local models) - Coming soon
 * - Custom providers - Easy to extend
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class LLMClientFactory {

    // Singleton pattern
    private static LLMClientFactory instance;

    // Cache clients by provider name
    private final Map<String, LLMClient> clientCache;

    private LLMClientFactory() {
        this.clientCache = new HashMap<>();
        log.info("Initialized LLMClientFactory");
    }

    public static synchronized LLMClientFactory getInstance() {
        if (instance == null) {
            instance = new LLMClientFactory();
        }
        return instance;
    }

    /**
     * Create or get cached LLM client for the given configuration
     */
    public LLMClient getClient(LLMProviderConfig config) {
        config.validate();

        String cacheKey = config.getProvider().name();

        // Return cached client if exists
        if (clientCache.containsKey(cacheKey)) {
            log.debug("Returning cached client for provider: {}", cacheKey);
            return clientCache.get(cacheKey);
        }

        // Create new client
        LLMClient client = createClient(config);
        clientCache.put(cacheKey, client);

        log.info("Created new {} client", config.getProvider());
        return client;
    }

    /**
     * Create a new LLM client (not cached)
     */
    public LLMClient createClient(LLMProviderConfig config) {
        config.validate();

        switch (config.getProvider()) {
            case OPENAI:
                return new OpenAIClient(config);

            case ANTHROPIC:
                return new AnthropicClient(config);

            case OLLAMA:
                return new OllamaClient(config);

            case CUSTOM:
                throw new LLMException("Custom client not yet implemented. Please implement your own LLMClient.");

            default:
                throw new LLMException("Unknown provider: " + config.getProvider());
        }
    }

    /**
     * Clear all cached clients
     */
    public void clearCache() {
        clientCache.clear();
        log.info("Cleared all cached LLM clients");
    }

    /**
     * Remove specific client from cache
     */
    public void removeFromCache(LLMProviderConfig.Provider provider) {
        clientCache.remove(provider.name());
        log.debug("Removed {} client from cache", provider);
    }
}

