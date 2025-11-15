package com.noteflix.pcm.ui;

import atlantafx.base.theme.Styles;
import com.noteflix.pcm.core.events.ThemeChangeListener;
import com.noteflix.pcm.core.theme.ThemeManager;
import com.noteflix.pcm.ui.components.SidebarView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.octicons.Octicons;
import org.kordamp.ikonli.javafx.FontIcon;

/** Main application view built with pure Java (no FXML) Following AtlantaFX Sampler patterns */
@Slf4j
public class MainView extends BorderPane implements ThemeChangeListener {

  private final MainController controller;
  private final ThemeManager themeManager;
  private SidebarView sidebar;
  private Button themeButton;

  public MainView(MainController controller) {
    this.controller = controller;
    this.themeManager = ThemeManager.getInstance();

    getStyleClass().add("root");

    // Register for theme changes
    themeManager.addThemeChangeListener(this);

    // Build UI: Sidebar left, right side divided into navbar (top) and content (bottom)
    setLeft(createSidebar());
    setCenter(createRightSide());
  }

  /** Creates the left sidebar */
  private SidebarView createSidebar() {
    sidebar = new SidebarView();
    return sidebar;
  }

  /** Creates the right side with navbar at top and content below */
  private BorderPane createRightSide() {
    BorderPane rightSide = new BorderPane();
    rightSide.getStyleClass().add("right-side");

    // Top: Navbar
    rightSide.setTop(createNavbar());

    // Center: Content area (pages will be displayed here)
    StackPane contentArea = new StackPane();
    contentArea.getStyleClass().add("content-area");
    rightSide.setCenter(contentArea);

    // Initialize navigation system for the content area
    initializeNavigation(contentArea);

    return rightSide;
  }

  /** Initialize navigation system */
  private void initializeNavigation(StackPane contentArea) {
    var pageNavigator = new com.noteflix.pcm.core.navigation.DefaultPageNavigator(contentArea);

    // Set navigator for sidebar
    if (sidebar != null) {
      sidebar.setPageNavigator(pageNavigator);
    }

    // Navigate to default page
    pageNavigator.navigateToPage(com.noteflix.pcm.ui.pages.AIAssistantPage.class);
  }

  /** Creates the top navigation bar */
  private HBox createNavbar() {
    HBox navbar = new HBox(16);
    navbar.getStyleClass().add("navbar");
    navbar.setPadding(new Insets(12, 16, 12, 16));
    navbar.setAlignment(Pos.CENTER_LEFT);

    // Sidebar toggle button
    Button sidebarToggleBtn = new Button();
    sidebarToggleBtn.setGraphic(new FontIcon(Octicons.THREE_BARS_16));
    sidebarToggleBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, "icon-btn");
    sidebarToggleBtn.setOnAction(e -> toggleSidebar());

