# LLM Function Annotation System

## 🎯 Goal

Dùng **annotations** để định nghĩa functions, tự động scan & register, với **full DI support** trong context hiện tại.

## ✨ Key Features

1. ✅ **@LLMFunction** - Định nghĩa function
2. ✅ **@Param** - Định nghĩa parameters với metadata
3. ✅ **Auto-scanning** - Tự động tìm & register functions
4. ✅ **DI Integration** - Functions có access tới services
5. ✅ **Type Safety** - Compile-time checking
6. ✅ **Reflection** - Auto parameter mapping
7. ✅ **Context Aware** - Execute trong context hiện tại

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────┐
│        FunctionRegistry (Enhanced)              │
│  - scanPackage(packageName)                     │
│  - register(class/instance)                     │
│  - execute(name, args) with DI                  │
└────────────────┬────────────────────────────────┘
                 │
      ┌──────────┴──────────┐
      │                     │
┌─────▼─────┐      ┌────────▼────────┐
│ Annotation│      │   Reflection    │
│  Scanner  │      │   Invoker       │
└───────────┘      └─────────────────┘
                            │
                   ┌────────▼────────┐
                   │   DI Injector   │
                   │ (Current context)│
                   └─────────────────┘
```

---

## 📝 Annotations

### **1. @LLMFunction**

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LLMFunction {
    /**
     * Function name (required)
     * Must be unique across all functions
     */
    String name();
    
    /**
     * Human-readable description for LLM
     * Should explain what the function does and when to use it
     */
    String description();
    
    /**
     * Category for organization (optional)
     * e.g., "database", "search", "analysis"
     */
    String category() default "";
    
    /**
     * Whether function requires authentication
     */
    boolean requiresAuth() default false;
    
    /**
     * Aliases for the function (optional)
     */
    String[] aliases() default {};
}
```

### **2. @Param**

```java
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
    /**
     * Parameter name (required)
     * Must match JSON property name
     */
    String value();
    
    /**
     * Human-readable description for LLM
     */
    String description() default "";
    
    /**
     * Whether parameter is required
     */
    boolean required() default true;
    
    /**
     * Default value if not provided
     */
    String defaultValue() default "";
    
    /**
     * Example value for documentation
     */
    String example() default "";
}
```

### **3. @FunctionProvider** (Class-level)

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FunctionProvider {
    /**
     * Provider name for grouping
     */
    String value() default "";
    
    /**
     * Whether to auto-register all @LLMFunction methods
     */
    boolean autoRegister() default true;
}
```

---

## 💡 Usage Examples

### **Example 1: Simple Function**

```java
@FunctionProvider("projects")
public class ProjectFunctions {
    
    @Inject  // DI support!
    private ProjectService projectService;
    
    @LLMFunction(
        name = "search_projects",
        description = "Search for projects in database by query string"
    )
    public List<Project> searchProjects(
        @Param(value = "query", description = "Search query") 
        String query
    ) {
        return projectService.search(query);
    }
    
    @LLMFunction(
        name = "get_project_details",
        description = "Get detailed information about a specific project"
    )
    public Project getProjectDetails(
        @Param(value = "id", description = "Project ID") 
        Long projectId
    ) {
        return projectService.getById(projectId);
    }
    
    @LLMFunction(
        name = "create_project",
        description = "Create a new project",
        requiresAuth = true
    )
    public Project createProject(
        @Param(value = "name", description = "Project name")
        String name,
        
        @Param(value = "description", description = "Project description", required = false)
        String description
    ) {
        return projectService.create(name, description);
    }
}
```

### **Example 2: With Services & Repository**

```java
@FunctionProvider("database")
public class DatabaseFunctions {
    
    @Inject
    private DatabaseService databaseService;
    
    @Inject
    private ProjectRepository projectRepository;
    
    @LLMFunction(
        name = "get_schema_info",
        description = "Get database schema information"
    )
    public SchemaInfo getSchemaInfo() {
        // Has access to all injected services!
        return databaseService.getSchemaInfo();
    }
    
    @LLMFunction(
        name = "analyze_table",
        description = "Analyze a specific database table"
    )
    public TableAnalysis analyzeTable(
        @Param("tableName") String tableName
    ) {
        // Can use repository
        return projectRepository.analyzeTable(tableName);
    }
}
```

### **Example 3: Complex Types**

```java
@FunctionProvider
public class AnalysisFunctions {
    
