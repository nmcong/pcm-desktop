package com.noteflix.pcm.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Information about a specific LLM model.
 *
 * <p>Describes model capabilities, context window, pricing, etc. Used to list available models and
 * their characteristics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelInfo {

  /** Model identifier (e.g., "gpt-4-turbo-preview", "claude-3-5-sonnet-20241022"). */
  private String id;

  /** Human-readable model name. */
  private String name;

  /** Model description. */
  private String description;

  /** Provider name (e.g., "openai", "anthropic"). */
  private String provider;

  /** Context window size (max tokens: input + output). */
  private int contextWindow;

  /** Maximum output tokens. */
  private int maxOutputTokens;

  /** Does this model support tool/function calling? */
  @Builder.Default private boolean supportsTools = false;

  /** Does this model support thinking/reasoning mode? */
  @Builder.Default private boolean supportsThinking = false;

  /** Does this model support vision (image inputs)? */
  @Builder.Default private boolean supportsVision = false;

  /** Does this model support audio inputs? */
  @Builder.Default private boolean supportsAudio = false;

  /** Cost per 1K input tokens (USD). */
  private Double costPer1kInputTokens;

  /** Cost per 1K output tokens (USD). */
  private Double costPer1kOutputTokens;

  /** Training data cutoff date (if known). */
  private String knowledgeCutoff;

  /** Is this model deprecated? */
  @Builder.Default private boolean deprecated = false;

  /** Release date or version. */
  private String version;
}
