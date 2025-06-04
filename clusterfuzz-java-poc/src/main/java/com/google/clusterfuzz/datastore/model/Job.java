package com.google.clusterfuzz.datastore.model;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.clusterfuzz.common.Environment;
import com.google.clusterfuzz.search.SearchTokenizer;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Definition of a job type used by the bots.
 * Converted from Python Job model in data_types.py
 */
@Entity
@Table(name = "Job")
public class Job extends BaseModel {

    // Valid name regex pattern - allows alphanumeric, underscore, and hyphen
    public static final String VALID_NAME_PATTERN = "^[a-zA-Z0-9_-]+$";

    @Column(name = "name", nullable = false, unique = true)
    @Pattern(regexp = VALID_NAME_PATTERN, message = "Job name must contain only alphanumeric characters, underscores, and hyphens")
    private String name;

    @Column(name = "environment_string", columnDefinition = "TEXT")
    private String environmentString;

    @Column(name = "platform")
    private String platform;

    @Column(name = "custom_binary_key")
    private String customBinaryKey;

    @Column(name = "custom_binary_filename")
    private String customBinaryFilename;

    @Column(name = "custom_binary_revision")
    private Integer customBinaryRevision;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "job_templates", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "template_name")
    private Set<String> templates = new HashSet<>();

    @Column(name = "project")
    private String project;

    @ElementCollection
    @CollectionTable(name = "job_keywords", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "keyword")
    private Set<String> keywords = new HashSet<>();

    @Column(name = "external_reproduction_topic")
    private String externalReproductionTopic;

    @Column(name = "external_updates_subscription")
    private String externalUpdatesSubscription;

    // Default constructor
    public Job() {
        super();
    }

    // Constructor with name
    public Job(String name) {
        super();
        this.name = name;
    }

    /**
     * Check if this job is external (has external reproduction infrastructure).
     */
    public boolean isExternal() {
        return (externalReproductionTopic != null && !externalReproductionTopic.isEmpty()) ||
               (externalUpdatesSubscription != null && !externalUpdatesSubscription.isEmpty());
    }

    /**
     * Get the environment as a Map for this job, including any environment
     * variables from its templates.
     */
    public Map<String, String> getEnvironment() {
        if (templates == null || templates.isEmpty()) {
            return Environment.parseEnvironmentDefinition(environmentString);
        }

        Map<String, String> jobEnvironment = new HashMap<>();
        
        // Process templates first
        for (String templateName : templates) {
            // TODO: Implement JobTemplate lookup when that model is converted
            // JobTemplate template = jobTemplateRepository.findByName(templateName);
            // if (template != null) {
            //     Map<String, String> templateEnvironment = 
            //         Environment.parseEnvironmentDefinition(template.getEnvironmentString());
            //     jobEnvironment.putAll(templateEnvironment);
            // }
        }

        // Apply environment overrides
        Map<String, String> environmentOverrides = 
            Environment.parseEnvironmentDefinition(environmentString);
        jobEnvironment.putAll(environmentOverrides);

        return jobEnvironment;
    }

    /**
     * Get the environment string for this job, including any environment
     * variables from its templates. Avoid using this if possible.
     */
    public String getEnvironmentString() {
        Map<String, String> jobEnvironment = getEnvironment();
        return jobEnvironment.entrySet().stream()
            .map(entry -> entry.getKey() + " = " + entry.getValue())
            .collect(Collectors.joining("\n"));
    }

    /**
     * Populate keywords for fast job searching.
     */
    public void populateIndices() {
        Set<String> allKeywords = new HashSet<>();
        
        if (name != null) {
            allKeywords.addAll(SearchTokenizer.tokenize(name));
        }
        
        if (project != null) {
            allKeywords.addAll(SearchTokenizer.tokenize(project));
        }
        
        this.keywords = allKeywords;
    }

    /**
     * Pre-persist hook to set project and populate indices.
     */
    @PrePersist
    @PreUpdate
    public void prePersist() {
        // Set project from environment if not already set
        if (project == null || project.isEmpty()) {
            Map<String, String> env = getEnvironment();
            project = env.getOrDefault("PROJECT_NAME", Environment.getDefaultProjectName());
        }
        
        populateIndices();
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

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getCustomBinaryKey() {
        return customBinaryKey;
    }

    public void setCustomBinaryKey(String customBinaryKey) {
        this.customBinaryKey = customBinaryKey;
    }

    public String getCustomBinaryFilename() {
        return customBinaryFilename;
    }

    public void setCustomBinaryFilename(String customBinaryFilename) {
        this.customBinaryFilename = customBinaryFilename;
    }

    public Integer getCustomBinaryRevision() {
        return customBinaryRevision;
    }

    public void setCustomBinaryRevision(Integer customBinaryRevision) {
        this.customBinaryRevision = customBinaryRevision;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getTemplates() {
        return templates;
    }

    public void setTemplates(Set<String> templates) {
        this.templates = templates;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(Set<String> keywords) {
        this.keywords = keywords;
    }

    public String getExternalReproductionTopic() {
        return externalReproductionTopic;
    }

    public void setExternalReproductionTopic(String externalReproductionTopic) {
        this.externalReproductionTopic = externalReproductionTopic;
    }

    public String getExternalUpdatesSubscription() {
        return externalUpdatesSubscription;
    }

    public void setExternalUpdatesSubscription(String externalUpdatesSubscription) {
        this.externalUpdatesSubscription = externalUpdatesSubscription;
    }

    @Override
    public String toString() {
        return "Job{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", platform='" + platform + '\'' +
                ", project='" + project + '\'' +
                ", isExternal=" + isExternal() +
                '}';
    }
}