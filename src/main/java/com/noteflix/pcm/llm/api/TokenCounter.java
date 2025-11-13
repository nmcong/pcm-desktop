package com.noteflix.pcm.llm.api;

import com.noteflix.pcm.llm.model.Message;
import java.util.List;

/**
 * Interface for counting tokens in text and messages. Different LLM providers may have different
 * tokenization schemes.
 */
public interface TokenCounter {

  /**
   * Count tokens in a text string (accurate)
   *
   * @param text Text to count tokens for
   * @return Number of tokens
   */
  int count(String text);

  /**
   * Count tokens in a list of messages Includes overhead for message formatting
   *
   * @param messages List of messages
   * @return Total number of tokens
   */
  int count(List<Message> messages);

  /**
   * Quick estimation of tokens (fast approximation) Use when exact count is not critical
   *
   * @param text Text to estimate tokens for
   * @return Estimated number of tokens
   */
  int estimate(String text);

  /**
   * Get the name of this token counter
   *
   * @return Token counter name (e.g., "gpt-4", "claude", "default")
   */
  String getName();
}
