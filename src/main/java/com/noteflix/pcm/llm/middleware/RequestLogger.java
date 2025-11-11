package com.noteflix.pcm.llm.middleware;

import com.noteflix.pcm.llm.model.LLMRequest;
import com.noteflix.pcm.llm.model.LLMResponse;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Request logger for LLM operations
 * 
 * Logs requests, responses, and metrics for monitoring and debugging.
 * 
 * Features:
 * - Request/response logging
 * - Performance metrics
 * - Token usage tracking
 * - Cost estimation
 * - Error tracking
 * 
 * Example usage:
 * <pre>
 * RequestLogger logger = RequestLogger.builder()
 *     .logRequests(true)
 *     .logResponses(true)
 *     .trackMetrics(true)
 *     .build();
 * 
 * String requestId = logger.logRequest("openai", request);
 * // ... make API call ...
 * logger.logResponse(requestId, response);
 * </pre>
 * 
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
@Data
@Builder
public class RequestLogger {
    
    /**
     * Enable request logging
     * Default: true
     */
    @Builder.Default
    private boolean logRequests = true;
    
    /**
     * Enable response logging
     * Default: true
     */
    @Builder.Default
    private boolean logResponses = true;
    
    /**
     * Log full request/response content (can be large)
     * Default: false (only log summaries)
     */
    @Builder.Default
    private boolean logFullContent = false;
    
    /**
     * Track metrics (tokens, latency, costs)
     * Default: true
     */
    @Builder.Default
    private boolean trackMetrics = true;
    
    /**
     * Enable error logging
     * Default: true
     */
    @Builder.Default
    private boolean logErrors = true;
    
    // Metrics storage
    private final ConcurrentHashMap<String, RequestMetrics> activeRequests = new ConcurrentHashMap<>();
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalTokens = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    
    /**
     * Log the start of a request
     * 
     * @param provider Provider name
     * @param request LLM request
     * @return Request ID for tracking
     */
    public String logRequest(String provider, LLMRequest request) {
        String requestId = generateRequestId();
        
        if (logRequests) {
            if (logFullContent) {
                log.info("🚀 [{}] Request to {}: model={}, messages={}, temp={}", 
                    requestId, provider, request.getModel(), 
                    request.getMessages(), request.getTemperature());
            } else {
                log.info("🚀 [{}] Request to {}: model={}, messageCount={}", 
                    requestId, provider, request.getModel(), 
                    request.getMessages() != null ? request.getMessages().size() : 0);
            }
        }
        
        if (trackMetrics) {
            RequestMetrics metrics = RequestMetrics.builder()
                .requestId(requestId)
                .provider(provider)
                .model(request.getModel())
                .startTime(Instant.now())
                .messageCount(request.getMessages() != null ? request.getMessages().size() : 0)
                .build();
            
            activeRequests.put(requestId, metrics);
            totalRequests.incrementAndGet();
        }
        
        return requestId;
    }
    
    /**
     * Log a successful response
     * 
     * @param requestId Request ID from logRequest
     * @param response LLM response
     */
    public void logResponse(String requestId, LLMResponse response) {
        RequestMetrics metrics = activeRequests.remove(requestId);
        
        if (logResponses) {
            if (logFullContent) {
                log.info("✅ [{}] Response: content={}, tokens={}, finishReason={}", 
                    requestId, response.getContent(), 
                    response.getUsage() != null ? response.getUsage().getTotalTokens() : 0,
                    response.getFinishReason());
            } else {
                log.info("✅ [{}] Response: contentLength={}, tokens={}", 
                    requestId, 
                    response.getContent() != null ? response.getContent().length() : 0,
                    response.getUsage() != null ? response.getUsage().getTotalTokens() : 0);
            }
        }
        
        if (trackMetrics && metrics != null) {
            metrics.setEndTime(Instant.now());
            metrics.setSuccess(true);
            
            if (response.getUsage() != null) {
                metrics.setPromptTokens(response.getUsage().getPromptTokens());
                metrics.setCompletionTokens(response.getUsage().getCompletionTokens());
                metrics.setTotalTokens(response.getUsage().getTotalTokens());
                totalTokens.addAndGet(response.getUsage().getTotalTokens());
            }
            
            long latencyMs = Duration.between(metrics.getStartTime(), metrics.getEndTime()).toMillis();
            metrics.setLatencyMs(latencyMs);
            
            log.info("📊 [{}] Metrics: latency={}ms, tokens={}, provider={}, model={}", 
                requestId, latencyMs, metrics.getTotalTokens(), 
                metrics.getProvider(), metrics.getModel());
        }
    }
    
