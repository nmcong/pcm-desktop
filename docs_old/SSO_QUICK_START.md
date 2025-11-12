# SSO Integration - Quick Start Guide 🚀

## Tóm Tắt Nhanh

**Vấn đề**: Làm thế nào để app tự động login sau khi user đăng nhập Portal?

**Giải pháp**: SSO (Single Sign-On) với Token-based Authentication

---

## 3 Bước Đơn Giản

### 1️⃣ Portal Tạo Token Khi User Login

```
User login vào Portal
    ↓
Portal validate username/password
    ↓
Portal tạo JWT token
    ↓
Portal lưu token vào file:
  ~/.pcm/auth/token.enc (encrypted)
```

### 2️⃣ Apps Đọc Token Từ File

```java
// App khởi động
TokenService tokenService = new TokenService();
String token = tokenService.getAccessToken();

if (token != null) {
    // User đã login ✅
    // App tự động authenticated
} else {
    // User chưa login ❌
    // Hiển thị login dialog
}
```

### 3️⃣ Apps Call API Với Token

```java
// Gọi API với token trong Authorization header
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://api.example.com/conversations"))
    .header("Authorization", "Bearer " + token)
    .GET()
    .build();

HttpResponse<String> response = httpClient.send(request, ...);
```

---

## Kiến Trúc Đơn Giản

```
┌────────────────────────────────────────────────────┐
│            User's Computer                         │
│                                                    │
│  ┌──────────────┐         Token Storage           │
│  │ SSO Portal   │────────►  ~/.pcm/auth/           │
│  │ (Login UI)   │          token.enc (encrypted)   │
│  └──────────────┘                                  │
│                               │                    │
│                               │ Read token         │
│                   ┌───────────┴────────────┐       │
│                   │                        │       │
│            ┌──────▼─────┐          ┌──────▼─────┐ │
│            │   App 1    │          │   App 2    │ │
│            │   (PCM)    │          │   (CRM)    │ │
│            └──────┬─────┘          └──────┬─────┘ │
└───────────────────┼────────────────────────┼───────┘
                    │                        │
                    │ API calls with token   │
                    ↓                        ↓
            ┌───────────────────────────────────────┐
            │         Backend API Server            │
            │  • Validate token                     │
            │  • Return data                        │
            └───────────────────────────────────────┘
```

---

## Token Format (JWT)

```
eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMTIzIiwibmFtZSI6IkpvaG4gRG9lIiwiZW1haWwiOiJqb2huQGV4YW1wbGUuY29tIiwicm9sZXMiOlsidXNlciIsImFkbWluIl0sImlhdCI6MTY5OTg3NjU0MywiZXhwIjoxNjk5ODgwMTQzfQ.signature

Decoded:
{
  "sub": "user123",
  "name": "John Doe",
  "email": "john@example.com",
  "roles": ["user", "admin"],
  "iat": 1699876543,
  "exp": 1699880143
}
```

---

## Implementation (3 Classes)

### Class 1: TokenService

```java
public class TokenService {
    
    private TokenData cachedToken;
    
    // Lấy token (auto-refresh nếu cần)
    public Optional<String> getAccessToken() {
        if (cachedToken == null) {
            loadToken(); // Load từ file
        }
        
        if (isTokenExpired(cachedToken)) {
            refreshToken(); // Làm mới token
        }
        
        return Optional.ofNullable(cachedToken)
            .map(TokenData::getAccessToken);
    }
    
    // Kiểm tra user đã login chưa
    public boolean isAuthenticated() {
        return getAccessToken().isPresent();
    }
}
```

### Class 2: SecureTokenStorage

```java
public class SecureTokenStorage {
    
    private static final String TOKEN_FILE = 
        System.getProperty("user.home") + "/.pcm/auth/token.enc";
    
    // Lưu token (encrypted)
    public void saveToken(TokenData token) {
        String json = toJson(token);
        byte[] encrypted = encrypt(json);
        Files.write(Paths.get(TOKEN_FILE), encrypted);
    }
    
    // Đọc token (decrypt)
    public TokenData loadToken() {
        byte[] encrypted = Files.readAllBytes(Paths.get(TOKEN_FILE));
        String json = decrypt(encrypted);
        return fromJson(json, TokenData.class);
    }
}
```

