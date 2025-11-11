package com.noteflix.pcm.ui.components.text.renderers;

import com.noteflix.pcm.ui.components.text.TextContentType;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Simple renderer for plain text content.
 * 
 * This renderer displays plain text in a scrollable area with proper formatting
 * and monospace font for code-like content.
 */
public class PlainTextRenderer implements TextRenderer {
    
    private boolean useMonospace = false;
    private double fontSize = 12.0;
    
    public PlainTextRenderer() {
        // Default constructor
    }
    
    @Override
    public Node render(String content, TextContentType contentType) throws RenderException {
        if (content == null) {
            content = "";
        }
        
        try {
            VBox container = new VBox();
            container.getStyleClass().add("plain-text-container");
            container.setPadding(new Insets(10));
            
            // Split content into lines and create labels
            String[] lines = content.split("\n");
            
            for (String line : lines) {
                Label lineLabel = new Label(line.isEmpty() ? " " : line); // Use space for empty lines
                lineLabel.getStyleClass().add("plain-text-line");
                lineLabel.setWrapText(true);
                lineLabel.setMaxWidth(Double.MAX_VALUE);
                
                // Apply font
                if (useMonospace) {
                    lineLabel.setFont(Font.font("Consolas", fontSize));
                } else {
                    lineLabel.setFont(Font.font("System", FontWeight.NORMAL, fontSize));
                }
                
                container.getChildren().add(lineLabel);
            }
            
            // If no content, show placeholder
            if (lines.length == 0 || (lines.length == 1 && lines[0].trim().isEmpty())) {
                Label placeholder = new Label("No content to display");
                placeholder.getStyleClass().addAll("plain-text-placeholder", "text-muted");
                placeholder.setFont(Font.font("System", FontWeight.NORMAL, fontSize));
                container.getChildren().add(placeholder);
            }
            
            // Wrap in ScrollPane
            ScrollPane scrollPane = new ScrollPane(container);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.getStyleClass().add("plain-text-scroll");
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            
            return scrollPane;
            
        } catch (Exception e) {
            throw new RenderException("Failed to render plain text content", e);
        }
    }
    
    @Override
    public boolean supports(TextContentType contentType) {
        return contentType == TextContentType.PLAIN_TEXT;
    }
    
    @Override
    public String getRendererName() {
        return "Plain Text Renderer";
    }
    
    @Override
    public boolean supportsLiveUpdate() {
        return true;
    }
    
    @Override
    public Node updateContent(Node node, String newContent, TextContentType contentType) 
            throws RenderException {
        // For plain text, it's easier to re-render than to update in place
        return render(newContent, contentType);
    }
    
    /**
     * Set whether to use monospace font
     * 
     * @param useMonospace true to use monospace font, false for system font
     */
    public void setUseMonospace(boolean useMonospace) {
        this.useMonospace = useMonospace;
    }
    
    /**
     * Check if monospace font is being used
     */
    public boolean isUseMonospace() {
        return useMonospace;
    }
    
    /**
     * Set the font size
     * 
     * @param fontSize the font size in points
     */
    public void setFontSize(double fontSize) {
        this.fontSize = Math.max(8.0, Math.min(72.0, fontSize)); // Clamp between 8 and 72
    }
    
    /**
     * Get the current font size
     */
    public double getFontSize() {
        return fontSize;
    }
    
    /**
     * Create a renderer optimized for code display (monospace, smaller font)
     */
    public static PlainTextRenderer createCodeRenderer() {
        PlainTextRenderer renderer = new PlainTextRenderer();
        renderer.setUseMonospace(true);
        renderer.setFontSize(11.0);
        return renderer;
    }
    
    /**
     * Create a renderer optimized for text display (system font, larger font)
     */
    public static PlainTextRenderer createTextRenderer() {
        PlainTextRenderer renderer = new PlainTextRenderer();
        renderer.setUseMonospace(false);
        renderer.setFontSize(13.0);
        return renderer;
    }
}