package com.noteflix.pcm.ui.pages.ai.components;

import com.noteflix.pcm.domain.chat.Message;
import com.noteflix.pcm.ui.pages.ai.handlers.ChatEventHandler;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Component for displaying chat messages with scrolling
 */
@Slf4j
public class ChatMessagesArea extends VBox {

    private final ChatEventHandler eventHandler;
    private final VBox messagesContainer;
    private final ScrollPane scrollPane;
    private AIStatusIndicator statusIndicator;
    private Label streamingMessageLabel;
    private HBox streamingMessageBox;

    public ChatMessagesArea(ChatEventHandler eventHandler) {
        this.eventHandler = eventHandler;
        getStyleClass().add("messages-area");
        setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(this, Priority.ALWAYS);

        VBox messagesWrapper = new VBox();
        messagesWrapper.setAlignment(Pos.TOP_CENTER);
        messagesWrapper.setPadding(new Insets(20));

        messagesContainer = new VBox(12);
        messagesContainer.getStyleClass().add("chat-messages-area");
        messagesContainer.setMaxWidth(768);
        messagesContainer.setAlignment(Pos.TOP_LEFT);

        messagesWrapper.getChildren().add(messagesContainer);

        scrollPane = new ScrollPane(messagesWrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("chat-scroll");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().add(scrollPane);
    }

    public void displayMessages(List<Message> messages) {
        messagesContainer.getChildren().clear();

        for (Message msg : messages) {
            MessageBubble bubble = new MessageBubble(msg, eventHandler);
            messagesContainer.getChildren().add(bubble);
        }

        scrollToBottom();
    }

    public void addUserMessage(Message message) {
        MessageBubble bubble = new MessageBubble(message, eventHandler);
        messagesContainer.getChildren().add(bubble);
        scrollToBottom();
    }

    public void showThinkingStatus() {
        hideStatusIndicator();
        statusIndicator = new AIStatusIndicator();
        statusIndicator.showThinking();
        messagesContainer.getChildren().add(statusIndicator);
        scrollToBottom();
    }

    public void showLoadingStatus() {
        if (statusIndicator == null) {
            statusIndicator = new AIStatusIndicator();
            messagesContainer.getChildren().add(statusIndicator);
        }
        statusIndicator.showLoading();
        scrollToBottom();
    }

    public void showPlanningStatus() {
        if (statusIndicator == null) {
            statusIndicator = new AIStatusIndicator();
            messagesContainer.getChildren().add(statusIndicator);
        }
        statusIndicator.showPlanning();
        scrollToBottom();
    }

    public void showSearchingStatus(String query) {
        if (statusIndicator == null) {
            statusIndicator = new AIStatusIndicator();
            messagesContainer.getChildren().add(statusIndicator);
        }
        statusIndicator.showSearching(query);
        scrollToBottom();
    }

    public void setCustomStatus(String iconCode, String message) {
        if (statusIndicator == null) {
            statusIndicator = new AIStatusIndicator();
            messagesContainer.getChildren().add(statusIndicator);
        }
        statusIndicator.setCustomStatus(iconCode, message);
        scrollToBottom();
    }

    public void hideStatusIndicator() {
        if (statusIndicator != null) {
            messagesContainer.getChildren().remove(statusIndicator);
            statusIndicator = null;
        }
    }

    public void updateStreamingMessage(String content) {
        if (streamingMessageLabel == null) {
            hideStatusIndicator();

            streamingMessageLabel = new Label();
            streamingMessageBox = MessageBubble.createStreamingBubble(content, streamingMessageLabel);
            messagesContainer.getChildren().add(streamingMessageBox);
        } else {
            streamingMessageLabel.setText(content);
        }

        scrollToBottom();
    }

    public void finalizeStreamingMessage() {
        streamingMessageLabel = null;
        streamingMessageBox = null;
    }

    public void clear() {
        messagesContainer.getChildren().clear();
    }

    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }
}

