package com.noteflix.pcm.ui.pages;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * AI Assistant page - ChatGPT-like interface with sidebar chat history
 * Single Responsibility Principle - Handles only AI chat functionality
 */
@Slf4j
public class AIAssistantPage extends BasePage {
    
    // Chat data models
    private List<ChatSession> chatSessions = new ArrayList<>();
    private ChatSession currentSession;
    private VBox chatSessionsList;
    private VBox chatMessagesArea;
    private TextArea chatInput;
    private ScrollPane chatScroll;
    private VBox mainChatContentArea; // Store reference to rebuild layout
    private VBox loadingIndicator;
    
    public AIAssistantPage() {
        super(
            "AI Assistant", 
            "Chat with AI to analyze your system, review code, and get intelligent insights",
            new FontIcon(Feather.MESSAGE_CIRCLE)
        );
        initializeChatData();
        
        // Override default padding for full-screen chat
        setPadding(new Insets(0));
        setSpacing(0);
        getStyleClass().add("ai-chat-page");
    }
    
    private void initializeChatData() {
        chatSessions.clear();
        
        // Create sample chat sessions
        ChatSession session1 = new ChatSession("Database Analysis", "How can I optimize my database queries?");
        session1.addMessage(new ChatMessage("You", "How can I optimize my database queries?", false));
        session1.addMessage(new ChatMessage("AI Assistant", "I can help you optimize your database queries. Let me analyze your current schema and suggest improvements for indexing, query structure, and performance.", true));
        
        ChatSession session2 = new ChatSession("Code Review", "Please review my authentication logic");
        session2.addMessage(new ChatMessage("You", "Please review my authentication logic", false));
        session2.addMessage(new ChatMessage("AI Assistant", "I'll review your authentication implementation. I notice a few areas for improvement regarding security best practices and error handling.", true));
        
        ChatSession session3 = new ChatSession("Performance Issues", "My application is running slowly");
        
        chatSessions.add(session1);
        chatSessions.add(session2);
        chatSessions.add(session3);
        
        // Start with the first session
        currentSession = session1;
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
    
    // Override to customize the AI Chat layout (no standard page header)
    @Override
    protected VBox createPageHeader() {
        // AI Chat doesn't need the standard page header - it has its own chat header
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
    
    private VBox createContentArea() {
        VBox contentArea = new VBox();
        contentArea.getStyleClass().add("content-area");
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        
        // Top: Header
        HBox chatHeader = createChatHeader();
        
        // Bottom: Messages area + Input area (grows to fill remaining space)
        VBox messagesAndInputArea = createMessagesAndInputArea();
        VBox.setVgrow(messagesAndInputArea, Priority.ALWAYS);
        
        contentArea.getChildren().addAll(chatHeader, messagesAndInputArea);
        return contentArea;
    }
    
    private VBox createMessagesAndInputArea() {
        VBox container = new VBox();
        container.getStyleClass().add("messages-and-input-area");
        VBox.setVgrow(container, Priority.ALWAYS);
        
        // Messages area (grows to fill remaining space)
        VBox messagesArea = createMessagesArea();
        VBox.setVgrow(messagesArea, Priority.ALWAYS);
        
        // Input area (fixed height at bottom)
        VBox inputArea = createChatInputArea();
        inputArea.getStyleClass().add("fixed-bottom-input");
        
        container.getChildren().addAll(messagesArea, inputArea);
        return container;
    }
    
    private VBox createMessagesArea() {
        VBox messagesArea = new VBox();
        messagesArea.getStyleClass().add("messages-area");
        VBox.setVgrow(messagesArea, Priority.ALWAYS);
        
        // Build content based on current state
        buildMessagesContent(messagesArea);
        
        return messagesArea;
    }
    
    private void buildMessagesContent(VBox messagesArea) {
        // Clear existing content
        messagesArea.getChildren().clear();
        
        if (currentSession == null || currentSession.getMessages().isEmpty()) {
            // Welcome state: center the welcome content
            VBox welcomeContent = createWelcomeContentCentered();
            messagesArea.getChildren().add(welcomeContent);
            messagesArea.setAlignment(Pos.CENTER);
            
            // Reset chatMessagesArea to null for welcome state
            chatMessagesArea = null;
        } else {
            // Chat state: messages from top to bottom
            messagesArea.setAlignment(Pos.TOP_LEFT);
            
            // Messages container - align from top
            VBox messagesContainer = new VBox();
            messagesContainer.setAlignment(Pos.TOP_CENTER);
            messagesContainer.setPadding(new Insets(20));
            
            chatMessagesArea = new VBox(16);
            chatMessagesArea.getStyleClass().add("chat-messages-area");
            chatMessagesArea.setMaxWidth(768); // Keep messages readable width
            chatMessagesArea.setAlignment(Pos.TOP_LEFT); // Messages from top
            
            messagesContainer.getChildren().add(chatMessagesArea);
            
            chatScroll = new ScrollPane(messagesContainer);
            chatScroll.setFitToWidth(true);
            chatScroll.getStyleClass().add("chat-scroll");
            chatScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            chatScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            VBox.setVgrow(chatScroll, Priority.ALWAYS);
            
            messagesArea.getChildren().add(chatScroll);
            
            // Load current session messages if they exist
            if (currentSession != null && !currentSession.getMessages().isEmpty()) {
                for (ChatMessage message : currentSession.getMessages()) {
                    VBox messageBox = createMessageBox(message);
                    chatMessagesArea.getChildren().add(messageBox);
                }
                scrollToBottom();
            }
        }
    }
    
    private VBox createWelcomeContentCentered() {
        VBox welcomeContent = new VBox(40);
        welcomeContent.setAlignment(Pos.CENTER);
        welcomeContent.getStyleClass().add("welcome-content-centered");
        welcomeContent.setPadding(new Insets(60, 40, 60, 40));
        
        // Welcome message
        VBox welcome = showWelcomeMessage();
        
        // Quick suggestions
        HBox suggestions = createQuickSuggestions();
        
        welcomeContent.getChildren().addAll(welcome, suggestions);
        
        return welcomeContent;
    }
    
    private HBox createChatContentLayout() {
        HBox layout = new HBox();
        layout.getStyleClass().add("chat-content-layout");
        layout.setSpacing(0);
        
        // Left sidebar with chat history
        VBox sidebar = createChatSidebar();
        sidebar.setPrefWidth(280);
        sidebar.setMinWidth(280);
        sidebar.setMaxWidth(280);
        
        // Main chat area (centered with max width)
        VBox mainChatArea = createMainChatArea();
        HBox.setHgrow(mainChatArea, Priority.ALWAYS);
        
        layout.getChildren().addAll(sidebar, mainChatArea);
        return layout;
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
        
        // Chat sessions list
        Label historyLabel = new Label("Chat History");
        historyLabel.getStyleClass().addAll(Styles.TEXT_BOLD, "sidebar-section-title");
        
        chatSessionsList = new VBox(8);
        chatSessionsList.getStyleClass().add("chat-sessions-list");
        
        // Populate chat sessions
        updateChatSessionsList();
        
        ScrollPane sessionsScroll = new ScrollPane(chatSessionsList);
        sessionsScroll.setFitToWidth(true);
        sessionsScroll.getStyleClass().add("sessions-scroll");
        VBox.setVgrow(sessionsScroll, Priority.ALWAYS);
        
        sidebar.getChildren().addAll(sidebarHeader, searchBox, historyLabel, sessionsScroll);
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
        newChatBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, "new-chat-btn");
        newChatBtn.setTooltip(new Tooltip("New Chat"));
        newChatBtn.setOnAction(e -> createNewChat());
        
        header.getChildren().addAll(title, spacer, newChatBtn);
        return header;
    }
    
    private void updateChatSessionsList() {
        chatSessionsList.getChildren().clear();
        
        // Initialize data if not yet done
        if (chatSessions == null) {
            chatSessions = new ArrayList<>();
        }
        
        for (ChatSession session : chatSessions) {
            Button sessionBtn = createChatSessionButton(session);
            chatSessionsList.getChildren().add(sessionBtn);
        }
    }
    
    private Button createChatSessionButton(ChatSession session) {
        HBox content = new HBox(12);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(12));
        
        // Text content (no bot icon)
        VBox textContent = new VBox(4);
        textContent.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContent, Priority.ALWAYS);
        
