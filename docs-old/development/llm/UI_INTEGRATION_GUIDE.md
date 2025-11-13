# 🎨 LLM UI Integration Guide

> How to integrate CustomAPIProvider & new LLM architecture into your UI

---

## 📋 **Overview**

This guide shows how to integrate the new LLM architecture (with CustomAPIProvider, thinking mode, token tracking) into your JavaFX UI.

---

## 🎯 **Quick Integration Steps**

### 1. **Use AIServiceV2 Instead of AIService**

```java
// Old way (AIService)
private final AIService aiService;

// New way (AIServiceV2) ⭐
private final AIServiceV2 aiServiceV2;

public AIAssistantPage() {
    this.aiServiceV2 = new AIServiceV2(); // Auto-initializes all providers
}
```

### 2. **Setup Monitoring Callbacks**

```java
// Setup callbacks for UI updates
aiServiceV2.setOnThinking(thinking -> {
    Platform.runLater(() -> {
        thinkingLabel.setText("🤔 " + thinking);
        thinkingLabel.setVisible(true);
    });
});

aiServiceV2.setOnTokenUpdate(tokens -> {
    Platform.runLater(() -> {
        tokenLabel.setText("📊 Tokens: " + tokens);
    });
});

aiServiceV2.setOnError(error -> {
    Platform.runLater(() -> {
        errorLabel.setText("❌ " + error);
        errorLabel.setVisible(true);
    });
});
```

### 3. **Update Streaming Code**

```java
// Old way (with StreamingObserver)
aiService.streamResponse(conversation, message, new StreamingObserver() {
    @Override
    public void onChunk(LLMChunk chunk) {
        Platform.runLater(() -> {
            textArea.appendText(chunk.getContent());
        });
    }
    // ...
});

// New way (with ChatEventAdapter) ⭐
aiServiceV2.streamResponse(conversation, message, new ChatEventAdapter() {
    
    @Override
    public void onThinking(String thinking) {
        // NEW! Handle thinking mode
        Platform.runLater(() -> {
            thinkingIndicator.setText(thinking);
            thinkingIndicator.setVisible(true);
        });
    }
    
    @Override
    public void onToken(String token) {
        Platform.runLater(() -> {
            textArea.appendText(token);
            scrollToBottom();
        });
    }
    
    @Override
    public void onComplete(ChatResponse response) {
        Platform.runLater(() -> {
            thinkingIndicator.setVisible(false);
            busyIndicator.setVisible(false);
            
            // NEW! Token tracking
            if (response.getUsage() != null) {
                tokenLabel.setText("Tokens: " + response.getTotalTokens());
            }
            
            // NEW! Remaining tokens (for CustomAPIProvider)
            if (aiServiceV2.getCurrentProvider().equals("custom")) {
                int remaining = aiServiceV2.getRemainingTokens(response.getId());
                remainingLabel.setText("Remaining: " + remaining);
            }
        });
    }
    
    @Override
    public void onError(Throwable error) {
        Platform.runLater(() -> {
            showError(error.getMessage());
        });
    }
});
```

---

## 🎨 **UI Components**

### Add Status Bar with Monitoring

```java
private VBox createStatusBar() {
    VBox statusBar = new VBox(10);
    statusBar.setPadding(new Insets(10));
    statusBar.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd;");
    
    // Provider selector
    HBox providerRow = new HBox(10);
    providerRow.setAlignment(Pos.CENTER_LEFT);
    
    Label providerLabel = new Label("Provider:");
    ComboBox<String> providerSelector = new ComboBox<>();
    providerSelector.getItems().addAll(aiServiceV2.getAvailableProviders());
    providerSelector.setValue(aiServiceV2.getCurrentProvider());
    providerSelector.setOnAction(e -> {
        aiServiceV2.switchProvider(providerSelector.getValue());
    });
    
    providerRow.getChildren().addAll(providerLabel, providerSelector);
    
    // Thinking indicator
    thinkingLabel = new Label();
    thinkingLabel.setGraphic(new FontIcon(Feather.ZAP));
    thinkingLabel.setStyle("-fx-text-fill: orange; -fx-font-style: italic;");
    thinkingLabel.setVisible(false);
    
    // Token tracking
    HBox tokenRow = new HBox(15);
    tokenRow.setAlignment(Pos.CENTER_LEFT);
    
    tokenLabel = new Label("📊 Tokens: 0");
    tokenLabel.setStyle("-fx-text-fill: green;");
    
    remainingLabel = new Label("⏳ Remaining: --");
    remainingLabel.setStyle("-fx-text-fill: blue;");
    
    tokenRow.getChildren().addAll(tokenLabel, remainingLabel);
    
    // Error status
    errorLabel = new Label();
    errorLabel.setGraphic(new FontIcon(Feather.ALERT_CIRCLE));
    errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
    errorLabel.setVisible(false);
    
    statusBar.getChildren().addAll(
        providerRow,
        thinkingLabel,
        tokenRow,
        errorLabel
    );
    
    return statusBar;
}
```

---

