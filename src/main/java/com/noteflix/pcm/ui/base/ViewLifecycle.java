package com.noteflix.pcm.ui.base;

/**
 * Lifecycle interface for Views
 *
 * <p>Defines lifecycle methods that views should implement
 * for proper resource management and state handling.
 *
 * <p>Lifecycle flow:
 * <pre>
 * Constructor → onActivate() → [Active State] → onDeactivate() → cleanup()
 * </pre>
 *
 * @author PCM Team
 * @version 2.0.0
 */
public interface ViewLifecycle {

    /**
     * Called when view becomes active/visible
     * Use this to:
     * - Load data
     * - Start background tasks
     * - Register listeners
     * - Initialize state
     */
    void onActivate();

    /**
     * Called when view becomes inactive/hidden
     * Use this to:
     * - Pause background tasks
     * - Save state
     * - Clear temporary data
     */
    void onDeactivate();

    /**
     * Called when view is being destroyed
     * Use this to:
     * - Unregister listeners
     * - Cancel tasks
     * - Release resources
     * - Prevent memory leaks
     */
    void cleanup();
}

