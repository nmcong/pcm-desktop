# Hướng Dẫn Tích Hợp Single Sign-On (SSO) - PCM Desktop

## 📋 Tổng Quan

Tài liệu này hướng dẫn cách tích hợp PCM Desktop với hệ thống Single Sign-On (SSO) tự động, cho phép ứng dụng sử dụng token/cookie được sinh ra bởi hệ thống login portal để thực hiện API calls mà không cần người dùng nhập lại thông tin đăng nhập.

## 🏗️ Kiến Trúc SSO Integration

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   User Login    │    │   SSO Portal    │    │  PCM Desktop    │
│                 │    │                 │    │                 │
│ 1. Enter creds  │───▶│ 2. Authenticate │───▶│ 3. Auto login   │
│                 │    │ 3. Generate     │    │ 4. Extract      │
│                 │    │    tokens       │    │    tokens       │
│                 │    │ 4. Store in     │    │ 5. Use for      │
│                 │    │    browser      │    │    API calls    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🔧 Triển Khai SSO Integration

### 1. SSO Token Manager

Đầu tiên, tạo class để quản lý SSO tokens:

```java
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
 */
@Slf4j
public class SSOTokenManager {
    
    private static SSOTokenManager instance;
    private final Map<String, TokenInfo> tokenCache = new ConcurrentHashMap<>();
    private final TokenExtractor tokenExtractor;
    private final SecurityAuditLogger auditLogger;
    
    private SSOTokenManager() {
        this.tokenExtractor = new TokenExtractor();
        this.auditLogger = new SecurityAuditLogger();
        log.info("SSO Token Manager initialized");
    }
    
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
    
    /**
     * Get token for a specific service
     * @param serviceName Name of the service (e.g., "company-portal", "api-gateway")
     * @return Optional containing the token if found and valid
     */
    public Optional<String> getToken(String serviceName) {
        try {
            // Check cache first
            TokenInfo tokenInfo = tokenCache.get(serviceName);
            if (tokenInfo != null && !tokenInfo.isExpired()) {
                auditLogger.logTokenAccess(serviceName, "GET_CACHED", true);
                return Optional.of(tokenInfo.getToken());
            }
            
            // Extract fresh token
            Optional<String> token = tokenExtractor.extractToken(serviceName);
            if (token.isPresent()) {
                // Cache the token with expiration
                TokenInfo newTokenInfo = new TokenInfo(
                    token.get(),
                    Instant.now().plus(30, ChronoUnit.MINUTES) // 30 minutes expiration
                );
                tokenCache.put(serviceName, newTokenInfo);
                auditLogger.logTokenAccess(serviceName, "GET_NEW", true);
                return token;
            }
            
            auditLogger.logTokenAccess(serviceName, "GET_FAILED", false);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Error getting token for service {}: {}", serviceName, e.getMessage());
            auditLogger.logTokenAccess(serviceName, "GET_ERROR", false);
            return Optional.empty();
        }
    }
    
    /**
     * Manually set token for a service
     */
    public void setToken(String serviceName, String token, Instant expiration) {
        TokenInfo tokenInfo = new TokenInfo(token, expiration);
        tokenCache.put(serviceName, tokenInfo);
        auditLogger.logTokenAccess(serviceName, "SET_MANUAL", true);
        log.info("Token manually set for service: {}", serviceName);
    }
    
    /**
     * Clear cached token for a service
     */
    public void clearToken(String serviceName) {
        tokenCache.remove(serviceName);
        auditLogger.logTokenAccess(serviceName, "CLEAR", true);
        log.info("Token cleared for service: {}", serviceName);
    }
    
    /**
     * Clear all cached tokens
     */
    public void clearAllTokens() {
        int count = tokenCache.size();
        tokenCache.clear();
        auditLogger.logTokenAccess("ALL", "CLEAR_ALL", true);
        log.info("All tokens cleared. Count: {}", count);
    }
    
    /**
     * Get all available service names
     */
    public Set<String> getAvailableServices() {
        return tokenExtractor.getAvailableServices();
    }
    
    /**
     * Check if token is available for a service
     */
    public boolean isTokenAvailable(String serviceName) {
        return getToken(serviceName).isPresent();
    }
    
    // Inner class to hold token information
    private static class TokenInfo {
        private final String token;
        private final Instant expiration;
        
        public TokenInfo(String token, Instant expiration) {
            this.token = token;
            this.expiration = expiration;
        }
        
        public String getToken() {
            return token;
        }
        
        public boolean isExpired() {
            return Instant.now().isAfter(expiration);
        }
    }
}
```

### 2. Token Extractor

Class chính để extract tokens từ nhiều nguồn khác nhau:

