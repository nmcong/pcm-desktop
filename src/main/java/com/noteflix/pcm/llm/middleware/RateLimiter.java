package com.noteflix.pcm.llm.middleware;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * Rate limiter using Token Bucket algorithm
 * <p>
 * Prevents exceeding API rate limits by controlling request frequency.
 * Each provider can have different limits.
 * <p>
 * Features:
 * - Token bucket algorithm
 * - Per-provider limits
 * - Configurable refill rate
 * - Thread-safe
 * <p>
 * Example usage:
 * <pre>
 * RateLimiter limiter = new RateLimiter(10, Duration.ofSeconds(1)); // 10 requests per second
 * limiter.acquire("openai"); // Blocks if rate limit exceeded
 * </pre>
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class RateLimiter {

    private final int maxTokens;
    private final Duration refillInterval;
    private final ConcurrentHashMap<String, TokenBucket> buckets;

    /**
     * Create a rate limiter
     *
     * @param maxTokens      Maximum number of tokens (requests) in bucket
     * @param refillInterval Time interval for refilling tokens
     */
    public RateLimiter(int maxTokens, Duration refillInterval) {
        this.maxTokens = maxTokens;
        this.refillInterval = refillInterval;
        this.buckets = new ConcurrentHashMap<>();
        log.info("RateLimiter initialized: {} tokens per {}", maxTokens, refillInterval);
    }

    /**
     * Create a rate limiter for OpenAI (default: 10 req/min for free tier)
     */
    public static RateLimiter forOpenAI() {
        return new RateLimiter(10, Duration.ofMinutes(1));
    }

    /**
     * Create a rate limiter for Anthropic (default: 5 req/min for free tier)
     */
    public static RateLimiter forAnthropic() {
        return new RateLimiter(5, Duration.ofMinutes(1));
    }

    /**
     * Create a rate limiter for local Ollama (unlimited)
     */
    public static RateLimiter forOllama() {
        return new RateLimiter(1000, Duration.ofSeconds(1));
    }

    /**
     * Acquire permission to make a request
     * Blocks if rate limit is exceeded
     *
     * @param provider Provider name (e.g., "openai", "anthropic")
     * @return true if permission granted, false if interrupted
     */
    public boolean acquire(String provider) {
        return acquire(provider, 1);
    }

    /**
     * Acquire multiple tokens
     *
     * @param provider Provider name
     * @param tokens   Number of tokens to acquire
     * @return true if permission granted, false if interrupted
     */
    public boolean acquire(String provider, int tokens) {
        TokenBucket bucket = getBucket(provider);

        try {
            bucket.refill();
            boolean acquired = bucket.tryAcquire(tokens);

            if (!acquired) {
                log.debug("Rate limit reached for provider: {}. Waiting...", provider);
                // Wait for refill
                long waitTime = refillInterval.toMillis();
                Thread.sleep(waitTime);
                bucket.refill();
                acquired = bucket.tryAcquire(tokens);
            }

            if (acquired) {
                log.trace("Acquired {} token(s) for provider: {}", tokens, provider);
            }

            return acquired;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Rate limiter interrupted for provider: {}", provider);
            return false;
        }
    }

    /**
     * Try to acquire without blocking
     *
     * @param provider Provider name
     * @return true if acquired, false if rate limit exceeded
     */
    public boolean tryAcquire(String provider) {
        TokenBucket bucket = getBucket(provider);
        bucket.refill();
        return bucket.tryAcquire(1);
    }

    /**
     * Get remaining tokens for a provider
     *
     * @param provider Provider name
     * @return Number of available tokens
     */
    public int getAvailableTokens(String provider) {
        TokenBucket bucket = getBucket(provider);
        bucket.refill();
        return bucket.getAvailableTokens();
    }

    /**
     * Reset rate limiter for a provider
     *
     * @param provider Provider name
     */
    public void reset(String provider) {
        buckets.remove(provider);
        log.debug("Reset rate limiter for provider: {}", provider);
    }

    /**
     * Reset all rate limiters
     */
    public void resetAll() {
        buckets.clear();
        log.debug("Reset all rate limiters");
    }

    private TokenBucket getBucket(String provider) {
        return buckets.computeIfAbsent(provider,
                k -> new TokenBucket(maxTokens, refillInterval));
    }

    /**
     * Token bucket implementation
     */
    private static class TokenBucket {
        private final int capacity;
        private final Duration refillInterval;
        private final Semaphore semaphore;
        private Instant lastRefillTime;

        TokenBucket(int capacity, Duration refillInterval) {
            this.capacity = capacity;
            this.refillInterval = refillInterval;
            this.semaphore = new Semaphore(capacity);
            this.lastRefillTime = Instant.now();
        }

        void refill() {
            Instant now = Instant.now();
            Duration elapsed = Duration.between(lastRefillTime, now);

            if (elapsed.compareTo(refillInterval) >= 0) {
                // Calculate how many tokens to add
                long intervals = elapsed.toMillis() / refillInterval.toMillis();
                int tokensToAdd = (int) Math.min(intervals, capacity - semaphore.availablePermits());

                if (tokensToAdd > 0) {
                    semaphore.release(tokensToAdd);
                    lastRefillTime = now;
                }
            }
        }

        boolean tryAcquire(int tokens) {
            return semaphore.tryAcquire(tokens);
        }

        int getAvailableTokens() {
            return semaphore.availablePermits();
        }
    }
}

