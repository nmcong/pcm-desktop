package com.noteflix.pcm.ui.components.common.lists;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import com.noteflix.pcm.ui.styles.StyleConstants;
import com.noteflix.pcm.ui.utils.LayoutHelper;
import com.noteflix.pcm.ui.utils.UIFactory;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * List Item Component
 *
 * <p>Generic list item with flexible layout:
 * [Leading] [Title/Subtitle] [Trailing]
 *
 * <p>Features:
 * - Optional leading node (icon, avatar, etc.)
 * - Title and subtitle
 * - Optional trailing node (icon, button, badge, etc.)
 * - Click action support
 * - Hover effects
 *
 * <p>Usage:
 * <pre>{@code
 * ListItem item = new ListItem()
 *     .withLeading(new FontIcon(Octicons.FILE_16))
 *     .withTitle("Document.pdf")
 *     .withSubtitle("Modified 2 hours ago")
 *     .withTrailing(moreButton)
 *     .withAction(this::openDocument);
 * }</pre>
 *
 * @author PCM Team
 * @version 2.0.0
 */
public class ListItem extends HBox {

    protected Node leadingNode;
    protected Label titleLabel;
    protected Label subtitleLabel;
    protected Node trailingNode;
    protected VBox textContent;

    /**
     * Create empty list item
     */
    public ListItem() {
        super(LayoutConstants.SPACING_MD);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add(StyleConstants.LIST_ITEM);
        setPadding(LayoutConstants.PADDING_COMPACT);

        // Initialize text content area
        textContent = LayoutHelper.createVBox(LayoutConstants.SPACING_XS);
        LayoutHelper.setHGrow(textContent);
    }

    /**
     * Set leading node (icon, avatar, checkbox, etc.)
     *
     * @param node Leading node
     * @return this for chaining
     */
    public ListItem withLeading(Node node) {
        if (leadingNode != null) {
            getChildren().remove(leadingNode);
        }
        leadingNode = node;
        rebuildLayout();
        return this;
    }

    /**
     * Set title
     *
     * @param title Title text
     * @return this for chaining
     */
    public ListItem withTitle(String title) {
        if (titleLabel == null) {
            titleLabel = new Label(title);
            titleLabel.getStyleClass().add(Styles.TEXT_BOLD);
        } else {
            titleLabel.setText(title);
        }
        rebuildTextContent();
        return this;
    }

    /**
     * Set subtitle
     *
     * @param subtitle Subtitle text
     * @return this for chaining
     */
    public ListItem withSubtitle(String subtitle) {
        if (subtitleLabel == null) {
            subtitleLabel = UIFactory.createMutedLabel(subtitle);
            subtitleLabel.getStyleClass().add(Styles.TEXT_SMALL);
        } else {
            subtitleLabel.setText(subtitle);
        }
        rebuildTextContent();
        return this;
    }

    /**
     * Set trailing node (button, icon, badge, etc.)
     *
     * @param node Trailing node
     * @return this for chaining
     */
    public ListItem withTrailing(Node node) {
        if (trailingNode != null) {
            getChildren().remove(trailingNode);
        }
        trailingNode = node;
        rebuildLayout();
        return this;
    }

    /**
     * Set click action
     *
     * @param action Action to perform on click
     * @return this for chaining
     */
    public ListItem withAction(Runnable action) {
        if (action != null) {
            setOnMouseClicked(e -> action.run());
            setCursor(Cursor.HAND);
            getStyleClass().add("clickable");
        }
        return this;
    }

    /**
     * Apply selected state
     *
     * @param selected true to mark as selected
     * @return this for chaining
     */
    public ListItem withSelected(boolean selected) {
        if (selected) {
            getStyleClass().add(StyleConstants.SELECTED);
        } else {
            getStyleClass().remove(StyleConstants.SELECTED);
        }
        return this;
    }

    /**
     * Apply disabled state
     *
     * @param disabled true to disable
     * @return this for chaining
     */
    public ListItem withDisabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    /**
     * Rebuild text content area
     */
    protected void rebuildTextContent() {
        textContent.getChildren().clear();

        if (titleLabel != null) {
            textContent.getChildren().add(titleLabel);
        }

        if (subtitleLabel != null) {
            textContent.getChildren().add(subtitleLabel);
        }
    }

    /**
     * Rebuild entire layout
     */
    protected void rebuildLayout() {
        getChildren().clear();

        // Add leading node if present
        if (leadingNode != null) {
            getChildren().add(leadingNode);
        }

        // Add text content
        rebuildTextContent();
        getChildren().add(textContent);

        // Add trailing node if present
        if (trailingNode != null) {
            getChildren().add(trailingNode);
        }
    }

    /**
     * Get title text
     *
     * @return Title text or null
     */
    public String getTitle() {
        return titleLabel != null ? titleLabel.getText() : null;
    }

    /**
     * Get subtitle text
     *
     * @return Subtitle text or null
     */
    public String getSubtitle() {
        return subtitleLabel != null ? subtitleLabel.getText() : null;
    }
}

