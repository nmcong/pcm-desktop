package com.noteflix.pcm.llm.examples;

import com.noteflix.pcm.application.service.chat.AIService;
import com.noteflix.pcm.domain.chat.Conversation;
import com.noteflix.pcm.llm.api.ChatEventAdapter;
import com.noteflix.pcm.llm.model.ChatResponse;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Simple UI Integration Example for new LLM Architecture.
 *
 * <p>Demonstrates: - Using AIServiceV2 with all providers - Thinking mode indicator - Token
 * tracking - Error monitoring - Provider switching
 *
 * <p>Run this standalone to test the integration!
 */
@Slf4j
public class UIIntegrationExample extends Application {

    private AIService aiService;

    // UI Components
    private ComboBox<String> providerSelector;
    private TextArea chatArea;
    private TextField inputField;
    private Button sendButton;
    private Label thinkingLabel;
    private Label tokenLabel;
    private Label remainingLabel;
    private Label errorLabel;
    private ProgressIndicator busyIndicator;

    public static void main(String[] args) {
        // Set environment variables for testing
        // export OPENAI_API_KEY=sk-...
        // export ANTHROPIC_API_KEY=sk-...
        // export CUSTOM_LLM_URL=https://your-api.com
        // export CUSTOM_LLM_KEY=your-key

        log.info("Starting LLM UI Integration Demo");
        log.info("Make sure to set API keys in environment variables!");

        launch(args);
    }

    @Override
    public void start(Stage stage) {
        log.info("Starting UI Integration Example");

        // Initialize AI Service (NEW ARCHITECTURE!)
        aiService = new AIService();
        setupCallbacks();

        // Create UI
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // Status Bar
        VBox statusBar = createStatusBar();

        // Chat Area
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPromptText("Chat messages will appear here...");
        VBox.setVgrow(chatArea, Priority.ALWAYS);

        // Input Area
        HBox inputBox = createInputArea();

        root.getChildren().addAll(statusBar, chatArea, inputBox);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("LLM UI Integration Demo - CustomAPIProvider & Thinking Mode");
        stage.show();

        // Welcome message
        chatArea.appendText("🤖 Welcome to LLM Integration Demo!\n\n");
        chatArea.appendText("Available providers: " + aiService.getAvailableProviders() + "\n");
        chatArea.appendText("Current provider: " + aiService.getCurrentProvider() + "\n\n");
        chatArea.appendText("Try asking something! 😊\n\n");
    }

    /**
     * Create status bar with monitoring.
     */
    private VBox createStatusBar() {
        VBox statusBar = new VBox(5);
        statusBar.setPadding(new Insets(10));
        statusBar.setStyle(
                "-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");

        // First row: Provider selector
        HBox firstRow = new HBox(10);
        firstRow.setAlignment(Pos.CENTER_LEFT);

        Label providerLabel = new Label("Provider:");
        providerLabel.setStyle("-fx-font-weight: bold;");

        providerSelector = new ComboBox<>();
        providerSelector.getItems().addAll(aiService.getAvailableProviders());

        if (!aiService.getAvailableProviders().isEmpty()) {
            providerSelector.setValue(aiService.getCurrentProvider());
        }

        providerSelector.setOnAction(
                e -> {
                    String selected = providerSelector.getValue();
                    if (selected != null) {
                        try {
                            aiService.switchProvider(selected);
                            chatArea.appendText("\n✅ Switched to provider: " + selected + "\n\n");
                        } catch (Exception ex) {
                            chatArea.appendText("\n❌ Failed to switch provider: " + ex.getMessage() + "\n\n");
                        }
                    }
                });

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        busyIndicator = new ProgressIndicator();
        busyIndicator.setPrefSize(20, 20);
        busyIndicator.setVisible(false);

        firstRow.getChildren().addAll(providerLabel, providerSelector, spacer1, busyIndicator);

        // Second row: Thinking indicator
        thinkingLabel = new Label();
        thinkingLabel.setGraphic(new FontIcon(Feather.ZAP));
        thinkingLabel.setStyle(
                "-fx-text-fill: #ff9800; -fx-font-style: italic; -fx-background-color: #fff3e0;"
                        + " -fx-padding: 5px;");
        thinkingLabel.setVisible(false);
        thinkingLabel.setManaged(false);

        // Third row: Token tracking & error
        HBox thirdRow = new HBox(15);
        thirdRow.setAlignment(Pos.CENTER_LEFT);

        tokenLabel = new Label("📊 Tokens: 0");
        tokenLabel.setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");

        remainingLabel = new Label();
        remainingLabel.setStyle("-fx-text-fill: #2196f3;");
        remainingLabel.setVisible(false);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        errorLabel = new Label();
        errorLabel.setGraphic(new FontIcon(Feather.ALERT_CIRCLE));
        errorLabel.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        thirdRow.getChildren().addAll(tokenLabel, remainingLabel, spacer2, errorLabel);

        statusBar.getChildren().addAll(firstRow, thinkingLabel, thirdRow);

        return statusBar;
    }

    /**
     * Create input area.
     */
    private HBox createInputArea() {
        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER);

        inputField = new TextField();
        inputField.setPromptText("Type your message here...");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        inputField.setOnAction(e -> sendMessage());

