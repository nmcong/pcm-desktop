import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.noteflix.pcm.core.theme.ThemeManager;

/**
 * Minimal test to verify CSS loading in JavaFX
 */
public class MinimalCSSTest extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        Label testLabel = new Label("CSS Theme Test");
        testLabel.getStyleClass().add("ai-chat-page"); // Class from ai-assistant-dark.css
        
        Button toggleButton = new Button("Toggle Dark Theme");
        
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(testLabel, toggleButton);
        
        Scene scene = new Scene(root, 300, 200);
        
        // Test manual CSS loading
        String mainCss = getClass().getResource("/css/styles.css").toExternalForm();
        scene.getStylesheets().add(mainCss);
        System.out.println("✅ Added main CSS: " + mainCss);
        
        String darkCss = getClass().getResource("/css/ai-assistant-dark.css").toExternalForm();
        scene.getStylesheets().add(darkCss);
        System.out.println("✅ Added dark theme CSS: " + darkCss);
        
        toggleButton.setOnAction(e -> {
            System.out.println("Current stylesheets: " + scene.getStylesheets().size());
            scene.getStylesheets().forEach(css -> System.out.println("  - " + css));
        });
        
        primaryStage.setTitle("Minimal CSS Test");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        System.out.println("✅ JavaFX CSS test application started successfully");
        System.out.println("🎨 ai-assistant-dark.css should be applied to .ai-chat-page elements");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}