        Label titleLabel = new Label(session.getTitle());
        titleLabel.getStyleClass().addAll(Styles.TEXT_BOLD, "session-title");
        titleLabel.setMaxWidth(220);
        
        Label previewLabel = new Label(session.getPreview());
        previewLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted", "session-preview");
        previewLabel.setWrapText(false);
        previewLabel.setMaxWidth(220);
        
        // Truncate preview text if too long
        String preview = session.getPreview();
        if (preview.length() > 40) {
            preview = preview.substring(0, 37) + "...";
        }
        previewLabel.setText(preview);
        
        textContent.getChildren().addAll(titleLabel, previewLabel);
        
        // Time and status
        VBox rightContent = new VBox(4);
        rightContent.setAlignment(Pos.TOP_RIGHT);
        
        Label timeLabel = new Label(session.getLastMessageTime().format(DateTimeFormatter.ofPattern("MMM dd")));
        timeLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted", "session-time");
        
        // Message count indicator (optional)
        if (!session.getMessages().isEmpty()) {
            Label countLabel = new Label(String.valueOf(session.getMessages().size()));
            countLabel.getStyleClass().add("message-count-badge");
        }
        
        rightContent.getChildren().add(timeLabel);
        
        content.getChildren().addAll(textContent, rightContent);
        