### Class 3: AuthenticatedHttpClient

```java
public class AuthenticatedHttpClient {
    
    private final TokenService tokenService;
    
    // Gửi request với token tự động
    public <T> HttpResponse<T> send(HttpRequest request, ...) {
        
        // Thêm Authorization header
        String token = tokenService.getAccessToken().orElse(null);
        HttpRequest authRequest = HttpRequest.newBuilder(request, ...)
            .header("Authorization", "Bearer " + token)
            .build();
        
        // Gửi request
        return httpClient.send(authRequest, ...);
    }
}
```

---

## Usage Example

```java
public class AIAssistantPage {
    
    private final TokenService tokenService;
    private final AuthenticatedHttpClient httpClient;
    
    public AIAssistantPage() {
        // Initialize
        this.tokenService = new TokenService();
        this.httpClient = new AuthenticatedHttpClient(tokenService);
    }
    
    @Override
    public void onPageActivated() {
        // Kiểm tra authentication
        if (!tokenService.isAuthenticated()) {
            showLoginDialog();
            return;
        }
        
        // Load data from API
        loadConversations();
    }
    
    private void loadConversations() {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.example.com/conversations"))
            .GET()
            .build();
        
        // Token tự động được thêm vào request
        HttpResponse<String> response = httpClient.send(request, ...);
        
        if (response.statusCode() == 200) {
            // Success ✅
            displayConversations(response.body());
        } else if (response.statusCode() == 401) {
            // Token invalid/expired ❌
            showLoginDialog();
        }
    }
}
```

---

## Cookie vs Token

### ❌ Cookie (Không khuyến khích cho Desktop App)

```java
// Phức tạp hơn
CookieManager cookieManager = new CookieManager();
HttpCookie cookie = new HttpCookie("session_token", token);
cookie.setDomain("api.example.com");
cookie.setSecure(true);
cookie.setHttpOnly(true);
cookieManager.getCookieStore().add(uri, cookie);
```

**Nhược điểm:**
- CSRF vulnerability
- Chỉ hoạt động với same domain
- Phức tạp cho desktop app

### ✅ Authorization Header (Khuyến khích)

```java
// Đơn giản, chuẩn
request.header("Authorization", "Bearer " + token)
```

**Ưu điểm:**
- Standard HTTP header
- Không bị CSRF
- Dễ test
- Hoạt động với mọi API

---

## Security Checklist

- [x] ✅ Dùng HTTPS (không dùng HTTP)
- [x] ✅ Encrypt token trước khi lưu file
- [x] ✅ Token expiration ngắn (15-60 phút)
- [x] ✅ Implement token refresh
- [x] ✅ Secure file permissions (600)
- [x] ✅ Validate token signature
- [x] ❌ Không log token
- [x] ❌ Không để token trong URL

---

## Troubleshooting

### Token Not Found

```
Lỗi: Token file not found
Nguyên nhân: User chưa login vào Portal
Giải pháp: Hiển thị login dialog
```

### Token Expired

```
Lỗi: HTTP 401 Unauthorized
Nguyên nhân: Token hết hạn
Giải pháp: Auto-refresh hoặc yêu cầu re-login
```

### Token Invalid

```
Lỗi: Invalid JWT signature
Nguyên nhân: Token bị thay đổi hoặc sai public key
Giải pháp: Clear token và re-login
```

---

## Tài Liệu Chi Tiết

Xem: `docs/development/SSO_INTEGRATION_GUIDE.md`

---

**Tóm lại:**
1. Portal tạo token và lưu vào file (encrypted)
2. Apps đọc token từ file
3. Apps gửi token trong `Authorization` header khi call API
4. Backend validate token và trả về data

**Đơn giản vậy thôi!** 🎉

