package com.noteflix.pcm.ui.components.common.lists;

import com.noteflix.pcm.ui.styles.ColorConstants;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import lombok.Getter;

/**
 * Avatar List Item Component
 *
 * <p>List item with avatar (initials or image).
 * Useful for user lists, contact lists, etc.
 *
 * <p>Features:
 * - Avatar with initials or image
 * - Color-coded avatar background
 * - Optional status indicator
 * - Title and subtitle
 *
 * <p>Usage:
 * <pre>{@code
 * AvatarListItem userItem = new AvatarListItem("JD", "John Doe")
 *     .withSubtitle("john@example.com")
 *     .withColor(ColorConstants.COLOR_ACCENT)
 *     .withStatus(Status.ONLINE);
 * }</pre>
 *
 * @author PCM Team
 * @version 2.0.0
 */
public class AvatarListItem extends ListItem {

    @Getter
    private final String initials;
    private final StackPane avatarPane;
    private Label initialsLabel;
    private ImageView imageView;
    private Circle statusIndicator;

    /**
     * Create avatar list item with initials
     *
     * @param initials Initials to display (e.g., "JD")
     * @param name     Name for title
     */
    public AvatarListItem(String initials, String name) {
        super();
        this.initials = initials;

        // Create avatar pane
        avatarPane = new StackPane();
        avatarPane.setMinSize(LayoutConstants.AVATAR_SIZE_SM, LayoutConstants.AVATAR_SIZE_SM);
        avatarPane.setMaxSize(LayoutConstants.AVATAR_SIZE_SM, LayoutConstants.AVATAR_SIZE_SM);
        avatarPane.setStyle(
                "-fx-background-color: " + ColorConstants.COLOR_ACCENT + "; " +
                        "-fx-background-radius: " + LayoutConstants.RADIUS_MD + "px;");

        // Create initials label
        initialsLabel = new Label(initials);
        initialsLabel.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: bold;");
        avatarPane.getChildren().add(initialsLabel);

        // Set as leading node
        withLeading(avatarPane);

        // Set title
        withTitle(name);
    }

    /**
     * Create avatar list item with image
     *
     * @param image Avatar image
     * @param name  Name for title
     */
    public AvatarListItem(Image image, String name) {
        this("", name);

        // Remove initials label
        avatarPane.getChildren().remove(initialsLabel);

        // Add image
        imageView = new ImageView(image);
        imageView.setFitWidth(LayoutConstants.AVATAR_SIZE_SM);
        imageView.setFitHeight(LayoutConstants.AVATAR_SIZE_SM);
        imageView.setPreserveRatio(true);

        // Circular clip
        Circle clip = new Circle(LayoutConstants.AVATAR_SIZE_SM / 2);
        imageView.setClip(clip);

        avatarPane.getChildren().add(imageView);
    }

    /**
     * Set avatar color
     *
     * @param colorVar CSS color variable
     * @return this for chaining
     */
    public AvatarListItem withColor(String colorVar) {
        avatarPane.setStyle(
                "-fx-background-color: " + colorVar + "; " +
                        "-fx-background-radius: " + LayoutConstants.RADIUS_MD + "px;");
        return this;
    }

    /**
     * Set avatar size
     *
     * @param size Avatar size in pixels
     * @return this for chaining
     */
    public AvatarListItem withSize(double size) {
        avatarPane.setMinSize(size, size);
        avatarPane.setMaxSize(size, size);

        if (imageView != null) {
            imageView.setFitWidth(size);
            imageView.setFitHeight(size);
        }

        return this;
    }

    /**
     * Set status indicator
     *
     * @param status Status to display
     * @return this for chaining
     */
    public AvatarListItem withStatus(Status status) {
        if (statusIndicator == null) {
            statusIndicator = new Circle(4);
            statusIndicator.setStroke(javafx.scene.paint.Color.WHITE);
            statusIndicator.setStrokeWidth(2);

            StackPane.setAlignment(statusIndicator, Pos.BOTTOM_RIGHT);
            avatarPane.getChildren().add(statusIndicator);
        }

        String fillColor = switch (status) {
            case ONLINE -> "#22c55e";  // Green
            case AWAY -> "#eab308";     // Yellow
            case BUSY -> "#ef4444";     // Red
            case OFFLINE -> "#6b7280";  // Gray
        };

        statusIndicator.setFill(javafx.scene.paint.Color.web(fillColor));
        statusIndicator.setVisible(true);

        return this;
    }

    /**
     * Remove status indicator
     *
     * @return this for chaining
     */
    public AvatarListItem withoutStatus() {
        if (statusIndicator != null) {
            statusIndicator.setVisible(false);
        }
        return this;
    }

    /**
     * Status enum
     */
    public enum Status {
        ONLINE,
        AWAY,
        BUSY,
        OFFLINE
    }
}