    /**
     * Log an error
     * 
     * @param requestId Request ID from logRequest
     * @param error Exception that occurred
     */
    public void logError(String requestId, Throwable error) {
        RequestMetrics metrics = activeRequests.remove(requestId);
        
        if (logErrors) {
            log.error("❌ [{}] Error: {}", requestId, error.getMessage(), error);
        }
        
        if (trackMetrics) {
            totalErrors.incrementAndGet();
            
            if (metrics != null) {
                metrics.setEndTime(Instant.now());
                metrics.setSuccess(false);
                metrics.setErrorMessage(error.getMessage());
                
                long latencyMs = Duration.between(metrics.getStartTime(), metrics.getEndTime()).toMillis();
                metrics.setLatencyMs(latencyMs);
            }
        }
    }
    
    /**
     * Get total number of requests
     */
    public long getTotalRequests() {
        return totalRequests.get();
    }
    
    /**
     * Get total tokens consumed
     */
    public long getTotalTokens() {
        return totalTokens.get();
    }
    
    /**
     * Get total errors
     */
    public long getTotalErrors() {
        return totalErrors.get();
    }
    
    /**
     * Get success rate as percentage
     */
    public double getSuccessRate() {
        long total = totalRequests.get();
        if (total == 0) return 0.0;
        
        long errors = totalErrors.get();
        return ((double) (total - errors) / total) * 100.0;
    }
    
    /**
     * Get number of active (in-flight) requests
     */
    public int getActiveRequestCount() {
        return activeRequests.size();
    }
    
    /**
     * Print metrics summary
     */
    public void printMetrics() {
        log.info("═══════════════════════════════════════════");
        log.info("📊 LLM Request Metrics Summary");
        log.info("═══════════════════════════════════════════");
        log.info("Total Requests:     {}", totalRequests.get());
        log.info("Total Tokens:       {}", totalTokens.get());
        log.info("Total Errors:       {}", totalErrors.get());
        log.info("Success Rate:       {:.2f}%", getSuccessRate());
        log.info("Active Requests:    {}", activeRequests.size());
        log.info("═══════════════════════════════════════════");
    }
    
    /**
     * Reset all metrics
     */
    public void reset() {
        activeRequests.clear();
        totalRequests.set(0);
        totalTokens.set(0);
        totalErrors.set(0);
        log.info("🔄 Metrics reset");
    }
    
    private String generateRequestId() {
        return "req-" + System.currentTimeMillis() + "-" + 
            Integer.toHexString((int) (Math.random() * 0xFFFF));
    }
    
    /**
     * Metrics for a single request
     */
    @Data
    @Builder
    private static class RequestMetrics {
        private String requestId;
        private String provider;
        private String model;
        private Instant startTime;
        private Instant endTime;
        private long latencyMs;
        private int messageCount;
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;
        private boolean success;
        private String errorMessage;
    }
    
    /**
     * Create a default logger
     */
    public static RequestLogger defaultLogger() {
        return RequestLogger.builder().build();
    }
    
    /**
     * Create a verbose logger (logs everything)
     */
    public static RequestLogger verbose() {
        return RequestLogger.builder()
            .logRequests(true)
            .logResponses(true)
            .logFullContent(true)
            .trackMetrics(true)
            .logErrors(true)
            .build();
    }
    
    /**
     * Create a minimal logger (only errors and metrics)
     */
    public static RequestLogger minimal() {
        return RequestLogger.builder()
            .logRequests(false)
            .logResponses(false)
            .logFullContent(false)
            .trackMetrics(true)
            .logErrors(true)
            .build();
    }
    
    /**
     * Create a silent logger (no logging)
     */
    public static RequestLogger silent() {
        return RequestLogger.builder()
            .logRequests(false)
            .logResponses(false)
            .logFullContent(false)
            .trackMetrics(false)
            .logErrors(false)
            .build();
    }
}

