package com.noteflix.pcm.ast.llm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.noteflix.pcm.ast.model.CodeMetadata;
import com.noteflix.pcm.ast.model.CodePosition;
import com.noteflix.pcm.ast.model.MethodFlowInfo;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LLM-optimized representation of Java code.
 *
 * <p>This class provides structured, semantic-rich representations of source code designed
 * specifically for LLM consumption and understanding.
 */
@Slf4j
public class LLMCodeRepresentation {

    private final ObjectMapper objectMapper;

    public LLMCodeRepresentation() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * Convert CodeMetadata to LLM-friendly structured format
     */
    public LLMCodeStructure toLLMStructure(CodeMetadata metadata) {
        LLMCodeStructure.LLMCodeStructureBuilder builder = LLMCodeStructure.builder();

        // File context
        builder.fileInfo(
                LLMFileInfo.builder()
                        .fileName(metadata.getFileName())
                        .packageName(metadata.getPackageName())
                        .imports(metadata.getImports())
                        .totalLines(metadata.getTotalLines())
                        .linesOfCode(metadata.getLinesOfCode())
                        .build());

        // Convert classes
        List<LLMClassInfo> llmClasses =
                metadata.getClasses().stream().map(this::convertClassMetadata).collect(Collectors.toList());
        builder.classes(llmClasses);

        // Convert interfaces
        List<LLMInterfaceInfo> llmInterfaces =
                metadata.getInterfaces().stream()
                        .map(this::convertInterfaceMetadata)
                        .collect(Collectors.toList());
        builder.interfaces(llmInterfaces);

        // Dependencies and relationships
        builder.dependencies(extractDependencies(metadata));
        builder.codeMetrics(extractCodeMetrics(metadata));

        return builder.build();
    }

    /**
     * Convert to JSON string optimized for LLM prompts
     */
    public String toJson(CodeMetadata metadata) {
        try {
            LLMCodeStructure structure = toLLMStructure(metadata);
            return objectMapper.writeValueAsString(structure);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert to JSON", e);
            return "{}";
        }
    }

    /**
     * Generate natural language summary for LLM context
     */
    public String toNaturalLanguageSummary(CodeMetadata metadata) {
        StringBuilder summary = new StringBuilder();

        summary.append("### Code Analysis Summary\n\n");
        summary.append(String.format("**File**: %s\n", metadata.getFileName()));

        if (metadata.getPackageName() != null) {
            summary.append(String.format("**Package**: %s\n", metadata.getPackageName()));
        }

        summary.append(String.format("**Lines of Code**: %d\n", metadata.getLinesOfCode()));
        summary.append(String.format("**Total Classes**: %d\n", metadata.getClasses().size()));
        summary.append(String.format("**Total Interfaces**: %d\n", metadata.getInterfaces().size()));

        // Class summaries
        if (!metadata.getClasses().isEmpty()) {
            summary.append("\n#### Classes:\n");
            for (CodeMetadata.ClassMetadata clazz : metadata.getClasses()) {
                summary.append(
                        String.format(
                                "- **%s**: %d methods, %d fields",
                                clazz.getClassName(), clazz.getMethodCount(), clazz.getFieldCount()));

                if (clazz.getSuperClass() != null) {
                    summary.append(String.format(" (extends %s)", clazz.getSuperClass()));
                }

                if (!clazz.getInterfaces().isEmpty()) {
                    summary.append(
                            String.format(" (implements %s)", String.join(", ", clazz.getInterfaces())));
                }
                summary.append("\n");

                // Method details
                for (CodeMetadata.MethodMetadata method : clazz.getMethods()) {
                    summary.append(
                            String.format(
                                    "  - `%s`: %s", method.getMethodName(), generateMethodSignature(method)));

                    if (method.getFlowInfo() != null) {
                        MethodFlowInfo flow = method.getFlowInfo();
                        summary.append(
                                String.format(
                                        " (complexity: %d, calls: %d)",
                                        flow.getCyclomaticComplexity(), flow.getMethodCalls().size()));
                    }
                    summary.append("\n");
                }
            }
        }

        // Interface summaries
        if (!metadata.getInterfaces().isEmpty()) {
            summary.append("\n#### Interfaces:\n");
            for (CodeMetadata.InterfaceMetadata iface : metadata.getInterfaces()) {
                summary.append(
                        String.format(
                                "- **%s**: %d methods\n", iface.getInterfaceName(), iface.getMethods().size()));
            }
        }

        return summary.toString();
    }

