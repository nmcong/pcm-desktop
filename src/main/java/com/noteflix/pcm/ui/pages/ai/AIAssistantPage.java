package com.noteflix.pcm.ui.pages.ai;

import com.noteflix.pcm.application.service.chat.AIService;
import com.noteflix.pcm.application.service.chat.ConversationService;
import com.noteflix.pcm.domain.chat.Conversation;
import com.noteflix.pcm.domain.chat.Message;
import com.noteflix.pcm.llm.model.LLMChunk;
import com.noteflix.pcm.llm.model.StreamingObserver;
import com.noteflix.pcm.ui.base.BaseView;
import com.noteflix.pcm.ui.pages.ai.components.*;
import com.noteflix.pcm.ui.pages.ai.handlers.ChatEventHandler;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.Optional;

/**
 * AI Assistant Page - Clean Architecture Implementation (Refactored)
 *
 * <p>Follows SOLID principles with component-based architecture:
 * - Single Responsibility: Delegates UI responsibilities to specialized components
 * - Dependency Inversion: Depends on service interfaces
 * - Open/Closed: Easy to extend with new features
 *
 * <p>Features:
 * - Real-time AI chat with streaming responses
 * - Persistent conversation history via database
 * - Search and conversation management
 * - Multi-LLM provider support
 *
 * <p>Components:
 * - ChatSidebar: Manages conversation list and search
 * - ChatHeader: Displays conversation title and actions
 * - ChatMessagesArea: Shows message history with scrolling
 * - ChatInputArea: Handles user input
 * - WelcomeScreen: Initial state when no conversation is active
 *
 * @author PCM Team
 * @version 3.0.0 - Refactored with components
 */
@Slf4j
public class AIAssistantPage extends BaseView implements ChatEventHandler {

    // Services (injected via constructor)
    private final ConversationService conversationService;
    private final AIService aiService;
    private final String currentUserId = "default-user"; // TODO: Get from auth service

    // Current state
    private Long currentConversationId;

    // UI Components
    private ChatSidebar sidebar;
    private ChatHeader chatHeader;
    private ChatMessagesArea messagesArea;
    private ChatInputArea inputArea;

    /**
     * Constructor with dependency injection
     */
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
     * Default constructor (creates services internally)
     */
    public AIAssistantPage() {
        this(new ConversationService(), new AIService());
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

        // Left sidebar
        sidebar = new ChatSidebar(this);

        // Right content area
        VBox contentArea = createContentArea();
        HBox.setHgrow(contentArea, Priority.ALWAYS);

        mainLayout.getChildren().addAll(sidebar, contentArea);
        return mainLayout;
    }

    private VBox createContentArea() {
        VBox area = new VBox();
        area.getStyleClass().add("content-area");
        VBox.setVgrow(area, Priority.ALWAYS);

        // Top: Header
        chatHeader = new ChatHeader(this);

        // Middle/Bottom: Messages or Welcome screen + Input
        VBox messagesAndInputContainer = createMessagesAndInputContainer();
        VBox.setVgrow(messagesAndInputContainer, Priority.ALWAYS);

        area.getChildren().addAll(chatHeader, messagesAndInputContainer);
        return area;
    }

    private VBox createMessagesAndInputContainer() {
        VBox container = new VBox();
        container.getStyleClass().add("messages-and-input-area");
        VBox.setVgrow(container, Priority.ALWAYS);

        if (currentConversationId == null) {
            // Welcome state
            WelcomeScreen welcomeScreen = new WelcomeScreen(this);
            VBox.setVgrow(welcomeScreen, Priority.ALWAYS);
            container.getChildren().add(welcomeScreen);
        } else {
            // Chat state
            messagesArea = new ChatMessagesArea();
            VBox.setVgrow(messagesArea, Priority.ALWAYS);

            // Load messages
            loadMessages();

            container.getChildren().add(messagesArea);
        }

        // Input area (always visible)
        inputArea = new ChatInputArea(this);
        container.getChildren().add(inputArea);

        return container;
    }

