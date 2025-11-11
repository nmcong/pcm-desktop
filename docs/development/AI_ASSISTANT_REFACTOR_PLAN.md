# AI Assistant Page Refactoring Plan

## 🎯 Goals

1. **Apply SOLID Principles** - Single Responsibility, Dependency Inversion
2. **Clean Architecture** - Separate concerns (Domain, Data, Presentation)
3. **Database Integration** - Store conversations in SQLite
4. **LLM Integration** - Real AI responses via LLM providers
5. **Dark Theme** - Apply dark theme from reference image
6. **Best Practices** - Clean code, design patterns

---

## 📊 Current Issues

### ❌ Problems with Current Code

1. **Violates Single Responsibility** - One class does everything
2. **Data Models Inside UI** - ChatSession, ChatMessage in same file
3. **No Separation of Concerns** - UI + Business Logic + Data mixed
4. **Hardcoded Responses** - Fake AI responses
5. **No Database** - Data lost on restart
6. **No Real LLM** - Just simulated responses
7. **1104 lines** - Too long, hard to maintain

---

## 🏗️ New Architecture

```
┌─────────────────────────────────────────────────┐
│         UI Layer (Presentation)                 │
│  AIAssistantPage, ChatMessageView               │
└────────────────────┬────────────────────────────┘
                     │ Uses
┌────────────────────▼────────────────────────────┐
│      Service Layer (Business Logic)             │
│  ConversationService, AIService                 │
└────────────────────┬────────────────────────────┘
                     │ Uses
┌────────────────────▼────────────────────────────┐
│    Repository Layer (Data Access)               │
│  ConversationRepository                         │
└────────────────────┬────────────────────────────┘
                     │ Uses
┌────────────────────▼────────────────────────────┐
│      Domain Layer (Models)                      │
│  Conversation, Message                          │
└─────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────┐
│      External Integrations                      │
│  LLMClient (OpenAI, etc), Database              │
└─────────────────────────────────────────────────┘
```

---

## 📦 Package Structure

```
com.noteflix.pcm/
├── domain/
│   └── chat/
│       ├── Conversation.java              # Domain entity
│       ├── Message.java                   # Domain entity
│       └── MessageRole.java               # Enum
│
├── infrastructure/
│   ├── repository/
│   │   ├── ConversationRepository.java    # Interface
│   │   └── ConversationRepositoryImpl.java # SQLite impl
│   │
│   └── dao/
│       ├── ConversationDAO.java           # Database DAO
│       └── MessageDAO.java                # Database DAO
│
├── application/
│   └── service/
│       ├── ConversationService.java       # Business logic
│       └── AIService.java                 # LLM integration
│
└── ui/
    └── pages/
        ├── AIAssistantPage.java           # Main UI (refactored)
        ├── components/
        │   ├── ChatSidebarView.java       # Sidebar component
        │   ├── ChatMessageView.java       # Message bubble
        │   └── ChatInputView.java         # Input area
        └── model/
            └── ChatUIState.java            # UI state model
```

---

## 🎨 SOLID Principles Applied

### 1. Single Responsibility Principle (SRP)

```java
// ❌ Before: One class does everything
public class AIAssistantPage {
    // UI rendering
    // Data storage
    // Business logic
    // LLM calls
}

// ✅ After: Each class has one job
public class AIAssistantPage {
    // Only UI rendering & user interaction
}

public class ConversationService {
    // Only business logic
}

public class ConversationRepository {
    // Only data access
}

public class AIService {
    // Only LLM communication
}
```

### 2. Open/Closed Principle (OCP)

```java
// ✅ Open for extension (new LLM providers)
public interface LLMClient {
    LLMResponse sendMessage(LLMRequest request);
}

// Add new provider without modifying existing code
public class OpenAIClient implements LLMClient { }
public class ClaudeClient implements LLMClient { }
```

### 3. Liskov Substitution Principle (LSP)

```java
// ✅ Any Repository implementation works
ConversationRepository repo = new ConversationRepositoryImpl();
// Can be replaced with mock for testing
ConversationRepository mockRepo = new MockConversationRepository();
```

### 4. Interface Segregation Principle (ISP)

```java
// ✅ Specific interfaces
public interface Readable<T, ID> {
    Optional<T> findById(ID id);
}

public interface Writable<T, ID> {
    T save(T entity);
}
```

### 5. Dependency Inversion Principle (DIP)

```java
// ✅ Depend on abstractions
public class ConversationService {
    private final ConversationRepository repository; // Interface
    private final AIService aiService; // Interface
    
    // Dependencies injected
    public ConversationService(ConversationRepository repository, AIService aiService) {
        this.repository = repository;
        this.aiService = aiService;
    }
}
```

