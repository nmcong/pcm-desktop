package com.noteflix.pcm.llm.logging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Statistics about LLM usage over a time period.
 * 
 * Useful for:
 * - Cost tracking
 * - Performance monitoring
 * - Usage analytics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogStatistics {
    
    /**
     * Total number of LLM calls
     */
    private int totalCalls;
    
    /**
     * Total tokens used (prompt + completion + thinking)
     */
    private long totalTokens;
    
    /**
     * Total estimated cost (USD)
     */
    private double totalCost;
    
    /**
     * Average call duration (milliseconds)
     */
    private long avgDurationMs;
    
    /**
     * Number of unique conversations
     */
    private int uniqueConversations;
    
    /**
     * Calls by provider (provider name -> count)
     */
    private Map<String, Integer> callsByProvider;
    
    /**
     * Calls by model (model name -> count)
     */
    private Map<String, Integer> callsByModel;
    
    /**
     * Tool usage count (tool name -> count)
     */
    private Map<String, Integer> toolUsageCount;
    
    /**
     * Number of calls with errors
     */
    private int errorCount;
    
    /**
     * Error rate (percentage)
     */
    public double getErrorRate() {
        return totalCalls > 0 ? (double) errorCount / totalCalls * 100 : 0.0;
    }
}

