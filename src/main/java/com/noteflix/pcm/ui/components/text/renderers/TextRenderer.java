package com.noteflix.pcm.ui.components.text.renderers;

import com.noteflix.pcm.ui.components.text.TextContentType;
import javafx.scene.Node;

/**
 * Interface for rendering different types of text content.
 * Each renderer is responsible for converting text content into a JavaFX Node
 * that can be displayed in the UI.
 */
public interface TextRenderer {
    
    /**
     * Render the given content as a JavaFX Node
     * 
     * @param content The text content to render
     * @param contentType The type of content being rendered
     * @return A JavaFX Node containing the rendered content
     * @throws RenderException if rendering fails
     */
    Node render(String content, TextContentType contentType) throws RenderException;
    
    /**
     * Check if this renderer supports the given content type
     * 
     * @param contentType The content type to check
     * @return true if this renderer can handle the content type
     */
    boolean supports(TextContentType contentType);
    
    /**
     * Get the display name of this renderer
     * 
     * @return A human-readable name for this renderer
     */
    String getRendererName();
    
    /**
     * Check if this renderer supports live updates
     * (i.e., can re-render content efficiently when text changes)
     * 
     * @return true if live updates are supported
     */
    default boolean supportsLiveUpdate() {
        return false;
    }
    
    /**
     * Update the rendered content with new text (if live update is supported)
     * 
     * @param node The previously rendered node
     * @param newContent The new content to display
     * @param contentType The content type
     * @return The updated node (may be the same instance or a new one)
     * @throws RenderException if update fails
     * @throws UnsupportedOperationException if live update is not supported
     */
    default Node updateContent(Node node, String newContent, TextContentType contentType) 
            throws RenderException {
        if (!supportsLiveUpdate()) {
            throw new UnsupportedOperationException("Live update not supported by " + getRendererName());
        }
        return render(newContent, contentType);
    }
    
    /**
     * Clean up any resources used by the renderer
     */
    default void dispose() {
        // Default implementation does nothing
    }
    
    /**
     * Exception thrown when rendering fails
     */
    class RenderException extends Exception {
        public RenderException(String message) {
            super(message);
        }
        
        public RenderException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}