```java
package com.noteflix.pcm.core.auth;

import lombok.extern.slf4j.Slf4j;
import java.util.Optional;
import java.util.Set;
import java.util.LinkedHashSet;

/**
 * Extracts tokens from various sources in order of priority
 */
@Slf4j
public class TokenExtractor {
    
    private final BrowserCookieExtractor cookieExtractor;
    private final BrowserLocalStorageExtractor localStorageExtractor;
    private final WindowsRegistryExtractor registryExtractor;
    private final FileBasedTokenExtractor fileExtractor;
    
    public TokenExtractor() {
        this.cookieExtractor = new BrowserCookieExtractor();
        this.localStorageExtractor = new BrowserLocalStorageExtractor();
        this.registryExtractor = new WindowsRegistryExtractor();
        this.fileExtractor = new FileBasedTokenExtractor();
    }
    
    /**
     * Extract token for a service from multiple sources
     * Priority: Cookies -> LocalStorage -> Registry -> Files
     */
    public Optional<String> extractToken(String serviceName) {
        log.debug("Attempting to extract token for service: {}", serviceName);
        
        // Try cookies first (most common for web-based SSO)
        Optional<String> token = cookieExtractor.extractFromCookies(serviceName);
        if (token.isPresent()) {
            log.debug("Token found in browser cookies for service: {}", serviceName);
            return token;
        }
        
        // Try localStorage (for SPA applications)
        token = localStorageExtractor.extractFromLocalStorage(serviceName);
        if (token.isPresent()) {
            log.debug("Token found in browser localStorage for service: {}", serviceName);
            return token;
        }
        
        // Try Windows Registry (for enterprise applications)
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            token = registryExtractor.extractFromRegistry(serviceName);
            if (token.isPresent()) {
                log.debug("Token found in Windows Registry for service: {}", serviceName);
                return token;
            }
        }
        
        // Try shared files (cross-platform solution)
        token = fileExtractor.extractFromFile(serviceName);
        if (token.isPresent()) {
            log.debug("Token found in shared files for service: {}", serviceName);
            return token;
        }
        
        log.warn("No token found for service: {}", serviceName);
        return Optional.empty();
    }
    
    /**
     * Get all services that have tokens available
     */
    public Set<String> getAvailableServices() {
        Set<String> services = new LinkedHashSet<>();
        
        services.addAll(cookieExtractor.getAvailableServices());
        services.addAll(localStorageExtractor.getAvailableServices());
        services.addAll(registryExtractor.getAvailableServices());
        services.addAll(fileExtractor.getAvailableServices());
        
        return services;
    }
}
```

### 3. Browser Cookie Extractor

Extract tokens từ browser cookies:

```java
package com.noteflix.pcm.core.auth;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * Extracts tokens from browser cookies (Chrome, Edge, Firefox)
 */
@Slf4j
public class BrowserCookieExtractor {
    
    private final Map<String, String> serviceTokenNames = Map.of(
        "company-portal", "session_token",
        "api-gateway", "auth_token",
        "sso-service", "sso_token"
    );
    
    /**
     * Extract token from browser cookies
     */
    public Optional<String> extractFromCookies(String serviceName) {
        String cookieName = serviceTokenNames.get(serviceName);
        if (cookieName == null) {
            log.warn("No cookie mapping found for service: {}", serviceName);
            return Optional.empty();
        }
        
        // Try Chrome first
        Optional<String> token = extractFromChromeCookies(cookieName);
        if (token.isPresent()) {
            return token;
        }
        
        // Try Edge
        token = extractFromEdgeCookies(cookieName);
        if (token.isPresent()) {
            return token;
        }
        
        // Try Firefox
        token = extractFromFirefoxCookies(cookieName);
        if (token.isPresent()) {
            return token;
        }
        
        return Optional.empty();
    }
    
    private Optional<String> extractFromChromeCookies(String cookieName) {
        try {
            String userHome = System.getProperty("user.home");
            Path cookiesPath;
            
            // Platform-specific Chrome cookie paths
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                cookiesPath = Paths.get(userHome, "AppData", "Local", "Google", "Chrome", "User Data", "Default", "Cookies");
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                cookiesPath = Paths.get(userHome, "Library", "Application Support", "Google", "Chrome", "Default", "Cookies");
            } else {
                cookiesPath = Paths.get(userHome, ".config", "google-chrome", "Default", "Cookies");
            }
            
            if (!Files.exists(cookiesPath)) {
                log.debug("Chrome cookies file not found: {}", cookiesPath);
                return Optional.empty();
            }
            
            // Create a copy to avoid locking issues
            Path tempCookies = Files.copy(cookiesPath, Paths.get(System.getProperty("java.io.tmpdir"), "chrome_cookies_temp.db"));
            
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + tempCookies.toString())) {
                String sql = "SELECT value FROM cookies WHERE name = ? ORDER BY creation_utc DESC LIMIT 1";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, cookieName);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            String encryptedValue = rs.getString("value");
                            String decryptedValue = decryptCookieValue(encryptedValue);
                            return Optional.ofNullable(decryptedValue);
                        }
                    }
                }
            } finally {
                Files.deleteIfExists(tempCookies);
            }
            
        } catch (Exception e) {
            log.error("Error extracting Chrome cookies: {}", e.getMessage());
        }
        
        return Optional.empty();
    }
    
    private Optional<String> extractFromEdgeCookies(String cookieName) {
        // Similar implementation for Edge
        // Edge uses the same SQLite format as Chrome but different path
        try {
            String userHome = System.getProperty("user.home");
            Path cookiesPath;
            
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                cookiesPath = Paths.get(userHome, "AppData", "Local", "Microsoft", "Edge", "User Data", "Default", "Cookies");
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                cookiesPath = Paths.get(userHome, "Library", "Application Support", "Microsoft Edge", "Default", "Cookies");
            } else {
                cookiesPath = Paths.get(userHome, ".config", "microsoft-edge", "Default", "Cookies");
            }
            
            // Implementation similar to Chrome
            return extractFromSQLiteCookies(cookiesPath, cookieName);
            
        } catch (Exception e) {
            log.error("Error extracting Edge cookies: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    private Optional<String> extractFromFirefoxCookies(String cookieName) {
        // Firefox uses different format (SQLite but different schema)
        try {
            String userHome = System.getProperty("user.home");
            Path profilePath;
            
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                profilePath = Paths.get(userHome, "AppData", "Roaming", "Mozilla", "Firefox", "Profiles");
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                profilePath = Paths.get(userHome, "Library", "Application Support", "Firefox", "Profiles");
            } else {
                profilePath = Paths.get(userHome, ".mozilla", "firefox");
            }
            
            // Find the default profile directory
            Optional<Path> defaultProfile = Files.list(profilePath)
                .filter(Files::isDirectory)
                .filter(path -> path.getFileName().toString().contains("default"))
                .findFirst();
                
            if (defaultProfile.isPresent()) {
                Path cookiesPath = defaultProfile.get().resolve("cookies.sqlite");
                return extractFromFirefoxCookiesFile(cookiesPath, cookieName);
            }
            
        } catch (Exception e) {
            log.error("Error extracting Firefox cookies: {}", e.getMessage());
        }
        
        return Optional.empty();
    }
    
    private Optional<String> extractFromSQLiteCookies(Path cookiesPath, String cookieName) {
        if (!Files.exists(cookiesPath)) {
            return Optional.empty();
        }
        
        Path tempCookies = null;
        try {
            tempCookies = Files.copy(cookiesPath, Paths.get(System.getProperty("java.io.tmpdir"), "cookies_temp_" + System.currentTimeMillis() + ".db"));
            
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + tempCookies.toString())) {
                String sql = "SELECT value FROM cookies WHERE name = ? ORDER BY creation_utc DESC LIMIT 1";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, cookieName);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            String value = rs.getString("value");
                            return Optional.ofNullable(value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error reading SQLite cookies: {}", e.getMessage());
        } finally {
            if (tempCookies != null) {
                try {
                    Files.deleteIfExists(tempCookies);
                } catch (IOException e) {
                    log.warn("Failed to delete temporary cookies file: {}", e.getMessage());
                }
            }
        }
        
        return Optional.empty();
    }
    
    private Optional<String> extractFromFirefoxCookiesFile(Path cookiesPath, String cookieName) {
        // Firefox-specific cookie extraction
        return extractFromSQLiteCookies(cookiesPath, cookieName);
    }
    
    private String decryptCookieValue(String encryptedValue) {
        // Implement cookie decryption if needed
        // For now, return as-is (many cookies are stored in plain text)
        return encryptedValue;
    }
    
    public Set<String> getAvailableServices() {
        Set<String> services = new HashSet<>();
        
        for (String service : serviceTokenNames.keySet()) {
            if (extractFromCookies(service).isPresent()) {
                services.add(service);
            }
        }
        
        return services;
    }
}
```

