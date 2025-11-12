# SSO Integration Guide - Single Sign-On Implementation

## 📋 Table of Contents

1. [Overview](#overview)
2. [SSO Architecture](#sso-architecture)
3. [Authentication Flow](#authentication-flow)
4. [Token Management](#token-management)
5. [API Integration](#api-integration)
6. [Security Best Practices](#security-best-practices)
7. [Implementation Patterns](#implementation-patterns)
8. [Troubleshooting](#troubleshooting)

---

## Overview

### What is SSO?

**Single Sign-On (SSO)** là cơ chế xác thực cho phép người dùng đăng nhập một lần vào một **Portal trung tâm** và tự động được xác thực cho tất cả các ứng dụng liên kết mà không cần đăng nhập lại.

### Benefits

✅ **User Experience**: Đăng nhập một lần, sử dụng mọi ứng dụng  
✅ **Security**: Quản lý xác thực tập trung  
✅ **Productivity**: Giảm thời gian đăng nhập lặp lại  
✅ **Management**: Dễ dàng thu hồi quyền truy cập  

### Use Case (Your System)

```
┌──────────────────────────────────────────────────────────┐
│  User logs into Portal (once)                            │
│  ↓                                                        │
│  Portal generates Authentication Token                   │
│  ↓                                                        │
│  All apps on the machine automatically get the token     │
│  ↓                                                        │
│  Apps call APIs using the token (no re-login needed)     │
└──────────────────────────────────────────────────────────┘
```

---

## SSO Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    User's Computer                              │
│                                                                 │
│  ┌──────────────────┐                                          │
│  │   SSO Portal     │  (1) User Login                          │
│  │  (Web/Desktop)   │◄──────────────────────┐                  │
│  └──────────────────┘                       │                  │
│           │                                 │                  │
│           │ (2) Generate Token              │                  │
│           ↓                                 │                  │
│  ┌──────────────────────────────────────────┴─────────────┐   │
│  │     Token Storage (Central)                             │   │
│  │  • File System (encrypted)                              │   │
│  │  • System Keychain/Credential Manager                   │   │
│  │  • Shared Memory                                        │   │
│  │  • Local HTTP Server (localhost:port)                   │   │
│  └─────────────────────────────────────────────────────────┘   │
│           │                                                     │
│           │ (3) Apps read token                                │
│           ↓                                                     │
│  ┌──────────────────┐   ┌──────────────────┐   ┌────────────┐│
│  │   App 1 (PCM)    │   │   App 2 (CRM)    │   │   App 3    ││
│  │                  │   │                  │   │            ││
│  │  ConversationApp │   │  Sales Dashboard │   │  Analytics ││
│  └──────────────────┘   └──────────────────┘   └────────────┘│
│           │                       │                     │      │
└───────────┼───────────────────────┼─────────────────────┼──────┘
            │                       │                     │
            │ (4) Call API with token                     │
            ↓                       ↓                     ↓
┌─────────────────────────────────────────────────────────────┐
│                    Backend API Server                       │
│  ┌─────────────────────────────────────────────────────┐   │
│  │         Token Validation Service                     │   │
│  │  • Validate JWT signature                            │   │
│  │  • Check token expiration                            │   │
│  │  • Verify user permissions                           │   │
│  └─────────────────────────────────────────────────────┘   │
│           │                                                  │
│           ↓                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ Conversation │  │     User     │  │    LLM       │     │
│  │   Service    │  │   Service    │  │   Service    │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

### Components

#### 1. **SSO Portal** (Identity Provider)
- Xử lý đăng nhập người dùng
- Sinh token (JWT/OAuth token)
- Quản lý session
- Làm mới token (refresh)

#### 2. **Token Storage** (Central Storage)
- Lưu trữ token trên máy người dùng
- Chia sẻ token giữa các ứng dụng
- Bảo mật token

#### 3. **Desktop Applications** (Service Providers)
- Đọc token từ Token Storage
- Gửi token khi call API
- Tự động làm mới token khi hết hạn

#### 4. **Backend API Server**
- Xác thực token
- Cung cấp dịch vụ cho apps
- Quản lý permissions

---

## Authentication Flow

### 1. Initial Login Flow

```
┌─────────┐         ┌─────────────┐         ┌──────────────┐
│  User   │         │ SSO Portal  │         │ Auth Server  │
└────┬────┘         └──────┬──────┘         └──────┬───────┘
     │                     │                        │
     │ (1) Open Portal     │                        │
     ├────────────────────>│                        │
     │                     │                        │
     │ (2) Enter credentials                        │
     ├────────────────────>│                        │
     │                     │ (3) Validate           │
     │                     ├───────────────────────>│
     │                     │                        │
     │                     │ (4) User info + token  │
     │                     │<───────────────────────┤
     │                     │                        │
     │                     │ (5) Store token        │
     │                     │ (File/Keychain/Server) │
     │                     ├───────────────┐        │
     │                     │               │        │
     │                     │<──────────────┘        │
     │ (6) Success         │                        │
     │<────────────────────┤                        │
     │                     │                        │
```

### 2. App Auto-Login Flow

```
┌──────────┐      ┌───────────────┐      ┌──────────┐
│  App     │      │ Token Storage │      │   API    │
│  (PCM)   │      │               │      │  Server  │
└────┬─────┘      └───────┬───────┘      └────┬─────┘
     │                    │                    │
     │ (1) App starts     │                    │
     ├──────────────┐     │                    │
     │              │     │                    │
     │<─────────────┘     │                    │
     │                    │                    │
     │ (2) Request token  │                    │
     ├───────────────────>│                    │
     │                    │                    │
     │ (3) Return token   │                    │
     │<───────────────────┤                    │
     │                    │                    │
     │ (4) Call API with token                 │
     ├────────────────────────────────────────>│
     │                    │                    │
     │                    │ (5) Validate token │
     │                    │    ├──────────┐    │
     │                    │    │          │    │
     │                    │    │<─────────┘    │
     │                    │                    │
     │ (6) Return data    │                    │
     │<────────────────────────────────────────┤
     │                    │                    │
```

### 3. Token Refresh Flow

```
┌──────────┐      ┌───────────────┐      ┌──────────┐
│  App     │      │ Token Storage │      │   Auth   │
│          │      │               │      │  Server  │
└────┬─────┘      └───────┬───────┘      └────┬─────┘
     │                    │                    │
     │ (1) API call fails │                    │
     │     (401 Unauthorized)                  │
     │<────────────────────────────────────────┤
     │                    │                    │
     │ (2) Request refresh│                    │
     ├───────────────────>│                    │
     │                    │                    │
     │                    │ (3) Refresh token  │
     │                    ├───────────────────>│
     │                    │                    │
     │                    │ (4) New token      │
     │                    │<───────────────────┤
     │                    │                    │
     │                    │ (5) Store new token│
     │                    ├──────────┐         │
     │                    │          │         │
     │                    │<─────────┘         │
     │                    │                    │
     │ (6) Return new token                    │
     │<───────────────────┤                    │
     │                    │                    │
     │ (7) Retry API call with new token       │
     ├────────────────────────────────────────>│
     │                    │                    │
```

---

## Token Management

### Token Types

#### 1. **JWT (JSON Web Token)** - RECOMMENDED ⭐

```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user123",
    "name": "John Doe",
    "email": "john@example.com",
    "roles": ["user", "admin"],
    "iat": 1699876543,
    "exp": 1699880143
  },
  "signature": "..."
}
```

**Format:**
```
eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMTIzIiwibmFtZSI6IkpvaG4gRG9lIiwiZW1haWwiOiJqb2huQGV4YW1wbGUuY29tIiwicm9sZXMiOlsidXNlciIsImFkbWluIl0sImlhdCI6MTY5OTg3NjU0MywiZXhwIjoxNjk5ODgwMTQzfQ.signature
```

**Benefits:**
- ✅ Self-contained (chứa user info)
- ✅ Stateless (không cần lưu trên server)
- ✅ Secure (signed với private key)
- ✅ Có expiration time

#### 2. **OAuth 2.0 Token**

```json
{
  "access_token": "eyJhbGciOiJ...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "tGzv3JOkF0XG5Qx2TlKW",
  "scope": "read write"
}
```

#### 3. **Session Token**

```
SESSION_ID=abc123def456ghi789
```

### Token Storage Options

#### Option 1: **File System (Encrypted)** - SIMPLE ⭐

```
Location: 
  Windows: C:\Users\<username>\.pcm\auth\token.enc
  macOS:   ~/.pcm/auth/token.enc
  Linux:   ~/.pcm/auth/token.enc

Format:
  Encrypted JSON file containing:
  {
    "access_token": "...",
    "refresh_token": "...",
    "expires_at": 1699880143,
    "user_id": "user123"
  }

Encryption:
  AES-256-GCM with machine-specific key
```

**Pros:**
- ✅ Simple to implement
- ✅ Works across all apps on the machine
- ✅ Persistent storage

**Cons:**
- ❌ Requires proper file permissions
- ❌ Need to implement encryption/decryption

#### Option 2: **System Keychain** - MOST SECURE ⭐⭐⭐

```
Windows: Windows Credential Manager
macOS:   Keychain Access
Linux:   libsecret / gnome-keyring
```

**Pros:**
- ✅ OS-level encryption
- ✅ Most secure
- ✅ Native OS integration

**Cons:**
- ❌ Platform-specific implementation
- ❌ More complex

#### Option 3: **Local HTTP Server** - DYNAMIC ⭐⭐

```
SSO Portal runs a local server:
  http://localhost:8765/api/token

Apps connect to this server to get token.
```

**Pros:**
- ✅ Real-time token updates
- ✅ Centralized control
- ✅ Easy token revocation

**Cons:**
- ❌ Requires SSO Portal to always run
- ❌ Port conflicts
- ❌ Network overhead

#### Option 4: **Environment Variables** - SIMPLE BUT LESS SECURE

```bash
export SSO_TOKEN="eyJhbGciOiJ..."
export SSO_REFRESH_TOKEN="tGzv3JOkF0XG..."
```

**Pros:**
- ✅ Very simple
- ✅ Works across processes

**Cons:**
- ❌ Less secure (visible in process list)
- ❌ Not persistent across reboots
- ❌ Can be accessed by any process

### Recommended: **Hybrid Approach**

```
1. Use System Keychain for long-term storage (refresh token)
2. Use File System for short-term cache (access token)
3. Use Environment Variables for current session
```

---

## API Integration

### 1. Sending Token with API Requests

#### Method A: **Authorization Header (RECOMMENDED)** ⭐

```java
// Java (using HttpURLConnection)
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestProperty("Authorization", "Bearer " + accessToken);

// Java (using HttpClient)
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://api.example.com/conversations"))
    .header("Authorization", "Bearer " + accessToken)
    .GET()
    .build();
```

```http
GET /api/conversations HTTP/1.1
Host: api.example.com
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Pros:**
- ✅ Standard HTTP header
- ✅ Works with REST APIs
- ✅ Not cached by browsers
- ✅ Easy to test

#### Method B: **Cookie**

```java
// Java
CookieManager cookieManager = new CookieManager();
CookieHandler.setDefault(cookieManager);

HttpCookie cookie = new HttpCookie("session_token", accessToken);
cookie.setDomain("api.example.com");
cookie.setPath("/");
cookie.setSecure(true);
cookie.setHttpOnly(true);

cookieManager.getCookieStore().add(URI.create("https://api.example.com"), cookie);
```

```http
GET /api/conversations HTTP/1.1
Host: api.example.com
Cookie: session_token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Pros:**
- ✅ Automatic browser handling
- ✅ HTTP-only flag for security
- ✅ Automatic expiration

**Cons:**
- ❌ CSRF vulnerability
- ❌ Limited to same domain
- ❌ More complex for desktop apps

#### Method C: **Query Parameter (NOT RECOMMENDED)**

```http
GET /api/conversations?token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Cons:**
- ❌ Token visible in URL
- ❌ Logged in server logs
- ❌ Cached by proxies
- ❌ **SECURITY RISK**

### 2. Token Validation on Backend

```java
// Backend API - Token validation middleware
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // 1. Extract token from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid token");
            return;
        }
        
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        
        try {
            // 2. Validate JWT signature and expiration
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            // 3. Extract user info
            String userId = claims.getSubject();
            List<String> roles = claims.get("roles", List.class);
            
            // 4. Set authentication in security context
            Authentication auth = new JWTAuthentication(userId, roles);
            SecurityContextHolder.getContext().setAuthentication(auth);
            
            // 5. Continue with request
            filterChain.doFilter(request, response);
            
        } catch (JwtException e) {
            // Token invalid or expired
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token: " + e.getMessage());
        }
    }
}
```

---

## Security Best Practices

### 1. Token Security

#### ✅ DO's

```java
// ✅ Use HTTPS for all API calls
String apiUrl = "https://api.example.com"; // NOT http://

// ✅ Store tokens encrypted
String encryptedToken = AES.encrypt(token, machineKey);
Files.write(tokenFile, encryptedToken.getBytes());

// ✅ Use short expiration times (15-60 minutes)
long expirationMinutes = 30;

// ✅ Implement token refresh
if (isTokenExpired(accessToken)) {
    accessToken = refreshToken(refreshToken);
}

// ✅ Validate token signature
Claims claims = Jwts.parserBuilder()
    .setSigningKey(publicKey) // Verify with public key
    .build()
    .parseClaimsJws(token)
    .getBody();

// ✅ Secure file permissions (owner-only read/write)
Files.setPosixFilePermissions(tokenFile, 
    PosixFilePermissions.fromString("rw-------")); // 600
```

#### ❌ DON'Ts

```java
// ❌ Don't store tokens in plain text
Files.write(tokenFile, token.getBytes()); // BAD!

// ❌ Don't use HTTP (unencrypted)
String apiUrl = "http://api.example.com"; // INSECURE!

// ❌ Don't put tokens in URLs
String url = "https://api.example.com?token=" + token; // BAD!

// ❌ Don't use long expiration times
long expirationHours = 24 * 365; // 1 year - TOO LONG!

// ❌ Don't log tokens
logger.info("Token: " + token); // SECURITY RISK!

// ❌ Don't hardcode secrets
String secret = "my-secret-key-123"; // BAD!
```

### 2. Token Refresh Strategy

```java
public class TokenManager {
    
    private String accessToken;
    private String refreshToken;
    private long expiresAt;
    
    public String getAccessToken() {
        // Check if token is about to expire (within 5 minutes)
        if (isTokenExpiringSoon()) {
            refreshAccessToken();
        }
        return accessToken;
    }
    
    private boolean isTokenExpiringSoon() {
        long now = System.currentTimeMillis() / 1000;
        long fiveMinutes = 5 * 60;
        return (expiresAt - now) < fiveMinutes;
    }
    
    private void refreshAccessToken() {
        try {
            // Call refresh endpoint
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://auth.example.com/token/refresh"))
                .header("Authorization", "Bearer " + refreshToken)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                TokenResponse tokenResponse = parseTokenResponse(response.body());
                this.accessToken = tokenResponse.getAccessToken();
                this.expiresAt = tokenResponse.getExpiresAt();
                
                // Save to storage
                saveTokens();
            } else {
                // Refresh failed - require re-login
                handleRefreshFailure();
            }
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            handleRefreshFailure();
        }
    }
}
```

### 3. Secure Token Storage Implementation

```java
public class SecureTokenStorage {
    
    private static final String TOKEN_FILE = System.getProperty("user.home") + "/.pcm/auth/token.enc";
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    
    /**
     * Save token securely
     */
    public void saveToken(TokenData tokenData) throws Exception {
        // 1. Serialize token data
        String json = objectMapper.writeValueAsString(tokenData);
        
        // 2. Encrypt with machine-specific key
        SecretKey key = getMachineSpecificKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        
        byte[] encryptedData = cipher.doFinal(json.getBytes(StandardCharsets.UTF_8));
        byte[] iv = cipher.getIV();
        
        // 3. Combine IV + encrypted data
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(iv.length);
        outputStream.write(iv);
        outputStream.write(encryptedData);
        
        // 4. Write to file with secure permissions
        Path tokenPath = Paths.get(TOKEN_FILE);
        Files.createDirectories(tokenPath.getParent());
        Files.write(tokenPath, outputStream.toByteArray());
        
        // 5. Set file permissions (owner-only read/write)
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            Files.setPosixFilePermissions(tokenPath, 
                PosixFilePermissions.fromString("rw-------"));
        }
        
        log.info("Token saved securely");
    }
    
    /**
     * Load token securely
     */
    public TokenData loadToken() throws Exception {
        Path tokenPath = Paths.get(TOKEN_FILE);
        if (!Files.exists(tokenPath)) {
            return null;
        }
        
        // 1. Read encrypted data
        byte[] fileData = Files.readAllBytes(tokenPath);
        
        // 2. Extract IV and encrypted data
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileData);
        int ivLength = inputStream.read();
        byte[] iv = new byte[ivLength];
        inputStream.read(iv);
        byte[] encryptedData = inputStream.readAllBytes();
        
        // 3. Decrypt
        SecretKey key = getMachineSpecificKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
        
        byte[] decryptedData = cipher.doFinal(encryptedData);
        String json = new String(decryptedData, StandardCharsets.UTF_8);
        
        // 4. Deserialize
        TokenData tokenData = objectMapper.readValue(json, TokenData.class);
        
        log.info("Token loaded successfully");
        return tokenData;
    }
    
    /**
     * Generate machine-specific encryption key
     */
    private SecretKey getMachineSpecificKey() throws Exception {
        // Use machine-specific identifier (e.g., MAC address, hostname)
        String machineId = getMachineIdentifier();
        
        // Derive key using PBKDF2
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(
            machineId.toCharArray(),
            "pcm-salt".getBytes(), // Salt
            65536,  // Iteration count
            256     // Key length
        );
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }
    
    private String getMachineIdentifier() throws Exception {
        // Get MAC address or other machine-specific ID
        InetAddress ip = InetAddress.getLocalHost();
        NetworkInterface network = NetworkInterface.getByInetAddress(ip);
        byte[] mac = network.getHardwareAddress();
        return DatatypeConverter.printHexBinary(mac);
    }
}

/**
 * Token data model
 */
@Data
@Builder
public class TokenData {
    private String accessToken;
    private String refreshToken;
    private long expiresAt;
    private String userId;
    private String userName;
    private List<String> roles;
}
```

---

## Implementation Patterns

### Pattern 1: Token Service (Recommended)

```java
package com.noteflix.pcm.auth;

import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

/**
 * Centralized token management service
 * 
 * Responsibilities:
 * - Load token from storage
 * - Refresh token when expired
 * - Provide token for API calls
 * 
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class TokenService {
    
    private final SecureTokenStorage tokenStorage;
    private final HttpClient httpClient;
    private final String authServerUrl;
    
    private TokenData cachedToken;
    
    public TokenService(String authServerUrl) {
        this.tokenStorage = new SecureTokenStorage();
        this.httpClient = HttpClient.newHttpClient();
        this.authServerUrl = authServerUrl;
        
        // Load token on initialization
        loadToken();
    }
    
    /**
     * Get valid access token (auto-refresh if needed)
     * 
     * @return Access token or empty if not authenticated
     */
    public Optional<String> getAccessToken() {
        if (cachedToken == null) {
            loadToken();
        }
        
        if (cachedToken == null) {
            log.warn("No token available - user not authenticated");
            return Optional.empty();
        }
        
        // Check if token is expired or expiring soon
        if (isTokenExpiringSoon(cachedToken)) {
            log.info("Token expiring soon, refreshing...");
            refreshToken();
        }
        
        return Optional.ofNullable(cachedToken).map(TokenData::getAccessToken);
    }
    
    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        return getAccessToken().isPresent();
    }
    
    /**
     * Get current user info
     */
    public Optional<UserInfo> getCurrentUser() {
        return Optional.ofNullable(cachedToken).map(token -> 
            UserInfo.builder()
                .userId(token.getUserId())
                .userName(token.getUserName())
                .roles(token.getRoles())
                .build()
        );
    }
    
    /**
     * Clear token (logout)
     */
    public void logout() {
        cachedToken = null;
        try {
            tokenStorage.deleteToken();
            log.info("User logged out");
        } catch (Exception e) {
            log.error("Failed to delete token", e);
        }
    }
    
    /**
     * Load token from storage
     */
    private void loadToken() {
        try {
            cachedToken = tokenStorage.loadToken();
            if (cachedToken != null) {
                log.info("Token loaded for user: {}", cachedToken.getUserId());
            }
        } catch (Exception e) {
            log.error("Failed to load token", e);
            cachedToken = null;
        }
    }
    
    /**
     * Check if token is expiring soon (within 5 minutes)
     */
    private boolean isTokenExpiringSoon(TokenData token) {
        long now = System.currentTimeMillis() / 1000;
        long fiveMinutes = 5 * 60;
        return (token.getExpiresAt() - now) < fiveMinutes;
    }
    
    /**
     * Refresh access token using refresh token
     */
    private void refreshToken() {
        if (cachedToken == null || cachedToken.getRefreshToken() == null) {
            log.error("Cannot refresh - no refresh token available");
            return;
        }
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(authServerUrl + "/token/refresh"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                    "{\"refresh_token\":\"" + cachedToken.getRefreshToken() + "\"}"
                ))
                .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                // Parse new token
                TokenResponse tokenResponse = parseTokenResponse(response.body());
                
                // Update cached token
                cachedToken.setAccessToken(tokenResponse.getAccessToken());
                cachedToken.setExpiresAt(tokenResponse.getExpiresAt());
                
                // Save to storage
                tokenStorage.saveToken(cachedToken);
                
                log.info("Token refreshed successfully");
            } else {
                log.error("Token refresh failed: HTTP {}", response.statusCode());
                // Clear invalid token
                logout();
            }
        } catch (Exception e) {
            log.error("Failed to refresh token", e);
            logout();
        }
    }
    
    /**
     * Parse token response from JSON
     */
    private TokenResponse parseTokenResponse(String json) {
        // TODO: Use Jackson or Gson to parse JSON
        // For now, simple implementation
        return null; // Placeholder
    }
}
```

### Pattern 2: HTTP Client Interceptor

```java
package com.noteflix.pcm.http;