    /**
     * Generate method flow diagram in text format
     */
    public String generateMethodFlowDiagram(MethodFlowInfo methodFlow) {
        StringBuilder diagram = new StringBuilder();

        diagram.append(String.format("### Method Flow: %s\n\n", methodFlow.getMethodName()));
        diagram.append(String.format("**Signature**: %s\n", methodFlow.getSignature()));
        diagram.append(String.format("**Complexity**: %d\n", methodFlow.getCyclomaticComplexity()));
        diagram.append(String.format("**Lines**: %d\n\n", methodFlow.getLinesOfCode()));

        // Control flow
        if (!methodFlow.getControlFlowNodes().isEmpty()) {
            diagram.append("#### Control Flow:\n");
            for (MethodFlowInfo.ControlFlowNode node : methodFlow.getControlFlowNodes()) {
                String indent = "  ".repeat(node.getDepth());
                diagram.append(
                        String.format(
                                "%s- %s: %s (line %d)\n",
                                indent,
                                node.getNodeType(),
                                node.getCondition(),
                                node.getPosition().getStartLine()));
            }
            diagram.append("\n");
        }

        // Method calls
        if (!methodFlow.getMethodCalls().isEmpty()) {
            diagram.append("#### Method Calls:\n");
            for (MethodFlowInfo.MethodCallInfo call : methodFlow.getMethodCalls()) {
                diagram.append(
                        String.format(
                                "- %s.%s() (line %d)\n",
                                call.getTargetClass() != null ? call.getTargetClass() : "this",
                                call.getTargetMethod(),
                                call.getPosition().getStartLine()));
            }
            diagram.append("\n");
        }

        // Variables
        if (!methodFlow.getLocalVariables().isEmpty()) {
            diagram.append("#### Local Variables:\n");
            for (MethodFlowInfo.VariableInfo var : methodFlow.getLocalVariables()) {
                diagram.append(
                        String.format(
                                "- %s %s (line %d)",
                                var.getType(), var.getName(), var.getDeclarationPosition().getStartLine()));
                if (var.getInitialValue() != null) {
                    diagram.append(String.format(" = %s", var.getInitialValue()));
                }
                diagram.append("\n");
            }
        }

        return diagram.toString();
    }

    private LLMClassInfo convertClassMetadata(CodeMetadata.ClassMetadata clazz) {
        return LLMClassInfo.builder()
                .name(clazz.getClassName())
                .fullName(clazz.getFullQualifiedName())
                .location(convertPosition(clazz.getPosition()))
                .modifiers(extractModifiers(clazz))
                .superClass(clazz.getSuperClass())
                .interfaces(clazz.getInterfaces())
                .methods(
                        clazz.getMethods().stream()
                                .map(this::convertMethodMetadata)
                                .collect(Collectors.toList()))
                .fields(
                        clazz.getFields().stream().map(this::convertFieldMetadata).collect(Collectors.toList()))
                .metrics(
                        LLMMetrics.builder()
                                .methodCount(clazz.getMethodCount())
                                .fieldCount(clazz.getFieldCount())
                                .cyclomaticComplexity(clazz.getCyclomaticComplexity())
                                .linesOfCode(clazz.getLinesOfCode())
                                .build())
                .build();
    }

    private LLMInterfaceInfo convertInterfaceMetadata(CodeMetadata.InterfaceMetadata iface) {
        return LLMInterfaceInfo.builder()
                .name(iface.getInterfaceName())
                .fullName(iface.getFullQualifiedName())
                .location(convertPosition(iface.getPosition()))
                .extendedInterfaces(iface.getExtendedInterfaces())
                .methods(
                        iface.getMethods().stream()
                                .map(this::convertMethodMetadata)
                                .collect(Collectors.toList()))
                .constants(
                        iface.getConstants().stream()
                                .map(this::convertFieldMetadata)
                                .collect(Collectors.toList()))
                .build();
    }

    private LLMMethodInfo convertMethodMetadata(CodeMetadata.MethodMetadata method) {
        LLMMethodInfo.LLMMethodInfoBuilder builder =
                LLMMethodInfo.builder()
                        .name(method.getMethodName())
                        .signature(method.getSignature())
                        .returnType(method.getReturnType())
                        .parameterTypes(method.getParameterTypes())
                        .location(convertPosition(method.getPosition()))
                        .javadoc(method.getJavadoc());

        if (method.getFlowInfo() != null) {
            builder.flowAnalysis(convertMethodFlow(method.getFlowInfo()));
        }

        return builder.build();
    }

    private LLMFieldInfo convertFieldMetadata(CodeMetadata.FieldMetadata field) {
        return LLMFieldInfo.builder()
                .name(field.getFieldName())
                .type(field.getType())
                .location(convertPosition(field.getPosition()))
                .modifiers(extractFieldModifiers(field))
                .initialValue(field.getInitialValue())
                .javadoc(field.getJavadoc())
                .build();
    }

    private LLMMethodFlow convertMethodFlow(MethodFlowInfo flow) {
        return LLMMethodFlow.builder()
                .complexity(flow.getCyclomaticComplexity())
                .linesOfCode(flow.getLinesOfCode())
                .parameters(
                        flow.getParameters().stream()
                                .map(p -> String.format("%s %s", p.getType(), p.getName()))
                                .collect(Collectors.toList()))
                .localVariables(
                        flow.getLocalVariables().stream()
                                .map(v -> String.format("%s %s", v.getType(), v.getName()))
                                .collect(Collectors.toList()))
                .methodCalls(
                        flow.getMethodCalls().stream()
                                .map(
                                        c ->
                                                String.format(
                                                        "%s.%s()",
                                                        c.getTargetClass() != null ? c.getTargetClass() : "this",
                                                        c.getTargetMethod()))
                                .collect(Collectors.toList()))
                .controlFlow(
                        flow.getControlFlowNodes().stream()
                                .map(n -> String.format("%s: %s", n.getNodeType(), n.getCondition()))
                                .collect(Collectors.toList()))
                .build();
    }

