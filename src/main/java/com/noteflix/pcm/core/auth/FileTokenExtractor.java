package com.noteflix.pcm.core.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

/**
 * Extracts tokens from shared files
 * Common locations where SSO systems store tokens
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class FileTokenExtractor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Extract token from shared file locations
     */
    public Optional<String> extractFromFile(String serviceName) {
        try {
            // Try JSON files first
            Optional<String> jsonToken = extractFromJsonFiles(serviceName);
            if (jsonToken.isPresent()) {
                return jsonToken;
            }

            // Try properties files
            Optional<String> propsToken = extractFromPropertiesFiles(serviceName);
            if (propsToken.isPresent()) {
                return propsToken;
            }

            // Try plain text files
            Optional<String> textToken = extractFromTextFiles(serviceName);
            return textToken;

        } catch (Exception e) {
            log.error("Error extracting from files", e);
            return Optional.empty();
        }
    }

    private Optional<String> extractFromJsonFiles(String serviceName) {
        String[] jsonFilePaths = {
                // Common shared locations
                System.getProperty("user.home") + "/.auth/tokens.json",
                System.getProperty("user.home") + "/.sso/credentials.json",
                System.getProperty("user.home") + "/.config/" + serviceName + "/auth.json",

                // Application data directories
                System.getProperty("user.home") + "/AppData/Local/" + serviceName + "/auth.json", // Windows
                System.getProperty("user.home") + "/Library/Application Support/" + serviceName + "/auth.json", // macOS

                // PCM specific locations
                System.getProperty("user.home") + "/.pcm/auth.json",
                System.getProperty("user.home") + "/.pcm/tokens/" + serviceName + ".json",

                // Temp directories (less secure but sometimes used)
                System.getProperty("java.io.tmpdir") + "/" + serviceName + "_auth.json",

                // Current directory
                "./auth.json",
                "./" + serviceName + "_auth.json",
                "./config/auth.json"
        };

        for (String filePath : jsonFilePaths) {
            try {
                Path path = Paths.get(filePath);
                if (Files.exists(path) && Files.isReadable(path)) {
                    log.debug("Checking JSON file: {}", filePath);
                    String content = Files.readString(path);
                    Optional<String> token = parseJsonForToken(content, serviceName);
                    if (token.isPresent()) {
                        log.info("Found token in JSON file: {}", filePath);
                        return token;
                    }
                }
            } catch (Exception e) {
                log.debug("Error reading JSON file {}: {}", filePath, e.getMessage());
            }
        }

        return Optional.empty();
    }

    private Optional<String> parseJsonForToken(String jsonContent, String serviceName) {
        try {
            JsonNode root = objectMapper.readTree(jsonContent);

            // Try different JSON structures
            String[] tokenPaths = {
                    serviceName + ".token",
                    serviceName + ".access_token",
                    serviceName + ".auth_token",
                    "tokens." + serviceName,
                    "auth." + serviceName + ".token",
                    "credentials." + serviceName + ".token",
                    "services." + serviceName + ".token",
                    "sso." + serviceName,
                    "token",
                    "access_token",
                    "auth_token",
                    "jwt",
                    "bearer_token",
                    "session_token"
            };

            for (String tokenPath : tokenPaths) {
                JsonNode tokenNode = getNestedJsonValue(root, tokenPath);
                if (tokenNode != null && tokenNode.isTextual()) {
                    String token = tokenNode.asText();
                    if (isValidToken(token)) {
                        return Optional.of(token);
                    }
                }
            }

        } catch (Exception e) {
            log.debug("Error parsing JSON content: {}", e.getMessage());
        }

        return Optional.empty();
    }

    private JsonNode getNestedJsonValue(JsonNode root, String path) {
        String[] parts = path.split("\\.");
        JsonNode current = root;

        for (String part : parts) {
            if (current != null && current.has(part)) {
                current = current.get(part);
            } else {
                return null;
            }
        }

        return current;
    }

    private Optional<String> extractFromPropertiesFiles(String serviceName) {
        String[] propsFilePaths = {
                System.getProperty("user.home") + "/.auth/tokens.properties",
                System.getProperty("user.home") + "/.sso/auth.properties",
                System.getProperty("user.home") + "/.config/" + serviceName + "/auth.properties",
                System.getProperty("user.home") + "/.pcm/auth.properties",
                "./auth.properties",
                "./" + serviceName + ".properties",
                "./config/auth.properties"
        };

        for (String filePath : propsFilePaths) {
            try {
                Path path = Paths.get(filePath);
                if (Files.exists(path) && Files.isReadable(path)) {
                    log.debug("Checking properties file: {}", filePath);
                    Properties props = new Properties();
                    props.load(Files.newInputStream(path));

                    Optional<String> token = extractTokenFromProperties(props, serviceName);
                    if (token.isPresent()) {
                        log.info("Found token in properties file: {}", filePath);
                        return token;
                    }
                }
            } catch (Exception e) {
                log.debug("Error reading properties file {}: {}", filePath, e.getMessage());
            }
        }

        return Optional.empty();
    }

    private Optional<String> extractTokenFromProperties(Properties props, String serviceName) {
        String[] tokenKeys = {
                serviceName + ".token",
                serviceName + ".auth_token",
                serviceName + ".access_token",
                serviceName + ".jwt",
                "token",
                "auth_token",
                "access_token",
                "jwt",
                "session_token",
                "bearer_token",
                "sso_token"
        };

        for (String key : tokenKeys) {
            String value = props.getProperty(key);
            if (value != null && isValidToken(value)) {
                return Optional.of(value);
            }
        }

        return Optional.empty();
    }

    private Optional<String> extractFromTextFiles(String serviceName) {
        String[] textFilePaths = {
                System.getProperty("user.home") + "/.auth/token",
                System.getProperty("user.home") + "/.sso/token",
                System.getProperty("user.home") + "/." + serviceName + "_token",
                System.getProperty("user.home") + "/.pcm/" + serviceName + "_token",
                System.getProperty("java.io.tmpdir") + "/" + serviceName + "_token.txt",
                "./token.txt",
                "./" + serviceName + "_token.txt"
        };

        for (String filePath : textFilePaths) {
            try {
                Path path = Paths.get(filePath);
                if (Files.exists(path) && Files.isReadable(path)) {
                    log.debug("Checking text file: {}", filePath);
                    String content = Files.readString(path).trim();
                    if (isValidToken(content)) {
                        log.info("Found token in text file: {}", filePath);
                        return Optional.of(content);
                    }
                }
            } catch (Exception e) {
                log.debug("Error reading text file {}: {}", filePath, e.getMessage());
            }
        }

        // For demonstration - create a demo token file if it doesn't exist
        if (serviceName.equals("demo-service")) {
            try {
                Path demoPath = Paths.get("./demo_service_token.txt");
                if (!Files.exists(demoPath)) {
                    String demoToken = "demo-file-token-" + System.currentTimeMillis();
                    Files.writeString(demoPath, demoToken);
                    log.info("Demo: Created token file for demo-service");
                    return Optional.of(demoToken);
                } else {
                    String content = Files.readString(demoPath).trim();
                    if (isValidToken(content)) {
                        log.info("Demo: Found token in demo file");
                        return Optional.of(content);
                    }
                }
            } catch (Exception e) {
                log.debug("Error with demo file: {}", e.getMessage());
            }
        }

        return Optional.empty();
    }

    private boolean isValidToken(String token) {
        return token != null &&
                token.length() > 20 &&
                !token.contains("\n") &&
                !token.contains("\r") &&
                (token.startsWith("eyJ") || // JWT
                        token.startsWith("Bearer ") ||
                        token.startsWith("demo-") || // Demo tokens
                        token.matches("[A-Za-z0-9+/=_-]{20,}")); // Token-like pattern
    }
}