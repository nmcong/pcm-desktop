# Hướng Dẫn Tích Hợp Single Sign-On (SSO) - PCM Desktop

## 📋 Tổng Quan

Tài liệu này hướng dẫn cách tích hợp PCM Desktop với hệ thống Single Sign-On (SSO) tự động, cho phép ứng dụng sử dụng
token/cookie được sinh ra bởi hệ thống login portal để thực hiện API calls mà không cần người dùng nhập lại thông tin
đăng nhập.

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
    
    public static SSOTokenManager getInstance() {
        if (instance == null) {
            instance = new SSOTokenManager();
        }
        return instance;
    }
    
    private SSOTokenManager() {
        this.tokenExtractor = new TokenExtractor();
        log.info("SSO Token Manager initialized");
    }
    
    /**
     * Get token for a specific service/API
     */
    public Optional<String> getToken(String serviceName) {
        TokenInfo tokenInfo = tokenCache.get(serviceName);
        
        // Check if token exists and is not expired
        if (tokenInfo != null && !tokenInfo.isExpired()) {
            return Optional.of(tokenInfo.getToken());
        }
        
        // Try to extract fresh token
        Optional<String> freshToken = tokenExtractor.extractToken(serviceName);
        if (freshToken.isPresent()) {
            // Cache the token with default 1 hour expiry
            cacheToken(serviceName, freshToken.get(), 3600);
            return freshToken;
        }
        
        return Optional.empty();
    }
    
    /**
     * Cache token with expiry time
     */
    public void cacheToken(String serviceName, String token, long expirySeconds) {
        Instant expiry = Instant.now().plus(expirySeconds, ChronoUnit.SECONDS);
        tokenCache.put(serviceName, new TokenInfo(token, expiry));
        log.info("Cached token for service: {}", serviceName);
    }
    
    /**
     * Clear all cached tokens
     */
    public void clearTokens() {
        tokenCache.clear();
        log.info("All tokens cleared");
    }
    
    /**
     * Check if user is authenticated for a service
     */
    public boolean isAuthenticated(String serviceName) {
        return getToken(serviceName).isPresent();
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
        
        public boolean isExpired() {
            return Instant.now().isAfter(expiry);
        }
    }
}
```

### 2. Token Extractor

Tạo class để extract tokens từ các nguồn khác nhau:

```java
package com.noteflix.pcm.core.auth;

import lombok.extern.slf4j.Slf4j;
import java.util.Optional;
import java.util.prefs.Preferences;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts tokens from various sources: browser cookies, localStorage, 
 * Windows registry, shared files, etc.
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
        
        // Strategy 1: Try browser cookies first
        Optional<String> cookieToken = cookieExtractor.extractFromCookies(serviceName);
        if (cookieToken.isPresent()) {
            log.info("Found token in browser cookies for: {}", serviceName);
            return cookieToken;
        }
        
        // Strategy 2: Try browser localStorage
        Optional<String> localStorageToken = localStorageExtractor.extractFromLocalStorage(serviceName);
        if (localStorageToken.isPresent()) {
            log.info("Found token in localStorage for: {}", serviceName);
            return localStorageToken;
        }
        
        // Strategy 3: Try Windows Registry (if on Windows)
        if (isWindows()) {
            Optional<String> registryToken = registryExtractor.extractFromRegistry(serviceName);
            if (registryToken.isPresent()) {
                log.info("Found token in Windows Registry for: {}", serviceName);
                return registryToken;
            }
        }
        
        // Strategy 4: Try shared file location
        Optional<String> fileToken = fileExtractor.extractFromFile(serviceName);
        if (fileToken.isPresent()) {
            log.info("Found token in shared file for: {}", serviceName);
            return fileToken;
        }
        
        log.warn("No token found for service: {}", serviceName);
        return Optional.empty();
    }
    
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
}
```

### 3. Browser Cookie Extractor

```java
package com.noteflix.pcm.core.auth;

import lombok.extern.slf4j.Slf4j;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Extracts authentication tokens from browser cookies
 * Supports Chrome, Edge, Firefox
 */
@Slf4j
public class BrowserCookieExtractor {
    
    private static final String[] CHROME_COOKIE_PATHS = {
        System.getProperty("user.home") + "/AppData/Local/Google/Chrome/User Data/Default/Cookies", // Windows
        System.getProperty("user.home") + "/Library/Application Support/Google/Chrome/Default/Cookies", // macOS
        System.getProperty("user.home") + "/.config/google-chrome/Default/Cookies" // Linux
    };
    
    private static final String[] EDGE_COOKIE_PATHS = {
        System.getProperty("user.home") + "/AppData/Local/Microsoft/Edge/User Data/Default/Cookies", // Windows
        System.getProperty("user.home") + "/Library/Application Support/Microsoft Edge/Default/Cookies" // macOS
    };
    
    /**
     * Extract token from browser cookies
     */
    public Optional<String> extractFromCookies(String serviceName) {
        try {
            // Try Chrome first
            Optional<String> chromeToken = extractFromChrome(serviceName);
            if (chromeToken.isPresent()) {
                return chromeToken;
            }
            
            // Try Edge
            Optional<String> edgeToken = extractFromEdge(serviceName);
            if (edgeToken.isPresent()) {
                return edgeToken;
            }
            
            // Try Firefox
            Optional<String> firefoxToken = extractFromFirefox(serviceName);
            return firefoxToken;
            
        } catch (Exception e) {
            log.error("Error extracting cookies", e);
            return Optional.empty();
        }
    }
    
    private Optional<String> extractFromChrome(String serviceName) {
        return extractFromChromiumBased(CHROME_COOKIE_PATHS, serviceName);
    }
    
    private Optional<String> extractFromEdge(String serviceName) {
        return extractFromChromiumBased(EDGE_COOKIE_PATHS, serviceName);
    }
    
