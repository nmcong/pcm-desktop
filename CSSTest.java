/**
 * Test if CSS resources are accessible
 */
public class CSSTest {
    public static void main(String[] args) {
        System.out.println("Testing CSS resource access...");
        
        // Test main CSS
        if (CSSTest.class.getResource("/css/styles.css") != null) {
            System.out.println("✅ Found main CSS: " + CSSTest.class.getResource("/css/styles.css").toExternalForm());
        } else {
            System.out.println("❌ Main CSS not found: /css/styles.css");
        }
        
        // Test dark theme CSS
        if (CSSTest.class.getResource("/css/ai-assistant-dark.css") != null) {
            System.out.println("✅ Found dark theme CSS: " + CSSTest.class.getResource("/css/ai-assistant-dark.css").toExternalForm());
        } else {
            System.out.println("❌ Dark theme CSS not found: /css/ai-assistant-dark.css");
        }
        
        System.out.println("✅ CSS resource test complete!");
    }
}