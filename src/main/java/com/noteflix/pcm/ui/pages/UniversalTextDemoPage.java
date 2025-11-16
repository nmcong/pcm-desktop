package com.noteflix.pcm.ui.pages;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.ui.base.BaseView;
import com.noteflix.pcm.ui.components.text.TextContentType;
import com.noteflix.pcm.ui.components.text.UniversalTextComponent;
import com.noteflix.pcm.ui.components.text.ViewMode;
import com.noteflix.pcm.ui.styles.LayoutConstants;
import com.noteflix.pcm.ui.styles.StyleConstants;
import com.noteflix.pcm.ui.utils.UIFactory;
import com.noteflix.pcm.ui.utils.LayoutHelper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.octicons.Octicons;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Universal Text Demo page - Modern MVVM Architecture
 * 
 * Demo page for testing the Universal Text Component.
 * Refactored to follow UI_GUIDE.md best practices with:
 * - BaseView template pattern
 * - UI constants and factory methods
 * - Proper component structure
 */
@Slf4j
public class UniversalTextDemoPage extends BaseView {

  private UniversalTextComponent textComponent;
  private ComboBox<SampleContent> sampleSelector;

  public UniversalTextDemoPage() {
    super(
        "Universal Text Demo",
        "Test the Universal Text Component with Markdown, Mermaid, and Code rendering",
        new FontIcon(Octicons.FILE_24));
  }

  @Override
  protected Node createMainContent() {
    VBox content = LayoutHelper.createVBox(LayoutConstants.SPACING_LG);
    content.getStyleClass().add(StyleConstants.PAGE_CONTAINER);
    content.setPadding(LayoutConstants.PADDING_DEFAULT);

    // Demo controls
    HBox controls = createControls();

    // Universal Text Component
    textComponent = new UniversalTextComponent();
    textComponent.setContent(SampleContent.MARKDOWN_SAMPLE.getContent());
    textComponent.setContentType(TextContentType.MARKDOWN);
    textComponent.setViewMode(ViewMode.SPLIT);
    LayoutHelper.setVGrow(textComponent);

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

    content.getChildren().addAll(controls, textComponent);

    return content;
  }

  private HBox createControls() {
    HBox controls = LayoutHelper.createHBox(Pos.CENTER_LEFT, LayoutConstants.SPACING_MD);
    controls.setPadding(LayoutConstants.PADDING_COMPACT);
    controls.getStyleClass().add("demo-controls");

    // Sample content selector
    Label sampleLabel = UIFactory.createBoldLabel("Sample:");

    sampleSelector = new ComboBox<>();
    sampleSelector.getItems().addAll(SampleContent.values());
    sampleSelector.setValue(SampleContent.MARKDOWN_SAMPLE);
    sampleSelector.setOnAction(e -> loadSampleContent());

    // Quick action buttons
    Button loadMarkdownBtn = UIFactory.createSecondaryButton(
        "Markdown", 
        () -> loadSample(SampleContent.MARKDOWN_SAMPLE)
    );

    Button loadCodeBtn = UIFactory.createSecondaryButton(
        "Code", 
        () -> loadSample(SampleContent.CODE_SAMPLE)
    );

    Button loadPlainBtn = UIFactory.createSecondaryButton(
        "Plain Text", 
        () -> loadSample(SampleContent.PLAIN_TEXT_SAMPLE)
    );

    // Mode toggle button
    Button toggleModeBtn = UIFactory.createSecondaryButton(
        "Toggle Mode", 
        () -> {
          if (textComponent != null) {
            textComponent.toggleViewMode();
          }
        }
    );
    toggleModeBtn.setGraphic(new FontIcon(Octicons.SYNC_24));

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
