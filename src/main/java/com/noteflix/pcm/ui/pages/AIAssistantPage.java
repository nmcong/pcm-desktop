package com.noteflix.pcm.ui.pages;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.application.service.chat.AIService;
import com.noteflix.pcm.application.service.chat.ConversationService;
import com.noteflix.pcm.domain.chat.Conversation;
import com.noteflix.pcm.domain.chat.Message;
import com.noteflix.pcm.llm.model.LLMChunk;
import com.noteflix.pcm.llm.model.StreamingObserver;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * AI Assistant Page - Clean Architecture Implementation
 *
 * <p>Follows SOLID principles: - Single Responsibility: Only handles UI rendering - Dependency
 * Inversion: Depends on service interfaces - Open/Closed: Easy to extend with new features
 *
 * <p>Features: - Real-time AI chat with streaming responses - Persistent conversation history via
 * database - Search and conversation management - Multi-LLM provider support
 *
 * @author PCM Team
 * @version 2.0.0
 */
@Slf4j
public class AIAssistantPage extends BasePage {

  // Services (injected via constructor)
  private final ConversationService conversationService;
  private final AIService aiService;
  private final String currentUserId = "default-user"; // TODO: Get from auth service
  // Current state
  private Long currentConversationId;
  // UI Components
  private VBox chatSessionsList;
  private VBox chatMessagesArea;
  private TextArea chatInput;
  private ScrollPane chatScroll;
  private VBox loadingIndicator;
  private Label streamingMessageLabel;

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