    // ========== ChatEventHandler Implementation ==========

    @Override
    public void onNewChat() {
        currentConversationId = null;
        rebuildUI();
        if (inputArea != null) {
            inputArea.requestInputFocus();
        }
    }

    @Override
    public void onClearChat() {
        if (currentConversationId != null) {
            conversationService.clearConversation(currentConversationId);
            rebuildUI();
        }
    }

    @Override
    public void onSendMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }

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
                Conversation conv = conversationService.createConversation(
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
        Message userMsg = Message.user(currentConversationId, message);
        messagesArea.addUserMessage(userMsg);

        // Clear and disable input
        inputArea.clear();
        inputArea.setInputEnabled(false);

        // Show loading
        messagesArea.showLoadingIndicator();

        // Get AI response (with streaming)
        aiService.streamResponse(
                conversationService.getConversation(currentConversationId).get(),
                message,
                new StreamingObserver() {
                    private final StringBuilder fullResponse = new StringBuilder();

                    @Override
                    public void onChunk(LLMChunk chunk) {
                        Platform.runLater(() -> {
                            fullResponse.append(chunk.getContent());
                            messagesArea.updateStreamingMessage(fullResponse.toString());
                        });
                    }

                    @Override
                    public void onComplete() {
                        Platform.runLater(() -> {
                            messagesArea.hideLoadingIndicator();
                            messagesArea.finalizeStreamingMessage();
                            inputArea.setInputEnabled(true);
                            inputArea.requestInputFocus();
                            loadConversations(); // Refresh sidebar
                            loadMessages(); // Reload to get persisted message
                        });
                    }

                    @Override
                    public void onError(Throwable error) {
                        Platform.runLater(() -> {
                            messagesArea.hideLoadingIndicator();
                            showError(error);
                            inputArea.setInputEnabled(true);
                        });
                    }
                });
    }

    @Override
    public void onConversationSelected(Long conversationId) {
        if (!conversationId.equals(currentConversationId)) {
            currentConversationId = conversationId;
            rebuildUI();
        }
    }

    @Override
    public void onSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadConversations();
        } else {
            List<Conversation> results = conversationService.searchConversations(currentUserId, query);
            updateSidebar(results);
        }
    }

    // ========== Helper Methods ==========

    private void loadConversations() {
        if (conversationService == null) {
            log.warn("ConversationService is null - cannot load conversations");
            return;
        }

        try {
            List<Conversation> conversations = conversationService.getUserConversations(currentUserId);
            updateSidebar(conversations);
        } catch (Exception e) {
            log.error("Failed to load conversations", e);
            sidebar.showError("Failed to load conversations");
        }
    }

    private void updateSidebar(List<Conversation> conversations) {
        sidebar.setCurrentConversationId(currentConversationId);
        sidebar.updateConversations(conversations);
    }

    private void loadMessages() {
        if (currentConversationId == null || messagesArea == null) {
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
                messagesArea.displayMessages(conv.getMessages());

                // Update header title
                chatHeader.setTitle(conv.getTitle());
            }
        } catch (Exception e) {
            log.error("Failed to load messages", e);
        }
    }

    private void showError(Throwable error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Failed to get AI response");
        alert.setContentText(error.getMessage());
        alert.showAndWait();

        log.error("AI response error", error);
    }

    private void rebuildUI() {
        VBox mainContent = (VBox) getChildren().get(getChildren().size() - 1);
        mainContent.getChildren().clear();

        HBox mainLayout = createMainLayout();
        VBox.setVgrow(mainLayout, Priority.ALWAYS);

        mainContent.getChildren().add(mainLayout);
    }

    @Override
    public void onActivate() {
        super.onActivate();

        // Load conversations on first activation
        if (sidebar != null) {
            loadConversations();
        }

        if (inputArea != null) {
            inputArea.requestInputFocus();
        }
    }
}
