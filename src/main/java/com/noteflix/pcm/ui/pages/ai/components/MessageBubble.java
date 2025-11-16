package com.noteflix.pcm.ui.pages.ai.components;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.domain.chat.Message;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

/**
 * Message bubble component for displaying individual messages
 */
public class MessageBubble extends VBox {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public MessageBubble(Message message) {
        getStyleClass().add("message-box");
        setSpacing(8);

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

        Label timeLabel = new Label(message.getCreatedAt().format(TIME_FORMATTER));
        timeLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");

        bubble.getChildren().addAll(messageLabel, timeLabel);

        messageRow.getChildren().add(bubble);

        getChildren().add(messageRow);
    }

    /**
     * Create a streaming message bubble (without timestamp)
     */
    public static VBox createStreamingBubble(String content, Label contentLabel) {
        VBox bubble = new VBox(4);
        bubble.getStyleClass().add("ai-message-bubble");
        bubble.setPadding(new Insets(12, 16, 12, 16));
        bubble.setMaxWidth(600);

        contentLabel.setText(content);
        contentLabel.setWrapText(true);
        contentLabel.getStyleClass().add("message-text");

        bubble.getChildren().add(contentLabel);
        return bubble;
    }
}

