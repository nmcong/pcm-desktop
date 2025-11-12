package com.noteflix.pcm.domain.chat;

/**
 * Role of message sender in a conversation
 *
 * @author PCM Team
 * @version 1.0.0
 */
public enum MessageRole {
  /** System message (instructions, context) */
  SYSTEM,

  /** User message (from human) */
  USER,

  /** Assistant message (from AI) */
  ASSISTANT,

  /** Function result message (from function calling) */
  FUNCTION
}