### 4. Windows Registry Extractor

Extract tokens từ Windows Registry:

```java
package com.noteflix.pcm.core.auth;

import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * Extracts tokens from Windows Registry
 */
@Slf4j
public class WindowsRegistryExtractor {
    
    private final Map<String, String> serviceRegistryPaths = Map.of(
        "company-portal", "HKEY_CURRENT_USER\\Software\\CompanyName\\SSO\\Portal",
        "api-gateway", "HKEY_CURRENT_USER\\Software\\CompanyName\\API\\Gateway"
    );
    
    /**
     * Extract token from Windows Registry
     */
    public Optional<String> extractFromRegistry(String serviceName) {
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            log.debug("Registry extraction only supported on Windows");
            return Optional.empty();
        }
        
        String registryPath = serviceRegistryPaths.get(serviceName);
        if (registryPath == null) {
            log.debug("No registry path configured for service: {}", serviceName);
            return Optional.empty();
        }
        
        try {
            // Use Java Preferences API to access registry
            Preferences prefs = Preferences.userRoot().node("Software/CompanyName/SSO");
            String token = prefs.get(serviceName + "_token", null);
            
            if (token != null && !token.isEmpty()) {
                log.debug("Token found in registry for service: {}", serviceName);
                return Optional.of(token);
            }
            
        } catch (Exception e) {
            log.error("Error reading from registry for service {}: {}", serviceName, e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Store token in Windows Registry
     */
    public void storeTokenInRegistry(String serviceName, String token) {
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            log.warn("Registry storage only supported on Windows");
            return;
        }
        
        try {
            Preferences prefs = Preferences.userRoot().node("Software/CompanyName/SSO");
            prefs.put(serviceName + "_token", token);
            prefs.flush();
            log.info("Token stored in registry for service: {}", serviceName);
        } catch (Exception e) {
            log.error("Error storing token in registry for service {}: {}", serviceName, e.getMessage());
        }
    }
    
    public Set<String> getAvailableServices() {
        Set<String> services = new HashSet<>();
        
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            return services;
        }
        
        for (String service : serviceRegistryPaths.keySet()) {
            if (extractFromRegistry(service).isPresent()) {
                services.add(service);
            }
        }
        
        return services;
    }
}
```

