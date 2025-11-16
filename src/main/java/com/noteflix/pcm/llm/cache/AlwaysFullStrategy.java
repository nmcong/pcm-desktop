package com.noteflix.pcm.llm.cache;

/**
 * Cache strategy that always sends full results (no summarization).
 *
 * <p>Use when: - Accuracy is critical - Token cost is not a concern - Results are typically small
 *
 * <p>This is the default strategy (simplest, safest).
 */
public class AlwaysFullStrategy implements ToolResultCacheStrategy {

    @Override
    public CacheDecision decide(ToolExecutionContext context) {
        return CacheDecision.builder()
                .shouldCache(true)
                .shouldSummarize(false)
                .summarizationStrategy("none")
                .reason("AlwaysFullStrategy: Always send full results for maximum accuracy")
                .build();
    }

    @Override
    public String getStrategyName() {
        return "always-full";
    }
}
