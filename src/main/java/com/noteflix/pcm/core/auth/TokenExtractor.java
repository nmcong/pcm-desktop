package com.noteflix.pcm.core.auth;

import lombok.extern.slf4j.Slf4j;
import java.util.Optional;

/**
 * Extracts tokens from various sources: browser cookies, localStorage, 
 * Windows registry, shared files, etc.
 * 
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class TokenExtractor {
    
    private final BrowserCookieExtractor cookieExtractor;
    private final LocalStorageExtractor localStorageExtractor;
    private final RegistryExtractor registryExtractor;
    private final FileTokenExtractor fileExtractor;
    
    public TokenExtractor() {
        this.cookieExtractor = new BrowserCookieExtractor();
        this.localStorageExtractor = new LocalStorageExtractor();
        this.registryExtractor = new RegistryExtractor();
        this.fileExtractor = new FileTokenExtractor();
    }
    
    /**
     * Extract token for a service using multiple strategies
     */
    public Optional<String> extractToken(String serviceName) {
        log.debug("Extracting token for service: {}", serviceName);
        
        // Strategy 1: Try browser cookies first (most common for web SSO)
        try {
            Optional<String> cookieToken = cookieExtractor.extractFromCookies(serviceName);
            if (cookieToken.isPresent()) {
                log.info("Found token in browser cookies for: {}", serviceName);
                return cookieToken;
            }
        } catch (Exception e) {
            log.debug("Failed to extract from cookies for {}: {}", serviceName, e.getMessage());
        }
        
        // Strategy 2: Try browser localStorage
        try {
            Optional<String> localStorageToken = localStorageExtractor.extractFromLocalStorage(serviceName);
            if (localStorageToken.isPresent()) {
                log.info("Found token in localStorage for: {}", serviceName);
                return localStorageToken;
            }
        } catch (Exception e) {
            log.debug("Failed to extract from localStorage for {}: {}", serviceName, e.getMessage());
        }
        
        // Strategy 3: Try Windows Registry (if on Windows)
        if (isWindows()) {
            try {
                Optional<String> registryToken = registryExtractor.extractFromRegistry(serviceName);
                if (registryToken.isPresent()) {
                    log.info("Found token in Windows Registry for: {}", serviceName);
                    return registryToken;
                }
            } catch (Exception e) {
                log.debug("Failed to extract from registry for {}: {}", serviceName, e.getMessage());
            }
        }
        
        // Strategy 4: Try shared file location
        try {
            Optional<String> fileToken = fileExtractor.extractFromFile(serviceName);
            if (fileToken.isPresent()) {
                log.info("Found token in shared file for: {}", serviceName);
                return fileToken;
            }
        } catch (Exception e) {
            log.debug("Failed to extract from files for {}: {}", serviceName, e.getMessage());
        }
        
        log.warn("No token found for service: {}", serviceName);
        return Optional.empty();
    }
    
    /**
     * Check if running on Windows
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
    
    /**
     * Check if running on macOS
     */
    private boolean isMacOS() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }
    
    /**
     * Check if running on Linux
     */
    private boolean isLinux() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("linux") || os.contains("unix");
    }
}