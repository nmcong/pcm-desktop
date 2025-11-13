package com.noteflix.pcm.rag.embedded;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manager for embedded Qdrant process.
 * 
 * Starts Qdrant binary as separate process and manages its lifecycle.
 * 
 * Usage:
 * <pre>
 * QdrantEmbeddedManager qdrant = new QdrantEmbeddedManager();
 * qdrant.start();
 * 
 * // Use Qdrant...
 * 
 * qdrant.stop();
 * </pre>
 * 
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class QdrantEmbeddedManager {
    
    private Process qdrantProcess;
    private final String binaryPath;
    private final String storagePath;
    private final int port;
    
    /**
     * Create manager with default settings.
     */
    public QdrantEmbeddedManager() {
        this("bin/qdrant", "data/qdrant-storage", 6333);
    }
    
    /**
     * Create manager with custom settings.
     * 
     * @param binaryPath Path to Qdrant binary (without OS suffix)
     * @param storagePath Path to storage directory
     * @param port Port to run on
     */
    public QdrantEmbeddedManager(String binaryPath, String storagePath, int port) {
        this.binaryPath = binaryPath;
        this.storagePath = storagePath;
        this.port = port;
    }
    
    /**
     * Start Qdrant process.
     * 
     * @throws IOException if start fails
     */
    public void start() throws IOException {
        if (isRunning()) {
            log.info("Qdrant already running");
            return;
        }
        
        // Get OS-specific binary
        String binary = getQdrantBinary();
        
        if (!Files.exists(Paths.get(binary))) {
            throw new IOException(
                "Qdrant binary not found: " + binary + "\n" +
                "Please download Qdrant binary from: https://github.com/qdrant/qdrant/releases\n" +
                "Or use Docker: docker run -p 6333:6333 qdrant/qdrant"
            );
        }
        
        // Create storage directory
        Path storage = Paths.get(storagePath);
        Files.createDirectories(storage);
        
        // Build command
        ProcessBuilder pb = new ProcessBuilder(
            binary,
            "--storage-path", storagePath,
            "--http-port", String.valueOf(port)
        );
        
        pb.redirectErrorStream(true);
        
        log.info("Starting Qdrant: {} (port: {}, storage: {})", binary, port, storagePath);
        
        qdrantProcess = pb.start();
        
        // Log output in background thread
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(qdrantProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("[Qdrant] {}", line);
                }
            } catch (IOException e) {
                log.error("Error reading Qdrant output", e);
            }
        }, "qdrant-output").start();
        
        // Wait for Qdrant to start
        waitForQdrant();
        
        log.info("Qdrant started successfully on port {}", port);
    }
    
    /**
     * Stop Qdrant process.
     */
    public void stop() {
        if (qdrantProcess != null && qdrantProcess.isAlive()) {
            log.info("Stopping Qdrant...");
            qdrantProcess.destroy();
            
            try {
                boolean exited = qdrantProcess.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
                if (!exited) {
                    log.warn("Qdrant did not stop gracefully, forcing...");
                    qdrantProcess.destroyForcibly();
                }
                log.info("Qdrant stopped");
            } catch (InterruptedException e) {
                log.error("Interrupted while waiting for Qdrant to stop", e);
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Check if Qdrant is running.
     * 
     * @return true if running
     */
    public boolean isRunning() {
        if (qdrantProcess != null && qdrantProcess.isAlive()) {
            return true;
        }
        
        // Check if Qdrant is accessible
        return checkHealth();
    }
    
    /**
     * Get Qdrant URL.
     * 
     * @return URL (e.g., http://localhost:6333)
     */
    public String getUrl() {
        return "http://localhost:" + port;
    }
    
    /**
     * Get port.
     * 
     * @return port number
     */
    public int getPort() {
        return port;
    }
    
    // ========== Private Methods ==========
    
    /**
     * Get OS-specific Qdrant binary path.
     */
    private String getQdrantBinary() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();
        
        if (os.contains("mac")) {
            if (arch.contains("aarch64") || arch.contains("arm")) {
                return binaryPath + "-aarch64-macos";
            } else {
                return binaryPath + "-x86_64-macos";
            }
        } else if (os.contains("linux")) {
            return binaryPath + "-x86_64-linux";
        } else if (os.contains("win")) {
            return binaryPath + "-x86_64-windows.exe";
        }
        
        throw new IllegalStateException(
            "Unsupported OS: " + os + " (" + arch + ")\n" +
            "Supported: macOS (Intel/ARM), Linux (x86_64), Windows (x86_64)"
        );
    }
    
    /**
     * Wait for Qdrant to be ready.
     */
    private void waitForQdrant() throws IOException {
        log.info("Waiting for Qdrant to be ready...");
        
        for (int i = 0; i < 30; i++) {
            if (checkHealth()) {
                log.info("Qdrant is ready!");
                return;
            }
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for Qdrant", e);
            }
        }
        
        throw new IOException(
            "Qdrant failed to start within 30 seconds\n" +
            "Check logs for errors or try running manually:\n" +
            "  " + getQdrantBinary() + " --storage-path " + storagePath
        );
    }
    
    /**
     * Check Qdrant health.
     */
    private boolean checkHealth() {
        try {
            URL url = new URL(getUrl() + "/health");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            
            int responseCode = conn.getResponseCode();
            return responseCode == 200;
            
        } catch (Exception e) {
            return false;
        }
    }
}

