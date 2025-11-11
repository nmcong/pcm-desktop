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
        
        // AI Chat header always at top
        HBox chatHeader = createChatHeader();
        
        // Chat content layout: sidebar + main chat area
        HBox chatContentLayout = createChatContentLayout();
        VBox.setVgrow(chatContentLayout, Priority.ALWAYS);
        
        mainContent.getChildren().addAll(chatHeader, chatContentLayout);
        return mainContent;
    }
    
    // Override to customize the AI Chat layout (no standard page header)
    @Override
    protected VBox createPageHeader() {
        // AI Chat doesn't need the standard page header - it has its own chat header
        return null;
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
        
        // Bot icon
        com.noteflix.pcm.core.theme.ThemeManager themeManager = com.noteflix.pcm.core.theme.ThemeManager.getInstance();
        javafx.scene.image.ImageView botIcon = com.noteflix.pcm.core.utils.IconUtils.createImageView(
            themeManager.getBotIcon(), 24, 24
        );
        botIcon.getStyleClass().add("session-bot-icon");
        
        // Text content
        VBox textContent = new VBox(4);
        textContent.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContent, Priority.ALWAYS);
        
        Label titleLabel = new Label(session.getTitle());
        titleLabel.getStyleClass().addAll(Styles.TEXT_BOLD, "session-title");
        titleLabel.setMaxWidth(180);
        
        Label previewLabel = new Label(session.getPreview());
        previewLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted", "session-preview");
        previewLabel.setWrapText(false);
        previewLabel.setMaxWidth(180);
        
        // Truncate preview text if too long
        String preview = session.getPreview();
        if (preview.length() > 35) {
            preview = preview.substring(0, 32) + "...";
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
        
        content.getChildren().addAll(botIcon, textContent, rightContent);
        
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
        mainChatContentArea.setMaxWidth(768);
        mainChatContentArea.setAlignment(Pos.CENTER);
        VBox.setVgrow(mainChatContentArea, Priority.ALWAYS);
        
        // Build initial content based on current state
        buildChatContent();
        
        // Wrap to center the main area horizontally
        VBox wrapper = new VBox();
        wrapper.getStyleClass().add("chat-area-wrapper");
        wrapper.setAlignment(Pos.CENTER);
        VBox.setVgrow(wrapper, Priority.ALWAYS);
        
        // Center the main area
        HBox centeringBox = new HBox(mainChatContentArea);
        centeringBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(centeringBox, Priority.ALWAYS);
        
        wrapper.getChildren().add(centeringBox);
        
        return wrapper;
    }
    
    private void buildChatContent() {
        // Clear existing content
        mainChatContentArea.getChildren().clear();
        
        // Check if there are messages to determine layout
        if (currentSession == null || currentSession.getMessages().isEmpty()) {
            // Welcome state: centered welcome message, suggestions, and input
            VBox welcomeContent = createWelcomeContent();
            VBox.setVgrow(welcomeContent, Priority.ALWAYS);
            mainChatContentArea.getChildren().add(welcomeContent);
            
            // Reset chatMessagesArea to null for welcome state
            chatMessagesArea = null;
        } else {
            // Chat state: messages scroll area + fixed input at bottom
            createChatStateContent(mainChatContentArea);
        }
    }
    
    private VBox createWelcomeContent() {
        VBox welcomeContent = new VBox(40);
        welcomeContent.setAlignment(Pos.CENTER);
        welcomeContent.getStyleClass().add("welcome-content");
        welcomeContent.setPadding(new Insets(60, 40, 60, 40));
        VBox.setVgrow(welcomeContent, Priority.ALWAYS);
        
        // Welcome message
        VBox welcome = showWelcomeMessage();
        
        // Quick suggestions
        HBox suggestions = createQuickSuggestions();
        
        // Input area
        VBox inputArea = createChatInputArea();
        
        welcomeContent.getChildren().addAll(welcome, suggestions, inputArea);
        
        return welcomeContent;
    }
    
    private void createChatStateContent(VBox mainArea) {
        // Messages area
        chatMessagesArea = new VBox(16);
        chatMessagesArea.getStyleClass().add("chat-messages-area");
        chatMessagesArea.setPadding(new Insets(20));
        
        chatScroll = new ScrollPane(chatMessagesArea);
        chatScroll.setFitToWidth(true);
        chatScroll.getStyleClass().add("chat-scroll");
        chatScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        chatScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(chatScroll, Priority.ALWAYS);
        
        // Input area at bottom (fixed)
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
        
        // Quick suggestions (like ChatGPT)
        if (currentSession == null || currentSession.getMessages().isEmpty()) {
            HBox suggestions = createQuickSuggestions();
            inputArea.getChildren().add(suggestions);
        }
        
        // Main input box
        HBox inputBox = new HBox(12);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.getStyleClass().add("chat-input-box");
        
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
        inputArea.getChildren().add(inputBox);
        
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
            "Review code quality",
            "Check system performance",
            "Security assessment"
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
        messageRow.setAlignment(message.isFromAI() ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        
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
        VBox messageBox = createMessageBox(userMessage);
        chatMessagesArea.getChildren().add(messageBox);
        
        // Add loading indicator
        showLoadingIndicator();
        scrollToBottom();
        
        // Simulate AI response
        simulateAIResponse(message);
    }
    
    private void rebuildChatLayout() {
        // Rebuild the chat content using the stored reference
        buildChatContent();
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
        messageRow.setAlignment(Pos.CENTER_LEFT);
        
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
            
            // Rebuild the entire chat content to reflect the new session
            buildChatContent();
            updateChatSessionsList(); // Update active state
            
            // Update chat header (rebuild the main content to include updated header)
            rebuildMainContent();
        }
    }
    
    private void rebuildMainContent() {
        // Get the main content VBox and rebuild it
        VBox mainContent = (VBox) getChildren().get(getChildren().size() - 1);
        mainContent.getChildren().clear();
        
        // Add header and chat content
        HBox chatHeader = createChatHeader();
        HBox chatContentLayout = createChatContentLayout();
        VBox.setVgrow(chatContentLayout, Priority.ALWAYS);
        
        mainContent.getChildren().addAll(chatHeader, chatContentLayout);
    }
    
    private void createNewChat() {
        currentSession = null;
        
        // Rebuild content for welcome state
        buildChatContent();
        updateChatSessionsList();
        
        // Rebuild main content to update header
        rebuildMainContent();
        
        // Focus input
        if (chatInput != null) {
            chatInput.requestFocus();
        }
    }
    
    private void clearCurrentChat() {
        if (currentSession != null) {
            currentSession.getMessages().clear();
            
            // Rebuild content since messages are now empty
            buildChatContent();
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