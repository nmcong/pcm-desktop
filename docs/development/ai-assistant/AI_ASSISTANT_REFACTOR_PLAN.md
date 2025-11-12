# AI Assistant Page - Refactoring Plan

## 🎯 Current Problems

### Violations of SOLID Principles

1. **Single Responsibility Principle (SRP) ❌**
   - `AIAssistantPage` does EVERYTHING: UI, data management, business logic, animations
   - Inner classes `ChatSession` and `ChatMessage` should be separate entities
   - Hard-coded AI responses (no LLM integration)
   - No database persistence

2. **Open/Closed Principle (OCP) ❌**
   - Hard to extend without modifying the page
   - Hard-coded message generation logic

3. **Dependency Inversion Principle (DIP) ❌**
   - Depends on concrete implementations, not abstractions
   - No interfaces for services

### Code Smells

- **God Class**: 1,100+ lines doing everything
- **Feature Envy**: UI code accessing data structures directly
- **Primitive Obsession**: Using Lists instead of repositories
- **Long Method**: Methods like `createMainLayout()`, `simulateAIResponse()`
- **Data Clumps**: ChatSession and ChatMessage should be domain entities

---

## 🏗️ Refactoring Architecture

### Clean Architecture Layers

```
┌─────────────────────────────────────────────────┐
│          Presentation Layer (UI)                │
│  AIAssistantPage (View)                         │
│  - Only handles UI rendering                    │
│  - Delegates to ViewModel/Presenter             │
└────────────────────┬────────────────────────────┘
                     │ Uses
┌────────────────────▼────────────────────────────┐
│    Application Layer (Use Cases/Services)       │
│  ConversationService, AIService                 │
│  - Business logic                               │
│  - Orchestrates operations                      │
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
        └── AIAssistantPage.java           # UI only
```

---

## 🎨 Design Patterns to Apply

### 1. **Repository Pattern**
- Abstract data access from business logic
- `ConversationRepository` interface
- `ConversationRepositoryImpl` for SQLite

### 2. **Service Layer Pattern**
- `ConversationService` - manages conversations
- `AIService` - integrates with LLM

### 3. **Builder Pattern**
- `Conversation.builder()`
- `Message.builder()`

### 4. **Observer Pattern**
- Notify UI when new messages arrive
- Streaming LLM responses

### 5. **Strategy Pattern**
- Different LLM providers (OpenAI, Claude, Ollama)

### 6. **Dependency Injection**
- Constructor injection for services
- Easy testing with mocks

---

## 📋 Implementation Plan

### Phase 1: Domain Layer ✅ (Create Entities)

**Files to Create:**
- `domain/chat/Conversation.java`
- `domain/chat/Message.java`
- `domain/chat/MessageRole.java`

**Features:**
- Immutable entities with Builder pattern
- Validation
- Business logic (if any)

### Phase 2: Repository Layer ✅ (Data Access)

**Files to Create:**
- `infrastructure/repository/chat/ConversationRepository.java` (interface)
- `infrastructure/repository/chat/ConversationRepositoryImpl.java`
- `infrastructure/dao/ConversationDAO.java`
- `infrastructure/dao/MessageDAO.java`

**Features:**
- CRUD operations for Conversation
- CRUD operations for Message
- Queries: findByUserId, findRecent, search

### Phase 3: Service Layer ✅ (Business Logic)

**Files to Create:**
- `application/service/chat/ConversationService.java`
- `application/service/chat/AIService.java`

**Features:**
- ConversationService:
  - Create/update/delete conversations
  - Add messages to conversation
  - Search conversations
  
- AIService:
  - Integrate with LLMService
  - Generate AI responses
  - Stream responses
  - Function calling support

### Phase 4: Refactor UI ✅ (Presentation Layer)

**Files to Refactor:**
- `ui/pages/AIAssistantPage.java`

