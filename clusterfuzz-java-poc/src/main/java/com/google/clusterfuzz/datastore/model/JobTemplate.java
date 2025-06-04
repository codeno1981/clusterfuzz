package com.google.clusterfuzz.datastore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * JobTemplate entity for storing job template configurations.
 * Templates provide reusable environment configurations for jobs.
 */
@Entity
@Table(name = "job_templates", indexes = {
    @Index(name = "idx_job_template_name", columnList = "name", unique = true)
})
public class JobTemplate extends BaseModel {

    @NotBlank(message = "Template name is required")
    @Size(max = 255, message = "Template name must not exceed 255 characters")
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Lob
    @Column(name = "environment_string", columnDefinition = "TEXT")
    private String environmentString;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "usage_count", nullable = false)
    private Long usageCount = 0L;

    // Constructors
    public JobTemplate() {
        super();
    }

    public JobTemplate(String name, String environmentString) {
        super();
        this.name = name;
        this.environmentString = environmentString;
    }

    public JobTemplate(String name, String environmentString, String description) {
        this(name, environmentString);
        this.description = description;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnvironmentString() {
        return environmentString;
    }

    public void setEnvironmentString(String environmentString) {
        this.environmentString = environmentString;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Long usageCount) {
        this.usageCount = usageCount;
    }

    // Business methods
    
    /**
     * Increment usage count when template is used.
     */
    public void incrementUsage() {
        this.usageCount = (this.usageCount != null ? this.usageCount : 0L) + 1;
    }

    /**
     * Check if template is valid for use.
     */
    public boolean isValidForUse() {
        return active != null && active && name != null && !name.trim().isEmpty();
    }

    /**
     * Get environment variables as a parsed map-like structure.
     * This would typically parse the environment string into key-value pairs.
     */
    public boolean hasEnvironmentVariable(String key) {
        if (environmentString == null || environmentString.trim().isEmpty()) {
            return false;
        }
        return environmentString.contains(key + "=");
    }

    /**
     * Check if template has any environment configuration.
     */
    public boolean hasEnvironmentConfiguration() {
        return environmentString != null && !environmentString.trim().isEmpty();
    }

    /**
     * Get a summary of the template for display purposes.
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Template: ").append(name);
        
        if (description != null && !description.trim().isEmpty()) {
            summary.append(" - ").append(description);
        }
        
        summary.append(" (Used ").append(usageCount != null ? usageCount : 0).append(" times)");
        
        if (active != null && !active) {
            summary.append(" [INACTIVE]");
        }
        
        return summary.toString();
    }

    /**
     * Validate template name format.
     */
    public boolean isValidName() {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        // Template names should follow similar rules to job names
        return name.matches("^[a-zA-Z0-9_-]+$");
    }

    /**
     * Get environment string length for validation.
     */
    public int getEnvironmentStringLength() {
        return environmentString != null ? environmentString.length() : 0;
    }

    /**
     * Check if template is heavily used.
     */
    public boolean isHeavilyUsed() {
        return usageCount != null && usageCount > 10;
    }

    /**
     * Deactivate template.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Activate template.
     */
    public void activate() {
        this.active = true;
    }

    @Override
    public String toString() {
        return "JobTemplate{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", active=" + active +
                ", usageCount=" + usageCount +
                ", hasEnvironment=" + hasEnvironmentConfiguration() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobTemplate)) return false;
        if (!super.equals(o)) return false;

        JobTemplate that = (JobTemplate) o;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}