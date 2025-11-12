package com.noteflix.pcm.ast;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ASTAnalysisResult {
    
    private boolean successful;
    private String errorMessage;
    private String packageName;
    private List<String> imports;
    private List<ClassInfo> classes;
    private List<MethodInfo> methods;
    
    @Data
    @Builder
    public static class ClassInfo {
        private String name;
        private boolean isInterface;
        private boolean isAbstract;
        private boolean isPublic;
    }
    
    @Data
    @Builder
    public static class MethodInfo {
        private String name;
        private String returnType;
        private boolean isPublic;
        private boolean isPrivate;
        private boolean isStatic;
        private int parameterCount;
    }
}