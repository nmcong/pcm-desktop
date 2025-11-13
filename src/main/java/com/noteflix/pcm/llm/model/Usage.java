package com.noteflix.pcm.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token usage statistics for an LLM request.
 *
 * <p>Tracks input tokens (prompt), output tokens (completion), and optionally thinking tokens (for
 * reasoning models).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usage {

  /** Tokens in the prompt (input). */
  private int promptTokens;

  /** Tokens in the completion (output). */
  private int completionTokens;

  /** Total tokens (prompt + completion + thinking). */
  private int totalTokens;

  /** Thinking/reasoning tokens (for o1/o3 models). Only present in models with thinking mode. */
  @Builder.Default private int thinkingTokens = 0;

  /** Estimated cost in USD. Calculated based on model pricing. */
  private Double estimatedCost;

  /** Calculate total tokens if not provided. */
  public int getTotalTokens() {
    if (totalTokens > 0) {
      return totalTokens;
    }
    return promptTokens + completionTokens + thinkingTokens;
  }
}
