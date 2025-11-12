package com.noteflix.pcm.llm.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result after processing through cache.
 * 
 * Contains:
 * - Raw result (for application use)
 * - Display result (what to send to LLM - may be summarized)
 * - Cache metadata (was it from cache? was it summarized?)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedToolResult {
    
    /**
     * Original raw result from tool execution
     */
    private Object rawResult;
    
    /**
     * Result to display/send to LLM (may be summarized)
     */
    private String displayResult;
    
    /**
     * Was this result retrieved from cache?
     */
    @Builder.Default
    private boolean fromCache = false;
    
    /**
     * Was the result summarized?
     */
    @Builder.Default
    private boolean wasSummarized = false;
    
    /**
     * How many tokens were saved by summarization
     */
    @Builder.Default
    private int tokensSaved = 0;
}