    @Inject
    private AnalysisService analysisService;
    
    @LLMFunction(
        name = "analyze_code_quality",
        description = "Analyze code quality for a project with various metrics"
    )
    public CodeQualityReport analyzeCodeQuality(
        @Param(value = "projectId", description = "Project ID to analyze")
        Long projectId,
        
        @Param(value = "metrics", description = "Metrics to analyze", required = false)
        List<String> metrics,
        
        @Param(value = "threshold", description = "Quality threshold (0-100)", defaultValue = "70")
        Integer threshold
    ) {
        return analysisService.analyzeQuality(projectId, metrics, threshold);
    }
}
```

---

## 🔧 Implementation

### **1. Enhanced FunctionRegistry**

```java
@Slf4j
public class FunctionRegistry {
    private static FunctionRegistry instance;
    
    // Registered functions
    private Map<String, RegisteredFunction> functions = new HashMap<>();
    
    // Function providers (instances with DI)
    private Map<String, Object> providers = new HashMap<>();
    
    // DI Injector
    private Injector injector;
    
    private FunctionRegistry() {
        this.injector = Injector.getInstance();
    }
    
    public static FunctionRegistry getInstance() {
        if (instance == null) {
            instance = new FunctionRegistry();
        }
        return instance;
    }
    
    /**
     * Scan package for @FunctionProvider classes
     */
    public void scanPackage(String packageName) {
        log.info("Scanning package for functions: {}", packageName);
        
        ClassPathScanner scanner = new ClassPathScanner();
        Set<Class<?>> classes = scanner.findAnnotatedClasses(
            packageName, 
            FunctionProvider.class
        );
        
        for (Class<?> clazz : classes) {
            registerProvider(clazz);
        }
        
        log.info("Registered {} functions from {} providers", 
            functions.size(), providers.size());
    }
    
    /**
     * Register a function provider class
     * Will create instance and inject dependencies
     */
    public void registerProvider(Class<?> providerClass) {
        try {
            // Create instance with DI
            Object instance = createInstanceWithDI(providerClass);
            
            // Store provider
            providers.put(providerClass.getName(), instance);
            
            // Find all @LLMFunction methods
            for (Method method : providerClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(LLMFunction.class)) {
                    registerFunction(instance, method);
                }
            }
        } catch (Exception e) {
            log.error("Failed to register provider: {}", providerClass.getName(), e);
        }
    }
    
    /**
     * Create instance with dependency injection
     */
    private Object createInstanceWithDI(Class<?> clazz) throws Exception {
        // Create instance
        Object instance = clazz.getDeclaredConstructor().newInstance();
        
        // Inject dependencies
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                Object dependency = injector.get(field.getType());
                field.set(instance, dependency);
                log.debug("Injected {} into {}", 
                    field.getType().getSimpleName(), 
                    clazz.getSimpleName());
            }
        }
        
        return instance;
    }
    
    /**
     * Register a single function method
     */
    private void registerFunction(Object instance, Method method) {
        LLMFunction annotation = method.getAnnotation(LLMFunction.class);
        
        // Create function definition
        AnnotatedFunction function = new AnnotatedFunction(
            instance,
            method,
            annotation
        );
        
        // Register
        functions.put(annotation.name(), function);
        log.debug("Registered function: {}", annotation.name());
    }
    
    /**
     * Execute function by name
     */
    public Object execute(String name, Map<String, Object> arguments) {
        RegisteredFunction function = functions.get(name);
        if (function == null) {
            throw new IllegalArgumentException("Function not found: " + name);
        }
        
        return function.execute(arguments);
    }
    
    /**
     * Get all tools for LLM
     */
    public List<Tool> getAllTools() {
        return functions.values().stream()
            .map(RegisteredFunction::toTool)
            .collect(Collectors.toList());
    }
}
```

### **2. AnnotatedFunction**

```java
public class AnnotatedFunction implements RegisteredFunction {
    private final Object instance;
    private final Method method;
    private final LLMFunction annotation;
    private final List<ParameterInfo> parameters;
    
    public AnnotatedFunction(Object instance, Method method, LLMFunction annotation) {
        this.instance = instance;
        this.method = method;
        this.annotation = annotation;
        this.parameters = parseParameters(method);
    }
    
    @Override
    public String getName() {
        return annotation.name();
    }
    
