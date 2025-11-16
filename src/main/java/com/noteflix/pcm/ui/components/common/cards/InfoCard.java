package com.noteflix.pcm.ui.components.common.cards;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.ui.components.common.buttons.PrimaryButton;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import com.noteflix.pcm.ui.styles.StyleConstants;
import com.noteflix.pcm.ui.utils.UIFactory;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.Getter;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Info Card Component
 *
 * <p>Card with icon, title, description, and optional action button.
 * Useful for feature highlights, help cards, status messages, etc.
 *
 * <p>Features:
 * - Large icon
 * - Title
 * - Description text
 * - Optional action button
 * - Color-coded
 *
 * <p>Usage:
 * <pre>{@code
 * InfoCard helpCard = new InfoCard(
 *     "Need Help?",
 *     "Contact our support team for assistance",
 *     Octicons.QUESTION_16
 * ).withAction("Contact Support", this::openSupport);
 * }</pre>
 *
 * @author PCM Team
 * @version 2.0.0
 */
public class InfoCard extends VBox {

    @Getter
    private final String title;
    @Getter
    private final String description;
    @Getter
    private final FontIcon icon;

    private Label titleLabel;
    private Label descriptionLabel;
    private Button actionButton;

    /**
     * Create info card
     *
     * @param title       Card title
     * @param description Card description
     * @param iconCode    Icon to display
     */
    public InfoCard(String title, String description, Ikon iconCode) {
        super(LayoutConstants.SPACING_MD);
        this.title = title;
        this.description = description;
        this.icon = new FontIcon(iconCode);
        this.icon.setIconSize(LayoutConstants.ICON_SIZE_XXL);

        // Styling
        getStyleClass().addAll(StyleConstants.CARD, "info-card");
        setPadding(LayoutConstants.PADDING_CARD);
        setAlignment(Pos.CENTER);

        // Build UI
        buildUI();
    }

    /**
     * Build card UI
     */
    private void buildUI() {
        // Icon at top
        getChildren().add(icon);

        // Title
        titleLabel = UIFactory.createTitleLabel(title);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-weight: bold;");
        getChildren().add(titleLabel);

        // Description
        descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().addAll(Styles.TEXT_MUTED);
        descriptionLabel.setAlignment(Pos.CENTER);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(300);
        getChildren().add(descriptionLabel);
    }

    /**
     * Add action button
     *
     * @param text   Button text
     * @param action Action to perform
     * @return this for chaining
     */
    public InfoCard withAction(String text, Runnable action) {
        if (actionButton == null) {
            actionButton = new PrimaryButton(text)
                    .withAction(action);
            getChildren().add(actionButton);
        } else {
            actionButton.setText(text);
            actionButton.setOnAction(e -> action.run());
        }
        return this;
    }

    /**
     * Set icon size
     *
     * @param size Icon size in pixels
     * @return this for chaining
     */
    public InfoCard withIconSize(int size) {
        icon.setIconSize(size);
        return this;
    }

    /**
     * Apply accent color to icon
     *
     * @return this for chaining
     */
    public InfoCard asAccent() {
        icon.getStyleClass().add(Styles.ACCENT);
        return this;
    }

    /**
     * Apply success color to icon
     *
     * @return this for chaining
     */
    public InfoCard asSuccess() {
        icon.getStyleClass().add(Styles.SUCCESS);
        return this;
    }

    /**
     * Apply warning color to icon
     *
     * @return this for chaining
     */
    public InfoCard asWarning() {
        icon.getStyleClass().add(Styles.WARNING);
        return this;
    }

    /**
     * Apply danger color to icon
     *
     * @return this for chaining
     */
    public InfoCard asDanger() {
        icon.getStyleClass().add(Styles.DANGER);
        return this;
    }

    /**
     * Update description text
     *
     * @param newDescription New description
     * @return this for chaining
     */
    public InfoCard updateDescription(String newDescription) {
        descriptionLabel.setText(newDescription);
        return this;
    }
}

