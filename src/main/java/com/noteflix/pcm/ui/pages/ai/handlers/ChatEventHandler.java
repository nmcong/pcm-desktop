package com.noteflix.pcm.ui.pages.ai.handlers;

/**
 * Interface for handling chat-related events
 */
public interface ChatEventHandler {
    void onNewChat();

    void onClearChat();

    void onSendMessage(String message);

    void onConversationSelected(Long conversationId);

    void onSearch(String query);
}