    private Optional<String> extractFromChromiumBased(String[] cookiePaths, String serviceName) {
        for (String cookiePath : cookiePaths) {
            Path path = Paths.get(cookiePath);
            if (Files.exists(path)) {
                try {
                    return extractFromChromiumDatabase(cookiePath, serviceName);
                } catch (Exception e) {
                    log.debug("Failed to extract from: {}", cookiePath);
                }
            }
        }
        return Optional.empty();
    }
    
    private Optional<String> extractFromChromiumDatabase(String dbPath, String serviceName) throws SQLException {
        String url = "jdbc:sqlite:" + dbPath;
        
        // Common cookie names for authentication tokens
        String[] tokenCookieNames = {
            "auth_token", "session_token", "jwt", "access_token",
            serviceName + "_token", serviceName + "_auth",
            "sso_token", "portal_token", "login_token"
        };
        
        try (Connection conn = DriverManager.getConnection(url)) {
            for (String cookieName : tokenCookieNames) {
                String sql = "SELECT encrypted_value, value FROM cookies WHERE name = ? AND host_key LIKE ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, cookieName);
                    stmt.setString(2, "%" + serviceName + "%");
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            byte[] encryptedValue = rs.getBytes("encrypted_value");
                            String plainValue = rs.getString("value");
                            
                            // If encrypted, try to decrypt (Chrome v80+)
                            if (encryptedValue != null && encryptedValue.length > 0) {
                                String decrypted = decryptChromeCookie(encryptedValue);
                                if (decrypted != null && !decrypted.isEmpty()) {
                                    return Optional.of(decrypted);
                                }
                            }
                            
                            // Fall back to plain value (older Chrome versions)
                            if (plainValue != null && !plainValue.isEmpty()) {
                                return Optional.of(plainValue);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Database error extracting Chrome cookies", e);
        }
        
        return Optional.empty();
    }
    
    private String decryptChromeCookie(byte[] encryptedValue) {
        try {
            // Chrome cookie decryption logic (simplified)
            // In real implementation, you need to handle Chrome's encryption properly
            // This might require accessing Chrome's Local State file for the encryption key
            
            // For demonstration - in real app, implement proper Chrome cookie decryption
            return new String(encryptedValue, "UTF-8");
            
        } catch (Exception e) {
            log.debug("Failed to decrypt Chrome cookie", e);
            return null;
        }
    }
    
    private Optional<String> extractFromFirefox(String serviceName) {
        // Firefox uses a different format (cookies.sqlite)
        // Implementation would be similar but simpler (no encryption by default)
        try {
            String firefoxProfile = findFirefoxProfile();
            if (firefoxProfile != null) {
                String cookiePath = firefoxProfile + "/cookies.sqlite";
                return extractFromFirefoxDatabase(cookiePath, serviceName);
            }
        } catch (Exception e) {
            log.debug("Failed to extract Firefox cookies", e);
        }
        
        return Optional.empty();
    }
    
    private String findFirefoxProfile() {
        // Logic to find Firefox profile directory
        String[] possiblePaths = {
            System.getProperty("user.home") + "/AppData/Roaming/Mozilla/Firefox/Profiles", // Windows
            System.getProperty("user.home") + "/Library/Application Support/Firefox/Profiles", // macOS
            System.getProperty("user.home") + "/.mozilla/firefox" // Linux
        };
        
        for (String path : possiblePaths) {
            Path profileDir = Paths.get(path);
            if (Files.exists(profileDir)) {
                try {
                    return Files.walk(profileDir)
                        .filter(Files::isDirectory)
                        .filter(p -> p.getFileName().toString().endsWith(".default") || 
                                   p.getFileName().toString().contains("default"))
                        .findFirst()
                        .map(Path::toString)
                        .orElse(null);
                } catch (Exception e) {
                    log.debug("Error finding Firefox profile", e);
                }
            }
        }
        
        return null;
    }
    
    private Optional<String> extractFromFirefoxDatabase(String dbPath, String serviceName) throws SQLException {
        String url = "jdbc:sqlite:" + dbPath;
        
        String[] tokenCookieNames = {
            "auth_token", "session_token", "jwt", "access_token",
            serviceName + "_token", serviceName + "_auth"
        };
        
        try (Connection conn = DriverManager.getConnection(url)) {
            for (String cookieName : tokenCookieNames) {
                String sql = "SELECT value FROM moz_cookies WHERE name = ? AND host LIKE ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, cookieName);
                    stmt.setString(2, "%" + serviceName + "%");
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            String value = rs.getString("value");
                            if (value != null && !value.isEmpty()) {
                                return Optional.of(value);
                            }
                        }
                    }
                }
            }
        }
        
        return Optional.empty();
    }
}
```

### 4. Local Storage Extractor

```java
package com.noteflix.pcm.core.auth;

import lombok.extern.slf4j.Slf4j;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

/**
 * Extracts tokens from browser localStorage
 */
@Slf4j
public class LocalStorageExtractor {
    
    /**
     * Extract token from browser localStorage
     */
    public Optional<String> extractFromLocalStorage(String serviceName) {
        try {
            // Try Chrome localStorage
            Optional<String> chromeToken = extractFromChromeLocalStorage(serviceName);
            if (chromeToken.isPresent()) {
                return chromeToken;
            }
            
            // Try Edge localStorage
            Optional<String> edgeToken = extractFromEdgeLocalStorage(serviceName);
            return edgeToken;
            
        } catch (Exception e) {
            log.error("Error extracting from localStorage", e);
            return Optional.empty();
        }
    }
    
