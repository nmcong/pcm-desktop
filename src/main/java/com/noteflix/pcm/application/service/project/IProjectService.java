package com.noteflix.pcm.application.service.project;

import com.noteflix.pcm.domain.entity.Project;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Project Service interface following Single Responsibility Principle
 * Defines contract for project-related business operations
 *
 * <p>Follows Interface Segregation Principle - only includes methods related to project management
 */
public interface IProjectService {

    /**
     * Gets all projects from the system
     *
     * @return List of all projects
     */
    List<Project> getAllProjects();

    /**
     * Gets all projects asynchronously
     *
     * @return CompletableFuture containing list of all projects
     */
    CompletableFuture<List<Project>> getAllProjectsAsync();

    /**
     * Gets only favorite projects
     *
     * @return List of favorite projects
     */
    List<Project> getFavoriteProjects();

    /**
     * Gets only non-favorite projects
     *
     * @return List of non-favorite projects
     */
    List<Project> getNonFavoriteProjects();

    /**
     * Finds a project by its code
     *
     * @param code The project code
     * @return Optional containing the project if found
     */
    Optional<Project> getProjectByCode(String code);

    /**
     * Finds a project by its code asynchronously
     *
     * @param code The project code
     * @return CompletableFuture containing Optional with the project if found
     */
    CompletableFuture<Optional<Project>> getProjectByCodeAsync(String code);

    /**
     * Toggles favorite status of a project
     *
     * @param projectCode The project code
     * @return Updated project, or empty Optional if project not found
     */
    Optional<Project> toggleFavorite(String projectCode);

    /**
     * Sets favorite status of a project
     *
     * @param projectCode The project code
     * @param isFavorite  The favorite status to set
     * @return Updated project, or empty Optional if project not found
     */
    Optional<Project> setFavorite(String projectCode, boolean isFavorite);

    /**
     * Creates a new project
     *
     * @param project The project to create
     * @return The created project with generated ID
     */
    Project createProject(Project project);

    /**
     * Updates an existing project
     *
     * @param project The project to update
     * @return The updated project, or empty Optional if project not found
     */
    Optional<Project> updateProject(Project project);

    /**
     * Gets project statistics
     *
     * @return Project statistics summary
     */
    ProjectStatistics getStatistics();

    /**
     * Refreshes project data asynchronously (simulates re-fetching from database/API)
     *
     * @return CompletableFuture that completes when refresh is done
     */
    CompletableFuture<Void> refreshDataAsync();

    /**
     * Project statistics data structure
     */
    record ProjectStatistics(
            int totalProjects,
            int favoriteProjects,
            int activeProjects,
            int archivedProjects
    ) {
    }
}