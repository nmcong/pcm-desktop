package com.noteflix.pcm.examples;

import com.noteflix.pcm.core.auth.SSOTokenManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

/**
 * Demo for SSO Integration functionality
 * <p>
 * Demonstrates:
 * - Token extraction from various sources
 * - Token caching and management
 * - Integration with LLM services
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class SSOIntegrationDemo {

    private static SSOTokenManager ssoManager;
    private static Scanner scanner;

    public static void main(String[] args) {
        System.out.println("🔐 PCM Desktop SSO Integration Demo");
        System.out.println("====================================");

        scanner = new Scanner(System.in);
        ssoManager = SSOTokenManager.getInstance();

        showMainMenu();
        scanner.close();
    }

    private static void showMainMenu() {
        while (true) {
            System.out.println("\n🔧 Choose a demo:");
            System.out.println("1. Test Token Extraction");
            System.out.println("2. Manual Token Injection");
            System.out.println("3. Token Cache Management");
            System.out.println("4. Service Authentication Status");
            System.out.println("5. Simulate Browser Token");
            System.out.println("6. File Token Demo");
            System.out.println("7. View Security Audit Log");
            System.out.println("0. Exit");
            System.out.print("\nYour choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    testTokenExtraction();
                    break;
                case "2":
                    manualTokenInjection();
                    break;
                case "3":
                    tokenCacheManagement();
                    break;
                case "4":
                    checkServiceStatus();
                    break;
                case "5":
                    simulateBrowserToken();
                    break;
                case "6":
                    fileTokenDemo();
                    break;
                case "7":
                    viewAuditLog();
                    break;
                case "0":
                    System.out.println("👋 Goodbye!");
                    return;
                default:
                    System.out.println("❌ Invalid choice. Please try again.");
            }
        }
    }

    private static void testTokenExtraction() {
        System.out.println("\n🔍 Token Extraction Demo");
        System.out.println("========================");

        System.out.print("Enter service name to test (e.g., 'demo-service'): ");
        String serviceName = scanner.nextLine().trim();

        if (serviceName.isEmpty()) {
            System.out.println("❌ Service name cannot be empty");
            return;
        }

        try {
            System.out.println("\n🔄 Attempting to extract token for: " + serviceName);

            var token = ssoManager.getToken(serviceName);

            if (token.isPresent()) {
                String tokenValue = token.get();
                String maskedToken = maskToken(tokenValue);
                System.out.println("✅ Token found: " + maskedToken);

                // Check expiry
                var expiry = ssoManager.getTokenExpiry(serviceName);
                if (expiry.isPresent()) {
                    System.out.println("📅 Expires: " + expiry.get());
                }

                // Check if near expiry
                if (ssoManager.isTokenNearExpiry(serviceName)) {
                    System.out.println("⚠️  Token will expire soon!");
                }

            } else {
                System.out.println("❌ No token found for service: " + serviceName);
                System.out.println("\n💡 Try running demo 5 or 6 to create demo tokens first");
            }

        } catch (Exception e) {
            System.out.println("❌ Error extracting token: " + e.getMessage());
            log.error("Token extraction error", e);
        }
    }

    private static void manualTokenInjection() {
        System.out.println("\n💉 Manual Token Injection Demo");
        System.out.println("===============================");

        System.out.print("Enter service name: ");
        String serviceName = scanner.nextLine().trim();

        if (serviceName.isEmpty()) {
            System.out.println("❌ Service name cannot be empty");
            return;
        }

        System.out.print("Enter token value: ");
        String token = scanner.nextLine().trim();

        if (token.isEmpty()) {
            System.out.println("❌ Token cannot be empty");
            return;
        }

        System.out.print("Enter expiry time in seconds (default 3600): ");
        String expiryStr = scanner.nextLine().trim();
        long expiry = expiryStr.isEmpty() ? 3600 : Long.parseLong(expiryStr);

        try {
            ssoManager.cacheToken(serviceName, token, expiry);
            System.out.println("✅ Token cached successfully for: " + serviceName);
            System.out.println("🕒 Expires in: " + expiry + " seconds");

        } catch (Exception e) {
            System.out.println("❌ Error caching token: " + e.getMessage());
            log.error("Token caching error", e);
        }
    }

    private static void tokenCacheManagement() {
        System.out.println("\n🗃️  Token Cache Management Demo");
        System.out.println("================================");

        var cachedServices = ssoManager.getCachedServices();

        if (cachedServices.isEmpty()) {
            System.out.println("📭 No tokens currently cached");
            return;
        }

        System.out.println("📋 Cached services:");
        for (String service : cachedServices) {
            var token = ssoManager.getToken(service);
            var expiry = ssoManager.getTokenExpiry(service);

            System.out.printf("  • %s: %s", service,
                    token.isPresent() ? "✅ Valid" : "❌ Expired");

            if (expiry.isPresent()) {
                System.out.printf(" (expires: %s)", expiry.get());
            }
            System.out.println();
        }

        System.out.println("\nActions:");
        System.out.println("1. Clear all tokens");
        System.out.println("2. Clear specific service");
        System.out.println("3. Back to main menu");
        System.out.print("Choose: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                ssoManager.clearTokens();
                System.out.println("✅ All tokens cleared");
                break;
            case "2":
                System.out.print("Enter service name to clear: ");
                String serviceName = scanner.nextLine().trim();
                if (!serviceName.isEmpty()) {
                    ssoManager.clearToken(serviceName);
                    System.out.println("✅ Token cleared for: " + serviceName);
                }
                break;
            case "3":
                return;
        }
    }

    private static void checkServiceStatus() {
        System.out.println("\n🔍 Service Authentication Status");
        System.out.println("=================================");

        String[] testServices = {
                "demo-service",
                "openai-service",
                "company-portal",
                "anthropic-service",
                "internal-llm"
        };

        System.out.println("Checking authentication status for common services:");

        for (String service : testServices) {
            boolean authenticated = ssoManager.isAuthenticated(service);
            System.out.printf("  • %-20s: %s\n", service,
                    authenticated ? "✅ Authenticated" : "❌ Not authenticated");
        }

        System.out.print("\nEnter custom service name to check (or press Enter to skip): ");
        String customService = scanner.nextLine().trim();

        if (!customService.isEmpty()) {
            boolean authenticated = ssoManager.isAuthenticated(customService);
            System.out.printf("  • %-20s: %s\n", customService,
                    authenticated ? "✅ Authenticated" : "❌ Not authenticated");
        }
    }

    private static void simulateBrowserToken() {
        System.out.println("\n🌐 Simulate Browser Token Demo");
        System.out.println("===============================");

        // Create demo tokens that the browser extractors will find
        String demoToken = "demo-browser-token-" + System.currentTimeMillis();

        // Cache as if extracted from browser
        ssoManager.cacheToken("demo-service", demoToken, 7200); // 2 hours

        System.out.println("✅ Simulated browser SSO token for 'demo-service'");
        System.out.println("🔗 Token: " + maskToken(demoToken));

        // Test extraction
        var extractedToken = ssoManager.getToken("demo-service");
        if (extractedToken.isPresent()) {
            System.out.println("✅ Token successfully retrieved from cache");
            System.out.println("🔐 Authenticated: " + ssoManager.isAuthenticated("demo-service"));
        }
    }

    private static void fileTokenDemo() {
        System.out.println("\n📁 File Token Demo");
        System.out.println("===================");

        try {
            // Create a demo token file
            String demoToken = "demo-file-token-" + System.currentTimeMillis();
            java.nio.file.Path tokenFile = java.nio.file.Paths.get("./demo_service_token.txt");

            java.nio.file.Files.writeString(tokenFile, demoToken);
            System.out.println("📝 Created demo token file: " + tokenFile.toAbsolutePath());

            // Try to extract it
            var extractedToken = ssoManager.getToken("demo-service");

            if (extractedToken.isPresent()) {
                System.out.println("✅ Token extracted from file: " + maskToken(extractedToken.get()));
            } else {
                System.out.println("❌ Failed to extract token from file");
            }

            // Cleanup
            System.out.print("\nDelete demo file? (y/n): ");
            String delete = scanner.nextLine().trim().toLowerCase();
            if (delete.equals("y") || delete.equals("yes")) {
                java.nio.file.Files.deleteIfExists(tokenFile);
                System.out.println("🗑️  Demo file deleted");
            }

        } catch (Exception e) {
            System.out.println("❌ Error with file token demo: " + e.getMessage());
            log.error("File token demo error", e);
        }
    }

    private static void viewAuditLog() {
        System.out.println("\n📋 Security Audit Log");
        System.out.println("======================");

        try {
            java.nio.file.Path logFile = java.nio.file.Paths.get("logs/security-audit.log");

            if (!java.nio.file.Files.exists(logFile)) {
                System.out.println("📭 No audit log file found");
                return;
            }

            var lines = java.nio.file.Files.readAllLines(logFile);

            if (lines.isEmpty()) {
                System.out.println("📭 Audit log is empty");
                return;
            }

            System.out.println("📊 Recent security events:");

            // Show last 10 lines
            int startIndex = Math.max(0, lines.size() - 10);
            for (int i = startIndex; i < lines.size(); i++) {
                System.out.printf("%3d: %s\n", i + 1, lines.get(i));
            }

            if (lines.size() > 10) {
                System.out.printf("\n... and %d earlier entries\n", lines.size() - 10);
            }

        } catch (Exception e) {
            System.out.println("❌ Error reading audit log: " + e.getMessage());
            log.error("Audit log read error", e);
        }
    }

    private static String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "***";
        }

        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
}