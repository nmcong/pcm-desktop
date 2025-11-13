package com.noteflix.pcm.ast.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noteflix.pcm.ast.model.CodeMetadata;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Storage system for code metadata optimized for LLM usage.
 * Features:
 * - JSON-based persistent storage
 * - Fast lookup by file path or package
 * - Automatic versioning and change tracking
 * - Efficient caching for performance
 */
@Slf4j
public class CodeMetadataStorage {
    
    private final String storageBasePath;
    private final ObjectMapper objectMapper;
    private final Map<String, CodeMetadata> cache;
    private final Map<String, String> fileHashCache;
    
    public CodeMetadataStorage(String storageBasePath) {
        this.storageBasePath = storageBasePath;
        this.objectMapper = new ObjectMapper();
        this.cache = new ConcurrentHashMap<>();
        this.fileHashCache = new ConcurrentHashMap<>();
        
        // Create storage directory
        try {
            Files.createDirectories(Paths.get(storageBasePath));
            Files.createDirectories(Paths.get(storageBasePath, "metadata"));
            Files.createDirectories(Paths.get(storageBasePath, "indexes"));
            Files.createDirectories(Paths.get(storageBasePath, "history"));
        } catch (IOException e) {
            log.error("Failed to create storage directories", e);
        }
        
        log.info("Code metadata storage initialized at: {}", storageBasePath);
    }
    
    /**
     * Store code metadata with automatic versioning
     */
    public void store(CodeMetadata metadata) {
        if (metadata == null || metadata.getFileName() == null) {
            log.warn("Invalid metadata provided for storage");
            return;
        }
        
        try {
            String fileName = sanitizeFileName(metadata.getFileName());
            Path metadataPath = getMetadataPath(fileName);
            
            // Check if file has changed
            String currentHash = metadata.getFileHash();
            String previousHash = fileHashCache.get(fileName);
            
            if (currentHash != null && currentHash.equals(previousHash)) {
                log.debug("File {} unchanged, skipping storage", fileName);
                return;
            }
            
            // Create backup of previous version
            if (Files.exists(metadataPath)) {
                createBackup(metadataPath, fileName);
            }
            
            // Store new metadata
            objectMapper.writeValue(metadataPath.toFile(), metadata);
            
            // Update cache
            cache.put(fileName, metadata);
            if (currentHash != null) {
                fileHashCache.put(fileName, currentHash);
            }
            
            // Update indexes
            updateIndexes(metadata);
            
            log.debug("Stored metadata for: {}", fileName);
            
        } catch (IOException e) {
            log.error("Failed to store metadata for: {}", metadata.getFileName(), e);
        }
    }
    
    /**
     * Retrieve metadata by file name
     */
    public Optional<CodeMetadata> get(String fileName) {
        String sanitizedName = sanitizeFileName(fileName);
        
        // Check cache first
        CodeMetadata cached = cache.get(sanitizedName);
        if (cached != null) {
            return Optional.of(cached);
        }
        
        // Load from storage
        try {
            Path metadataPath = getMetadataPath(sanitizedName);
            
            if (!Files.exists(metadataPath)) {
                return Optional.empty();
            }
            
            CodeMetadata metadata = objectMapper.readValue(metadataPath.toFile(), CodeMetadata.class);
            cache.put(sanitizedName, metadata);
            
            return Optional.of(metadata);
            
        } catch (IOException e) {
            log.error("Failed to load metadata for: {}", fileName, e);
            return Optional.empty();
        }
    }
    