## 🔧 **Update AIAssistantPage.java**

### Option 1: Replace AIService with AIServiceV2

```java
// In AIAssistantPage.java

// Change field
-private final AIService aiService;
+private final AIServiceV2 aiServiceV2;

// Change constructor
public AIAssistantPage() {
-   this.aiService = new AIService();
+   this.aiServiceV2 = new AIServiceV2();
    setupCallbacks();
}

// Add callback setup
private void setupCallbacks() {
    aiServiceV2.setOnThinking(thinking -> {
        // Update UI
    });
    
    aiServiceV2.setOnTokenUpdate(tokens -> {
        // Update UI
    });
    
    aiServiceV2.setOnError(error -> {
        // Update UI
    });
}

// Update handleSendMessage() method
private void handleSendMessage() {
    String message = chatInput.getText().trim();
    if (message.isEmpty()) return;
    
    // ... existing code ...
    
    // Change this line
-   aiService.streamResponse(conversation, message, observer);
+   aiServiceV2.streamResponse(conversation, message, new ChatEventAdapter() {
+       @Override
+       public void onThinking(String thinking) {
+           // Handle thinking
+       }
+       
+       @Override
+       public void onToken(String token) {
+           // Handle token
+       }
+       
+       @Override
+       public void onComplete(ChatResponse response) {
+           // Handle complete
+       }
+       
+       @Override
+       public void onError(Throwable error) {
+           // Handle error
+       }
+   });
}
```

### Option 2: Keep Both (Fallback Support)

```java
// Support both old and new
private final AIService aiService;
private final AIServiceV2 aiServiceV2;

public AIAssistantPage() {
    this.aiService = new AIService(); // Fallback
    this.aiServiceV2 = new AIServiceV2(); // New architecture
    
    // Check if new providers available
    if (aiServiceV2.getAvailableProviders().isEmpty()) {
        log.warn("No providers available in AIServiceV2, using AIService fallback");
    }
}

// In message sending
if (!aiServiceV2.getAvailableProviders().isEmpty()) {
    // Use new service
    aiServiceV2.streamResponse(...);
} else {
    // Fallback to old service
    aiService.streamResponse(...);
}
```

---

## 🌟 **UI Features**

### 1. **Thinking Mode Indicator**

```java
// Show when LLM is thinking (reasoning)
private Label thinkingIndicator;

@Override
public void onThinking(String thinking) {
    Platform.runLater(() -> {
        thinkingIndicator.setText("🤔 Thinking: " + thinking);
        thinkingIndicator.setVisible(true);
        thinkingIndicator.setStyle("-fx-background-color: #fff3e0; -fx-padding: 5px;");
    });
}

@Override
public void onComplete(ChatResponse response) {
    Platform.runLater(() -> {
        thinkingIndicator.setVisible(false);
    });
}
```

### 2. **Token Tracking**

```java
// Show token usage
private Label tokenLabel;
private ProgressBar tokenProgress;

@Override
public void onComplete(ChatResponse response) {
    Platform.runLater(() -> {
        if (response.getUsage() != null) {
            int total = response.getTotalTokens();
            tokenLabel.setText(String.format("📊 Tokens: %d", total));
            
            // Update progress bar (assuming 4096 max)
            tokenProgress.setProgress((double) total / 4096.0);
        }
    });
}
```

### 3. **Remaining Tokens (CustomAPIProvider)**

```java
// Show remaining tokens for conversations
private Label remainingLabel;

@Override
public void onComplete(ChatResponse response) {
    Platform.runLater(() -> {
        // Only for custom provider
        if (aiServiceV2.getCurrentProvider().equals("custom")) {
            int remaining = aiServiceV2.getRemainingTokens(response.getId());
            
            if (remaining >= 0) {
                remainingLabel.setText("⏳ Remaining: " + remaining);
                remainingLabel.setVisible(true);
                
                // Warn if low
                if (remaining < 100) {
                    remainingLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    showWarning("⚠️ Low tokens remaining!");
                }
            }
        }
    });
}
```

### 4. **Error Monitoring**

```java
// Show errors with auto-hide
private Label errorLabel;

@Override
public void onError(Throwable error) {
    Platform.runLater(() -> {
        errorLabel.setText("❌ " + error.getMessage());
        errorLabel.setVisible(true);
        
        // Auto-hide after 5 seconds
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(5), e -> {
                errorLabel.setVisible(false);
            })
        );
        timeline.play();
    });
}
```

### 5. **Provider Switching**

```java
// Allow users to switch providers
private ComboBox<String> providerSelector;

private void setupProviderSelector() {
    providerSelector = new ComboBox<>();
    providerSelector.getItems().addAll(aiServiceV2.getAvailableProviders());
    providerSelector.setValue(aiServiceV2.getCurrentProvider());
    
    providerSelector.setOnAction(e -> {
        String selected = providerSelector.getValue();
        if (selected != null) {
            try {
                aiServiceV2.switchProvider(selected);
                showInfo("✅ Switched to: " + selected);
            } catch (Exception ex) {
                showError("Failed to switch provider: " + ex.getMessage());
            }
        }
    });
}
```

