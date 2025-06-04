package com.google.clusterfuzz.datastore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a data bundle associated with a fuzzer.
 * Data bundles contain test data, dictionaries, and other resources needed for fuzzing.
 */
@Entity
@Table(name = "data_bundles", indexes = {
    @Index(name = "idx_data_bundle_name", columnList = "name", unique = true),
    @Index(name = "idx_data_bundle_bucket", columnList = "bucketName"),
    @Index(name = "idx_data_bundle_timestamp", columnList = "timestamp"),
    @Index(name = "idx_data_bundle_sync_to_worker", columnList = "syncToWorker"),
    @Index(name = "idx_data_bundle_active", columnList = "active")
})
public class DataBundle extends BaseModel {

    /**
     * Valid name pattern - alphanumeric, hyphens, and underscores only.
     */
    private static final String VALID_NAME_PATTERN = "^[a-zA-Z0-9_-]+$";

    /**
     * The data bundle's name (important for identifying shared bundles).
     * Must be unique across all data bundles.
     */
    @Column(name = "name", nullable = false, unique = true, length = 200)
    @NotBlank(message = "Data bundle name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    @Pattern(regexp = VALID_NAME_PATTERN, message = "Name must contain only alphanumeric characters, hyphens, and underscores")
    private String name;

    /**
     * Name of cloud storage bucket on GCS.
     */
    @Column(name = "bucket_name", nullable = false, length = 100)
    @NotBlank(message = "Bucket name is required")
    @Size(max = 100, message = "Bucket name must not exceed 100 characters")
    private String bucketName;

    /**
     * Data bundle's source (for accountability).
     * This field is deprecated but kept for compatibility.
     */
    @Column(name = "source", length = 200)
    @Size(max = 200, message = "Source must not exceed 200 characters")
    @Deprecated
    private String source;

    /**
     * Creation timestamp.
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * Whether or not bundle should be synced to worker instead.
     * Fuzzer scripts are usually run on trusted hosts, so data bundles are synced
     * there. In libFuzzer's case, we want the bundle to be on the same machine as
     * where the libFuzzer binary will run (untrusted).
     */
    @Column(name = "sync_to_worker", nullable = false)
    private Boolean syncToWorker = false;

    /**
     * Whether this data bundle is currently active and available for use.
     */
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /**
     * Description of the data bundle contents.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Version or revision of the data bundle.
     */
    @Column(name = "version", length = 50)
    @Size(max = 50, message = "Version must not exceed 50 characters")
    private String version;

    /**
     * Size of the data bundle in bytes.
     */
    @Column(name = "size_bytes")
    private Long sizeBytes;

    /**
     * Number of files in the data bundle.
     */
    @Column(name = "file_count")
    private Integer fileCount;

    /**
     * Last time this bundle was updated.
     */
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    /**
     * Last time this bundle was accessed/used.
     */
    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    /**
     * Number of times this bundle has been used.
     */
    @Column(name = "usage_count", nullable = false)
    private Long usageCount = 0L;

    /**
     * Tags associated with this data bundle (comma-separated).
     */
    @Column(name = "tags", length = 500)
    @Size(max = 500, message = "Tags must not exceed 500 characters")
    private String tags;

    /**
     * Checksum/hash of the bundle contents for integrity verification.
     */
    @Column(name = "checksum", length = 128)
    @Size(max = 128, message = "Checksum must not exceed 128 characters")
    private String checksum;

    /**
     * Default constructor for JPA.
     */
    public DataBundle() {
        super();
        this.timestamp = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Constructor with required fields.
     */
    public DataBundle(String name, String bucketName) {
        this();
        this.name = name;
        this.bucketName = bucketName;
    }

    /**
     * Constructor with sync preference.
     */
    public DataBundle(String name, String bucketName, Boolean syncToWorker) {
        this(name, bucketName);
        this.syncToWorker = syncToWorker != null ? syncToWorker : false;
    }

    /**
     * Pre-update hook to update the lastUpdated timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Returns the GCS URL of the bucket storing this data bundle's contents.
     */
    public String getBucketUrl() {
        if (bucketName == null || bucketName.isEmpty()) {
            return null;
        }
        return "gs://" + bucketName;
    }

    /**
     * Returns the full GCS path for this data bundle.
     */
    public String getFullPath() {
        String bucketUrl = getBucketUrl();
        if (bucketUrl == null) {
            return null;
        }
        return bucketUrl + "/" + name;
    }

    /**
     * Mark this bundle as accessed and increment usage count.
     */
    public void markAccessed() {
        this.lastAccessed = LocalDateTime.now();
        this.usageCount = (this.usageCount != null ? this.usageCount : 0L) + 1;
    }

    /**
     * Update bundle metadata after sync/upload.
     */
    public void updateMetadata(Long sizeBytes, Integer fileCount, String checksum) {
        this.sizeBytes = sizeBytes;
        this.fileCount = fileCount;
        this.checksum = checksum;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Check if this bundle is stale (not accessed recently).
     */
    public boolean isStale(int days) {
        if (lastAccessed == null) {
            return true;
        }
        return lastAccessed.isBefore(LocalDateTime.now().minusDays(days));
    }

    /**
     * Check if this bundle is recently updated.
     */
    public boolean isRecentlyUpdated(int hours) {
        if (lastUpdated == null) {
            return false;
        }
        return lastUpdated.isAfter(LocalDateTime.now().minusHours(hours));
    }

    /**
     * Get formatted size string.
     */
    public String getFormattedSize() {
        if (sizeBytes == null) {
            return "Unknown";
        }
        
        if (sizeBytes < 1024) {
            return sizeBytes + " B";
        } else if (sizeBytes < 1024 * 1024) {
            return String.format("%.1f KB", sizeBytes / 1024.0);
        } else if (sizeBytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", sizeBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Add a tag to this bundle.
     */
    public void addTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return;
        }
        
        if (tags == null || tags.isEmpty()) {
            tags = tag.trim();
        } else if (!hasTag(tag)) {
            tags = tags + "," + tag.trim();
        }
    }

    /**
     * Remove a tag from this bundle.
     */
    public void removeTag(String tag) {
        if (tags == null || tag == null) {
            return;
        }
        
        String[] tagArray = tags.split(",");
        StringBuilder newTags = new StringBuilder();
        
        for (String existingTag : tagArray) {
            if (!existingTag.trim().equals(tag.trim())) {
                if (newTags.length() > 0) {
                    newTags.append(",");
                }
                newTags.append(existingTag.trim());
            }
        }
        
        tags = newTags.toString();
    }

    /**
     * Check if this bundle has a specific tag.
     */
    public boolean hasTag(String tag) {
        if (tags == null || tag == null) {
            return false;
        }
        
        String[] tagArray = tags.split(",");
        for (String existingTag : tagArray) {
            if (existingTag.trim().equals(tag.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get tags as an array.
     */
    public String[] getTagsArray() {
        if (tags == null || tags.isEmpty()) {
            return new String[0];
        }
        return tags.split(",");
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    @Deprecated
    public String getSource() {
        return source;
    }

    @Deprecated
    public void setSource(String source) {
        this.source = source;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getSyncToWorker() {
        return syncToWorker;
    }

    public void setSyncToWorker(Boolean syncToWorker) {
        this.syncToWorker = syncToWorker != null ? syncToWorker : false;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active != null ? active : true;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public Integer getFileCount() {
        return fileCount;
    }

    public void setFileCount(Integer fileCount) {
        this.fileCount = fileCount;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public LocalDateTime getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(LocalDateTime lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public Long getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Long usageCount) {
        this.usageCount = usageCount != null ? usageCount : 0L;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DataBundle that = (DataBundle) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }

    @Override
    public String toString() {
        return "DataBundle{" +
               "id=" + getId() +
               ", name='" + name + '\'' +
               ", bucketName='" + bucketName + '\'' +
               ", syncToWorker=" + syncToWorker +
               ", active=" + active +
               ", version='" + version + '\'' +
               ", sizeBytes=" + sizeBytes +
               ", fileCount=" + fileCount +
               ", usageCount=" + usageCount +
               ", timestamp=" + timestamp +
               '}';
    }
}