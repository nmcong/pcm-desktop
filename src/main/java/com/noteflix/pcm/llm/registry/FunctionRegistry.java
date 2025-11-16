package com.noteflix.pcm.llm.registry;

import com.noteflix.pcm.llm.api.RegisteredFunction;
import com.noteflix.pcm.llm.exception.FunctionExecutionException;
import com.noteflix.pcm.llm.exception.FunctionNotFoundException;
import com.noteflix.pcm.llm.model.Tool;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for managing LLM-callable functions.
 *
 * <p>Provides: - Function registration and discovery - Function execution - Tool list generation
 * for LLM requests - Annotation-based auto-registration
 *
 * <p>This is a singleton - get the instance via getInstance().
 *
 * <p>Example usage:
 *
 * <pre>
 * FunctionRegistry registry = FunctionRegistry.getInstance();
 *
 * // Register a function
 * registry.register(new SearchProjectsFunction());
 *
 * // Get all tools for LLM
 * List&lt;Tool&gt; tools = registry.getAllTools();
 *
 * // Execute a function
 * Map&lt;String, Object&gt; args = Map.of("query", "noteflix");
 * Object result = registry.execute("search_projects", args);
 * </pre>
 */
@Slf4j
public class FunctionRegistry {

    private static FunctionRegistry instance;

    private final Map<String, RegisteredFunction> functions;

    private FunctionRegistry() {
        this.functions = new ConcurrentHashMap<>();
        log.info("FunctionRegistry initialized");
    }

    /**
     * Get the singleton instance.
     *
     * @return FunctionRegistry instance
     */
    public static synchronized FunctionRegistry getInstance() {
        if (instance == null) {
            instance = new FunctionRegistry();
        }
        return instance;
    }

    /**
     * Register a function.
     *
     * @param function Function implementation
     */
    public void register(RegisteredFunction function) {
        if (function == null) {
            throw new IllegalArgumentException("Function cannot be null");
        }

        String name = function.getName();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Function name cannot be null or empty");
        }

        functions.put(name, function);
        log.info("Registered function: {} - {}", name, function.getDescription());
    }

    /**
     * Register a function by name. Convenience method for programmatic registration.
     *
     * @param name     Function name
     * @param function Function implementation
     */
    public void register(String name, RegisteredFunction function) {
        functions.put(name, function);
        log.info("Registered function: {} - {}", name, function.getDescription());
    }

    /**
     * Get a registered function by name.
     *
     * @param name Function name
     * @return Function implementation, or null if not found
     */
    public RegisteredFunction get(String name) {
        return functions.get(name);
    }

    /**
     * Check if a function is registered.
     *
     * @param name Function name
     * @return true if registered, false otherwise
     */
    public boolean hasFunction(String name) {
        return functions.containsKey(name);
    }

    /**
     * Get all registered function names.
     *
     * @return List of function names
     */
    public List<String> getFunctionNames() {
        return new ArrayList<>(functions.keySet());
    }

    /**
     * Get all registered functions.
     *
     * @return Map of name to function
     */
    public Map<String, RegisteredFunction> getAllFunctions() {
        return new HashMap<>(functions);
    }

    /**
     * Get all tools in LLM-compatible format.
     *
     * <p>Converts registered functions to Tool objects that can be included in LLM requests.
     *
     * @return List of tools
     */
    public List<Tool> getAllTools() {
        List<Tool> tools = new ArrayList<>();

        for (RegisteredFunction function : functions.values()) {
            Tool tool =
                    Tool.function(function.getName(), function.getDescription(), function.getParameters());
            tools.add(tool);
        }

        return tools;
    }

    /**
     * Get specific tools by name.
     *
     * @param names Function names to include
     * @return List of matching tools
     */
    public List<Tool> getTools(String... names) {
        List<Tool> tools = new ArrayList<>();

        for (String name : names) {
            RegisteredFunction function = functions.get(name);
            if (function != null) {
                Tool tool =
                        Tool.function(function.getName(), function.getDescription(), function.getParameters());
                tools.add(tool);
            } else {
                log.warn("Function not found: {}", name);
            }
        }

        return tools;
    }

    /**
     * Execute a function by name.
     *
     * @param name      Function name
     * @param arguments Function arguments
     * @return Function result
     * @throws FunctionNotFoundException  if function not found
     * @throws FunctionExecutionException if execution fails
     */
    public Object execute(String name, Map<String, Object> arguments)
            throws FunctionNotFoundException, FunctionExecutionException {
        RegisteredFunction function = functions.get(name);

        if (function == null) {
            throw new FunctionNotFoundException(name);
        }

        log.debug("Executing function: {} with args: {}", name, arguments);

        try {
            long startTime = System.currentTimeMillis();
            Object result = function.execute(arguments);
            long duration = System.currentTimeMillis() - startTime;

            log.debug("Function {} completed in {}ms", name, duration);

            return result;

        } catch (Exception e) {
            log.error("Function execution failed: {}", name, e);
            throw new FunctionExecutionException(name, arguments, e);
        }
    }

    /**
     * Unregister a function.
     *
     * @param name Function name
     * @return true if function was removed, false if not found
     */
    public boolean unregister(String name) {
        RegisteredFunction removed = functions.remove(name);

        if (removed != null) {
            log.info("Unregistered function: {}", name);
            return true;
        }

        return false;
    }

    /**
     * Clear all registered functions.
     */
    public void clear() {
        functions.clear();
        log.info("All functions cleared");
    }

    /**
     * Get count of registered functions.
     *
     * @return Number of registered functions
     */
    public int getFunctionCount() {
        return functions.size();
    }

    /**
     * Scan a package for @LLMFunction annotated methods and register them.
     *
     * @param packageName Package to scan (e.g., "com.noteflix.pcm.functions")
     * @return Number of functions registered
     */
    public int scanPackage(String packageName) {
        AnnotationFunctionScanner scanner = new AnnotationFunctionScanner(this);
        return scanner.scanPackage(packageName);
    }

    /**
     * Scan a specific class for @LLMFunction methods.
     *
     * @param providerClass Class to scan
     * @return Number of functions registered
     */
    public int scanClass(Class<?> providerClass) {
        AnnotationFunctionScanner scanner = new AnnotationFunctionScanner(this);
        return scanner.scanProviderClass(providerClass);
    }
}