    private Optional<String> extractFromChromeLocalStorage(String serviceName) {
        String[] localStoragePaths = {
            System.getProperty("user.home") + "/AppData/Local/Google/Chrome/User Data/Default/Local Storage/leveldb", // Windows
            System.getProperty("user.home") + "/Library/Application Support/Google/Chrome/Default/Local Storage/leveldb", // macOS
            System.getProperty("user.home") + "/.config/google-chrome/Default/Local Storage/leveldb" // Linux
        };
        
        return extractFromLevelDB(localStoragePaths, serviceName);
    }
    
    private Optional<String> extractFromEdgeLocalStorage(String serviceName) {
        String[] localStoragePaths = {
            System.getProperty("user.home") + "/AppData/Local/Microsoft/Edge/User Data/Default/Local Storage/leveldb", // Windows
            System.getProperty("user.home") + "/Library/Application Support/Microsoft Edge/Default/Local Storage/leveldb" // macOS
        };
        
        return extractFromLevelDB(localStoragePaths, serviceName);
    }
    
    private Optional<String> extractFromLevelDB(String[] paths, String serviceName) {
        // Note: LevelDB files are binary and complex to parse
        // In practice, you might need a specialized library or tool
        // For now, we'll implement a simplified version
        
        for (String path : paths) {
            Path leveldbDir = Paths.get(path);
            if (Files.exists(leveldbDir)) {
                try {
                    // This is a simplified approach
                    // Real implementation would need proper LevelDB parsing
                    return searchLevelDBFiles(leveldbDir, serviceName);
                } catch (Exception e) {
                    log.debug("Failed to read from: {}", path);
                }
            }
        }
        
        return Optional.empty();
    }
    
    private Optional<String> searchLevelDBFiles(Path leveldbDir, String serviceName) {
        try {
            // Look for .ldb files and search for token patterns
            return Files.walk(leveldbDir)
                .filter(p -> p.toString().endsWith(".ldb") || p.toString().endsWith(".log"))
                .map(p -> searchFileForToken(p, serviceName))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
                
        } catch (Exception e) {
            log.debug("Error searching LevelDB files", e);
            return Optional.empty();
        }
    }
    
    private Optional<String> searchFileForToken(Path file, String serviceName) {
        try {
            // Read file as bytes and search for token patterns
            byte[] content = Files.readAllBytes(file);
            String stringContent = new String(content, "UTF-8");
            
            // Look for common token patterns
            String[] tokenKeys = {
                serviceName + "_token",
                "auth_token", 
                "access_token",
                "jwt_token",
                "session_token",
                "bearer_token"
            };
            
            for (String tokenKey : tokenKeys) {
                int keyIndex = stringContent.indexOf(tokenKey);
                if (keyIndex != -1) {
                    // Extract the token value following the key
                    String tokenValue = extractTokenValue(stringContent, keyIndex + tokenKey.length());
                    if (tokenValue != null && !tokenValue.isEmpty()) {
                        return Optional.of(tokenValue);
                    }
                }
            }
            
        } catch (Exception e) {
            log.debug("Error reading file: {}", file);
        }
        
        return Optional.empty();
    }
    
    private String extractTokenValue(String content, int startIndex) {
        // Skip whitespace and separators
        int valueStart = startIndex;
        while (valueStart < content.length() && 
               (Character.isWhitespace(content.charAt(valueStart)) || 
                content.charAt(valueStart) == ':' || 
                content.charAt(valueStart) == '=' ||
                content.charAt(valueStart) == '"')) {
            valueStart++;
        }
        
        if (valueStart >= content.length()) {
            return null;
        }
        
        // Extract value until whitespace or delimiter
        int valueEnd = valueStart;
        while (valueEnd < content.length() && 
               !Character.isWhitespace(content.charAt(valueEnd)) && 
               content.charAt(valueEnd) != '"' &&
               content.charAt(valueEnd) != ',' &&
               content.charAt(valueEnd) != '}' &&
               content.charAt(valueEnd) != '\0') {
            valueEnd++;
        }
        
        if (valueEnd > valueStart) {
            String token = content.substring(valueStart, valueEnd);
            // Validate token format (basic check)
            if (token.length() > 20 && !token.contains(" ")) {
                return token;
            }
        }
        
        return null;
    }
}
```

### 5. Windows Registry Extractor

```java
package com.noteflix.pcm.core.auth;

import lombok.extern.slf4j.Slf4j;
import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * Extracts tokens from Windows Registry
 * Useful when SSO system stores tokens in registry
 */
@Slf4j
public class RegistryExtractor {
    
    /**
     * Extract token from Windows Registry
     */
    public Optional<String> extractFromRegistry(String serviceName) {
        try {
            // Try HKEY_CURRENT_USER first
            Optional<String> userToken = extractFromUserRegistry(serviceName);
            if (userToken.isPresent()) {
                return userToken;
            }
            
            // Try HKEY_LOCAL_MACHINE
            Optional<String> machineToken = extractFromMachineRegistry(serviceName);
            return machineToken;
            
        } catch (Exception e) {
            log.error("Error extracting from Windows Registry", e);
            return Optional.empty();
        }
    }
    
    private Optional<String> extractFromUserRegistry(String serviceName) {
        try {
            // Common registry paths for SSO tokens
            String[] registryPaths = {
                "Software/Microsoft/Windows/CurrentVersion/Authentication/LogonUI/SessionData",
                "Software/" + serviceName + "/Auth",
                "Software/SSO/Tokens",
                "Software/Enterprise/SSO/" + serviceName
            };
            
            for (String registryPath : registryPaths) {
                Optional<String> token = readRegistryValue(Preferences.userRoot(), registryPath, serviceName);
                if (token.isPresent()) {
                    return token;
                }
            }
            
        } catch (Exception e) {
            log.debug("Error reading from user registry", e);
        }
        
        return Optional.empty();
    }
    
