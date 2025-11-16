package com.noteflix.pcm.llm.cache;

/**
 * Strategy for deciding whether to cache and/or summarize tool results.
 *
 * <p>Different strategies optimize for different goals: - AlwaysFullStrategy: Accuracy first (no
 * summarization) - SmartSummarizationStrategy: Balance accuracy vs cost - TokenBudgetStrategy:
 * Strict token limits - AdaptiveStrategy: Learn from usage patterns
 */
public interface ToolResultCacheStrategy {

    /**
     * Decide how to handle a tool execution result.
     *
     * @param context Execution context with result size, remaining tokens, etc.
     * @return Decision on caching and summarization
     */
    CacheDecision decide(ToolExecutionContext context);

    /**
     * Get strategy name for logging/debugging.
     *
     * @return Strategy name
     */
    String getStrategyName();
}
