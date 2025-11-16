package com.noteflix.pcm.ui.pages;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.ui.base.BaseView;
import com.noteflix.pcm.ui.components.text.TextContentType;
import com.noteflix.pcm.ui.components.text.UniversalTextComponent;
import com.noteflix.pcm.ui.components.text.ViewMode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/** Demo page for testing the Universal Text Component */
@Slf4j
public class UniversalTextDemoPage extends BaseView {

  private UniversalTextComponent textComponent;
  private ComboBox<SampleContent> sampleSelector;

  public UniversalTextDemoPage() {
    super(
        "Universal Text Demo",
        "Test the Universal Text Component with Markdown, Mermaid, and Code rendering",
        new FontIcon(Feather.FILE_TEXT));
  }

  @Override
  protected VBox createMainContent() {
    VBox content = new VBox(16);
    content.setPadding(new Insets(20));

    // Demo controls
    HBox controls = createControls();

    // Universal Text Component
    textComponent = new UniversalTextComponent();
    textComponent.setContent(SampleContent.MARKDOWN_SAMPLE.getContent());
    textComponent.setContentType(TextContentType.MARKDOWN);
    textComponent.setViewMode(ViewMode.SPLIT);

    // Setup callbacks
    textComponent.setOnContentChanged(
        newContent -> {
          log.info("Content changed: {} characters", newContent.length());
        });

    textComponent.setOnContentTypeChanged(
        newType -> {
          log.info("Content type changed to: {}", newType);
        });

    textComponent.setOnViewModeChanged(
        newMode -> {
          log.info("View mode changed to: {}", newMode);
        });

    VBox.setVgrow(textComponent, Priority.ALWAYS);

    content.getChildren().addAll(controls, textComponent);

    return content;
  }

  private HBox createControls() {
    HBox controls = new HBox(12);
    controls.setAlignment(Pos.CENTER_LEFT);
    controls.setPadding(new Insets(10));
    controls.getStyleClass().add("demo-controls");

    // Sample content selector
    Label sampleLabel = new Label("Sample:");
    sampleLabel.getStyleClass().add(Styles.TEXT_BOLD);

    sampleSelector = new ComboBox<>();
    sampleSelector.getItems().addAll(SampleContent.values());
    sampleSelector.setValue(SampleContent.MARKDOWN_SAMPLE);
    sampleSelector.setOnAction(e -> loadSampleContent());

    // Quick action buttons
    Button loadMarkdownBtn = new Button("Markdown");
    loadMarkdownBtn.setOnAction(e -> loadSample(SampleContent.MARKDOWN_SAMPLE));

    Button loadCodeBtn = new Button("Code");
    loadCodeBtn.setOnAction(e -> loadSample(SampleContent.CODE_SAMPLE));

    Button loadPlainBtn = new Button("Plain Text");
    loadPlainBtn.setOnAction(e -> loadSample(SampleContent.PLAIN_TEXT_SAMPLE));

    // Mode toggle button
    Button toggleModeBtn = new Button("Toggle Mode");
    toggleModeBtn.setGraphic(new FontIcon(Feather.REFRESH_CCW));
    toggleModeBtn.setOnAction(e -> textComponent.toggleViewMode());

    controls
        .getChildren()
        .addAll(
            sampleLabel,
            sampleSelector,
            new Separator(),
            loadMarkdownBtn,
            loadCodeBtn,
            loadPlainBtn,
            new Separator(),
            toggleModeBtn);

    return controls;
  }

  private void loadSampleContent() {
    SampleContent selected = sampleSelector.getValue();
    if (selected != null) {
      loadSample(selected);
    }
  }

  private void loadSample(SampleContent sample) {
    if (textComponent != null && sample != null) {
      textComponent.setContent(sample.getContent());
      textComponent.setContentType(sample.getContentType());
      sampleSelector.setValue(sample);
    }
  }

  @Override
  public void onActivate() {
    super.onActivate();
    if (textComponent != null) {
      textComponent.focusEditor();
    }
  }

