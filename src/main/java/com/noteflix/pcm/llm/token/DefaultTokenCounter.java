package com.noteflix.pcm.llm.token;

import com.noteflix.pcm.llm.api.TokenCounter;
import com.noteflix.pcm.llm.model.Message;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Default token counter using character-based approximation.
 *
 * <p>This is a simple approximation based on average character-to-token ratios: - English: ~4
 * characters per token - Code: ~3.5 characters per token - Chinese/Japanese: ~2 characters per
 * token
 *
 * <p>For production use with specific models, consider using model-specific counters.
 */
@Slf4j
public class DefaultTokenCounter implements TokenCounter {

  private static final double CHARS_PER_TOKEN = 4.0;
  private static final int MESSAGE_OVERHEAD = 4; // Tokens per message for formatting

  @Override
  public int count(String text) {
    if (text == null || text.isEmpty()) {
      return 0;
    }

    // Character-based approximation
    int estimate = (int) Math.ceil(text.length() / CHARS_PER_TOKEN);

    log.trace("Token count for text ({} chars): ~{} tokens", text.length(), estimate);

    return estimate;
  }

  @Override
  public int count(List<Message> messages) {
    if (messages == null || messages.isEmpty()) {
      return 0;
    }

    int totalTokens = 0;

    for (Message message : messages) {
      // Count content tokens
      if (message.getContent() != null) {
        totalTokens += count(message.getContent());
      }

      // Count name tokens if present
      if (message.getName() != null) {
        totalTokens += count(message.getName());
      }

      // Add message overhead (role, formatting, etc.)
      totalTokens += MESSAGE_OVERHEAD;
    }

    log.debug("Total token count for {} messages: ~{} tokens", messages.size(), totalTokens);

    return totalTokens;
  }

  @Override
  public int estimate(String text) {
    // For default counter, estimate is same as count
    return count(text);
  }

  @Override
  public String getName() {
    return "default";
  }
}
