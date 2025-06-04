package com.google.clusterfuzz.datastore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents the count of fuzz targets for each job.
 * This entity tracks how many fuzz targets are associated with each job for monitoring and statistics.
 */
@Entity
@Table(name = "fuzz_targets_count", indexes = {
    @Index(name = "idx_fuzz_targets_count_job_name", columnList = "jobName", unique = true),
    @Index(name = "idx_fuzz_targets_count_count", columnList = "count"),
    @Index(name = "idx_fuzz_targets_count_last_updated", columnList = "lastUpdated")
})
public class FuzzTargetsCount extends BaseModel {

    /**
     * The job name this count is for.
     * This serves as the unique identifier for the count record.
     */
    @Column(name = "job_name", nullable = false, unique = true, length = 200)
    @NotBlank(message = "Job name is required")
    @Size(max = 200, message = "Job name must not exceed 200 characters")
    private String jobName;

    /**
     * The number of fuzz targets associated with this job.
     * This field is not indexed in the original Python model for performance.
     */
    @Column(name = "count", nullable = false)
    @NotNull(message = "Count is required")
    @Min(value = 0, message = "Count must be non-negative")
    private Integer count = 0;

    /**
     * The last time this count was updated.
     * Useful for cache invalidation and monitoring.
     */
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    /**
     * The number of active fuzz targets (subset of total count).
     */
    @Column(name = "active_count", nullable = false)
    @Min(value = 0, message = "Active count must be non-negative")
    private Integer activeCount = 0;

    /**
     * The number of enabled fuzz targets (subset of total count).
     */
    @Column(name = "enabled_count", nullable = false)
    @Min(value = 0, message = "Enabled count must be non-negative")
    private Integer enabledCount = 0;

    /**
     * Average weight of fuzz targets for this job.
     */
    @Column(name = "average_weight")
    private Double averageWeight;

    /**
     * Default constructor for JPA.
     */
    public FuzzTargetsCount() {
        super();
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Constructor with job name.
     */
    public FuzzTargetsCount(String jobName) {
        this();
        this.jobName = jobName;
    }

    /**
     * Constructor with job name and count.
     */
    public FuzzTargetsCount(String jobName, Integer count) {
        this(jobName);
        this.count = count != null ? count : 0;
    }

    /**
     * Constructor with all counts.
     */
    public FuzzTargetsCount(String jobName, Integer count, Integer activeCount, Integer enabledCount) {
        this(jobName, count);
        this.activeCount = activeCount != null ? activeCount : 0;
        this.enabledCount = enabledCount != null ? enabledCount : 0;
    }

    /**
     * Pre-persist and pre-update hook to update timestamp.
     */
    @PrePersist
    @PreUpdate
    protected void updateTimestamp() {
        this.lastUpdated = LocalDateTime.now();
        
        // Validate count relationships
        if (activeCount != null && count != null && activeCount > count) {
            throw new IllegalStateException("Active count cannot exceed total count");
        }
        
        if (enabledCount != null && count != null && enabledCount > count) {
            throw new IllegalStateException("Enabled count cannot exceed total count");
        }
    }

    /**
     * Update the count and refresh timestamp.
     */
    public void updateCount(Integer newCount) {
        this.count = newCount != null ? newCount : 0;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Update all counts and refresh timestamp.
     */
    public void updateCounts(Integer totalCount, Integer activeCount, Integer enabledCount) {
        this.count = totalCount != null ? totalCount : 0;
        this.activeCount = activeCount != null ? activeCount : 0;
        this.enabledCount = enabledCount != null ? enabledCount : 0;
        this.lastUpdated = LocalDateTime.now();
        
        // Validate relationships
        if (this.activeCount > this.count) {
            throw new IllegalArgumentException("Active count cannot exceed total count");
        }
        
        if (this.enabledCount > this.count) {
            throw new IllegalArgumentException("Enabled count cannot exceed total count");
        }
    }

    /**
     * Update the average weight.
     */
    public void updateAverageWeight(Double averageWeight) {
        this.averageWeight = averageWeight;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Increment the count by the specified amount.
     */
    public void incrementCount(int increment) {
        this.count = (this.count != null ? this.count : 0) + increment;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Decrement the count by the specified amount.
     */
    public void decrementCount(int decrement) {
        this.count = Math.max(0, (this.count != null ? this.count : 0) - decrement);
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Check if the count data is stale (older than specified hours).
     */
    public boolean isStale(int hours) {
        if (lastUpdated == null) {
            return true;
        }
        return lastUpdated.isBefore(LocalDateTime.now().minusHours(hours));
    }

    /**
     * Get the percentage of active targets.
     */
    public Double getActivePercentage() {
        if (count == null || count == 0) {
            return 0.0;
        }
        return (activeCount != null ? activeCount : 0) * 100.0 / count;
    }

    /**
     * Get the percentage of enabled targets.
     */
    public Double getEnabledPercentage() {
        if (count == null || count == 0) {
            return 0.0;
        }
        return (enabledCount != null ? enabledCount : 0) * 100.0 / count;
    }

    /**
     * Check if this job has any fuzz targets.
     */
    public boolean hasTargets() {
        return count != null && count > 0;
    }

    /**
     * Check if this job has active fuzz targets.
     */
    public boolean hasActiveTargets() {
        return activeCount != null && activeCount > 0;
    }

    // Getters and Setters
    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count != null ? count : 0;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Integer getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(Integer activeCount) {
        this.activeCount = activeCount != null ? activeCount : 0;
    }

    public Integer getEnabledCount() {
        return enabledCount;
    }

    public void setEnabledCount(Integer enabledCount) {
        this.enabledCount = enabledCount != null ? enabledCount : 0;
    }

    public Double getAverageWeight() {
        return averageWeight;
    }

    public void setAverageWeight(Double averageWeight) {
        this.averageWeight = averageWeight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FuzzTargetsCount that = (FuzzTargetsCount) o;
        return Objects.equals(jobName, that.jobName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), jobName);
    }

    @Override
    public String toString() {
        return "FuzzTargetsCount{" +
               "id=" + getId() +
               ", jobName='" + jobName + '\'' +
               ", count=" + count +
               ", activeCount=" + activeCount +
               ", enabledCount=" + enabledCount +
               ", averageWeight=" + averageWeight +
               ", lastUpdated=" + lastUpdated +
               '}';
    }
}