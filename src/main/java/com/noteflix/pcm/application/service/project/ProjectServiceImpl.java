package com.noteflix.pcm.application.service.project;

import com.noteflix.pcm.domain.entity.Project;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of ProjectService following SOLID principles
 *
 * <p>Single Responsibility: Handles project business logic only
 * <p>Open/Closed: Extensible through interface without modifying existing code
 * <p>Liskov Substitution: Can be replaced by any IProjectService implementation
 * <p>Interface Segregation: Implements focused IProjectService interface
 * <p>Dependency Inversion: Depends on abstractions (interface) not concretions
 */
@Slf4j
public class ProjectServiceImpl implements IProjectService {

    // In-memory storage for demo purposes
    // In real application, this would be replaced by repository pattern
    private final ConcurrentHashMap<String, Project> projectStorage;

    public ProjectServiceImpl() {
        this.projectStorage = new ConcurrentHashMap<>();
        initializeMockData();
    }

    @Override
    public List<Project> getAllProjects() {
        log.debug("Retrieving all projects");
        return new ArrayList<>(projectStorage.values());
    }

    @Override
    public CompletableFuture<List<Project>> getAllProjectsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Loading all projects asynchronously");
                // Simulate network delay
                Thread.sleep(1500);

                List<Project> projects = getAllProjects();
                log.info("Successfully loaded {} projects", projects.size());
                return projects;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Projects loading was interrupted", e);
            }
        });
    }

    @Override
    public List<Project> getFavoriteProjects() {
        log.debug("Retrieving favorite projects");
        return projectStorage.values().stream()
                .filter(Project::isFavorite)
                .toList();
    }

    @Override
    public List<Project> getNonFavoriteProjects() {
        log.debug("Retrieving non-favorite projects");
        return projectStorage.values().stream()
                .filter(project -> !project.isFavorite())
                .toList();
    }

    @Override
    public Optional<Project> getProjectByCode(String code) {
        log.debug("Retrieving project by code: {}", code);
        return Optional.ofNullable(projectStorage.get(code));
    }

    @Override
    public CompletableFuture<Optional<Project>> getProjectByCodeAsync(String code) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Loading project data for: {}", code);
                // Simulate network/database delay
                Thread.sleep(2000);

                // Simulate potential loading failure (5% chance)
                if (Math.random() < 0.05) {
                    throw new RuntimeException("Failed to load project data from server");
                }

                Optional<Project> project = getProjectByCode(code);
                log.info("Successfully loaded project data for: {}", code);
                return project;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Project loading was interrupted", e);
            }
        });
    }

    @Override
    public Optional<Project> toggleFavorite(String projectCode) {
        log.debug("Toggling favorite status for project: {}", projectCode);
        Project project = projectStorage.get(projectCode);
        if (project != null) {
            project.setFavorite(!project.isFavorite());
            project.setUpdatedAt(LocalDateTime.now());
            log.info("Toggled favorite status for project {}: {}", projectCode, project.isFavorite());
            return Optional.of(project);
        }
        log.warn("Project not found for code: {}", projectCode);
        return Optional.empty();
    }

    @Override
    public Optional<Project> setFavorite(String projectCode, boolean isFavorite) {
        log.debug("Setting favorite status for project {}: {}", projectCode, isFavorite);
        Project project = projectStorage.get(projectCode);
        if (project != null) {
            project.setFavorite(isFavorite);
            project.setUpdatedAt(LocalDateTime.now());
            log.info("Set favorite status for project {}: {}", projectCode, isFavorite);
            return Optional.of(project);
        }
        log.warn("Project not found for code: {}", projectCode);
        return Optional.empty();
    }

    @Override
    public Project createProject(Project project) {
        log.debug("Creating new project: {}", project.getCode());
        project.validate(); // Business rule validation
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        projectStorage.put(project.getCode(), project);
        log.info("Created new project: {}", project.getCode());
        return project;
    }

    @Override
    public Optional<Project> updateProject(Project project) {
        log.debug("Updating project: {}", project.getCode());
        if (projectStorage.containsKey(project.getCode())) {
            project.validate(); // Business rule validation
            project.setUpdatedAt(LocalDateTime.now());
            projectStorage.put(project.getCode(), project);
            log.info("Updated project: {}", project.getCode());
            return Optional.of(project);
        }
        log.warn("Project not found for update: {}", project.getCode());
        return Optional.empty();
    }

    @Override
    public ProjectStatistics getStatistics() {
        log.debug("Calculating project statistics");
        List<Project> allProjects = getAllProjects();

        int totalProjects = allProjects.size();
        int favoriteProjects = (int) allProjects.stream().filter(Project::isFavorite).count();
        int activeProjects = (int) allProjects.stream()
                .filter(p -> p.getStatus() == Project.ProjectStatus.ACTIVE).count();
        int archivedProjects = (int) allProjects.stream()
                .filter(p -> p.getStatus() == Project.ProjectStatus.ARCHIVED).count();

        return new ProjectStatistics(totalProjects, favoriteProjects, activeProjects, archivedProjects);
    }

    @Override
    public CompletableFuture<Void> refreshDataAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Refreshing project data asynchronously");

                // Simulate data refresh delay (in real app, this would be API call/database query)
                Thread.sleep(800);

                // In real application, this is where we would:
                // 1. Fetch fresh data from database/API
                // 2. Update the projectStorage with new data
                // For demo purposes, we just simulate the delay

                log.info("Project data refresh completed");

            } catch (InterruptedException e) {
                log.debug("Project data refresh was cancelled");
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Initialize mock data for demonstration
     * In real application, this would be replaced by data from repository/database
     */
    private void initializeMockData() {
        log.info("Initializing mock project data");

        // Create sample projects with favorites
        createMockProject("CS", "Customer Service", "Complete customer support and service management system",
                "-color-accent-emphasis", true, 24);
        createMockProject("OM", "Order Management", "Comprehensive e-commerce order processing system",
                "-color-success-emphasis", true, 18);
        createMockProject("PG", "Payment Gateway", "Secure payment processing system",
                "-color-warning-emphasis", false, 12);
        createMockProject("IA", "Inventory Admin", "Advanced inventory management system",
                "-color-accent-emphasis", false, 15);
        createMockProject("RP", "Reports Portal", "Business intelligence and analytics platform",
                "-color-danger-emphasis", false, 8);

        log.info("Initialized {} mock projects", projectStorage.size());
    }

    private void createMockProject(String code, String name, String description,
                                   String color, boolean isFavorite, int screenCount) {
        Project project = Project.builder()
                .code(code)
                .name(name)
                .description(description)
                .type(Project.ProjectType.SUBSYSTEM)
                .status(Project.ProjectStatus.ACTIVE)
                .color(color)
                .isFavorite(isFavorite)
                .screenCount(screenCount)
                .createdBy("system")
                .updatedBy("system")
                .build();

        project.setCreatedAt(LocalDateTime.now().minusDays(30));
        project.setUpdatedAt(LocalDateTime.now().minusHours(1));

        projectStorage.put(code, project);
    }
}