---

## 📝 **Complete Example**

See: `src/main/java/com/noteflix/pcm/llm/examples/UIIntegrationExample.java`

```java
public class SimpleAIChatDemo extends Application {
    
    private AIServiceV2 aiService;
    private TextArea chatArea;
    private TextField inputField;
    private Label thinkingLabel;
    private Label tokenLabel;
    
    @Override
    public void start(Stage stage) {
        // Initialize service
        aiService = new AIServiceV2();
        
        // Setup callbacks
        setupCallbacks();
        
        // Create UI
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        
        // Status bar
        thinkingLabel = new Label();
        tokenLabel = new Label();
        
        // Chat area
        chatArea = new TextArea();
        chatArea.setEditable(false);
        VBox.setVgrow(chatArea, Priority.ALWAYS);
        
        // Input
        HBox inputBox = new HBox(10);
        inputField = new TextField();
        HBox.setHgrow(inputField, Priority.ALWAYS);
        Button sendBtn = new Button("Send");
        sendBtn.setOnAction(e -> sendMessage());
        
        inputBox.getChildren().addAll(inputField, sendBtn);
        
        root.getChildren().addAll(
            thinkingLabel,
            tokenLabel,
            chatArea,
            inputBox
        );
        
        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.setTitle("AI Chat Demo");
        stage.show();
    }
    
    private void setupCallbacks() {
        aiService.setOnThinking(thinking -> {
            Platform.runLater(() -> {
                thinkingLabel.setText("🤔 Thinking: " + thinking);
                thinkingLabel.setVisible(true);
            });
        });
        
        aiService.setOnTokenUpdate(tokens -> {
            Platform.runLater(() -> {
                tokenLabel.setText("📊 Tokens: " + tokens);
            });
        });
        
        aiService.setOnError(error -> {
            Platform.runLater(() -> {
                chatArea.appendText("\n❌ Error: " + error + "\n");
            });
        });
    }
    
    private void sendMessage() {
        String message = inputField.getText().trim();
        if (message.isEmpty()) return;
        
        chatArea.appendText("\n👤 You: " + message + "\n");
        inputField.clear();
        inputField.setDisable(true);
        
        // Create simple conversation
        Conversation conv = Conversation.builder()
            .llmProvider("openai")
            .llmModel("gpt-3.5-turbo")
            .build();
        
        // Stream response
        aiService.streamResponse(conv, message, new ChatEventAdapter() {
            private StringBuilder response = new StringBuilder();
            
            @Override
            public void onToken(String token) {
                Platform.runLater(() -> {
                    if (response.length() == 0) {
                        chatArea.appendText("\n🤖 AI: ");
                    }
                    response.append(token);
                    chatArea.appendText(token);
                });
            }
            
            @Override
            public void onComplete(ChatResponse resp) {
                Platform.runLater(() -> {
                    chatArea.appendText("\n\n");
                    inputField.setDisable(false);
                    inputField.requestFocus();
                    thinkingLabel.setVisible(false);
                });
            }
            
            @Override
            public void onError(Throwable error) {
                Platform.runLater(() -> {
                    chatArea.appendText("\n❌ Error: " + error.getMessage() + "\n");
                    inputField.setDisable(false);
                });
            }
        });
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
```

---

## ✅ **Checklist**

Integration checklist:

- [ ] Replace `AIService` with `AIServiceV2`
- [ ] Setup monitoring callbacks (`onThinking`, `onTokenUpdate`, `onError`)
- [ ] Update streaming code to use `ChatEventAdapter`
- [ ] Add thinking indicator UI
- [ ] Add token tracking display
- [ ] Add error status label
- [ ] Add provider selector (optional)
- [ ] Handle remaining tokens for CustomAPIProvider
- [ ] Test with all providers (OpenAI, Anthropic, Ollama, Custom)
- [ ] Test thinking mode (with CustomAPIProvider or o1 models)
- [ ] Test error handling
- [ ] Test token tracking

---

## 🎯 **Benefits**

After integration:

✅ **Thinking Mode** - See AI's reasoning process  
✅ **Token Tracking** - Monitor usage in real-time  
✅ **Error Monitoring** - Better error visibility  
✅ **Multi-Provider** - Easy switching between providers  
✅ **CustomAPIProvider** - Use your own LLM service  
✅ **Event-Driven** - Responsive UI updates  
✅ **Production Ready** - Robust error handling  

---

## 📚 **References**

- **AIServiceV2**: `src/main/java/com/noteflix/pcm/application/service/chat/AIServiceV2.java`
- **CustomAPIProvider**: `src/main/java/com/noteflix/pcm/llm/provider/CustomAPIProvider.java`
- **ChatEventAdapter**: `src/main/java/com/noteflix/pcm/llm/api/ChatEventAdapter.java`
- **Usage Examples**: `src/main/java/com/noteflix/pcm/llm/examples/`

---

*Integration Guide - Last Updated: November 13, 2025*

