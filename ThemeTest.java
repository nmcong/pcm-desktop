import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.NordDark;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.noteflix.pcm.core.theme.ThemeManager;

/**
 * Simple test application to verify theme system integration
 */
public class ThemeTest extends Application {
    
    private ThemeManager themeManager;
    private Label statusLabel;
    
    @Override
    public void start(Stage primaryStage) {
        themeManager = ThemeManager.getInstance();
        
        statusLabel = new Label("Theme: " + (themeManager.isDarkTheme() ? "Dark" : "Light"));
        statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Button lightButton = new Button("Light Theme");
        lightButton.setOnAction(e -> {
            themeManager.setTheme(false);
            updateStatus();
        });
        
        Button darkButton = new Button("Dark Theme");
        darkButton.setOnAction(e -> {
            themeManager.setTheme(true);
            updateStatus();
        });
        
        Button toggleButton = new Button("Toggle Theme");
        toggleButton.setOnAction(e -> {
            themeManager.toggleTheme();
            updateStatus();
        });
        
        // Test elements from ai-assistant-dark.css
        Label testUser = new Label("User Message Test");
        testUser.getStyleClass().add("user-message-bubble");
        testUser.setStyle("-fx-background-color: -fx-user-bubble; -fx-text-fill: white; -fx-padding: 10px; -fx-background-radius: 10px;");
        
        Label testAI = new Label("AI Message Test");
        testAI.getStyleClass().add("ai-message-bubble");
        testAI.setStyle("-fx-background-color: -fx-ai-bubble; -fx-text-fill: -fx-text-primary; -fx-padding: 10px; -fx-background-radius: 10px;");
        
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("ai-chat-page");
        root.getChildren().addAll(statusLabel, lightButton, darkButton, toggleButton, testUser, testAI);
        
        Scene scene = new Scene(root, 400, 350);
        
        // Initialize ThemeManager with scene
        themeManager.setMainScene(scene);
        themeManager.setTheme(false); // Start with light
        
        primaryStage.setTitle("Theme Test - CSS Integration");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        System.out.println("✅ Theme Test application started");
        System.out.println("🎨 Click buttons to test theme switching and CSS loading");
    }
    
    private void updateStatus() {
        statusLabel.setText("Theme: " + (themeManager.isDarkTheme() ? "Dark" : "Light"));
        System.out.println("Theme changed to: " + (themeManager.isDarkTheme() ? "Dark" : "Light"));
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}