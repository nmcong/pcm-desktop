package com.noteflix.pcm.ast.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.noteflix.pcm.ast.model.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class EnhancedASTAnalyzer {
    
    private final JavaParser javaParser;
    
    public EnhancedASTAnalyzer() {
        this.javaParser = new JavaParser();
    }
    
    public Optional<CodeMetadata> analyzeFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            
            if (!Files.exists(path)) {
                log.error("File not found: {}", filePath);
                return Optional.empty();
            }
            
            String content = Files.readString(path);
            return analyzeCode(content, filePath);
            
        } catch (IOException e) {
            log.error("Error reading file: {}", filePath, e);
            return Optional.empty();
        }
    }
    
    public Optional<CodeMetadata> analyzeCode(String javaCode, String fileName) {
        try {
            ParseResult<CompilationUnit> parseResult = javaParser.parse(javaCode);
            
            if (!parseResult.isSuccessful() || parseResult.getResult().isEmpty()) {
                log.error("Failed to parse Java code. Errors: {}", parseResult.getProblems());
                return Optional.empty();
            }
            
            CompilationUnit cu = parseResult.getResult().get();
            return Optional.of(buildCodeMetadata(cu, javaCode, fileName));
            
        } catch (Exception e) {
            log.error("Exception while analyzing Java code", e);
            return Optional.empty();
        }
    }
    
    private CodeMetadata buildCodeMetadata(CompilationUnit cu, String content, String fileName) {
        CodeMetadata.CodeMetadataBuilder builder = CodeMetadata.builder();
        
        // Basic file information
        builder.fileName(fileName)
               .filePath(fileName)
               .fileSize(content.length())
               .lastModified(LocalDateTime.now())
               .analyzedAt(LocalDateTime.now())
               .analysisVersion("1.0.0");
        
        // Package information
        cu.getPackageDeclaration().ifPresent(pkg -> 
            builder.packageName(pkg.getNameAsString()));
        
        // Imports
        List<String> imports = new ArrayList<>();
        cu.getImports().forEach(importDecl -> 
            imports.add(importDecl.getNameAsString()));
        builder.imports(imports);
        
        // Code metrics
        String[] lines = content.split("\n");
        builder.totalLines(lines.length);
        
        // Analyze classes and interfaces
        StructureAnalyzer analyzer = new StructureAnalyzer(content);
        cu.accept(analyzer, null);
        
        builder.classes(analyzer.getClasses())
               .interfaces(analyzer.getInterfaces())
               .linesOfCode(analyzer.getLinesOfCode())
               .commentLines(analyzer.getCommentLines())
               .blankLines(analyzer.getBlankLines());
        
        return builder.build();
    }
    
    private static class StructureAnalyzer extends VoidVisitorAdapter<Void> {
        
        private final String sourceCode;
        private final List<CodeMetadata.ClassMetadata> classes = new ArrayList<>();
        private final List<CodeMetadata.InterfaceMetadata> interfaces = new ArrayList<>();
        
        private int linesOfCode = 0;
        private int commentLines = 0;
        private int blankLines = 0;
        
        public StructureAnalyzer(String sourceCode) {
            this.sourceCode = sourceCode;
            calculateLineMetrics();
        }
        
        private void calculateLineMetrics() {
            String[] lines = sourceCode.split("\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    blankLines++;
                } else if (trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("*")) {
                    commentLines++;
                } else {
                    linesOfCode++;
                }
            }
        }
        
        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void arg) {
            if (n.isInterface()) {
                interfaces.add(buildInterfaceMetadata(n));
            } else {
                classes.add(buildClassMetadata(n));
            }
            super.visit(n, arg);
        }
        
        private CodeMetadata.ClassMetadata buildClassMetadata(ClassOrInterfaceDeclaration n) {
            CodeMetadata.ClassMetadata.ClassMetadataBuilder builder = CodeMetadata.ClassMetadata.builder();
            
            builder.className(n.getNameAsString())
                   .fullQualifiedName(n.getFullyQualifiedName().orElse(n.getNameAsString()))
                   .position(createCodePosition(n, n.getNameAsString()))
                   .isPublic(n.isPublic())
                   .isAbstract(n.isAbstract())
                   .isFinal(n.isFinal())
                   .isStatic(n.isStatic())
                   .isInner(n.isInnerClass());
            
            // Inheritance
            if (n.getExtendedTypes().isNonEmpty()) {
                builder.superClass(n.getExtendedTypes().get(0).getNameAsString());
            }
            
            List<String> implementedInterfaces = new ArrayList<>();
            n.getImplementedTypes().forEach(type -> 
                implementedInterfaces.add(type.getNameAsString()));
            builder.interfaces(implementedInterfaces);
            
            // Annotations
            List<String> annotations = new ArrayList<>();
            n.getAnnotations().forEach(anno -> 
                annotations.add(anno.getNameAsString()));
            builder.annotations(annotations);
            
            // Analyze members
            List<CodeMetadata.MethodMetadata> methods = new ArrayList<>();
            List<CodeMetadata.FieldMetadata> fields = new ArrayList<>();
            List<CodeMetadata.ConstructorMetadata> constructors = new ArrayList<>();
            
            for (BodyDeclaration<?> member : n.getMembers()) {
                if (member instanceof MethodDeclaration) {
                    methods.add(buildMethodMetadata((MethodDeclaration) member));
                } else if (member instanceof FieldDeclaration) {
                    ((FieldDeclaration) member).getVariables().forEach(var ->
                        fields.add(buildFieldMetadata(var, (FieldDeclaration) member)));
                } else if (member instanceof ConstructorDeclaration) {
                    constructors.add(buildConstructorMetadata((ConstructorDeclaration) member));
                }
            }
            
            return builder.methods(methods)
                         .fields(fields)
                         .constructors(constructors)
                         .methodCount(methods.size())
                         .fieldCount(fields.size())
                         .build();
        }
        
        private CodeMetadata.InterfaceMetadata buildInterfaceMetadata(ClassOrInterfaceDeclaration n) {
            CodeMetadata.InterfaceMetadata.InterfaceMetadataBuilder builder = 
                CodeMetadata.InterfaceMetadata.builder();
            
            builder.interfaceName(n.getNameAsString())
                   .fullQualifiedName(n.getFullyQualifiedName().orElse(n.getNameAsString()))
                   .position(createCodePosition(n, n.getNameAsString()))
                   .isPublic(n.isPublic());
            
            List<String> extendedInterfaces = new ArrayList<>();
            n.getExtendedTypes().forEach(type -> 
                extendedInterfaces.add(type.getNameAsString()));
            builder.extendedInterfaces(extendedInterfaces);
            
            List<String> annotations = new ArrayList<>();
            n.getAnnotations().forEach(anno -> 
                annotations.add(anno.getNameAsString()));
            builder.annotations(annotations);
            
            List<CodeMetadata.MethodMetadata> methods = new ArrayList<>();
            List<CodeMetadata.FieldMetadata> constants = new ArrayList<>();
            
            for (BodyDeclaration<?> member : n.getMembers()) {
                if (member instanceof MethodDeclaration) {
                    methods.add(buildMethodMetadata((MethodDeclaration) member));
                } else if (member instanceof FieldDeclaration) {
                    ((FieldDeclaration) member).getVariables().forEach(var ->
                        constants.add(buildFieldMetadata(var, (FieldDeclaration) member)));
                }
            }
            
            return builder.methods(methods).constants(constants).build();
        }
        
        private CodeMetadata.MethodMetadata buildMethodMetadata(MethodDeclaration n) {
            CodeMetadata.MethodMetadata.MethodMetadataBuilder builder = 
                CodeMetadata.MethodMetadata.builder();
            
            builder.methodName(n.getNameAsString())
                   .signature(n.getSignature().asString())
                   .position(createCodePosition(n, n.getNameAsString()))
                   .returnType(n.getTypeAsString());
            
            List<String> paramTypes = new ArrayList<>();
            n.getParameters().forEach(param -> 
                paramTypes.add(param.getTypeAsString()));
            builder.parameterTypes(paramTypes);
            
            List<String> annotations = new ArrayList<>();
            n.getAnnotations().forEach(anno -> 
                annotations.add(anno.getNameAsString()));
            builder.annotations(annotations);
            
            // Javadoc
            n.getJavadocComment().ifPresent(javadoc -> 
                builder.javadoc(javadoc.getContent()));
            
            // Detailed flow analysis
            MethodFlowInfo flowInfo = analyzeMethodFlow(n);
            builder.flowInfo(flowInfo);
            
            return builder.build();
        }
        
        private CodeMetadata.FieldMetadata buildFieldMetadata(VariableDeclarator var, FieldDeclaration field) {
            CodeMetadata.FieldMetadata.FieldMetadataBuilder builder = 
                CodeMetadata.FieldMetadata.builder();
            
            builder.fieldName(var.getNameAsString())
                   .type(var.getTypeAsString())
                   .position(createCodePosition(var, var.getNameAsString()))
                   .isPublic(field.isPublic())
                   .isPrivate(field.isPrivate())
                   .isProtected(field.isProtected())
                   .isStatic(field.isStatic())
                   .isFinal(field.isFinal());
            
            var.getInitializer().ifPresent(init -> 
                builder.initialValue(init.toString()));
            
            List<String> annotations = new ArrayList<>();
            field.getAnnotations().forEach(anno -> 
                annotations.add(anno.getNameAsString()));
            builder.annotations(annotations);
            
            field.getJavadocComment().ifPresent(javadoc -> 
                builder.javadoc(javadoc.getContent()));
            
            return builder.build();
        }
        
        private CodeMetadata.ConstructorMetadata buildConstructorMetadata(ConstructorDeclaration n) {
            CodeMetadata.ConstructorMetadata.ConstructorMetadataBuilder builder = 
                CodeMetadata.ConstructorMetadata.builder();
            
            builder.signature(n.getSignature().asString())
                   .position(createCodePosition(n, n.getNameAsString()))
                   .isPublic(n.isPublic())
                   .isPrivate(n.isPrivate())
                   .isProtected(n.isProtected());
            
            List<String> paramTypes = new ArrayList<>();
            n.getParameters().forEach(param -> 
                paramTypes.add(param.getTypeAsString()));
            builder.parameterTypes(paramTypes);
            
            List<String> annotations = new ArrayList<>();
            n.getAnnotations().forEach(anno -> 
                annotations.add(anno.getNameAsString()));
            builder.annotations(annotations);
            
            n.getJavadocComment().ifPresent(javadoc -> 
                builder.javadoc(javadoc.getContent()));
            
            return builder.build();
        }
        
        private MethodFlowInfo analyzeMethodFlow(MethodDeclaration method) {
            MethodFlowAnalyzer flowAnalyzer = new MethodFlowAnalyzer();
            method.accept(flowAnalyzer, null);
            
            MethodFlowInfo.MethodFlowInfoBuilder builder = MethodFlowInfo.builder();
            
            builder.methodName(method.getNameAsString())
                   .returnType(method.getTypeAsString())
                   .signature(method.getSignature().asString())
                   .position(createCodePosition(method, method.getNameAsString()))
                   .isPublic(method.isPublic())
                   .isPrivate(method.isPrivate())
                   .isProtected(method.isProtected())
                   .isStatic(method.isStatic())
                   .isAbstract(method.isAbstract())
                   .isFinal(method.isFinal())
                   .isSynchronized(method.isSynchronized());
            
            // Parameters
            List<MethodFlowInfo.ParameterInfo> parameters = new ArrayList<>();
            method.getParameters().forEach(param -> {
                MethodFlowInfo.ParameterInfo paramInfo = MethodFlowInfo.ParameterInfo.builder()
                    .name(param.getNameAsString())
                    .type(param.getTypeAsString())
                    .position(createCodePosition(param, param.getNameAsString()))
                    .isFinal(param.isFinal())
                    .isVarargs(param.isVarArgs())
                    .build();
                parameters.add(paramInfo);
            });
            builder.parameters(parameters);
            
            // Flow analysis results
            builder.controlFlowNodes(flowAnalyzer.getControlFlowNodes())
                   .methodCalls(flowAnalyzer.getMethodCalls())
                   .fieldAccesses(flowAnalyzer.getFieldAccesses())
                   .localVariables(flowAnalyzer.getLocalVariables())
                   .tryCatchBlocks(flowAnalyzer.getTryCatchBlocks());
            
            // Calculate complexity
            builder.cyclomaticComplexity(flowAnalyzer.getCyclomaticComplexity())
                   .linesOfCode(method.getRange().map(r -> r.end.line - r.begin.line + 1).orElse(0));
            
            return builder.build();
        }
        
        private CodePosition createCodePosition(Node node, String context) {
            Optional<Range> range = node.getRange();
            if (range.isEmpty()) {
                return CodePosition.builder()
                    .startLine(0).endLine(0)
                    .startColumn(0).endColumn(0)
                    .build();
            }
            
            Range r = range.get();
            return CodePosition.builder()
                .startLine(r.begin.line)
                .endLine(r.end.line)
                .startColumn(r.begin.column)
                .endColumn(r.end.column)
                .build();
        }
        
        public List<CodeMetadata.ClassMetadata> getClasses() { return classes; }
        public List<CodeMetadata.InterfaceMetadata> getInterfaces() { return interfaces; }
        public int getLinesOfCode() { return linesOfCode; }
        public int getCommentLines() { return commentLines; }
        public int getBlankLines() { return blankLines; }
    }
    
    private static class MethodFlowAnalyzer extends VoidVisitorAdapter<Void> {
        
        private final List<MethodFlowInfo.ControlFlowNode> controlFlowNodes = new ArrayList<>();
        private final List<MethodFlowInfo.MethodCallInfo> methodCalls = new ArrayList<>();
        private final List<MethodFlowInfo.FieldAccessInfo> fieldAccesses = new ArrayList<>();
        private final List<MethodFlowInfo.VariableInfo> localVariables = new ArrayList<>();
        private final List<MethodFlowInfo.TryCatchInfo> tryCatchBlocks = new ArrayList<>();
        
        private int cyclomaticComplexity = 1; // Base complexity
        private int currentDepth = 0;
        
        @Override
        public void visit(IfStmt n, Void arg) {
            cyclomaticComplexity++;
            
            MethodFlowInfo.ControlFlowNode node = MethodFlowInfo.ControlFlowNode.builder()
                .nodeType("IF")
                .condition(n.getCondition().toString())
                .position(createPosition(n))
                .depth(currentDepth++)
                .build();
            controlFlowNodes.add(node);
            
            super.visit(n, arg);
            currentDepth--;
        }
        
        @Override
        public void visit(WhileStmt n, Void arg) {
            cyclomaticComplexity++;
            
            MethodFlowInfo.ControlFlowNode node = MethodFlowInfo.ControlFlowNode.builder()
                .nodeType("WHILE")
                .condition(n.getCondition().toString())
                .position(createPosition(n))
                .depth(currentDepth++)
                .build();
            controlFlowNodes.add(node);
            
            super.visit(n, arg);
            currentDepth--;
        }
        
        @Override
        public void visit(ForStmt n, Void arg) {
            cyclomaticComplexity++;
            
            MethodFlowInfo.ControlFlowNode node = MethodFlowInfo.ControlFlowNode.builder()
                .nodeType("FOR")
                .condition(n.getCompare().map(Object::toString).orElse(""))
                .position(createPosition(n))
                .depth(currentDepth++)
                .build();
            controlFlowNodes.add(node);
            
            super.visit(n, arg);
            currentDepth--;
        }
        
        @Override
        public void visit(MethodCallExpr n, Void arg) {
            MethodFlowInfo.MethodCallInfo callInfo = MethodFlowInfo.MethodCallInfo.builder()
                .targetMethod(n.getNameAsString())
                .callType("INSTANCE")
                .position(createPosition(n))
                .build();
            methodCalls.add(callInfo);
            
            super.visit(n, arg);
        }
        
        @Override
        public void visit(FieldAccessExpr n, Void arg) {
            MethodFlowInfo.FieldAccessInfo fieldInfo = MethodFlowInfo.FieldAccessInfo.builder()
                .fieldName(n.getNameAsString())
                .accessType("READ")
                .position(createPosition(n))
                .build();
            fieldAccesses.add(fieldInfo);
            
            super.visit(n, arg);
        }
        
        @Override
        public void visit(VariableDeclarationExpr n, Void arg) {
            for (VariableDeclarator var : n.getVariables()) {
                MethodFlowInfo.VariableInfo varInfo = MethodFlowInfo.VariableInfo.builder()
                    .name(var.getNameAsString())
                    .type(var.getTypeAsString())
                    .declarationPosition(createPosition(var))
                    .isFinal(n.isFinal())
                    .initialValue(var.getInitializer().map(Object::toString).orElse(null))
                    .build();
                localVariables.add(varInfo);
            }
            
            super.visit(n, arg);
        }
        
        @Override
        public void visit(TryStmt n, Void arg) {
            MethodFlowInfo.TryCatchInfo.TryCatchInfoBuilder builder = 
                MethodFlowInfo.TryCatchInfo.builder();
            
            builder.tryPosition(createPosition(n.getTryBlock()));
            
            List<MethodFlowInfo.TryCatchInfo.CatchClause> catchClauses = new ArrayList<>();
            for (CatchClause catchClause : n.getCatchClauses()) {
                MethodFlowInfo.TryCatchInfo.CatchClause clause = 
                    MethodFlowInfo.TryCatchInfo.CatchClause.builder()
                        .exceptionType(catchClause.getParameter().getTypeAsString())
                        .exceptionName(catchClause.getParameter().getNameAsString())
                        .position(createPosition(catchClause))
                        .build();
                catchClauses.add(clause);
            }
            builder.catchClauses(catchClauses);
            
            n.getFinallyBlock().ifPresent(finallyBlock -> 
                builder.finallyPosition(createPosition(finallyBlock)));
            
            tryCatchBlocks.add(builder.build());
            
            super.visit(n, arg);
        }
        
        private CodePosition createPosition(Node node) {
            Optional<Range> range = node.getRange();
            if (range.isEmpty()) {
                return CodePosition.builder()
                    .startLine(0).endLine(0)
                    .startColumn(0).endColumn(0)
                    .build();
            }
            
            Range r = range.get();
            return CodePosition.builder()
                .startLine(r.begin.line)
                .endLine(r.end.line)
                .startColumn(r.begin.column)
                .endColumn(r.end.column)
                .build();
        }
        
        public List<MethodFlowInfo.ControlFlowNode> getControlFlowNodes() { return controlFlowNodes; }
        public List<MethodFlowInfo.MethodCallInfo> getMethodCalls() { return methodCalls; }
        public List<MethodFlowInfo.FieldAccessInfo> getFieldAccesses() { return fieldAccesses; }
        public List<MethodFlowInfo.VariableInfo> getLocalVariables() { return localVariables; }
        public List<MethodFlowInfo.TryCatchInfo> getTryCatchBlocks() { return tryCatchBlocks; }
        public int getCyclomaticComplexity() { return cyclomaticComplexity; }
    }
}