import com.noteflix.pcm.auth.TokenService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP Client with automatic token injection
 * 
 * Usage:
 *   AuthenticatedHttpClient httpClient = new AuthenticatedHttpClient(tokenService);
 *   HttpResponse<String> response = httpClient.send(request);
 * 
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class AuthenticatedHttpClient {
    
    private final HttpClient httpClient;
    private final TokenService tokenService;
    
    public AuthenticatedHttpClient(TokenService tokenService) {
        this.httpClient = HttpClient.newHttpClient();
        this.tokenService = tokenService;
    }
    
    /**
     * Send HTTP request with automatic token injection
     */
    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler) 
            throws IOException, InterruptedException {
        
        // Add Authorization header
        HttpRequest authenticatedRequest = addAuthorizationHeader(request);
        
        // Send request
        HttpResponse<T> response = httpClient.send(authenticatedRequest, bodyHandler);
        
        // Handle 401 Unauthorized
        if (response.statusCode() == 401) {
            log.warn("Received 401 Unauthorized - token may be invalid");
            // You could implement automatic retry with refreshed token here
        }
        
        return response;
    }
    
    /**
     * Send async HTTP request with automatic token injection
     */
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(
            HttpRequest request, 
            HttpResponse.BodyHandler<T> bodyHandler) {
        
        HttpRequest authenticatedRequest = addAuthorizationHeader(request);
        return httpClient.sendAsync(authenticatedRequest, bodyHandler);
    }
    
    /**
     * Add Authorization header with token
     */
    private HttpRequest addAuthorizationHeader(HttpRequest originalRequest) {
        String token = tokenService.getAccessToken().orElse(null);
        
        if (token == null) {
            log.warn("No token available - request will be sent without authentication");
            return originalRequest;
        }
        
        return HttpRequest.newBuilder(originalRequest, (name, value) -> true)
            .header("Authorization", "Bearer " + token)
            .build();
    }
}
```

### Pattern 3: Integration with Existing Services

```java
package com.noteflix.pcm.application.service.chat;