        Button sessionBtn = new Button();
        sessionBtn.setGraphic(content);
        sessionBtn.getStyleClass().add("chat-session-btn");
        sessionBtn.setMaxWidth(Double.MAX_VALUE);
        sessionBtn.setAlignment(Pos.CENTER_LEFT);
        
        // Highlight current session
        if (session == currentSession) {
            sessionBtn.getStyleClass().add("active");
        }
        
        sessionBtn.setOnAction(e -> switchToSession(session));
        
        return sessionBtn;
    }
    
    private VBox createMainChatArea() {
        mainChatContentArea = new VBox();
        mainChatContentArea.getStyleClass().add("main-chat-area");
        
        // Set width and style based on whether we have messages
        if (currentSession == null || currentSession.getMessages().isEmpty()) {
            // Welcome state: centered with max width
            mainChatContentArea.setMaxWidth(768);
            mainChatContentArea.setAlignment(Pos.CENTER);
            mainChatContentArea.getStyleClass().remove("chat-state");
            if (!mainChatContentArea.getStyleClass().contains("welcome-state")) {
                mainChatContentArea.getStyleClass().add("welcome-state");
            }
        } else {
            // Chat state: full width
            mainChatContentArea.setMaxWidth(Double.MAX_VALUE);
            mainChatContentArea.setAlignment(Pos.TOP_LEFT);
            mainChatContentArea.getStyleClass().remove("welcome-state");
            if (!mainChatContentArea.getStyleClass().contains("chat-state")) {
                mainChatContentArea.getStyleClass().add("chat-state");
            }
        }
        
        VBox.setVgrow(mainChatContentArea, Priority.ALWAYS);
        
        // Build initial content based on current state (without input area)
        buildChatContentWithoutInput();
        
        // Wrap to center the main area horizontally in welcome state only
        VBox wrapper = new VBox();
        wrapper.getStyleClass().add("chat-area-wrapper");
        VBox.setVgrow(wrapper, Priority.ALWAYS);
        
        if (currentSession == null || currentSession.getMessages().isEmpty()) {
            // Welcome state: center the main area
            wrapper.setAlignment(Pos.CENTER);
            HBox centeringBox = new HBox(mainChatContentArea);
            centeringBox.setAlignment(Pos.CENTER);
            HBox.setHgrow(centeringBox, Priority.ALWAYS);
            wrapper.getChildren().add(centeringBox);
        } else {
            // Chat state: full width
            wrapper.setAlignment(Pos.TOP_LEFT);
            HBox.setHgrow(mainChatContentArea, Priority.ALWAYS);
            wrapper.getChildren().add(mainChatContentArea);
        }
        
        return wrapper;
    }
    
    private void buildChatContent() {
        buildChatContentWithoutInput();
    }
    
    private void buildChatContentWithoutInput() {
        // Clear existing content
        mainChatContentArea.getChildren().clear();
        
        // Check if there are messages to determine layout
        if (currentSession == null || currentSession.getMessages().isEmpty()) {
            // Welcome state: centered welcome message and suggestions only
            mainChatContentArea.setMaxWidth(768);
            mainChatContentArea.setAlignment(Pos.CENTER);
            mainChatContentArea.getStyleClass().remove("chat-state");
            if (!mainChatContentArea.getStyleClass().contains("welcome-state")) {
                mainChatContentArea.getStyleClass().add("welcome-state");
            }
            
            VBox welcomeContent = createWelcomeContentWithoutInput();
            VBox.setVgrow(welcomeContent, Priority.ALWAYS);
            mainChatContentArea.getChildren().add(welcomeContent);
            
            // Reset chatMessagesArea to null for welcome state
            chatMessagesArea = null;
        } else {
            // Chat state: messages scroll area only (no input)
            mainChatContentArea.setMaxWidth(Double.MAX_VALUE);
            mainChatContentArea.setAlignment(Pos.TOP_LEFT);
            mainChatContentArea.getStyleClass().remove("welcome-state");
            if (!mainChatContentArea.getStyleClass().contains("chat-state")) {
                mainChatContentArea.getStyleClass().add("chat-state");
            }
            
            createChatStateContentWithoutInput(mainChatContentArea);
        }
    }
    
    private VBox createWelcomeContent() {
        VBox welcomeContent = new VBox();
        welcomeContent.setAlignment(Pos.TOP_CENTER);
        welcomeContent.getStyleClass().add("welcome-content");
        VBox.setVgrow(welcomeContent, Priority.ALWAYS);
        
        // Scrollable welcome area
        VBox scrollableWelcome = new VBox(40);
        scrollableWelcome.setAlignment(Pos.CENTER);
        scrollableWelcome.setPadding(new Insets(60, 40, 20, 40));
        
        // Welcome message
        VBox welcome = showWelcomeMessage();
        
        // Quick suggestions
        HBox suggestions = createQuickSuggestions();
        
        scrollableWelcome.getChildren().addAll(welcome, suggestions);
        
        ScrollPane welcomeScroll = new ScrollPane(scrollableWelcome);
        welcomeScroll.setFitToWidth(true);
        welcomeScroll.getStyleClass().add("welcome-scroll");
        welcomeScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        welcomeScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(welcomeScroll, Priority.ALWAYS);
        
        // Fixed input area at bottom
        VBox inputArea = createChatInputArea();
        inputArea.getStyleClass().add("fixed-input-area");
        
        welcomeContent.getChildren().addAll(welcomeScroll, inputArea);
        
        return welcomeContent;
    }
    
    private VBox createWelcomeContentWithoutInput() {
        VBox welcomeContent = new VBox(40);
        welcomeContent.setAlignment(Pos.CENTER);
        welcomeContent.getStyleClass().add("welcome-content");
        welcomeContent.setPadding(new Insets(60, 40, 60, 40));
        VBox.setVgrow(welcomeContent, Priority.ALWAYS);
        
        // Welcome message
        VBox welcome = showWelcomeMessage();
        
        // Quick suggestions
        HBox suggestions = createQuickSuggestions();
        
        welcomeContent.getChildren().addAll(welcome, suggestions);
        
        return welcomeContent;
    }
    
    private void createChatStateContent(VBox mainArea) {
        // Messages area - constrain to max width for readability but allow horizontal scrolling if needed
        VBox messagesContainer = new VBox();
        messagesContainer.setAlignment(Pos.CENTER);
        messagesContainer.setPadding(new Insets(20));
        
        chatMessagesArea = new VBox(16);
        chatMessagesArea.getStyleClass().add("chat-messages-area");
        chatMessagesArea.setMaxWidth(768); // Keep messages readable width
        chatMessagesArea.setAlignment(Pos.CENTER);
        
        messagesContainer.getChildren().add(chatMessagesArea);
        
        chatScroll = new ScrollPane(messagesContainer);
        chatScroll.setFitToWidth(true);
        chatScroll.getStyleClass().add("chat-scroll");
        chatScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        chatScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(chatScroll, Priority.ALWAYS);
        
        // Input area at bottom (fixed) - full width
        VBox inputArea = createChatInputArea();
        inputArea.getStyleClass().add("fixed-input-area");
        
        mainArea.getChildren().addAll(chatScroll, inputArea);
        
        // Load current session messages if they exist
        if (currentSession != null && !currentSession.getMessages().isEmpty()) {
            for (ChatMessage message : currentSession.getMessages()) {
                VBox messageBox = createMessageBox(message);
                chatMessagesArea.getChildren().add(messageBox);
            }
            scrollToBottom();
        }
    }
    
    private void createChatStateContentWithoutInput(VBox mainArea) {
        // Messages area - constrain to max width for readability but allow horizontal scrolling if needed
        VBox messagesContainer = new VBox();
        messagesContainer.setAlignment(Pos.CENTER);
        messagesContainer.setPadding(new Insets(20));
        
        chatMessagesArea = new VBox(16);
        chatMessagesArea.getStyleClass().add("chat-messages-area");
        chatMessagesArea.setMaxWidth(768); // Keep messages readable width
        chatMessagesArea.setAlignment(Pos.CENTER);
        
        messagesContainer.getChildren().add(chatMessagesArea);
        
        chatScroll = new ScrollPane(messagesContainer);
        chatScroll.setFitToWidth(true);
        chatScroll.getStyleClass().add("chat-scroll");
        chatScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        chatScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(chatScroll, Priority.ALWAYS);
        
        mainArea.getChildren().add(chatScroll);
        
        // Load current session messages if they exist
        if (currentSession != null && !currentSession.getMessages().isEmpty()) {
            for (ChatMessage message : currentSession.getMessages()) {
                VBox messageBox = createMessageBox(message);
                chatMessagesArea.getChildren().add(messageBox);
            }
            scrollToBottom();
        }
    }
    
    private HBox createChatHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("chat-header");
        header.setPadding(new Insets(16, 20, 16, 20));
        
        FontIcon chatIcon = new FontIcon(Feather.MESSAGE_CIRCLE);
        chatIcon.setIconSize(20);
        
        VBox titleBox = new VBox(2);
        Label titleLabel = new Label(currentSession != null ? currentSession.getTitle() : "New Chat");
        titleLabel.getStyleClass().addAll(Styles.TITLE_4);
        
        Label subtitleLabel = new Label("AI-powered system analysis and assistance");
        subtitleLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");
        
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Chat actions
        Button clearBtn = new Button();
        clearBtn.setGraphic(new FontIcon(Feather.TRASH_2));
        clearBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, "chat-action-btn");
        clearBtn.setTooltip(new Tooltip("Clear Chat"));
        clearBtn.setOnAction(e -> clearCurrentChat());
        
        Button settingsBtn = new Button();
        settingsBtn.setGraphic(new FontIcon(Feather.SETTINGS));
        settingsBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, "chat-action-btn");
        settingsBtn.setTooltip(new Tooltip("Chat Settings"));
        
        HBox actions = new HBox(8, clearBtn, settingsBtn);
        
        header.getChildren().addAll(chatIcon, titleBox, spacer, actions);
        return header;
    }
    
    private VBox createChatInputArea() {
        VBox inputArea = new VBox(12);
        inputArea.getStyleClass().add("chat-input-container");
        inputArea.setPadding(new Insets(20));
        inputArea.setAlignment(Pos.CENTER);
        
        // Container for input with max width constraint
        VBox inputContainer = new VBox();
        inputContainer.setAlignment(Pos.CENTER);
        
        // Main input box - always constrained to 768px max width
        HBox inputBox = new HBox(12);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.getStyleClass().add("chat-input-box");
        inputBox.setMaxWidth(768); // Always 768px max width like messages
        
        VBox inputWrapper = new VBox();
        inputWrapper.getStyleClass().add("input-wrapper");
        
        chatInput = new TextArea();
        chatInput.setPromptText("Ask me anything about your system...");
        chatInput.getStyleClass().add("chat-input");
        chatInput.setPrefRowCount(1);
        chatInput.setWrapText(true);
        
        // Auto-resize text area
        chatInput.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.contains("\n")) {
                int lines = newText.split("\n").length;
                chatInput.setPrefRowCount(Math.min(lines, 8));
            }
        });
        
        // Input actions
        HBox inputActions = new HBox(8);
        inputActions.setAlignment(Pos.CENTER_RIGHT);
        inputActions.setPadding(new Insets(8));
        
        Button attachBtn = new Button();
        attachBtn.setGraphic(new FontIcon(Feather.PAPERCLIP));
        attachBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, "input-action-btn");
        attachBtn.setTooltip(new Tooltip("Attach File"));
        
        Button sendBtn = new Button();
        sendBtn.setGraphic(new FontIcon(Feather.SEND));
        sendBtn.getStyleClass().addAll(Styles.BUTTON_ICON, "send-btn");
        sendBtn.setOnAction(e -> handleSendMessage());
        
        inputActions.getChildren().addAll(attachBtn, sendBtn);
        
        inputWrapper.getChildren().addAll(chatInput, inputActions);
        HBox.setHgrow(inputWrapper, Priority.ALWAYS);
        
        inputBox.getChildren().add(inputWrapper);
        inputContainer.getChildren().add(inputBox);
        inputArea.getChildren().add(inputContainer);
        
        // Handle Enter key
        chatInput.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER") && !e.isShiftDown()) {
                e.consume();
                handleSendMessage();
            }
        });
        
        return inputArea;
    }
    
    private HBox createQuickSuggestions() {
        HBox suggestions = new HBox(12);
        suggestions.setAlignment(Pos.CENTER);
        suggestions.getStyleClass().add("quick-suggestions");
        
        String[] suggestionTexts = {
            "Analyze my database schema",
            "Review code quality"
        };
        
        for (String text : suggestionTexts) {
            Button suggestionBtn = new Button(text);
            suggestionBtn.getStyleClass().add("suggestion-btn");
            suggestionBtn.setOnAction(e -> {
                chatInput.setText(text);
                handleSendMessage();
            });
            suggestions.getChildren().add(suggestionBtn);
        }
        
        return suggestions;
    }
    
    
    private VBox showWelcomeMessage() {
        VBox welcome = new VBox(16);
        welcome.setAlignment(Pos.CENTER);
        welcome.getStyleClass().add("welcome-message");
        
        // Use bot icon instead of message circle
        com.noteflix.pcm.core.theme.ThemeManager themeManager = com.noteflix.pcm.core.theme.ThemeManager.getInstance();
        javafx.scene.image.ImageView botIcon = com.noteflix.pcm.core.utils.IconUtils.createImageView(
            themeManager.getBotIcon(), 64, 64
        );
        botIcon.getStyleClass().add("welcome-bot-icon");
        
        Label title = new Label("AI Assistant");
        title.getStyleClass().addAll(Styles.TITLE_2, "welcome-title");
        
        Label subtitle = new Label("How can I help you today?");
        subtitle.getStyleClass().addAll(Styles.TEXT_MUTED, "welcome-subtitle");
        
        welcome.getChildren().addAll(botIcon, title, subtitle);
        return welcome;
    }
    
    private VBox createMessageBox(ChatMessage message) {
        VBox messageBox = new VBox(8);
        messageBox.getStyleClass().add("message-box");
        
        HBox messageRow = new HBox(12);
        messageRow.setAlignment(message.isFromAI() ? Pos.TOP_LEFT : Pos.TOP_RIGHT);
        
        // Create message icon
        javafx.scene.Node messageIcon = createMessageIcon(message.isFromAI());
        
        // Message bubble
        VBox bubble = new VBox(4);
        bubble.getStyleClass().add(message.isFromAI() ? "ai-message-bubble" : "user-message-bubble");
        bubble.setPadding(new Insets(12, 16, 12, 16));
        bubble.setMaxWidth(600);
        
        Label messageLabel = new Label(message.getContent());
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("message-text");
        
        Label timeLabel = new Label(message.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted", "message-time");
        
        bubble.getChildren().addAll(messageLabel, timeLabel);
        
        if (message.isFromAI()) {
            messageBox.getStyleClass().add("ai-message-box");
            messageRow.getChildren().addAll(messageIcon, bubble);
        } else {
            messageBox.getStyleClass().add("user-message-box");
            messageRow.getChildren().addAll(bubble, messageIcon);
        }
        
        messageBox.getChildren().add(messageRow);
        return messageBox;
    }
    
    private javafx.scene.Node createMessageIcon(boolean isFromAI) {
        if (isFromAI) {
            // Use bot icon for AI messages
            com.noteflix.pcm.core.theme.ThemeManager themeManager = com.noteflix.pcm.core.theme.ThemeManager.getInstance();
            javafx.scene.image.ImageView botIcon = com.noteflix.pcm.core.utils.IconUtils.createImageView(
                themeManager.getBotIcon(), 32, 32
            );
            botIcon.getStyleClass().add("message-bot-icon");
            return botIcon;
        } else {
            // Use user icon for user messages
            FontIcon userIcon = new FontIcon(Feather.USER);
            userIcon.setIconSize(24);
            userIcon.getStyleClass().add("message-user-icon");
            
            // Wrap in a circular background
            StackPane iconWrapper = new StackPane(userIcon);
            iconWrapper.getStyleClass().add("user-icon-wrapper");
            iconWrapper.setMinSize(32, 32);
            iconWrapper.setMaxSize(32, 32);
            
            return iconWrapper;
        }
    }
    
    private void handleSendMessage() {
        String message = chatInput.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        
        // Create new session if none exists
        if (currentSession == null) {
            currentSession = new ChatSession("New Chat", message);
            chatSessions.add(0, currentSession); // Add to top of list
            updateChatSessionsList();
        }
        
        // Add user message
        ChatMessage userMessage = new ChatMessage("You", message, false);
        currentSession.addMessage(userMessage);
        
        // Clear input and disable it
        chatInput.clear();
        chatInput.setPrefRowCount(1);
        chatInput.setDisable(true);
        
        // Check if we need to rebuild the layout (from welcome state to chat state)
        if (chatMessagesArea == null) {
            rebuildChatLayout();
        }
        
        // Add message to UI
        if (chatMessagesArea != null) {
            VBox messageBox = createMessageBox(userMessage);
            chatMessagesArea.getChildren().add(messageBox);
        }
        
        // Add loading indicator
        showLoadingIndicator();
        scrollToBottom();
        
        // Simulate AI response
        simulateAIResponse(message);
    }
    
    private void rebuildChatLayout() {
        // Rebuild the entire main content with new layout
        rebuildMainContent();
    }
    
    private void showLoadingIndicator() {
        if (chatMessagesArea != null) {
            loadingIndicator = createLoadingIndicator();
            chatMessagesArea.getChildren().add(loadingIndicator);
        }
    }
    
    private void hideLoadingIndicator() {
        if (chatMessagesArea != null && loadingIndicator != null) {
            // Stop the animation
            javafx.animation.Timeline timeline = (javafx.animation.Timeline) loadingIndicator.getUserData();
            if (timeline != null) {
                timeline.stop();
            }
            
            chatMessagesArea.getChildren().remove(loadingIndicator);
            loadingIndicator = null;
        }
    }
    
    private VBox createLoadingIndicator() {
        VBox indicator = new VBox(8);
        indicator.getStyleClass().add("loading-indicator");
        indicator.setAlignment(Pos.CENTER_LEFT);
        indicator.setPadding(new Insets(16, 20, 16, 20));
        
        HBox messageRow = new HBox(12);
        messageRow.setAlignment(Pos.TOP_LEFT);
        
        // Bot icon
        com.noteflix.pcm.core.theme.ThemeManager themeManager = com.noteflix.pcm.core.theme.ThemeManager.getInstance();
        javafx.scene.image.ImageView botIcon = com.noteflix.pcm.core.utils.IconUtils.createImageView(
            themeManager.getBotIcon(), 32, 32
        );
        botIcon.getStyleClass().add("message-bot-icon");
        
        // Loading bubble with animated dots
        VBox bubble = new VBox(4);
        bubble.getStyleClass().add("loading-bubble");
        bubble.setPadding(new Insets(12, 16, 12, 16));
        bubble.setMaxWidth(600);
        
        // Animated loading text
        Label loadingText = new Label("AI is thinking");
        loadingText.getStyleClass().addAll("loading-text");
        
        // Three animated dots
        HBox dotsBox = new HBox(4);
        dotsBox.setAlignment(Pos.CENTER_LEFT);
        
        Label dot1 = new Label("•");
        Label dot2 = new Label("•");
        Label dot3 = new Label("•");
        
        dot1.getStyleClass().add("loading-dot");
        dot2.getStyleClass().add("loading-dot");
        dot3.getStyleClass().add("loading-dot");
        
        dotsBox.getChildren().addAll(dot1, dot2, dot3);
        
        // Create simple animation using opacity changes
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        
        // Dot 1 animation
        javafx.animation.KeyFrame kf1a = new javafx.animation.KeyFrame(
            javafx.util.Duration.millis(0), 
            new javafx.animation.KeyValue(dot1.opacityProperty(), 1.0)
        );
        javafx.animation.KeyFrame kf1b = new javafx.animation.KeyFrame(
            javafx.util.Duration.millis(300), 
            new javafx.animation.KeyValue(dot1.opacityProperty(), 0.3)
        );
        
        // Dot 2 animation (delayed)
        javafx.animation.KeyFrame kf2a = new javafx.animation.KeyFrame(
            javafx.util.Duration.millis(300), 
            new javafx.animation.KeyValue(dot2.opacityProperty(), 1.0)
        );
        javafx.animation.KeyFrame kf2b = new javafx.animation.KeyFrame(
            javafx.util.Duration.millis(600), 
            new javafx.animation.KeyValue(dot2.opacityProperty(), 0.3)
        );
        
        // Dot 3 animation (more delayed)
        javafx.animation.KeyFrame kf3a = new javafx.animation.KeyFrame(
            javafx.util.Duration.millis(600), 
            new javafx.animation.KeyValue(dot3.opacityProperty(), 1.0)
        );
        javafx.animation.KeyFrame kf3b = new javafx.animation.KeyFrame(
            javafx.util.Duration.millis(900), 
            new javafx.animation.KeyValue(dot3.opacityProperty(), 0.3)
        );
        
        timeline.getKeyFrames().addAll(kf1a, kf1b, kf2a, kf2b, kf3a, kf3b);
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
        
        // Store animation reference to stop it later
        indicator.setUserData(timeline);
        
        HBox textAndDots = new HBox(8);
        textAndDots.setAlignment(Pos.CENTER_LEFT);
        textAndDots.getChildren().addAll(loadingText, dotsBox);
        
        bubble.getChildren().add(textAndDots);
        messageRow.getChildren().addAll(botIcon, bubble);
        indicator.getChildren().add(messageRow);
        
        return indicator;
    }
    
    private void simulateAIResponse(String userMessage) {
        new Thread(() -> {
            try {
                Thread.sleep(1500); // Simulate processing time
                
                String response = generateAIResponse(userMessage);
                ChatMessage aiMessage = new ChatMessage("AI Assistant", response, true);
                
                javafx.application.Platform.runLater(() -> {
                    // Remove loading indicator
                    hideLoadingIndicator();
                    
                    currentSession.addMessage(aiMessage);
                    VBox messageBox = createMessageBox(aiMessage);
                    chatMessagesArea.getChildren().add(messageBox);
                    scrollToBottom();
                    
                    // Re-enable input
                    chatInput.setDisable(false);
                    chatInput.requestFocus();
                    
                    // Update session preview
                    currentSession.setPreview(response.length() > 50 ? response.substring(0, 47) + "..." : response);
                    updateChatSessionsList();
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    private String generateAIResponse(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();
        
        if (lowerMessage.contains("database") || lowerMessage.contains("query")) {
            return "I can help you optimize your database. Let me analyze your schema and query patterns. I recommend creating indexes on frequently queried columns and reviewing your join operations for efficiency.";
        } else if (lowerMessage.contains("code") || lowerMessage.contains("review")) {
            return "I'll review your code for best practices, security vulnerabilities, and performance improvements. Please share the specific files or components you'd like me to analyze.";
        } else if (lowerMessage.contains("performance") || lowerMessage.contains("slow")) {
            return "Performance analysis is one of my specialties. I can help identify bottlenecks in your application, database queries, and system resources. What specific performance issues are you experiencing?";
        } else if (lowerMessage.contains("security") || lowerMessage.contains("secure")) {
            return "Security is crucial for any application. I can perform a comprehensive security assessment, checking for common vulnerabilities like SQL injection, XSS, authentication issues, and data exposure risks.";
        } else {
            return "Thank you for your question. I'm here to help with system analysis, code review, performance optimization, security assessments, and much more. Could you provide more details about what you'd like assistance with?";
        }
    }
    
    private void scrollToBottom() {
        javafx.application.Platform.runLater(() -> {
            chatScroll.setVvalue(1.0);
        });
    }
    
    private void switchToSession(ChatSession session) {
        if (currentSession != session) {
            currentSession = session;
            
            // Update active state and rebuild entire layout
            updateChatSessionsList();
            rebuildMainContent();
        }
    }
    
    private void rebuildMainContent() {
        // Get the main content VBox and rebuild it
        VBox mainContent = (VBox) getChildren().get(getChildren().size() - 1);
        mainContent.getChildren().clear();
        
        // Use the new 2-column layout
        HBox mainLayout = createMainLayout();
        VBox.setVgrow(mainLayout, Priority.ALWAYS);
        
        mainContent.getChildren().add(mainLayout);
    }
    
    private void createNewChat() {
        currentSession = null;
        
        // Rebuild entire layout for welcome state
        updateChatSessionsList();
        rebuildMainContent();
        
        // Focus input
        if (chatInput != null) {
            chatInput.requestFocus();
        }
    }
    
    private void clearCurrentChat() {
        if (currentSession != null) {
            currentSession.getMessages().clear();
            
            // Rebuild entire layout since messages are now empty
            rebuildMainContent();
        }
    }
    
    @Override
    public void onPageActivated() {
        super.onPageActivated();
        if (chatInput != null) {
            chatInput.requestFocus();
        }
    }
    
    // Data models
    public static class ChatSession {
        private String title;
        private String preview;
        private LocalDateTime lastMessageTime;
        private List<ChatMessage> messages;
        
        public ChatSession(String title, String preview) {
            this.title = title;
            this.preview = preview;
            this.lastMessageTime = LocalDateTime.now();
            this.messages = new ArrayList<>();
        }
        
        public void addMessage(ChatMessage message) {
            messages.add(message);
            lastMessageTime = LocalDateTime.now();
        }
        
        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getPreview() { return preview; }
        public void setPreview(String preview) { this.preview = preview; }
        public LocalDateTime getLastMessageTime() { return lastMessageTime; }
        public List<ChatMessage> getMessages() { return messages; }
    }
    
    public static class ChatMessage {
        private String sender;
        private String content;
        private boolean fromAI;
        private LocalDateTime timestamp;
        
        public ChatMessage(String sender, String content, boolean fromAI) {
            this.sender = sender;
            this.content = content;
            this.fromAI = fromAI;
            this.timestamp = LocalDateTime.now();
        }
        
        // Getters
        public String getSender() { return sender; }
        public String getContent() { return content; }
        public boolean isFromAI() { return fromAI; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}