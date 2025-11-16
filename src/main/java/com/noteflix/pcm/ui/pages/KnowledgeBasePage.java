package com.noteflix.pcm.ui.pages;

import com.noteflix.pcm.core.i18n.I18n;
import com.noteflix.pcm.ui.base.BaseView;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import com.noteflix.pcm.ui.styles.StyleConstants;
import com.noteflix.pcm.ui.utils.LayoutHelper;
import com.noteflix.pcm.ui.utils.UIFactory;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

/**
 * Knowledge Base page - Placeholder Implementation
 * <p>
 * Simple placeholder page showing basic information about the Knowledge Base functionality.
 * This serves as a template for future implementation of knowledge management features.
 */
@Slf4j
public class KnowledgeBasePage extends BaseView {

    public KnowledgeBasePage() {
        super(
                I18n.get("page.kb.title"),
                I18n.get("page.kb.subtitle"),
                new FontIcon(Octicons.BOOK_24)
        );
        log.debug("KnowledgeBasePage placeholder initialized");
    }

    @Override
    protected Node createMainContent() {
        VBox content = LayoutHelper.createVBox(LayoutConstants.SPACING_XL);
        content.getStyleClass().add(StyleConstants.PAGE_CONTAINER);
        content.setAlignment(Pos.CENTER);
        LayoutHelper.setVGrow(content);

        // Create placeholder content
        VBox placeholderCard = UIFactory.createCard();
        placeholderCard.setAlignment(Pos.CENTER);
        placeholderCard.setPrefHeight(400);
        placeholderCard.getStyleClass().add("placeholder-card");

        FontIcon icon = new FontIcon(Octicons.BOOK_24);
        icon.setIconSize(64);
        icon.getStyleClass().add("placeholder-icon");

        Label title = UIFactory.createSectionTitle("Knowledge Base Management");
        title.getStyleClass().add("placeholder-title");

        Label description = UIFactory.createMutedLabel(
                "This page will contain knowledge base management functionality including:\n\n" +
                        "• Browse and search documentation\n" +
                        "• Create and edit knowledge articles\n" +
                        "• Organize content by categories\n" +
                        "• Version control and collaboration\n" +
                        "• Export and sharing capabilities\n\n" +
                        "Coming soon..."
        );
        description.setWrapText(true);
        description.getStyleClass().add("placeholder-description");

        placeholderCard.getChildren().addAll(icon, title, description);
        content.getChildren().add(placeholderCard);

        return content;
    }

    @Override
    public void onActivate() {
        super.onActivate();
        log.debug("KnowledgeBasePage placeholder activated");
    }
}