import com.noteflix.pcm.auth.TokenService;
import com.noteflix.pcm.http.AuthenticatedHttpClient;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Conversation Service with SSO integration
 * 
 * @author PCM Team
 * @version 2.0.0 (SSO)
 */
@Slf4j
public class ConversationService {
    
    private final ConversationRepository repository;
    private final AuthenticatedHttpClient httpClient;
    private final TokenService tokenService;
    private final String apiBaseUrl;
    
    public ConversationService(
            ConversationRepository repository,
            TokenService tokenService,
            String apiBaseUrl) {
        this.repository = repository;
        this.tokenService = tokenService;
        this.httpClient = new AuthenticatedHttpClient(tokenService);
        this.apiBaseUrl = apiBaseUrl;
    }
    
    /**
     * Sync conversations with backend API
     */
    public void syncConversations(String userId) {
        // Check authentication
        if (!tokenService.isAuthenticated()) {
            log.warn("User not authenticated - cannot sync conversations");
            return;
        }
        
        try {
            // Build API request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiBaseUrl + "/api/conversations?userId=" + userId))
                .GET()
                .build();
            
            // Send request (token automatically added)
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                // Parse and save conversations
                List<Conversation> conversations = parseConversations(response.body());
                conversations.forEach(repository::save);
                
                log.info("Synced {} conversations from API", conversations.size());
            } else {
                log.error("Failed to sync conversations: HTTP {}", response.statusCode());
            }
        } catch (Exception e) {
            log.error("Failed to sync conversations", e);
        }
    }
}
```

---

## Troubleshooting

### Common Issues

#### Issue 1: Token Not Found

**Symptom:**
```
Token file not found: ~/.pcm/auth/token.enc
```

**Possible Causes:**
- User hasn't logged in to SSO Portal
- Token file was deleted
- File permissions issue

**Solution:**
```java
// Check if token exists
if (!tokenService.isAuthenticated()) {
    // Show login dialog
    showLoginDialog();
    
    // Or redirect to SSO Portal
    openSSOPortal();
}
```

#### Issue 2: Token Expired

**Symptom:**
```
HTTP 401 Unauthorized
Token expired
```

**Possible Causes:**
- Access token expired
- Refresh token expired
- Server time mismatch

**Solution:**
```java
// Automatic token refresh
if (response.statusCode() == 401) {
    // Try to refresh token
    boolean refreshed = tokenService.refreshToken();
    
    if (refreshed) {
        // Retry request
        return retryRequest(request);
    } else {
        // Require re-login
        showLoginDialog();
    }
}
```

#### Issue 3: Token Validation Failed

**Symptom:**
```
Invalid JWT signature
Token validation failed
```

**Possible Causes:**
- Token was tampered with
- Wrong public key on server
- Token format incorrect

**Solution:**
```java
// Verify token locally before sending
try {
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(publicKey)
        .build()
        .parseClaimsJws(token)
        .getBody();
    
    // Token valid
} catch (JwtException e) {
    log.error("Token validation failed", e);
    // Clear invalid token and re-login
    tokenService.logout();
    showLoginDialog();
}
```

#### Issue 4: Multiple Apps Conflicting

**Symptom:**
```
Token file locked
Another process is using the token
```

**Solution:**
```java
// Use file locking
try (FileChannel channel = FileChannel.open(tokenPath, 
        StandardOpenOption.READ, StandardOpenOption.WRITE)) {
    
    try (FileLock lock = channel.tryLock()) {
        if (lock == null) {
            log.warn("Token file is locked by another process");
            // Wait and retry
            Thread.sleep(100);
            return loadToken(); // Retry
        }
        
        // Read token
        ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
        channel.read(buffer);
        // ...
    }
}
```

---

## Next Steps

1. **Design SSO Portal UI**
   - Login form
   - Token management dashboard
   - Connected apps list

2. **Implement Token Service**
   - SecureTokenStorage
   - TokenService
   - AuthenticatedHttpClient

3. **Integrate with Existing Services**
   - ConversationService
   - AIService
   - Database sync

4. **Add Authentication UI**
   - Login dialog
   - "Not authenticated" message
   - Re-login prompt

5. **Testing**
   - Test token storage/retrieval
   - Test token refresh
   - Test API calls with token
   - Test multi-app scenarios

---

## Summary

### Key Takeaways

1. **SSO = One Login, Many Apps** ✅
   - User logs into Portal once
   - All apps automatically authenticated

2. **Token Storage Options** ✅
   - File System (encrypted) - Simple
   - System Keychain - Most secure
   - Local HTTP Server - Dynamic

3. **API Authentication** ✅
   - Use `Authorization: Bearer <token>` header
   - Auto-refresh expired tokens
   - Handle 401 errors gracefully

4. **Security Best Practices** ✅
   - Always use HTTPS
   - Encrypt tokens at rest
   - Short expiration times
   - Secure file permissions

5. **Implementation Patterns** ✅
   - TokenService for centralized management
   - AuthenticatedHttpClient for automatic token injection
   - Integration with existing services

---

**Author**: PCM Development Team  
**Date**: November 12, 2024  
**Version**: 1.0.0

**Status**: ✅ **SSO Integration Guide Complete**

