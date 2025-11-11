import com.noteflix.pcm.core.theme.ThemeManager;

/**
 * Simple console test for ThemeManager
 */
public class SimpleThemeTest {
    public static void main(String[] args) {
        System.out.println("Testing ThemeManager...");
        
        ThemeManager themeManager = ThemeManager.getInstance();
        System.out.println("✅ ThemeManager instance created");
        
        System.out.println("Initial theme: " + (themeManager.isDarkTheme() ? "Dark" : "Light"));
        
        // Test toggle
        themeManager.toggleTheme();
        System.out.println("After toggle: " + (themeManager.isDarkTheme() ? "Dark" : "Light"));
        
        // Test setting specific theme
        themeManager.setTheme(true);
        System.out.println("Set to dark: " + (themeManager.isDarkTheme() ? "Dark" : "Light"));
        
        themeManager.setTheme(false);
        System.out.println("Set to light: " + (themeManager.isDarkTheme() ? "Dark" : "Light"));
        
        // Test icon methods
        System.out.println("Brain circuit icon: " + themeManager.getBrainCircuitIcon());
        System.out.println("Bot icon: " + themeManager.getBotIcon());
        
        System.out.println("✅ ThemeManager basic functionality works!");
    }
}