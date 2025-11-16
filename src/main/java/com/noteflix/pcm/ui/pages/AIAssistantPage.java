package com.noteflix.pcm.ui.pages;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.application.service.chat.AIService;
import com.noteflix.pcm.application.service.chat.ConversationService;
import com.noteflix.pcm.domain.chat.Conversation;
import com.noteflix.pcm.domain.chat.Message;
import com.noteflix.pcm.llm.model.LLMChunk;
import com.noteflix.pcm.llm.model.StreamingObserver;
import com.noteflix.pcm.ui.pages.ai.components.ChatInputArea;
import com.noteflix.pcm.ui.pages.ai.components.ChatMessageList;
import com.noteflix.pcm.ui.pages.ai.components.ChatSidebar;
import com.noteflix.pcm.ui.pages.ai.components.ConversationItem;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import com.noteflix.pcm.ui.utils.DialogHelper;
import com.noteflix.pcm.ui.utils.LayoutHelper;

import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * AI Assistant Page - Refactored with Component Architecture
 *
 * <p>Clean implementation using reusable components:
 * - ChatSidebar: Conversation list and search
 * - ChatMessageList: Message display with streaming
 * - ChatInputArea: User input with send button
 * - ConversationItem: Individual conversation item
 *
 * <p>Features:
 * - Real-time AI chat with streaming responses
 * - Persistent conversation history via database
 * - Search and conversation management
 * - Multi-LLM provider support
 *
 * @author PCM Team
 * @version 3.0.0 (Refactored)
 */
@Slf4j
public class AIAssistantPage extends BasePage {

  // Services (injected via constructor)
  private final ConversationService conversationService;
  private final AIService aiService;
  private final String currentUserId = "default-user"; // TODO: Get from auth service

  // Current state
  private Long currentConversationId;

  // UI Components (new component-based architecture)
  private ChatSidebar chatSidebar;
  private ChatMessageList chatMessageList;
  private ChatInputArea chatInputArea;

  /** Constructor with dependency injection */
  public AIAssistantPage(ConversationService conversationService, AIService aiService) {
    super(
        "AI Assistant",
        "Chat with AI to analyze your system, review code, and get intelligent insights",
        new FontIcon(Feather.MESSAGE_CIRCLE));

    // IMPORTANT: Assign services IMMEDIATELY after super() to avoid NPE
    this.conversationService = conversationService;
    this.aiService = aiService;

    // Override default padding for full-screen chat
    setPadding(new Insets(0));
    setSpacing(0);
    getStyleClass().add("ai-chat-page");

    log.info("AIAssistantPage initialized with component-based architecture");
  }

  /** Default constructor (creates services internally) */
  public AIAssistantPage() {
    this(createDefaultConversationService(), createDefaultAIService());
  }

  /** Factory method for default ConversationService */
  private static ConversationService createDefaultConversationService() {
    return new ConversationService();
  }

  /** Factory method for default AIService */
  private static AIService createDefaultAIService() {
    return new AIService();
  }

  @Override
  protected VBox createMainContent() {
    VBox mainContent = new VBox();
    mainContent.getStyleClass().add("ai-chat-main");
    mainContent.setPadding(new Insets(0));
    VBox.setVgrow(mainContent, Priority.ALWAYS);

    // Main layout: 2 columns (sidebar + content)
    HBox mainLayout = createMainLayout();
    VBox.setVgrow(mainLayout, Priority.ALWAYS);

    mainContent.getChildren().add(mainLayout);
    return mainContent;
  }

  @Override
  protected VBox createPageHeader() {
    // AI Chat doesn't need the standard page header
    return null;
  }

  /**
   * Create main layout with sidebar and content area
   * This is the only layout method - all other UI is in components
   */
  private HBox createMainLayout() {
    HBox mainLayout = new HBox();
    mainLayout.getStyleClass().add("main-layout");
    mainLayout.setSpacing(0);

    // Left sidebar - NEW: Use ChatSidebar component
    chatSidebar = new ChatSidebar()
        .withNewChatHandler(this::handleNewChat)
        .withSearchHandler(this::handleSearch);

    // Right content area
    VBox contentArea = createContentArea();
    HBox.setHgrow(contentArea, Priority.ALWAYS);

    mainLayout.getChildren().addAll(chatSidebar, contentArea);
    return mainLayout;
  }