        sendButton = new Button("Send");
        sendButton.setGraphic(new FontIcon(Feather.SEND));
        sendButton.setDefaultButton(true);
        sendButton.setOnAction(e -> sendMessage());

        Button clearButton = new Button("Clear");
        clearButton.setGraphic(new FontIcon(Feather.TRASH_2));
        clearButton.setOnAction(e -> chatArea.clear());

        inputBox.getChildren().addAll(inputField, sendButton, clearButton);

        return inputBox;
    }

    /**
     * Setup monitoring callbacks.
     */
    private void setupCallbacks() {
        // Thinking mode callback
        aiService.setOnThinking(
                thinking -> {
                    Platform.runLater(
                            () -> {
                                if (thinkingLabel != null) {
                                    String display =
                                            thinking.length() > 80 ? thinking.substring(0, 80) + "..." : thinking;
                                    thinkingLabel.setText("🤔 Thinking: " + display);
                                    thinkingLabel.setVisible(true);
                                    thinkingLabel.setManaged(true);
                                }
                            });
                });

        // Token update callback
        aiService.setOnTokenUpdate(
                tokens -> {
                    Platform.runLater(
                            () -> {
                                if (tokenLabel != null) {
                                    tokenLabel.setText("📊 Tokens: " + tokens);
                                }
                            });
                });

        // Error callback
        aiService.setOnError(
                error -> {
                    Platform.runLater(
                            () -> {
                                if (errorLabel != null) {
                                    errorLabel.setText("❌ " + error);
                                    errorLabel.setVisible(true);
                                    errorLabel.setManaged(true);

                                    // Auto-hide after 5 seconds
                                    new Thread(
                                            () -> {
                                                try {
                                                    Thread.sleep(5000);
                                                    Platform.runLater(
                                                            () -> {
                                                                if (errorLabel != null) {
                                                                    errorLabel.setVisible(false);
                                                                    errorLabel.setManaged(false);
                                                                }
                                                            });
                                                } catch (InterruptedException ex) {
                                                    // Ignore
                                                }
                                            })
                                            .start();
                                }
                            });
                });
    }

    /**
     * Send message to AI.
     */
    private void sendMessage() {
        String message = inputField.getText().trim();
        if (message.isEmpty()) return;

        // Append user message
        chatArea.appendText("👤 You: " + message + "\n");

        // Clear input and disable
        inputField.clear();
        inputField.setDisable(true);
        sendButton.setDisable(true);
        busyIndicator.setVisible(true);

        // Hide previous indicators
        if (thinkingLabel != null) {
            thinkingLabel.setVisible(false);
            thinkingLabel.setManaged(false);
        }
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }

        // Create simple conversation
        Conversation conversation =
                Conversation.builder()
                        .id(1L)
                        .title("Demo Chat")
                        .userId("demo-user")
                        .llmProvider(aiService.getCurrentProvider())
                        .llmModel("default")
                        .systemPrompt("You are a helpful AI assistant.")
                        .build();

        // Stream response
        chatArea.appendText("🤖 AI: ");

        aiService.streamResponse(
                conversation,
                message,
                new ChatEventAdapter() {
                    private StringBuilder response = new StringBuilder();

                    @Override
                    public void onThinking(String thinking) {
                        log.debug("Thinking: {}", thinking);
                        // Callback already handles UI update
                    }

                    @Override
                    public void onToken(String token) {
                        Platform.runLater(
                                () -> {
                                    response.append(token);
                                    chatArea.appendText(token);
                                });
                    }

                    @Override
                    public void onComplete(ChatResponse resp) {
                        Platform.runLater(
                                () -> {
                                    chatArea.appendText("\n\n");

                                    // Hide thinking indicator
                                    if (thinkingLabel != null) {
                                        thinkingLabel.setVisible(false);
                                        thinkingLabel.setManaged(false);
                                    }

                                    // Re-enable input
                                    inputField.setDisable(false);
                                    sendButton.setDisable(false);
                                    busyIndicator.setVisible(false);
                                    inputField.requestFocus();

                                    // Check remaining tokens (for custom provider)
                                    if (aiService.getCurrentProvider().equals("custom")) {
                                        int remaining = aiService.getRemainingTokens(resp.getId());
                                        if (remaining >= 0 && remainingLabel != null) {
                                            remainingLabel.setText("⏳ Remaining: " + remaining);
                                            remainingLabel.setVisible(true);

                                            // Warn if low
                                            if (remaining < 100) {
                                                remainingLabel.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                                                chatArea.appendText(
                                                        "⚠️ Warning: Low tokens remaining (" + remaining + ")\n\n");
                                            } else {
                                                remainingLabel.setStyle("-fx-text-fill: #2196f3;");
                                            }
                                        }
                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable error) {
                        Platform.runLater(
                                () -> {
                                    chatArea.appendText("\n❌ Error: " + error.getMessage() + "\n\n");

                                    // Hide thinking
                                    if (thinkingLabel != null) {
                                        thinkingLabel.setVisible(false);
                                        thinkingLabel.setManaged(false);
                                    }

                                    // Re-enable input
                                    inputField.setDisable(false);
                                    sendButton.setDisable(false);
                                    busyIndicator.setVisible(false);
                                });
                    }
                });
    }
}
