# 🤖 AI Assistant Page - Refactoring Guide

## 📋 Overview

Complete refactoring plan for AIAssistantPage to integrate **database**, **LLM**, apply **SOLID principles**, **design patterns**, **clean code**, and **dark theme**.

---

## 📚 Documentation

### Main Documents

1. **[AI_ASSISTANT_REFACTOR_PLAN.md](./development/AI_ASSISTANT_REFACTOR_PLAN.md)**
   - Complete architecture design
   - SOLID principles explained
   - Design patterns
   - Package structure

2. **[AI_ASSISTANT_REFACTORING_SUMMARY.md](./development/AI_ASSISTANT_REFACTORING_SUMMARY.md)**
   - Before vs After comparison
   - Code examples
   - Integration points
   - Testing strategy
   - Metrics

3. **[ai-assistant-dark.css](../src/main/resources/css/ai-assistant-dark.css)**
   - Complete dark theme
   - Based on reference image
   - Modern design

---

## 🏗️ Architecture

### Current State (Before)
```
AIAssistantPage.java (1104 lines) ❌
├── UI rendering
├── Inner classes (ChatSession, ChatMessage)
├── Business logic
├── Fake AI responses
└── No persistence
```

### Target State (After)
```
Domain Layer
├── Conversation.java
└── Message.java

Infrastructure Layer
├── ConversationRepository.java
├── ConversationRepositoryImpl.java
├── ConversationDAO.java
└── MessageDAO.java

Application Layer
├── ConversationService.java
└── AIService.java

Presentation Layer
└── AIAssistantPage.java (300 lines) ✅
    - Only UI logic
    - Inject dependencies
    - Uses services
```

---

## ✅ SOLID Principles

### Single Responsibility
```java
// ✅ Each class has ONE job
AIAssistantPage        → UI rendering
ConversationService    → Business logic
ConversationRepository → Data access
LLMClient              → LLM communication
```

### Dependency Inversion
```java
// ✅ Depend on abstractions
public class AIAssistantPage {
    private final ConversationService service; // Interface
    
    public AIAssistantPage(ConversationService service) {
        this.service = service; // Injected
    }
}
```

### Open/Closed
```java
// ✅ Open for extension (new providers), closed for modification
public interface LLMClient { }
public class OpenAIClient implements LLMClient { }
public class ClaudeClient implements LLMClient { }
```

---

## 🎨 Dark Theme

### Colors (from reference image)

| Color | Value | Usage |
|-------|-------|-------|
| **Background Primary** | `#1a1d2e` | Main background |
| **Background Secondary** | `#16192a` | Sidebar, panels |
| **Background Tertiary** | `#12151f` | Input, cards |
| **Accent Primary** | `#6366f1` | Buttons, highlights |
| **Accent Secondary** | `#8b5cf6` | Hover states |
| **Text Primary** | `#f8fafc` | Main text |
| **Text Secondary** | `#cbd5e1` | Secondary text |
| **Text Muted** | `#64748b` | Placeholders |
| **Border** | `#2d3142` | Borders |

### CSS File
- ✅ Created: `src/main/resources/css/ai-assistant-dark.css`
- ✅ Complete styling for all components
- ✅ Responsive design
- ✅ Animations

---

## 🔧 Integration Points

### 1. Database Integration

```java
// Save conversation to database
public class ConversationRepositoryImpl implements ConversationRepository {
    
    private final ConnectionManager connectionManager;
    
    @Override
    public Conversation save(Conversation conversation) {
        try (Connection conn = connectionManager.getConnection()) {
            String sql = "INSERT INTO conversations (title, created_at) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, conversation.getTitle());
            stmt.setTimestamp(2, Timestamp.valueOf(conversation.getCreatedAt()));
            stmt.executeUpdate();
            return conversation;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save conversation", e);
        }
    }
}
```

### 2. LLM Integration

```java
// Use real LLM for responses
public class AIServiceImpl implements AIService {
    
    private final LLMClient llmClient;
    
    @Override
    public String generateResponse(String userMessage) {
        LLMRequest request = LLMRequest.builder()
            .model("gpt-4")
            .messages(List.of(
                Message.user(userMessage)
            ))
            .temperature(0.7)
            .build();
        
        LLMResponse response = llmClient.sendMessage(request);
        return response.getContent();
    }
}
```

### 3. Streaming Support

```java
// Stream AI responses in real-time
public void streamResponse(String message, StreamingObserver observer) {
    if (llmClient instanceof StreamingCapable streaming) {
        LLMRequest request = LLMRequest.builder()
            .model("gpt-4")
            .messages(List.of(Message.user(message)))
            .stream(true)
            .build();
        
        streaming.streamMessage(request, new StreamingObserver() {
            @Override
            public void onChunk(LLMChunk chunk) {
                Platform.runLater(() -> {
                    observer.onChunk(chunk);
                });
            }
            
            @Override
            public void onComplete() {
                Platform.runLater(observer::onComplete);
            }
            
            @Override
            public void onError(Throwable error) {
                Platform.runLater(() -> observer.onError(error));
            }
        });
    }
}
```

