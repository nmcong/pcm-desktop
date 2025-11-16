package com.noteflix.pcm.ui.pages.ai.components;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.domain.chat.Conversation;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import com.noteflix.pcm.ui.utils.LayoutHelper;
import com.noteflix.pcm.ui.utils.UIFactory;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * Conversation Item Component
 *
 * <p>Represents a single conversation in the sidebar list.
 *
 * @author PCM Team
 * @version 2.0.0
 */
public class ConversationItem extends HBox {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, HH:mm");

    @Getter
    private final Conversation conversation;
    private final Label titleLabel;
    private final Label timestampLabel;
    private Consumer<Conversation> onSelect;

    /**
     * Create conversation item
     *
     * @param conversation Conversation to display
     */
    public ConversationItem(Conversation conversation) {
        super(LayoutConstants.SPACING_SM);
        this.conversation = conversation;

        setAlignment(Pos.CENTER_LEFT);
        setPadding(LayoutConstants.PADDING_COMPACT);
        getStyleClass().addAll("conversation-item", "list-item");
        setCursor(Cursor.HAND);

        // Text content
        VBox textContent = LayoutHelper.createVBox(LayoutConstants.SPACING_XS);

        titleLabel = new Label(conversation.getTitle());
        titleLabel.getStyleClass().add(Styles.TEXT_BOLD);
        titleLabel.setMaxWidth(200);
        titleLabel.setWrapText(false);

        timestampLabel = UIFactory.createMutedLabel(
                conversation.getCreatedAt().format(DATE_FORMATTER)
        );
        timestampLabel.getStyleClass().add(Styles.TEXT_SMALL);

        textContent.getChildren().addAll(titleLabel, timestampLabel);

        getChildren().add(textContent);

        // Click handler
        setOnMouseClicked(e -> {
            if (onSelect != null) {
                onSelect.accept(conversation);
            }
        });

        // Hover effect
        setOnMouseEntered(e -> getStyleClass().add("hover"));
        setOnMouseExited(e -> getStyleClass().remove("hover"));
    }

    /**
     * Set select handler
     *
     * @param handler Select handler
     * @return this for chaining
     */
    public ConversationItem withSelectHandler(Consumer<Conversation> handler) {
        this.onSelect = handler;
        return this;
    }

    /**
     * Set selected state
     *
     * @param selected true if selected
     * @return this for chaining
     */
    public ConversationItem withSelected(boolean selected) {
        if (selected) {
            getStyleClass().add("selected");
        } else {
            getStyleClass().remove("selected");
        }
        return this;
    }

    /**
     * Update title
     *
     * @param newTitle New title
     */
    public void updateTitle(String newTitle) {
        titleLabel.setText(newTitle);
    }
}

