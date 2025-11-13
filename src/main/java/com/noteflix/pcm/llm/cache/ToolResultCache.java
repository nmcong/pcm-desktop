package com.noteflix.pcm.llm.cache;

import com.noteflix.pcm.llm.api.TokenCounter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * Cache for tool execution results.
 *
 * <p>Features: - Automatic caching based on strategy - Optional summarization to save tokens -
 * TTL-based expiration - Cache hit/miss tracking
 *
 * <p>Example usage:
 *
 * <pre>
 * ToolResultCache cache = new ToolResultCache(
 *     new SmartSummarizationStrategy(),
 *     tokenCounter
 * );
 *
 * ProcessedToolResult result = cache.process(context);
 * // Result is either full or summarized based on strategy
 * </pre>
 */
@Slf4j
public class ToolResultCache {

  private final Map<String, CachedToolResult> cache;
  private final ToolResultCacheStrategy strategy;
  private final TokenCounter tokenCounter;

  public ToolResultCache(ToolResultCacheStrategy strategy, TokenCounter tokenCounter) {
    this.cache = new ConcurrentHashMap<>();
    this.strategy = strategy;
    this.tokenCounter = tokenCounter;

    log.info("ToolResultCache initialized with strategy: {}", strategy.getStrategyName());
  }

  /**
   * Process a tool result according to cache strategy.
   *
   * @param context Tool execution context
   * @return Processed result (may be cached or summarized)
   */
  public ProcessedToolResult process(ToolExecutionContext context) {
    // Generate cache key
    String cacheKey = generateCacheKey(context.getToolName(), context.getArguments());

    // Check cache first
    CachedToolResult cached = cache.get(cacheKey);
    if (cached != null && !cached.isExpired()) {
      log.debug("Cache hit for tool: {}", context.getToolName());
      cached.setUseCount(cached.getUseCount() + 1);

      CacheDecision decision = strategy.decide(context);
      return ProcessedToolResult.builder()
          .rawResult(cached.getRawResult())
          .displayResult(
              decision.isShouldSummarize() && cached.getSummary() != null
                  ? cached.getSummary()
                  : cached.getRawResult().toString())
          .fromCache(true)
          .wasSummarized(decision.isShouldSummarize())
          .build();
    }

    // Not in cache, process new result
    CacheDecision decision = strategy.decide(context);

    String displayResult;
    String summary = null;
    int tokensSaved = 0;

    if (decision.isShouldSummarize()) {
      // Summarize result
      summary = summarizeResult(context.getRawResult(), decision.getSummarizationStrategy());
      displayResult = summary;

      int originalTokens = context.getResultTokenCount();
      int summaryTokens = tokenCounter.count(summary);
      tokensSaved = originalTokens - summaryTokens;

      log.debug(
          "Summarized tool result: {} -> {} tokens (saved {})",
          originalTokens,
          summaryTokens,
          tokensSaved);
    } else {
      // Use full result
      displayResult = context.getRawResult() != null ? context.getRawResult().toString() : "";
    }

    // Cache if requested
    if (decision.isShouldCache()) {
      CachedToolResult cacheEntry =
          CachedToolResult.builder()
              .toolName(context.getToolName())
              .arguments(context.getArguments())
              .rawResult(context.getRawResult())
              .summary(summary)
              .build();

      cache.put(cacheKey, cacheEntry);
      log.debug("Cached tool result: {}", cacheKey);
    }

    return ProcessedToolResult.builder()
        .rawResult(context.getRawResult())
        .displayResult(displayResult)
        .fromCache(false)
        .wasSummarized(decision.isShouldSummarize())
        .tokensSaved(tokensSaved)
        .build();
  }

  /** Summarize a result based on strategy. */
  private String summarizeResult(Object result, String summarizationStrategy) {
    if (result == null) {
      return "";
    }

    String fullText = result.toString();

    switch (summarizationStrategy) {
      case "extractive":
        // Simple extractive summarization: first N chars + "..."
        int maxLength = 500;
        if (fullText.length() <= maxLength) {
          return fullText;
        }
        return fullText.substring(0, maxLength) + "... [truncated]";

      case "llm":
        // TODO: Implement LLM-based summarization
        log.warn("LLM summarization not yet implemented, using extractive");
        return summarizeResult(result, "extractive");

      case "none":
      default:
        return fullText;
    }
  }

  /** Generate cache key from tool name and arguments. */
  private String generateCacheKey(String toolName, Map<String, Object> args) {
    // Simple key: toolName + sorted args
    StringBuilder key = new StringBuilder(toolName);
    if (args != null) {
      args.entrySet().stream()
          .sorted(Map.Entry.comparingByKey())
          .forEach(
              entry -> key.append(":").append(entry.getKey()).append("=").append(entry.getValue()));
    }
    return key.toString();
  }

  /** Clear all cached results. */
  public void clear() {
    cache.clear();
    log.info("Cache cleared");
  }

  /** Remove expired cache entries. */
  public int cleanupExpired() {
    int removed = 0;
    for (Map.Entry<String, CachedToolResult> entry : cache.entrySet()) {
      if (entry.getValue().isExpired()) {
        cache.remove(entry.getKey());
        removed++;
      }
    }

    if (removed > 0) {
      log.info("Removed {} expired cache entries", removed);
    }

    return removed;
  }

  /** Get cache statistics. */
  public CacheStatistics getStatistics() {
    int totalEntries = cache.size();
    int expiredEntries = 0;
    int totalReuses = 0;

    for (CachedToolResult result : cache.values()) {
      if (result.isExpired()) {
        expiredEntries++;
      }
      totalReuses += result.getUseCount();
    }

    return CacheStatistics.builder()
        .totalEntries(totalEntries)
        .expiredEntries(expiredEntries)
        .totalReuses(totalReuses)
        .hitRate(totalReuses > 0 ? (double) totalReuses / (totalReuses + totalEntries) : 0.0)
        .build();
  }

  @lombok.Data
  @lombok.Builder
  public static class CacheStatistics {
    private int totalEntries;
    private int expiredEntries;
    private int totalReuses;
    private double hitRate;
  }
}
