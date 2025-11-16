package com.noteflix.pcm.ui.components.common.cards;

import com.noteflix.pcm.ui.styles.LayoutConstants;
import com.noteflix.pcm.ui.styles.StyleConstants;
import com.noteflix.pcm.ui.utils.UIFactory;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Card Component
 *
 * <p>A container with padding, background, and optional shadow.
 * Basic building block for content sections.
 *
 * <p>Features:
 * - Optional title
 * - Content area
 * - Optional footer
 * - Consistent padding and styling
 * - Fluent API
 *
 * <p>Usage:
 * <pre>{@code
 * Card card = new Card()
 *     .withTitle("User Information")
 *     .withContent(userDetailsPane)
 *     .withFooter(actionButtons);
 * }</pre>
 *
 * @author PCM Team
 * @version 2.0.0
 */
public class Card extends VBox {

    private Label titleLabel;
    private VBox contentArea;
    private VBox footerArea;

    /**
     * Create empty card
     */
    public Card() {
        super(LayoutConstants.SPACING_MD);
        getStyleClass().add(StyleConstants.CARD);
        setPadding(LayoutConstants.PADDING_CARD);

        // Initialize content area
        contentArea = new VBox(LayoutConstants.SPACING_SM);
    }

    /**
     * Create card with title
     *
     * @param title Card title
     */
    public Card(String title) {
        this();
        withTitle(title);
    }

    /**
     * Set card title
     *
     * @param title Title text
     * @return this for chaining
     */
    public Card withTitle(String title) {
        if (titleLabel == null) {
            titleLabel = UIFactory.createSectionTitle(title);

            // Rebuild children
            rebuildChildren();
        } else {
            titleLabel.setText(title);
        }
        return this;
    }

    /**
     * Add content to card
     *
     * @param content Content nodes
     * @return this for chaining
     */
    public Card withContent(Node... content) {
        contentArea.getChildren().addAll(content);
        rebuildChildren();
        return this;
    }

    /**
     * Set footer
     *
     * @param footer Footer node
     * @return this for chaining
     */
    public Card withFooter(Node footer) {
        if (footerArea == null) {
            footerArea = new VBox(LayoutConstants.SPACING_SM);
        }
        footerArea.getChildren().clear();
        footerArea.getChildren().add(footer);
        rebuildChildren();
        return this;
    }

    /**
     * Clear all content
     *
     * @return this for chaining
     */
    public Card clearContent() {
        contentArea.getChildren().clear();
        rebuildChildren();
        return this;
    }

    /**
     * Apply additional style classes
     *
     * @param styleClasses Style classes to add
     * @return this for chaining
     */
    public Card withStyle(String... styleClasses) {
        getStyleClass().addAll(styleClasses);
        return this;
    }

    /**
     * Set spacing between content items
     *
     * @param spacing Spacing in pixels
     * @return this for chaining
     */
    public Card withSpacing(double spacing) {
        setSpacing(spacing);
        contentArea.setSpacing(spacing);
        return this;
    }

    /**
     * Rebuild card structure
     */
    private void rebuildChildren() {
        getChildren().clear();

        // Add title if present
        if (titleLabel != null) {
            getChildren().add(titleLabel);

            // Add separator after title if there's content
            if (!contentArea.getChildren().isEmpty()) {
                getChildren().add(UIFactory.createSeparator());
            }
        }

        // Add content area if not empty
        if (!contentArea.getChildren().isEmpty()) {
            getChildren().add(contentArea);
        }

        // Add footer if present
        if (footerArea != null && !footerArea.getChildren().isEmpty()) {
            getChildren().add(UIFactory.createSeparator());
            getChildren().add(footerArea);
        }
    }

    /**
     * Get content area for direct manipulation
     *
     * @return Content VBox
     */
    public VBox getContentArea() {
        return contentArea;
    }

    /**
     * Get footer area for direct manipulation
     *
     * @return Footer VBox (may be null)
     */
    public VBox getFooterArea() {
        return footerArea;
    }
}

