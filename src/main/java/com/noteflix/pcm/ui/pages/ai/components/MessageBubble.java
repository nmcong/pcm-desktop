package com.noteflix.pcm.ui.pages.ai.components;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.domain.chat.Message;
import com.noteflix.pcm.ui.pages.ai.handlers.ChatEventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

import java.time.format.DateTimeFormatter;

/**
 * Message bubble component for displaying individual messages
 */
public class MessageBubble extends VBox {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public MessageBubble(Message message, ChatEventHandler eventHandler) {
        getStyleClass().add("message-box");
        setSpacing(8);

        HBox messageRow = new HBox(12);
        messageRow.setAlignment(message.isFromAI() ? Pos.TOP_LEFT : Pos.TOP_RIGHT);

        // Add avatar icon for messages
        VBox avatarBox = createAvatar(
                message.isFromAI() ? Octicons.DEPENDABOT_24 : Octicons.PERSON_16,
                message.isFromAI() ? "ai-avatar" : "user-avatar"
        );
        messageRow.getChildren().add(avatarBox);

        // Message content container
        VBox contentContainer = new VBox(4);
        contentContainer.setMaxWidth(600);

        if (message.isFromAI()) {
            // AI message - no bubble, just plain text
            contentContainer.getStyleClass().add("ai-message-content");

            Label messageLabel = new Label(message.getContent());
            messageLabel.setWrapText(true);
            messageLabel.getStyleClass().add("ai-message-text");

            HBox bottomRow = createAIBottomRow(message, eventHandler);

            contentContainer.getChildren().addAll(messageLabel, bottomRow);
        } else {
            // User message - with bubble (using old AI style)
            VBox bubble = new VBox(4);
            bubble.getStyleClass().add("user-message-bubble");
            bubble.setPadding(new Insets(12, 16, 12, 16));

            Label messageLabel = new Label(message.getContent());
            messageLabel.setWrapText(true);
            messageLabel.getStyleClass().add("message-text");

            Label timeLabel = new Label(message.getCreatedAt().format(TIME_FORMATTER));
            timeLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");

            bubble.getChildren().addAll(messageLabel, timeLabel);
            contentContainer.getChildren().add(bubble);
        }

        messageRow.getChildren().add(contentContainer);
        getChildren().add(messageRow);
    }

    private VBox createAvatar(Octicons icon, String styleClass) {
        VBox avatarBox = new VBox();
        avatarBox.setAlignment(Pos.CENTER);
        avatarBox.getStyleClass().add("message-avatar");
        avatarBox.setPrefSize(32, 32);
        avatarBox.setMinSize(32, 32);
        avatarBox.setMaxSize(32, 32);

        FontIcon avatarIcon = new FontIcon(icon);
        avatarIcon.setIconSize(18);
        avatarIcon.getStyleClass().add(styleClass);

        avatarBox.getChildren().add(avatarIcon);
        return avatarBox;
    }

    private HBox createAIBottomRow(Message message, ChatEventHandler eventHandler) {
        HBox bottomRow = new HBox(8);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        bottomRow.getStyleClass().add("ai-message-actions");

        Label timeLabel = new Label(message.getCreatedAt().format(TIME_FORMATTER));
        timeLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Action buttons (always reserved space, shown/hidden on hover)
        HBox actionButtons = new HBox(4);
        actionButtons.getStyleClass().add("message-action-buttons");
        actionButtons.setVisible(false);
        actionButtons.setManaged(true); // Always reserve space for buttons

        // Copy button
        Button copyBtn = new Button();
        copyBtn.setGraphic(new FontIcon(Octicons.CLIPPY_16));
        copyBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, "message-action-btn");
        copyBtn.setTooltip(new Tooltip("Copy"));
        copyBtn.setOnAction(e -> eventHandler.onCopyMessage(message.getContent()));

        // Token info button
        Button tokenBtn = new Button();
        tokenBtn.setGraphic(new FontIcon(Octicons.INFO_16));
        tokenBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, "message-action-btn");
        tokenBtn.setTooltip(new Tooltip("Token Info"));
        tokenBtn.setOnAction(e -> eventHandler.onShowTokenInfo(message.getId()));

        actionButtons.getChildren().addAll(copyBtn, tokenBtn);

        bottomRow.getChildren().addAll(timeLabel, spacer, actionButtons);

        // Show actions on hover
        setOnMouseEntered(e -> {
            actionButtons.setVisible(true);
        });
        setOnMouseExited(e -> {
            actionButtons.setVisible(false);
        });

        return bottomRow;
    }

    /**
     * Create a streaming message bubble (without timestamp)
     */
    public static HBox createStreamingBubble(String content, Label contentLabel) {
        HBox messageRow = new HBox(12);
        messageRow.setAlignment(Pos.TOP_LEFT);

        // AI avatar
        VBox avatarBox = new VBox();
        avatarBox.setAlignment(Pos.CENTER);
        avatarBox.getStyleClass().add("message-avatar");
        avatarBox.setPrefSize(32, 32);
        avatarBox.setMinSize(32, 32);
        avatarBox.setMaxSize(32, 32);

        FontIcon avatarIcon = new FontIcon(Octicons.DEPENDABOT_24);
        avatarIcon.setIconSize(18);
        avatarIcon.getStyleClass().add("ai-avatar");

        avatarBox.getChildren().add(avatarIcon);
        messageRow.getChildren().add(avatarBox);

        // Message content (no bubble)
        VBox contentContainer = new VBox(4);
        contentContainer.setMaxWidth(600);
        contentContainer.getStyleClass().add("ai-message-content");

        contentLabel.setText(content);
        contentLabel.setWrapText(true);
        contentLabel.getStyleClass().add("ai-message-text");

        contentContainer.getChildren().add(contentLabel);
        messageRow.getChildren().add(contentContainer);

        return messageRow;
    }
}