    private Optional<String> extractFromMachineRegistry(String serviceName) {
        try {
            // System-wide registry paths
            String[] registryPaths = {
                "Software/" + serviceName + "/Auth",
                "Software/SSO/Tokens",
                "Software/Enterprise/Authentication"
            };
            
            for (String registryPath : registryPaths) {
                Optional<String> token = readRegistryValue(Preferences.systemRoot(), registryPath, serviceName);
                if (token.isPresent()) {
                    return token;
                }
            }
            
        } catch (Exception e) {
            log.debug("Error reading from machine registry", e);
        }
        
        return Optional.empty();
    }
    
    private Optional<String> readRegistryValue(Preferences root, String path, String serviceName) {
        try {
            Preferences prefs = root.node(path);
            
            // Try different token key names
            String[] tokenKeys = {
                "token",
                "auth_token", 
                "access_token",
                serviceName + "_token",
                "jwt",
                "session_token",
                "sso_token"
            };
            
            for (String key : tokenKeys) {
                String value = prefs.get(key, null);
                if (value != null && !value.isEmpty() && isValidToken(value)) {
                    log.info("Found token in registry: {}/{}", path, key);
                    return Optional.of(value);
                }
            }
            
        } catch (Exception e) {
            log.debug("Error reading registry path: {}", path);
        }
        
        return Optional.empty();
    }
    
    private boolean isValidToken(String token) {
        // Basic token validation
        return token.length() > 10 && 
               !token.contains(" ") && 
               (token.startsWith("eyJ") || // JWT
                token.startsWith("Bearer ") ||
                token.matches("[A-Za-z0-9+/=]{20,}")); // Base64-like
    }
}
```

### 6. File Token Extractor

```java
package com.noteflix.pcm.core.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Extracts tokens from shared files
 * Common locations where SSO systems store tokens
 */
@Slf4j
public class FileTokenExtractor {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Extract token from shared file locations
     */
    public Optional<String> extractFromFile(String serviceName) {
        try {
            // Try JSON files first
            Optional<String> jsonToken = extractFromJsonFiles(serviceName);
            if (jsonToken.isPresent()) {
                return jsonToken;
            }
            
            // Try properties files
            Optional<String> propsToken = extractFromPropertiesFiles(serviceName);
            if (propsToken.isPresent()) {
                return propsToken;
            }
            
            // Try plain text files
            Optional<String> textToken = extractFromTextFiles(serviceName);
            return textToken;
            
        } catch (Exception e) {
            log.error("Error extracting from files", e);
            return Optional.empty();
        }
    }
    
    private Optional<String> extractFromJsonFiles(String serviceName) {
        String[] jsonFilePaths = {
            // Common shared locations
            System.getProperty("user.home") + "/.auth/tokens.json",
            System.getProperty("user.home") + "/.sso/credentials.json",
            System.getProperty("user.home") + "/.config/" + serviceName + "/auth.json",
            
            // Application data directories
            System.getProperty("user.home") + "/AppData/Local/" + serviceName + "/auth.json", // Windows
            System.getProperty("user.home") + "/Library/Application Support/" + serviceName + "/auth.json", // macOS
            
            // Temp directories (less secure but sometimes used)
            System.getProperty("java.io.tmpdir") + "/" + serviceName + "_auth.json",
            
            // Current directory
            "./auth.json",
            "./" + serviceName + "_auth.json"
        };
        
        for (String filePath : jsonFilePaths) {
            try {
                Path path = Paths.get(filePath);
                if (Files.exists(path) && Files.isReadable(path)) {
                    String content = Files.readString(path);
                    Optional<String> token = parseJsonForToken(content, serviceName);
                    if (token.isPresent()) {
                        log.info("Found token in JSON file: {}", filePath);
                        return token;
                    }
                }
            } catch (Exception e) {
                log.debug("Error reading JSON file: {}", filePath);
            }
        }
        
        return Optional.empty();
    }
    
    private Optional<String> parseJsonForToken(String jsonContent, String serviceName) {
        try {
            JsonNode root = objectMapper.readTree(jsonContent);
            
            // Try different JSON structures
            String[] tokenPaths = {
                serviceName + ".token",
                serviceName + ".access_token", 
                serviceName + ".auth_token",
                "tokens." + serviceName,
                "auth." + serviceName + ".token",
                "credentials." + serviceName + ".token",
                "token",
                "access_token",
                "auth_token",
                "jwt",
                "bearer_token"
            };
            
            for (String tokenPath : tokenPaths) {
                JsonNode tokenNode = getNestedJsonValue(root, tokenPath);
                if (tokenNode != null && tokenNode.isTextual()) {
                    String token = tokenNode.asText();
                    if (isValidToken(token)) {
                        return Optional.of(token);
                    }
                }
            }
            
        } catch (Exception e) {
            log.debug("Error parsing JSON content", e);
        }
        
        return Optional.empty();
    }
    
    private JsonNode getNestedJsonValue(JsonNode root, String path) {
        String[] parts = path.split("\\.");
        JsonNode current = root;
        
        for (String part : parts) {
            if (current != null && current.has(part)) {
                current = current.get(part);
            } else {
                return null;
            }
        }
        
        return current;
    }
    
    private Optional<String> extractFromPropertiesFiles(String serviceName) {
        String[] propsFilePaths = {
            System.getProperty("user.home") + "/.auth/tokens.properties",
            System.getProperty("user.home") + "/.sso/auth.properties",
            System.getProperty("user.home") + "/.config/" + serviceName + "/auth.properties",
            "./auth.properties",
            "./" + serviceName + ".properties"
        };
        
        for (String filePath : propsFilePaths) {
            try {
                Path path = Paths.get(filePath);
                if (Files.exists(path) && Files.isReadable(path)) {
                    Properties props = new Properties();
                    props.load(Files.newInputStream(path));
                    
                    Optional<String> token = extractTokenFromProperties(props, serviceName);
                    if (token.isPresent()) {
                        log.info("Found token in properties file: {}", filePath);
                        return token;
                    }
                }
            } catch (Exception e) {
                log.debug("Error reading properties file: {}", filePath);
            }
        }
        
        return Optional.empty();
    }
    
