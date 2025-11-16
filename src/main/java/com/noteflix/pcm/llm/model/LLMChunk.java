package com.noteflix.pcm.llm.model;

import lombok.Builder;
import lombok.Data;

/**
 * A chunk of streaming response
 *
 * <p>When streaming is enabled, responses arrive in chunks (SSE). Each chunk contains a piece of
 * the generated text.
 *
 * <p>Example streaming flow:
 *
 * <pre>
 * Chunk 1: "Hello"
 * Chunk 2: " there"
 * Chunk 3: "!"
 * Chunk 4: " How"
 * Chunk 5: " can"
 * ...
 * </pre>
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Data
@Builder
public class LLMChunk {

    /**
     * Chunk ID
     */
    private String id;

    /**
     * Model that generated this chunk
     */
    private String model;

    /**
     * Content delta (new text in this chunk) Append this to previous chunks to build full response
     */
    private String content;

    /**
     * Finish reason (only on last chunk) - "stop": Natural completion - "length": Hit max tokens -
     * null: More chunks coming
     */
    private String finishReason;

    /**
     * Function call (for function calling with streaming) May be present on last chunk if LLM wants
     * to call a function
     */
    private FunctionCall functionCall;

    /**
     * Chunk index (0-based) First chunk is 0, second is 1, etc.
     */
    private Integer index;

    /**
     * Check if this is the last chunk
     *
     * @return true if this is the final chunk
     */
    public boolean isLast() {
        return finishReason != null;
    }

    /**
     * Check if chunk contains content
     *
     * @return true if content is not empty
     */
    public boolean hasContent() {
        return content != null && !content.isEmpty();
    }
}
