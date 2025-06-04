package com.google.clusterfuzz.datastore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Heartbeat entity for tracking bot health and status.
 * Stores information about bot activity, current tasks, and system health.
 */
@Entity
@Table(name = "heartbeats", indexes = {
    @Index(name = "idx_heartbeat_bot_name", columnList = "botName"),
    @Index(name = "idx_heartbeat_last_beat_time", columnList = "lastBeatTime"),
    @Index(name = "idx_heartbeat_platform_id", columnList = "platformId"),
    @Index(name = "idx_heartbeat_source_version", columnList = "sourceVersion"),
    @Index(name = "idx_heartbeat_task_end_time", columnList = "taskEndTime"),
    @Index(name = "idx_heartbeat_bot_platform", columnList = "botName, platformId")
})
public class Heartbeat extends BaseModel {

    @NotBlank(message = "Bot name is required")
    @Size(max = 255, message = "Bot name must not exceed 255 characters")
    @Column(name = "bot_name", nullable = false)
    private String botName;

    @Column(name = "last_beat_time")
    private LocalDateTime lastBeatTime;

    @Column(name = "task_payload", columnDefinition = "TEXT")
    private String taskPayload;

    @Column(name = "task_end_time")
    private LocalDateTime taskEndTime;

    @Size(max = 255, message = "Source version must not exceed 255 characters")
    @Column(name = "source_version")
    private String sourceVersion;

    @Size(max = 255, message = "Platform ID must not exceed 255 characters")
    @Column(name = "platform_id")
    private String platformId;

    @ElementCollection
    @CollectionTable(name = "heartbeat_keywords", joinColumns = @JoinColumn(name = "heartbeat_id"))
    @Column(name = "keyword")
    private List<String> keywords = new ArrayList<>();

    @Column(name = "task_type")
    private String taskType;

    @Column(name = "job_name")
    private String jobName;

    @Column(name = "is_alive", nullable = false)
    private Boolean isAlive = true;

    @Column(name = "cpu_usage")
    private Double cpuUsage;

    @Column(name = "memory_usage")
    private Double memoryUsage;

    @Column(name = "disk_usage")
    private Double diskUsage;

    @Column(name = "network_usage")
    private Double networkUsage;

    // Constructors
    public Heartbeat() {
        super();
        this.lastBeatTime = LocalDateTime.now();
    }

    public Heartbeat(String botName) {
        this();
        this.botName = botName;
    }

    public Heartbeat(String botName, String platformId) {
        this(botName);
        this.platformId = platformId;
    }

    // Getters and Setters
    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public LocalDateTime getLastBeatTime() {
        return lastBeatTime;
    }

    public void setLastBeatTime(LocalDateTime lastBeatTime) {
        this.lastBeatTime = lastBeatTime;
    }

    public String getTaskPayload() {
        return taskPayload;
    }

    public void setTaskPayload(String taskPayload) {
        this.taskPayload = taskPayload;
    }

    public LocalDateTime getTaskEndTime() {
        return taskEndTime;
    }

    public void setTaskEndTime(LocalDateTime taskEndTime) {
        this.taskEndTime = taskEndTime;
    }

    public String getSourceVersion() {
        return sourceVersion;
    }

