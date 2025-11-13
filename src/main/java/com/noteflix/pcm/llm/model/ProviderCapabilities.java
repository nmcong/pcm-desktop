package com.noteflix.pcm.llm.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Describes the capabilities of an LLM provider.
 *
 * <p>Different providers support different features: - Streaming - Function calling - Thinking mode
 * - Vision (image inputs) - Audio
 *
 * <p>Use this to check what features are available before using them.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderCapabilities {

  /** Provider name (e.g., "openai", "anthropic", "ollama"). */
  private String providerName;

  /** Does this provider support streaming responses? */
  @Builder.Default private boolean supportsStreaming = false;

  /** Does this provider support function/tool calling? */
  @Builder.Default private boolean supportsFunctionCalling = false;

  /** Does this provider support thinking/reasoning mode? */
  @Builder.Default private boolean supportsThinking = false;

  /** Does this provider support vision (image inputs)? */
  @Builder.Default private boolean supportsVision = false;

  /** Does this provider support audio inputs? */
  @Builder.Default private boolean supportsAudio = false;

  /** Does this provider support JSON mode (structured output)? */
  @Builder.Default private boolean supportsJsonMode = false;

  /** Maximum tokens this provider can handle in a request. */
  private int maxTokens;

  /** Maximum context window size (total tokens: input + output). */
  private int maxContextWindow;

  /**
   * List of supported features/capabilities. Can include custom capabilities beyond the boolean
   * flags.
   */
  private List<String> supportedFeatures;

  /** Additional provider-specific metadata. */
  private java.util.Map<String, Object> metadata;
}
