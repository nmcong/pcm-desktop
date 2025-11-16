package com.noteflix.pcm.core.navigation;

import com.noteflix.pcm.ui.base.BaseView;

/**
 * Interface for listening to navigation events
 * Allows components like SidebarView to respond to page changes
 */
public interface NavigationListener {

    /**
     * Called when navigation to a new page occurs
     *
     * @param previousPage The page that was previously active (can be null)
     * @param currentPage  The page that is now active
     */
    void onNavigationChanged(BaseView previousPage, BaseView currentPage);
}