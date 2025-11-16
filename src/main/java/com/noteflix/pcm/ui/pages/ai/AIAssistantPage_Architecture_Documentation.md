# AIAssistantPage.java - Tài Liệu Kiến Trúc

## Tổng Quan

`AIAssistantPage.java` là một component UI phức tạp được thiết kế để cung cấp giao diện chat AI tương tác thời gian
thực. File này tuân thủ nguyên lý Clean Architecture và các nguyên tắc SOLID.

## Thông Tin Cơ Bản

- **File**: `src/main/java/com/noteflix/pcm/ui/pages/ai/AIAssistantPage.java`
- **Kích thước**: 827 dòng code
- **Framework UI**: JavaFX với AtlantaFX theme
- **Architecture Pattern**: Clean Architecture
- **Design Patterns**: Dependency Injection, Observer Pattern, Service Layer

## Kiến Trúc Chính

### 1. Kế Thừa và Cấu Trúc Class

```java

@Slf4j
public class AIAssistantPage extends BaseView
```

**Đặc điểm:**

- Kế thừa từ `BaseView` để tích hợp với hệ thống navigation
- Sử dụng `@Slf4j` annotation để logging
- Implements Clean Architecture principles

### 2. Dependency Injection

```java
// Services (injected via constructor)
private final ConversationService conversationService;
private final AIService aiService;

public AIAssistantPage(ConversationService conversationService, AIService aiService) {
    // Khởi tạo services ngay sau super() để tránh NPE
    this.conversationService = conversationService;
    this.aiService = aiService;
}
```

**Factory Methods:**

- `createDefaultConversationService()`: Tạo service quản lý conversation
- `createDefaultAIService()`: Tạo service tương tác AI

### 3. UI Components Architecture

#### A. Layout Structure (Cấu trúc 2 cột)

```
HBox mainLayout
├── VBox sidebar (280px cố định)
│   ├── SidebarHeader (New chat button)
│   ├── TextField searchBox
│   └── ScrollPane sessionsScroll (Chat history)
└── VBox contentArea (responsive)
    ├── HBox chatHeader
    └── VBox messagesAndInputArea
        ├── VBox messagesArea
        └── VBox chatInputArea
```

#### B. Key UI Components

- **Sidebar**: Chat history và search functionality
- **Content Area**: Main chat interface
- **Messages Area**: Hiển thị tin nhắn với scroll support
- **Input Area**: Text input với auto-resize và send button

### 4. State Management

#### Current State Variables

```java
private Long currentConversationId; // ID của conversation hiện tại
private final String currentUserId = "default-user"; // TODO: Get from auth service
```

#### UI Component References

```java
private VBox chatSessionsList;     // Danh sách chat sessions
private VBox chatMessagesArea;     // Khu vực hiển thị tin nhắn
private TextArea chatInput;        // Input field
private ScrollPane chatScroll;     // Scroll container
private VBox loadingIndicator;     // Loading animation
private Label streamingMessageLabel; // Real-time streaming text
```

## Core Functionality

### 1. Chat Interface States

#### Welcome State (currentConversationId == null)

- Hiển thị welcome screen với AI icon
- 4 suggestion cards (2x2 grid layout):
    - Search Knowledge
    - Find Solutions
    - Create Content
    - Analyze System

#### Chat State (currentConversationId != null)

- Hiển thị message history
- Real-time message streaming
- Input area với send functionality

### 2. Real-time Streaming Implementation

#### StreamingObserver Pattern

```java
aiService.streamResponse(conversation, message, new StreamingObserver() {
    private StringBuilder fullResponse = new StringBuilder();

    @Override
    public void onChunk (LLMChunk chunk){
        Platform.runLater(() -> {
            fullResponse.append(chunk.getContent());
            updateStreamingMessage(fullResponse.toString());
        });
    }

    @Override
    public void onComplete () {
        Platform.runLater(() -> {
            hideLoadingIndicator();
            finalizeStreamingMessage();
            chatInput.setDisable(false);
        });
    }

    @Override
    public void onError (Throwable error){
        Platform.runLater(() -> {
            hideLoadingIndicator();
            showError(error);
        });
    }
});
```

### 3. Message Flow Architecture

#### Send Message Process

1. **Input Validation**: Kiểm tra message không empty
2. **Conversation Creation**: Tạo conversation mới nếu cần
3. **UI Updates**:
    - Display user message
    - Clear input và disable
    - Show loading indicator
4. **AI Processing**: Stream response từ AI service
5. **Real-time Updates**: Update streaming message
6. **Completion**: Finalize message, enable input, refresh sidebar

### 4. UI Layout Methods

#### Main Layout Creation