    /**
     * Find metadata by package name
     */
    public Map<String, CodeMetadata> findByPackage(String packageName) {
        Map<String, CodeMetadata> results = new HashMap<>();
        
        try {
            Path metadataDir = Paths.get(storageBasePath, "metadata");
            
            Files.list(metadataDir)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".json"))
                .forEach(path -> {
                    try {
                        CodeMetadata metadata = objectMapper.readValue(path.toFile(), CodeMetadata.class);
                        if (packageName.equals(metadata.getPackageName())) {
                            results.put(metadata.getFileName(), metadata);
                        }
                    } catch (IOException e) {
                        log.warn("Failed to read metadata file: {}", path, e);
                    }
                });
                
        } catch (IOException e) {
            log.error("Failed to search metadata by package: {}", packageName, e);
        }
        
        return results;
    }
    
    /**
     * Get all stored metadata
     */
    public Map<String, CodeMetadata> getAllMetadata() {
        Map<String, CodeMetadata> allMetadata = new HashMap<>();
        
        try {
            Path metadataDir = Paths.get(storageBasePath, "metadata");
            
            if (!Files.exists(metadataDir)) {
                return allMetadata;
            }
            
            Files.list(metadataDir)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".json"))
                .forEach(path -> {
                    try {
                        CodeMetadata metadata = objectMapper.readValue(path.toFile(), CodeMetadata.class);
                        allMetadata.put(metadata.getFileName(), metadata);
                    } catch (IOException e) {
                        log.warn("Failed to read metadata file: {}", path, e);
                    }
                });
                
        } catch (IOException e) {
            log.error("Failed to load all metadata", e);
        }
        
        return allMetadata;
    }
    
    /**
     * Delete metadata for a file
     */
    public boolean delete(String fileName) {
        String sanitizedName = sanitizeFileName(fileName);
        
        try {
            Path metadataPath = getMetadataPath(sanitizedName);
            
            if (Files.exists(metadataPath)) {
                // Create backup before deletion
                createBackup(metadataPath, sanitizedName);
                
                Files.delete(metadataPath);
                cache.remove(sanitizedName);
                fileHashCache.remove(sanitizedName);
                
                log.info("Deleted metadata for: {}", fileName);
                return true;
            }
            
            return false;
            
        } catch (IOException e) {
            log.error("Failed to delete metadata for: {}", fileName, e);
            return false;
        }
    }
    
    /**
     * Clear all cached data (storage files remain)
     */
    public void clearCache() {
        cache.clear();
        fileHashCache.clear();
        log.info("Metadata cache cleared");
    }
    
    /**
     * Get storage statistics
     */
    public StorageStats getStats() {
        StorageStats.StorageStatsBuilder builder = StorageStats.builder();
        
        try {
            Path metadataDir = Paths.get(storageBasePath, "metadata");
            
            if (Files.exists(metadataDir)) {
                long fileCount = Files.list(metadataDir)
                    .filter(Files::isRegularFile)
                    .count();
                
                long totalSize = Files.walk(metadataDir)
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .sum();
                
                builder.storedFiles(fileCount)
                       .totalStorageSize(totalSize);
            }
            
        } catch (IOException e) {
            log.error("Failed to calculate storage stats", e);
        }
        
        return builder.cacheSize(cache.size())
                     .hashCacheSize(fileHashCache.size())
                     .build();
    }
    
    /**
     * Export all metadata to a single JSON file for LLM consumption
     */
    public void exportToLLMFormat(String outputPath) {
        try {
            Map<String, CodeMetadata> allMetadata = getAllMetadata();
            
            Map<String, Object> llmExport = new HashMap<>();
            llmExport.put("exportedAt", LocalDateTime.now().toString());
            llmExport.put("totalFiles", allMetadata.size());
            llmExport.put("metadata", allMetadata);
            
            // Add summary statistics
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalClasses", allMetadata.values().stream()
                .mapToInt(m -> m.getClasses().size()).sum());
            summary.put("totalMethods", allMetadata.values().stream()
                .mapToInt(m -> m.getClasses().stream()
                    .mapToInt(c -> c.getMethodCount()).sum()).sum());
            summary.put("totalLinesOfCode", allMetadata.values().stream()
                .mapToInt(CodeMetadata::getLinesOfCode).sum());
            
            llmExport.put("summary", summary);
            
            objectMapper.writeValue(Paths.get(outputPath).toFile(), llmExport);
            
            log.info("Exported {} metadata files to LLM format: {}", 
                allMetadata.size(), outputPath);
                
        } catch (IOException e) {
            log.error("Failed to export metadata to LLM format", e);
        }
    }
    
    // Helper methods
    
    private Path getMetadataPath(String fileName) {
        return Paths.get(storageBasePath, "metadata", fileName + ".json");
    }
    
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
    
    private void createBackup(Path originalPath, String fileName) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupName = fileName + "_" + timestamp + ".json";
            Path backupPath = Paths.get(storageBasePath, "history", backupName);
            
            Files.createDirectories(backupPath.getParent());
            Files.copy(originalPath, backupPath);
            
        } catch (IOException e) {
            log.warn("Failed to create backup for: {}", fileName, e);
        }
    }
    
    private void updateIndexes(CodeMetadata metadata) {
        // Create package index
        try {
            Path packageIndexPath = Paths.get(storageBasePath, "indexes", "packages.json");
            Map<String, Object> packageIndex = new HashMap<>();
            
            if (Files.exists(packageIndexPath)) {
                packageIndex = objectMapper.readValue(packageIndexPath.toFile(), Map.class);
            }
            
            if (metadata.getPackageName() != null) {
                packageIndex.put(metadata.getPackageName(), metadata.getFileName());
            }
            
            objectMapper.writeValue(packageIndexPath.toFile(), packageIndex);
            
        } catch (IOException e) {
            log.warn("Failed to update package index", e);
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class StorageStats {
        private long storedFiles;
        private long totalStorageSize;
        private int cacheSize;
        private int hashCacheSize;
    }
}