### 5. File-Based Token Extractor

Extract tokens từ shared files:

```java
package com.noteflix.pcm.core.auth;

import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Extracts tokens from shared configuration files
 */
@Slf4j
public class FileBasedTokenExtractor {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<Path> configPaths;
    
    public FileBasedTokenExtractor() {
        this.configPaths = initializeConfigPaths();
    }
    
    private List<Path> initializeConfigPaths() {
        List<Path> paths = new ArrayList<>();
        
        String userHome = System.getProperty("user.home");
        
        // Standard config locations
        paths.add(Paths.get(userHome, ".config", "company-sso", "tokens.json"));
        paths.add(Paths.get(userHome, ".company-sso", "config.json"));
        paths.add(Paths.get("config", "sso-tokens.json"));
        
        // Windows-specific
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            paths.add(Paths.get(userHome, "AppData", "Local", "CompanySSO", "tokens.json"));
        }
        
        // macOS-specific
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            paths.add(Paths.get(userHome, "Library", "Application Support", "CompanySSO", "tokens.json"));
        }
        
        return paths;
    }
    
    /**
     * Extract token from configuration files
     */
    public Optional<String> extractFromFile(String serviceName) {
        for (Path configPath : configPaths) {
            Optional<String> token = readTokenFromFile(configPath, serviceName);
            if (token.isPresent()) {
                log.debug("Token found in file {} for service: {}", configPath, serviceName);
                return token;
            }
        }
        
        return Optional.empty();
    }
    
    private Optional<String> readTokenFromFile(Path filePath, String serviceName) {
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        
        try {
            String content = Files.readString(filePath);
            JsonNode root = objectMapper.readTree(content);
            
            // Support nested structure: { "services": { "service-name": { "token": "..." } } }
            JsonNode servicesNode = root.get("services");
            if (servicesNode != null) {
                JsonNode serviceNode = servicesNode.get(serviceName);
                if (serviceNode != null) {
                    JsonNode tokenNode = serviceNode.get("token");
                    if (tokenNode != null) {
                        return Optional.of(tokenNode.asText());
                    }
                }
            }
            
            // Support flat structure: { "service-name-token": "..." }
            JsonNode tokenNode = root.get(serviceName + "-token");
            if (tokenNode != null) {
                return Optional.of(tokenNode.asText());
            }
            
            // Support direct mapping: { "service-name": "token-value" }
            tokenNode = root.get(serviceName);
            if (tokenNode != null && tokenNode.isTextual()) {
                return Optional.of(tokenNode.asText());
            }
            
        } catch (Exception e) {
            log.error("Error reading token from file {} for service {}: {}", 
                filePath, serviceName, e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Store token in configuration file
     */
    public void storeTokenInFile(String serviceName, String token) {
        Path configPath = configPaths.get(0); // Use first path as default
        
        try {
            // Create directory if it doesn't exist
            Files.createDirectories(configPath.getParent());
            
            Map<String, Object> config = new HashMap<>();
            
            // Read existing config if file exists
            if (Files.exists(configPath)) {
                String content = Files.readString(configPath);
                config = objectMapper.readValue(content, Map.class);
            }
            
            // Update services section
            Map<String, Object> services = (Map<String, Object>) config.computeIfAbsent("services", k -> new HashMap<>());
            Map<String, Object> serviceConfig = (Map<String, Object>) services.computeIfAbsent(serviceName, k -> new HashMap<>());
            serviceConfig.put("token", token);
            serviceConfig.put("updated", System.currentTimeMillis());
            
            // Write back to file
            String updatedContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);
            Files.writeString(configPath, updatedContent);
            
            log.info("Token stored in file {} for service: {}", configPath, serviceName);
            
        } catch (Exception e) {
            log.error("Error storing token in file for service {}: {}", serviceName, e.getMessage());
        }
    }
    
    public Set<String> getAvailableServices() {
        Set<String> services = new HashSet<>();
        
        for (Path configPath : configPaths) {
            services.addAll(getServicesFromFile(configPath));
        }
        
        return services;
    }
    
    private Set<String> getServicesFromFile(Path filePath) {
        Set<String> services = new HashSet<>();
        
        if (!Files.exists(filePath)) {
            return services;
        }
        
        try {
            String content = Files.readString(filePath);
            JsonNode root = objectMapper.readTree(content);
            
            // Check services section
            JsonNode servicesNode = root.get("services");
            if (servicesNode != null) {
                servicesNode.fieldNames().forEachRemaining(services::add);
            }
            
            // Check flat structure
            root.fieldNames().forEachRemaining(fieldName -> {
                if (fieldName.endsWith("-token")) {
                    services.add(fieldName.substring(0, fieldName.length() - 6));
                }
            });
            
        } catch (Exception e) {
            log.error("Error reading services from file {}: {}", filePath, e.getMessage());
        }
        
        return services;
    }
}
```

### 6. Security Audit Logger

Logging cho security auditing:

```java
package com.noteflix.pcm.core.auth;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Security audit logger for SSO token operations
 */
@Slf4j
public class SecurityAuditLogger {
    
    private final Path auditLogPath;
    private final DateTimeFormatter formatter;
    
    public SecurityAuditLogger() {
        this.auditLogPath = Paths.get("logs", "sso-audit.log");
        this.formatter = DateTimeFormatter.ISO_INSTANT;
        initializeAuditLog();
    }
    
    private void initializeAuditLog() {
        try {
            Files.createDirectories(auditLogPath.getParent());
            if (!Files.exists(auditLogPath)) {
                Files.createFile(auditLogPath);
                log.info("Created SSO audit log file: {}", auditLogPath);
            }
        } catch (IOException e) {
            log.error("Failed to initialize audit log: {}", e.getMessage());
        }
    }
    
    /**
     * Log token access attempt
     */
    public void logTokenAccess(String serviceName, String operation, boolean success) {
        String logEntry = String.format(
            "TOKEN_ACCESS|service=%s|operation=%s|success=%s|timestamp=%s|user=%s|host=%s%n",
            serviceName, operation, success, formatter.format(Instant.now()),
            System.getProperty("user.name"), getHostname()
        );
        
        writeAuditEntry(logEntry);
    }
    
    /**
     * Log token manipulation
     */
    public void logTokenManipulation(String serviceName, String operation, String source) {
        String logEntry = String.format(
            "TOKEN_MANIPULATION|service=%s|operation=%s|source=%s|timestamp=%s|user=%s|host=%s%n",
            serviceName, operation, source, formatter.format(Instant.now()),
            System.getProperty("user.name"), getHostname()
        );
        
        writeAuditEntry(logEntry);
    }
    
    /**
     * Log security event
     */
    public void logSecurityEvent(String eventType, String description) {
        String logEntry = String.format(
            "SECURITY_EVENT|type=%s|description=%s|timestamp=%s|user=%s|host=%s%n",
            eventType, description, formatter.format(Instant.now()),
            System.getProperty("user.name"), getHostname()
        );
        
        writeAuditEntry(logEntry);
    }
    
    private void writeAuditEntry(String entry) {
        try {
            Files.write(auditLogPath, entry.getBytes(), 
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.error("Failed to write audit entry: {}", e.getMessage());
        }
    }
    
    private String getHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
```

## 🚀 Cách Sử Dụng SSO Integration

### 1. Basic Usage

```java
// Initialize SSO Token Manager
SSOTokenManager ssoManager = SSOTokenManager.getInstance();

// Get token for a service
Optional<String> token = ssoManager.getToken("company-portal");

if (token.isPresent()) {
    // Use token for API calls
    String authHeader = "Bearer " + token.get();
    
    // Make API call with token
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.company.com/v1/data"))
        .header("Authorization", authHeader)
        .GET()
        .build();
        
    HttpResponse<String> response = HttpClient.newHttpClient()
        .send(request, HttpResponse.BodyHandlers.ofString());
        
    System.out.println("API Response: " + response.body());
} else {
    System.out.println("No token available for service");
}
```

### 2. Integration với LLM Service

```java
public class LLMServiceWithSSO extends LLMService {
    
    private final SSOTokenManager ssoManager;
    
    public LLMServiceWithSSO() {
        super();
        this.ssoManager = SSOTokenManager.getInstance();
    }
    
    @Override
    protected String getAuthToken(String provider) {
        // Try to get SSO token first
        Optional<String> ssoToken = ssoManager.getToken("api-gateway");
        if (ssoToken.isPresent()) {
            return ssoToken.get();
        }
        
        // Fallback to configured token
        return super.getAuthToken(provider);
    }
    
    @Override
    public CompletableFuture<LLMResponse> callLLM(LLMRequest request) {
        // Ensure we have a valid token before making the call
        if (!ssoManager.isTokenAvailable("api-gateway")) {
            return CompletableFuture.failedFuture(
                new RuntimeException("No valid SSO token available"));
        }
        
        return super.callLLM(request);
    }
}
```

### 3. Manual Token Management

```java
// Manually set token (for testing or initial setup)
Instant expiration = Instant.now().plus(1, ChronoUnit.HOURS);
ssoManager.setToken("test-service", "manual-token-123", expiration);

// Clear specific token
ssoManager.clearToken("test-service");

// Clear all tokens
ssoManager.clearAllTokens();

// Check available services
Set<String> services = ssoManager.getAvailableServices();
System.out.println("Available services: " + services);
```

## 🔧 Configuration

### 1. Environment Variables

```bash
# Enable SSO integration
SSO_ENABLED=true

# Configure service mappings
SSO_SERVICE_COMPANY_PORTAL=session_token
SSO_SERVICE_API_GATEWAY=auth_token

# Configure paths
SSO_CONFIG_PATH=/path/to/sso/config
SSO_AUDIT_LOG_PATH=/path/to/audit.log
```

### 2. Configuration File (config/sso-config.json)

```json
{
  "enabled": true,
  "services": {
    "company-portal": {
      "cookieName": "session_token",
      "domain": "company.com",
      "registryPath": "HKEY_CURRENT_USER\\Software\\Company\\SSO",
      "configFile": "portal-config.json"
    },
    "api-gateway": {
      "cookieName": "auth_token",
      "domain": "api.company.com",
      "registryPath": "HKEY_CURRENT_USER\\Software\\Company\\API",
      "configFile": "api-config.json"
    }
  },
  "extraction": {
    "priority": ["cookies", "localStorage", "registry", "files"],
    "cacheExpiration": "30m",
    "retryAttempts": 3
  },
  "security": {
    "auditLogging": true,
    "encryptTokens": true,
    "logPath": "logs/sso-audit.log"
  }
}
```