    private Optional<String> extractTokenFromProperties(Properties props, String serviceName) {
        String[] tokenKeys = {
            serviceName + ".token",
            serviceName + ".auth_token",
            serviceName + ".access_token",
            "token",
            "auth_token", 
            "access_token",
            "jwt",
            "session_token"
        };
        
        for (String key : tokenKeys) {
            String value = props.getProperty(key);
            if (value != null && isValidToken(value)) {
                return Optional.of(value);
            }
        }
        
        return Optional.empty();
    }
    
    private Optional<String> extractFromTextFiles(String serviceName) {
        String[] textFilePaths = {
            System.getProperty("user.home") + "/.auth/token",
            System.getProperty("user.home") + "/.sso/token", 
            System.getProperty("user.home") + "/." + serviceName + "_token",
            System.getProperty("java.io.tmpdir") + "/" + serviceName + "_token.txt"
        };
        
        for (String filePath : textFilePaths) {
            try {
                Path path = Paths.get(filePath);
                if (Files.exists(path) && Files.isReadable(path)) {
                    String content = Files.readString(path).trim();
                    if (isValidToken(content)) {
                        log.info("Found token in text file: {}", filePath);
                        return Optional.of(content);
                    }
                }
            } catch (Exception e) {
                log.debug("Error reading text file: {}", filePath);
            }
        }
        
        return Optional.empty();
    }
    
    private boolean isValidToken(String token) {
        return token != null &&
               token.length() > 20 &&
               !token.contains("\\n") &&
               !token.contains("\\r") &&
               (token.startsWith("eyJ") || // JWT
                token.startsWith("Bearer ") ||
                token.matches("[A-Za-z0-9+/=_-]{20,}")); // Token-like pattern
    }
}
```

### 7. Enhanced LLM Provider Config với SSO

```java
package com.noteflix.pcm.llm.model;

import com.noteflix.pcm.core.auth.SSOTokenManager;
import lombok.Builder;
import lombok.Data;
import java.util.Map;
import java.util.Optional;

/**
 * Enhanced LLMProviderConfig with SSO support
 */
@Data
@Builder
public class SSOLLMProviderConfig extends LLMProviderConfig {
    
    /**
     * SSO service name for token extraction
     */
    private String ssoServiceName;
    
    /**
     * Whether to use SSO token instead of static token
     */
    @Builder.Default
    private Boolean useSSOToken = false;
    
    /**
     * Fallback static token if SSO token not available
     */
    private String fallbackToken;
    
    /**
     * Get the actual token to use (SSO or static)
     */
    @Override
    public String getToken() {
        if (useSSOToken && ssoServiceName != null) {
            SSOTokenManager ssoManager = SSOTokenManager.getInstance();
            Optional<String> ssoToken = ssoManager.getToken(ssoServiceName);
            
            if (ssoToken.isPresent()) {
                return ssoToken.get();
            } else if (fallbackToken != null) {
                return fallbackToken;
            } else {
                throw new IllegalStateException("No SSO token available for service: " + ssoServiceName);
            }
        }
        
        return super.getToken();
    }
    
    /**
     * Check if SSO authentication is available
     */
    public boolean isSSOAuthenticated() {
        if (!useSSOToken || ssoServiceName == null) {
            return false;
        }
        
        SSOTokenManager ssoManager = SSOTokenManager.getInstance();
        return ssoManager.isAuthenticated(ssoServiceName);
    }
}
```

## 🚀 Cách Sử Dụng SSO Integration

### 1. Basic SSO Setup

```java
public class SSOIntegrationExample {
    
    public void setupWithSSO() {
        // Initialize SSO token manager
        SSOTokenManager ssoManager = SSOTokenManager.getInstance();
        
        // Check if user is authenticated
        if (ssoManager.isAuthenticated("mycompany-portal")) {
            System.out.println("✅ User already authenticated via SSO");
        } else {
            System.out.println("❌ No SSO token found, user needs to login");
            return;
        }
        
        // Configure LLM service with SSO
        SSOLLMProviderConfig config = SSOLLMProviderConfig.builder()
            .provider(LLMProviderConfig.Provider.OPENAI)
            .name("Company OpenAI with SSO")
            .url("https://api.openai.com/v1/chat/completions")
            .ssoServiceName("mycompany-portal")
            .useSSOToken(true)
            .fallbackToken(System.getenv("OPENAI_API_KEY")) // Fallback
            .model("gpt-4")
            .build();
        
        // Initialize LLM service
        LLMService llmService = new LLMService();
        llmService.initialize(config);
        
        // Use normally
        String response = llmService.chat("Hello from SSO authenticated user!");
        System.out.println("Response: " + response);
    }
}
```

### 2. Custom Token Injection

```java
public class CustomTokenExample {
    
    public void injectCustomToken() {
        SSOTokenManager ssoManager = SSOTokenManager.getInstance();
        
        // Manually inject a token (e.g., from custom source)
        String customToken = getTokenFromCustomSource();
        ssoManager.cacheToken("myservice", customToken, 3600); // Cache for 1 hour
        
        // Now use with LLM
        SSOLLMProviderConfig config = SSOLLMProviderConfig.builder()
            .provider(LLMProviderConfig.Provider.CUSTOM)
            .url("https://mycompany.com/api/llm")
            .ssoServiceName("myservice")
            .useSSOToken(true)
            .model("company-model")
            .build();
        
        LLMService llmService = new LLMService();
        llmService.initialize(config);
        
        String response = llmService.chat("Test with custom token");
    }
    
