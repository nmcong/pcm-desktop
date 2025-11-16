package com.noteflix.pcm.llm.middleware;

import com.noteflix.pcm.llm.exception.LLMException;
import com.noteflix.pcm.llm.exception.LLMProviderException;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * Retry policy with exponential backoff
 *
 * <p>Automatically retries failed requests with configurable backoff strategy. Useful for handling
 * transient network errors and rate limits.
 *
 * <p>Features: - Exponential backoff - Configurable max retries - Custom retry conditions - Jitter
 * to prevent thundering herd
 *
 * <p>Example usage:
 *
 * <pre>
 * RetryPolicy policy = RetryPolicy.builder()
 *     .maxRetries(3)
 *     .initialDelay(Duration.ofSeconds(1))
 *     .maxDelay(Duration.ofSeconds(30))
 *     .build();
 *
 * String result = policy.execute(() -> llmClient.sendMessage(request));
 * </pre>
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
@Data
@Builder
public class RetryPolicy {

    /**
     * Maximum number of retry attempts Default: 3
     */
    @Builder.Default
    private int maxRetries = 3;

    /**
     * Initial delay before first retry Default: 1 second
     */
    @Builder.Default
    private Duration initialDelay = Duration.ofSeconds(1);

    /**
     * Maximum delay between retries Default: 30 seconds
     */
    @Builder.Default
    private Duration maxDelay = Duration.ofSeconds(30);

    /**
     * Backoff multiplier Each retry delay = previous delay * multiplier Default: 2.0 (exponential)
     */
    @Builder.Default
    private double backoffMultiplier = 2.0;

    /**
     * Add random jitter to prevent thundering herd Default: true
     */
    @Builder.Default
    private boolean useJitter = true;

    /**
     * Predicate to determine if an exception should be retried Default: retry on LLMProviderException
     * with 5xx status codes
     */
    @Builder.Default
    private Predicate<Throwable> retryCondition = RetryPolicy::isRetryableException;

    /**
     * Default retry condition Retries on: - Network errors (IOException) - Server errors (5xx status
     * codes) - Rate limit errors (429 status code)
     *
     * <p>Does NOT retry on: - Client errors (4xx except 429) - Invalid requests
     */
    private static boolean isRetryableException(Throwable throwable) {
        // Retry on network errors
        if (throwable instanceof java.io.IOException) {
            return true;
        }

        // Retry on provider exceptions with specific status codes
        if (throwable instanceof LLMProviderException) {
            LLMProviderException providerEx = (LLMProviderException) throwable;
            int statusCode = providerEx.getStatusCode();

            // Retry on server errors (5xx)
            if (statusCode >= 500 && statusCode < 600) {
                return true;
            }

            // Retry on rate limit (429)
            if (statusCode == 429) {
                return true;
            }

            // Retry on timeout (408)
            if (statusCode == 408) {
                return true;
            }
        }

        return false;
    }

    /**
     * Create a default retry policy (3 retries, exponential backoff)
     */
    public static RetryPolicy defaultPolicy() {
        return RetryPolicy.builder().build();
    }

    /**
     * Create an aggressive retry policy (5 retries, faster backoff)
     */
    public static RetryPolicy aggressive() {
        return RetryPolicy.builder()
                .maxRetries(5)
                .initialDelay(Duration.ofMillis(500))
                .maxDelay(Duration.ofSeconds(15))
                .backoffMultiplier(1.5)
                .build();
    }

    /**
     * Create a conservative retry policy (2 retries, slower backoff)
     */
    public static RetryPolicy conservative() {
        return RetryPolicy.builder()
                .maxRetries(2)
                .initialDelay(Duration.ofSeconds(2))
                .maxDelay(Duration.ofMinutes(1))
                .backoffMultiplier(3.0)
                .build();
    }

    /**
     * Create a no-retry policy (for debugging)
     */
    public static RetryPolicy noRetry() {
        return RetryPolicy.builder().maxRetries(0).build();
    }

    /**
     * Execute a callable with retry logic
     *
     * @param callable The operation to execute
     * @param <T>      Return type
     * @return Result of the operation
     * @throws LLMException if all retries fail
     */
    public <T> T execute(Callable<T> callable) throws LLMException {
        int attempt = 0;
        Throwable lastException = null;

        while (attempt <= maxRetries) {
            try {
                if (attempt > 0) {
                    log.info("Retry attempt {}/{}", attempt, maxRetries);
                }

                return callable.call();

            } catch (Exception e) {
                lastException = e;
                attempt++;

                // Check if we should retry
                if (!retryCondition.test(e)) {
                    log.debug("Exception not retryable: {}", e.getMessage());
                    throw new LLMException("Operation failed", e);
                }

                // Check if we've exhausted retries
                if (attempt > maxRetries) {
                    log.error("All retry attempts exhausted ({}/{})", attempt - 1, maxRetries);
                    break;
                }

                // Calculate delay
                long delayMs = calculateDelay(attempt);

                log.warn(
                        "Request failed (attempt {}/{}): {}. Retrying in {}ms...",
                        attempt,
                        maxRetries,
                        e.getMessage(),
                        delayMs);

                // Wait before retry
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new LLMException("Retry interrupted", ie);
                }
            }
        }

        // All retries failed
        throw new LLMException(
                String.format("Operation failed after %d retries", maxRetries), lastException);
    }

    /**
     * Calculate delay for a specific retry attempt
     *
     * @param attempt Retry attempt number (1-based)
     * @return Delay in milliseconds
     */
    private long calculateDelay(int attempt) {
        // Exponential backoff: delay = initialDelay * (multiplier ^ (attempt - 1))
        long delay = (long) (initialDelay.toMillis() * Math.pow(backoffMultiplier, attempt - 1));

        // Cap at max delay
        delay = Math.min(delay, maxDelay.toMillis());

        // Add jitter (random ±25%)
        if (useJitter) {
            double jitterFactor = 0.75 + (Math.random() * 0.5); // 0.75 to 1.25
            delay = (long) (delay * jitterFactor);
        }

        return delay;
    }
}
