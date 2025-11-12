package com.noteflix.pcm.core.auth;

import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Manager for SSO tokens and authentication
 * Supports multiple token sources: cookies, localStorage, registry, files
 * 
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class SSOTokenManager {
    
    private static SSOTokenManager instance;
    private final Map<String, TokenInfo> tokenCache = new ConcurrentHashMap<>();
    private final TokenExtractor tokenExtractor;
    private final SecurityAuditLogger auditLogger;
    
    public static SSOTokenManager getInstance() {
        if (instance == null) {
            synchronized (SSOTokenManager.class) {
                if (instance == null) {
                    instance = new SSOTokenManager();
                }
            }
        }
        return instance;
    }
    
    private SSOTokenManager() {
        this.tokenExtractor = new TokenExtractor();
        this.auditLogger = new SecurityAuditLogger();
        log.info("SSO Token Manager initialized");
    }
    
    /**
     * Get token for a specific service/API
     */
    public Optional<String> getToken(String serviceName) {
        TokenInfo tokenInfo = tokenCache.get(serviceName);
        
        // Check if token exists and is not expired
        if (tokenInfo != null && !tokenInfo.isExpired()) {
            auditLogger.logTokenAccess(serviceName, "GET_CACHED", true);
            return Optional.of(tokenInfo.getToken());
        }
        
        // Try to extract fresh token
        try {
            Optional<String> freshToken = tokenExtractor.extractToken(serviceName);
            if (freshToken.isPresent()) {
                // Cache the token with default 1 hour expiry
                cacheToken(serviceName, freshToken.get(), 3600);
                auditLogger.logTokenAccess(serviceName, "EXTRACT_FRESH", true);
                return freshToken;
            }
            
            auditLogger.logTokenAccess(serviceName, "EXTRACT_FRESH", false);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Failed to extract token for service: {}", serviceName, e);
            auditLogger.logSuspiciousActivity("TOKEN_EXTRACTION_ERROR", 
                "Service: " + serviceName + ", Error: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Cache token with expiry time
     */
    public void cacheToken(String serviceName, String token, long expirySeconds) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Attempted to cache empty token for service: {}", serviceName);
            return;
        }
        
        if (!isValidToken(token)) {
            log.warn("Attempted to cache invalid token format for service: {}", serviceName);
            auditLogger.logSuspiciousActivity("INVALID_TOKEN_CACHE", "Service: " + serviceName);
            return;
        }
        
        Instant expiry = Instant.now().plus(expirySeconds, ChronoUnit.SECONDS);
        tokenCache.put(serviceName, new TokenInfo(token, expiry));
        auditLogger.logTokenAccess(serviceName, "CACHE", true);
        log.info("Cached token for service: {} (expires: {})", serviceName, expiry);
    }
    
    /**
     * Clear all cached tokens
     */
    public void clearTokens() {
        int count = tokenCache.size();
        tokenCache.clear();
        auditLogger.logTokenAccess("ALL_SERVICES", "CLEAR_CACHE", true);
        log.info("Cleared {} cached tokens", count);
    }
    
    /**
     * Clear token for specific service
     */
    public void clearToken(String serviceName) {
        TokenInfo removed = tokenCache.remove(serviceName);
        if (removed != null) {
            auditLogger.logTokenAccess(serviceName, "CLEAR_SERVICE", true);
            log.info("Cleared cached token for service: {}", serviceName);
        }
    }
    
    /**
     * Check if user is authenticated for a service
     */
    public boolean isAuthenticated(String serviceName) {
        return getToken(serviceName).isPresent();
    }
    
    /**
     * Get token expiry time
     */
    public Optional<Instant> getTokenExpiry(String serviceName) {
        TokenInfo tokenInfo = tokenCache.get(serviceName);
        return tokenInfo != null ? Optional.of(tokenInfo.getExpiry()) : Optional.empty();
    }
    
    /**
     * Check if token is about to expire (within 5 minutes)
     */
    public boolean isTokenNearExpiry(String serviceName) {
        return getTokenExpiry(serviceName)
            .map(expiry -> expiry.isBefore(Instant.now().plus(5, ChronoUnit.MINUTES)))
            .orElse(false);
    }
    
    /**
     * Get all cached service names
     */
    public java.util.Set<String> getCachedServices() {
        return tokenCache.keySet();
    }
    
    /**
     * Validate token format
     */
    private boolean isValidToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        // Basic token validation
        return token.length() > 20 && 
               !token.contains("\n") && 
               !token.contains("\r") &&
               (token.startsWith("eyJ") || // JWT
                token.startsWith("Bearer ") ||
                token.matches("[A-Za-z0-9+/=_-]{20,}")); // Token-like pattern
    }
    
    /**
     * Token information with expiry
     */
    private static class TokenInfo {
        private final String token;
        private final Instant expiry;
        
        public TokenInfo(String token, Instant expiry) {
            this.token = token;
            this.expiry = expiry;
        }
        
        public String getToken() {
            return token;
        }
        
        public Instant getExpiry() {
            return expiry;
        }
        
        public boolean isExpired() {
            return Instant.now().isAfter(expiry);
        }
    }
}