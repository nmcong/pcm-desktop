package com.noteflix.pcm.llm.logging;

import com.noteflix.pcm.llm.model.ChatOptions;
import com.noteflix.pcm.llm.model.Message;
import com.noteflix.pcm.llm.model.Tool;
import com.noteflix.pcm.llm.model.Usage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Complete log entry for an LLM call.
 *
 * <p>Contains: - Request details (messages, options, tools) - Response details (content, thinking,
 * tool calls) - Execution metadata (timing, tokens, cost) - Error information (if any)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMCallLog {

    // ========== Identification ==========

    /**
     * Unique call ID
     */
    private String id;

    /**
     * Conversation ID (links multiple calls)
     */
    private String conversationId;

    /**
     * Provider name (openai, anthropic, ollama)
     */
    private String provider;

    /**
     * Model used
     */
    private String model;

    // ========== Timing ==========

    /**
     * When call started
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * How long it took (milliseconds)
     */
    private long durationMs;

    // ========== Request ==========

    /**
     * Request messages
     */
    private List<Message> requestMessages;

    /**
     * Request options
     */
    private ChatOptions requestOptions;

    /**
     * Available tools
     */
    private List<Tool> requestTools;

    // ========== Response ==========

    /**
     * Response text content
     */
    private String responseContent;

    /**
     * Thinking/reasoning content
     */
    private String thinkingContent;

    /**
     * Tool calls made by LLM
     */
    private List<ToolCallLog> toolCalls;

    /**
     * Token usage statistics
     */
    private Usage usage;

    /**
     * Finish reason (stop, length, tool_calls, etc.)
     */
    private String finishReason;

    // ========== Error Handling ==========

    /**
     * Whether an error occurred
     */
    @Builder.Default
    private boolean hasError = false;

    /**
     * Error message
     */
    private String errorMessage;

    /**
     * Error stack trace
     */
    private String errorStackTrace;

    // ========== Metadata ==========

    /**
     * User who made the call
     */
    private String userId;

    /**
     * Session ID
     */
    private String sessionId;

    /**
     * Custom metadata
     */
    private Map<String, Object> metadata;
}
