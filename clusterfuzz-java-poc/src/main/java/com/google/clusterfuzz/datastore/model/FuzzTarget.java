package com.google.clusterfuzz.datastore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Represents a fuzz target entity.
 * A fuzz target is a specific binary that can be fuzzed by a fuzzing engine.
 */
@Entity
@Table(name = "fuzz_targets", indexes = {
    @Index(name = "idx_fuzz_target_engine", columnList = "engine"),
    @Index(name = "idx_fuzz_target_project", columnList = "project"),
    @Index(name = "idx_fuzz_target_binary", columnList = "binary"),
    @Index(name = "idx_fuzz_target_fully_qualified_name", columnList = "fullyQualifiedName", unique = true)
})
public class FuzzTarget extends BaseModel {

    /**
     * Pattern for normalizing special characters in names.
     * Replaces special characters like slash, colon, etc. with hyphens.
     */
    private static final Pattern SPECIAL_CHARS_REGEX = Pattern.compile("[^a-zA-Z0-9_-]+");

    /**
     * The fuzzing engine this target belongs to (e.g., "libFuzzer", "AFL").
     */
    @Column(name = "engine", nullable = false, length = 100)
    @NotBlank(message = "Engine is required")
    @Size(max = 100, message = "Engine name must not exceed 100 characters")
    private String engine;

    /**
     * The project name this target belongs to.
     */
    @Column(name = "project", nullable = false, length = 200)
    @NotBlank(message = "Project is required")
    @Size(max = 200, message = "Project name must not exceed 200 characters")
    private String project;

    /**
     * The binary name of the fuzz target.
     */
    @Column(name = "binary", nullable = false, length = 300)
    @NotBlank(message = "Binary name is required")
    @Size(max = 300, message = "Binary name must not exceed 300 characters")
    private String binary;

    /**
     * The fully qualified name of the fuzz target.
     * Format: {engine}_{project}_{binary} (with normalization)
     */
    @Column(name = "fully_qualified_name", nullable = false, unique = true, length = 500)
    private String fullyQualifiedName;

    /**
     * The project qualified name of the fuzz target.
     * Format: {project}_{binary} (with normalization, excluding default project)
     */
    @Column(name = "project_qualified_name", nullable = false, length = 400)
    private String projectQualifiedName;

    /**
     * Default constructor for JPA.
     */
    public FuzzTarget() {
        super();
    }

    /**
     * Constructor with required fields.
     */
    public FuzzTarget(String engine, String project, String binary) {
        this();
        this.engine = engine;
        this.project = project;
        this.binary = binary;
        updateQualifiedNames();
    }

    /**
     * Pre-persist hook to update qualified names.
     */
    @PrePersist
    @PreUpdate
    protected void updateQualifiedNames() {
        if (engine != null && project != null && binary != null) {
            this.fullyQualifiedName = generateFullyQualifiedName(engine, project, binary);
            this.projectQualifiedName = generateProjectQualifiedName(project, binary);
        }
    }

    /**
     * Generate the fully qualified name for a fuzz target.
     * Format: {engine}_{project_qualified_name}
     */
    public static String generateFullyQualifiedName(String engine, String project, String binary) {
        if (engine == null || project == null || binary == null) {
            throw new IllegalArgumentException("Engine, project, and binary must not be null");
        }
        return engine + "_" + generateProjectQualifiedName(project, binary);
    }

    /**
     * Generate the project qualified name for a fuzz target.
     * Handles default project logic and normalization.
     */
    public static String generateProjectQualifiedName(String project, String binary) {
        if (project == null || binary == null) {
            throw new IllegalArgumentException("Project and binary must not be null");
        }

        String normalizedBinary = normalizeName(binary);
        
        // Don't prefix with project name if it's empty or the default project
        if (project.isEmpty() || "clusterfuzz".equals(project)) {
            return normalizedBinary;
        }

        String normalizedProjectPrefix = normalizeName(project) + "_";
        
        // Don't add prefix if binary already starts with it
        if (normalizedBinary.startsWith(normalizedProjectPrefix)) {
            return normalizedBinary;
        }

        return normalizedProjectPrefix + normalizedBinary;
    }

    /**
     * Normalize a name by replacing special characters with hyphens.
     * Important for file system and storage path compatibility.
     */
    public static String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        return SPECIAL_CHARS_REGEX.matcher(name).replaceAll("-").replaceAll("^-+|-+$", "");
    }

    /**
     * Get the fully qualified name for this fuzz target.
     */
    public String getFullyQualifiedName() {
        if (fullyQualifiedName == null && engine != null && project != null && binary != null) {
            fullyQualifiedName = generateFullyQualifiedName(engine, project, binary);
        }
        return fullyQualifiedName;
    }

    /**
     * Get the project qualified name for this fuzz target.
     */
    public String getProjectQualifiedName() {
        if (projectQualifiedName == null && project != null && binary != null) {
            projectQualifiedName = generateProjectQualifiedName(project, binary);
        }
        return projectQualifiedName;
    }

    /**
     * Check if this target belongs to the default project.
     */
    public boolean isDefaultProject() {
        return "clusterfuzz".equals(project) || project == null || project.isEmpty();
    }

    /**
     * Get the normalized binary name.
     */
    public String getNormalizedBinary() {
        return normalizeName(binary);
    }

    /**
     * Get the normalized project name.
     */
    public String getNormalizedProject() {
        return normalizeName(project);
    }

    // Getters and Setters
    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
        updateQualifiedNames();
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
        updateQualifiedNames();
    }

    public String getBinary() {
        return binary;
    }

    public void setBinary(String binary) {
        this.binary = binary;
        updateQualifiedNames();
    }

    public void setFullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }

    public void setProjectQualifiedName(String projectQualifiedName) {
        this.projectQualifiedName = projectQualifiedName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FuzzTarget that = (FuzzTarget) o;
        return Objects.equals(engine, that.engine) &&
               Objects.equals(project, that.project) &&
               Objects.equals(binary, that.binary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), engine, project, binary);
    }

    @Override
    public String toString() {
        return "FuzzTarget{" +
               "id=" + getId() +
               ", engine='" + engine + '\'' +
               ", project='" + project + '\'' +
               ", binary='" + binary + '\'' +
               ", fullyQualifiedName='" + getFullyQualifiedName() + '\'' +
               ", timestamp=" + getTimestamp() +
               '}';
    }
}