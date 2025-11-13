package com.noteflix.pcm.ast.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class MethodFlowInfo {
    
    private String methodName;
    private String returnType;
    private String signature;
    private CodePosition position;
    
    // Method characteristics
    private boolean isPublic;
    private boolean isPrivate;
    private boolean isProtected;
    private boolean isStatic;
    private boolean isAbstract;
    private boolean isFinal;
    private boolean isSynchronized;
    
    // Parameters and variables
    private List<ParameterInfo> parameters;
    private List<VariableInfo> localVariables;
    
    // Control flow
    private List<ControlFlowNode> controlFlowNodes;
    private List<MethodCallInfo> methodCalls;
    private List<FieldAccessInfo> fieldAccesses;
    
    // Exception handling
    private List<String> thrownExceptions;
    private List<TryCatchInfo> tryCatchBlocks;
    
    // Complexity metrics
    private int cyclomaticComplexity;
    private int linesOfCode;
    private int cognitiveComplexity;
    
    @Data
    @Builder
    public static class ParameterInfo {
        private String name;
        private String type;
        private CodePosition position;
        private boolean isFinal;
        private boolean isVarargs;
    }
    
    @Data
    @Builder
    public static class VariableInfo {
        private String name;
        private String type;
        private CodePosition declarationPosition;
        private CodePosition scope;
        private boolean isFinal;
        private String initialValue;
    }
    
    @Data
    @Builder
    public static class ControlFlowNode {
        private String nodeType; // IF, WHILE, FOR, SWITCH, etc.
        private String condition;
        private CodePosition position;
        private List<String> branches;
        private int depth;
    }
    
    @Data
    @Builder
    public static class MethodCallInfo {
        private String targetMethod;
        private String targetClass;
        private String callType; // INSTANCE, STATIC, CONSTRUCTOR
        private CodePosition position;
        private List<String> arguments;
        private boolean isChained;
        private String chainContext;
    }
    
    @Data
    @Builder
    public static class FieldAccessInfo {
        private String fieldName;
        private String fieldType;
        private String accessType; // READ, WRITE, READ_WRITE
        private CodePosition position;
        private boolean isStatic;
        private String ownerClass;
    }
    
    @Data
    @Builder
    public static class TryCatchInfo {
        private CodePosition tryPosition;
        private List<CatchClause> catchClauses;
        private CodePosition finallyPosition;
        
        @Data
        @Builder
        public static class CatchClause {
            private String exceptionType;
            private String exceptionName;
            private CodePosition position;
        }
    }
}