**Changes:**
- **Remove** inner classes (ChatSession, ChatMessage)
- **Inject** ConversationService and AIService
- **Separate** UI components into smaller methods
- **Use** reactive patterns (Observable, LiveData) for updates
- **Delegate** business logic to services

**New UI Components:**
- `ui/components/chat/ChatSidebarView.java`
- `ui/components/chat/ChatMessageView.java`
- `ui/components/chat/ChatInputView.java`

---

## 🔧 Refactoring Steps

### Step 1: Extract Domain Entities

```java
// Before (Inner class)
public static class ChatSession {
    private String title;
    private List<ChatMessage> messages;
    // ...
}

// After (Domain entity)
@Data
@Builder
public class Conversation extends BaseEntity {
    private Long id;
    private String title;
    private String userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Message> messages;
}
```

### Step 2: Create Repository

```java
public interface ConversationRepository extends Repository<Conversation, Long> {
    List<Conversation> findByUserId(String userId);
    List<Conversation> findRecent(int limit);
    List<Conversation> search(String query);
}
```

### Step 3: Create Services

```java
@Slf4j
public class ConversationService {
    private final ConversationRepository repository;
    private final AIService aiService;
    
    public Conversation createConversation(String title, String userId) {
        // Business logic
    }
    
    public Message sendMessage(Long conversationId, String content) {
        // Business logic + AI integration
    }
}
```

### Step 4: Refactor UI

```java
public class AIAssistantPage extends BasePage {
    // Inject dependencies
    private final ConversationService conversationService;
    private final AIService aiService;
    
    // UI components only
    private VBox chatSidebar;
    private VBox chatMessages;
    private TextArea chatInput;
    
    public AIAssistantPage(ConversationService conversationService, AIService aiService) {
        this.conversationService = conversationService;
        this.aiService = aiService;
        // UI setup only
    }
    
    private void handleSendMessage() {
        String message = chatInput.getText();
        // Delegate to service
        conversationService.sendMessage(currentConversationId, message)
            .thenAccept(this::displayMessage);
    }
}
```

---

## ✅ Benefits After Refactoring

### SOLID Principles ✅
- **SRP**: Each class has one responsibility
- **OCP**: Easy to extend (new LLM providers, new UI themes)
- **LSP**: Repository implementations interchangeable
- **ISP**: Focused interfaces
- **DIP**: Depend on abstractions

### Clean Code ✅
- **Testable**: Services can be tested with mock repositories
- **Maintainable**: Clear separation of concerns
- **Reusable**: Services can be used elsewhere
- **Scalable**: Easy to add features

### Features ✅
- **Database Persistence**: Conversations saved to SQLite
- **LLM Integration**: Real AI responses (OpenAI, Claude, Ollama)
- **Streaming**: Real-time AI responses
- **Search**: Find conversations
- **History**: View past conversations
- **Multi-user**: Support multiple users

---

## 📊 Metrics

### Before Refactoring
- **Lines in AIAssistantPage**: 1,100+
- **Classes**: 1 (with 2 inner classes)
- **Responsibilities**: 10+ (UI, data, logic, animation, etc.)
- **Testability**: ❌ Hard to test
- **SOLID**: ❌ Violations everywhere

### After Refactoring
- **Lines in AIAssistantPage**: ~300 (UI only)
- **Classes**: 10+ (separated by responsibility)
- **Responsibilities**: 1 per class
- **Testability**: ✅ Easy to test
- **SOLID**: ✅ Fully compliant

---

## 🚀 Implementation Priority

### High Priority (Do Now)
1. Create domain entities (Conversation, Message)
2. Create repository interfaces
3. Create ConversationService
4. Create AIService (integrate LLMService)
5. Refactor AIAssistantPage to use services

### Medium Priority (Next)
6. Add database persistence (ConversationDAO)
7. Add search functionality
8. Add streaming support

### Low Priority (Later)
9. Extract UI components
10. Add tests
11. Add caching
12. Add export/import

---

*This plan follows Clean Architecture, SOLID principles, and best practices.*
