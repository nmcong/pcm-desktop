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

  /** Function name (only for TOOL role) Name of the function that was executed */
  private String name;
  
  /**
   * Tool calls (new architecture - supports multiple tool calls)
   * Used when ASSISTANT wants to call multiple tools in sequence
   */
  private java.util.List<ToolCall> toolCalls;
  
  /**
   * Tool call ID (for TOOL role messages)
   * Links tool result back to the specific tool call
   */
  private String toolCallId;

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
  
  /** 
   * Create a tool result message
   * 
   * @param toolCallId ID of the tool call this is responding to
   * @param result Result of tool execution
   */
  public static Message tool(String toolCallId, String result) {
    return Message.builder()
        .role(Role.TOOL)
        .toolCallId(toolCallId)
        .content(result)
        .build();
  }
  
  /** 
   * Create an assistant message with tool calls
   * 
   * @param toolCalls List of tool calls the assistant wants to make
   */
  public static Message assistantWithTools(java.util.List<ToolCall> toolCalls) {
    return Message.builder()
        .role(Role.ASSISTANT)
        .toolCalls(toolCalls)
        .build();
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
    
    /**
     * Tool message (result of tool execution)
     * Links back to a specific tool call via toolCallId
     */
    TOOL
  }
}