  /**
   * Create content area with messages and input
   * Uses new ChatMessageList and ChatInputArea components
   */
  private VBox createContentArea() {
    VBox contentArea = LayoutHelper.createVBox(0);
    contentArea.getStyleClass().add("content-area");
    VBox.setVgrow(contentArea, Priority.ALWAYS);

    // Top: Header
    HBox chatHeader = createChatHeader();

    // Middle: Messages area - NEW: Use ChatMessageList component
    chatMessageList = new ChatMessageList();
    LayoutHelper.setVGrow(chatMessageList);

    // Show welcome if no conversation
    if (currentConversationId == null) {
      chatMessageList.showWelcome();
    }

    // Bottom: Input area - NEW: Use ChatInputArea component
    chatInputArea = new ChatInputArea()
        .withSendHandler(this::handleSendMessage);

    contentArea.getChildren().addAll(chatHeader, chatMessageList, chatInputArea);
    return contentArea;
  }

  /**
   * Create chat header with title and actions
   */
  private HBox createChatHeader() {
    HBox header = LayoutHelper.createHBox(
        javafx.geometry.Pos.CENTER_LEFT,
        LayoutConstants.SPACING_MD
    );
    header.getStyleClass().add("chat-header");
    header.setPadding(LayoutConstants.PADDING_DEFAULT);

    FontIcon chatIcon = new FontIcon(Feather.MESSAGE_CIRCLE);
    chatIcon.setIconSize(LayoutConstants.ICON_SIZE_SM);

    VBox titleBox = LayoutHelper.createVBox(LayoutConstants.SPACING_XS);
    Label titleLabel = new Label(getCurrentTitle());
    titleLabel.getStyleClass().addAll(Styles.TITLE_4);

    Label subtitleLabel = new Label("AI-powered system analysis and assistance");
    subtitleLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");

    titleBox.getChildren().addAll(titleLabel, subtitleLabel);

    // Spacer
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    // Clear chat button
    Button clearBtn = new Button();
    clearBtn.setGraphic(new FontIcon(Feather.TRASH_2));
    clearBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, "icon-btn");
    clearBtn.setTooltip(new Tooltip("Clear Chat"));
    clearBtn.setOnAction(e -> handleClearChat());

