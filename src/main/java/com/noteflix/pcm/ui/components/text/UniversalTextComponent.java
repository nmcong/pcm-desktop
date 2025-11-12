package com.noteflix.pcm.ui.components.text;

import com.noteflix.pcm.ui.components.text.renderers.PlainTextRenderer;
import com.noteflix.pcm.ui.components.text.renderers.TextRenderer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Universal Text Component that supports multiple content types and viewing modes.
 *
 * <p>This component can render and edit: - Markdown with live preview - Mermaid diagrams - Source
 * code with syntax highlighting - Plain text
 *
 * <p>Supports three viewing modes: - VIEW: Read-only rendered content - EDIT: Text editor only -
 * SPLIT: Editor and preview side-by-side
 */
@Slf4j
public class UniversalTextComponent extends VBox {

  // Properties
  private final StringProperty content = new SimpleStringProperty("");
  private final ObjectProperty<TextContentType> contentType =
      new SimpleObjectProperty<>(TextContentType.MARKDOWN);
  private final ObjectProperty<ViewMode> viewMode = new SimpleObjectProperty<>(ViewMode.VIEW);
  private final BooleanProperty darkTheme = new SimpleBooleanProperty(false);
  // Renderers
  private final Map<TextContentType, TextRenderer> renderers = new HashMap<>();
  // UI Components
  private ToolBar toolbar;
  private StackPane contentArea;
  private Node currentRenderedView;
  private TextArea textEditor;
  private SplitPane splitPane;
  // Callbacks
  private Consumer<String> onContentChanged;
  private Consumer<TextContentType> onContentTypeChanged;
  private Consumer<ViewMode> onViewModeChanged;

  public UniversalTextComponent() {
    initialize();
    setupBindings();
    setupStyling();
  }

  public UniversalTextComponent(String initialContent, TextContentType initialType) {
    this();
    setContent(initialContent);
    setContentType(initialType);
  }

  private void initialize() {
    getStyleClass().add("universal-text-component");
    setPadding(new Insets(5));
    setSpacing(5);

    // Initialize renderers
    initializeRenderers();

    // Create UI components
    createToolbar();
    createContentArea();
    createTextEditor();

    // Build initial layout
    buildLayout();
  }

  private void initializeRenderers() {
    try {
      // renderers.put(TextContentType.MARKDOWN, new MarkdownRenderer());
      renderers.put(TextContentType.PLAIN_TEXT, new PlainTextRenderer());
      // TODO: Add other renderers when implemented
      // renderers.put(TextContentType.MERMAID, new MermaidRenderer());
      // renderers.put(TextContentType.CODE, new CodeRenderer());

      log.info("Initialized {} text renderers", renderers.size());
    } catch (Exception e) {
      log.error("Failed to initialize text renderers", e);
    }
  }

  private void createToolbar() {
    toolbar = new ToolBar();
    toolbar.getStyleClass().add("text-component-toolbar");

    // Content type selector
    ComboBox<TextContentType> typeSelector = new ComboBox<>();
    typeSelector.getItems().addAll(TextContentType.values());
    typeSelector.setValue(contentType.get());
    typeSelector.setOnAction(e -> setContentType(typeSelector.getValue()));
    contentType.addListener((obs, old, newType) -> typeSelector.setValue(newType));

    // View mode buttons
    ToggleGroup viewModeGroup = new ToggleGroup();

    ToggleButton viewBtn = new ToggleButton();
    viewBtn.setGraphic(new FontIcon(Feather.EYE));
    viewBtn.setTooltip(new Tooltip("View Mode"));
    viewBtn.setToggleGroup(viewModeGroup);
    viewBtn.setOnAction(e -> setViewMode(ViewMode.VIEW));

    ToggleButton editBtn = new ToggleButton();
    editBtn.setGraphic(new FontIcon(Feather.EDIT));
    editBtn.setTooltip(new Tooltip("Edit Mode"));
    editBtn.setToggleGroup(viewModeGroup);
    editBtn.setOnAction(e -> setViewMode(ViewMode.EDIT));

    ToggleButton splitBtn = new ToggleButton();
    splitBtn.setGraphic(new FontIcon(Feather.COLUMNS));
    splitBtn.setTooltip(new Tooltip("Split Mode"));
    splitBtn.setToggleGroup(viewModeGroup);
    splitBtn.setOnAction(e -> setViewMode(ViewMode.SPLIT));

    // Set initial selection
    switch (viewMode.get()) {
      case VIEW -> viewBtn.setSelected(true);
      case EDIT -> editBtn.setSelected(true);
      case SPLIT -> splitBtn.setSelected(true);
    }

    // Spacer
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    // Theme toggle
    ToggleButton themeBtn = new ToggleButton();
    themeBtn.setGraphic(new FontIcon(Feather.MOON));
    themeBtn.setTooltip(new Tooltip("Toggle Dark Theme"));
    themeBtn.setSelected(darkTheme.get());
    themeBtn.setOnAction(e -> setDarkTheme(themeBtn.isSelected()));

    toolbar
        .getItems()
        .addAll(
            new Label("Type:"),
            typeSelector,
            new Separator(),
            viewBtn,
            editBtn,
            splitBtn,
            spacer,
            themeBtn);
  }