    private String getTokenFromCustomSource() {
        // Your custom logic here
        // Could be from database, API call, file, etc.
        return "custom-token-from-your-source";
    }
}
```

### 3. Multi-Service SSO

```java
public class MultiServiceSSOExample {
    
    public void setupMultipleServices() {
        SSOTokenManager ssoManager = SSOTokenManager.getInstance();
        
        // Check authentication for multiple services
        String[] services = {"openai-service", "anthropic-service", "internal-llm"};
        
        for (String service : services) {
            if (ssoManager.isAuthenticated(service)) {
                System.out.println("✅ " + service + " is authenticated");
                setupServiceWithSSO(service);
            } else {
                System.out.println("❌ " + service + " requires authentication");
            }
        }
    }
    
    private void setupServiceWithSSO(String serviceName) {
        SSOLLMProviderConfig config = SSOLLMProviderConfig.builder()
            .provider(determineProvider(serviceName))
            .name(serviceName + " with SSO")
            .url(getServiceURL(serviceName))
            .ssoServiceName(serviceName)
            .useSSOToken(true)
            .model(getDefaultModel(serviceName))
            .build();
        
        LLMService llmService = new LLMService();
        llmService.initialize(config);
        
        // Test the connection
        try {
            String testResponse = llmService.chat("Test connection");
            System.out.println(serviceName + " connected successfully");
        } catch (Exception e) {
            System.out.println(serviceName + " connection failed: " + e.getMessage());
        }
    }
    
    private LLMProviderConfig.Provider determineProvider(String serviceName) {
        switch (serviceName) {
            case "openai-service": return LLMProviderConfig.Provider.OPENAI;
            case "anthropic-service": return LLMProviderConfig.Provider.ANTHROPIC;
            default: return LLMProviderConfig.Provider.CUSTOM;
        }
    }
    
    private String getServiceURL(String serviceName) {
        // Return appropriate URL for each service
        switch (serviceName) {
            case "openai-service": return "https://api.openai.com/v1/chat/completions";
            case "anthropic-service": return "https://api.anthropic.com/v1/messages";
            default: return "https://internal.company.com/api/llm";
        }
    }
    
    private String getDefaultModel(String serviceName) {
        switch (serviceName) {
            case "openai-service": return "gpt-4";
            case "anthropic-service": return "claude-3-5-sonnet-20241022";
            default: return "company-model-v1";
        }
    }
}
```

### 4. Token Refresh và Error Handling

```java
public class TokenRefreshExample {
    
    private LLMService llmService;
    private SSOLLMProviderConfig config;
    
    public void setupWithAutoRefresh() {
        config = SSOLLMProviderConfig.builder()
            .provider(LLMProviderConfig.Provider.OPENAI)
            .url("https://api.openai.com/v1/chat/completions")
            .ssoServiceName("company-portal")
            .useSSOToken(true)
            .model("gpt-4")
            .build();
        
        llmService = new LLMService();
        llmService.initialize(config);
    }
    
    public String chatWithAutoTokenRefresh(String message) {
        try {
            return llmService.chat(message);
            
        } catch (LLMProviderException e) {
            if (e.getMessage().contains("401") || e.getMessage().contains("Unauthorized")) {
                System.out.println("Token expired, attempting refresh...");
                
                if (refreshToken()) {
                    // Retry with new token
                    try {
                        return llmService.chat(message);
                    } catch (Exception retryException) {
                        throw new RuntimeException("Failed even after token refresh", retryException);
                    }
                } else {
                    throw new RuntimeException("Token refresh failed, user needs to re-authenticate");
                }
            } else {
                throw e; // Re-throw other exceptions
            }
        }
    }
    
    private boolean refreshToken() {
        try {
            SSOTokenManager ssoManager = SSOTokenManager.getInstance();
            
            // Clear cached token to force refresh
            ssoManager.clearTokens();
            
            // Try to extract fresh token
            TokenExtractor extractor = new TokenExtractor();
            Optional<String> freshToken = extractor.extractToken(config.getSsoServiceName());
            
            if (freshToken.isPresent()) {
                // Cache the fresh token
                ssoManager.cacheToken(config.getSsoServiceName(), freshToken.get(), 3600);
                
                // Re-initialize LLM service with fresh token
                llmService.initialize(config);
                
                System.out.println("✅ Token refreshed successfully");
                return true;
            } else {
                System.out.println("❌ Could not obtain fresh token");
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error during token refresh: " + e.getMessage());
            return false;
        }
    }
}
```

## 🔧 Configuration Examples

### 1. Configuration File

Tạo file `sso-config.json`:

```json
{
  "sso": {
    "enabled": true,
    "services": {
      "company-portal": {
        "displayName": "Company SSO Portal",
        "tokenSources": ["cookies", "localStorage", "registry"],
        "cookieNames": ["auth_token", "sso_token", "company_auth"],
        "localStorageKeys": ["authToken", "companyAuth"],
        "registryPaths": [
          "Software/Company/SSO/Auth",
          "Software/Company/Portal/Token"
        ],
        "filePaths": [
          "${user.home}/.company/auth.json",
          "${user.home}/AppData/Local/Company/auth.json"
        ]
      },
      "openai-service": {
        "displayName": "Company OpenAI Service",
        "tokenSources": ["files", "registry"],
        "filePaths": [
          "${user.home}/.openai/company_token",
          "/shared/tokens/openai.json"
        ]
      }
    }
  },
  "llm": {
    "providers": [
      {
        "name": "Company OpenAI",
        "provider": "OPENAI",
        "url": "https://api.openai.com/v1/chat/completions",
        "ssoServiceName": "openai-service",
        "useSSOToken": true,
        "fallbackToken": "${env.OPENAI_API_KEY}",
        "model": "gpt-4"
      },
      {
        "name": "Internal LLM",
        "provider": "CUSTOM", 
        "url": "https://internal.company.com/api/llm",
        "ssoServiceName": "company-portal",
        "useSSOToken": true,
        "model": "company-llm-v1",
        "headers": {
          "X-Company-Auth": "sso-token"
        }
      }
    ]
  }
}
```

### 2. Configuration Loader

```java
package com.noteflix.pcm.core.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

@Slf4j
public class SSOConfigurationLoader {
    
