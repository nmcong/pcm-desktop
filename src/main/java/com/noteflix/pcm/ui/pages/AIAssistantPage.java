package com.noteflix.pcm.ui.pages;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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
    
    public AIAssistantPage() {
        super(
            "AI Assistant", 
            "Chat with AI to analyze your system, review code, and get intelligent insights",
            new FontIcon(Feather.MESSAGE_CIRCLE)
        );
        initializeChatData();
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
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        
        // Create ChatGPT-like layout: sidebar + main chat area
        HBox chatLayout = createChatLayout();
        VBox.setVgrow(chatLayout, Priority.ALWAYS);
        
        mainContent.getChildren().add(chatLayout);
        return mainContent;
    }
    
    private HBox createChatLayout() {
        HBox layout = new HBox();
        layout.getStyleClass().add("chat-layout");
        layout.setSpacing(0); // No gap between sidebar and main content
        
        // Left sidebar with chat history
        VBox sidebar = createChatSidebar();
        sidebar.setPrefWidth(280);
        sidebar.setMinWidth(280);
        sidebar.setMaxWidth(280);
        
        // Main chat area
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
        VBox content = new VBox(4);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(12));
        
        Label titleLabel = new Label(session.getTitle());
        titleLabel.getStyleClass().addAll(Styles.TEXT_BOLD, "session-title");
        titleLabel.setMaxWidth(200);
        
        Label previewLabel = new Label(session.getPreview());
        previewLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted", "session-preview");
        previewLabel.setWrapText(true);
        previewLabel.setMaxWidth(200);
        
        Label timeLabel = new Label(session.getLastMessageTime().format(DateTimeFormatter.ofPattern("MMM dd")));
        timeLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted", "session-time");
        
        content.getChildren().addAll(titleLabel, previewLabel, timeLabel);
        
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
        VBox mainArea = new VBox();
        mainArea.getStyleClass().add("main-chat-area");
        
        // Chat header
        HBox chatHeader = createChatHeader();
        
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
        
        // Input area at bottom
        VBox inputArea = createChatInputArea();
        
        mainArea.getChildren().addAll(chatHeader, chatScroll, inputArea);
        
        // Load current session messages
        loadCurrentSessionMessages();
        
        return mainArea;
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
    
    private void loadCurrentSessionMessages() {
        chatMessagesArea.getChildren().clear();
        
        if (currentSession == null) {
            // Show welcome message
            showWelcomeMessage();
            return;
        }
        
        for (ChatMessage message : currentSession.getMessages()) {
            VBox messageBox = createMessageBox(message);
            chatMessagesArea.getChildren().add(messageBox);
        }
        
        // Scroll to bottom
        scrollToBottom();
    }
    
    private void showWelcomeMessage() {
        VBox welcome = new VBox(16);
        welcome.setAlignment(Pos.CENTER);
        welcome.getStyleClass().add("welcome-message");
        welcome.setPadding(new Insets(40));
        
        FontIcon icon = new FontIcon(Feather.MESSAGE_CIRCLE);
        icon.setIconSize(48);
        icon.getStyleClass().add("welcome-icon");
        
        Label title = new Label("AI Assistant");
        title.getStyleClass().addAll(Styles.TITLE_2, "welcome-title");
        
        Label subtitle = new Label("How can I help you today?");
        subtitle.getStyleClass().addAll(Styles.TEXT_MUTED, "welcome-subtitle");
        
        welcome.getChildren().addAll(icon, title, subtitle);
        chatMessagesArea.getChildren().add(welcome);
    }
    
    private VBox createMessageBox(ChatMessage message) {
        VBox messageBox = new VBox(8);
        messageBox.getStyleClass().add("message-box");
        
        if (message.isFromAI()) {
            messageBox.getStyleClass().add("ai-message-box");
            messageBox.setAlignment(Pos.CENTER_LEFT);
        } else {
            messageBox.getStyleClass().add("user-message-box");
            messageBox.setAlignment(Pos.CENTER_RIGHT);
        }
        
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
        messageBox.getChildren().add(bubble);
        
        return messageBox;
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
        
        // Clear input
        chatInput.clear();
        chatInput.setPrefRowCount(1);
        
        // Add message to UI
        VBox messageBox = createMessageBox(userMessage);
        chatMessagesArea.getChildren().add(messageBox);
        scrollToBottom();
        
        // Simulate AI response
        simulateAIResponse(message);
    }
    
    private void simulateAIResponse(String userMessage) {
        new Thread(() -> {
            try {
                Thread.sleep(1500); // Simulate processing time
                
                String response = generateAIResponse(userMessage);
                ChatMessage aiMessage = new ChatMessage("AI Assistant", response, true);
                
                javafx.application.Platform.runLater(() -> {
                    currentSession.addMessage(aiMessage);
                    VBox messageBox = createMessageBox(aiMessage);
                    chatMessagesArea.getChildren().add(messageBox);
                    scrollToBottom();
                    
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
            loadCurrentSessionMessages();
            updateChatSessionsList(); // Update active state
            
            // Update chat header
            if (chatMessagesArea.getParent() != null) {
                VBox mainArea = (VBox) chatMessagesArea.getParent().getParent();
                HBox newHeader = createChatHeader();
                mainArea.getChildren().set(0, newHeader);
            }
        }
    }
    
    private void createNewChat() {
        currentSession = null;
        loadCurrentSessionMessages();
        updateChatSessionsList();
        
        // Update header
        if (chatMessagesArea.getParent() != null) {
            VBox mainArea = (VBox) chatMessagesArea.getParent().getParent();
            HBox newHeader = createChatHeader();
            mainArea.getChildren().set(0, newHeader);
        }
        
        // Focus input
        if (chatInput != null) {
            chatInput.requestFocus();
        }
    }
    
    private void clearCurrentChat() {
        if (currentSession != null) {
            currentSession.getMessages().clear();
            loadCurrentSessionMessages();
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