    private LLMLocation convertPosition(CodePosition position) {
        if (position == null) return null;

        return LLMLocation.builder()
                .startLine(position.getStartLine())
                .endLine(position.getEndLine())
                .startColumn(position.getStartColumn())
                .endColumn(position.getEndColumn())
                .build();
    }

    private List<String> extractModifiers(CodeMetadata.ClassMetadata clazz) {
        List<String> modifiers = new ArrayList<>();
        if (clazz.isPublic()) modifiers.add("public");
        if (clazz.isAbstract()) modifiers.add("abstract");
        if (clazz.isFinal()) modifiers.add("final");
        if (clazz.isStatic()) modifiers.add("static");
        return modifiers;
    }

    private List<String> extractFieldModifiers(CodeMetadata.FieldMetadata field) {
        List<String> modifiers = new ArrayList<>();
        if (field.isPublic()) modifiers.add("public");
        if (field.isPrivate()) modifiers.add("private");
        if (field.isProtected()) modifiers.add("protected");
        if (field.isStatic()) modifiers.add("static");
        if (field.isFinal()) modifiers.add("final");
        return modifiers;
    }

    private Map<String, Object> extractDependencies(CodeMetadata metadata) {
        Map<String, Object> dependencies = new HashMap<>();
        dependencies.put("imports", metadata.getImports());
        dependencies.put("externalDependencies", metadata.getExternalDependencies());
        dependencies.put("internalDependencies", metadata.getInternalDependencies());
        return dependencies;
    }

    private LLMMetrics extractCodeMetrics(CodeMetadata metadata) {
        return LLMMetrics.builder()
                .totalLines(metadata.getTotalLines())
                .linesOfCode(metadata.getLinesOfCode())
                .commentLines(metadata.getCommentLines())
                .blankLines(metadata.getBlankLines())
                .classCount(metadata.getClasses().size())
                .interfaceCount(metadata.getInterfaces().size())
                .build();
    }

    private String generateMethodSignature(CodeMetadata.MethodMetadata method) {
        StringBuilder sig = new StringBuilder();
        sig.append(method.getReturnType()).append(" ");
        sig.append(method.getMethodName()).append("(");
        sig.append(String.join(", ", method.getParameterTypes()));
        sig.append(")");
        return sig.toString();
    }

    // LLM-optimized data structures

    @Data
    @Builder
    public static class LLMCodeStructure {
        private LLMFileInfo fileInfo;
        private List<LLMClassInfo> classes;
        private List<LLMInterfaceInfo> interfaces;
        private Map<String, Object> dependencies;
        private LLMMetrics codeMetrics;
    }

    @Data
    @Builder
    public static class LLMFileInfo {
        private String fileName;
        private String packageName;
        private List<String> imports;
        private int totalLines;
        private int linesOfCode;
    }

    @Data
    @Builder
    public static class LLMClassInfo {
        private String name;
        private String fullName;
        private LLMLocation location;
        private List<String> modifiers;
        private String superClass;
        private List<String> interfaces;
        private List<LLMMethodInfo> methods;
        private List<LLMFieldInfo> fields;
        private LLMMetrics metrics;
    }

    @Data
    @Builder
    public static class LLMInterfaceInfo {
        private String name;
        private String fullName;
        private LLMLocation location;
        private List<String> extendedInterfaces;
        private List<LLMMethodInfo> methods;
        private List<LLMFieldInfo> constants;
    }

    @Data
    @Builder
    public static class LLMMethodInfo {
        private String name;
        private String signature;
        private String returnType;
        private List<String> parameterTypes;
        private LLMLocation location;
        private String javadoc;
        private LLMMethodFlow flowAnalysis;
    }

    @Data
    @Builder
    public static class LLMFieldInfo {
        private String name;
        private String type;
        private LLMLocation location;
        private List<String> modifiers;
        private String initialValue;
        private String javadoc;
    }

    @Data
    @Builder
    public static class LLMMethodFlow {
        private int complexity;
        private int linesOfCode;
        private List<String> parameters;
        private List<String> localVariables;
        private List<String> methodCalls;
        private List<String> controlFlow;
    }

    @Data
    @Builder
    public static class LLMLocation {
        private int startLine;
        private int endLine;
        private int startColumn;
        private int endColumn;
    }

    @Data
    @Builder
    public static class LLMMetrics {
        private int totalLines;
        private int linesOfCode;
        private int commentLines;
        private int blankLines;
        private int classCount;
        private int interfaceCount;
        private int methodCount;
        private int fieldCount;
        private int cyclomaticComplexity;
    }
}
