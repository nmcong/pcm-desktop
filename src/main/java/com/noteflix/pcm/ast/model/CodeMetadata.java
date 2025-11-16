package com.noteflix.pcm.ast.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class CodeMetadata {

    // File information
    private String fileName;
    private String filePath;
    private String packageName;
    private long fileSize;
    private LocalDateTime lastModified;
    private String fileHash;

    // AST analysis results
    private List<ClassMetadata> classes;
    private List<InterfaceMetadata> interfaces;
    private List<String> imports;

    // Code metrics
    private int totalLines;
    private int linesOfCode;
    private int commentLines;
    private int blankLines;

    // Dependencies
    private List<String> externalDependencies;
    private List<String> internalDependencies;

    // Analysis metadata
    private LocalDateTime analyzedAt;
    private String analysisVersion;
    private Map<String, Object> customMetadata;

    @Data
    @Builder
    public static class ClassMetadata {
        private String className;
        private String fullQualifiedName;
        private CodePosition position;

        // Class characteristics
        private boolean isPublic;
        private boolean isAbstract;
        private boolean isFinal;
        private boolean isStatic;
        private boolean isInner;

        // Inheritance
        private String superClass;
        private List<String> interfaces;
        private List<String> annotations;

        // Members
        private List<MethodMetadata> methods;
        private List<FieldMetadata> fields;
        private List<ConstructorMetadata> constructors;

        // Metrics
        private int methodCount;
        private int fieldCount;
        private int cyclomaticComplexity;
        private int linesOfCode;
    }

    @Data
    @Builder
    public static class InterfaceMetadata {
        private String interfaceName;
        private String fullQualifiedName;
        private CodePosition position;

        private boolean isPublic;
        private List<String> extendedInterfaces;
        private List<String> annotations;

        private List<MethodMetadata> methods;
        private List<FieldMetadata> constants;
    }

    @Data
    @Builder
    public static class MethodMetadata {
        private String methodName;
        private String signature;
        private CodePosition position;

        // Method details from MethodFlowInfo
        private MethodFlowInfo flowInfo;

        // Quick access properties
        private String returnType;
        private List<String> parameterTypes;
        private List<String> annotations;

        // Documentation
        private String javadoc;
        private List<String> comments;

        // Usage tracking
        private List<String> calledBy;
        private List<String> calls;
    }

    @Data
    @Builder
    public static class FieldMetadata {
        private String fieldName;
        private String type;
        private CodePosition position;

        private boolean isPublic;
        private boolean isPrivate;
        private boolean isProtected;
        private boolean isStatic;
        private boolean isFinal;
        private boolean isVolatile;
        private boolean isTransient;

        private String initialValue;
        private List<String> annotations;
        private String javadoc;
    }

    @Data
    @Builder
    public static class ConstructorMetadata {
        private String signature;
        private CodePosition position;

        private boolean isPublic;
        private boolean isPrivate;
        private boolean isProtected;

        private List<String> parameterTypes;
        private List<String> annotations;
        private String javadoc;

        private MethodFlowInfo flowInfo;
    }
}