## 🧪 Testing SSO Integration

### 1. Interactive Demo

Tạo demo để test SSO functionality:

```java
package com.noteflix.pcm.examples;

import com.noteflix.pcm.core.auth.SSOTokenManager;
import java.util.Scanner;
import java.util.Set;

/**
 * Interactive demo for SSO Integration
 */
public class SSOIntegrationDemo {
    
    public static void main(String[] args) {
        SSOTokenManager ssoManager = SSOTokenManager.getInstance();
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    listAvailableServices(ssoManager);
                    break;
                case "2":
                    getTokenForService(ssoManager, scanner);
                    break;
                case "3":
                    setManualToken(ssoManager, scanner);
                    break;
                case "4":
                    clearTokenForService(ssoManager, scanner);
                    break;
                case "5":
                    clearAllTokens(ssoManager);
                    break;
                case "6":
                    testAPICall(ssoManager, scanner);
                    break;
                case "0":
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
            
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }
    
    private static void printMenu() {
        System.out.println("\n=== SSO Integration Demo ===");
        System.out.println("1. List available services");
        System.out.println("2. Get token for service");
        System.out.println("3. Set manual token");
        System.out.println("4. Clear token for service");
        System.out.println("5. Clear all tokens");
        System.out.println("6. Test API call");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }
    
    private static void listAvailableServices(SSOTokenManager ssoManager) {
        Set<String> services = ssoManager.getAvailableServices();
        if (services.isEmpty()) {
            System.out.println("No services with tokens found.");
        } else {
            System.out.println("Available services:");
            services.forEach(service -> System.out.println("  - " + service));
        }
    }
    
    private static void getTokenForService(SSOTokenManager ssoManager, Scanner scanner) {
        System.out.print("Enter service name: ");
        String serviceName = scanner.nextLine().trim();
        
        Optional<String> token = ssoManager.getToken(serviceName);
        if (token.isPresent()) {
            String maskedToken = maskToken(token.get());
            System.out.println("Token found: " + maskedToken);
        } else {
            System.out.println("No token found for service: " + serviceName);
        }
    }
    
    private static void setManualToken(SSOTokenManager ssoManager, Scanner scanner) {
        System.out.print("Enter service name: ");
        String serviceName = scanner.nextLine().trim();
        
        System.out.print("Enter token: ");
        String token = scanner.nextLine().trim();
        
        System.out.print("Enter expiration minutes (default 30): ");
        String expirationInput = scanner.nextLine().trim();
        int minutes = expirationInput.isEmpty() ? 30 : Integer.parseInt(expirationInput);
        
        Instant expiration = Instant.now().plus(minutes, ChronoUnit.MINUTES);
        ssoManager.setToken(serviceName, token, expiration);
        
        System.out.println("Token set successfully for service: " + serviceName);
    }
    
    private static void clearTokenForService(SSOTokenManager ssoManager, Scanner scanner) {
        System.out.print("Enter service name: ");
        String serviceName = scanner.nextLine().trim();
        
        ssoManager.clearToken(serviceName);
        System.out.println("Token cleared for service: " + serviceName);
    }
    
    private static void clearAllTokens(SSOTokenManager ssoManager) {
        ssoManager.clearAllTokens();
        System.out.println("All tokens cleared successfully.");
    }
    
    private static void testAPICall(SSOTokenManager ssoManager, Scanner scanner) {
        System.out.print("Enter service name: ");
        String serviceName = scanner.nextLine().trim();
        
        System.out.print("Enter API URL: ");
        String apiUrl = scanner.nextLine().trim();
        
        Optional<String> token = ssoManager.getToken(serviceName);
        if (token.isPresent()) {
            // Simulate API call
            System.out.println("Making API call to: " + apiUrl);
            System.out.println("Using token: " + maskToken(token.get()));
            
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Authorization", "Bearer " + token.get())
                    .GET()
                    .build();
                    
                HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
                    
                System.out.println("Response Status: " + response.statusCode());
                System.out.println("Response Body: " + response.body().substring(0, 
                    Math.min(response.body().length(), 200)) + "...");
                    
            } catch (Exception e) {
                System.out.println("API call failed: " + e.getMessage());
            }
        } else {
            System.out.println("No token available for service: " + serviceName);
        }
    }
    
    private static String maskToken(String token) {
        if (token.length() <= 8) {
            return "*".repeat(token.length());
        }
        return token.substring(0, 4) + "*".repeat(token.length() - 8) + token.substring(token.length() - 4);
    }
}
```

