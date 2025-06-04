package com.google.clusterfuzz.datastore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents the mapping between fuzz targets and jobs with additional metadata for selection.
 * This entity tracks which fuzz targets are associated with which jobs and their execution metadata.
 */
@Entity
@Table(name = "fuzz_target_jobs", indexes = {
    @Index(name = "idx_fuzz_target_job_target_name", columnList = "fuzzTargetName"),
    @Index(name = "idx_fuzz_target_job_job", columnList = "job"),
    @Index(name = "idx_fuzz_target_job_engine", columnList = "engine"),
    @Index(name = "idx_fuzz_target_job_last_run", columnList = "lastRun"),
    @Index(name = "idx_fuzz_target_job_weight", columnList = "weight"),
    @Index(name = "idx_fuzz_target_job_composite_key", columnList = "compositeKey", unique = true)
})
public class FuzzTargetJob extends BaseModel {

    /**
     * The fully qualified fuzz target name.
     * This should match the fullyQualifiedName from FuzzTarget.
     */
    @Column(name = "fuzz_target_name", nullable = false, length = 500)
    @NotBlank(message = "Fuzz target name is required")
    @Size(max = 500, message = "Fuzz target name must not exceed 500 characters")
    private String fuzzTargetName;

    /**
     * The job this target runs as.
     */
    @Column(name = "job", nullable = false, length = 200)
    @NotBlank(message = "Job is required")
    @Size(max = 200, message = "Job name must not exceed 200 characters")
    private String job;

    /**
     * The fuzzing engine this target runs with.
     */
    @Column(name = "engine", nullable = false, length = 100)
    @NotBlank(message = "Engine is required")
    @Size(max = 100, message = "Engine name must not exceed 100 characters")
    private String engine;

    /**
     * Relative frequency with which to select this fuzzer.
     * Higher weights mean higher probability of selection.
     */
    @Column(name = "weight", nullable = false)
    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    private Double weight = 1.0;

    /**
     * Approximate last time this target was run.
     * Used for scheduling and load balancing decisions.
     */
    @Column(name = "last_run")
    private LocalDateTime lastRun;

    /**
     * Composite key for uniqueness constraint.
     * Format: {fuzzTargetName}/{job}
     */
    @Column(name = "composite_key", nullable = false, unique = true, length = 700)
    private String compositeKey;

    /**
     * Number of times this target has been executed.
     */
    @Column(name = "execution_count", nullable = false)
    private Long executionCount = 0L;

    /**
     * Average execution time in seconds.
     */
    @Column(name = "average_execution_time")
    private Double averageExecutionTime;

    /**
     * Success rate as a percentage (0.0 to 100.0).
     */
    @Column(name = "success_rate")
    private Double successRate;

    /**
     * Whether this target-job combination is currently active.
     */
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /**
     * Priority level for this target-job combination.
     * Higher values indicate higher priority.
     */
    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    /**
     * Default constructor for JPA.
     */
    public FuzzTargetJob() {
        super();
    }

    /**
     * Constructor with required fields.
     */
    public FuzzTargetJob(String fuzzTargetName, String job, String engine) {
        this();
        this.fuzzTargetName = fuzzTargetName;
        this.job = job;
        this.engine = engine;
        this.weight = 1.0;
        updateCompositeKey();
    }

    /**
     * Constructor with weight.
     */
    public FuzzTargetJob(String fuzzTargetName, String job, String engine, Double weight) {
        this(fuzzTargetName, job, engine);
        this.weight = weight != null ? weight : 1.0;
    }

    /**
     * Pre-persist hook to update composite key.
     */
    @PrePersist
    @PreUpdate
    protected void updateCompositeKey() {
        if (fuzzTargetName != null && job != null) {
            this.compositeKey = generateCompositeKey(fuzzTargetName, job);
        }
    }

