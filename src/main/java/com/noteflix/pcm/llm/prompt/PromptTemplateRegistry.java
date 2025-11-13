package com.noteflix.pcm.llm.prompt;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * Central registry for prompt templates.
 *
 * <p>Features: - Template registration and retrieval - Built-in templates for common use cases -
 * Locale support for i18n - Category-based organization
 *
 * <p>Example usage:
 *
 * <pre>
 * PromptTemplateRegistry registry = PromptTemplateRegistry.getInstance();
 *
 * // Use built-in template
 * String prompt = registry.render("system.default", Map.of());
 *
 * // Register custom template
 * registry.register("my_template", myTemplate);
 * </pre>
 */
@Slf4j
public class PromptTemplateRegistry {

  private static PromptTemplateRegistry instance;

  private final Map<String, PromptTemplate> templates;
  private Locale currentLocale;

  private PromptTemplateRegistry() {
    this.templates = new ConcurrentHashMap<>();
    this.currentLocale = Locale.ENGLISH;
    registerBuiltInTemplates();

    log.info("PromptTemplateRegistry initialized");
  }

  /** Get singleton instance. */
  public static synchronized PromptTemplateRegistry getInstance() {
    if (instance == null) {
      instance = new PromptTemplateRegistry();
    }
    return instance;
  }

  /** Register a template. */
  public void register(String key, PromptTemplate template) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("Template key cannot be null or empty");
    }
    if (template == null) {
      throw new IllegalArgumentException("Template cannot be null");
    }

    templates.put(key, template);
    log.debug("Registered template: {}", key);
  }

  /** Get a template by key. */
  public PromptTemplate get(String key) {
    PromptTemplate template = templates.get(key);
    if (template == null) {
      throw new IllegalArgumentException("Template not found: " + key);
    }
    return template;
  }

  /** Check if template exists. */
  public boolean hasTemplate(String key) {
    return templates.containsKey(key);
  }

  /**
   * Render a template with variables. Convenience method that gets template and renders in one
   * call.
   */
  public String render(String key, Map<String, Object> variables) {
    return get(key).render(variables);
  }

  /** Get all template keys. */
  public Set<String> getTemplateKeys() {
    return new HashSet<>(templates.keySet());
  }

  /** Set current locale for i18n templates. */
  public void setLocale(Locale locale) {
    this.currentLocale = locale;
    log.info("Locale set to: {}", locale);
  }

  /** Get current locale. */
  public Locale getCurrentLocale() {
    return currentLocale;
  }

  /** Register built-in templates. */
  private void registerBuiltInTemplates() {
    // System message templates
    register(
        "system.default",
        SimplePromptTemplate.builder()
            .name("Default System Message")
            .description("Basic helpful assistant")
            .template("You are a helpful AI assistant.")
            .build());

    register(
        "system.with_role",
        SimplePromptTemplate.builder()
            .name("System Message with Role")
            .description("Assistant with specific expertise")
            .template("You are a helpful AI assistant specialized in {role}.")
            .requiredVariables(Set.of("role"))
            .build());

    register(
        "system.with_context",
        SimplePromptTemplate.builder()
            .name("System Message with Context")
            .description("Assistant with role and context")
            .template("You are a {role} expert. Context: {context}")
            .requiredVariables(Set.of("role", "context"))
            .build());

    // Summarization templates
    register(
        "summarize.conversation",
        SimplePromptTemplate.builder()
            .name("Conversation Summarization")
            .description("Summarize a conversation")
            .template(
                """
                Please summarize the following conversation concisely,
                preserving all key information, decisions made, and important context.
                Focus on actionable items and main topics discussed.

                Conversation:
                {messages}
                """)
            .requiredVariables(Set.of("messages"))
            .build());

    register(
        "summarize.tool_result",
        SimplePromptTemplate.builder()
            .name("Tool Result Summarization")
            .description("Summarize tool execution result")
            .template(
                """
                Summarize the following tool execution result concisely,
                keeping only the most relevant information for {purpose}.

                Tool: {tool_name}
                Result:
                {result}
                """)
            .requiredVariables(Set.of("tool_name", "result", "purpose"))
            .build());

    // Function calling templates
    register(
        "function.instruction",
        SimplePromptTemplate.builder()
            .name("Function Calling Instructions")
            .description("Instructions for using functions")
            .template(
                """
                You have access to the following functions:
                {functions}

                When you need to use a function, respond with a function call.
                You can make multiple function calls in sequence if needed.
                """)
            .requiredVariables(Set.of("functions"))
            .build());

    // Thinking mode templates
    register(
        "thinking.instruction",
        SimplePromptTemplate.builder()
            .name("Thinking Mode Instructions")
            .description("Enable step-by-step reasoning")
            .template(
                """
                Think step by step. Show your reasoning process before providing
                the final answer. Consider multiple approaches and evaluate them.
                """)
            .build());

    // Error recovery templates
    register(
        "error.tool_execution",
        SimplePromptTemplate.builder()
            .name("Tool Execution Error")
            .description("Handle tool execution failure")
            .template(
                """
                The function call to '{tool_name}' failed with error: {error_message}

                Please try again with corrected parameters, or suggest an alternative approach.
                """)
            .requiredVariables(Set.of("tool_name", "error_message"))
            .build());

    log.info("Registered {} built-in templates", templates.size());
  }

  /** Unregister a template. */
  public boolean unregister(String key) {
    PromptTemplate removed = templates.remove(key);
    if (removed != null) {
      log.debug("Unregistered template: {}", key);
      return true;
    }
    return false;
  }

  /** Clear all templates (except built-ins). */
  public void clear() {
    templates.clear();
    registerBuiltInTemplates();
    log.info("Templates cleared, built-ins reloaded");
  }
}