### 2. Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class SSOTokenManagerTest {
    
    @Mock
    private TokenExtractor tokenExtractor;
    
    @InjectMocks
    private SSOTokenManager ssoTokenManager;
    
    @Test
    void shouldReturnTokenFromCache() {
        // Given
        String serviceName = "test-service";
        String expectedToken = "cached-token";
        
        // Set up cache
        Instant futureExpiration = Instant.now().plus(10, ChronoUnit.MINUTES);
        ssoTokenManager.setToken(serviceName, expectedToken, futureExpiration);
        
        // When
        Optional<String> result = ssoTokenManager.getToken(serviceName);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedToken);
        verify(tokenExtractor, never()).extractToken(serviceName);
    }
    
    @Test
    void shouldExtractFreshTokenWhenCacheExpired() {
        // Given
        String serviceName = "test-service";
        String cachedToken = "expired-token";
        String freshToken = "fresh-token";
        
        // Set up expired cache
        Instant pastExpiration = Instant.now().minus(1, ChronoUnit.HOURS);
        ssoTokenManager.setToken(serviceName, cachedToken, pastExpiration);
        
        when(tokenExtractor.extractToken(serviceName))
            .thenReturn(Optional.of(freshToken));
        
        // When
        Optional<String> result = ssoTokenManager.getToken(serviceName);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(freshToken);
        verify(tokenExtractor).extractToken(serviceName);
    }
    
    @Test
    void shouldReturnEmptyWhenNoTokenAvailable() {
        // Given
        String serviceName = "non-existent-service";
        
        when(tokenExtractor.extractToken(serviceName))
            .thenReturn(Optional.empty());
        
        // When
        Optional<String> result = ssoTokenManager.getToken(serviceName);
        
        // Then
        assertThat(result).isEmpty();
        verify(tokenExtractor).extractToken(serviceName);
    }
}
```

## 🔒 Security Considerations

### 1. Token Encryption

```java
public class TokenEncryption {
    
    private final String encryptionKey;
    
    public TokenEncryption() {
        this.encryptionKey = System.getenv("SSO_ENCRYPTION_KEY");
        if (encryptionKey == null) {
            throw new IllegalStateException("SSO_ENCRYPTION_KEY environment variable not set");
        }
    }
    
    public String encryptToken(String token) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(encryptionKey.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            
            byte[] encrypted = cipher.doFinal(token.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt token", e);
        }
    }
    
    public String decryptToken(String encryptedToken) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(encryptionKey.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedToken));
            return new String(decrypted);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt token", e);
        }
    }
}
```

### 2. Access Control

```java
public class SSOAccessControl {
    
    private final Set<String> allowedUsers = Set.of("admin", "service-account");
    private final Set<String> allowedHosts = Set.of("workstation-1", "server-prod");
    
    public boolean isAccessAllowed(String operation) {
        String currentUser = System.getProperty("user.name");
        String currentHost = getHostname();
        
        if (!allowedUsers.contains(currentUser)) {
            log.warn("Access denied for user: {}", currentUser);
            return false;
        }
        
        if (!allowedHosts.contains(currentHost)) {
            log.warn("Access denied for host: {}", currentHost);
            return false;
        }
        
        return true;
    }
    
    private String getHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
```

### 3. Rate Limiting

```java
public class SSOThrottling {
    
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> lastResetTimes = new ConcurrentHashMap<>();
    private final int maxRequestsPerMinute = 60;
    
    public boolean isRequestAllowed(String operation) {
        String key = System.getProperty("user.name") + ":" + operation;
        long currentTime = System.currentTimeMillis();
        
        // Reset counter if a minute has passed
        Long lastReset = lastResetTimes.get(key);
        if (lastReset == null || currentTime - lastReset > 60000) {
            requestCounts.put(key, new AtomicInteger(0));
            lastResetTimes.put(key, currentTime);
        }
        
        AtomicInteger counter = requestCounts.get(key);
        int currentCount = counter.incrementAndGet();
        
        if (currentCount > maxRequestsPerMinute) {
            log.warn("Rate limit exceeded for {}: {} requests in the last minute", key, currentCount);
            return false;
        }
        
        return true;
    }
}
```

## 🔄 Advanced Features

### 1. Token Refresh

```java
public class TokenRefreshService {
    
    private final SSOTokenManager ssoManager;
    private final ScheduledExecutorService scheduler;
    
    public TokenRefreshService(SSOTokenManager ssoManager) {
        this.ssoManager = ssoManager;
        this.scheduler = Executors.newScheduledThreadPool(1);
        startTokenRefreshScheduler();
    }
    
    private void startTokenRefreshScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                refreshAllTokens();
            } catch (Exception e) {
                log.error("Error during token refresh: {}", e.getMessage());
            }
        }, 5, 5, TimeUnit.MINUTES); // Check every 5 minutes
    }
    
    private void refreshAllTokens() {
        Set<String> services = ssoManager.getAvailableServices();
        
        for (String service : services) {
            try {
                Optional<String> currentToken = ssoManager.getToken(service);
                if (currentToken.isPresent()) {
                    // Force refresh by clearing cache and re-extracting
                    ssoManager.clearToken(service);
                    Optional<String> freshToken = ssoManager.getToken(service);
                    
                    if (freshToken.isPresent()) {
                        log.debug("Token refreshed for service: {}", service);
                    } else {
                        log.warn("Failed to refresh token for service: {}", service);
                    }
                }
            } catch (Exception e) {
                log.error("Error refreshing token for service {}: {}", service, e.getMessage());
            }
        }
    }
}
```

### 2. Token Validation

```java
public class TokenValidator {
    
    public boolean isValidToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        // Check token format (e.g., JWT)
        if (isJwtToken(token)) {
            return validateJwtToken(token);
        }
        
        // Check other token formats
        if (isOpaqueToken(token)) {
            return validateOpaqueToken(token);
        }
        