    // Spacer
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    // Action buttons
    // Theme toggle button
    themeButton = new Button();
    themeButton.setGraphic(new FontIcon(themeManager.isDarkTheme() ? Octicons.MOON_16 : Octicons.SUN_16));
    themeButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, "icon-btn");
    themeButton.setTooltip(new Tooltip("Switch Theme"));
    themeButton.setOnAction(e -> themeManager.toggleTheme());

    Button notificationsBtn = new Button();
    notificationsBtn.setGraphic(new FontIcon(Octicons.BELL_16));
    notificationsBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, "icon-btn");

    Button settingsBtn = new Button();
    settingsBtn.setGraphic(new FontIcon(Octicons.GEAR_16));
    settingsBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, "icon-btn");

    MenuButton userMenu = new MenuButton();
    userMenu.setGraphic(new FontIcon(Octicons.PERSON_16));
    userMenu.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, "icon-btn");

    MenuItem profileItem = new MenuItem("Profile");
    MenuItem preferencesItem = new MenuItem("Preferences");
    MenuItem aboutItem = new MenuItem("About");
    aboutItem.setOnAction(e -> controller.handleHelpAbout());
    MenuItem logoutItem = new MenuItem("Logout");
    logoutItem.setOnAction(e -> controller.handleFileExit());

    userMenu
        .getItems()
        .addAll(
            profileItem,
            preferencesItem,
            new SeparatorMenuItem(),
            aboutItem,
            new SeparatorMenuItem(),
            logoutItem);

    navbar
        .getChildren()
        .addAll(sidebarToggleBtn, spacer, themeButton, notificationsBtn, settingsBtn, userMenu);

    return navbar;
  }

  /** Toggles sidebar visibility */
  private void toggleSidebar() {
    if (getLeft() != null) {
      // Hide sidebar
      setLeft(null);
    } else {
      // Show sidebar
      setLeft(sidebar);
    }
  }

  /** Creates demo content for the main area */
  private VBox createDemoContent() {
    VBox content = new VBox(16);
    content.getStyleClass().add("content-wrapper");
    content.setPadding(new Insets(16));

    // Content header with tabs
    content.getChildren().add(createContentHeader());

    // Content body with scroll
    ScrollPane scrollPane = new ScrollPane(createContentBody());
    scrollPane.setFitToWidth(true);
    scrollPane.getStyleClass().add("content-scroll");
    VBox.setVgrow(scrollPane, Priority.ALWAYS);

    content.getChildren().add(scrollPane);

    return content;
  }

  /** Creates content header with tabs */
  private VBox createContentHeader() {
    VBox header = new VBox(12);
    header.getStyleClass().add("content-header");

    // Breadcrumb
    Label breadcrumb = new Label("Projects / Customer Service / Login Screen");
    breadcrumb.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");

    // Title and actions
    Label pageTitle = new Label("Login Screen");
    pageTitle.getStyleClass().add(Styles.TITLE_1);
    pageTitle.setStyle("-fx-font-weight: bold;");

    Button editBtn = new Button("Edit");
    editBtn.setGraphic(new FontIcon(Octicons.PENCIL_16));
    editBtn.getStyleClass().add(Styles.ACCENT);

    Button moreBtn = new Button();
    moreBtn.setGraphic(new FontIcon(Octicons.KEBAB_HORIZONTAL_16));
    moreBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox titleRow = new HBox(16, pageTitle, spacer, editBtn, moreBtn);
    titleRow.setAlignment(Pos.CENTER_LEFT);

    // Tabs
    ToggleGroup tabGroup = new ToggleGroup();

    ToggleButton overviewTab = new ToggleButton("📋 Overview");
    overviewTab.setToggleGroup(tabGroup);
    overviewTab.setSelected(true);
    overviewTab.getStyleClass().addAll(Styles.BUTTON_OUTLINED);

    ToggleButton codeTab = new ToggleButton("💻 Code");
    codeTab.setToggleGroup(tabGroup);
    codeTab.getStyleClass().addAll(Styles.BUTTON_OUTLINED);

    ToggleButton dataTab = new ToggleButton("🗄️ Data");
    dataTab.setToggleGroup(tabGroup);
    dataTab.getStyleClass().addAll(Styles.BUTTON_OUTLINED);

    ToggleButton workflowTab = new ToggleButton("🔄 Workflow");
    workflowTab.setToggleGroup(tabGroup);
    workflowTab.getStyleClass().addAll(Styles.BUTTON_OUTLINED);

    HBox tabs = new HBox(8, overviewTab, codeTab, dataTab, workflowTab);

    header.getChildren().addAll(breadcrumb, titleRow, new Separator(), tabs);

    return header;
  }

  /** Creates the main content body */
  private HBox createContentBody() {
    HBox body = new HBox(16);
    body.getStyleClass().add("content-wrapper");
    body.setPadding(new Insets(16));

    // Main content
    VBox mainContentArea = new VBox(16);
    mainContentArea.getStyleClass().add("content-main");
    HBox.setHgrow(mainContentArea, Priority.ALWAYS);

    mainContentArea
        .getChildren()
        .addAll(
            createStatsCards(),
            createDescriptionCard(),
            createTagsCard(),
            createRelatedItemsCard());

    // Right sidebar
    VBox rightSidebar = new VBox(16);
    rightSidebar.getStyleClass().add("sidebar-right");
    rightSidebar.setPrefWidth(300);
    rightSidebar.setMinWidth(300);
    rightSidebar.setMaxWidth(300);

    rightSidebar.getChildren().addAll(createPropertiesPanel(), createActivityPanel());

    body.getChildren().addAll(mainContentArea, rightSidebar);

    return body;
  }

  /** Creates statistics cards */
  private HBox createStatsCards() {
    HBox statsCards = new HBox(16);

    statsCards
        .getChildren()
        .addAll(
            createStatCard("📊 Components", "24", "+3 this week", "-color-accent-emphasis"),
            createStatCard("💾 Database", "8 tables", "2 views", "-color-success-emphasis"),
            createStatCard("🔄 Workflows", "12 flows", "Active", "-color-warning-emphasis"),
            createStatCard("📝 Documentation", "89%", "Complete", "-color-fg-muted"));

    return statsCards;
  }

  private VBox createStatCard(String title, String value, String subtitle, String colorVar) {
    VBox card = new VBox(8);
    card.getStyleClass().add("card");
    card.setPadding(new Insets(16));
    HBox.setHgrow(card, Priority.ALWAYS);

    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");

    Label valueLabel = new Label(value);
    valueLabel.getStyleClass().add(Styles.TITLE_2);
    valueLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorVar + ";");

    Label subtitleLabel = new Label(subtitle);
    subtitleLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");

    card.getChildren().addAll(titleLabel, valueLabel, subtitleLabel);

    return card;
  }

  /** Creates description card */
  private VBox createDescriptionCard() {
    VBox card = new VBox(12);
    card.getStyleClass().add("card");
    card.setPadding(new Insets(16));

    // Header
    Label header = new Label("📄 Description");
    header.getStyleClass().add(Styles.TITLE_4);
    header.setStyle("-fx-font-weight: bold;");

    // Description text
    TextArea description =
        new TextArea(
            "Main authentication screen for the Customer Service portal. "
                + "Handles user login with username/password and provides SSO integration.");
    description.setWrapText(true);
    description.setPrefRowCount(3);
    VBox.setVgrow(description, Priority.ALWAYS);

    card.getChildren().addAll(header, description);

    return card;
  }

  /** Creates tags card */
  private VBox createTagsCard() {
    VBox card = new VBox(12);
    card.getStyleClass().add("card");
    card.setPadding(new Insets(16));

    // Header
    Label header = new Label("🏷️ Tags");
    header.getStyleClass().add(Styles.TITLE_4);
    header.setStyle("-fx-font-weight: bold;");

    // Tags
    FlowPane tagsPane = new FlowPane(8, 8);
    tagsPane
        .getChildren()
        .addAll(
            createTag("Authentication", Styles.ACCENT),
            createTag("Security", Styles.SUCCESS),
            createTag("High Priority", Styles.DANGER),
            createTag("v2.0", Styles.WARNING));

    card.getChildren().addAll(header, tagsPane);

    return card;
  }

  private Label createTag(String text, String style) {
    Label tag = new Label(text);
    tag.getStyleClass().addAll("tag", style);
    tag.setStyle("-fx-padding: 4 12 4 12; -fx-background-radius: 12px;");
    return tag;
  }

  /** Creates related items card */
  private VBox createRelatedItemsCard() {
    VBox card = new VBox(12);
    card.getStyleClass().add("card");
    card.setPadding(new Insets(16));

    // Header
    HBox headerRow = new HBox(8);
    headerRow.setAlignment(Pos.CENTER_LEFT);

    Label header = new Label("🔗 Related Screens");
    header.getStyleClass().add(Styles.TITLE_4);
    header.setStyle("-fx-font-weight: bold;");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    Button addBtn = new Button();
    addBtn.setGraphic(new FontIcon(Octicons.PLUS_16));
    addBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.SMALL);

    headerRow.getChildren().addAll(header, spacer, addBtn);

    // Related items
    VBox items = new VBox(8);
    items
        .getChildren()
        .addAll(
            createRelatedItem("Dashboard", "Main entry point"),
            createRelatedItem("Password Reset", "Forgot password flow"),
            createRelatedItem("Registration", "New user signup"));

    card.getChildren().addAll(headerRow, items);

    return card;
  }

  private HBox createRelatedItem(String name, String description) {
    HBox item = new HBox(12);
    item.getStyleClass().add("list-item");
    item.setPadding(new Insets(8));
    item.setAlignment(Pos.CENTER_LEFT);

    FontIcon icon = new FontIcon(Octicons.FILE_16);
    icon.setIconSize(16);

    VBox textBox = new VBox(2);
    Label nameLabel = new Label(name);
    nameLabel.getStyleClass().add(Styles.TEXT_BOLD);
    Label descLabel = new Label(description);
    descLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");
    textBox.getChildren().addAll(nameLabel, descLabel);
    HBox.setHgrow(textBox, Priority.ALWAYS);

    item.getChildren().addAll(icon, textBox);

    return item;
  }

  /** Creates properties panel */
  private VBox createPropertiesPanel() {
    VBox panel = new VBox(12);
    panel.getStyleClass().add("card");
    panel.setPadding(new Insets(16));

    // Header
    Label header = new Label("⚙️ Properties");
    header.getStyleClass().add(Styles.TITLE_4);
    header.setStyle("-fx-font-weight: bold;");

    // Properties
    VBox properties = new VBox(12);
    properties
        .getChildren()
        .addAll(
            createProperty("Status", "Active"),
            createProperty("Type", "Screen"),
            createProperty("Category", "Authentication"),
            createProperty("Owner", "John Doe"),
            createProperty("Created", "Jan 15, 2025"),
            createProperty("Modified", "2 hours ago"));

    panel.getChildren().addAll(header, new Separator(), properties);

    return panel;
  }

  private VBox createProperty(String label, String value) {
    VBox prop = new VBox(4);

    Label labelNode = new Label(label);
    labelNode.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");

    Label valueNode = new Label(value);
    valueNode.getStyleClass().add(Styles.TEXT_BOLD);

    prop.getChildren().addAll(labelNode, valueNode);

    return prop;
  }

  /** Creates activity panel */
  private VBox createActivityPanel() {
    VBox panel = new VBox(12);
    panel.getStyleClass().add("card");
    panel.setPadding(new Insets(16));

    // Header
    Label header = new Label("📋 Recent Activity");
    header.getStyleClass().add(Styles.TITLE_4);
    header.setStyle("-fx-font-weight: bold;");

    // Activity items
    VBox activities = new VBox(12);
    activities
        .getChildren()
        .addAll(
            createActivityItem("John Doe updated description", "2 hours ago"),
            createActivityItem("Jane Smith added tag 'Security'", "5 hours ago"),
            createActivityItem("Mike Johnson created this screen", "2 days ago"));

    panel.getChildren().addAll(header, new Separator(), activities);

    return panel;
  }

  private VBox createActivityItem(String text, String time) {
    VBox item = new VBox(4);

    Label textLabel = new Label(text);
    textLabel.setWrapText(true);

    Label timeLabel = new Label(time);
    timeLabel.getStyleClass().addAll(Styles.TEXT_SMALL, "text-muted");

    item.getChildren().addAll(textLabel, timeLabel);

    return item;
  }

  @Override
  public void onThemeChanged(boolean isDarkTheme) {
    log.debug("Theme changed to: {}", isDarkTheme ? "dark" : "light");

    // Update theme button
    if (themeButton != null) {
      FontIcon newIcon = new FontIcon(isDarkTheme ? Octicons.MOON_16 : Octicons.SUN_16);
      themeButton.setGraphic(newIcon);
      themeButton.setTooltip(
          new Tooltip(isDarkTheme ? "Switch to Light Theme" : "Switch to Dark Theme"));
    }
  }

  /**
   * Cleanup method to unregister listeners
   * Should be called when the component is no longer needed
   */
  public void cleanup() {
    if (themeManager != null) {
      themeManager.removeThemeChangeListener(this);
    }
    if (sidebar != null) {
      sidebar.cleanup();
    }
  }
}
