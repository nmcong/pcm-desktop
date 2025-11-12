package com.noteflix.pcm.ui.viewmodel;

import com.noteflix.pcm.application.service.chat.AIService;
import com.noteflix.pcm.application.service.chat.ConversationService;
import com.noteflix.pcm.domain.chat.Conversation;
import com.noteflix.pcm.domain.chat.Message;
import com.noteflix.pcm.llm.model.LLMChunk;
import com.noteflix.pcm.llm.model.StreamingObserver;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;

/**
 * ViewModel for AI Assistant Page
 *
 * <p>Follows MVVM pattern: - Contains all UI state as Observable Properties - Exposes commands
 * (methods) for UI actions - No direct JavaFX UI components - Business logic coordination (not
 * implementation)
 *
 * <p>Follows SOLID principles: - Single Responsibility: manages AI chat UI state - Dependency
 * Inversion: depends on service interfaces
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class AIAssistantViewModel {

  // Dependencies (injected)
  private final ConversationService conversationService;
  private final AIService aiService;

  // State Properties
  private final StringProperty userInput = new SimpleStringProperty("");
  private final StringProperty searchQuery = new SimpleStringProperty("");
  private final BooleanProperty isBusy = new SimpleBooleanProperty(false);
  private final BooleanProperty isStreaming = new SimpleBooleanProperty(false);
  private final StringProperty errorMessage = new SimpleStringProperty(null);
  private final StringProperty currentConversationTitle = new SimpleStringProperty("New Chat");
  private final ObjectProperty<Long> currentConversationId = new SimpleObjectProperty<>(null);
  private final StringProperty streamingContent = new SimpleStringProperty("");

  // Observable Lists
  private final ObservableList<Conversation> conversations = FXCollections.observableArrayList();
  private final ObservableList<Message> messages = FXCollections.observableArrayList();

  // Current user (TODO: get from auth service)
  private final String currentUserId = "default-user";

  /**
   * Constructor with dependency injection
   *
   * @param conversationService Conversation service
   * @param aiService AI service
   */
  public AIAssistantViewModel(ConversationService conversationService, AIService aiService) {
    this.conversationService = conversationService;
    this.aiService = aiService;
    log.info("AIAssistantViewModel initialized");
  }

  // ========== Commands (Actions) ==========

  /** Load conversations for current user */
  public void loadConversations() {
    try {
      var loadedConversations = conversationService.getUserConversations(currentUserId);
      Platform.runLater(
          () -> {
            conversations.clear();
            conversations.addAll(loadedConversations);
          });
      log.debug("Loaded {} conversations", loadedConversations.size());
    } catch (Exception e) {
      log.error("Failed to load conversations", e);
      setError("Failed to load conversations: " + e.getMessage());
    }
  }

  /** Search conversations */
  public void searchConversations() {
    String query = searchQuery.get();
    if (query == null || query.trim().isEmpty()) {
      loadConversations();
      return;
    }

    try {
      var results = conversationService.searchConversations(currentUserId, query);
      Platform.runLater(
          () -> {
            conversations.clear();
            conversations.addAll(results);
          });
      log.debug("Found {} conversations matching '{}'", results.size(), query);
    } catch (Exception e) {
      log.error("Failed to search conversations", e);
      setError("Search failed: " + e.getMessage());
    }
  }

  /** Create a new conversation */
  public void createNewConversation() {
    currentConversationId.set(null);
    currentConversationTitle.set("New Chat");
    messages.clear();
    userInput.set("");
    streamingContent.set("");
    clearError();
    log.info("Created new conversation");
  }

  /** Switch to an existing conversation */
  public void switchToConversation(Long conversationId) {
    if (conversationId.equals(currentConversationId.get())) {
      return; // Already on this conversation
    }

    currentConversationId.set(conversationId);
    loadMessages(conversationId);

    // Update title
    Optional<Conversation> convOpt = conversationService.getConversation(conversationId);
    convOpt.ifPresent(conv -> currentConversationTitle.set(conv.getTitle()));

    log.info("Switched to conversation: {}", conversationId);
  }

  /** Load messages for a conversation */
  private void loadMessages(Long conversationId) {
    try {
      Optional<Conversation> convOpt =
          conversationService.getConversationWithMessages(conversationId);

      if (convOpt.isPresent()) {
        Conversation conv = convOpt.get();
        Platform.runLater(
            () -> {
              messages.clear();
              messages.addAll(conv.getMessages());
            });
        log.debug(
            "Loaded {} messages for conversation {}", conv.getMessages().size(), conversationId);
      }
    } catch (Exception e) {
      log.error("Failed to load messages", e);
      setError("Failed to load messages: " + e.getMessage());
    }
  }

  /** Send a message */
  public void sendMessage(Runnable onComplete) {
    String message = userInput.get();
    if (message == null || message.trim().isEmpty()) {
      log.warn("Cannot send empty message");
      return;
    }

    // Create conversation if needed
    if (currentConversationId.get() == null) {
      try {
        Conversation conv =
            conversationService.createConversation(
                "New Chat", currentUserId, "openai", "gpt-3.5-turbo");
        currentConversationId.set(conv.getId());
        currentConversationTitle.set(conv.getTitle());
      } catch (Exception e) {
        log.error("Failed to create conversation", e);
        setError("Failed to create conversation: " + e.getMessage());
        return;
      }
    }

    // Add user message to UI
    Message userMsg = Message.user(currentConversationId.get(), message);
    messages.add(userMsg);

    // Clear input and set busy state
    userInput.set("");
    isBusy.set(true);
    isStreaming.set(true);
    streamingContent.set("");
    clearError();

    // Get conversation
    Optional<Conversation> convOpt =
        conversationService.getConversation(currentConversationId.get());

    if (convOpt.isEmpty()) {
      log.error("Conversation not found: {}", currentConversationId.get());
      setError("Conversation not found");
      isBusy.set(false);
      isStreaming.set(false);
      return;
    }

    Conversation conversation = convOpt.get();

    // Stream AI response
    aiService.streamResponse(
        conversation,
        message,
        new StreamingObserver() {
          private final StringBuilder fullResponse = new StringBuilder();

          @Override
          public void onChunk(LLMChunk chunk) {
            Platform.runLater(
                () -> {
                  fullResponse.append(chunk.getContent());
                  streamingContent.set(fullResponse.toString());
                });
          }

          @Override
          public void onComplete() {
            Platform.runLater(
                () -> {
                  // Reload messages to get the persisted AI response
                  loadMessages(currentConversationId.get());
                  streamingContent.set("");
                  isBusy.set(false);
                  isStreaming.set(false);
                  loadConversations(); // Refresh sidebar
                  if (onComplete != null) {
                    onComplete.run();
                  }
                });
          }

          @Override
          public void onError(Throwable error) {
            Platform.runLater(
                () -> {
                  setError("AI response failed: " + error.getMessage());
                  streamingContent.set("");
                  isBusy.set(false);
                  isStreaming.set(false);
                });
          }
        });

    log.info("Sent message to conversation: {}", currentConversationId.get());
  }

  /** Clear current conversation */
  public void clearConversation() {
    Long convId = currentConversationId.get();
    if (convId != null) {
      try {
        conversationService.clearConversation(convId);
        messages.clear();
        log.info("Cleared conversation: {}", convId);
      } catch (Exception e) {
        log.error("Failed to clear conversation", e);
        setError("Failed to clear conversation: " + e.getMessage());
      }
    }
  }

  /** Delete current conversation */
  public void deleteConversation() {
    Long convId = currentConversationId.get();
    if (convId != null) {
      try {
        conversationService.deleteConversation(convId);
        createNewConversation();
        loadConversations();
        log.info("Deleted conversation: {}", convId);
      } catch (Exception e) {
        log.error("Failed to delete conversation", e);
        setError("Failed to delete conversation: " + e.getMessage());
      }
    }
  }

  // ========== Property Accessors ==========

  public StringProperty userInputProperty() {
    return userInput;
  }

  public String getUserInput() {
    return userInput.get();
  }

  public void setUserInput(String input) {
    userInput.set(input);
  }

  public StringProperty searchQueryProperty() {
    return searchQuery;
  }

  public String getSearchQuery() {
    return searchQuery.get();
  }

  public void setSearchQuery(String query) {
    searchQuery.set(query);
  }

  public BooleanProperty isBusyProperty() {
    return isBusy;
  }

  public boolean isBusy() {
    return isBusy.get();
  }

  public BooleanProperty isStreamingProperty() {
    return isStreaming;
  }

  public boolean isStreaming() {
    return isStreaming.get();
  }

  public StringProperty errorMessageProperty() {
    return errorMessage;
  }

  public String getErrorMessage() {
    return errorMessage.get();
  }

  private void setError(String error) {
    errorMessage.set(error);
  }

  private void clearError() {
    errorMessage.set(null);
  }

  public StringProperty currentConversationTitleProperty() {
    return currentConversationTitle;
  }

  public String getCurrentConversationTitle() {
    return currentConversationTitle.get();
  }

  public ObjectProperty<Long> currentConversationIdProperty() {
    return currentConversationId;
  }

  public Long getCurrentConversationId() {
    return currentConversationId.get();
  }

  public StringProperty streamingContentProperty() {
    return streamingContent;
  }

  public String getStreamingContent() {
    return streamingContent.get();
  }

  public ObservableList<Conversation> getConversations() {
    return conversations;
  }

  public ObservableList<Message> getMessages() {
    return messages;
  }

  // ========== Lifecycle ==========

  /** Called when ViewModel is activated (page shown) */
  public void onActivate() {
    loadConversations();
    log.debug("AIAssistantViewModel activated");
  }

  /** Called when ViewModel is deactivated (page hidden) */
  public void onDeactivate() {
    clearError();
    log.debug("AIAssistantViewModel deactivated");
  }
}
