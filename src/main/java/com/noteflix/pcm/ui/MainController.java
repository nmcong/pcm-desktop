package com.noteflix.pcm.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Main View Controller for PCM Desktop
 * 
 * PCM (Project Code Management) - AI-Powered System Analysis Tool
 * 
 * This controller manages the main application window, providing navigation
 * and coordination between different modules:
 * - System/Subsystem management
 * - Screen/Form analysis
 * - Database objects management
 * - Batch job tracking
 * - Workflow visualization
 * - AI-powered query interface
 * 
 * @author Noteflix Team
 */
@Slf4j
public class MainController implements Initializable {

    @FXML
    private BorderPane rootPane;

    @FXML
    private MenuBar menuBar;

    @FXML
    private ToolBar toolBar;

    @FXML
    private TreeView<String> navigationTree;

    @FXML
    private TabPane contentTabPane;

    @FXML
    private Label statusLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Initializing Main Controller...");
        
        setupMenuBar();
        setupNavigationTree();
        setupTabPane();
        updateStatus("Ready");
        
        log.info("Main Controller initialized");
    }

    /**
     * Setup menu bar with actions
     */
    private void setupMenuBar() {
        // Menu bar is defined in FXML
        log.debug("Setting up menu bar...");
    }

    /**
     * Setup navigation tree
     */
    private void setupNavigationTree() {
        log.debug("Setting up navigation tree...");
        
        TreeItem<String> rootItem = new TreeItem<>("PCM");
        rootItem.setExpanded(true);

        // Create main categories for PCM
        TreeItem<String> dashboardItem = new TreeItem<>("📊 Dashboard");
        TreeItem<String> subsystemsItem = new TreeItem<>("🏗️ Subsystems & Projects");
        TreeItem<String> screensItem = new TreeItem<>("🖥️ Screens & Forms");
        TreeItem<String> databaseItem = new TreeItem<>("🗄️ Database Objects");
        TreeItem<String> batchJobsItem = new TreeItem<>("⚙️ Batch Jobs");
        TreeItem<String> workflowItem = new TreeItem<>("🔄 Workflows");
        TreeItem<String> knowledgeItem = new TreeItem<>("📚 Knowledge Base");
        TreeItem<String> queryItem = new TreeItem<>("🤖 AI Query");
        TreeItem<String> settingsItem = new TreeItem<>("⚙️ Settings");

        rootItem.getChildren().addAll(
            dashboardItem,
            subsystemsItem,
            screensItem,
            databaseItem,
            batchJobsItem,
            workflowItem,
            knowledgeItem,
            queryItem,
            settingsItem
        );

        navigationTree.setRoot(rootItem);
        navigationTree.setShowRoot(false);

        // Handle selection
        navigationTree.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    handleNavigationSelection(newValue.getValue());
                }
            }
        );
    }

    /**
     * Setup tab pane
     */
    private void setupTabPane() {
        log.debug("Setting up tab pane...");
        
        // Add welcome tab
        Tab welcomeTab = new Tab("Welcome");
        welcomeTab.setClosable(false);
        welcomeTab.setContent(createWelcomeView());
        
        contentTabPane.getTabs().add(welcomeTab);
    }

    /**
     * Create welcome view
     */
    private javafx.scene.layout.VBox createWelcomeView() {
        javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(20);
        vbox.setAlignment(javafx.geometry.Pos.CENTER);
        vbox.setStyle("-fx-padding: 40;");

        Label titleLabel = new Label("Welcome to PCM Desktop");
        titleLabel.setStyle("-fx-font-size: 32; -fx-font-weight: bold;");

        Label subtitleLabel = new Label("AI-Powered System Analysis & Business Management");
        subtitleLabel.setStyle("-fx-font-size: 18; -fx-text-fill: #666;");
        
        Label descriptionLabel = new Label("Analyze source code, manage business logic, and query your enterprise system with AI");
        descriptionLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #888;");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(600);

        Button getStartedBtn = new Button("Get Started");
        getStartedBtn.setStyle("-fx-font-size: 14; -fx-padding: 10 30;");
        getStartedBtn.setOnAction(e -> handleGetStarted());

        vbox.getChildren().addAll(titleLabel, subtitleLabel, descriptionLabel, getStartedBtn);
        return vbox;
    }

    /**
     * Handle navigation selection
     */
    private void handleNavigationSelection(String item) {
        log.info("Navigation selected: {}", item);
        updateStatus("Selected: " + item);
        
        // Create new tab based on selection
        String tabTitle = item.replaceAll("[^\\w\\s]", "").trim();
        Tab newTab = new Tab(tabTitle);
        
        Label content = new Label("Content for: " + tabTitle);
        content.setStyle("-fx-font-size: 24; -fx-padding: 40;");
        newTab.setContent(content);
        
        contentTabPane.getTabs().add(newTab);
        contentTabPane.getSelectionModel().select(newTab);
    }

    /**
     * Handle get started button
     */
    @FXML
    private void handleGetStarted() {
        log.info("Get Started clicked");
        showAlert("Getting Started", 
            "Welcome to PCM Desktop!\n\n" +
            "PCM (Project Code Management) is an AI-powered tool for:\n" +
            "• Analyzing enterprise system architecture\n" +
            "• Managing subsystems, screens, and workflows\n" +
            "• Tracking database objects and batch jobs\n" +
            "• Querying system information with natural language\n\n" +
            "Start by exploring the navigation menu on the left.", 
            Alert.AlertType.INFORMATION);
    }

    /**
     * Handle File -> New action
     */
    @FXML
    private void handleFileNew() {
        log.info("File -> New clicked");
        showAlert("New File", "Create new file functionality coming soon!", Alert.AlertType.INFORMATION);
    }

    /**
     * Handle File -> Open action
     */
    @FXML
    private void handleFileOpen() {
        log.info("File -> Open clicked");
        showAlert("Open File", "Open file functionality coming soon!", Alert.AlertType.INFORMATION);
    }

    /**
     * Handle File -> Exit action
     */
    @FXML
    private void handleFileExit() {
        log.info("File -> Exit clicked");
        javafx.application.Platform.exit();
    }

    /**
     * Handle Help -> About action
     */
    @FXML
    private void handleHelpAbout() {
        log.info("Help -> About clicked");
        showAlert(
            "About PCM Desktop",
            "PCM Desktop Application\n" +
            "Version 1.0.0\n\n" +
            "Project Code Management\n" +
            "AI-Powered System Analysis & Business Management Tool\n\n" +
            "Built with JavaFX 21 and Java 21\n" +
            "© 2025 Noteflix Team",
            Alert.AlertType.INFORMATION
        );
    }

    /**
     * Update status bar
     */
    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    /**
     * Show alert dialog
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

