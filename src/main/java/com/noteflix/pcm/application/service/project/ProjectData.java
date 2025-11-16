package com.noteflix.pcm.application.service.project;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Project Data Model
 * <p>
 * Represents project information loaded from backend systems.
 * Contains all necessary data for displaying project details.
 */
@Data
@Builder
public class ProjectData {
    private String code;
    private String name;
    private String description;
    private int screenCount;
    private String status;
    private int teamSize;
    private LocalDateTime lastUpdated;
    private List<String> features;

    /**
     * Gets a formatted status with emoji
     */
    public String getFormattedStatus() {
        return switch (status) {
            case "Active" -> "🟢 Active";
            case "Development" -> "🟡 Development";
            case "Planning" -> "🔵 Planning";
            case "Maintenance" -> "🟠 Maintenance";
            default -> "⚫ " + status;
        };
    }

    /**
     * Gets project color class based on status
     */
    public String getProjectColorClass() {
        return switch (status) {
            case "Active" -> "project-color-success";
            case "Development" -> "project-color-warning";
            case "Planning" -> "project-color-accent";
            case "Maintenance" -> "project-color-danger";
            default -> "project-color-accent";
        };
    }

    /**
     * Gets formatted screen count
     */
    public String getFormattedScreenCount() {
        return screenCount + " screen" + (screenCount != 1 ? "s" : "");
    }

    /**
     * Gets formatted team size
     */
    public String getFormattedTeamSize() {
        return teamSize + " developer" + (teamSize != 1 ? "s" : "");
    }
}