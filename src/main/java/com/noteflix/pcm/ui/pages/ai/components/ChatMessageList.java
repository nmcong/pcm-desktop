package com.noteflix.pcm.ui.pages.ai.components;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.domain.chat.Message;
import com.noteflix.pcm.domain.chat.MessageRole;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import com.noteflix.pcm.ui.utils.LayoutHelper;
import com.noteflix.pcm.ui.utils.UIFactory;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Getter;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.format.DateTimeFormatter;

/**
 * Chat Message List Component
 *
 * <p>Displays list of chat messages with auto-scroll.
 *
 * @author PCM Team
 * @version 2.0.0
 */
public class ChatMessageList extends VBox {

  private static final DateTimeFormatter TIME_FORMATTER = 
      DateTimeFormatter.ofPattern("HH:mm");

  @Getter private final VBox messagesContainer;
  private final ScrollPane scrollPane;
  private Label streamingLabel;

  /**
   * Create chat message list
   */
  public ChatMessageList() {
    super();
    getStyleClass().add("chat-messages-area");
    LayoutHelper.setVGrow(this);

    // Messages container
    messagesContainer = LayoutHelper.createVBox(LayoutConstants.SPACING_MD);
    messagesContainer.setPadding(LayoutConstants.PADDING_DEFAULT);

    // Scroll pane
    scrollPane = new ScrollPane(messagesContainer);
    scrollPane.setFitToWidth(true);
    scrollPane.getStyleClass().add("chat-scroll");
    LayoutHelper.setVGrow(scrollPane);

    getChildren().add(scrollPane);
  }

  /**
   * Bind to messages observable list
   *
   * @param messages Observable list of messages
   * @return this for chaining
   */
  public ChatMessageList withMessages(ObservableList<Message> messages) {
    // Listen for changes
    messages.addListener((ListChangeListener<Message>) c -> {
      while (c.next()) {
        if (c.wasAdded()) {
          c.getAddedSubList().forEach(this::addMessage);
        }
        if (c.wasRemoved()) {
          // Handle removal if needed
        }
      }
    });

    // Add existing messages
    messages.forEach(this::addMessage);

    return this;
  }

  /**
   * Add message to list
   *
   * @param message Message to add
   */
  public void addMessage(Message message) {
    VBox messageBox = createMessageBox(message);
    messagesContainer.getChildren().add(messageBox);
    scrollToBottom();
  }

  /**
   * Create message box
   */
  private VBox createMessageBox(Message message) {
    VBox box = LayoutHelper.createVBox(LayoutConstants.SPACING_XS);
    box.getStyleClass().add("message-box");

    // Message header (role + timestamp)
    HBox header = LayoutHelper.createHBox(
        Pos.CENTER_LEFT,
        LayoutConstants.SPACING_SM
    );

    boolean isUser = message.getRole() == MessageRole.USER;
    
    FontIcon icon = new FontIcon(
        isUser ? Feather.USER : Feather.CPU
    );
    icon.setIconSize(LayoutConstants.ICON_SIZE_SM);

    Label roleLabel = new Label(isUser ? "You" : "AI Assistant");
    roleLabel.getStyleClass().addAll(Styles.TEXT_BOLD, Styles.TEXT_SMALL);

    Label timeLabel = UIFactory.createMutedLabel(
        message.getCreatedAt().format(TIME_FORMATTER)
    );
    timeLabel.getStyleClass().add(Styles.TEXT_SMALL);

    header.getChildren().addAll(
        icon,
        roleLabel,
        UIFactory.createHorizontalSpacer(),
        timeLabel
    );

    // Message content
    Label contentLabel = new Label(message.getContent());
    contentLabel.setWrapText(true);
    contentLabel.getStyleClass().add("message-content");

    if (isUser) {
      box.getStyleClass().add("user-message");
      box.setAlignment(Pos.CENTER_RIGHT);
    } else {
      box.getStyleClass().add("assistant-message");
      box.setAlignment(Pos.CENTER_LEFT);
    }

    box.getChildren().addAll(header, contentLabel);
    return box;
  }

  /**
   * Show streaming message (AI is typing)
   *
   * @param content Current streaming content
   */
  public void showStreaming(String content) {
    if (streamingLabel == null) {
      VBox streamingBox = LayoutHelper.createVBox(LayoutConstants.SPACING_XS);
      streamingBox.getStyleClass().addAll("message-box", "assistant-message", "streaming");

      HBox header = LayoutHelper.createHBox(Pos.CENTER_LEFT, LayoutConstants.SPACING_SM);
      FontIcon icon = new FontIcon(Feather.CPU);
      icon.setIconSize(LayoutConstants.ICON_SIZE_SM);
      Label roleLabel = new Label("AI Assistant");
      roleLabel.getStyleClass().addAll(Styles.TEXT_BOLD, Styles.TEXT_SMALL);
      header.getChildren().addAll(icon, roleLabel);

      streamingLabel = new Label();
      streamingLabel.setWrapText(true);
      streamingLabel.getStyleClass().add("message-content");

      streamingBox.getChildren().addAll(header, streamingLabel);
      messagesContainer.getChildren().add(streamingBox);
    }

    streamingLabel.setText(content);
    scrollToBottom();
  }

  /**
   * Hide streaming indicator
   */
  public void hideStreaming() {
    if (streamingLabel != null && streamingLabel.getParent() != null) {
      messagesContainer.getChildren().remove(streamingLabel.getParent());
      streamingLabel = null;
    }
  }

  /**
   * Clear all messages
   */
  public void clearMessages() {
    messagesContainer.getChildren().clear();
    streamingLabel = null;
  }

  /**
   * Scroll to bottom
   */
  private void scrollToBottom() {
    scrollPane.setVvalue(1.0);
  }

  /**
   * Show welcome message
   */
  public void showWelcome() {
    VBox welcome = LayoutHelper.createVBox(LayoutConstants.SPACING_XL);
    welcome.setAlignment(Pos.CENTER);
    welcome.setPadding(LayoutConstants.PADDING_SPACIOUS);

    FontIcon icon = new FontIcon(Feather.MESSAGE_CIRCLE);
    icon.setIconSize(64);
    icon.getStyleClass().add("welcome-icon");

    Label title = UIFactory.createTitleLabel("Welcome to AI Assistant");
    title.setAlignment(Pos.CENTER);

    Label subtitle = UIFactory.createMutedLabel(
        "Start a conversation or select a previous chat from the sidebar"
    );
    subtitle.setAlignment(Pos.CENTER);
    subtitle.setMaxWidth(400);
    subtitle.setWrapText(true);

    welcome.getChildren().addAll(icon, title, subtitle);
    messagesContainer.getChildren().add(welcome);
  }
}

