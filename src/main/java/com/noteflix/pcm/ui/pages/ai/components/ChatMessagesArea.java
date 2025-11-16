package com.noteflix.pcm.ui.pages.ai.components;

import com.noteflix.pcm.domain.chat.Message;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Component for displaying chat messages with scrolling
 */
@Slf4j
public class ChatMessagesArea extends VBox {

    private final VBox messagesContainer;
    private final ScrollPane scrollPane;
    private VBox loadingIndicator;
    private Label streamingMessageLabel;

    public ChatMessagesArea() {
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
            MessageBubble bubble = new MessageBubble(msg);
            messagesContainer.getChildren().add(bubble);
        }

        scrollToBottom();
    }

    public void addUserMessage(Message message) {
        MessageBubble bubble = new MessageBubble(message);
        messagesContainer.getChildren().add(bubble);
        scrollToBottom();
    }

    public void showLoadingIndicator() {
        loadingIndicator = new VBox(8);
        loadingIndicator.getStyleClass().add("loading-indicator");
        loadingIndicator.setAlignment(Pos.CENTER_LEFT);

        Label loadingText = new Label("AI is thinking...");
        loadingText.getStyleClass().add("loading-text");

        loadingIndicator.getChildren().add(loadingText);
        messagesContainer.getChildren().add(loadingIndicator);
        scrollToBottom();
    }

    public void hideLoadingIndicator() {
        if (loadingIndicator != null) {
            messagesContainer.getChildren().remove(loadingIndicator);
            loadingIndicator = null;
        }
    }

    public void updateStreamingMessage(String content) {
        if (streamingMessageLabel == null) {
            hideLoadingIndicator();

            streamingMessageLabel = new Label();
            VBox bubble = MessageBubble.createStreamingBubble(content, streamingMessageLabel);
            messagesContainer.getChildren().add(bubble);
        } else {
            streamingMessageLabel.setText(content);
        }

        scrollToBottom();
    }

    public void finalizeStreamingMessage() {
        streamingMessageLabel = null;
    }

    public void clear() {
        messagesContainer.getChildren().clear();
    }

    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }
}

