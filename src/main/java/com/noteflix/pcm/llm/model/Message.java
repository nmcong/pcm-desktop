package com.noteflix.pcm.llm.model;

import lombok.Builder;
import lombok.Data;

/**
 * Chat message in a conversation
 *
 * <p>Represents a single message in the chat history Can be from system, user, assistant, or
 * function
 *
 * <p>Example conversation:
 *
 * <pre>
 * [
 *   Message(role=SYSTEM, content="You are a helpful assistant"),
 *   Message(role=USER, content="What is Java?"),
 *   Message(role=ASSISTANT, content="Java is a programming language...")
 * ]
 * </pre>
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Data
@Builder
public class Message {

  /** Message role */
  private Role role;

  /**
   * Message content (text) For USER and ASSISTANT: the actual message For SYSTEM:
   * instructions/context For FUNCTION: function execution result (JSON)
   */
  private String content;

  /** Function name (only for FUNCTION role) Name of the function that was executed */
  private String name;

  /**
   * Function call details (only for ASSISTANT role with function call) When LLM wants to call a
   * function
   */
  private FunctionCall functionCall;

  /** Create a system message */
  public static Message system(String content) {
    return Message.builder().role(Role.SYSTEM).content(content).build();
  }

  /** Create a user message */
  public static Message user(String content) {
    return Message.builder().role(Role.USER).content(content).build();
  }

  /** Create an assistant message */
  public static Message assistant(String content) {
    return Message.builder().role(Role.ASSISTANT).content(content).build();
  }

  /** Create a function result message */
  public static Message function(String name, String result) {
    return Message.builder().role(Role.FUNCTION).name(name).content(result).build();
  }

  /** Message role */
  public enum Role {
    /**
     * System message (instructions for the LLM) Example: "You are a helpful coding assistant"
     * Usually the first message
     */
    SYSTEM,

    /** User message (from the human) Example: "How do I create a Java class?" */
    USER,

    /**
     * Assistant message (from the LLM) Example: "To create a Java class, use the class keyword..."
     */
    ASSISTANT,

    /** Function message (result of function execution) Used in function calling flow */
    FUNCTION
  }
}