- `createMainLayout()`: Tạo HBox chính với sidebar + content
- `createChatSidebar()`: Sidebar với search và chat history
- `createContentArea()`: Main content area
- `createMessagesArea()`: Messages display với welcome/chat states

#### Welcome Content Creation

- `createWelcomeContent()`: Welcome screen với suggestions
- `createWelcomeIcon()`: AI icon với styling
- `createQuickSuggestions()`: GridPane 2x2 với suggestion cards

#### Input Area Creation

- `createChatInputArea()`: Input container
- Auto-resize TextArea functionality
- Send button với keyboard shortcuts (Enter key)

## Service Integration

### 1. ConversationService Integration

```java
// Conversation management
Conversation conv = conversationService.createConversation(
        "New Chat", currentUserId, "openai", "gpt-3.5-turbo");

// Message persistence
List<Conversation> conversations = conversationService.getUserConversations(currentUserId);
Optional<Conversation> convWithMessages = conversationService.getConversationWithMessages(conversationId);
```

### 2. AIService Integration

```java
// Streaming AI responses
aiService.streamResponse(conversation, message, streamingObserver);
```

## UI State Management

### 1. Message Display

- **User Messages**: Right-aligned, blue bubble
- **AI Messages**: Left-aligned, gray bubble
- **Streaming Messages**: Real-time update với StringBuilder
- **Message Timestamps**: Formatted display

### 2. Loading States

- **Loading Indicator**: "AI is thinking..." message
- **Streaming Updates**: Real-time text updates
- **Input Disable**: Prevent multiple concurrent requests

### 3. Navigation Integration

- **Page Activation**: Load conversations on first activation
- **Focus Management**: Auto-focus input field
- **UI Rebuild**: Efficient UI updates khi switch conversations

## Error Handling

### 1. Service Error Handling

```java
// Safety checks cho services
if(conversationService ==null||aiService ==null){
        log.

error("Services not initialized - cannot send message");

showError(new IllegalStateException("Services not initialized"));
        return;
        }
```

### 2. UI Error Display

```java
private void showError(Throwable error) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Error");
    alert.setHeaderText("Failed to get AI response");
    alert.setContentText(error.getMessage());
    alert.showAndWait();
}
```

## Performance Optimizations

### 1. Lazy Loading

- Conversations chỉ load khi page được activate
- Messages load on-demand khi switch conversation

### 2. UI Updates

- Platform.runLater() cho thread safety
- Efficient scroll management
- Component reference reuse

### 3. Memory Management

- StringBuilder cho streaming messages
- Proper cleanup trong error states

## CSS Integration

### 1. Style Classes

- `.ai-chat-page`: Main page styling
- `.chat-sidebar`: Sidebar styling
- `.chat-input-container`: Input area styling
- `.ai-message-bubble` / `.user-message-bubble`: Message styling
- `.suggestion-card`: Welcome suggestion styling

### 2. Layout Classes

- `.main-layout`: Main HBox layout
- `.content-area`: Right content area
- `.messages-area`: Messages display area

## Key Features

### 1. Real-time Chat

- ✅ Streaming AI responses
- ✅ Message persistence
- ✅ Conversation history
- ✅ Search functionality

### 2. User Experience

- ✅ Welcome screen với suggestions
- ✅ Auto-resize input field
- ✅ Keyboard shortcuts (Enter to send)
- ✅ Loading indicators
- ✅ Error handling

### 3. Architecture Benefits

- ✅ Clean separation of concerns
- ✅ Dependency injection support
- ✅ Service layer architecture
- ✅ Observer pattern cho streaming
- ✅ Thread-safe UI updates

## Extension Points

### 1. New Features

- Multi-LLM provider support (đã có interface)
- File attachment support
- Message editing/deletion
- Export conversation functionality

### 2. UI Enhancements

- Message reactions
- Code syntax highlighting
- Image/media message support
- Conversation sharing

### 3. Service Integrations

- Authentication service integration
- Advanced search capabilities
- Real-time collaboration features

## Best Practices Implemented

1. **SOLID Principles**: Single responsibility, dependency inversion
2. **Clean Architecture**: Service layer separation
3. **Error Handling**: Comprehensive error management
4. **Performance**: Lazy loading, efficient updates
5. **Thread Safety**: Platform.runLater() usage
6. **Code Organization**: Clear method separation and naming
7. **Documentation**: Comprehensive JavaDoc comments

## Kết Luận

AIAssistantPage.java là một implementation mạnh mẽ của chat interface với AI, tuân thủ các nguyên tắc architecture tốt
và cung cấp user experience mượt mà với real-time streaming capabilities. Code được tổ chức tốt, có khả năng mở rộng cao
và maintain dễ dàng.