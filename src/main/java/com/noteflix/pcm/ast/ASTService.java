package com.noteflix.pcm.ast;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ASTService {

  private final ASTParser astParser;
  private final ObjectMapper objectMapper;
  private final Map<String, ASTAnalysisResult> analysisCache;

  public ASTService() {
    this.astParser = new ASTParser();
    this.objectMapper = new ObjectMapper();
    this.analysisCache = new ConcurrentHashMap<>();
  }

  public ASTAnalysisResult analyzeJavaFile(String filePath) {
    if (analysisCache.containsKey(filePath)) {
      log.debug("Returning cached analysis result for: {}", filePath);
      return analysisCache.get(filePath);
    }

    try {
      Path path = Paths.get(filePath);

      if (!Files.exists(path)) {
        return ASTAnalysisResult.builder()
            .successful(false)
            .errorMessage("File not found: " + filePath)
            .build();
      }

      String javaCode = Files.readString(path);
      ASTAnalysisResult result = astParser.analyzeJavaCode(javaCode);

      if (result.isSuccessful()) {
        analysisCache.put(filePath, result);
        log.info("Successfully analyzed Java file: {}", filePath);
      }

      return result;
    } catch (IOException e) {
      log.error("Error reading Java file: {}", filePath, e);
      return ASTAnalysisResult.builder()
          .successful(false)
          .errorMessage("Error reading file: " + e.getMessage())
          .build();
    }
  }

  public ASTAnalysisResult analyzeJavaCode(String javaCode) {
    return astParser.analyzeJavaCode(javaCode);
  }

  public String getAnalysisAsJson(String javaCode) {
    try {
      ASTAnalysisResult result = astParser.analyzeJavaCode(javaCode);
      return objectMapper.writeValueAsString(result);
    } catch (Exception e) {
      log.error("Error converting analysis result to JSON", e);
      return "{\"successful\": false, \"errorMessage\": \"Failed to serialize result\"}";
    }
  }

  public Map<String, Object> getProjectStructureAnalysis(String projectRootPath) {
    Map<String, Object> analysis = new HashMap<>();

    try {
      Path rootPath = Paths.get(projectRootPath);

      if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
        analysis.put("error", "Project root path not found or not a directory: " + projectRootPath);
        return analysis;
      }

      Map<String, ASTAnalysisResult> fileAnalysis = new HashMap<>();

      Files.walk(rootPath)
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".java"))
          .forEach(
              javaFile -> {
                String relativePath = rootPath.relativize(javaFile).toString();
                ASTAnalysisResult result = analyzeJavaFile(javaFile.toString());
                if (result.isSuccessful()) {
                  fileAnalysis.put(relativePath, result);
                }
              });

      analysis.put("totalJavaFiles", fileAnalysis.size());
      analysis.put("fileAnalysis", fileAnalysis);

      int totalClasses =
          fileAnalysis.values().stream()
              .mapToInt(result -> result.getClasses() != null ? result.getClasses().size() : 0)
              .sum();

      int totalMethods =
          fileAnalysis.values().stream()
              .mapToInt(result -> result.getMethods() != null ? result.getMethods().size() : 0)
              .sum();

      analysis.put("totalClasses", totalClasses);
      analysis.put("totalMethods", totalMethods);

      log.info("Project structure analysis completed for: {}", projectRootPath);

    } catch (IOException e) {
      log.error("Error analyzing project structure: {}", projectRootPath, e);
      analysis.put("error", "Error walking project directory: " + e.getMessage());
    }

    return analysis;
  }

  public void clearCache() {
    analysisCache.clear();
    log.info("AST analysis cache cleared");
  }

  public int getCacheSize() {
    return analysisCache.size();
  }
}
