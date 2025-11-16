package com.noteflix.pcm.ui.pages.ai.components;

import com.noteflix.pcm.ui.components.common.buttons.IconButton;
import com.noteflix.pcm.ui.components.common.buttons.PrimaryButton;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import com.noteflix.pcm.ui.utils.LayoutHelper;
import javafx.geometry.Pos;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import org.kordamp.ikonli.octicons.Octicons;

import java.util.function.Consumer;

/**
 * Chat Input Area Component
 *
 * <p>Contains:
 * - Text input area
 * - Send button
 * - Additional action buttons
 *
 * @author PCM Team
 * @version 2.0.0
 */
public class ChatInputArea extends VBox {

    @Getter
    private final TextArea inputField;
    private PrimaryButton sendButton;  // Not final to allow initialization in method
    private Consumer<String> onSend;

    /**
     * Create chat input area
     */
    public ChatInputArea() {
        super(LayoutConstants.SPACING_SM);
        getStyleClass().add("chat-input-area");
        setPadding(LayoutConstants.PADDING_DEFAULT);

        // Input field
        inputField = new TextArea();
        inputField.setPromptText("Type your message... (Shift+Enter for new line, Enter to send)");
        inputField.setWrapText(true);
        inputField.setPrefRowCount(3);
        inputField.getStyleClass().add("chat-input");

        // Handle Enter key
        inputField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && !e.isShiftDown()) {
                e.consume();
                handleSend();
            }
        });

        // Action buttons
        HBox actions = createActionButtons();

        getChildren().addAll(inputField, actions);
    }

    /**
     * Create action buttons
     */
    private HBox createActionButtons() {
        HBox actions = LayoutHelper.createHBox(
                Pos.CENTER_LEFT,
                LayoutConstants.SPACING_SM
        );

        // Additional action buttons
        IconButton attachBtn = new IconButton(Octicons.CLIPPY_16, "Attach file");
        IconButton voiceBtn = new IconButton(Octicons.UNMUTE_16, "Voice input");

        // Send button
        sendButton = new PrimaryButton("Send", Octicons.PAPER_AIRPLANE_16)
                .withAction(this::handleSend);

        // Spacer
        HBox spacer = new HBox();
        LayoutHelper.setHGrow(spacer);

        actions.getChildren().addAll(
                attachBtn,
                voiceBtn,
                spacer,
                sendButton
        );

        return actions;
    }

    /**
     * Handle send action
     */
    private void handleSend() {
        String text = inputField.getText().trim();
        if (!text.isEmpty() && onSend != null) {
            onSend.accept(text);
            inputField.clear();
        }
    }

    /**
     * Set send handler
     *
     * @param handler Send handler
     * @return this for chaining
     */
    public ChatInputArea withSendHandler(Consumer<String> handler) {
        this.onSend = handler;
        return this;
    }

    /**
     * Set input text
     *
     * @param text Text to set
     */
    public void setText(String text) {
        inputField.setText(text);
    }

    /**
     * Clear input
     */
    public void clear() {
        inputField.clear();
    }

    /**
     * Set disabled state
     *
     * @param disabled true to disable
     */
    public void setInputDisabled(boolean disabled) {
        inputField.setDisable(disabled);
        sendButton.setDisable(disabled);
    }

    /**
     * Focus input field
     */
    public void requestInputFocus() {
        inputField.requestFocus();
    }
}

