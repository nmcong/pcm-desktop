package com.noteflix.pcm.llm.api;

import com.noteflix.pcm.llm.model.ChatResponse;
import com.noteflix.pcm.llm.model.ToolCall;

/**
 * Adapter class for ChatEventListener with no-op implementations.
 * 
 * Extend this class and override only the methods you need,
 * instead of implementing all methods of the interface.
 * 
 * Example usage:
 * <pre>
 * provider.chatStream(messages, options, new ChatEventAdapter() {
 *     &#64;Override
 *     public void onToken(String token) {
 *         // Only handle tokens, ignore other events
 *         System.out.print(token);
 *     }
 * });
 * </pre>
 */
public class ChatEventAdapter implements ChatEventListener {
    
    @Override
    public void onToken(String token) {
        // No-op by default
    }
    
    @Override
    public void onThinking(String thinkingToken) {
        // No-op by default
    }
    
    @Override
    public void onToolCall(ToolCall toolCall) {
        // No-op by default
    }
    
    @Override
    public void onComplete(ChatResponse response) {
        // No-op by default
    }
    
    @Override
    public void onError(Throwable error) {
        // No-op by default
    }
}