  private void createContentArea() {
    contentArea = new StackPane();
    contentArea.getStyleClass().add("text-content-area");
    VBox.setVgrow(contentArea, Priority.ALWAYS);
  }

  private void createTextEditor() {
    textEditor = new TextArea();
    textEditor.getStyleClass().add("text-editor");
    textEditor.setWrapText(true);
    textEditor.textProperty().bindBidirectional(content);

    // Auto-detect content type when text changes
    textEditor
        .textProperty()
        .addListener(
            (obs, oldText, newText) -> {
              if (onContentChanged != null) {
                onContentChanged.accept(newText);
              }

              // Auto-detect content type if it's currently PLAIN_TEXT
              if (getContentType() == TextContentType.PLAIN_TEXT && newText != null) {
                TextContentType detected = TextContentType.autoDetect(newText);
                if (detected != TextContentType.PLAIN_TEXT) {
                  setContentType(detected);
                }
              }
            });
  }

  private void buildLayout() {
    getChildren().clear();
    getChildren().add(toolbar);

    switch (viewMode.get()) {
      case VIEW -> buildViewLayout();
      case EDIT -> buildEditLayout();
      case SPLIT -> buildSplitLayout();
    }
  }

  private void buildViewLayout() {
    renderContent();
    getChildren().add(contentArea);
  }

  private void buildEditLayout() {
    contentArea.getChildren().clear();
    contentArea.getChildren().add(textEditor);
    getChildren().add(contentArea);
  }

  private void buildSplitLayout() {
    splitPane = new SplitPane();
    splitPane.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
    splitPane.getStyleClass().add("text-split-pane");

    // Left: Editor
    VBox editorPane = new VBox(textEditor);
    editorPane.getStyleClass().add("editor-pane");

    // Right: Rendered view
    renderContent();
    VBox viewPane = new VBox();
    viewPane.getStyleClass().add("view-pane");
    if (currentRenderedView != null) {
      viewPane.getChildren().add(currentRenderedView);
    }

    splitPane.getItems().addAll(editorPane, viewPane);
    splitPane.setDividerPositions(0.5); // 50-50 split

    contentArea.getChildren().clear();
    contentArea.getChildren().add(splitPane);
    getChildren().add(contentArea);
  }

  private void renderContent() {
    try {
      TextRenderer renderer = renderers.get(contentType.get());
      if (renderer != null) {
        Node renderedNode = renderer.render(content.get(), contentType.get());
        setRenderedView(renderedNode);
      } else {
        // Fallback to plain text renderer
        TextRenderer fallbackRenderer = renderers.get(TextContentType.PLAIN_TEXT);
        if (fallbackRenderer != null) {
          Node renderedNode = fallbackRenderer.render(content.get(), contentType.get());
          setRenderedView(renderedNode);
        } else {
          // Ultimate fallback
          Label errorLabel = new Label("No renderer available for " + contentType.get());
          errorLabel.getStyleClass().add("error-label");
          setRenderedView(errorLabel);
        }
      }
    } catch (Exception e) {
      log.error("Failed to render content", e);
      Label errorLabel = new Label("Rendering error: " + e.getMessage());
      errorLabel.getStyleClass().add("error-label");
      setRenderedView(errorLabel);
    }
  }