    /**
     * Generate the composite key for a fuzz target job combination.
     */
    public static String generateCompositeKey(String fuzzTargetName, String job) {
        if (fuzzTargetName == null || job == null) {
            throw new IllegalArgumentException("Fuzz target name and job must not be null");
        }
        return fuzzTargetName + "/" + job;
    }

    /**
     * Update the last run timestamp to now.
     */
    public void updateLastRun() {
        this.lastRun = LocalDateTime.now();
        this.executionCount++;
    }

    /**
     * Update execution statistics.
     */
    public void updateExecutionStats(double executionTime, boolean success) {
        updateLastRun();
        
        // Update average execution time
        if (averageExecutionTime == null) {
            averageExecutionTime = executionTime;
        } else {
            // Exponential moving average with alpha = 0.1
            averageExecutionTime = 0.9 * averageExecutionTime + 0.1 * executionTime;
        }
        
        // Update success rate
        if (successRate == null) {
            successRate = success ? 100.0 : 0.0;
        } else {
            // Exponential moving average for success rate
            double currentSuccess = success ? 100.0 : 0.0;
            successRate = 0.9 * successRate + 0.1 * currentSuccess;
        }
    }

    /**
     * Calculate the effective weight considering success rate and recency.
     */
    public double getEffectiveWeight() {
        double baseWeight = weight != null ? weight : 1.0;
        
        // Adjust weight based on success rate
        if (successRate != null) {
            baseWeight *= (successRate / 100.0);
        }
        
        // Adjust weight based on recency (prefer targets that haven't run recently)
        if (lastRun != null) {
            long hoursSinceLastRun = java.time.Duration.between(lastRun, LocalDateTime.now()).toHours();
            // Increase weight for targets that haven't run in a while
            double recencyMultiplier = Math.min(2.0, 1.0 + (hoursSinceLastRun / 24.0));
            baseWeight *= recencyMultiplier;
        }
        
        return baseWeight;
    }

    /**
     * Check if this target-job combination is eligible for execution.
     */
    public boolean isEligibleForExecution() {
        return active != null && active && weight != null && weight > 0;
    }

    /**
     * Get time since last run in hours.
     */
    public Long getHoursSinceLastRun() {
        if (lastRun == null) {
            return null;
        }
        return java.time.Duration.between(lastRun, LocalDateTime.now()).toHours();
    }

    // Getters and Setters
    public String getFuzzTargetName() {
        return fuzzTargetName;
    }

    public void setFuzzTargetName(String fuzzTargetName) {
        this.fuzzTargetName = fuzzTargetName;
        updateCompositeKey();
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
        updateCompositeKey();
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight != null ? weight : 1.0;
    }

    public LocalDateTime getLastRun() {
        return lastRun;
    }

    public void setLastRun(LocalDateTime lastRun) {
        this.lastRun = lastRun;
    }

    public String getCompositeKey() {
        return compositeKey;
    }

    public void setCompositeKey(String compositeKey) {
        this.compositeKey = compositeKey;
    }

    public Long getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(Long executionCount) {
        this.executionCount = executionCount != null ? executionCount : 0L;
    }

    public Double getAverageExecutionTime() {
        return averageExecutionTime;
    }

    public void setAverageExecutionTime(Double averageExecutionTime) {
        this.averageExecutionTime = averageExecutionTime;
    }

    public Double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active != null ? active : true;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority != null ? priority : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FuzzTargetJob that = (FuzzTargetJob) o;
        return Objects.equals(fuzzTargetName, that.fuzzTargetName) &&
               Objects.equals(job, that.job);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fuzzTargetName, job);
    }

    @Override
    public String toString() {
        return "FuzzTargetJob{" +
               "id=" + getId() +
               ", fuzzTargetName='" + fuzzTargetName + '\'' +
               ", job='" + job + '\'' +
               ", engine='" + engine + '\'' +
               ", weight=" + weight +
               ", lastRun=" + lastRun +
               ", executionCount=" + executionCount +
               ", successRate=" + successRate +
               ", active=" + active +
               '}';
    }
}