        return false;
    }
    
    private boolean isJwtToken(String token) {
        return token.split("\\.").length == 3;
    }
    
    private boolean validateJwtToken(String token) {
        try {
            String[] parts = token.split("\\.");
            
            // Decode header
            String header = new String(Base64.getUrlDecoder().decode(parts[0]));
            JsonNode headerNode = new ObjectMapper().readTree(header);
            
            // Decode payload
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode payloadNode = new ObjectMapper().readTree(payload);
            
            // Check expiration
            JsonNode expNode = payloadNode.get("exp");
            if (expNode != null) {
                long exp = expNode.asLong();
                long now = System.currentTimeMillis() / 1000;
                
                if (now >= exp) {
                    log.warn("JWT token is expired");
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Error validating JWT token: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean isOpaqueToken(String token) {
        // Simple heuristic for opaque tokens
        return token.length() >= 32 && token.matches("[A-Za-z0-9+/=]+");
    }
    
    private boolean validateOpaqueToken(String token) {
        // For opaque tokens, we might need to call a validation endpoint
        // For now, just check basic format
        return token.length() >= 32 && token.length() <= 2048;
    }
}
```

## 🚦 Production Deployment

### 1. Configuration Management

```yaml
# application.yml
sso:
  enabled: ${SSO_ENABLED:true}
  services:
    company-portal:
      cookie-name: ${SSO_PORTAL_COOKIE:session_token}
      domain: ${SSO_PORTAL_DOMAIN:company.com}
    api-gateway:
      cookie-name: ${SSO_API_COOKIE:auth_token}
      domain: ${SSO_API_DOMAIN:api.company.com}
  
  extraction:
    priority: ${SSO_EXTRACTION_PRIORITY:cookies,localStorage,registry,files}
    cache-expiration: ${SSO_CACHE_EXPIRATION:30m}
    retry-attempts: ${SSO_RETRY_ATTEMPTS:3}
  
  security:
    audit-logging: ${SSO_AUDIT_LOGGING:true}
    encrypt-tokens: ${SSO_ENCRYPT_TOKENS:true}
    log-path: ${SSO_LOG_PATH:logs/sso-audit.log}
```

### 2. Monitoring và Metrics

```java
@Component
public class SSOMetrics {
    
    private final Counter tokenAccessCounter;
    private final Timer tokenExtractionTimer;
    private final Gauge activeTokensGauge;
    
    public SSOMetrics(MeterRegistry meterRegistry) {
        this.tokenAccessCounter = Counter.builder("sso.token.access")
            .description("Number of token access attempts")
            .tag("result", "success")
            .register(meterRegistry);
            
        this.tokenExtractionTimer = Timer.builder("sso.token.extraction")
            .description("Time taken to extract tokens")
            .register(meterRegistry);
            
        this.activeTokensGauge = Gauge.builder("sso.tokens.active")
            .description("Number of active tokens")
            .register(meterRegistry, this, SSOMetrics::getActiveTokenCount);
    }
    
    public void recordTokenAccess(String result) {
        tokenAccessCounter.increment(Tags.of("result", result));
    }
    
    public void recordTokenExtraction(Duration duration) {
        tokenExtractionTimer.record(duration);
    }
    
    private double getActiveTokenCount() {
        return SSOTokenManager.getInstance().getAvailableServices().size();
    }
}
```

### 3. Health Checks

```java
@Component
public class SSOHealthIndicator implements HealthIndicator {
    
    private final SSOTokenManager ssoManager;
    
    public SSOHealthIndicator(SSOTokenManager ssoManager) {
        this.ssoManager = ssoManager;
    }
    
    @Override
    public Health health() {
        try {
            Set<String> availableServices = ssoManager.getAvailableServices();
            
            if (availableServices.isEmpty()) {
                return Health.down()
                    .withDetail("reason", "No SSO tokens available")
                    .build();
            }
            
            // Check if critical services have tokens
            boolean hasCriticalTokens = availableServices.contains("company-portal") || 
                                      availableServices.contains("api-gateway");
            
            if (!hasCriticalTokens) {
                return Health.down()
                    .withDetail("reason", "Critical SSO services not available")
                    .withDetail("available", availableServices)
                    .build();
            }
            
            return Health.up()
                .withDetail("availableServices", availableServices.size())
                .withDetail("services", availableServices)
                .build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("reason", "SSO system error")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

---

## 📚 Best Practices

1. **Security First**: Always encrypt sensitive tokens and use secure storage
2. **Fail Gracefully**: Provide fallback mechanisms when SSO tokens are unavailable  
3. **Audit Everything**: Log all token access and manipulation for security auditing
4. **Cache Wisely**: Balance performance with security by setting appropriate cache expiration
5. **Test Thoroughly**: Test all token extraction methods on different environments
6. **Monitor Actively**: Set up alerts for SSO failures and token expiration

## 🔗 References

- [Java Preferences API](https://docs.oracle.com/javase/8/docs/api/java/util/prefs/Preferences.html)
- [SQLite JDBC Driver](https://github.com/xerial/sqlite-jdbc)
- [Jackson JSON Processing](https://github.com/FasterXML/jackson)
- [SLF4J Logging](http://www.slf4j.org/)

---

*Được tạo bởi PCM Desktop Development Team*