    @Override
    public String getDescription() {
        return annotation.description();
    }
    
    @Override
    public JsonSchema getParameters() {
        JsonSchema.Builder schema = JsonSchema.builder();
        
        for (ParameterInfo param : parameters) {
            PropertySchema propSchema = createPropertySchema(param);
            schema.property(param.name, propSchema);
            
            if (param.required) {
                schema.required(param.name);
            }
        }
        
        return schema.build();
    }
    
    @Override
    public Object execute(Map<String, Object> args) {
        try {
            // Map arguments to method parameters
            Object[] methodArgs = mapArguments(args);
            
            // Invoke method on instance
            return method.invoke(instance, methodArgs);
            
        } catch (Exception e) {
            throw new FunctionExecutionException(
                "Failed to execute function: " + getName(), e
            );
        }
    }
    
    @Override
    public Tool toTool() {
        return Tool.function(getName(), getDescription(), getParameters());
    }
    
    /**
     * Parse method parameters
     */
    private List<ParameterInfo> parseParameters(Method method) {
        List<ParameterInfo> params = new ArrayList<>();
        
        for (Parameter param : method.getParameters()) {
            Param annotation = param.getAnnotation(Param.class);
            if (annotation != null) {
                params.add(new ParameterInfo(
                    annotation.value(),
                    annotation.description(),
                    annotation.required(),
                    annotation.defaultValue(),
                    param.getType()
                ));
            }
        }
        
        return params;
    }
    
    /**
     * Map JSON arguments to method parameters
     */
    private Object[] mapArguments(Map<String, Object> args) {
        Object[] result = new Object[parameters.size()];
        
        for (int i = 0; i < parameters.size(); i++) {
            ParameterInfo param = parameters.get(i);
            Object value = args.get(param.name);
            
            if (value == null && param.required) {
                throw new IllegalArgumentException(
                    "Required parameter missing: " + param.name
                );
            }
            
            if (value == null && !param.defaultValue.isEmpty()) {
                value = parseDefaultValue(param.defaultValue, param.type);
            }
            
            // Type conversion
            result[i] = convertType(value, param.type);
        }
        
        return result;
    }
    
    /**
     * Convert JSON value to target type
     */
    private Object convertType(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        
        // Already correct type
        if (targetType.isInstance(value)) {
            return value;
        }
        
        // String conversions
        if (targetType == String.class) {
            return value.toString();
        }
        
        // Number conversions
        if (targetType == Integer.class || targetType == int.class) {
            return ((Number) value).intValue();
        }
        
        if (targetType == Long.class || targetType == long.class) {
            return ((Number) value).longValue();
        }
        
        if (targetType == Double.class || targetType == double.class) {
            return ((Number) value).doubleValue();
        }
        
        // Boolean
        if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value.toString());
        }
        
        // List/Array
        if (List.class.isAssignableFrom(targetType)) {
            return value;  // Already a list from JSON
        }
        
        // Complex objects - use Jackson
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(value, targetType);
    }
}
```

### **3. ClassPath Scanner**

```java
public class ClassPathScanner {
    
    /**
     * Find all classes in package with specific annotation
     */
    public Set<Class<?>> findAnnotatedClasses(
        String packageName, 
        Class<? extends Annotation> annotation
    ) {
        Set<Class<?>> classes = new HashSet<>();
        
        try {
            // Get classpath
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            Enumeration<URL> resources = classLoader.getResources(path);
            
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                File directory = new File(resource.getFile());
                
                if (directory.exists()) {
                    classes.addAll(findClassesInDirectory(
                        directory, 
                        packageName, 
                        annotation
                    ));
                }
            }
        } catch (Exception e) {
            log.error("Failed to scan package: {}", packageName, e);
        }
        
        return classes;
    }
    
    private Set<Class<?>> findClassesInDirectory(
        File directory, 
        String packageName,
        Class<? extends Annotation> annotation
    ) {
        Set<Class<?>> classes = new HashSet<>();
        
        if (!directory.exists()) {
            return classes;
        }
        
        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClassesInDirectory(
                    file, 
                    packageName + "." + file.getName(),
                    annotation
                ));
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + 
                    file.getName().substring(0, file.getName().length() - 6);
                
                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(annotation)) {
                        classes.add(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    log.warn("Class not found: {}", className);
                }
            }
        }
        
        return classes;
    }
}
```

---

## 🚀 Usage in Application

### **1. Setup at Startup**

```java
public class PCMApplication extends Application {
    
