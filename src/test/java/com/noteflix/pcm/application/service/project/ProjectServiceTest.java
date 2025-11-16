package com.noteflix.pcm.application.service.project;

import com.noteflix.pcm.domain.entity.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProjectServiceImpl
 * Tests all business logic and SOLID principles compliance
 */
class ProjectServiceTest {

    private IProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectServiceImpl();
    }

    @Test
    @DisplayName("Should return all projects")
    void shouldReturnAllProjects() {
        // When
        List<Project> projects = projectService.getAllProjects();

        // Then
        assertNotNull(projects);
        assertEquals(5, projects.size());

        // Verify project codes exist
        List<String> codes = projects.stream().map(Project::getCode).toList();
        assertTrue(codes.contains("CS"));
        assertTrue(codes.contains("OM"));
        assertTrue(codes.contains("PG"));
        assertTrue(codes.contains("IA"));
        assertTrue(codes.contains("RP"));
    }

    @Test
    @DisplayName("Should return only favorite projects")
    void shouldReturnOnlyFavoriteProjects() {
        // When
        List<Project> favoriteProjects = projectService.getFavoriteProjects();

        // Then
        assertNotNull(favoriteProjects);
        assertEquals(2, favoriteProjects.size());

        // Verify all returned projects are favorites
        assertTrue(favoriteProjects.stream().allMatch(Project::isFavorite));

        // Verify specific favorites
        List<String> favoriteCodes = favoriteProjects.stream().map(Project::getCode).toList();
        assertTrue(favoriteCodes.contains("CS"));
        assertTrue(favoriteCodes.contains("OM"));
    }

    @Test
    @DisplayName("Should return only non-favorite projects")
    void shouldReturnOnlyNonFavoriteProjects() {
        // When
        List<Project> nonFavoriteProjects = projectService.getNonFavoriteProjects();

        // Then
        assertNotNull(nonFavoriteProjects);
        assertEquals(3, nonFavoriteProjects.size());

        // Verify all returned projects are not favorites
        assertTrue(nonFavoriteProjects.stream().noneMatch(Project::isFavorite));

        // Verify specific non-favorites
        List<String> nonFavoriteCodes = nonFavoriteProjects.stream().map(Project::getCode).toList();
        assertTrue(nonFavoriteCodes.contains("PG"));
        assertTrue(nonFavoriteCodes.contains("IA"));
        assertTrue(nonFavoriteCodes.contains("RP"));
    }

    @Test
    @DisplayName("Should find project by code")
    void shouldFindProjectByCode() {
        // When
        Optional<Project> project = projectService.getProjectByCode("CS");

        // Then
        assertTrue(project.isPresent());
        assertEquals("CS", project.get().getCode());
        assertEquals("Customer Service", project.get().getName());
        assertTrue(project.get().isFavorite());
    }

    @Test
    @DisplayName("Should return empty optional for non-existent project")
    void shouldReturnEmptyOptionalForNonExistentProject() {
        // When
        Optional<Project> project = projectService.getProjectByCode("XXX");

        // Then
        assertTrue(project.isEmpty());
    }

    @Test
    @DisplayName("Should toggle favorite status")
    void shouldToggleFavoriteStatus() {
        // Given - PG is initially not favorite
        Optional<Project> initialProject = projectService.getProjectByCode("PG");
        assertTrue(initialProject.isPresent());
        assertFalse(initialProject.get().isFavorite());

        // When - toggle to favorite
        Optional<Project> updatedProject = projectService.toggleFavorite("PG");

        // Then
        assertTrue(updatedProject.isPresent());
        assertTrue(updatedProject.get().isFavorite());

        // When - toggle back to non-favorite
        Optional<Project> toggledBackProject = projectService.toggleFavorite("PG");

        // Then
        assertTrue(toggledBackProject.isPresent());
        assertFalse(toggledBackProject.get().isFavorite());
    }

    @Test
    @DisplayName("Should set favorite status")
    void shouldSetFavoriteStatus() {
        // Given - IA is initially not favorite
        Optional<Project> initialProject = projectService.getProjectByCode("IA");
        assertTrue(initialProject.isPresent());
        assertFalse(initialProject.get().isFavorite());

        // When - set as favorite
        Optional<Project> updatedProject = projectService.setFavorite("IA", true);

        // Then
        assertTrue(updatedProject.isPresent());
        assertTrue(updatedProject.get().isFavorite());

        // When - set as non-favorite
        Optional<Project> removedFromFavorites = projectService.setFavorite("IA", false);

        // Then
        assertTrue(removedFromFavorites.isPresent());
        assertFalse(removedFromFavorites.get().isFavorite());
    }

    @Test
    @DisplayName("Should return empty optional when toggling non-existent project")
    void shouldReturnEmptyOptionalWhenTogglingNonExistentProject() {
        // When
        Optional<Project> result = projectService.toggleFavorite("XXX");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return project statistics")
    void shouldReturnProjectStatistics() {
        // When
        IProjectService.ProjectStatistics stats = projectService.getStatistics();

        // Then
        assertNotNull(stats);
        assertEquals(5, stats.totalProjects());
        assertEquals(2, stats.favoriteProjects());
        assertEquals(5, stats.activeProjects()); // All mock projects are active
        assertEquals(0, stats.archivedProjects());
    }

    @Test
    @DisplayName("Should create new project")
    void shouldCreateNewProject() {
        // Given
        Project newProject = Project.builder()
                .code("TEST")
                .name("Test Project")
                .description("Test Description")
                .type(Project.ProjectType.MODULE)
                .status(Project.ProjectStatus.ACTIVE)
                .color("-color-info-emphasis")
                .isFavorite(false)
                .screenCount(5)
                .createdBy("test")
                .updatedBy("test")
                .build();

        // When
        Project createdProject = projectService.createProject(newProject);

        // Then
        assertNotNull(createdProject);
        assertEquals("TEST", createdProject.getCode());
        assertEquals("Test Project", createdProject.getName());

        // Verify it's in the system
        Optional<Project> foundProject = projectService.getProjectByCode("TEST");
        assertTrue(foundProject.isPresent());
        assertEquals("Test Project", foundProject.get().getName());
    }

    @Test
    @DisplayName("Should update existing project")
    void shouldUpdateExistingProject() {
        // Given
        Optional<Project> existingProject = projectService.getProjectByCode("CS");
        assertTrue(existingProject.isPresent());

        Project updatedProject = existingProject.get();
        updatedProject.setDescription("Updated Description");
        updatedProject.setScreenCount(30);

        // When
        Optional<Project> result = projectService.updateProject(updatedProject);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Updated Description", result.get().getDescription());
        assertEquals(30, result.get().getScreenCount());

        // Verify update persisted
        Optional<Project> verifyProject = projectService.getProjectByCode("CS");
        assertTrue(verifyProject.isPresent());
        assertEquals("Updated Description", verifyProject.get().getDescription());
        assertEquals(30, verifyProject.get().getScreenCount());
    }
}