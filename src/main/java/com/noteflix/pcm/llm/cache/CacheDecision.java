package com.noteflix.pcm.llm.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Decision from cache strategy about how to handle a tool result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheDecision {

    /**
     * Should we cache this result for future reuse?
     */
    @Builder.Default
    private boolean shouldCache = true;

    /**
     * Should we summarize before sending to LLM?
     */
    @Builder.Default
    private boolean shouldSummarize = false;

    /**
     * Summarization strategy to use (if shouldSummarize = true) - "none": No summarization -
     * "extractive": Extract key points (fast) - "llm": Use LLM to summarize (high quality)
     */
    @Builder.Default
    private String summarizationStrategy = "none";

    /**
     * Reason for this decision (for logging/debugging)
     */
    private String reason;
}
