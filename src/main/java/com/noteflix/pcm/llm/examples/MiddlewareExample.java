package com.noteflix.pcm.llm.examples;

import com.noteflix.pcm.llm.middleware.RateLimiter;
import com.noteflix.pcm.llm.middleware.RequestLogger;
import com.noteflix.pcm.llm.middleware.RetryPolicy;
import com.noteflix.pcm.llm.model.LLMProviderConfig;
import com.noteflix.pcm.llm.model.LLMRequest;
import com.noteflix.pcm.llm.model.LLMResponse;
import com.noteflix.pcm.llm.model.Message;
import com.noteflix.pcm.llm.service.LLMService;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Examples of using middleware components
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class MiddlewareExample {

  public static void main(String[] args) {
    // Example 1: Rate Limiting
    rateLimiterExample();

    // Example 2: Retry Policy
    // retryPolicyExample();

    // Example 3: Request Logging
    // requestLoggingExample();

    // Example 4: Complete Stack
    // completeStackExample();
  }

  /** Example 1: Rate Limiting */
  public static void rateLimiterExample() {
    log.info("=== Example 1: Rate Limiting ===");

    // Create rate limiter: 3 requests per 10 seconds
    RateLimiter rateLimiter = new RateLimiter(3, Duration.ofSeconds(10));

    // Make 5 requests (will rate limit after 3)
    for (int i = 1; i <= 5; i++) {
      log.info("Request {} - Available tokens: {}", i, rateLimiter.getAvailableTokens("openai"));

      boolean acquired = rateLimiter.acquire("openai");

      if (acquired) {
        log.info("✅ Request {} allowed", i);
      } else {
        log.info("❌ Request {} rate limited", i);
      }
    }
  }

  /** Example 2: Retry Policy */
  public static void retryPolicyExample() {
    log.info("=== Example 2: Retry Policy ===");

    // Create retry policy
    RetryPolicy retryPolicy =
        RetryPolicy.builder()
            .maxRetries(3)
            .initialDelay(Duration.ofSeconds(1))
            .maxDelay(Duration.ofSeconds(10))
            .build();

    try {
      // Simulate a request that might fail
      String result =
          retryPolicy.execute(
              () -> {
                log.info("Attempting request...");

                // Simulate random failure
                if (Math.random() < 0.7) {
                  throw new RuntimeException("Simulated failure");
                }

                return "Success!";
              });

      log.info("✅ Result: {}", result);

    } catch (Exception e) {
      log.error("❌ All retries failed: {}", e.getMessage());
    }
  }

  /** Example 3: Request Logging */
  public static void requestLoggingExample() {
    log.info("=== Example 3: Request Logging ===");

    // Create verbose logger
    RequestLogger logger = RequestLogger.verbose();

    // Simulate some requests
    for (int i = 1; i <= 5; i++) {
      LLMRequest request =
          LLMRequest.builder()
              .model("gpt-3.5-turbo")
              .messages(List.of(Message.user("Test message " + i)))
              .build();

      // Log request
      String requestId = logger.logRequest("openai", request);

      try {
        // Simulate processing
        Thread.sleep(100);

        // Simulate response
        LLMResponse response =
            LLMResponse.builder()
                .id(requestId)
                .model("gpt-3.5-turbo")
                .content("Response " + i)
                .usage(
                    LLMResponse.Usage.builder()
                        .promptTokens(10)
                        .completionTokens(20)
                        .totalTokens(30)
                        .build())
                .build();

        // Log response
        logger.logResponse(requestId, response);

      } catch (Exception e) {
        logger.logError(requestId, e);
      }
    }

    // Print metrics summary
    logger.printMetrics();
  }

  /** Example 4: Complete Stack with All Middleware */
  public static void completeStackExample() {
    log.info("=== Example 4: Complete Middleware Stack ===");

    // Setup middleware
    RateLimiter rateLimiter = RateLimiter.forOpenAI();
    RetryPolicy retryPolicy = RetryPolicy.defaultPolicy();
    RequestLogger logger = RequestLogger.defaultLogger();

    // Setup LLM service
    LLMService service = new LLMService();
    service.initialize(
        LLMProviderConfig.builder()
            .provider(LLMProviderConfig.Provider.OPENAI)
            .url("https://api.openai.com/v1/chat/completions")
            .token(System.getenv("OPENAI_API_KEY"))
            .model("gpt-3.5-turbo")
            .build());

    // Create request
    LLMRequest request =
        LLMRequest.builder()
            .model("gpt-3.5-turbo")
            .messages(List.of(Message.user("What is the capital of France?")))
            .temperature(0.7)
            .maxTokens(100)
            .build();

    try {
      // 1. Check rate limit
      if (!rateLimiter.acquire("openai")) {
        log.error("Rate limit exceeded!");
        return;
      }

      // 2. Log request
      String requestId = logger.logRequest("openai", request);

      // 3. Execute with retry policy
      LLMResponse response = retryPolicy.execute(() -> service.sendMessage(request));

      // 4. Log response
      logger.logResponse(requestId, response);

      log.info("✅ Final result: {}", response.getContent());

    } catch (Exception e) {
      log.error("❌ Request failed: {}", e.getMessage());
    }

    // Print final metrics
    logger.printMetrics();
  }
}
