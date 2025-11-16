package com.noteflix.pcm.llm.api;

import com.noteflix.pcm.llm.model.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Unified interface for LLM providers.
 *
 * <p>All LLM providers (OpenAI, Anthropic, Ollama, etc.) implement this interface, providing a
 * consistent API for: - Chat completion (with/without streaming) - Function/tool calling - Token
 * counting - Provider capabilities - Model listing
 *
 * <p>This abstraction allows easy switching between providers and supports multiple active
 * providers simultaneously.
 *
 * <p>Example usage:
 *
 * <pre>
 * LLMProvider provider = ProviderRegistry.getInstance().getActive();
 *
 * List&lt;Message&gt; messages = List.of(
 *     Message.system("You are a helpful assistant"),
 *     Message.user("Hello!")
 * );
 *
 * ChatResponse response = provider.chat(messages, ChatOptions.defaults()).get();
 * System.out.println(response.getContent());
 * </pre>
 */
public interface LLMProvider {

    // ========== Core Metadata ==========

    /**
     * Get provider name (e.g., "openai", "anthropic", "ollama").
     *
     * @return Provider name
     */
    String getName();

    /**
     * Configure this provider with API keys, model, etc.
     *
     * @param config Provider configuration
     */
    void configure(ProviderConfig config);

    /**
     * Get current configuration.
     *
     * @return Provider configuration
     */
    ProviderConfig getConfig();

    // ========== Chat API ==========

    /**
     * Send a chat request (non-streaming).
     *
     * <p>Returns a CompletableFuture that resolves when the complete response is ready. For streaming
     * responses, use chatStream() instead.
     *
     * @param messages Conversation messages
     * @param options  Chat options (model, temperature, tools, etc.)
     * @return Future with complete response
     */
    CompletableFuture<ChatResponse> chat(List<Message> messages, ChatOptions options);

    /**
     * Send a streaming chat request.
     *
     * <p>Tokens arrive incrementally via the provided event listener. Use this for real-time UI
     * updates as text is generated.
     *
     * @param messages Conversation messages
     * @param options  Chat options
     * @param listener Event listener for streaming events
     */
    void chatStream(List<Message> messages, ChatOptions options, ChatEventListener listener);

    // ========== Capabilities ==========

    /**
     * Get provider capabilities (streaming, function calling, etc.).
     *
     * <p>Use this to check what features are supported before using them.
     *
     * @return Provider capabilities
     */
    ProviderCapabilities getCapabilities();

    /**
     * List available models from this provider.
     *
     * <p>Returns models with their characteristics (context window, pricing, etc.).
     *
     * @return List of available models
     */
    List<ModelInfo> getModels();

    /**
     * Get information about a specific model.
     *
     * @param modelId Model identifier
     * @return Model information, or null if not found
     */
    ModelInfo getModelInfo(String modelId);

    // ========== Token Management ==========

    /**
     * Count tokens in a text string.
     *
     * @param text Text to count
     * @return Estimated token count
     */
    int countTokens(String text);

    /**
     * Count tokens in a list of messages.
     *
     * <p>Includes overhead for message formatting.
     *
     * @param messages Messages to count
     * @return Estimated total token count
     */
    int countTokens(List<Message> messages);

    /**
     * Get the token counter used by this provider.
     *
     * @return Token counter
     */
    TokenCounter getTokenCounter();

    /**
     * Set a custom token counter.
     *
     * <p>By default, providers use a counter appropriate for their models. You can override it with a
     * custom implementation.
     *
     * @param counter Custom token counter
     */
    void setTokenCounter(TokenCounter counter);

    // ========== Status ==========

    /**
     * Check if provider is ready to handle requests.
     *
     * <p>Returns false if not configured or credentials are invalid.
     *
     * @return true if ready, false otherwise
     */
    boolean isReady();

    /**
     * Test the connection to provider API.
     *
     * <p>Makes a minimal API call to verify credentials and connectivity.
     *
     * @return true if connection successful, false otherwise
     */
    boolean testConnection();
}
