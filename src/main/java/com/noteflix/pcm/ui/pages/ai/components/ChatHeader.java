package com.noteflix.pcm.ui.pages.ai.components;

import atlantafx.base.theme.Styles;
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
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Header component for chat area
 */
public class ChatHeader extends HBox {

    private final ChatEventHandler eventHandler;
    private final Label titleLabel;
    private final Label subtitleLabel;

    public ChatHeader(ChatEventHandler eventHandler) {
        this.eventHandler = eventHandler;

        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("chat-header");
        setPadding(new Insets(12, 16, 12, 16));
        setSpacing(12);

        FontIcon chatIcon = new FontIcon(Feather.MESSAGE_CIRCLE);
        chatIcon.setIconSize(16);

        VBox titleBox = new VBox(2);
        titleLabel = new Label("New Chat");
        titleLabel.getStyleClass().addAll(Styles.TITLE_4);

        subtitleLabel = new Label("AI-powered system analysis and assistance");
        subtitleLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Chat actions
        Button clearBtn = new Button();
        clearBtn.setGraphic(new FontIcon(Feather.TRASH_2));
        clearBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        clearBtn.setTooltip(new Tooltip("Clear Chat"));
        clearBtn.setOnAction(e -> eventHandler.onClearChat());

        HBox actions = new HBox(8, clearBtn);

        getChildren().addAll(chatIcon, titleBox, spacer, actions);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle);
    }
}

