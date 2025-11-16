package com.noteflix.pcm.ast.examples;

import com.noteflix.pcm.ast.ASTAnalysisResult;
import com.noteflix.pcm.ast.ASTController;
import com.noteflix.pcm.ast.ASTService;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class ASTUsageExample {

    public static void main(String[] args) {
        log.info("Starting AST Usage Example");

        // Example 1: Analyze a simple Java code snippet
        demonstrateCodeAnalysis();

        // Example 2: Use controller for structured responses
        demonstrateControllerUsage();

        // Example 3: Analyze current project
        demonstrateProjectAnalysis();

        log.info("AST Usage Example completed");
    }

    private static void demonstrateCodeAnalysis() {
        log.info("=== Demo 1: Basic Code Analysis ===");

        String sampleJavaCode =
                """
                        package com.example;

                        import java.util.List;
                        import java.util.ArrayList;

                        public class Calculator {

                            private int result;

                            public Calculator() {
                                this.result = 0;
                            }

                            public int add(int a, int b) {
                                return a + b;
                            }

                            public static int multiply(int a, int b) {
                                return a * b;
                            }

                            private void reset() {
                                this.result = 0;
                            }
                        }
                        """;

        ASTService astService = new ASTService();
        ASTAnalysisResult result = astService.analyzeJavaCode(sampleJavaCode);

        if (result.isSuccessful()) {
            log.info("Package: {}", result.getPackageName());
            log.info("Imports: {}", result.getImports());
            log.info("Classes found: {}", result.getClasses().size());
            log.info("Methods found: {}", result.getMethods().size());

            for (ASTAnalysisResult.ClassInfo classInfo : result.getClasses()) {
                log.info(
                        "Class: {} (public: {}, interface: {})",
                        classInfo.getName(),
                        classInfo.isPublic(),
                        classInfo.isInterface());
            }

            for (ASTAnalysisResult.MethodInfo methodInfo : result.getMethods()) {
                log.info(
                        "Method: {} returns {} (public: {}, static: {}, params: {})",
                        methodInfo.getName(),
                        methodInfo.getReturnType(),
                        methodInfo.isPublic(),
                        methodInfo.isStatic(),
                        methodInfo.getParameterCount());
            }
        } else {
            log.error("Analysis failed: {}", result.getErrorMessage());
        }
    }

    private static void demonstrateControllerUsage() {
        log.info("=== Demo 2: Controller Usage ===");

        String sampleCode =
                """
                        public interface DataProcessor {
                            void process(String data);
                            String getResult();
                        }
                        """;

        ASTController controller = new ASTController();
        Map<String, Object> response = controller.analyzeCode(sampleCode);

        log.info("Controller response success: {}", response.get("success"));
        log.info("Controller response message: {}", response.get("message"));

        if (Boolean.TRUE.equals(response.get("success"))) {
            ASTAnalysisResult data = (ASTAnalysisResult) response.get("data");
            log.info("Interface found: {}", data.getClasses().get(0).getName());
            log.info("Methods in interface: {}", data.getMethods().size());
        }
    }

    private static void demonstrateProjectAnalysis() {
        log.info("=== Demo 3: Project Analysis ===");

        ASTController controller = new ASTController();

        // Analyze current project's com.noteflix.pcm package
        String projectPath = "src/main/java/com/noteflix/pcm";
        Map<String, Object> projectResponse = controller.analyzeProject(projectPath);

        log.info("Project analysis success: {}", projectResponse.get("success"));

        if (Boolean.TRUE.equals(projectResponse.get("success"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) projectResponse.get("data");

            log.info("Total Java files: {}", data.get("totalJavaFiles"));
            log.info("Total classes: {}", data.get("totalClasses"));
            log.info("Total methods: {}", data.get("totalMethods"));
        }

        // Demonstrate cache functionality
        Map<String, Object> cacheInfo = controller.getCacheInfo();
        log.info("Cache size: {}", cacheInfo.get("cacheSize"));
    }
}