    private static final String DEFAULT_CONFIG_FILE = "sso-config.json";
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public SSOConfiguration loadConfiguration() {
        return loadConfiguration(DEFAULT_CONFIG_FILE);
    }
    
    public SSOConfiguration loadConfiguration(String configFile) {
        try {
            String content = Files.readString(Paths.get(configFile));
            JsonNode root = objectMapper.readTree(content);
            
            SSOConfiguration config = new SSOConfiguration();
            
            // Load SSO settings
            JsonNode ssoNode = root.get("sso");
            if (ssoNode != null) {
                config.setSsoEnabled(ssoNode.get("enabled").asBoolean());
                loadSSOServices(config, ssoNode.get("services"));
            }
            
            // Load LLM providers
            JsonNode llmNode = root.get("llm");
            if (llmNode != null) {
                loadLLMProviders(config, llmNode.get("providers"));
            }
            
            return config;
            
        } catch (Exception e) {
            log.error("Failed to load SSO configuration", e);
            return new SSOConfiguration(); // Return default config
        }
    }
    
    private void loadSSOServices(SSOConfiguration config, JsonNode servicesNode) {
        servicesNode.fieldNames().forEachRemaining(serviceName -> {
            JsonNode serviceNode = servicesNode.get(serviceName);
            SSOServiceConfig serviceConfig = new SSOServiceConfig();
            
            serviceConfig.setServiceName(serviceName);
            serviceConfig.setDisplayName(serviceNode.get("displayName").asText());
            
            // Load token sources
            JsonNode sourcesNode = serviceNode.get("tokenSources");
            if (sourcesNode != null && sourcesNode.isArray()) {
                List<String> sources = new ArrayList<>();
                sourcesNode.forEach(source -> sources.add(source.asText()));
                serviceConfig.setTokenSources(sources);
            }
            
            // Load cookie names
            loadStringArray(serviceNode, "cookieNames", serviceConfig::setCookieNames);
            
            // Load localStorage keys
            loadStringArray(serviceNode, "localStorageKeys", serviceConfig::setLocalStorageKeys);
            
            // Load registry paths
            loadStringArray(serviceNode, "registryPaths", serviceConfig::setRegistryPaths);
            
            // Load file paths
            loadStringArray(serviceNode, "filePaths", serviceConfig::setFilePaths);
            
            config.addSSOService(serviceConfig);
        });
    }
    
    private void loadStringArray(JsonNode parent, String fieldName, java.util.function.Consumer<List<String>> setter) {
        JsonNode arrayNode = parent.get(fieldName);
        if (arrayNode != null && arrayNode.isArray()) {
            List<String> items = new ArrayList<>();
            arrayNode.forEach(item -> items.add(expandVariables(item.asText())));
            setter.accept(items);
        }
    }
    
    private String expandVariables(String value) {
        // Expand ${user.home} and other variables
        return value
            .replace("${user.home}", System.getProperty("user.home"))
            .replace("${java.io.tmpdir}", System.getProperty("java.io.tmpdir"))
            .replace("${env.", System.getenv().getOrDefault("", ""));
    }
    
    private void loadLLMProviders(SSOConfiguration config, JsonNode providersNode) {
        if (providersNode != null && providersNode.isArray()) {
            providersNode.forEach(providerNode -> {
                SSOLLMProviderConfig providerConfig = SSOLLMProviderConfig.builder()
                    .provider(LLMProviderConfig.Provider.valueOf(providerNode.get("provider").asText()))
                    .name(providerNode.get("name").asText())
                    .url(providerNode.get("url").asText())
                    .ssoServiceName(providerNode.get("ssoServiceName").asText())
                    .useSSOToken(providerNode.get("useSSOToken").asBoolean())
                    .fallbackToken(expandVariables(providerNode.path("fallbackToken").asText()))
                    .model(providerNode.get("model").asText())
                    .build();
                
                config.addLLMProvider(providerConfig);
            });
        }
    }
}
```

## ⚠️ Security Considerations

### 1. Token Security

```java
package com.noteflix.pcm.core.auth;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.security.SecureRandom;
import lombok.extern.slf4j.Slf4j;

/**
 * Secure token storage and handling
 */
@Slf4j
public class SecureTokenStorage {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private final SecretKey secretKey;
    
    public SecureTokenStorage() {
        this.secretKey = generateOrLoadKey();
    }
    
    /**
     * Encrypt token before storing
     */
    public String encryptToken(String plainToken) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainToken.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("Failed to encrypt token", e);
            throw new RuntimeException("Token encryption failed", e);
        }
    }
    
    /**
     * Decrypt token when retrieving
     */
    public String decryptToken(String encryptedToken) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedToken));
            return new String(decryptedBytes);
        } catch (Exception e) {
            log.error("Failed to decrypt token", e);
            throw new RuntimeException("Token decryption failed", e);
        }
    }
    
    private SecretKey generateOrLoadKey() {
        try {
            // In production, load key from secure location
            // For demo, generate a new key
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(256);
            return keyGen.generateKey();
        } catch (Exception e) {
            log.error("Failed to generate encryption key", e);
            throw new RuntimeException("Key generation failed", e);
        }
    }
    
    /**
     * Securely clear sensitive data from memory
     */
    public void clearSensitiveData(String[] sensitiveStrings) {
        for (String sensitive : sensitiveStrings) {
            if (sensitive != null) {
                // Overwrite string content (limited effectiveness in Java)
                char[] chars = sensitive.toCharArray();
                java.util.Arrays.fill(chars, '\\0');
            }
        }
    }
}
```

### 2. Audit Logging

```java
package com.noteflix.pcm.core.auth;

