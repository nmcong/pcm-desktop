package com.noteflix.pcm.application.service.project;

/**
 * Factory for creating ProjectService instances
 * 
 * <p>Follows Factory Pattern and Dependency Inversion Principle
 * <p>Allows easy switching between different implementations
 * <p>Facilitates testing with mock implementations
 */
public class ProjectServiceFactory {
    
    private static IProjectService instance;
    
    /**
     * Gets singleton instance of ProjectService
     * @return ProjectService instance
     */
    public static IProjectService getInstance() {
        if (instance == null) {
            synchronized (ProjectServiceFactory.class) {
                if (instance == null) {
                    instance = new ProjectServiceImpl();
                }
            }
        }
        return instance;
    }
    
    /**
     * Sets a custom instance (useful for testing)
     * @param customInstance Custom ProjectService implementation
     */
    public static void setInstance(IProjectService customInstance) {
        instance = customInstance;
    }
    
    /**
     * Resets to default implementation
     */
    public static void resetToDefault() {
        instance = null;
    }
}