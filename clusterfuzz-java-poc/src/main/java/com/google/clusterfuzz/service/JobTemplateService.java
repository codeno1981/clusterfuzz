package com.google.clusterfuzz.service;

import com.google.clusterfuzz.datastore.model.JobTemplate;
import com.google.clusterfuzz.datastore.repository.JobTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for JobTemplate entity operations.
 * Provides business logic for job template management and validation.
 */
@Service
@Transactional
public class JobTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(JobTemplateService.class);

    @Autowired
    private JobTemplateRepository jobTemplateRepository;

    // Basic CRUD operations

    /**
     * Create a new job template.
     */
    public JobTemplate createTemplate(String name, String environmentString) {
        return createTemplate(name, environmentString, null);
    }

    /**
     * Create a new job template with description.
     */
    public JobTemplate createTemplate(String name, String environmentString, String description) {
        logger.info("Creating job template: {}", name);
        
        if (jobTemplateRepository.existsByName(name)) {
            throw new IllegalArgumentException("Job template with name '" + name + "' already exists");
        }
        
        JobTemplate template = new JobTemplate(name, environmentString, description);
        
        if (!template.isValidName()) {
            throw new IllegalArgumentException("Invalid template name format: " + name);
        }
        
        JobTemplate saved = jobTemplateRepository.save(template);
        logger.info("Created job template with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Get template by ID.
     */
    @Transactional(readOnly = true)
    public Optional<JobTemplate> getTemplateById(Long id) {
        return jobTemplateRepository.findById(id);
    }

    /**
     * Get template by name.
     */
    @Transactional(readOnly = true)
    public Optional<JobTemplate> getTemplateByName(String name) {
        return jobTemplateRepository.findByName(name);
    }

    /**
     * Get all active templates.
     */
    @Transactional(readOnly = true)
    public List<JobTemplate> getAllActiveTemplates() {
        return jobTemplateRepository.findAllActiveOrderByName();
    }

    /**
     * Get all templates ordered by usage.
     */
    @Transactional(readOnly = true)
    public List<JobTemplate> getTemplatesByUsage() {
        return jobTemplateRepository.findAllActiveOrderByUsageDesc();
    }

    /**
     * Get templates with pagination ordered by usage.
     */
    @Transactional(readOnly = true)
    public Page<JobTemplate> getTemplatesByUsage(Pageable pageable) {
        return jobTemplateRepository.findAllActiveOrderByUsageDesc(pageable);
    }

    /**
     * Update template.
     */
    public JobTemplate updateTemplate(JobTemplate template) {
        logger.info("Updating job template: {}", template.getName());
        
        if (!template.isValidName()) {
            throw new IllegalArgumentException("Invalid template name format: " + template.getName());
        }
        
        return jobTemplateRepository.save(template);
    }

    /**
     * Delete template.
     */
    public void deleteTemplate(Long id) {
        logger.info("Deleting job template with ID: {}", id);
        jobTemplateRepository.deleteById(id);
    }

    // Template usage operations

    /**
     * Use template and increment usage count.
     */
    public void useTemplate(String templateName) {
        logger.debug("Using template: {}", templateName);
        int updated = jobTemplateRepository.incrementUsageCountByName(templateName);
        if (updated == 0) {
            logger.warn("Template not found or not updated: {}", templateName);
        }
    }

    /**
     * Use template by ID and increment usage count.
     */
    public void useTemplate(Long templateId) {
        logger.debug("Using template with ID: {}", templateId);
        int updated = jobTemplateRepository.incrementUsageCount(templateId);
        if (updated == 0) {
            logger.warn("Template not found or not updated with ID: {}", templateId);
        }
    }

    /**
     * Get heavily used templates.
     */
    @Transactional(readOnly = true)
    public List<JobTemplate> getHeavilyUsedTemplates() {
        return jobTemplateRepository.findHeavilyUsedTemplates();
    }

    /**
     * Get unused active templates.
     */
    @Transactional(readOnly = true)
    public List<JobTemplate> getUnusedActiveTemplates() {
        return jobTemplateRepository.findUnusedActiveTemplates();
    }

    /**
     * Get templates by usage range.
     */
    @Transactional(readOnly = true)
    public List<JobTemplate> getTemplatesByUsageRange(Long minUsage, Long maxUsage) {
        return jobTemplateRepository.findByUsageRange(minUsage, maxUsage);
    }

    // Search and filtering operations

    /**
     * Search templates by name or description.
     */
    @Transactional(readOnly = true)
    public List<JobTemplate> searchTemplates(String searchTerm) {
        return jobTemplateRepository.searchActiveByNameOrDescription(searchTerm);
    }

    /**
     * Find templates by environment variable.
     */
    @Transactional(readOnly = true)
    public List<JobTemplate> findTemplatesByEnvironmentVariable(String envVar) {
        return jobTemplateRepository.findByEnvironmentVariable(envVar);
    }

    /**
     * Find templates by name pattern.
     */
    @Transactional(readOnly = true)
    public List<JobTemplate> findTemplatesByNamePattern(String pattern) {
        return jobTemplateRepository.findActiveByNamePattern(pattern);
    }

    // Template validation and maintenance

    /**
     * Validate template configuration.
     */
    @Transactional(readOnly = true)
    public boolean validateTemplate(JobTemplate template) {
        if (!template.isValidName()) {
            logger.warn("Invalid template name: {}", template.getName());
            return false;
        }
        
        if (!template.hasEnvironmentConfiguration()) {
            logger.warn("Template has no environment configuration: {}", template.getName());
            return false;
        }
        
        return true;
    }

    /**
     * Find templates without environment configuration.
     */
    @Transactional(readOnly = true)
    public List<JobTemplate> findTemplatesWithoutEnvironment() {
        return jobTemplateRepository.findTemplatesWithoutEnvironment();
    }

    /**
     * Find templates without description.
     */
    @Transactional(readOnly = true)
    public List<JobTemplate> findTemplatesWithoutDescription() {
        return jobTemplateRepository.findTemplatesWithoutDescription();
    }

    /**
     * Find templates with long environment strings.
     */
    @Transactional(readOnly = true)
    public List<JobTemplate> findTemplatesWithLongEnvironment(int maxLength) {
        return jobTemplateRepository.findTemplatesWithLongEnvironment(maxLength);
    }

    // Template activation/deactivation

    /**
     * Activate template.
     */
    public void activateTemplate(Long templateId) {
        logger.info("Activating template with ID: {}", templateId);
        jobTemplateRepository.updateActiveStatus(templateId, true);
    }

    /**
     * Deactivate template.
     */
    public void deactivateTemplate(Long templateId) {
        logger.info("Deactivating template with ID: {}", templateId);
        jobTemplateRepository.updateActiveStatus(templateId, false);
    }

    /**
     * Deactivate unused templates.
     */
    public int deactivateUnusedTemplates() {
        logger.info("Deactivating unused templates");
        int count = jobTemplateRepository.deactivateUnusedTemplates();
        logger.info("Deactivated {} unused templates", count);
        return count;
    }

    // Statistics and reporting

    /**
     * Get template statistics.
     */
    @Transactional(readOnly = true)
    public TemplateStatistics getTemplateStatistics() {
        TemplateStatistics stats = new TemplateStatistics();
        stats.setActiveCount(jobTemplateRepository.countActiveTemplates());
        stats.setInactiveCount(jobTemplateRepository.countInactiveTemplates());
        stats.setTotalUsageCount(jobTemplateRepository.getTotalUsageCount());
        stats.setAverageUsageCount(jobTemplateRepository.getAverageUsageCount());
        stats.setMaxUsageCount(jobTemplateRepository.getMaxUsageCount());
        return stats;
    }

    /**
     * Get template usage statistics.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getTemplateUsageStats() {
        return jobTemplateRepository.getTemplateUsageStats();
    }

    // Duplicate detection and cleanup

    /**
     * Find templates with duplicate environments.
     */
    @Transactional(readOnly = true)
    public List<JobTemplate> findTemplatesWithDuplicateEnvironments() {
        return jobTemplateRepository.findTemplatesWithDuplicateEnvironments();
    }

    /**
     * Find duplicate environments for a specific template.
     */
    @Transactional(readOnly = true)
    public List<JobTemplate> findDuplicateEnvironments(String environmentString, Long excludeId) {
        return jobTemplateRepository.findDuplicateEnvironments(environmentString, excludeId);
    }

    /**
     * Clean up old unused templates.
     */
    public int cleanupOldUnusedTemplates(int days) {
        logger.info("Cleaning up old unused templates older than {} days", days);
        int count = jobTemplateRepository.deleteOldUnusedTemplates(days);
        logger.info("Deleted {} old unused templates", count);
        return count;
    }

    // Recent activity

    /**
     * Get recently updated templates.
     */
    @Transactional(readOnly = true)
    public List<JobTemplate> getRecentlyUpdatedTemplates(int days) {
        return jobTemplateRepository.findRecentlyUpdated(days);
    }

    /**
     * Get recently created templates.
     */
    @Transactional(readOnly = true)
    public List<JobTemplate> getRecentlyCreatedTemplates(int days) {
        return jobTemplateRepository.findRecentlyCreated(days);
    }

    // Template copying and cloning

    /**
     * Clone template with new name.
     */
    public JobTemplate cloneTemplate(String originalName, String newName) {
        logger.info("Cloning template '{}' to '{}'", originalName, newName);
        
        Optional<JobTemplate> original = getTemplateByName(originalName);
        if (original.isEmpty()) {
            throw new IllegalArgumentException("Original template not found: " + originalName);
        }
        
        if (jobTemplateRepository.existsByName(newName)) {
            throw new IllegalArgumentException("Template with name '" + newName + "' already exists");
        }
        
        JobTemplate clone = new JobTemplate();
        clone.setName(newName);
        clone.setEnvironmentString(original.get().getEnvironmentString());
        clone.setDescription("Cloned from: " + originalName);
        clone.setActive(true);
        
        return jobTemplateRepository.save(clone);
    }

    /**
     * Update template description.
     */
    public void updateTemplateDescription(Long templateId, String description) {
        logger.info("Updating description for template ID: {}", templateId);
        jobTemplateRepository.updateDescription(templateId, description);
    }

    // Template validation helpers

    /**
     * Check if template name is available.
     */
    @Transactional(readOnly = true)
    public boolean isTemplateNameAvailable(String name) {
        return !jobTemplateRepository.existsByName(name);
    }

    /**
     * Get template count.
     */
    @Transactional(readOnly = true)
    public long getTemplateCount() {
        return jobTemplateRepository.count();
    }

    /**
     * Get active template count.
     */
    @Transactional(readOnly = true)
    public long getActiveTemplateCount() {
        return jobTemplateRepository.countActiveTemplates();
    }

    // Inner class for statistics
    public static class TemplateStatistics {
        private long activeCount;
        private long inactiveCount;
        private Long totalUsageCount;
        private Double averageUsageCount;
        private Long maxUsageCount;

        // Getters and setters
        public long getActiveCount() { return activeCount; }
        public void setActiveCount(long activeCount) { this.activeCount = activeCount; }

        public long getInactiveCount() { return inactiveCount; }
        public void setInactiveCount(long inactiveCount) { this.inactiveCount = inactiveCount; }

        public Long getTotalUsageCount() { return totalUsageCount; }
        public void setTotalUsageCount(Long totalUsageCount) { this.totalUsageCount = totalUsageCount; }

        public Double getAverageUsageCount() { return averageUsageCount; }
        public void setAverageUsageCount(Double averageUsageCount) { this.averageUsageCount = averageUsageCount; }

        public Long getMaxUsageCount() { return maxUsageCount; }
        public void setMaxUsageCount(Long maxUsageCount) { this.maxUsageCount = maxUsageCount; }

        public long getTotalCount() { return activeCount + inactiveCount; }
    }
}