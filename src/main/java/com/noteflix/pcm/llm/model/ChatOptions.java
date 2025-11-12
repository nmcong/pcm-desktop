package com.noteflix.pcm.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Configuration options for LLM chat requests.
 * 
 * Controls various aspects of the LLM behavior including:
 * - Model selection
 * - Temperature (creativity)
 * - Token limits
 * - Streaming
 * - Tool/function calling
 * - Thinking mode (for reasoning models)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatOptions {
    
    /**
     * Model identifier (e.g., "gpt-4", "claude-3-5-sonnet-20241022", "llama2").
     * If null, uses provider's default model.
     */
    private String model;
    
    /**
     * Sampling temperature (0.0 to 2.0).
     * - 0.0: Deterministic, focused
     * - 1.0: Balanced (default)
     * - 2.0: Creative, random
     */
    @Builder.Default
    private double temperature = 0.7;
    
    /**
     * Maximum tokens in response.
     * Prevents runaway token costs and ensures responses fit in context.
     */
    @Builder.Default
    private int maxTokens = 2000;
    
    /**
     * Top-p sampling (nucleus sampling).
     * Alternative to temperature. Usually use one or the other.
     * Range: 0.0 to 1.0
     */
    private Double topP;
    
    /**
     * Frequency penalty (0.0 to 2.0).
     * Reduces repetition of tokens based on frequency in text so far.
     */
    private Double frequencyPenalty;
    
    /**
     * Presence penalty (0.0 to 2.0).
     * Reduces repetition of topics/ideas.
     */
    private Double presencePenalty;
    
    /**
     * Stop sequences.
     * Generation stops when any of these strings are encountered.
     */
    private List<String> stop;
    
    /**
     * Available tools/functions for the LLM to call.
     * If provided, LLM can request tool calls in response.
     */
    private List<Tool> tools;
    
    /**
     * Tool choice strategy.
     * - "none": Never call tools
     * - "auto": LLM decides
     * - "required": Must call at least one tool
     * - {name: "tool_name"}: Force specific tool
     */
    @Builder.Default
    private String toolChoice = "auto";
    
    /**
     * Enable streaming response.
     * If true, tokens arrive incrementally via ChatEventListener.
     */
    @Builder.Default
    private boolean stream = false;
    
    /**
     * Enable thinking mode (for reasoning models like o1, o3).
     * When true, model shows internal reasoning before answering.
     */
    @Builder.Default
    private boolean thinking = false;
    
    /**
     * Maximum tokens for thinking/reasoning.
     * Only used when thinking=true.
     */
    @Builder.Default
    private int maxThinkingTokens = 5000;
    
    /**
     * User identifier for tracking/analytics.
     */
    private String user;
    
    /**
     * Seed for deterministic sampling (if supported by provider).
     */
    private Integer seed;
    
    /**
     * Create default options.
     */
    public static ChatOptions defaults() {
        return ChatOptions.builder().build();
    }
    
    /**
     * Create options with specific model.
     */
    public static ChatOptions withModel(String model) {
        return ChatOptions.builder()
            .model(model)
            .build();
    }
    
    /**
     * Create options for streaming.
     */
    public static ChatOptions streaming() {
        return ChatOptions.builder()
            .stream(true)
            .build();
    }
    
    /**
     * Create options with tools.
     */
    public static ChatOptions withTools(List<Tool> tools) {
        return ChatOptions.builder()
            .tools(tools)
            .build();
    }
}

