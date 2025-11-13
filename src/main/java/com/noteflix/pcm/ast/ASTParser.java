package com.noteflix.pcm.ast;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ASTParser {

  private final JavaParser javaParser;

  public ASTParser() {
    this.javaParser = new JavaParser();
  }

  public Optional<CompilationUnit> parseJavaCode(String javaCode) {
    try {
      ParseResult<CompilationUnit> parseResult = javaParser.parse(javaCode);

      if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
        return parseResult.getResult();
      } else {
        log.error("Failed to parse Java code. Errors: {}", parseResult.getProblems());
        return Optional.empty();
      }
    } catch (Exception e) {
      log.error("Exception while parsing Java code", e);
      return Optional.empty();
    }
  }

  public ASTAnalysisResult analyzeJavaCode(String javaCode) {
    Optional<CompilationUnit> cuOpt = parseJavaCode(javaCode);

    if (cuOpt.isEmpty()) {
      return ASTAnalysisResult.builder()
          .successful(false)
          .errorMessage("Failed to parse Java code")
          .build();
    }

    CompilationUnit cu = cuOpt.get();
    ASTAnalysisResult.ASTAnalysisResultBuilder builder = ASTAnalysisResult.builder();

    // Extract package information
    cu.getPackageDeclaration().ifPresent(pkg -> builder.packageName(pkg.getNameAsString()));

    // Extract imports
    List<String> imports = new ArrayList<>();
    cu.getImports().forEach(importDecl -> imports.add(importDecl.getNameAsString()));
    builder.imports(imports);

    // Extract classes and methods
    List<ASTAnalysisResult.ClassInfo> classes = new ArrayList<>();
    List<ASTAnalysisResult.MethodInfo> methods = new ArrayList<>();

    cu.accept(
        new VoidVisitorAdapter<Void>() {
          @Override
          public void visit(ClassOrInterfaceDeclaration n, Void arg) {
            ASTAnalysisResult.ClassInfo classInfo =
                ASTAnalysisResult.ClassInfo.builder()
                    .name(n.getNameAsString())
                    .isInterface(n.isInterface())
                    .isAbstract(n.isAbstract())
                    .isPublic(n.isPublic())
                    .build();
            classes.add(classInfo);

            super.visit(n, arg);
          }

          @Override
          public void visit(MethodDeclaration n, Void arg) {
            ASTAnalysisResult.MethodInfo methodInfo =
                ASTAnalysisResult.MethodInfo.builder()
                    .name(n.getNameAsString())
                    .returnType(n.getTypeAsString())
                    .isPublic(n.isPublic())
                    .isPrivate(n.isPrivate())
                    .isStatic(n.isStatic())
                    .parameterCount(n.getParameters().size())
                    .build();
            methods.add(methodInfo);

            super.visit(n, arg);
          }
        },
        null);

    return builder.successful(true).classes(classes).methods(methods).build();
  }

  public String getASTAsString(String javaCode) {
    Optional<CompilationUnit> cuOpt = parseJavaCode(javaCode);

    if (cuOpt.isEmpty()) {
      return "Failed to parse Java code";
    }

    return cuOpt.get().toString();
  }

  public List<Node> getAllNodes(String javaCode) {
    Optional<CompilationUnit> cuOpt = parseJavaCode(javaCode);

    if (cuOpt.isEmpty()) {
      return new ArrayList<>();
    }

    List<Node> nodes = new ArrayList<>();
    CompilationUnit cu = cuOpt.get();

    // Collect all nodes using walk method
    cu.walk(
        node -> {
          nodes.add(node);
        });

    return nodes;
  }
}