---

## 🎨 Design Patterns

### 1. Repository Pattern

```java
public interface ConversationRepository {
    Conversation save(Conversation conversation);
    Optional<Conversation> findById(Long id);
    List<Conversation> findAll();
    List<Conversation> findRecent(int limit);
}
```

### 2. Service Pattern

```java
public class ConversationService {
    public Conversation createConversation(String title);
    public Message sendMessage(Long conversationId, String content);
    public List<Conversation> getRecentConversations();
}
```

### 3. Observer Pattern (for streaming)

```java
public interface MessageStreamObserver {
    void onMessageChunk(String chunk);
    void onComplete();
    void onError(Throwable error);
}
```

### 4. Factory Pattern

```java
public class AIServiceFactory {
    public static AIService create(LLMProviderConfig config) {
        return new AIServiceImpl(LLMClientFactory.createClient(config));
    }
}
```

### 5. Dependency Injection

```java
// Service container
public class ServiceContainer {
    private static ServiceContainer instance;
    
    private final ConversationRepository conversationRepository;
    private final AIService aiService;
    private final ConversationService conversationService;
    
    private ServiceContainer() {
        // Initialize dependencies
        ConnectionManager connectionManager = ConnectionManager.INSTANCE;
        ConversationDAO conversationDAO = new ConversationDAOImpl(connectionManager);
        this.conversationRepository = new ConversationRepositoryImpl(conversationDAO);
        
        LLMProviderConfig llmConfig = loadLLMConfig();
        this.aiService = new AIServiceImpl(LLMClientFactory.createClient(llmConfig));
        
        this.conversationService = new ConversationService(conversationRepository, aiService);
    }
    
    public static ServiceContainer getInstance() {
        if (instance == null) {
            instance = new ServiceContainer();
        }
        return instance;
    }
}
```

---

## 🎨 Dark Theme (from image)

### Color Palette

```css
/* Background colors */
--color-bg-primary: #1a1d2e;
--color-bg-secondary: #16192a;
--color-bg-tertiary: #12151f;

/* Accent colors */
--color-accent-primary: #6366f1;    /* Indigo */
--color-accent-secondary: #8b5cf6;  /* Purple */

/* Text colors */
--color-text-primary: #f8fafc;
--color-text-secondary: #cbd5e1;
--color-text-muted: #64748b;

/* Border colors */
--color-border: #2d3142;

/* Message bubbles */
--color-user-bubble: #2563eb;
--color-ai-bubble: #1f2937;
```

---

## 📝 Implementation Steps

### Step 1: Create Domain Models ✅

```java
// Conversation.java
// Message.java
// MessageRole.java
```

### Step 2: Create Database Schema & DAO ✅

```sql
CREATE TABLE conversations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    conversation_id INTEGER NOT NULL,
    role TEXT NOT NULL, -- 'user', 'assistant', 'system'
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
);
```

### Step 3: Create Repository ✅

```java
// ConversationRepository interface
// ConversationRepositoryImpl
```

### Step 4: Create Services ✅

```java
// ConversationService
// AIService (wraps LLMClient)
```

### Step 5: Create UI Components ✅

```java
// ChatSidebarView
// ChatMessageView
// ChatInputView
```

### Step 6: Refactor AIAssistantPage ✅

```java
// Remove inner classes
// Inject dependencies
// Use services instead of direct logic
// Apply dark theme
```

### Step 7: Apply Dark Theme CSS ✅

```css
/* ai-assistant-dark.css */
```

---

## 📊 Before vs After

### Before

| Aspect | Status |
|--------|--------|
| Lines of code | 1104 lines |
| Classes in file | 3 (Page + 2 inner classes) |
| Dependencies | Tightly coupled |
| Data persistence | None (in-memory) |
| AI responses | Hardcoded/fake |
| Testability | Difficult |
| Maintainability | Poor |

### After

| Aspect | Status |
|--------|--------|
| Lines of code | ~300 lines (UI only) |
| Classes in file | 1 (UI only) |
| Dependencies | Loosely coupled (DI) |
| Data persistence | SQLite database |
| AI responses | Real LLM integration |
| Testability | Easy (mocking) |
| Maintainability | Excellent |

---

## ✅ Benefits

1. **Maintainability** - Small, focused classes
2. **Testability** - Easy to mock dependencies
3. **Scalability** - Easy to add features
4. **Reusability** - Components can be reused
5. **Flexibility** - Easy to swap implementations
6. **Clean Code** - Follow best practices

---

**Status**: Ready for Implementation  
**Estimated Time**: 4-6 hours  
**Priority**: High

