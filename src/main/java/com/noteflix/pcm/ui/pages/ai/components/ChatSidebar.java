package com.noteflix.pcm.ui.pages.ai.components;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.domain.chat.Conversation;
import com.noteflix.pcm.ui.components.common.buttons.PrimaryButton;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import com.noteflix.pcm.ui.styles.StyleConstants;
import com.noteflix.pcm.ui.utils.LayoutHelper;
import com.noteflix.pcm.ui.utils.UIFactory;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import org.kordamp.ikonli.octicons.Octicons;

import java.util.function.Consumer;

/**
 * Chat Sidebar Component for AI Assistant
 *
 * <p>Contains:
 * - New chat button
 * - Search box
 * - Conversation history list
 *
 * @author PCM Team
 * @version 2.0.0
 */
public class ChatSidebar extends VBox {

    @Getter
    private final TextField searchField;
    @Getter
    private final VBox conversationsList;
    private final ScrollPane scrollPane;

    private Consumer<String> onSearch;
    private Runnable onNewChat;

    /**
     * Create chat sidebar
     */
    public ChatSidebar() {
        super(LayoutConstants.SPACING_MD);
        getStyleClass().add(StyleConstants.CHAT_SIDEBAR);
        setPadding(LayoutConstants.PADDING_DEFAULT);

        // Fixed width
        setPrefWidth(LayoutConstants.SIDEBAR_WIDTH);
        setMinWidth(LayoutConstants.SIDEBAR_WIDTH);
        setMaxWidth(LayoutConstants.SIDEBAR_WIDTH);

        // Header with new chat button
        HBox header = createHeader();

        // Search box
        searchField = new TextField();
        searchField.setPromptText("Search conversations...");
        searchField.getStyleClass().add(StyleConstants.SEARCH_INPUT);
        searchField.textProperty().addListener((obs, old, newVal) -> {
            if (onSearch != null) {
                onSearch.accept(newVal);
            }
        });

        // History label
        Label historyLabel = UIFactory.createSectionTitle("Chat History");
        historyLabel.getStyleClass().add(Styles.TEXT_BOLD);

        // Conversations list
        conversationsList = LayoutHelper.createVBox(LayoutConstants.SPACING_SM);
        conversationsList.getStyleClass().add(StyleConstants.CHAT_SESSIONS_LIST);

        // Scrollable list
        scrollPane = new ScrollPane(conversationsList);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("content-scroll");
        LayoutHelper.setVGrow(scrollPane);

        getChildren().addAll(header, searchField, historyLabel, scrollPane);
    }

    /**
     * Create header with new chat button
     */
    private HBox createHeader() {
        HBox header = LayoutHelper.createHBox(
                Pos.CENTER_LEFT,
                LayoutConstants.SPACING_SM
        );

        Label title = UIFactory.createTitleLabel("Conversations");

        PrimaryButton newChatBtn = new PrimaryButton("New Chat", Octicons.PLUS_16)
                .withAction(() -> {
                    if (onNewChat != null) {
                        onNewChat.run();
                    }
                });
        newChatBtn.getStyleClass().add(Styles.SMALL);

        header.getChildren().addAll(
                title,
                UIFactory.createHorizontalSpacer(),
                newChatBtn
        );

        return header;
    }

    /**
     * Set search handler
     *
     * @param handler Search handler
     * @return this for chaining
     */
    public ChatSidebar withSearchHandler(Consumer<String> handler) {
        this.onSearch = handler;
        return this;
    }

    /**
     * Set new chat handler
     *
     * @param handler New chat handler
     * @return this for chaining
     */
    public ChatSidebar withNewChatHandler(Runnable handler) {
        this.onNewChat = handler;
        return this;
    }

    /**
     * Set conversations list
     *
     * @param conversations Observable list of conversations
     * @return this for chaining
     */
    public ChatSidebar withConversations(ObservableList<Conversation> conversations) {
        // This would typically bind to the list
        // For now, we'll let the page manage the list items
        return this;
    }

    /**
     * Clear conversations list
     */
    public void clearConversations() {
        conversationsList.getChildren().clear();
    }

    /**
     * Add conversation item
     *
     * @param item Conversation item component
     */
    public void addConversation(ConversationItem item) {
        conversationsList.getChildren().add(item);
    }
}

