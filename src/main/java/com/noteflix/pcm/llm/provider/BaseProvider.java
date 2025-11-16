package com.noteflix.pcm.llm.provider;

import com.noteflix.pcm.llm.api.LLMProvider;
import com.noteflix.pcm.llm.api.TokenCounter;
import com.noteflix.pcm.llm.exception.ProviderException;
import com.noteflix.pcm.llm.model.*;
import com.noteflix.pcm.llm.token.DefaultTokenCounter;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * Base implementation for LLM providers.
 *
 * <p>Provides common functionality: - Token counting - Configuration management - Error handling -
 * Retry logic - Logging integration
 *
 * <p>Subclasses implement provider-specific API calls.
 */
@Slf4j
public abstract class BaseProvider implements LLMProvider {

    protected ProviderConfig config;
    protected TokenCounter tokenCounter;
    protected int maxRetries = 3;
    protected long retryDelayMs = 1000;

    public BaseProvider() {
        this.tokenCounter = new DefaultTokenCounter();
    }

    @Override
    public void configure(ProviderConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        this.config = config;
        this.maxRetries = config.getMaxRetries();
        log.info("Configured {} provider: model={}", getName(), config.getModel());
    }

    @Override
    public ProviderConfig getConfig() {
        return config;
    }

    @Override
    public int countTokens(String text) {
        return tokenCounter.count(text);
    }

    @Override
    public int countTokens(List<Message> messages) {
        return tokenCounter.count(messages);
    }

    @Override
    public TokenCounter getTokenCounter() {
        return tokenCounter;
    }

    @Override
    public void setTokenCounter(TokenCounter counter) {
        if (counter == null) {
            throw new IllegalArgumentException("TokenCounter cannot be null");
        }
        this.tokenCounter = counter;
        log.debug("Custom token counter set for {}", getName());
    }

    @Override
    public boolean isReady() {
        return config != null && config.getApiKey() != null && !config.getApiKey().isEmpty();
    }

    @Override
    public boolean testConnection() {
        if (!isReady()) {
            return false;
        }

        try {
            // Simple test with minimal token usage
            List<Message> testMessages = List.of(Message.user("test"));

            ChatOptions testOptions = ChatOptions.builder().model(config.getModel()).maxTokens(5).build();

            ChatResponse response = chat(testMessages, testOptions).get();
            return response != null;

        } catch (Exception e) {
            log.warn("Connection test failed for {}: {}", getName(), e.getMessage());
            return false;
        }
    }

    /**
     * Execute with retry logic.
     *
     * @param operation Operation to execute
     * @param <T>       Return type
     * @return Result
     * @throws ProviderException if all retries fail
     */
    protected <T> T executeWithRetry(RetryableOperation<T> operation) throws ProviderException {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < maxRetries) {
            try {
                return operation.execute();

            } catch (ProviderException e) {
                lastException = e;
                attempts++;

                // Don't retry on auth errors or client errors (4xx)
                if (e.isAuthError() || (e.getStatusCode() >= 400 && e.getStatusCode() < 500)) {
                    throw e;
                }

                // Retry on rate limits and server errors
                if (attempts < maxRetries) {
                    long delay = calculateBackoff(attempts);
                    log.warn(
                            "Request failed (attempt {}/{}), retrying in {}ms: {}",
                            attempts,
                            maxRetries,
                            delay,
                            e.getMessage());

                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                }
            }
        }

        throw new ProviderException(
                getName(),
                "Failed after "
                        + maxRetries
                        + " attempts: "
                        + (lastException != null ? lastException.getMessage() : "Unknown error"),
                lastException);
    }

    /**
     * Calculate exponential backoff delay.
     */
    protected long calculateBackoff(int attempt) {
        return retryDelayMs * (long) Math.pow(2, attempt - 1);
    }

    /**
     * Validate request before sending.
     */
    protected void validateRequest(List<Message> messages, ChatOptions options)
            throws ProviderException {
        if (messages == null || messages.isEmpty()) {
            throw new ProviderException(getName(), "Messages cannot be null or empty");
        }

        if (!isReady()) {
            throw new ProviderException(getName(), "Provider not configured properly");
        }

        // Check token limits
        ProviderCapabilities capabilities = getCapabilities();
        if (capabilities != null && capabilities.getMaxContextWindow() > 0) {
            int tokenCount = countTokens(messages);
            if (tokenCount > capabilities.getMaxContextWindow()) {
                throw new ProviderException(
                        getName(),
                        String.format(
                                "Token count (%d) exceeds context window (%d)",
                                tokenCount, capabilities.getMaxContextWindow()));
            }
        }
    }

    /**
     * Get default model for this provider.
     */
    protected abstract String getDefaultModel();

    /**
     * Functional interface for retryable operations.
     */
    @FunctionalInterface
    protected interface RetryableOperation<T> {
        T execute() throws ProviderException;
    }
}
