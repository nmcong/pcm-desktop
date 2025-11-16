package com.noteflix.pcm.ui.styles;

import javafx.geometry.Insets;

/**
 * Layout Constants
 *
 * <p>Centralized constants for spacing, sizing, and layout values.
 * Using constants instead of hard-coded values improves:
 * - Consistency across UI
 * - Easy global adjustments
 * - Better readability
 *
 * @author PCM Team
 * @version 2.0.0
 */
public final class LayoutConstants {

    /**
     * Extra small spacing (4px)
     */
    public static final double SPACING_XS = 4.0;

    // ===== SPACING =====
    /**
     * Small spacing (8px)
     */
    public static final double SPACING_SM = 8.0;
    /**
     * Medium spacing (12px)
     */
    public static final double SPACING_MD = 12.0;
    /**
     * Large spacing (16px)
     */
    public static final double SPACING_LG = 16.0;
    /**
     * Extra large spacing (20px)
     */
    public static final double SPACING_XL = 20.0;
    /**
     * Double extra large spacing (24px)
     */
    public static final double SPACING_XXL = 24.0;
    /**
     * Triple extra large spacing (32px)
     */
    public static final double SPACING_XXXL = 32.0;
    /**
     * Default padding for containers (16px)
     */
    public static final double DEFAULT_PADDING = 16.0;

    // ===== PADDING =====
    /**
     * Card padding (16px)
     */
    public static final double CARD_PADDING = 16.0;
    /**
     * Page padding (24px)
     */
    public static final double PAGE_PADDING = 24.0;
    /**
     * Compact padding (8px)
     */
    public static final double COMPACT_PADDING = 8.0;
    /**
     * Spacious padding (32px)
     */
    public static final double SPACIOUS_PADDING = 32.0;
    /**
     * Default padding insets (16px all sides)
     */
    public static final Insets PADDING_DEFAULT = new Insets(DEFAULT_PADDING);

    // ===== PADDING PRESETS (Insets) =====
    /**
     * Card padding insets (16px all sides)
     */
    public static final Insets PADDING_CARD = new Insets(CARD_PADDING);
    /**
     * Page padding insets (32px top/bottom, 24px left/right)
     */
    public static final Insets PADDING_PAGE = new Insets(SPACING_XXXL, PAGE_PADDING, PAGE_PADDING, PAGE_PADDING);
    /**
     * Compact padding insets (8px all sides)
     */
    public static final Insets PADDING_COMPACT = new Insets(COMPACT_PADDING);
    /**
     * Spacious padding insets (32px all sides)
     */
    public static final Insets PADDING_SPACIOUS = new Insets(SPACIOUS_PADDING);
    /**
     * Sidebar width (280px)
     */
    public static final double SIDEBAR_WIDTH = 280.0;

    // ===== SIZES =====
    /**
     * Icon button size (32px)
     */
    public static final double ICON_BUTTON_SIZE = 32.0;
    /**
     * Avatar size small (32px)
     */
    public static final double AVATAR_SIZE_SM = 32.0;
    /**
     * Avatar size medium (48px)
     */
    public static final double AVATAR_SIZE_MD = 48.0;
    /**
     * Avatar size large (64px)
     */
    public static final double AVATAR_SIZE_LG = 64.0;
    /**
     * Minimum button width (80px)
     */
    public static final double BUTTON_MIN_WIDTH = 80.0;
    /**
     * Default button height (36px)
     */
    public static final double BUTTON_HEIGHT = 36.0;
    /**
     * Large button height (44px)
     */
    public static final double BUTTON_HEIGHT_LG = 44.0;
    /**
     * Small border radius (4px)
     */
    public static final double RADIUS_SM = 4.0;

    // ===== BORDER RADIUS =====
    /**
     * Medium border radius (6px)
     */
    public static final double RADIUS_MD = 6.0;
    /**
     * Large border radius (8px)
     */
    public static final double RADIUS_LG = 8.0;
    /**
     * Extra large border radius (12px)
     */
    public static final double RADIUS_XL = 12.0;
    /**
     * Circle/pill radius (999px - effectively round)
     */
    public static final double RADIUS_ROUND = 999.0;
    /**
     * Small icon size (14px)
     */
    public static final int ICON_SIZE_SM = 14;

    // ===== ICON SIZES =====
    /**
     * Medium icon size (16px)
     */
    public static final int ICON_SIZE_MD = 16;
    /**
     * Large icon size (20px)
     */
    public static final int ICON_SIZE_LG = 20;
    /**
     * Extra large icon size (24px)
     */
    public static final int ICON_SIZE_XL = 24;
    /**
     * Double extra large icon size (32px)
     */
    public static final int ICON_SIZE_XXL = 32;
    /**
     * Default horizontal gap (12px)
     */
    public static final double GAP_HORIZONTAL = 12.0;

    // ===== GAP (for FlowPane, TilePane, etc.) =====
    /**
     * Default vertical gap (12px)
     */
    public static final double GAP_VERTICAL = 12.0;
    /**
     * Small gap (8px)
     */
    public static final double GAP_SM = 8.0;
    /**
     * Large gap (16px)
     */
    public static final double GAP_LG = 16.0;

    private LayoutConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}

