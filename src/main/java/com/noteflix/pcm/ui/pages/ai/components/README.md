# AI Assistant Components

This package contains reusable UI components for the AI Assistant page, refactored from the monolithic `AIAssistantPage.java` for better maintainability and testability.

## Architecture

The components follow a **component-based architecture** with clear separation of concerns:

```
AIAssistantPage (Coordinator)
├── ChatSidebar (Left panel)
│   └── Conversation list with search
├── ChatHeader (Top panel)
│   └── Title and actions
├── Content Area (Main panel)
│   ├── WelcomeScreen (Initial state)
│   │   └── Suggestion cards
│   └── ChatMessagesArea (Active chat)
│       └── MessageBubble (Individual messages)
└── ChatInputArea (Bottom panel)
    └── Input field and send button
```

## Components

### 1. ChatEventHandler (Interface)

**Purpose**: Defines callbacks for chat-related events.

**Methods**:
- `onNewChat()` - Create a new conversation
- `onClearChat()` - Clear current conversation
- `onSendMessage(String)` - Send a message
- `onConversationSelected(Long)` - Switch to a conversation
- `onSearch(String)` - Search conversations

**Used by**: All interactive components to communicate with the coordinator

---

### 2. ChatSidebar

**Purpose**: Displays conversation list with search functionality.

**Responsibilities**:
- Render conversation items
- Handle search input
- Manage active conversation highlighting
- Trigger conversation selection events

**Key Methods**:
- `updateConversations(List<Conversation>)` - Refresh conversation list
- `setCurrentConversationId(Long)` - Highlight active conversation
- `showError(String)` - Display error state

**Styling**: `.chat-sidebar`, `.chat-sessions-list`, `.search-input`

---

### 3. ChatHeader

**Purpose**: Shows conversation title and action buttons.

**Responsibilities**:
- Display conversation title and subtitle
- Provide clear chat action
- Update title dynamically

**Key Methods**:
- `setTitle(String)` - Update conversation title
- `setSubtitle(String)` - Update subtitle text

**Styling**: `.chat-header`

---

### 4. ChatMessagesArea

**Purpose**: Displays message history with scrolling.

**Responsibilities**:
- Render message bubbles
- Handle streaming messages
- Show loading indicators
- Auto-scroll to bottom

**Key Methods**:
- `displayMessages(List<Message>)` - Load message history
- `addUserMessage(Message)` - Add a user message
- `showLoadingIndicator()` - Show "AI is thinking..."
- `updateStreamingMessage(String)` - Update streaming AI response
- `finalizeStreamingMessage()` - Complete streaming
- `clear()` - Clear all messages

**Styling**: `.messages-area`, `.chat-messages-area`, `.chat-scroll`

---

### 5. MessageBubble

**Purpose**: Renders individual message bubbles.

**Responsibilities**:
- Display message content
- Show timestamp
- Style differently for user vs AI messages
- Support streaming messages

**Key Methods**:
- Constructor: `MessageBubble(Message)` - Create from Message object
- Static: `createStreamingBubble(String, Label)` - Create streaming bubble

**Styling**: 
- `.message-box`
- `.user-message-bubble`
- `.ai-message-bubble`
- `.message-text`

---

### 6. ChatInputArea

**Purpose**: Handles user input with send functionality.

**Responsibilities**:
- Capture user input
- Auto-resize for multi-line input
- Handle Enter key (send) and Shift+Enter (newline)
- Enable/disable during AI response
- Trigger send message events

**Key Methods**:
- `clear()` - Clear input field
- `setInputEnabled(boolean)` - Enable/disable input
- `requestInputFocus()` - Focus on input field
- `setText(String)` - Set input text
- `getText()` - Get input text

**Styling**: `.chat-input-container`, `.chat-input-box`, `.chat-input`

---

### 7. WelcomeScreen

**Purpose**: Shows welcome state when no conversation is active.

**Responsibilities**:
- Display welcome icon and message
- Show suggestion cards
- Trigger message sending on card click

**Key Methods**:
- Constructor initializes all welcome elements

**Styling**: 
- `.welcome-content`
- `.welcome-icon-container`
- `.quick-suggestions`
- `.suggestion-card`

---

### 8. SuggestionCard (Record)

**Purpose**: Data class for suggestion cards.

**Fields**:
- `Ikon icon` - Icon for the suggestion
- `String title` - Suggestion title
- `String description` - Suggestion description

---

## Design Patterns

### 1. Component Pattern
Each component is a self-contained JavaFX node with its own:
- Layout structure
- Event handlers
- Styling classes
- Public API

### 2. Observer Pattern
Components communicate with the coordinator (`AIAssistantPage`) via the `ChatEventHandler` interface:
```java
// Component triggers event
eventHandler.onSendMessage(text);

// Coordinator handles event
@Override
public void onSendMessage(String message) {
    // Business logic here
}
```

### 3. Dependency Injection
Components receive their dependencies via constructor:
```java
public ChatSidebar(ChatEventHandler eventHandler) {
    this.eventHandler = eventHandler;
    // ...
}
```

