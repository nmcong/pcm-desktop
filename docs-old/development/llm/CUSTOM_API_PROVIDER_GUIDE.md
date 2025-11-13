# 🔌 Custom API Provider - Hướng Dẫn Sử Dụng

> Provider cho LLM service riêng của bạn với 3 API endpoints

---

## 📋 **Tổng Quan**

**CustomAPIProvider** là provider được thiết kế đặc biệt cho service LLM riêng của bạn với:
- ✅ Conversation management (tạo & tracking conversation ID)
- ✅ SSE streaming với **thinking mode** built-in
- ✅ Function calling (inject vào content)
- ✅ Token tracking (remaining tokens API)

---

## 🏗️ **API Endpoints**

### 1. **POST /api/chat/create**
Tạo conversation mới, trả về ID.

**Request:**
```json
{
  // Empty or with metadata
}
```

**Response:**
```json
{
  "id": "conv_12345"
}
// hoặc
{
  "conversation_id": "conv_12345"
}
// hoặc trả về trực tiếp string: "conv_12345"
```

### 2. **POST /api/chat/stream**
Stream chat với LLM (SSE format).

**Request:**
```json
{
  "conversation_id": "conv_12345",
  "content": "User message here...",
  "model": "default"
}
```

**Response (SSE):**
```
data: {"type": "thinking", "content": "Let me think..."}

data: {"type": "token", "content": "The"}

data: {"type": "token", "content": " answer"}

data: {"type": "token", "content": " is"}

data: {"type": "done", "usage": {"prompt_tokens": 10, "completion_tokens": 20}}
```

### 3. **GET /api/chat/tokens/{conversationId}**
Lấy số token còn lại của conversation.

**Response:**
```json
{
  "remaining_tokens": 1500
}
// hoặc
{
  "tokens": 1500
}
// hoặc trả về trực tiếp number: 1500
```

---

## 🚀 **Cách Sử Dụng**

### 1. Setup Provider

```java
import com.noteflix.pcm.llm.provider.CustomAPIProvider;
import com.noteflix.pcm.llm.model.ProviderConfig;
import com.noteflix.pcm.llm.registry.ProviderRegistry;

// Create provider
CustomAPIProvider provider = new CustomAPIProvider();

// Configure
provider.configure(ProviderConfig.builder()
    .baseUrl("https://your-api.com")  // ⬅️ Base URL của service
    .apiKey("your_api_key")           // ⬅️ API key (optional nếu không cần auth)
    .model("default")                 // ⬅️ Default model
    .timeoutMs(60000)                 // 60 seconds timeout
    .maxRetries(3)                    // Retry 3 lần nếu fail
    .build());

// Register
ProviderRegistry.getInstance().register("custom", provider);
ProviderRegistry.getInstance().setActive("custom");
```

### 2. Simple Chat

```java
List<Message> messages = List.of(
    Message.system("You are a helpful assistant."),
    Message.user("What is quantum computing?")
);

CompletableFuture<ChatResponse> future = provider.chat(messages, ChatOptions.defaults());
ChatResponse response = future.get();

System.out.println("Response: " + response.getContent());

// Check thinking (nếu có)
if (response.getThinkingContent() != null) {
    System.out.println("Thinking: " + response.getThinkingContent());
}

// Check usage
if (response.getUsage() != null) {
    System.out.println("Tokens: " + response.getTotalTokens());
}
```

### 3. Streaming với Thinking

```java
provider.chatStream(messages, ChatOptions.defaults(), new ChatEventAdapter() {
    
    @Override
    public void onThinking(String thinking) {
        // Thinking mode - hiện trong UI với style khác
        System.out.print("[Thinking] " + thinking);
    }
    
    @Override
    public void onToken(String token) {
        // Regular content - append vào text area
        System.out.print(token);
    }
    
    @Override
    public void onComplete(ChatResponse response) {
        System.out.println("\nDone!");
        if (response.getUsage() != null) {
            System.out.println("Total tokens: " + response.getTotalTokens());
        }
    }
    
    @Override
    public void onError(Throwable error) {
        System.err.println("Error: " + error.getMessage());
    }
});
```

### 4. Function Calling (Injected)

Vì API của bạn không hỗ trợ function calling native, provider sẽ **inject function definitions vào content**.

```java
FunctionRegistry funcRegistry = FunctionRegistry.getInstance();

// Register function (dùng @LLMFunction annotation hoặc manual)
// ... (xem QUICK_START.md)

ChatOptions options = ChatOptions.withTools(funcRegistry.getAllTools());

ChatResponse response = provider.chat(messages, options).get();

// LLM sẽ nhận được content kèm theo function definitions:
/*
[user]: What's the weather in Paris?

--- AVAILABLE FUNCTIONS ---
You can call these functions by responding in this format:
<function_call>
  <name>function_name</name>
  <arguments>{"arg1": "value1"}</arguments>
</function_call>

Available functions:
- get_weather: Get current weather for a location
  Parameters:
    - location (string): City name
*/

// Parse response để detect function call
if (response.getContent().contains("<function_call>")) {
    // Parse XML và execute function
    // Sau đó gửi kết quả lại cho LLM
}
```

### 5. Token Tracking

```java
// Chat first
ChatResponse response = provider.chat(messages, ChatOptions.defaults()).get();

// Get remaining tokens
String conversationId = response.getId();
int remainingTokens = provider.getRemainingTokens(conversationId);

System.out.println("Remaining tokens: " + remainingTokens);

// Alert user if low
if (remainingTokens < 100) {
    System.out.println("⚠️ Warning: Low tokens remaining!");
}
```

