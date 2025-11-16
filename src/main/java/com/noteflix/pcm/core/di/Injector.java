package com.noteflix.pcm.core.di;

import com.noteflix.pcm.application.service.chat.AIService;
import com.noteflix.pcm.application.service.chat.ConversationService;
import com.noteflix.pcm.core.navigation.PageNavigator;
import com.noteflix.pcm.core.theme.ThemeManager;
import com.noteflix.pcm.infrastructure.repository.chat.ConversationRepository;
import com.noteflix.pcm.infrastructure.repository.chat.ConversationRepositoryImpl;
import com.noteflix.pcm.ui.viewmodel.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Simple Dependency Injection Container
 *
 * <p>Lightweight DI implementation following these principles: - Dependency Inversion Principle:
 * depend on abstractions (interfaces) - Single Responsibility: only manages dependencies - Lazy
 * initialization: creates instances only when needed
 *
 * <p>Usage: <code>
 * Injector injector = Injector.getInstance();
 * ConversationService service = injector.get(ConversationService.class);
 * </code>
 *
 * <p>Note: For larger applications, consider using a full DI framework like: - Guice - Spring
 * (though heavier) - Afterburner.fx (JavaFX-specific)
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Slf4j
public class Injector {

    private static Injector instance;
    private final Map<Class<?>, Object> singletons;
    private final Map<Class<?>, Supplier<?>> factories;
    private PageNavigator navigator;

    private Injector() {
        this.singletons = new HashMap<>();
        this.factories = new HashMap<>();
        registerDefaults();
    }

    /**
     * Get singleton instance of Injector
     *
     * @return Injector instance
     */
    public static synchronized Injector getInstance() {
        if (instance == null) {
            instance = new Injector();
            log.info("✅ Injector initialized");
        }
        return instance;
    }

    /**
     * Register default service implementations
     */
    private void registerDefaults() {
        // Register factories for repositories
        registerFactory(ConversationRepository.class, ConversationRepositoryImpl::new);

        // Register singletons for services
        registerSingleton(AIService.class, new AIService());

        // Register factory for ConversationService with proper dependencies
        registerFactory(
                ConversationService.class,
                () -> {
                    ConversationRepository repo = get(ConversationRepository.class);
                    AIService aiService = get(AIService.class);
                    return new ConversationService(repo, aiService);
                });

        // Register ThemeManager singleton
        registerSingleton(ThemeManager.class, ThemeManager.getInstance());

        // Register ViewModels (create new instance each time for proper lifecycle)
        registerFactory(
                AIAssistantViewModel.class,
                () -> {
                    ConversationService convService = get(ConversationService.class);
                    AIService aiSvc = get(AIService.class);
                    return new AIAssistantViewModel(convService, aiSvc);
                });

        registerFactory(
                SettingsViewModel.class,
                () -> {
                    ThemeManager tm = get(ThemeManager.class);
                    return new SettingsViewModel(tm);
                });

        // Register other ViewModels
        registerFactory(KnowledgeBaseViewModel.class, KnowledgeBaseViewModel::new);
        registerFactory(DatabaseObjectsViewModel.class, DatabaseObjectsViewModel::new);
        registerFactory(BatchJobsViewModel.class, BatchJobsViewModel::new);

        log.debug("Default services and ViewModels registered");
    }

    /**
     * Register a singleton instance
     *
     * @param clazz    The class type
     * @param instance The singleton instance
     * @param <T>      Type parameter
     */
    public <T> void registerSingleton(Class<T> clazz, T instance) {
        singletons.put(clazz, instance);
        log.debug("Registered singleton: {}", clazz.getSimpleName());
    }

    /**
     * Register a factory for creating instances
     *
     * @param clazz   The class type
     * @param factory The factory supplier
     * @param <T>     Type parameter
     */
    public <T> void registerFactory(Class<T> clazz, Supplier<T> factory) {
        factories.put(clazz, factory);
        log.debug("Registered factory: {}", clazz.getSimpleName());
    }

    /**
     * Get an instance of the requested class
     *
     * @param clazz The class type to get
     * @param <T>   Type parameter
     * @return Instance of the requested class
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz) {
        // Check if it's already a singleton
        if (singletons.containsKey(clazz)) {
            return (T) singletons.get(clazz);
        }

        // Check if there's a factory
        if (factories.containsKey(clazz)) {
            Supplier<?> factory = factories.get(clazz);
            T instance = (T) factory.get();
            log.debug("Created instance from factory: {}", clazz.getSimpleName());
            return instance;
        }

        // Try to create instance using default constructor
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            log.debug("Created instance using default constructor: {}", clazz.getSimpleName());
            return instance;
        } catch (Exception e) {
            log.error("Failed to create instance of {}", clazz.getSimpleName(), e);
            throw new RuntimeException("Failed to create instance: " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Get or create a singleton instance This will create the instance using the factory or default
     * constructor, then cache it
     *
     * @param clazz The class type
     * @param <T>   Type parameter
     * @return Singleton instance
     */
    @SuppressWarnings("unchecked")
    public <T> T getSingleton(Class<T> clazz) {
        if (!singletons.containsKey(clazz)) {
            T instance = get(clazz);
            singletons.put(clazz, instance);
            log.debug("Created and cached singleton: {}", clazz.getSimpleName());
        }
        return (T) singletons.get(clazz);
    }

    /**
     * Get the page navigator
     *
     * @return PageNavigator instance
     */
    public PageNavigator getNavigator() {
        if (navigator == null) {
            throw new IllegalStateException("Navigator not initialized. Call setNavigator() first.");
        }
        return navigator;
    }

    /**
     * Set the page navigator instance
     *
     * @param navigator PageNavigator instance
     */
    public void setNavigator(PageNavigator navigator) {
        this.navigator = navigator;
        registerSingleton(PageNavigator.class, navigator);
        log.info("PageNavigator registered");
    }

    /**
     * Clear all singletons - useful for testing or cleanup
     */
    public void clear() {
        singletons.clear();
        factories.clear();
        navigator = null;
        log.info("Injector cleared");
    }

    /**
     * Reset and re-register defaults
     */
    public void reset() {
        clear();
        registerDefaults();
        log.info("Injector reset");
    }
}