  // Sample content enum
  public enum SampleContent {
    MARKDOWN_SAMPLE(
        "Markdown Sample",
        TextContentType.MARKDOWN,
        """
        # Welcome to Universal Text Component

        This is a **powerful** text component that supports multiple formats.

        ## Features

        - ✅ **Markdown** rendering with *live preview*
        - ✅ **Mermaid** diagram support
        - ✅ **Syntax highlighting** for code
        - ✅ **Three view modes**: View, Edit, Split
        - ✅ **Dark/Light** theme support

        ## Code Example

        ```java
        public class HelloWorld {
            public static void main(String[] args) {
                System.out.println("Hello, Universal Text Component!");
            }
        }
        ```

        ## Lists

        ### Todo List
        - [x] Implement markdown renderer
        - [x] Create base architecture
        - [ ] Add mermaid support
        - [ ] Add syntax highlighting
        - [ ] Add export functionality

        ### Shopping List
        1. Apples 🍎
        2. Bananas 🍌
        3. Coffee ☕
        4. Chocolate 🍫

        ## Links and Images

        Visit [GitHub](https://github.com) for more projects.

        ## Tables

        | Feature | Status | Priority |
        |---------|--------|----------|
        | Markdown | ✅ Done | High |
        | Mermaid | 🚧 In Progress | High |
        | Code | ⏳ Planned | Medium |
        | Export | ⏳ Planned | Low |

        > This is a blockquote with some important information.
        > It can span multiple lines and look great!

        ---

        **Happy coding!** 🚀
        """),

    CODE_SAMPLE(
        "Java Code Sample",
        TextContentType.CODE,
        """
        package com.example.demo;

        import javafx.application.Application;
        import javafx.scene.Scene;
        import javafx.scene.control.Label;
        import javafx.scene.layout.StackPane;
        import javafx.stage.Stage;

        /**
         * A simple JavaFX application demonstrating the Universal Text Component.
         * This class shows how to create a basic window with text content.
         */
        public class DemoApplication extends Application {

            @Override
            public void start(Stage primaryStage) {
                // Create the root pane
                StackPane root = new StackPane();

                // Create and configure the label
                Label welcomeLabel = new Label("Welcome to Universal Text Component!");
                welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

                // Add the label to the root pane
                root.getChildren().add(welcomeLabel);

                // Create and configure the scene
                Scene scene = new Scene(root, 800, 600);
                scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

                // Configure and show the stage
                primaryStage.setTitle("Universal Text Component Demo");
                primaryStage.setScene(scene);
                primaryStage.show();

                // Log application start
                System.out.println("Application started successfully!");
            }

            public static void main(String[] args) {
                launch(args);
            }

            /**
             * Helper method to demonstrate code formatting
             */
            private void setupEventHandlers(Stage stage) {
                stage.setOnCloseRequest(event -> {
                    System.out.println("Application is closing...");
                    // Perform cleanup here
                });
            }
        }
        """),

    PLAIN_TEXT_SAMPLE(
        "Plain Text Sample",
        TextContentType.PLAIN_TEXT,
        """
        This is a plain text sample.

        It contains multiple lines of text without any special formatting.
        No markdown, no code highlighting, just plain simple text.

        Here are some simple facts:

        - The Universal Text Component supports multiple content types
        - It has three different view modes
        - You can switch between them easily
        - It supports both light and dark themes

        This text will be displayed using a simple text renderer.
        Perfect for notes, logs, or any plain text content.

        Line 1: First line
        Line 2: Second line
        Line 3: Third line

        End of plain text sample.
        """),

    MERMAID_SAMPLE(
        "Mermaid Diagram",
        TextContentType.MERMAID,
        """
        graph TD
            A[Start] --> B{Is it working?}
            B -->|Yes| C[Great!]
            B -->|No| D[Debug]
            D --> E[Fix the issue]
            E --> B
            C --> F[Deploy]
            F --> G[End]

            style A fill:#e1f5fe
            style C fill:#e8f5e8
            style F fill:#fff3e0
            style G fill:#fce4ec
        """);

    private final String displayName;
    private final TextContentType contentType;
    private final String content;

    SampleContent(String displayName, TextContentType contentType, String content) {
      this.displayName = displayName;
      this.contentType = contentType;
      this.content = content;
    }

    public String getDisplayName() {
      return displayName;
    }

    public TextContentType getContentType() {
      return contentType;
    }

    public String getContent() {
      return content;
    }

    @Override
    public String toString() {
      return displayName;
    }
  }
}