  private void setRenderedView(Node node) {
    currentRenderedView = node;
    if (viewMode.get() == ViewMode.VIEW) {
      contentArea.getChildren().clear();
      contentArea.getChildren().add(currentRenderedView);
    } else if (viewMode.get() == ViewMode.SPLIT && splitPane != null) {
      // Update the view pane in split mode
      VBox viewPane = (VBox) splitPane.getItems().get(1);
      viewPane.getChildren().clear();
      viewPane.getChildren().add(currentRenderedView);
    }
  }

  private void setupBindings() {
    // Re-render when content or content type changes
    content.addListener(
        (obs, oldContent, newContent) -> {
          if (viewMode.get().canView()) {
            renderContent();
          }
        });

    contentType.addListener(
        (obs, oldType, newType) -> {
          if (onContentTypeChanged != null) {
            onContentTypeChanged.accept(newType);
          }
          if (viewMode.get().canView()) {
            renderContent();
          }
        });

    // Rebuild layout when view mode changes
    viewMode.addListener(
        (obs, oldMode, newMode) -> {
          if (onViewModeChanged != null) {
            onViewModeChanged.accept(newMode);
          }
          buildLayout();
        });

    // Apply theme changes to renderers
    darkTheme.addListener(
        (obs, oldTheme, newTheme) -> {
          applyThemeToRenderers(newTheme);
          if (viewMode.get().canView()) {
            renderContent(); // Re-render to apply theme
          }
        });
  }

  private void applyThemeToRenderers(boolean darkTheme) {
    renderers
        .values()
        .forEach(
            renderer -> {
              // if (renderer instanceof MarkdownRenderer) {
              //     ((MarkdownRenderer) renderer).setDarkTheme(darkTheme);
              // }
              // TODO: Apply theme to other renderers when implemented
            });
  }

  private void setupStyling() {
    getStyleClass().add("universal-text-component");
  }

  // Public API

  public String getContent() {
    return content.get();
  }

  public void setContent(String content) {
    this.content.set(content != null ? content : "");
  }

  public StringProperty contentProperty() {
    return content;
  }

  public TextContentType getContentType() {
    return contentType.get();
  }

  public void setContentType(TextContentType contentType) {
    this.contentType.set(contentType != null ? contentType : TextContentType.PLAIN_TEXT);
  }

  public ObjectProperty<TextContentType> contentTypeProperty() {
    return contentType;
  }

  public ViewMode getViewMode() {
    return viewMode.get();
  }

  public void setViewMode(ViewMode viewMode) {
    this.viewMode.set(viewMode != null ? viewMode : ViewMode.VIEW);
  }

  public ObjectProperty<ViewMode> viewModeProperty() {
    return viewMode;
  }

  public boolean isDarkTheme() {
    return darkTheme.get();
  }

  public void setDarkTheme(boolean darkTheme) {
    this.darkTheme.set(darkTheme);
  }

  public BooleanProperty darkThemeProperty() {
    return darkTheme;
  }

  // Callbacks

  public void setOnContentChanged(Consumer<String> callback) {
    this.onContentChanged = callback;
  }

  public void setOnContentTypeChanged(Consumer<TextContentType> callback) {
    this.onContentTypeChanged = callback;
  }

  public void setOnViewModeChanged(Consumer<ViewMode> callback) {
    this.onViewModeChanged = callback;
  }

  // Utility methods

  public void toggleViewMode() {
    setViewMode(getViewMode().next());
  }

  public void focusEditor() {
    if (textEditor != null) {
      textEditor.requestFocus();
    }
  }

  public void exportContent() {
    // TODO: Implement export functionality
    log.info("Export functionality not yet implemented");
  }

  public void dispose() {
    renderers.values().forEach(TextRenderer::dispose);
    renderers.clear();
  }
}
