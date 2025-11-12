package com.noteflix.pcm.llm.cache;

import lombok.extern.slf4j.Slf4j;

/**
 * Strategy with strict token budget per tool result.
 * 
 * Use when:
 * - Need to control costs tightly
 * - Have hard token limits
 * - Want predictable behavior
 * 
 * Any result exceeding budget is automatically summarized.
 */
@Slf4j
public class TokenBudgetStrategy implements ToolResultCacheStrategy {
    
    private final int maxTokensPerToolResult;
    
    public TokenBudgetStrategy(int maxTokensPerToolResult) {
        this.maxTokensPerToolResult = maxTokensPerToolResult;
    }
    
    @Override
    public CacheDecision decide(ToolExecutionContext context) {
        int resultTokens = context.getResultTokenCount();
        
        if (resultTokens <= maxTokensPerToolResult) {
            log.debug("Result {} within budget ({}/{})", 
                context.getToolName(), resultTokens, maxTokensPerToolResult);
            return CacheDecision.builder()
                .shouldCache(true)
                .shouldSummarize(false)
                .summarizationStrategy("none")
                .reason("Within token budget")
                .build();
        }
        
        log.debug("Result {} exceeds budget ({}/{}), summarizing",
            context.getToolName(), resultTokens, maxTokensPerToolResult);
        return CacheDecision.builder()
            .shouldCache(true)
            .shouldSummarize(true)
            .summarizationStrategy("extractive")
            .reason("Exceeds token budget: " + maxTokensPerToolResult)
            .build();
    }
    
    @Override
    public String getStrategyName() {
        return "token-budget-" + maxTokensPerToolResult;
    }
}

