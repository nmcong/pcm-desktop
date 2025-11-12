package com.noteflix.pcm.ast;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ASTController {
    
    private final ASTService astService;
    
    public ASTController() {
        this.astService = new ASTService();
    }
    
    public Map<String, Object> analyzeFile(String filePath) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            ASTAnalysisResult result = astService.analyzeJavaFile(filePath);
            
            response.put("success", result.isSuccessful());
            
            if (result.isSuccessful()) {
                response.put("data", result);
                response.put("message", "File analyzed successfully");
            } else {
                response.put("error", result.getErrorMessage());
            }
            
        } catch (Exception e) {
            log.error("Error in analyzeFile controller", e);
            response.put("success", false);
            response.put("error", "Internal server error: " + e.getMessage());
        }
        
        return response;
    }
    
    public Map<String, Object> analyzeCode(String javaCode) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (javaCode == null || javaCode.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Java code cannot be empty");
                return response;
            }
            
            ASTAnalysisResult result = astService.analyzeJavaCode(javaCode);
            
            response.put("success", result.isSuccessful());
            
            if (result.isSuccessful()) {
                response.put("data", result);
                response.put("message", "Code analyzed successfully");
            } else {
                response.put("error", result.getErrorMessage());
            }
            
        } catch (Exception e) {
            log.error("Error in analyzeCode controller", e);
            response.put("success", false);
            response.put("error", "Internal server error: " + e.getMessage());
        }
        
        return response;
    }
    
    public Map<String, Object> analyzeProject(String projectRootPath) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (projectRootPath == null || projectRootPath.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Project root path cannot be empty");
                return response;
            }
            
            Map<String, Object> analysis = astService.getProjectStructureAnalysis(projectRootPath);
            
            if (analysis.containsKey("error")) {
                response.put("success", false);
                response.put("error", analysis.get("error"));
            } else {
                response.put("success", true);
                response.put("data", analysis);
                response.put("message", "Project analyzed successfully");
            }
            
        } catch (Exception e) {
            log.error("Error in analyzeProject controller", e);
            response.put("success", false);
            response.put("error", "Internal server error: " + e.getMessage());
        }
        
        return response;
    }
    
    public Map<String, Object> getAnalysisAsJson(String javaCode) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (javaCode == null || javaCode.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Java code cannot be empty");
                return response;
            }
            
            String jsonResult = astService.getAnalysisAsJson(javaCode);
            
            response.put("success", true);
            response.put("json", jsonResult);
            response.put("message", "JSON analysis completed successfully");
            
        } catch (Exception e) {
            log.error("Error in getAnalysisAsJson controller", e);
            response.put("success", false);
            response.put("error", "Internal server error: " + e.getMessage());
        }
        
        return response;
    }
    
    public Map<String, Object> getCacheInfo() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("success", true);
            response.put("cacheSize", astService.getCacheSize());
            response.put("message", "Cache info retrieved successfully");
            
        } catch (Exception e) {
            log.error("Error in getCacheInfo controller", e);
            response.put("success", false);
            response.put("error", "Internal server error: " + e.getMessage());
        }
        
        return response;
    }
    
    public Map<String, Object> clearCache() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            astService.clearCache();
            
            response.put("success", true);
            response.put("message", "Cache cleared successfully");
            
        } catch (Exception e) {
            log.error("Error in clearCache controller", e);
            response.put("success", false);
            response.put("error", "Internal server error: " + e.getMessage());
        }
        
        return response;
    }
}