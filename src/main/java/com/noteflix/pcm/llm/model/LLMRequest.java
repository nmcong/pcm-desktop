package com.noteflix.pcm.llm.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Universal LLM request model Compatible with OpenAI API format for maximum compatibility
 *
 * <p>Follows Builder Pattern for flexible construction
 *
 * <p>Example usage:
 *
 * <pre>
 * LLMRequest request = LLMRequest.builder()
 *     .model("gpt-4")
 *     .messages(List.of(
 *         Message.builder()
 *             .role(Message.Role.USER)
 *             .content("Hello!")
 *             .build()
 *     ))
 *     .temperature(0.7)
 *     .maxTokens(2000)
 *     .stream(true)
 *     .build();
 * </pre>
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Data
@Builder
public class LLMRequest {

    /**
     * Model name to use Examples: "gpt-4", "gpt-3.5-turbo", "claude-3-5-sonnet-20241022", "llama3"
     */
    private String model;

    /**
     * Conversation messages (system, user, assistant, function)
     */
    private List<Message> messages;

    // ========== Generation Parameters ==========

    /**
     * Temperature controls randomness Range: 0.0 (deterministic) to 2.0 (very random) Default: 0.7
     *
     * <p>Lower values (0.1-0.3): More focused, deterministic Medium values (0.7-0.9): Balanced
     * creativity Higher values (1.0-2.0): More creative, random
     */
    @Builder.Default
    private Double temperature = 0.7;

    /**
     * Maximum tokens in response Includes both prompt and completion Default: 2000
     */
    @Builder.Default
    private Integer maxTokens = 2000;

    /**
     * Top-p (nucleus sampling) Alternative to temperature Range: 0.0 to 1.0 Example: 0.1 means only
     * top 10% probability tokens
     */
    private Double topP;

    /**
     * Number of completions to generate Default: 1 Set to > 1 to get multiple responses
     */
    private Integer n;

    /**
     * Stop sequences Generation stops when these strings are encountered Example: List.of("\n",
     * "User:", "AI:")
     */
    private List<String> stop;

    /**
     * Presence penalty Range: -2.0 to 2.0 Positive values increase likelihood of new topics
     */
    private Double presencePenalty;

    /**
     * Frequency penalty Range: -2.0 to 2.0 Positive values decrease repetition
     */
    private Double frequencyPenalty;

    // ========== Function Calling ==========

    /**
     * Available functions for the LLM to call Only used if provider supports function calling
     */
    private List<FunctionDefinition> functions;

    /**
     * Control function calling behavior - "auto": LLM decides - "none": Disable function calling -
     * {"name": "function_name"}: Force specific function
     */
    private Object functionCall;

    // ========== Streaming ==========

    /**
     * Enable streaming mode Response will be sent in chunks (SSE) Default: false
     */
    @Builder.Default
    private Boolean stream = false;

    // ========== Custom Parameters ==========

    /**
     * Provider-specific custom parameters Allows passing custom params to specific providers
     *
     * <p>Example for Anthropic: Map.of("max_tokens_to_sample", 1000)
     */
    private Map<String, Object> customParams;

    /**
     * Validate request before sending
     *
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Model is required");
        }

        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("At least one message is required");
        }

        if (temperature != null && (temperature < 0 || temperature > 2)) {
            throw new IllegalArgumentException("Temperature must be between 0 and 2");
        }

        if (maxTokens != null && maxTokens < 1) {
            throw new IllegalArgumentException("Max tokens must be positive");
        }
    }
}