---

## 📦 Implementation Steps

### Step 1: Create Domain Models (30 min)

```java
// domain/chat/Conversation.java
@Data
@Builder
public class Conversation extends BaseEntity {
    private String title;
    private List<Message> messages;
}

// domain/chat/Message.java
@Data
@Builder
public class Message extends BaseEntity {
    private Long conversationId;
    private MessageRole role;
    private String content;
}
```

### Step 2: Create Database Schema (30 min)

```sql
-- V2__chat_tables.sql
CREATE TABLE conversations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    conversation_id INTEGER NOT NULL,
    role TEXT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
);

CREATE INDEX idx_messages_conversation ON messages(conversation_id);
```

### Step 3: Create Repository (1 hour)

```java
// infrastructure/repository/chat/ConversationRepository.java
public interface ConversationRepository {
    Conversation save(Conversation conversation);
    Optional<Conversation> findById(Long id);
    List<Conversation> findRecent(int limit);
    void delete(Long id);
}

// infrastructure/repository/chat/ConversationRepositoryImpl.java
public class ConversationRepositoryImpl implements ConversationRepository {
    // Implementation using DAO
}
```

### Step 4: Create Services (1 hour)

```java
// application/service/chat/ConversationService.java
public class ConversationService {
    private final ConversationRepository repository;
    private final AIService aiService;
    
    public Conversation createConversation(String title) { }
    public Message sendMessage(Long conversationId, String content) { }
    public List<Conversation> getRecentConversations() { }
}

// application/service/chat/AIService.java
public interface AIService {
    String generateResponse(String userMessage);
    void streamResponse(String userMessage, StreamingObserver observer);
}
```

### Step 5: Setup Dependency Injection (30 min)

```java
// ui/ServiceContainer.java
public class ServiceContainer {
    private static ServiceContainer instance;
    
    private final ConversationRepository conversationRepository;
    private final AIService aiService;
    private final ConversationService conversationService;
    
    private ServiceContainer() {
        // Wire dependencies
        ConnectionManager cm = ConnectionManager.INSTANCE;
        ConversationDAO dao = new ConversationDAOImpl(cm);
        this.conversationRepository = new ConversationRepositoryImpl(dao);
        
        LLMProviderConfig config = loadConfig();
        LLMClient client = LLMClientFactory.createClient(config);
        this.aiService = new AIServiceImpl(client);
        
        this.conversationService = new ConversationService(
            conversationRepository, 
            aiService
        );
    }
    
    public static ServiceContainer getInstance() {
        if (instance == null) {
            instance = new ServiceContainer();
        }
        return instance;
    }
    
    public ConversationService getConversationService() {
        return conversationService;
    }
}
```

### Step 6: Refactor AIAssistantPage (1.5 hours)

```java
// ui/pages/AIAssistantPage.java
public class AIAssistantPage extends BasePage {
    
    // Inject dependencies
    private final ConversationService conversationService;
    
    // UI components
    private VBox conversationsListView;
    private VBox chatMessagesArea;
    private TextArea chatInput;
    
    public AIAssistantPage() {
        super("AI Assistant", "Chat with AI", new FontIcon(Feather.MESSAGE_CIRCLE));
        
        // Get service from container
        this.conversationService = ServiceContainer.getInstance().getConversationService();
        
        // Initialize UI
        initializeUI();
        loadConversations();
    }
    
    private void handleSendMessage() {
        String message = chatInput.getText().trim();
        if (message.isEmpty()) return;
        
        // Delegate to service
        Message response = conversationService.sendMessage(
            currentConversation.getId(), 
            message
        );
        
        // Update UI
        addMessageToUI(response);
        chatInput.clear();
    }
    
    private void loadConversations() {
        List<Conversation> conversations = 
            conversationService.getRecentConversations();
        updateConversationsListUI(conversations);
    }
}
```

### Step 7: Apply Dark Theme (30 min)

```java
// In AIAssistantPage constructor
Scene scene = getScene();
if (scene != null) {
    scene.getStylesheets().add(
        getClass().getResource("/css/ai-assistant-dark.css").toExternalForm()
    );
}
```

---

## 📊 Benefits

### Code Quality

| Metric | Before | After | Improvement |
|--------|--------|-------|-------------|
| Lines of code | 1104 | 300 | **73% reduction** |
| Number of classes | 3 (nested) | 1 (UI only) | **Separated** |
| Testability | Hard | Easy | **Mockable** |
| Coupling | Tight | Loose | **DI** |
| SOLID compliance | ❌ | ✅ | **100%** |

### Features