    public void setSourceVersion(String sourceVersion) {
        this.sourceVersion = sourceVersion;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords != null ? keywords : new ArrayList<>();
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Boolean getIsAlive() {
        return isAlive;
    }

    public void setIsAlive(Boolean isAlive) {
        this.isAlive = isAlive;
    }

    public Double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public Double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(Double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public Double getDiskUsage() {
        return diskUsage;
    }

    public void setDiskUsage(Double diskUsage) {
        this.diskUsage = diskUsage;
    }

    public Double getNetworkUsage() {
        return networkUsage;
    }

    public void setNetworkUsage(Double networkUsage) {
        this.networkUsage = networkUsage;
    }

    // Business methods

    /**
     * Update heartbeat with current timestamp.
     */
    public void beat() {
        this.lastBeatTime = LocalDateTime.now();
        this.isAlive = true;
    }

    /**
     * Update heartbeat with task information.
     */
    public void beat(String taskPayload, LocalDateTime taskEndTime) {
        beat();
        this.taskPayload = taskPayload;
        this.taskEndTime = taskEndTime;
        populateIndices();
    }

    /**
     * Check if bot is considered alive based on heartbeat timing.
     */
    public boolean isAlive(int timeoutMinutes) {
        if (lastBeatTime == null) {
            return false;
        }
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(timeoutMinutes);
        return lastBeatTime.isAfter(cutoff);
    }

    /**
     * Check if bot is considered alive with default timeout (10 minutes).
     */
    public boolean isAliveWithDefaultTimeout() {
        return isAlive(10);
    }

    /**
     * Check if bot is considered alive for Android (1 minute timeout).
     */
    public boolean isAliveForAndroid() {
        return isAlive(1);
    }

    /**
     * Get time since last heartbeat.
     */
    public Duration getTimeSinceLastBeat() {
        if (lastBeatTime == null) {
            return Duration.ofDays(365); // Very long duration if never beat
        }
        return Duration.between(lastBeatTime, LocalDateTime.now());
    }

    /**
     * Check if task is overdue.
     */
    public boolean isTaskOverdue() {
        if (taskEndTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(taskEndTime);
    }

    /**
     * Get time until task end.
     */
    public Duration getTimeUntilTaskEnd() {
        if (taskEndTime == null) {
            return Duration.ZERO;
        }
        return Duration.between(LocalDateTime.now(), taskEndTime);
    }

    /**
     * Check if bot is currently executing a task.
     */
    public boolean isExecutingTask() {
        return taskPayload != null && !taskPayload.trim().isEmpty() && 
               taskEndTime != null && !isTaskOverdue();
    }

    /**
     * Populate search keywords based on bot name and task payload.
     */
    public void populateIndices() {
        List<String> newKeywords = new ArrayList<>();
        
        // Add bot name tokens
        if (botName != null) {
            newKeywords.addAll(tokenize(botName));
        }
        
        // Add task payload tokens
        if (taskPayload != null) {
            newKeywords.addAll(tokenize(taskPayload));
        }
        
        // Add platform tokens
        if (platformId != null) {
            newKeywords.addAll(tokenize(platformId));
        }
        
        // Add job name tokens
        if (jobName != null) {
            newKeywords.addAll(tokenize(jobName));
        }
        
        this.keywords = newKeywords;
    }

    /**
     * Simple tokenization for search keywords.
     */
    private List<String> tokenize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return Arrays.asList(text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s]", " ")
                .split("\\s+"));
    }

    /**
     * Add keyword for searching.
     */
    public void addKeyword(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            if (keywords == null) {
                keywords = new ArrayList<>();
            }
            String normalizedKeyword = keyword.toLowerCase().trim();
            if (!keywords.contains(normalizedKeyword)) {
                keywords.add(normalizedKeyword);
            }
        }
    }

    /**
     * Update system resource usage.
     */
    public void updateResourceUsage(Double cpu, Double memory, Double disk, Double network) {
        this.cpuUsage = cpu;
        this.memoryUsage = memory;
        this.diskUsage = disk;
        this.networkUsage = network;
    }

    /**
     * Check if bot has high resource usage.
     */
    public boolean hasHighResourceUsage() {
        return (cpuUsage != null && cpuUsage > 80.0) ||
               (memoryUsage != null && memoryUsage > 80.0) ||
               (diskUsage != null && diskUsage > 90.0);
    }

    /**
     * Get bot status summary.
     */
    public String getStatusSummary() {
        StringBuilder status = new StringBuilder();
        status.append("Bot: ").append(botName);
        
        if (platformId != null) {
            status.append(" (").append(platformId).append(")");
        }
        
        if (isAliveWithDefaultTimeout()) {
            status.append(" - ALIVE");
        } else {
            status.append(" - DEAD");
        }
        
        if (isExecutingTask()) {
            status.append(" - BUSY");
        } else {
            status.append(" - IDLE");
        }
        
        if (hasHighResourceUsage()) {
            status.append(" - HIGH USAGE");
        }
        
        return status.toString();
    }

    /**
     * Mark bot as dead.
     */
    public void markDead() {
        this.isAlive = false;
    }

    /**
     * Clear current task.
     */
    public void clearTask() {
        this.taskPayload = null;
        this.taskEndTime = null;
        this.taskType = null;
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        populateIndices();
    }

    @Override
    public String toString() {
        return "Heartbeat{" +
                "id=" + getId() +
                ", botName='" + botName + '\'' +
                ", lastBeatTime=" + lastBeatTime +
                ", platformId='" + platformId + '\'' +
                ", isAlive=" + isAlive +
                ", hasTask=" + isExecutingTask() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Heartbeat)) return false;
        if (!super.equals(o)) return false;

        Heartbeat heartbeat = (Heartbeat) o;
        return botName != null ? botName.equals(heartbeat.botName) : heartbeat.botName == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (botName != null ? botName.hashCode() : 0);
        return result;
    }
}