    log.info("AIAssistantPage initialized with services");
  }

  /**
   * Default constructor (creates services internally) Note: Services are created here and passed to
   * main constructor
   */
  public AIAssistantPage() {
    // Create services BEFORE calling super to avoid NPE during UI construction
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

  private HBox createMainLayout() {
    HBox mainLayout = new HBox();
    mainLayout.getStyleClass().add("main-layout");
    mainLayout.setSpacing(0);

    // Left sidebar (280px fixed width)
    VBox sidebar = createChatSidebar();
    sidebar.setPrefWidth(280);
    sidebar.setMinWidth(280);
    sidebar.setMaxWidth(280);

    // Right content area (grows to fill remaining space)
    VBox contentArea = createContentArea();
    HBox.setHgrow(contentArea, Priority.ALWAYS);

    mainLayout.getChildren().addAll(sidebar, contentArea);
    return mainLayout;
  }

  private VBox createChatSidebar() {
    VBox sidebar = new VBox();
    sidebar.getStyleClass().add("chat-sidebar");
    sidebar.setPadding(new Insets(16));
    sidebar.setSpacing(12);

    // Sidebar header with new chat button
    HBox sidebarHeader = createSidebarHeader();

    // Search box
    TextField searchBox = new TextField();
    searchBox.setPromptText("Search conversations...");
    searchBox.getStyleClass().add("search-input");
    searchBox.textProperty().addListener((obs, old, newVal) -> handleSearch(newVal));

    // Chat sessions list
    Label historyLabel = new Label("Chat History");
    historyLabel.getStyleClass().addAll(Styles.TEXT_BOLD, "sidebar-section-title");

    chatSessionsList = new VBox(8);
    chatSessionsList.getStyleClass().add("chat-sessions-list");

    ScrollPane sessionsScroll = new ScrollPane(chatSessionsList);
    sessionsScroll.setFitToWidth(true);
    sessionsScroll.getStyleClass().add("sessions-scroll");
    VBox.setVgrow(sessionsScroll, Priority.ALWAYS);

    sidebar.getChildren().addAll(sidebarHeader, searchBox, historyLabel, sessionsScroll);

    // NOTE: Don't load conversations here - will be loaded in onPageActivated()
    // This avoids NPE during constructor execution

    return sidebar;
  }

  private HBox createSidebarHeader() {
    HBox header = new HBox(12);
    header.setAlignment(Pos.CENTER_LEFT);

    Label title = new Label("AI Assistant");
    title.getStyleClass().addAll(Styles.TITLE_4);

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    Button newChatBtn = new Button();
    newChatBtn.setGraphic(new FontIcon(Feather.PLUS));
    newChatBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
    newChatBtn.setTooltip(new Tooltip("New Chat"));
    newChatBtn.setOnAction(e -> createNewChat());

    header.getChildren().addAll(title, spacer, newChatBtn);
    return header;
  }

  private VBox createContentArea() {
    VBox contentArea = new VBox();
    contentArea.getStyleClass().add("content-area");
    VBox.setVgrow(contentArea, Priority.ALWAYS);

    // Top: Header
    HBox chatHeader = createChatHeader();

    // Bottom: Messages area + Input area
    VBox messagesAndInputArea = createMessagesAndInputArea();
    VBox.setVgrow(messagesAndInputArea, Priority.ALWAYS);

    contentArea.getChildren().addAll(chatHeader, messagesAndInputArea);
    return contentArea;
  }

  private HBox createChatHeader() {
    HBox header = new HBox(12);
    header.setAlignment(Pos.CENTER_LEFT);
    header.getStyleClass().add("chat-header");
    header.setPadding(new Insets(12, 16, 12, 16));

    FontIcon chatIcon = new FontIcon(Feather.MESSAGE_CIRCLE);
    chatIcon.setIconSize(16);

    VBox titleBox = new VBox(2);
    Label titleLabel = new Label(getCurrentTitle());
    titleLabel.getStyleClass().addAll(Styles.TITLE_4);

    Label subtitleLabel = new Label("AI-powered system analysis and assistance");
    subtitleLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");

    titleBox.getChildren().addAll(titleLabel, subtitleLabel);

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    // Chat actions
    Button clearBtn = new Button();
    clearBtn.setGraphic(new FontIcon(Feather.TRASH_2));
    clearBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
    clearBtn.setTooltip(new Tooltip("Clear Chat"));
    clearBtn.setOnAction(e -> clearCurrentChat());

    HBox actions = new HBox(8, clearBtn);

    header.getChildren().addAll(chatIcon, titleBox, spacer, actions);
    return header;
  }

  private VBox createMessagesAndInputArea() {
    VBox container = new VBox();
    container.getStyleClass().add("messages-and-input-area");
    VBox.setVgrow(container, Priority.ALWAYS);

    // Messages area (grows to fill space)
    VBox messagesArea = createMessagesArea();
    VBox.setVgrow(messagesArea, Priority.ALWAYS);

    // Input area (fixed height at bottom)
    VBox inputArea = createChatInputArea();

    container.getChildren().addAll(messagesArea, inputArea);
    return container;
  }

  private VBox createMessagesArea() {
    VBox messagesArea = new VBox();
    messagesArea.getStyleClass().add("messages-area");
    VBox.setVgrow(messagesArea, Priority.ALWAYS);

    if (currentConversationId == null) {
      // Welcome state
      VBox welcomeContent = createWelcomeContent();
      messagesArea.getChildren().add(welcomeContent);
      messagesArea.setAlignment(Pos.CENTER);
    } else {
      // Chat state
      messagesArea.setAlignment(Pos.TOP_LEFT);

      VBox messagesContainer = new VBox();
      messagesContainer.setAlignment(Pos.TOP_CENTER);
      messagesContainer.setPadding(new Insets(20));

      chatMessagesArea = new VBox(12);
      chatMessagesArea.getStyleClass().add("chat-messages-area");
      chatMessagesArea.setMaxWidth(768);
      chatMessagesArea.setAlignment(Pos.TOP_LEFT);

      messagesContainer.getChildren().add(chatMessagesArea);

      chatScroll = new ScrollPane(messagesContainer);
      chatScroll.setFitToWidth(true);
      chatScroll.getStyleClass().add("chat-scroll");
      chatScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
      chatScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
      VBox.setVgrow(chatScroll, Priority.ALWAYS);

      messagesArea.getChildren().add(chatScroll);

      // Load messages
      loadMessages();
    }

    return messagesArea;
  }

  private VBox createWelcomeContent() {
    VBox welcome = new VBox(24);
    welcome.setAlignment(Pos.CENTER);
    welcome.getStyleClass().add("welcome-content");
    welcome.setPadding(new Insets(40, 40, 40, 40));

    // AI icon with gradient background
    VBox iconContainer = createWelcomeIcon();

    Label title = new Label("AI Assistant");
    title.getStyleClass().addAll(Styles.TITLE_2);

    Label subtitle = new Label("How can I help you today?");
    subtitle.getStyleClass().addAll(Styles.TEXT_MUTED);

    // Quick suggestions
    javafx.scene.layout.GridPane suggestions = createQuickSuggestions();

    welcome.getChildren().addAll(iconContainer, title, subtitle, suggestions);
    return welcome;
  }

  private VBox createWelcomeIcon() {
    VBox container = new VBox();
    container.setAlignment(Pos.CENTER);
    container.getStyleClass().add("welcome-icon-container");
    container.setPrefSize(90, 90);
    container.setMinSize(90, 90);
    container.setMaxSize(90, 90);

    // Main AI icon - using CommentOutlined from Ant Design
    FontIcon mainIcon = new FontIcon(AntDesignIconsOutlined.COMMENT);
    mainIcon.setIconSize(40);
    mainIcon.getStyleClass().add("welcome-ai-icon");

    container.getChildren().add(mainIcon);
    return container;
  }

  private GridPane createQuickSuggestions() {
    javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
    grid.setAlignment(Pos.CENTER);
    grid.setHgap(16);
    grid.setVgap(16);
    grid.getStyleClass().add("quick-suggestions");
    grid.setMaxWidth(600);

    // Define 4 suggestion themes with icons
    SuggestionCard[] suggestions = {
      new SuggestionCard(
          Feather.SEARCH, "Search Knowledge", "Explore database schema and structure"),
      new SuggestionCard(Feather.TOOL, "Find Solutions", "Review code quality and best practices"),
      new SuggestionCard(Feather.EDIT_3, "Create Content", "Generate documentation and reports"),
      new SuggestionCard(
          Feather.ACTIVITY, "Analyze System", "Get insights on performance and metrics")
    };

    // Add cards to grid (2x2 layout)
    int row = 0, col = 0;
    for (SuggestionCard suggestion : suggestions) {
      VBox card = createSuggestionCard(suggestion);
      grid.add(card, col, row);

      col++;
      if (col > 1) {
        col = 0;
        row++;
      }
    }

    return grid;
  }

  private VBox createSuggestionCard(SuggestionCard suggestion) {
    VBox card = new VBox(8);
    card.setAlignment(Pos.CENTER);
    card.getStyleClass().add("suggestion-card");
    card.setPrefWidth(280);
    card.setPrefHeight(120);

    // Icon
    FontIcon icon = new FontIcon(suggestion.icon);
    icon.setIconSize(28);
    icon.getStyleClass().add("suggestion-icon");

    // Title
    Label titleLabel = new Label(suggestion.title);
    titleLabel.getStyleClass().addAll(Styles.TEXT_BOLD, "suggestion-title");
    titleLabel.setWrapText(true);
    titleLabel.setAlignment(Pos.CENTER);
    titleLabel.setMaxWidth(250);

    // Description
    Label descLabel = new Label(suggestion.description);
    descLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "suggestion-desc", "text-muted");
    descLabel.setWrapText(true);
    descLabel.setAlignment(Pos.CENTER);
    descLabel.setMaxWidth(250);

    card.getChildren().addAll(icon, titleLabel, descLabel);

    // Click handler
    card.setOnMouseClicked(
        e -> {
          chatInput.setText(suggestion.description);
          handleSendMessage();
        });

    return card;
  }

  // Helper class for suggestion data
  private static class SuggestionCard {
    final Feather icon;
    final String title;
    final String description;

    SuggestionCard(Feather icon, String title, String description) {
      this.icon = icon;
      this.title = title;
      this.description = description;
    }
  }

  private VBox createChatInputArea() {
    VBox inputArea = new VBox(12);
    inputArea.getStyleClass().add("chat-input-container");
    inputArea.setPadding(new Insets(20));
    inputArea.setAlignment(Pos.CENTER);

    HBox inputBox = new HBox(12);
    inputBox.setAlignment(Pos.CENTER);
    inputBox.getStyleClass().add("chat-input-box");
    inputBox.setMaxWidth(768);

    VBox inputWrapper = new VBox();
    inputWrapper.getStyleClass().add("input-wrapper");

    chatInput = new TextArea();
    chatInput.setPromptText("Ask me anything about your system...");
    chatInput.getStyleClass().add("chat-input");
    chatInput.setPrefRowCount(1);
    chatInput.setWrapText(true);

    // Auto-resize
    chatInput
        .textProperty()
        .addListener(
            (obs, old, newText) -> {
              if (newText.contains("\n")) {
                int lines = newText.split("\n").length;
                chatInput.setPrefRowCount(Math.min(lines, 8));
              }
            });

    // Input actions
    HBox inputActions = new HBox(8);
    inputActions.setAlignment(Pos.CENTER_RIGHT);
    inputActions.setPadding(new Insets(8));

    Button sendBtn = new Button();
    sendBtn.setGraphic(new FontIcon(Feather.SEND));
    sendBtn.getStyleClass().addAll(Styles.BUTTON_ICON, "send-btn");
    sendBtn.setOnAction(e -> handleSendMessage());

    inputActions.getChildren().add(sendBtn);

    inputWrapper.getChildren().addAll(chatInput, inputActions);
    HBox.setHgrow(inputWrapper, Priority.ALWAYS);

    inputBox.getChildren().add(inputWrapper);
    inputArea.getChildren().add(inputBox);

    // Enter key handler
    chatInput.setOnKeyPressed(
        e -> {
          if (e.getCode().toString().equals("ENTER") && !e.isShiftDown()) {
            e.consume();
            handleSendMessage();
          }
        });

    return inputArea;
  }

  // ========== Event Handlers ==========

  private void handleSendMessage() {
    String message = chatInput.getText().trim();
    if (message.isEmpty()) return;

    // Safety check
    if (conversationService == null || aiService == null) {
      log.error("Services not initialized - cannot send message");
      showError(new IllegalStateException("Services not initialized"));
      return;
    }

    log.info("Sending message: {}", message);

    // Create conversation if needed
    if (currentConversationId == null) {
      try {
        Conversation conv =
            conversationService.createConversation(
                "New Chat", currentUserId, "openai", "gpt-3.5-turbo");
        currentConversationId = conv.getId();
        rebuildUI();
      } catch (Exception e) {
        log.error("Failed to create conversation", e);
        showError(e);
        return;
      }
    }

    // Display user message
    displayUserMessage(message);

    // Clear and disable input
    chatInput.clear();
    chatInput.setPrefRowCount(1);
    chatInput.setDisable(true);

    // Show loading
    showLoadingIndicator();

    // Get AI response (with streaming)
    aiService.streamResponse(
        conversationService.getConversation(currentConversationId).get(),
        message,
        new StreamingObserver() {
          private StringBuilder fullResponse = new StringBuilder();

          @Override
          public void onChunk(LLMChunk chunk) {
            Platform.runLater(
                () -> {
                  fullResponse.append(chunk.getContent());
                  updateStreamingMessage(fullResponse.toString());
                });
          }

          @Override
          public void onComplete() {
            Platform.runLater(
                () -> {
                  hideLoadingIndicator();
                  finalizeStreamingMessage();
                  chatInput.setDisable(false);
                  chatInput.requestFocus();
                  loadConversations(); // Refresh sidebar
                });
          }

          @Override
          public void onError(Throwable error) {
            Platform.runLater(
                () -> {
                  hideLoadingIndicator();
                  showError(error);
                  chatInput.setDisable(false);
                });
          }
        });
  }

  private void createNewChat() {
    currentConversationId = null;
    rebuildUI();
    if (chatInput != null) {
      chatInput.requestFocus();
    }
  }

  private void clearCurrentChat() {
    if (currentConversationId != null) {
      conversationService.clearConversation(currentConversationId);
      rebuildUI();
    }
  }

  private void handleSearch(String query) {
    if (query == null || query.trim().isEmpty()) {
      loadConversations();
    } else {
      List<Conversation> results = conversationService.searchConversations(currentUserId, query);
      updateConversationsList(results);
    }
  }

  private void switchToConversation(Long conversationId) {
    if (!conversationId.equals(currentConversationId)) {
      currentConversationId = conversationId;
      rebuildUI();
    }
  }

  // ========== UI Update Methods ==========

  private void loadConversations() {
    // Safety check: Services should be initialized by now
    if (conversationService == null) {
      log.warn("ConversationService is null - cannot load conversations");
      return;
    }

    try {
      List<Conversation> conversations = conversationService.getUserConversations(currentUserId);
      updateConversationsList(conversations);
    } catch (Exception e) {
      log.error("Failed to load conversations", e);
      // Show error in UI
      if (chatSessionsList != null) {
        chatSessionsList.getChildren().clear();
        Label errorLabel = new Label("Failed to load conversations");
        errorLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-danger");
        chatSessionsList.getChildren().add(errorLabel);
      }
    }
  }

  private void updateConversationsList(List<Conversation> conversations) {
    chatSessionsList.getChildren().clear();

    for (Conversation conv : conversations) {
      Button sessionBtn = createConversationButton(conv);
      chatSessionsList.getChildren().add(sessionBtn);
    }
  }

  private Button createConversationButton(Conversation conv) {
    HBox content = new HBox(12);
    content.setAlignment(Pos.CENTER_LEFT);
    content.setPadding(new Insets(12));

    VBox textContent = new VBox(4);
    HBox.setHgrow(textContent, Priority.ALWAYS);

    Label titleLabel = new Label(conv.getTitle());
    titleLabel.getStyleClass().addAll(Styles.TEXT_BOLD);
    titleLabel.setMaxWidth(200);

    Label previewLabel =
        new Label(conv.getPreview() != null ? conv.getPreview() : "Empty conversation");
    previewLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");
    previewLabel.setMaxWidth(200);

    textContent.getChildren().addAll(titleLabel, previewLabel);

    Label timeLabel = new Label(conv.getUpdatedAt().format(DateTimeFormatter.ofPattern("MMM dd")));
    timeLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");

    content.getChildren().addAll(textContent, timeLabel);

    Button sessionBtn = new Button();
    sessionBtn.setGraphic(content);
    sessionBtn.getStyleClass().add("chat-session-btn");
    sessionBtn.setMaxWidth(Double.MAX_VALUE);

    if (conv.getId().equals(currentConversationId)) {
      sessionBtn.getStyleClass().add("active");
    }

    sessionBtn.setOnAction(e -> switchToConversation(conv.getId()));

    return sessionBtn;
  }

  private void loadMessages() {
    if (currentConversationId == null || chatMessagesArea == null) return;

    // Safety check
    if (conversationService == null) {
      log.warn("ConversationService is null - cannot load messages");
      return;
    }

    try {
      Optional<Conversation> convOpt =
          conversationService.getConversationWithMessages(currentConversationId);

      if (convOpt.isPresent()) {
        Conversation conv = convOpt.get();
        chatMessagesArea.getChildren().clear();

        for (Message msg : conv.getMessages()) {
          VBox messageBox = createMessageBox(msg);
          chatMessagesArea.getChildren().add(messageBox);
        }

        scrollToBottom();
      }
    } catch (Exception e) {
      log.error("Failed to load messages", e);
    }
  }

  private void displayUserMessage(String content) {
    Message userMsg = Message.user(currentConversationId, content);
    VBox messageBox = createMessageBox(userMsg);
    chatMessagesArea.getChildren().add(messageBox);
    scrollToBottom();
  }

  private VBox createMessageBox(Message message) {
    VBox messageBox = new VBox(8);
    messageBox.getStyleClass().add("message-box");

    HBox messageRow = new HBox(12);
    messageRow.setAlignment(message.isFromAI() ? Pos.TOP_LEFT : Pos.TOP_RIGHT);

    // Message bubble
    VBox bubble = new VBox(4);
    bubble.getStyleClass().add(message.isFromAI() ? "ai-message-bubble" : "user-message-bubble");
    bubble.setPadding(new Insets(12, 16, 12, 16));
    bubble.setMaxWidth(600);

    Label messageLabel = new Label(message.getContent());
    messageLabel.setWrapText(true);
    messageLabel.getStyleClass().add("message-text");

    Label timeLabel =
        new Label(message.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")));
    timeLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");

    bubble.getChildren().addAll(messageLabel, timeLabel);

    if (message.isFromAI()) {
      messageRow.getChildren().add(bubble);
    } else {
      messageRow.getChildren().add(bubble);
    }

    messageBox.getChildren().add(messageRow);
    return messageBox;
  }

  private void showLoadingIndicator() {
    if (chatMessagesArea != null) {
      loadingIndicator = new VBox(8);
      loadingIndicator.getStyleClass().add("loading-indicator");
      loadingIndicator.setAlignment(Pos.CENTER_LEFT);

      Label loadingText = new Label("AI is thinking...");
      loadingText.getStyleClass().add("loading-text");

      loadingIndicator.getChildren().add(loadingText);
      chatMessagesArea.getChildren().add(loadingIndicator);
      scrollToBottom();
    }
  }

  private void hideLoadingIndicator() {
    if (chatMessagesArea != null && loadingIndicator != null) {
      chatMessagesArea.getChildren().remove(loadingIndicator);
      loadingIndicator = null;
    }
  }

  private void updateStreamingMessage(String content) {
    if (streamingMessageLabel == null) {
      hideLoadingIndicator();

      VBox bubble = new VBox(4);
      bubble.getStyleClass().add("ai-message-bubble");
      bubble.setPadding(new Insets(12, 16, 12, 16));
      bubble.setMaxWidth(600);

      streamingMessageLabel = new Label(content);
      streamingMessageLabel.setWrapText(true);
      streamingMessageLabel.getStyleClass().add("message-text");

      bubble.getChildren().add(streamingMessageLabel);
      chatMessagesArea.getChildren().add(bubble);
    } else {
      streamingMessageLabel.setText(content);
    }

    scrollToBottom();
  }

  private void finalizeStreamingMessage() {
    streamingMessageLabel = null;
    loadMessages(); // Reload to get persisted message
  }

  private void showError(Throwable error) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Error");
    alert.setHeaderText("Failed to get AI response");
    alert.setContentText(error.getMessage());
    alert.showAndWait();

    log.error("AI response error", error);
  }

  private void scrollToBottom() {
    if (chatScroll != null) {
      Platform.runLater(() -> chatScroll.setVvalue(1.0));
    }
  }

  private void rebuildUI() {
    VBox mainContent = (VBox) getChildren().get(getChildren().size() - 1);
    mainContent.getChildren().clear();

    HBox mainLayout = createMainLayout();
    VBox.setVgrow(mainLayout, Priority.ALWAYS);

    mainContent.getChildren().add(mainLayout);
  }

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
    if (chatSessionsList != null && chatSessionsList.getChildren().isEmpty()) {
      loadConversations();
    }

    if (chatInput != null) {
      chatInput.requestFocus();
    }
  }
}
