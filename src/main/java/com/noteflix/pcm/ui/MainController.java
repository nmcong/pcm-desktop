package com.noteflix.pcm.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Main View Controller
 * Handles the main application window and navigation
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

        // Create categories
        TreeItem<String> dashboardItem = new TreeItem<>("📊 Dashboard");
        TreeItem<String> projectsItem = new TreeItem<>("📁 Projects");
        TreeItem<String> notesItem = new TreeItem<>("📝 Notes");
        TreeItem<String> tasksItem = new TreeItem<>("✓ Tasks");
        TreeItem<String> settingsItem = new TreeItem<>("⚙️ Settings");

        rootItem.getChildren().addAll(
            dashboardItem,
            projectsItem,
            notesItem,
            tasksItem,
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

        Label titleLabel = new Label("Welcome to PCM");
        titleLabel.setStyle("-fx-font-size: 32; -fx-font-weight: bold;");

        Label subtitleLabel = new Label("Personal Content Manager");
        subtitleLabel.setStyle("-fx-font-size: 18; -fx-text-fill: #666;");

        Button getStartedBtn = new Button("Get Started");
        getStartedBtn.setStyle("-fx-font-size: 14; -fx-padding: 10 30;");
        getStartedBtn.setOnAction(e -> handleGetStarted());

        vbox.getChildren().addAll(titleLabel, subtitleLabel, getStartedBtn);
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
        showAlert("Getting Started", "Welcome to PCM!\n\nThis is your personal content management system.", Alert.AlertType.INFORMATION);
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
            "About PCM",
            "PCM Desktop Application\nVersion 1.0.0\n\nPersonal Content Manager\nBuilt with JavaFX",
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