### 4. Builder Pattern (Implicit)
Components build their internal structure in the constructor, following a declarative style:
```java
public ChatHeader(ChatEventHandler eventHandler) {
    // Configure self
    setAlignment(Pos.CENTER_LEFT);
    getStyleClass().add("chat-header");
    
    // Build children
    VBox titleBox = new VBox(2);
    // ...
    
    // Assemble
    getChildren().addAll(icon, titleBox, spacer, actions);
}
```

## Benefits of Component-Based Architecture

### Before (Monolithic)
- **820 lines** in single file
- Mixed concerns (UI, events, state)
- Hard to test individual parts
- Difficult to reuse components

### After (Component-Based)
- **~370 lines** in main coordinator
- **7 specialized components** (~100-150 lines each)
- Clear separation of concerns
- Easy to test components in isolation
- Reusable components
- Better maintainability

## Usage Example

```java
// Create coordinator with services
AIAssistantPage page = new AIAssistantPage(
    conversationService,
    aiService
);

// Components are created and wired automatically
// Event handling flows through ChatEventHandler interface
```

## Testing Strategy

### Unit Testing Components
```java
@Test
void testChatSidebar_UpdatesConversations() {
    ChatEventHandler mockHandler = mock(ChatEventHandler.class);
    ChatSidebar sidebar = new ChatSidebar(mockHandler);
    
    List<Conversation> convs = List.of(
        new Conversation(1L, "Test Chat")
    );
    
    sidebar.updateConversations(convs);
    
    // Verify UI updated
    assertEquals(1, sidebar.getChatSessionsList().getChildren().size());
}
```

### Integration Testing
```java
@Test
void testMessageFlow_UserToAI() {
    // Given: Page with services
    AIAssistantPage page = new AIAssistantPage(
        mockConversationService,
        mockAIService
    );
    
    // When: User sends message
    page.onSendMessage("Hello AI");
    
    // Then: Message appears and AI responds
    verify(mockAIService).streamResponse(any(), eq("Hello AI"), any());
}
```

## Extending Components

### Adding a New Feature

Example: Add voice input to `ChatInputArea`

```java
public class ChatInputArea extends VBox {
    // ... existing code ...
    
    private Button voiceButton;
    
    public ChatInputArea(ChatEventHandler eventHandler) {
        // ... existing setup ...
        
        // Add voice button
        voiceButton = new Button();
        voiceButton.setGraphic(new FontIcon(Feather.MIC));
        voiceButton.setOnAction(e -> handleVoiceInput());
        
        inputActions.getChildren().add(voiceButton);
    }
    
    private void handleVoiceInput() {
        // Voice input logic
    }
}
```

### Creating a New Component

1. Extend appropriate JavaFX container (`VBox`, `HBox`, etc.)
2. Accept `ChatEventHandler` in constructor
3. Build UI structure in constructor
4. Expose public API methods
5. Add to `AIAssistantPage` coordinator

## CSS Styling

All components use semantic CSS classes. Define styles in `src/main/resources/css/styles.css`:

```css
/* Sidebar */
.chat-sidebar {
    -fx-background-color: -color-bg-subtle;
    -fx-border-color: -color-border-default;
}

.chat-session-btn {
    -fx-background-radius: 8px;
    -fx-padding: 0;
}

.chat-session-btn.active {
    -fx-background-color: -color-accent-subtle;
}

/* Messages */
.ai-message-bubble {
    -fx-background-color: -color-bg-default;
    -fx-background-radius: 12px;
}

.user-message-bubble {
    -fx-background-color: -color-accent-emphasis;
    -fx-background-radius: 12px;
}

/* Input */
.chat-input {
    -fx-font-size: 14px;
    -fx-padding: 12px;
}
```

## Best Practices

1. **Keep components focused**: Each component should have a single, well-defined responsibility
2. **Use interfaces for communication**: Don't directly couple components
3. **Favor composition**: Build complex UIs from simple components
4. **Make components reusable**: Avoid hardcoding values; use parameters
5. **Test components independently**: Write unit tests for each component
6. **Document public APIs**: Add JavaDoc to all public methods
7. **Use semantic CSS classes**: Make styling predictable and maintainable

## Future Improvements

- [ ] Extract common UI patterns into shared utilities
- [ ] Add animation transitions between states
- [ ] Support drag-and-drop for file attachments
- [ ] Add markdown rendering for AI responses
- [ ] Implement message reactions/feedback
- [ ] Add conversation export functionality
- [ ] Support conversation folders/tags
- [ ] Add keyboard shortcuts
- [ ] Implement accessibility features (ARIA, screen reader support)

## Related Documentation

- [UI Guide](../../../../../docs/ui/UI_GUIDE.md)
- [Architecture Overview](../../../../../docs/architecture/04-architecture-layers.md)
- [AtlantaFX Documentation](https://github.com/mkpaz/atlantafx)