### 6. JavaFX UI Integration

```java
// In your ViewModel
public void sendMessage(String userMessage) {
    setBusy(true);
    
    List<Message> messages = buildConversation(userMessage);
    
    provider.chatStream(messages, ChatOptions.defaults(), new ChatEventAdapter() {
        
        @Override
        public void onThinking(String thinking) {
            Platform.runLater(() -> {
                thinkingLabel.setText(thinking);
                thinkingLabel.setVisible(true);
            });
        }
        
        @Override
        public void onToken(String token) {
            Platform.runLater(() -> {
                responseTextArea.appendText(token);
                scrollToBottom();
            });
        }
        
        @Override
        public void onComplete(ChatResponse response) {
            Platform.runLater(() -> {
                thinkingLabel.setVisible(false);
                setBusy(false);
                
                // Update token count
                tokensLabel.setText("Tokens: " + response.getTotalTokens());
                
                // Check remaining
                try {
                    int remaining = provider.getRemainingTokens(response.getId());
                    remainingLabel.setText("Remaining: " + remaining);
                } catch (Exception e) {
                    log.warn("Could not get remaining tokens", e);
                }
            });
        }
        
        @Override
        public void onError(Throwable error) {
            Platform.runLater(() -> {
                setBusy(false);
                showError(error.getMessage());
            });
        }
    });
}
```

---

## ⚙️ **Tùy Chỉnh**

### Conversation Caching

Provider tự động cache conversation IDs để tái sử dụng:

```java
// Clear cache for specific key
provider.clearConversationCache("cache_key");

// Clear all cached conversations
provider.clearConversationCache(null);
```

### Custom SSE Response Format

Nếu SSE response format của bạn khác, sửa trong method `streamChat()`:

```java
// Line 330-380 trong CustomAPIProvider.java
if (type.equals("your_thinking_type")) {
    // Handle thinking
} else if (type.equals("your_token_type")) {
    // Handle token
}
```

### Custom Create Conversation Format

Nếu create conversation API cần metadata, sửa trong method `getOrCreateConversation()`:

```java
// Line 145 trong CustomAPIProvider.java
ObjectNode requestBody = objectMapper.createObjectNode();
requestBody.put("user_id", userId);
requestBody.put("metadata", metadata);
```

---

## 🎯 **Features**

### ✅ Conversation Management
- Tự động tạo conversation khi cần
- Cache conversation IDs để reuse
- Customizable cache strategy

### ✅ Thinking Mode (Built-in!)
- Tự động detect `"type": "thinking"` trong SSE
- Trigger `onThinking()` callback
- Hiển thị riêng trong UI

### ✅ Function Calling (Injected)
- Inject function definitions vào content
- Format: XML-style `<function_call>...</function_call>`
- LLM response theo format để call functions
- Bạn parse và execute

### ✅ Token Tracking
- Check remaining tokens cho conversation
- Alert user khi sắp hết tokens
- Monitor usage cho mỗi request

### ✅ Error Handling
- Retry logic với exponential backoff
- Comprehensive error messages
- `onError()` callback cho UI

---

## 📝 **Ví Dụ Đầy Đủ**

Xem file: `src/main/java/com/noteflix/pcm/llm/examples/CustomAPIUsageExample.java`

4 examples:
1. Basic setup & chat
2. Streaming với thinking
3. Function calling (injected)
4. Token tracking

---

## 🔍 **Troubleshooting**

### Provider not ready?
```java
if (!provider.isReady()) {
    throw new IllegalStateException("Provider not configured");
}

// Hoặc test connection
boolean connected = provider.testConnection();
```

### SSE format không match?
Kiểm tra logs để xem format response:
```java
log.debug("SSE chunk: {}", data);
```

Sau đó sửa parsing logic trong `streamChat()` method.

### Function calling không work?
Check content được gửi lên API:
```java
String content = buildContentWithFunctions(messages, options);
log.info("Content with functions: {}", content);
```

### Conversation ID bị conflict?
Clear cache:
```java
provider.clearConversationCache(null);
```

---

## 📊 **Response Format Examples**

### Option 1: Structured JSON
```json
{
  "type": "thinking",
  "content": "Let me analyze this..."
}
```

### Option 2: OpenAI-style
```json
{
  "choices": [{
    "delta": {
      "content": "token here"
    }
  }]
}
```

### Option 3: Simple
```json
{
  "content": "token here",
  "is_thinking": false
}
```

**Sửa parsing logic để match với format của bạn!**

---

## 🚨 **Lưu Ý Quan Trọng**

1. **Base URL**: Nhớ set đúng base URL của service
2. **Authentication**: Set API key nếu cần auth
3. **SSE Format**: Parse theo format của service bạn
4. **Function Format**: LLM phải hiểu XML format để call functions
5. **Conversation ID**: Provider tự động manage, nhưng có thể custom

---

## 🎊 **Ready!**

CustomAPIProvider đã sẵn sàng để use với service LLM của bạn!

**Next steps:**
1. ✅ Configure provider với base URL & API key
2. ✅ Test với simple chat
3. ✅ Enable thinking mode trong UI
4. ✅ Add function calling nếu cần
5. ✅ Monitor token usage

---

*Provider Location:* `src/main/java/com/noteflix/pcm/llm/provider/CustomAPIProvider.java`  
*Examples:* `src/main/java/com/noteflix/pcm/llm/examples/CustomAPIUsageExample.java`  
*Build Status:* ✅ SUCCESS (224 class files)

