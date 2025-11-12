package com.noteflix.pcm.domain.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Project entity representing a subsystem, module, or service
 * <p>
 * Follows Single Responsibility Principle - only represents project data
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class Project extends BaseEntity {

    private String name;
    private String code;
    private String description;
    private ProjectType type;
    private ProjectStatus status;
    private String color; // Hex color for UI
    private String createdBy;
    private String updatedBy;

    /**
     * Validation method
     */
    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Project name is required");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Project code is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("Project type is required");
        }
        if (status == null) {
            status = ProjectStatus.ACTIVE; // Default status
        }
    }

    /**
     * Project types
     */
    public enum ProjectType {
        SUBSYSTEM("Subsystem"),
        MODULE("Module"),
        SERVICE("Service");

        private final String displayName;

        ProjectType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Project status
     */
    public enum ProjectStatus {
        ACTIVE("Active"),
        ARCHIVED("Archived"),
        DEPRECATED("Deprecated");

        private final String displayName;

        ProjectStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}

