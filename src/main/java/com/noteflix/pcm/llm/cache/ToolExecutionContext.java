package com.noteflix.pcm.llm.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Context information for cache strategy decisions.
 * 
 * Provides all the information needed to decide whether to cache
 * and/or summarize a tool result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolExecutionContext {
    
    /**
     * Tool name
     */
    private String toolName;
    
    /**
     * Tool arguments
     */
    private Map<String, Object> arguments;
    
    /**
     * Raw result from tool execution
     */
    private Object rawResult;
    
    /**
     * Estimated token count of result
     */
    private int resultTokenCount;
    
    /**
     * Remaining tokens in context window
     */
    private int remainingContextTokens;
    
    /**
     * Is this the last tool in a sequence?
     */
    private boolean isLastInSequence;
    
    /**
     * Additional metadata about the tool
     */
    private Map<String, Object> metadata;
}

