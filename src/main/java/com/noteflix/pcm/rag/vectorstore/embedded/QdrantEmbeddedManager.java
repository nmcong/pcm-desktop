package com.noteflix.pcm.rag.vectorstore.embedded;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

/**
 * Manager for embedded Qdrant process.
 *
 * <p>Starts Qdrant binary as separate process and manages its lifecycle. This implementation
 * includes security validations and proper resource management.
 *
 * <p>Usage:
 *
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
 * @version 2.0.0 - Security improvements
 */
@Slf4j
public class QdrantEmbeddedManager {

  private Process qdrantProcess;
  private Thread outputReaderThread;
  private final String binaryPath;
  private final String storagePath;
  private final int port;
  private volatile boolean isShuttingDown = false;

  /** Create manager with default settings. */
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
    // Security validation for inputs
    validateInputs(binaryPath, storagePath, port);

    this.binaryPath = binaryPath;
    this.storagePath = storagePath;
    this.port = port;
  }

  /** Validate constructor inputs to prevent security issues. */
  private void validateInputs(String binaryPath, String storagePath, int port) {
    if (binaryPath == null || binaryPath.trim().isEmpty()) {
      throw new IllegalArgumentException("Binary path cannot be null or empty");
    }
    if (storagePath == null || storagePath.trim().isEmpty()) {
      throw new IllegalArgumentException("Storage path cannot be null or empty");
    }
    if (port <= 0 || port > 65535) {
      throw new IllegalArgumentException("Port must be between 1 and 65535");
    }

    // Prevent path traversal attacks
    if (binaryPath.contains("..") || binaryPath.contains("~")) {
      throw new IllegalArgumentException("Binary path contains unsafe characters");
    }
    if (storagePath.contains("..") || storagePath.contains("~")) {
      throw new IllegalArgumentException("Storage path contains unsafe characters");
    }

    // Validate paths are not absolute system paths
    Path normalizedBinaryPath = Paths.get(binaryPath).normalize();
    Path normalizedStoragePath = Paths.get(storagePath).normalize();

    // Additional security: ensure paths don't escape working directory
    if (normalizedBinaryPath.isAbsolute()
        && !normalizedBinaryPath.startsWith(Paths.get("").toAbsolutePath())) {
      log.warn("Binary path is outside working directory: {}", normalizedBinaryPath);
    }
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
          "Qdrant binary not found: "
              + binary
              + "\n"
              + "Please download Qdrant binary from: https://github.com/qdrant/qdrant/releases\n"
              + "Or use Docker: docker run -p 6333:6333 qdrant/qdrant");
    }

    // Create storage directory
    Path storage = Paths.get(storagePath);
    Files.createDirectories(storage);

    // Build command with validated arguments
    String[] command = buildSecureCommand(binary);
    ProcessBuilder pb = new ProcessBuilder(command);
    pb.redirectErrorStream(true);

    log.info("Starting Qdrant: {} (port: {}, storage: {})", binary, port, storagePath);

    qdrantProcess = pb.start();

    // Create managed background thread for output reading
    outputReaderThread = new Thread(this::readQdrantOutput, "qdrant-output-" + port);
    outputReaderThread.setDaemon(true); // Allow JVM shutdown even if thread is running
    outputReaderThread.start();

    // Wait for Qdrant to start
    waitForQdrant();

    log.info("Qdrant started successfully on port {}", port);
  }

  /** Stop Qdrant process. */
  public void stop() {
    isShuttingDown = true;

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

    // Clean up output reader thread
    if (outputReaderThread != null && outputReaderThread.isAlive()) {
      try {
        outputReaderThread.interrupt();
        outputReaderThread.join(2000); // Wait up to 2 seconds
      } catch (InterruptedException e) {
        log.warn("Interrupted while stopping output reader thread");
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

  /** Get OS-specific Qdrant binary path. */
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
        "Unsupported OS: "
            + os
            + " ("
            + arch
            + ")\n"
            + "Supported: macOS (Intel/ARM), Linux (x86_64), Windows (x86_64)");
  }

  /** Wait for Qdrant to be ready. */
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
        "Qdrant failed to start within 30 seconds\n"
            + "Check logs for errors or try running manually:\n"
            + "  "
            + getQdrantBinary()
            + " --storage-path "
            + storagePath);
  }

  /** Check Qdrant health. */
  private boolean checkHealth() {
    try {
      // Try the correct health endpoint first, then fallback to collections endpoint
      String[] healthUrls = {
        getUrl() + "/", // Qdrant root endpoint returns 200 when healthy
        getUrl() + "/collections", // Collections endpoint also indicates if service is ready
        getUrl() + "/metrics" // Alternative endpoint
      };
      
      for (String healthUrl : healthUrls) {
        try {
          URL url = new URL(healthUrl);
          HttpURLConnection conn = (HttpURLConnection) url.openConnection();
          conn.setRequestMethod("GET");
          conn.setConnectTimeout(1000);
          conn.setReadTimeout(1000);

          int responseCode = conn.getResponseCode();
          if (responseCode == 200) {
            log.debug("Qdrant health check successful via: {}", healthUrl);
            return true;
          }
        } catch (Exception e) {
          // Continue to next endpoint
        }
      }
      
      return false;

    } catch (Exception e) {
      return false;
    }
  }

  /** Build secure command array to prevent command injection. */
  private String[] buildSecureCommand(String binary) {
    // Validate binary path once more before execution
    Path binaryPath = Paths.get(binary);
    if (!Files.exists(binaryPath)) {
      throw new IllegalArgumentException("Binary file does not exist: " + binary);
    }

    // Build command with properly quoted arguments
    // Create a simple config file path in storage directory 
    String configDir = storagePath + "/config";
    try {
      Files.createDirectories(Paths.get(configDir));
      
      // Create minimal config file
      Path configFile = Paths.get(configDir, "config.yaml");
      if (!Files.exists(configFile)) {
        String config = String.format("""
            service:
              host: 127.0.0.1
              http_port: %d
              grpc_port: %d
            storage:
              storage_path: %s
            log_level: INFO
            """, port, port + 1, storagePath);
        Files.writeString(configFile, config);
        log.debug("Created Qdrant config: {}", configFile);
      }
      
      return new String[] {
        binary,
        "--config-path", configFile.toString()
      };
    } catch (IOException e) {
      log.warn("Failed to create config file, using basic command: {}", e.getMessage());
      // Fallback to basic command without config file
      return new String[] {binary};
    }
  }

  /** Safely read Qdrant process output. */
  private void readQdrantOutput() {
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(qdrantProcess.getInputStream()))) {

      String line;
      while (!isShuttingDown && (line = reader.readLine()) != null) {
        if (!isShuttingDown) { // Double-check to avoid logging during shutdown
          log.debug("[Qdrant] {}", line);
        }
      }
    } catch (IOException e) {
      if (!isShuttingDown) { // Only log errors if not shutting down
        log.error("Error reading Qdrant output", e);
      }
    }
  }
}
