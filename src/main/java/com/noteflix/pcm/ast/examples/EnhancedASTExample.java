package com.noteflix.pcm.ast.examples;

import com.noteflix.pcm.ast.analyzer.EnhancedASTAnalyzer;
import com.noteflix.pcm.ast.llm.LLMCodeRepresentation;
import com.noteflix.pcm.ast.model.*;
import com.noteflix.pcm.ast.storage.CodeMetadataStorage;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class EnhancedASTExample {
    
    public static void main(String[] args) {
        log.info("🚀 Starting Enhanced AST Analysis Example");
        
        // Demo 1: Detailed method flow analysis
        demonstrateMethodFlowAnalysis();
        
        // Demo 2: LLM-optimized code representation  
        demonstrateLLMRepresentation();
        
        // Demo 3: Metadata storage and retrieval
        demonstrateMetadataStorage();
        
        log.info("✅ Enhanced AST Analysis Example completed");
    }
    
    private static void demonstrateMethodFlowAnalysis() {
        log.info("=== Demo 1: Enhanced Method Flow Analysis ===");
        
        String complexJavaCode = """
            package com.example.demo;
            
            import java.util.*;
            import java.io.*;
            
            /**
             * Example class for complex flow analysis
             */
            public class DataProcessor {
                
                private List<String> data;
                private int processedCount;
                
                public DataProcessor() {
                    this.data = new ArrayList<>();
                    this.processedCount = 0;
                }
                
                /**
                 * Process data with complex control flow
                 * @param input Input data to process
                 * @return Processing result
                 * @throws ProcessingException if processing fails
                 */
                public ProcessingResult processData(String input) throws ProcessingException {
                    if (input == null || input.isEmpty()) {
                        throw new IllegalArgumentException("Input cannot be null or empty");
                    }
                    
                    ProcessingResult result = new ProcessingResult();
                    List<String> lines = Arrays.asList(input.split("\\n"));
                    
                    try {
                        for (int i = 0; i < lines.size(); i++) {
                            String line = lines.get(i).trim();
                            
                            if (line.startsWith("#")) {
                                // Skip comments
                                continue;
                            }
                            
                            if (line.contains("ERROR")) {
                                result.addError("Error found at line " + i);
                                continue;
                            }
                            
                            // Complex processing logic
                            String processed = processLine(line);
                            
                            if (processed != null) {
                                data.add(processed);
                                processedCount++;
                                result.addProcessedLine(processed);
                            }
                        }
                        
                        // Final validation
                        if (processedCount == 0) {
                            throw new ProcessingException("No valid data processed");
                        }
                        
                        result.setSuccess(true);
                        result.setProcessedCount(processedCount);
                        
                    } catch (Exception e) {
                        result.setSuccess(false);
                        result.addError("Processing failed: " + e.getMessage());
                        log.error("Processing error", e);
                    } finally {
                        // Cleanup resources
                        cleanup();
                    }
                    
                    return result;
                }
                
                private String processLine(String line) {
                    if (line.length() > 100) {
                        return line.substring(0, 100);
                    }
                    return line.toUpperCase();
                }
                
                private void cleanup() {
                    log.debug("Cleaning up resources");
                }
                
                public int getProcessedCount() {
                    return processedCount;
                }
            }
            """;
        
        EnhancedASTAnalyzer analyzer = new EnhancedASTAnalyzer();
        Optional<CodeMetadata> metadataOpt = analyzer.analyzeCode(complexJavaCode, "DataProcessor.java");
        
        if (metadataOpt.isPresent()) {
            CodeMetadata metadata = metadataOpt.get();
            
            log.info("📊 Analysis Results:");
            log.info("- Classes: {}", metadata.getClasses().size());
            log.info("- Total Methods: {}", 
                metadata.getClasses().stream().mapToInt(c -> c.getMethods().size()).sum());
            log.info("- Lines of Code: {}", metadata.getLinesOfCode());
            
            // Find the complex processData method
            metadata.getClasses().stream()
                .flatMap(c -> c.getMethods().stream())
                .filter(m -> "processData".equals(m.getMethodName()))
                .findFirst()
                .ifPresent(method -> {
                    MethodFlowInfo flow = method.getFlowInfo();
                    if (flow != null) {
                        log.info("🔍 Method Flow Analysis for processData:");
                        log.info("  - Signature: {}", flow.getSignature());
                        log.info("  - Cyclomatic Complexity: {}", flow.getCyclomaticComplexity());
                        log.info("  - Lines of Code: {}", flow.getLinesOfCode());
                        log.info("  - Control Flow Nodes: {}", flow.getControlFlowNodes().size());
                        log.info("  - Method Calls: {}", flow.getMethodCalls().size());
                        log.info("  - Local Variables: {}", flow.getLocalVariables().size());
                        log.info("  - Try-Catch Blocks: {}", flow.getTryCatchBlocks().size());
                        
                        // Log control flow details
                        flow.getControlFlowNodes().forEach(node -> 
                            log.info("    - {}: {} (depth {})", 
                                node.getNodeType(), node.getCondition(), node.getDepth()));
                        
                        // Log method calls
                        flow.getMethodCalls().forEach(call ->
                            log.info("    - Calls: {}.{}() at line {}", 
                                call.getTargetClass() != null ? call.getTargetClass() : "this",
                                call.getTargetMethod(), call.getPosition().getStartLine()));
                    }
                });
        }
    }
    
    private static void demonstrateLLMRepresentation() {
        log.info("=== Demo 2: LLM-Optimized Code Representation ===");
        
        String sampleCode = """
            package com.example.service;
            
            import java.util.List;
            import java.util.concurrent.CompletableFuture;
            
            /**
             * User service for managing user operations
             */
            public class UserService {
                
                private UserRepository repository;
                
                public UserService(UserRepository repository) {
                    this.repository = repository;
                }
                
                /**
                 * Find user by ID
                 * @param id User ID
                 * @return User or null if not found
                 */
                public User findById(Long id) {
                    if (id == null) {
                        return null;
                    }
                    return repository.findById(id);
                }
                
                /**
                 * Save user asynchronously
                 */
                public CompletableFuture<User> saveAsync(User user) {
                    return CompletableFuture.supplyAsync(() -> {
                        validateUser(user);
                        return repository.save(user);
                    });
                }
                
                private void validateUser(User user) {
                    if (user.getName() == null || user.getName().trim().isEmpty()) {
                        throw new IllegalArgumentException("User name is required");
                    }
                }
            }
            """;
        
        EnhancedASTAnalyzer analyzer = new EnhancedASTAnalyzer();
        Optional<CodeMetadata> metadataOpt = analyzer.analyzeCode(sampleCode, "UserService.java");
        
        if (metadataOpt.isPresent()) {
            CodeMetadata metadata = metadataOpt.get();
            LLMCodeRepresentation llmRep = new LLMCodeRepresentation();
            
            // Convert to LLM-friendly JSON
            String jsonRepresentation = llmRep.toJson(metadata);
            log.info("📋 LLM JSON Representation:");
            log.info("{}", jsonRepresentation);
            
            // Generate natural language summary
            String summary = llmRep.toNaturalLanguageSummary(metadata);
            log.info("📝 Natural Language Summary:");
            log.info("{}", summary);
            
            // Generate method flow diagram
            metadata.getClasses().stream()
                .flatMap(c -> c.getMethods().stream())
                .filter(m -> "saveAsync".equals(m.getMethodName()))
                .findFirst()
                .ifPresent(method -> {
                    if (method.getFlowInfo() != null) {
                        String flowDiagram = llmRep.generateMethodFlowDiagram(method.getFlowInfo());
                        log.info("🔄 Method Flow Diagram:");
                        log.info("{}", flowDiagram);
                    }
                });
        }
    }
    
    private static void demonstrateMetadataStorage() {
        log.info("=== Demo 3: Metadata Storage and Retrieval ===");
        
        // Create storage instance
        CodeMetadataStorage storage = new CodeMetadataStorage("data/ast-metadata");
        
        // Analyze and store multiple files
        String[] sampleCodes = {
            """
            package com.example.model;
            public class User {
                private String name;
                private String email;
                
                public User(String name, String email) {
                    this.name = name;
                    this.email = email;
                }
                
                public String getName() { return name; }
                public String getEmail() { return email; }
            }
            """,
            
            """
            package com.example.repository;
            import java.util.List;
            
            public interface UserRepository {
                User findById(Long id);
                List<User> findAll();
                User save(User user);
                void delete(Long id);
            }
            """
        };
        
        String[] fileNames = {"User.java", "UserRepository.java"};
        
        EnhancedASTAnalyzer analyzer = new EnhancedASTAnalyzer();
        
        // Store metadata
        for (int i = 0; i < sampleCodes.length; i++) {
            Optional<CodeMetadata> metadataOpt = analyzer.analyzeCode(sampleCodes[i], fileNames[i]);
            if (metadataOpt.isPresent()) {
                storage.store(metadataOpt.get());
                log.info("✅ Stored metadata for: {}", fileNames[i]);
            }
        }
        
        // Retrieve and demonstrate
        Optional<CodeMetadata> userMetadata = storage.get("User.java");
        if (userMetadata.isPresent()) {
            CodeMetadata metadata = userMetadata.get();
            log.info("📁 Retrieved metadata for User.java:");
            log.info("  - Package: {}", metadata.getPackageName());
            log.info("  - Classes: {}", metadata.getClasses().size());
            log.info("  - Methods: {}", 
                metadata.getClasses().stream().mapToInt(c -> c.getMethodCount()).sum());
        }
        
        // Search by package
        var packageMetadata = storage.findByPackage("com.example.model");
        log.info("🔍 Found {} files in package com.example.model", packageMetadata.size());
        
        // Show storage stats
        var stats = storage.getStats();
        log.info("📊 Storage Statistics:");
        log.info("  - Stored Files: {}", stats.getStoredFiles());
        log.info("  - Cache Size: {}", stats.getCacheSize());
        log.info("  - Total Storage Size: {} bytes", stats.getTotalStorageSize());
        
        // Export for LLM consumption
        storage.exportToLLMFormat("data/ast-metadata/llm-export.json");
        log.info("📤 Exported metadata to LLM format");
    }
}