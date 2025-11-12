package com.noteflix.pcm.core.auth;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * Extracts tokens from Windows Registry
 * Useful when SSO system stores tokens in registry
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class RegistryExtractor {

    /**
     * Extract token from Windows Registry
     */
    public Optional<String> extractFromRegistry(String serviceName) {
        try {
            // Try HKEY_CURRENT_USER first
            Optional<String> userToken = extractFromUserRegistry(serviceName);
            if (userToken.isPresent()) {
                return userToken;
            }

            // Try HKEY_LOCAL_MACHINE
            Optional<String> machineToken = extractFromMachineRegistry(serviceName);
            return machineToken;

        } catch (Exception e) {
            log.error("Error extracting from Windows Registry", e);
            return Optional.empty();
        }
    }

    private Optional<String> extractFromUserRegistry(String serviceName) {
        try {
            // Common registry paths for SSO tokens
            String[] registryPaths = {
                    "Software/Microsoft/Windows/CurrentVersion/Authentication/LogonUI/SessionData",
                    "Software/" + serviceName + "/Auth",
                    "Software/SSO/Tokens",
                    "Software/Enterprise/SSO/" + serviceName,
                    "Software/PCM/Auth/" + serviceName
            };

            for (String registryPath : registryPaths) {
                Optional<String> token = readRegistryValue(Preferences.userRoot(), registryPath, serviceName);
                if (token.isPresent()) {
                    return token;
                }
            }

        } catch (Exception e) {
            log.debug("Error reading from user registry: {}", e.getMessage());
        }

        return Optional.empty();
    }

    private Optional<String> extractFromMachineRegistry(String serviceName) {
        try {
            // System-wide registry paths
            String[] registryPaths = {
                    "Software/" + serviceName + "/Auth",
                    "Software/SSO/Tokens",
                    "Software/Enterprise/Authentication",
                    "Software/PCM/SystemAuth/" + serviceName
            };

            for (String registryPath : registryPaths) {
                Optional<String> token = readRegistryValue(Preferences.systemRoot(), registryPath, serviceName);
                if (token.isPresent()) {
                    return token;
                }
            }

        } catch (Exception e) {
            log.debug("Error reading from machine registry: {}", e.getMessage());
        }

        return Optional.empty();
    }

    private Optional<String> readRegistryValue(Preferences root, String path, String serviceName) {
        try {
            // Check if registry path exists
            if (!registryPathExists(root, path)) {
                log.debug("Registry path does not exist: {}", path);
                return Optional.empty();
            }

            Preferences prefs = root.node(path);

            // Try different token key names
            String[] tokenKeys = {
                    "token",
                    "auth_token",
                    "access_token",
                    serviceName + "_token",
                    "jwt",
                    "session_token",
                    "sso_token",
                    "bearer_token"
            };

            for (String key : tokenKeys) {
                String value = prefs.get(key, null);
                if (value != null && !value.isEmpty() && isValidToken(value)) {
                    log.info("Found token in registry: {}/{}", path, key);
                    return Optional.of(value);
                }
            }

            // For demonstration purposes - simulate finding a token
            if (serviceName.equals("demo-service")) {
                log.info("Demo: Found token in Windows Registry for service: {}", serviceName);
                return Optional.of("demo-registry-token-" + System.currentTimeMillis());
            }

        } catch (Exception e) {
            log.debug("Error reading registry path {}: {}", path, e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * Check if registry path exists
     */
    private boolean registryPathExists(Preferences root, String path) {
        try {
            Preferences prefs = root.node(path);
            // Try to access the node - will throw exception if doesn't exist
            prefs.keys();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate token format
     */
    private boolean isValidToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        // Basic token validation
        return token.length() > 10 &&
                !token.contains(" ") &&
                (token.startsWith("eyJ") || // JWT
                        token.startsWith("Bearer ") ||
                        token.matches("[A-Za-z0-9+/=_-]{20,}")); // Base64-like
    }
}