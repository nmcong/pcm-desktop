package com.noteflix.pcm.llm.token;

import com.noteflix.pcm.llm.api.TokenCounter;
import com.noteflix.pcm.llm.model.Message;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages conversation context to fit within token limits. Provides strategies for trimming
 * messages when context is too large.
 */
@Slf4j
public class ContextWindowManager {

  private final TokenCounter tokenCounter;

  public ContextWindowManager(TokenCounter tokenCounter) {
    this.tokenCounter = tokenCounter;
  }

  /**
   * Fit messages to a maximum token count by removing oldest messages. Always preserves system
   * messages.
   *
   * @param messages Original messages
   * @param maxTokens Maximum tokens allowed
   * @return Trimmed message list
   */
  public List<Message> fitToWindow(List<Message> messages, int maxTokens) {
    if (messages == null || messages.isEmpty()) {
      return new ArrayList<>();
    }

    int currentTokens = tokenCounter.count(messages);

    if (currentTokens <= maxTokens) {
      log.debug("Messages fit in window: {} <= {} tokens", currentTokens, maxTokens);
      return new ArrayList<>(messages);
    }

    log.info("Messages exceed window: {} > {} tokens. Trimming...", currentTokens, maxTokens);

    return trimOldest(messages, maxTokens);
  }

  /**
   * Trim oldest non-system messages until under target token count.
   *
   * <p>Strategy: 1. Keep all SYSTEM messages 2. Keep the most recent messages 3. Remove oldest
   * messages until under target
   *
   * @param messages Original messages
   * @param targetTokens Target token count
   * @return Trimmed messages
   */
  public List<Message> trimOldest(List<Message> messages, int targetTokens) {
    List<Message> result = new ArrayList<>();

    // First pass: collect system messages (always keep)
    List<Message> systemMessages = new ArrayList<>();
    List<Message> nonSystemMessages = new ArrayList<>();

    for (Message message : messages) {
      if (message.getRole() == Message.Role.SYSTEM) {
        systemMessages.add(message);
      } else {
        nonSystemMessages.add(message);
      }
    }

    // Add system messages first
    result.addAll(systemMessages);

    int currentTokens = tokenCounter.count(result);
    int remainingTokens = targetTokens - currentTokens;

    if (remainingTokens <= 0) {
      log.warn("System messages alone exceed target! {} tokens", currentTokens);
      return result;
    }

    // Add messages from newest to oldest until we hit the limit
    for (int i = nonSystemMessages.size() - 1; i >= 0; i--) {
      Message message = nonSystemMessages.get(i);
      int messageTokens = tokenCounter.count(message.getContent());

      if (currentTokens + messageTokens <= targetTokens) {
        result.add(message);
        currentTokens += messageTokens;
      } else {
        log.debug("Trimmed message {} (would exceed limit)", i);
        break;
      }
    }

    log.info(
        "Trimmed from {} to {} messages ({} tokens)",
        messages.size(),
        result.size(),
        currentTokens);

    return result;
  }

  /**
   * Calculate remaining tokens in context window
   *
   * @param messages Current messages
   * @param maxTokens Maximum context window
   * @return Remaining tokens available
   */
  public int getRemainingTokens(List<Message> messages, int maxTokens) {
    int used = tokenCounter.count(messages);
    return Math.max(0, maxTokens - used);
  }

  /**
   * Check if messages fit within window
   *
   * @param messages Messages to check
   * @param maxTokens Maximum tokens
   * @return true if fits, false otherwise
   */
  public boolean fitsInWindow(List<Message> messages, int maxTokens) {
    return tokenCounter.count(messages) <= maxTokens;
  }
}
