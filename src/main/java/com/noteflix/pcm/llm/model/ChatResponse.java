package com.noteflix.pcm.llm.model;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from an LLM chat request.
 *
 * <p>Contains the generated content, token usage statistics, and optionally tool calls or thinking
 * content.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

  /** Unique identifier for this response. */
  private String id;

  /** Provider that generated this response (e.g., "openai", "anthropic"). */
  private String provider;

  /** Model that generated this response. */
  private String model;

  /**
   * Response content (the actual text generated). May be null if response contains only tool calls.
   */
  private String content;

  /**
   * Thinking/reasoning content (for reasoning models). This is the internal thought process, not
   * the final answer.
   */
  private String thinkingContent;

  /** Tool calls requested by the LLM. Can be multiple calls in a single response. */
  private List<ToolCall> toolCalls;

  /** Token usage statistics. */
  private Usage usage;

  /**
   * Finish reason. - "stop": Natural completion - "length": Hit max tokens - "tool_calls": Stopped
   * to call tools - "content_filter": Filtered by safety system
   */
  private String finishReason;

  /** Timestamp when response was generated. */
  @Builder.Default private LocalDateTime timestamp = LocalDateTime.now();

  /** Response latency in milliseconds. */
  private Long latencyMs;

  /** Check if response contains tool calls. */
  public boolean hasToolCalls() {
    return toolCalls != null && !toolCalls.isEmpty();
  }

  /** Check if response contains multiple tool calls. */
  public boolean hasMultipleToolCalls() {
    return toolCalls != null && toolCalls.size() > 1;
  }

  /** Check if response has thinking content. */
  public boolean hasThinking() {
    return thinkingContent != null && !thinkingContent.isEmpty();
  }

  /** Check if response has text content. */
  public boolean hasContent() {
    return content != null && !content.isEmpty();
  }

  /** Get total tokens used (convenience method). */
  public int getTotalTokens() {
    return usage != null ? usage.getTotalTokens() : 0;
  }
}
