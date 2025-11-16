package com.noteflix.pcm.llm.registry;

import com.noteflix.pcm.llm.api.LLMProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for managing LLM providers.
 *
 * <p>Provides: - Provider registration and retrieval - Active provider management - Provider
 * discovery and listing
 *
 * <p>This is a singleton - get the instance via getInstance().
 *
 * <p>Example usage:
 *
 * <pre>
 * ProviderRegistry registry = ProviderRegistry.getInstance();
 *
 * // Register providers
 * registry.register("openai", new OpenAIProvider());
 * registry.register("anthropic", new AnthropicProvider());
 *
 * // Set active provider
 * registry.setActive("openai");
 *
 * // Use active provider
 * LLMProvider provider = registry.getActive();
 * ChatResponse response = provider.chat(messages, options).get();
 * </pre>
 */
@Slf4j
public class ProviderRegistry {

    private static ProviderRegistry instance;

    private final Map<String, LLMProvider> providers;
    private String activeProviderName;

    private ProviderRegistry() {
        this.providers = new ConcurrentHashMap<>();
        log.info("ProviderRegistry initialized");
    }

    /**
     * Get the singleton instance.
     *
     * @return ProviderRegistry instance
     */
    public static synchronized ProviderRegistry getInstance() {
        if (instance == null) {
            instance = new ProviderRegistry();
        }
        return instance;
    }

    /**
     * Register a provider.
     *
     * @param name     Provider name (e.g., "openai", "anthropic")
     * @param provider Provider instance
     */
    public void register(String name, LLMProvider provider) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Provider name cannot be null or empty");
        }
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }

        providers.put(name, provider);
        log.info("Registered provider: {}", name);

        // If this is the first provider, make it active
        if (activeProviderName == null) {
            setActive(name);
        }
    }

    /**
     * Get a provider by name.
     *
     * @param name Provider name
     * @return Provider instance, or null if not found
     */
    public LLMProvider get(String name) {
        return providers.get(name);
    }

    /**
     * Get the active provider.
     *
     * @return Active provider instance
     * @throws IllegalStateException if no provider is active
     */
    public LLMProvider getActive() {
        if (activeProviderName == null) {
            throw new IllegalStateException("No active provider set");
        }

        LLMProvider provider = providers.get(activeProviderName);
        if (provider == null) {
            throw new IllegalStateException("Active provider not found: " + activeProviderName);
        }

        return provider;
    }

    /**
     * Set the active provider.
     *
     * <p>The active provider is used by default in application code.
     *
     * @param name Provider name
     * @throws IllegalArgumentException if provider not found
     */
    public void setActive(String name) {
        if (!providers.containsKey(name)) {
            throw new IllegalArgumentException("Provider not found: " + name);
        }

        this.activeProviderName = name;
        log.info("Active provider set to: {}", name);
    }

    /**
     * Get the active provider name.
     *
     * @return Active provider name, or null if none set
     */
    public String getActiveProviderName() {
        return activeProviderName;
    }

    /**
     * Check if a provider is registered.
     *
     * @param name Provider name
     * @return true if registered, false otherwise
     */
    public boolean hasProvider(String name) {
        return providers.containsKey(name);
    }

    /**
     * Get list of all registered provider names.
     *
     * @return List of provider names
     */
    public List<String> getAvailableProviders() {
        return new ArrayList<>(providers.keySet());
    }

    /**
     * Get all registered providers.
     *
     * @return Map of name to provider
     */
    public Map<String, LLMProvider> getAllProviders() {
        return new HashMap<>(providers);
    }

    /**
     * Unregister a provider.
     *
     * <p>If this is the active provider, active will be set to null.
     *
     * @param name Provider name
     * @return true if provider was removed, false if not found
     */
    public boolean unregister(String name) {
        LLMProvider removed = providers.remove(name);

        if (removed != null) {
            log.info("Unregistered provider: {}", name);

            // If this was the active provider, clear active
            if (name.equals(activeProviderName)) {
                activeProviderName = null;
                log.info("Active provider cleared");

                // Automatically set another provider as active if available
                if (!providers.isEmpty()) {
                    String firstAvailable = providers.keySet().iterator().next();
                    setActive(firstAvailable);
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Clear all registered providers.
     */
    public void clear() {
        providers.clear();
        activeProviderName = null;
        log.info("All providers cleared");
    }

    /**
     * Get count of registered providers.
     *
     * @return Number of registered providers
     */
    public int getProviderCount() {
        return providers.size();
    }

    /**
     * Check if any provider is registered.
     *
     * @return true if at least one provider is registered
     */
    public boolean hasAnyProvider() {
        return !providers.isEmpty();
    }
}
