package com.noteflix.pcm.llm.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Configuration for an LLM provider
 * <p>
 * Contains all information needed to connect to and use an LLM provider.
 * <p>
 * Example configurations:
 * <p>
 * OpenAI:
 * <pre>
 * LLMProviderConfig.builder()
 *     .provider(Provider.OPENAI)
 *     .name("OpenAI GPT-4")
 *     .url("https://api.openai.com/v1/chat/completions")
 *     .token(System.getenv("OPENAI_API_KEY"))
 *     .model("gpt-4")
 *     .supportsStreaming(true)
 *     .supportsFunctionCalling(true)
 *     .build();
 * </pre>
 * <p>
 * Custom Provider:
 * <pre>
 * LLMProviderConfig.builder()
 *     .provider(Provider.CUSTOM)
 *     .name("My Custom LLM")
 *     .url("https://my-api.com/v1/chat")
 *     .token("my-secret-token")
 *     .model("custom-model-v1")
 *     .supportsStreaming(false)
 *     .supportsFunctionCalling(false)
 *     .build();
 * </pre>
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Data
@Builder
public class LLMProviderConfig {

    /**
     * Provider type
     */
    private Provider provider;
    /**
     * Provider display name
     * Example: "OpenAI GPT-4", "Claude 3.5 Sonnet", "Local Llama 3"
     */
    private String name;
    /**
     * API endpoint URL
     * <p>
     * Examples:
     * - OpenAI: "https://api.openai.com/v1/chat/completions"
     * - Anthropic: "https://api.anthropic.com/v1/messages"
     * - Ollama: "http://localhost:11434/api/chat"
     * - Custom: "https://your-api.com/v1/chat"
     */
    private String url;

    // ========== Connection Settings ==========
    /**
     * API token/key for authentication
     * <p>
     * Examples:
     * - OpenAI: "sk-..."
     * - Anthropic: "sk-ant-..."
     * - Ollama: "" (no token needed)
     * - Custom: Your API key
     */
    private String token;
    /**
     * Default model to use
     * <p>
     * Examples:
     * - OpenAI: "gpt-4", "gpt-3.5-turbo"
     * - Anthropic: "claude-3-5-sonnet-20241022"
     * - Ollama: "llama3", "mistral"
     */
    private String model;

    // ========== Model Settings ==========
    /**
     * Does this provider support streaming?
     */
    @Builder.Default
    private Boolean supportsStreaming = false;

    // ========== Capabilities ==========
    /**
     * Does this provider support function calling?
     */
    @Builder.Default
    private Boolean supportsFunctionCalling = false;
    /**
     * Request timeout in seconds
     * Default: 30 seconds
     */
    @Builder.Default
    private Integer timeout = 30;

    // ========== Advanced Settings ==========
    /**
     * Maximum retry attempts on failure
     * Default: 3
     */
    @Builder.Default
    private Integer maxRetries = 3;
    /**
     * Custom HTTP headers
     * <p>
     * Example:
     * Map.of(
     * "anthropic-version", "2023-06-01",
     * "x-custom-header", "value"
     * )
     */
    private Map<String, String> headers;

    /**
     * Validate configuration
     *
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (provider == null) {
            throw new IllegalArgumentException("Provider is required");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Provider name is required");
        }

        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL is required");
        }

        // Token is optional for some providers (e.g., Ollama)
        if (provider != Provider.OLLAMA && (token == null || token.trim().isEmpty())) {
            throw new IllegalArgumentException("Token is required for " + provider);
        }
    }

    /**
     * Supported providers
     */
    public enum Provider {
        OPENAI("OpenAI"),
        ANTHROPIC("Anthropic"),
        OLLAMA("Ollama"),
        CUSTOM("Custom");

        private final String displayName;

        Provider(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}

