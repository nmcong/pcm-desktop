package com.noteflix.pcm.llm.cache;

import lombok.extern.slf4j.Slf4j;

/**
 * Intelligent strategy that decides based on result size and context.
 *
 * <p>Rules: - Small results (< 500 tokens): Always full - Large results (> 2000 tokens): Always
 * summarize - Medium results: Depends on remaining context
 *
 * <p>This balances accuracy with token efficiency.
 */
@Slf4j
public class SmartSummarizationStrategy implements ToolResultCacheStrategy {

  private static final int SMALL_RESULT_THRESHOLD = 500; // tokens
  private static final int LARGE_RESULT_THRESHOLD = 2000; // tokens

  @Override
  public CacheDecision decide(ToolExecutionContext context) {
    int resultTokens = context.getResultTokenCount();
    int remainingTokens = context.getRemainingContextTokens();

    // Small results: always full
    if (resultTokens < SMALL_RESULT_THRESHOLD) {
      log.debug("Small result ({} tokens), sending full", resultTokens);
      return CacheDecision.builder()
          .shouldCache(true)
          .shouldSummarize(false)
          .summarizationStrategy("none")
          .reason("Result is small enough to send in full")
          .build();
    }

    // Large results: always summarize
    if (resultTokens > LARGE_RESULT_THRESHOLD) {
      log.debug("Large result ({} tokens), summarizing", resultTokens);
      return CacheDecision.builder()
          .shouldCache(true)
          .shouldSummarize(true)
          .summarizationStrategy("extractive")
          .reason("Result is too large, must summarize")
          .build();
    }

    // Medium results: depends on remaining context
    if (remainingTokens < resultTokens * 1.5) {
      log.debug(
          "Medium result ({} tokens) but limited context ({} remaining), summarizing",
          resultTokens,
          remainingTokens);
      return CacheDecision.builder()
          .shouldCache(true)
          .shouldSummarize(true)
          .summarizationStrategy("extractive")
          .reason("Not enough context window remaining")
          .build();
    }

    // Default: send full
    log.debug("Medium result ({} tokens) with sufficient context, sending full", resultTokens);
    return CacheDecision.builder()
        .shouldCache(true)
        .shouldSummarize(false)
        .summarizationStrategy("none")
        .reason("Sufficient context window available")
        .build();
  }

  @Override
  public String getStrategyName() {
    return "smart-summarization";
  }
}