    header.getChildren().addAll(chatIcon, titleBox, spacer, clearBtn);
    return header;
  }

  // ========== Event Handlers ==========

  /**
   * Handle sending a message
   * Updated to work with new ChatInputArea component
   */
  private void handleSendMessage(String message) {
    if (message == null || message.trim().isEmpty()) {
      return;
    }

    // Safety check
    if (conversationService == null || aiService == null) {
      log.error("Services not initialized - cannot send message");
      DialogHelper.showError("Error", "Services not initialized");
      return;
    }

    log.info("Sending message: {}", message);

    // Create conversation if needed
    if (currentConversationId == null) {
      try {
        Conversation conv = conversationService.createConversation(
            "New Chat",
            currentUserId,
            "openai",
            "gpt-3.5-turbo"
        );
        currentConversationId = conv.getId();
        
        // Rebuild UI to show messages area
        rebuildUI();
        
        // Re-send message after rebuild
        Platform.runLater(() -> handleSendMessage(message));
        return;
      } catch (Exception e) {
        log.error("Failed to create conversation", e);
        DialogHelper.showError("Error", "Failed to create conversation: " + e.getMessage());
        return;
      }
    }

    // Display user message
    Message userMsg = Message.user(currentConversationId, message);
    chatMessageList.addMessage(userMsg);

    // Clear and disable input
    chatInputArea.clear();
    chatInputArea.setInputDisabled(true);

    // Get AI response with streaming
    aiService.streamResponse(
        conversationService.getConversation(currentConversationId).get(),
        message,
        new StreamingObserver() {
          private StringBuilder fullResponse = new StringBuilder();

          @Override
          public void onChunk(LLMChunk chunk) {
            Platform.runLater(() -> {
              fullResponse.append(chunk.getContent());
              chatMessageList.showStreaming(fullResponse.toString());
            });
          }

          @Override
          public void onComplete() {
            Platform.runLater(() -> {
              chatMessageList.hideStreaming();
              loadMessages(); // Reload to get persisted message
              chatInputArea.setInputDisabled(false);
              chatInputArea.requestInputFocus();
              loadConversations(); // Refresh sidebar
            });
          }

          @Override
          public void onError(Throwable error) {
            Platform.runLater(() -> {
              chatMessageList.hideStreaming();
              DialogHelper.showError("AI Error", error.getMessage());
              chatInputArea.setInputDisabled(false);
              log.error("AI response error", error);
            });
          }
        });
  }

  /**
   * Handle creating a new chat
   */
  private void handleNewChat() {
    currentConversationId = null;
    rebuildUI();
    chatInputArea.requestInputFocus();
  }

  /**
   * Handle clearing current chat
   */
  private void handleClearChat() {
    if (currentConversationId != null) {
      conversationService.clearConversation(currentConversationId);
      rebuildUI();
    }
  }

  /**
   * Handle search in conversations
   */
  private void handleSearch(String query) {
    if (query == null || query.trim().isEmpty()) {
      loadConversations();
    } else {
      List<Conversation> results = conversationService.searchConversations(currentUserId, query);
      updateConversationsList(results);
    }
  }

  /**
   * Switch to a different conversation
   */
  private void switchToConversation(Long conversationId) {
    if (!conversationId.equals(currentConversationId)) {
      currentConversationId = conversationId;
      rebuildUI();
    }
  }

  // ========== UI Update Methods ==========

  /**
   * Load conversations from database and update sidebar
   */
  private void loadConversations() {
    if (conversationService == null) {
      log.warn("ConversationService is null - cannot load conversations");
      return;
    }

    try {
      List<Conversation> conversations = conversationService.getUserConversations(currentUserId);
      updateConversationsList(conversations);
    } catch (Exception e) {
      log.error("Failed to load conversations", e);
      DialogHelper.showError("Error", "Failed to load conversations: " + e.getMessage());
    }
  }

  /**
   * Update conversations list in sidebar
   * NEW: Uses ConversationItem component
   */
  private void updateConversationsList(List<Conversation> conversations) {
    chatSidebar.clearConversations();

    for (Conversation conv : conversations) {
      ConversationItem item = new ConversationItem(conv)
          .withSelectHandler(c -> switchToConversation(c.getId()))
          .withSelected(conv.getId().equals(currentConversationId));
      
      chatSidebar.addConversation(item);
    }
  }

  /**
   * Load messages for current conversation
   * NEW: Simplified with ChatMessageList component
   */
  private void loadMessages() {
    if (currentConversationId == null || chatMessageList == null) {
      return;
    }

    if (conversationService == null) {
      log.warn("ConversationService is null - cannot load messages");
      return;
    }

    try {
      Optional<Conversation> convOpt = 
          conversationService.getConversationWithMessages(currentConversationId);

      if (convOpt.isPresent()) {
        Conversation conv = convOpt.get();
        chatMessageList.clearMessages();

        for (Message msg : conv.getMessages()) {
          chatMessageList.addMessage(msg);
        }
      }
    } catch (Exception e) {
      log.error("Failed to load messages", e);
      DialogHelper.showError("Error", "Failed to load messages: " + e.getMessage());
    }
  }

  /**
   * Rebuild entire UI (when switching conversations)
   */
  private void rebuildUI() {
    VBox mainContent = (VBox) getChildren().get(getChildren().size() - 1);
    mainContent.getChildren().clear();

    HBox mainLayout = createMainLayout();
    VBox.setVgrow(mainLayout, Priority.ALWAYS);

    mainContent.getChildren().add(mainLayout);

    // Load data
    loadConversations();
    if (currentConversationId != null) {
      loadMessages();
    }
  }

  /**
   * Get current conversation title
   */
  private String getCurrentTitle() {
    if (currentConversationId != null) {
      Optional<Conversation> conv = conversationService.getConversation(currentConversationId);
      return conv.map(Conversation::getTitle).orElse("New Chat");
    }
    return "New Chat";
  }

  @Override
  public void onPageActivated() {
    super.onPageActivated();

    // Load conversations on first activation (lazy initialization)
    loadConversations();

    if (chatInputArea != null) {
      chatInputArea.requestInputFocus();
    }
  }
}
