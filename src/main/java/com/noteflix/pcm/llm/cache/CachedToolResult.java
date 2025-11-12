package com.noteflix.pcm.llm.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Cached tool execution result.
 * 
 * Stores both the raw result and optional summarized version,
 * along with cache metadata (TTL, use count, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CachedToolResult {
    
    /**
     * Tool name
     */
    private String toolName;
    
    /**
     * Tool arguments
     */
    private Map<String, Object> arguments;
    
    /**
     * Raw result (full)
     */
    private Object rawResult;
    
    /**
     * Summarized version (if available)
     */
    private String summary;
    
    /**
     * When this was cached
     */
    @Builder.Default
    private LocalDateTime cachedAt = LocalDateTime.now();
    
    /**
     * Time to live (how long until expired)
     */
    @Builder.Default
    private Duration ttl = Duration.ofHours(1);
    
    /**
     * How many times this cache entry was reused
     */
    @Builder.Default
    private int useCount = 0;
    
    /**
     * Check if cache entry is expired.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(cachedAt.plus(ttl));
    }
}

