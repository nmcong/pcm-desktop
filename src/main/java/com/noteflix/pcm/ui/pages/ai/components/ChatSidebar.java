package com.noteflix.pcm.ui.pages.ai.components;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.domain.chat.Conversation;
import com.noteflix.pcm.ui.pages.ai.handlers.ChatEventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Sidebar component for chat conversations list
 */
@Slf4j
public class ChatSidebar extends VBox {

    private final ChatEventHandler eventHandler;
    private final VBox chatSessionsList;
    private final TextField searchBox;
    private Long currentConversationId;

    public ChatSidebar(ChatEventHandler eventHandler) {
        this.eventHandler = eventHandler;

        getStyleClass().add("chat-sidebar");
        setPadding(new Insets(16));
        setSpacing(12);
        setPrefWidth(280);
        setMinWidth(280);
        setMaxWidth(280);

        // Sidebar header with new chat button
        HBox sidebarHeader = createSidebarHeader();

        // Search box
        searchBox = new TextField();
        searchBox.setPromptText("Search conversations...");
        searchBox.getStyleClass().add("search-input");
        searchBox.textProperty().addListener((obs, old, newVal) ->
                eventHandler.onSearch(newVal));

        // Chat sessions list
        Label historyLabel = new Label("Chat History");
        historyLabel.getStyleClass().addAll(Styles.TEXT_BOLD, "sidebar-section-title");

        chatSessionsList = new VBox(8);
        chatSessionsList.getStyleClass().add("chat-sessions-list");

        ScrollPane sessionsScroll = new ScrollPane(chatSessionsList);
        sessionsScroll.setFitToWidth(true);
        sessionsScroll.getStyleClass().add("sessions-scroll");
        VBox.setVgrow(sessionsScroll, Priority.ALWAYS);

        getChildren().addAll(sidebarHeader, searchBox, historyLabel, sessionsScroll);
    }

    private HBox createSidebarHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("AI Assistant");
        title.getStyleClass().addAll(Styles.TITLE_4);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button newChatBtn = new Button();
        newChatBtn.setGraphic(new FontIcon(Octicons.PLUS_16));
        newChatBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        newChatBtn.setTooltip(new Tooltip("New Chat"));
        newChatBtn.setOnAction(e -> eventHandler.onNewChat());

        header.getChildren().addAll(title, spacer, newChatBtn);
        return header;
    }

    public void updateConversations(List<Conversation> conversations) {
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
        titleLabel.setMaxWidth(160);

        Label previewLabel = new Label(
                conv.getPreview() != null ? conv.getPreview() : "Empty conversation");
        previewLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");
        previewLabel.setMaxWidth(160);

        textContent.getChildren().addAll(titleLabel, previewLabel);

        // Delete conversation button (hidden by default)
        Button deleteBtn = new Button();
        deleteBtn.setGraphic(new FontIcon(Octicons.X_16));
        deleteBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, "conversation-delete-btn");
        deleteBtn.setVisible(false);
        deleteBtn.setManaged(false);
        deleteBtn.setOnAction(e -> {
            e.consume(); // Prevent conversation selection
            eventHandler.onDeleteConversation(conv.getId());
        });

        Label timeLabel = new Label(
                conv.getUpdatedAt().format(DateTimeFormatter.ofPattern("MMM dd")));
        timeLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");

        content.getChildren().addAll(textContent, deleteBtn, timeLabel);

        Button sessionBtn = new Button();
        sessionBtn.setGraphic(content);
        sessionBtn.getStyleClass().add("chat-session-btn");
        sessionBtn.setMaxWidth(Double.MAX_VALUE);

        if (conv.getId().equals(currentConversationId)) {
            sessionBtn.getStyleClass().add("active");
        }

        // Show delete button on hover
        sessionBtn.setOnMouseEntered(e -> {
            deleteBtn.setVisible(true);
            deleteBtn.setManaged(true);
        });
        sessionBtn.setOnMouseExited(e -> {
            deleteBtn.setVisible(false);
            deleteBtn.setManaged(false);
        });

        sessionBtn.setOnAction(e -> eventHandler.onConversationSelected(conv.getId()));

        return sessionBtn;
    }

    public void setCurrentConversationId(Long conversationId) {
        this.currentConversationId = conversationId;
    }

    public void showError(String message) {
        chatSessionsList.getChildren().clear();
        Label errorLabel = new Label(message);
        errorLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-danger");
        chatSessionsList.getChildren().add(errorLabel);
    }
}