import lombok.extern.slf4j.Slf4j;
import java.time.Instant;

/**
 * Security audit logging for token access
 */
@Slf4j
public class SecurityAuditLogger {
    
    public void logTokenAccess(String serviceName, String operation, boolean success) {
        String logEntry = String.format(
            "TOKEN_ACCESS|service=%s|operation=%s|success=%s|timestamp=%s|user=%s",
            serviceName, 
            operation, 
            success,
            Instant.now(),
            System.getProperty("user.name")
        );
        
        if (success) {
            log.info("Security: {}", logEntry);
        } else {
            log.warn("Security: {}", logEntry);
        }
    }
    
    public void logSuspiciousActivity(String activity, String details) {
        String logEntry = String.format(
            "SECURITY_ALERT|activity=%s|details=%s|timestamp=%s|user=%s",
            activity,
            details,
            Instant.now(), 
            System.getProperty("user.name")
        );
        
        log.error("Security Alert: {}", logEntry);
    }
}
```

## 🧪 Testing SSO Integration

### 1. Unit Tests

```java
@Test
public void testSSOTokenExtraction() {
    // Mock browser cookie database
    SSOTokenManager ssoManager = SSOTokenManager.getInstance();
    
    // Test token caching
    ssoManager.cacheToken("test-service", "test-token-123", 3600);
    
    Optional<String> token = ssoManager.getToken("test-service");
    assertTrue(token.isPresent());
    assertEquals("test-token-123", token.get());
    
    // Test expiration
    ssoManager.cacheToken("expired-service", "expired-token", -1); // Already expired
    Optional<String> expiredToken = ssoManager.getToken("expired-service");
    assertFalse(expiredToken.isPresent());
}

@Test
public void testSSOLLMProviderConfig() {
    SSOTokenManager ssoManager = SSOTokenManager.getInstance();
    ssoManager.cacheToken("test-service", "sso-token-456", 3600);
    
    SSOLLMProviderConfig config = SSOLLMProviderConfig.builder()
        .provider(LLMProviderConfig.Provider.OPENAI)
        .url("https://api.openai.com/v1/chat/completions")
        .ssoServiceName("test-service")
        .useSSOToken(true)
        .fallbackToken("fallback-token")
        .build();
    
    assertEquals("sso-token-456", config.getToken());
    assertTrue(config.isSSOAuthenticated());
}
```

### 2. Integration Tests

```java
@Test
@EnabledIf("#{environment.containsKey('TEST_SSO_TOKEN')}")
public void testRealSSOIntegration() {
    // Only run if real SSO token is available for testing
    String testToken = System.getenv("TEST_SSO_TOKEN");
    
    SSOTokenManager ssoManager = SSOTokenManager.getInstance();
    ssoManager.cacheToken("integration-test", testToken, 3600);
    
    SSOLLMProviderConfig config = SSOLLMProviderConfig.builder()
        .provider(LLMProviderConfig.Provider.OPENAI)
        .url("https://api.openai.com/v1/chat/completions")
        .ssoServiceName("integration-test")
        .useSSOToken(true)
        .model("gpt-3.5-turbo")
        .build();
    
    LLMService service = new LLMService();
    service.initialize(config);
    
    String response = service.chat("Test SSO integration");
    assertNotNull(response);
    assertFalse(response.isEmpty());
}
```

## 🚀 Deployment Guide

### 1. Production Setup

```bash
# 1. Create SSO configuration
mkdir -p /opt/pcm-desktop/config
cp sso-config.json /opt/pcm-desktop/config/

# 2. Set up security
chmod 600 /opt/pcm-desktop/config/sso-config.json
chown pcm-user:pcm-group /opt/pcm-desktop/config/sso-config.json

# 3. Configure environment
export PCM_SSO_CONFIG="/opt/pcm-desktop/config/sso-config.json"
export PCM_SSO_ENABLED=true
export PCM_AUDIT_LOG="/var/log/pcm-desktop/security.log"
```

### 2. Monitoring & Alerts

```bash
# Monitor SSO token access
tail -f /var/log/pcm-desktop/security.log | grep "TOKEN_ACCESS"

# Alert on failed authentication
grep "SECURITY_ALERT" /var/log/pcm-desktop/security.log | 
  mail -s "PCM Desktop Security Alert" admin@company.com
```

---

## 📚 Best Practices Summary

1. **Security**:
    - ✅ Encrypt tokens in memory and storage
    - ✅ Use secure file permissions
    - ✅ Implement audit logging
    - ✅ Clear sensitive data from memory

2. **Reliability**:
    - ✅ Implement token refresh logic
    - ✅ Provide fallback authentication
    - ✅ Handle token expiration gracefully
    - ✅ Monitor authentication status

3. **Performance**:
    - ✅ Cache tokens with expiration
    - ✅ Minimize token extraction overhead
    - ✅ Use connection pooling
    - ✅ Implement async token refresh

4. **Maintainability**:
    - ✅ Use configuration files
    - ✅ Support multiple token sources
    - ✅ Provide clear error messages
    - ✅ Implement comprehensive logging

Với hệ thống SSO integration này, PCM Desktop có thể seamlessly tích hợp với bất kỳ enterprise SSO system nào và tự động
sử dụng tokens để gọi APIs mà không cần user intervention!