    @Override
    public void init() throws Exception {
        super.init();
        
        // Initialize DI
        Injector injector = Injector.getInstance();
        
        // Register services
        injector.registerSingleton(ProjectService.class, new ProjectService());
        injector.registerSingleton(DatabaseService.class, new DatabaseService());
        injector.registerSingleton(AnalysisService.class, new AnalysisService());
        
        // Auto-scan and register functions
        FunctionRegistry functionRegistry = FunctionRegistry.getInstance();
        functionRegistry.scanPackage("com.noteflix.pcm.llm.functions");
        
        log.info("✅ Registered {} LLM functions", 
            functionRegistry.getAllTools().size());
    }
}
```

### **2. Use with LLM**

```java
// Get all registered functions as tools
List<Tool> tools = FunctionRegistry.getInstance().getAllTools();

// Chat with tools
ChatOptions options = ChatOptions.builder()
    .tools(tools)
    .build();

ChatResponse response = provider.chat(messages, options).get();

if (response.hasToolCalls()) {
    // Execute with context awareness
    for (ToolCall call : response.getToolCalls()) {
        Object result = FunctionRegistry.getInstance()
            .execute(call.getName(), call.getArguments());
        
        messages.add(Message.tool(call.getId(), result.toString()));
    }
}
```

---

## ✅ Benefits

### **1. Clean & Declarative**

```java
// Before: Manual registration
functions.register("search", new RegisteredFunction() {
    public Object execute(Map args) { ... }
});

// After: Just annotations!
@LLMFunction(name = "search", description = "...")
public List<Project> search(@Param("query") String query) { ... }
```

### **2. DI Support**

```java
@FunctionProvider
public class ProjectFunctions {
    @Inject private ProjectService service;  // Auto-injected!
    
    @LLMFunction(...)
    public List<Project> search(@Param("query") String query) {
        return service.search(query);  // Has access to services!
    }
}
```

### **3. Type Safety**

```java
// Compile-time checking
@LLMFunction(...)
public Project getProject(@Param("id") Long id) {  // Type-safe!
    return projectService.getById(id);
}
```

### **4. Auto Documentation**

```java
// Generate OpenAPI/JSON Schema automatically
Tool tool = function.toTool();
// All metadata from annotations!
```

### **5. Context Awareness**

```java
// Functions execute in current context
// Full access to:
// - Services (via @Inject)
// - Repositories
// - Current user session
// - Configuration
// - Everything in DI container!
```

---

## 📁 File Structure

```
llm/
├── annotation/
│   ├── LLMFunction.java
│   ├── Param.java
│   └── FunctionProvider.java
│
├── registry/
│   ├── FunctionRegistry.java (enhanced)
│   ├── AnnotatedFunction.java
│   └── ClassPathScanner.java
│
└── functions/                    # User-defined functions
    ├── ProjectFunctions.java
    ├── DatabaseFunctions.java
    ├── AnalysisFunctions.java
    └── ...
```

---

## 🎯 Complete Example

```java
// 1. Define functions with annotations
@FunctionProvider("projects")
public class ProjectFunctions {
    @Inject private ProjectService projectService;
    @Inject private UserService userService;
    
    @LLMFunction(
        name = "search_projects",
        description = "Search for projects by query"
    )
    public List<Project> search(
        @Param(value = "query", description = "Search query") 
        String query,
        
        @Param(value = "limit", defaultValue = "10", required = false)
        Integer limit
    ) {
        // Has access to injected services!
        User currentUser = userService.getCurrentUser();
        return projectService.search(query, currentUser, limit);
    }
}

// 2. Auto-scan at startup
FunctionRegistry.getInstance().scanPackage("com.noteflix.pcm.llm.functions");

// 3. Use with LLM - automatic!
ChatResponse response = provider.chat(
    messages,
    ChatOptions.builder()
        .tools(FunctionRegistry.getInstance().getAllTools())
        .build()
).get();

// 4. Execute - with full DI context
if (response.hasToolCalls()) {
    for (ToolCall call : response.getToolCalls()) {
        Object result = FunctionRegistry.getInstance()
            .execute(call.getName(), call.getArguments());
        // Function executed with access to all services!
    }
}
```

---

**Status:** 🎨 Design Complete - Annotation-based Functions with Full DI Support!

