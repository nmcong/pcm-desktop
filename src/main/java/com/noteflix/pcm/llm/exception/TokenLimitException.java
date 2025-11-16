package com.noteflix.pcm.llm.exception;

/**
 * Exception thrown when token limit is exceeded.
 */
public class TokenLimitException extends LLMException {

    private final int requestedTokens;
    private final int maxTokens;

    public TokenLimitException(int requestedTokens, int maxTokens) {
        super(
                String.format(
                        "Token limit exceeded: %d requested, %d maximum", requestedTokens, maxTokens));
        this.requestedTokens = requestedTokens;
        this.maxTokens = maxTokens;
    }

    public int getRequestedTokens() {
        return requestedTokens;
    }

    public int getMaxTokens() {
        return maxTokens;
    }
}