| Feature | Before | After |
|---------|--------|-------|
| **Persistence** | ❌ In-memory | ✅ SQLite database |
| **Real LLM** | ❌ Fake responses | ✅ OpenAI/Claude/etc |
| **Streaming** | ❌ No | ✅ Real-time chunks |
| **Dark Theme** | ⚠️ Basic | ✅ Beautiful dark UI |
| **Testing** | ❌ Hard | ✅ Easy with mocks |

---

## 🧪 Testing

### Unit Test Example

```java
@Test
void testCreateConversation() {
    // Given
    ConversationRepository mockRepo = mock(ConversationRepository.class);
    AIService mockAI = mock(AIService.class);
    ConversationService service = new ConversationService(mockRepo, mockAI);
    
    Conversation expected = Conversation.builder()
        .id(1L)
        .title("Test")
        .build();
    
    when(mockRepo.save(any())).thenReturn(expected);
    
    // When
    Conversation result = service.createConversation("Test");
    
    // Then
    verify(mockRepo).save(any(Conversation.class));
    assertEquals("Test", result.getTitle());
}
```

---

## 🎯 Checklist

### Foundation
- [ ] Create domain models (Conversation, Message)
- [ ] Create MessageRole enum
- [ ] Create database schema
- [ ] Create DAO interfaces and implementations

### Infrastructure
- [ ] Create ConversationRepository interface
- [ ] Implement ConversationRepositoryImpl
- [ ] Add database migrations
- [ ] Test repository with real database

### Application Services
- [ ] Create ConversationService
- [ ] Create AIService interface
- [ ] Implement AIServiceImpl with LLM integration
- [ ] Add unit tests for services

### Dependency Injection
- [ ] Create ServiceContainer
- [ ] Wire all dependencies
- [ ] Test injection works correctly

### UI Refactoring
- [ ] Simplify AIAssistantPage
- [ ] Remove inner classes
- [ ] Inject services via constructor
- [ ] Update event handlers to use services
- [ ] Remove hardcoded logic

### Dark Theme
- [ ] Apply ai-assistant-dark.css
- [ ] Test all UI states (welcome, chat, loading)
- [ ] Verify colors match reference image
- [ ] Test responsive design

### Testing
- [ ] Write unit tests for services
- [ ] Write integration tests
- [ ] Test with real LLM provider
- [ ] Test database persistence

---

## 📚 Resources

### Already Created
- ✅ [LLM_INTEGRATION_PLAN.md](./development/LLM_INTEGRATION_PLAN.md)
- ✅ [LLM_QUICK_START.md](./development/LLM_QUICK_START.md)
- ✅ [SQLITE_IMPLEMENTATION_PLAN.md](./development/SQLITE_IMPLEMENTATION_PLAN.md)
- ✅ [DATABASE_QUICK_START.md](./development/DATABASE_QUICK_START.md)
- ✅ [ai-assistant-dark.css](../src/main/resources/css/ai-assistant-dark.css)

### LLM Integration
- All interfaces and models already created
- See `src/main/java/com/noteflix/pcm/llm/`
- Ready to use with OpenAI, Anthropic, Ollama

### Database
- Connection manager already exists
- Base entities and repositories defined
- See `src/main/java/com/noteflix/pcm/domain/` and `infrastructure/`

---

## 🚀 Getting Started

### Quick Start

1. **Read the plans**
   ```
   docs/development/AI_ASSISTANT_REFACTOR_PLAN.md
   docs/development/AI_ASSISTANT_REFACTORING_SUMMARY.md
   ```

2. **Review architecture**
   - Understand Clean Architecture layers
   - Review SOLID principles
   - Check design patterns

3. **Start implementation**
   - Follow steps 1-7
   - Test each component
   - Apply dark theme

4. **Test everything**
   - Unit tests
   - Integration tests
   - UI testing

---

## 🎯 Timeline

| Phase | Duration | Description |
|-------|----------|-------------|
| **Planning** | ✅ Complete | Architecture, design, CSS |
| **Foundation** | 30 min | Models, enums |
| **Infrastructure** | 1.5 hours | DAO, Repository |
| **Services** | 1 hour | Business logic |
| **DI Setup** | 30 min | Wire dependencies |
| **UI Refactor** | 1.5 hours | Simplify page |
| **Dark Theme** | 30 min | Apply CSS |
| **Testing** | 1 hour | Write tests |
| **Total** | **~6-7 hours** | Complete refactor |

---

## ✅ Success Criteria

- [ ] Code reduced from 1104 to ~300 lines
- [ ] All SOLID principles applied
- [ ] Database persistence working
- [ ] Real LLM responses
- [ ] Dark theme applied
- [ ] All tests passing
- [ ] Clean, maintainable code

---

**Status**: 📋 Ready for Implementation  
**Priority**: High  
**Impact**: Major improvement in code quality and features

---

**Let's Build! 🚀**

