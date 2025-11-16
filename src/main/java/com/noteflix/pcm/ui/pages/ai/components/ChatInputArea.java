package com.noteflix.pcm.ui.pages.ai.components;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.ui.pages.ai.handlers.ChatEventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Component for chat input with send button
 */
public class ChatInputArea extends VBox {

    private final ChatEventHandler eventHandler;
    private final TextArea inputField;
    private final Button sendButton;

    public ChatInputArea(ChatEventHandler eventHandler) {
        this.eventHandler = eventHandler;

        getStyleClass().add("chat-input-container");
        setPadding(new Insets(20));
        setAlignment(Pos.CENTER);
        setSpacing(12);

        HBox inputBox = new HBox(12);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.getStyleClass().add("chat-input-box");
        inputBox.setMaxWidth(768);

        VBox inputWrapper = new VBox();
        inputWrapper.getStyleClass().add("input-wrapper");

        inputField = new TextArea();
        inputField.setPromptText("Ask me anything about your system...");
        inputField.getStyleClass().add("chat-input");
        inputField.setPrefRowCount(1);
        inputField.setWrapText(true);

        // Auto-resize
        inputField.textProperty().addListener((obs, old, newText) -> {
            if (newText.contains("\n")) {
                int lines = newText.split("\n").length;
                inputField.setPrefRowCount(Math.min(lines, 8));
            }
        });

        // Enter key handler
        inputField.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER") && !e.isShiftDown()) {
                e.consume();
                handleSend();
            }
        });

        // Input actions
        HBox inputActions = new HBox(8);
        inputActions.setAlignment(Pos.CENTER_RIGHT);
        inputActions.setPadding(new Insets(8));

        sendButton = new Button();
        sendButton.setGraphic(new FontIcon(Feather.SEND));
        sendButton.getStyleClass().addAll(Styles.BUTTON_ICON, "send-btn");
        sendButton.setOnAction(e -> handleSend());

        inputActions.getChildren().add(sendButton);

        inputWrapper.getChildren().addAll(inputField, inputActions);
        HBox.setHgrow(inputWrapper, Priority.ALWAYS);

        inputBox.getChildren().add(inputWrapper);
        getChildren().add(inputBox);
    }

    private void handleSend() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            eventHandler.onSendMessage(message);
        }
    }

    public void clear() {
        inputField.clear();
        inputField.setPrefRowCount(1);
    }

    public void setInputEnabled(boolean enabled) {
        inputField.setDisable(!enabled);
        sendButton.setDisable(!enabled);
    }

    public void requestInputFocus() {
        inputField.requestFocus();
    }

    public String getText() {
        return inputField.getText();
    }

    public void setText(String text) {
        inputField.setText(text);
    }
}
