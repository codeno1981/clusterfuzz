package com.google.clusterfuzz.datastore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents metadata associated with a user uploaded test case.
 * This entity tracks the upload process, analysis status, and associated metadata.
 */
@Entity
@Table(name = "testcase_upload_metadata", indexes = {
    @Index(name = "idx_upload_metadata_timestamp", columnList = "timestamp"),
    @Index(name = "idx_upload_metadata_status", columnList = "status"),
    @Index(name = "idx_upload_metadata_uploader", columnList = "uploaderEmail"),
    @Index(name = "idx_upload_metadata_testcase_id", columnList = "testcaseId"),
    @Index(name = "idx_upload_metadata_duplicate_of", columnList = "duplicateOf"),
    @Index(name = "idx_upload_metadata_bot_name", columnList = "botName"),
    @Index(name = "idx_upload_metadata_security_flag", columnList = "securityFlag"),
    @Index(name = "idx_upload_metadata_bundled", columnList = "bundled")
})
public class TestcaseUploadMetadata extends BaseModel {

    /**
     * Upload status enumeration.
     */
    public enum UploadStatus {
        PENDING("pending"),
        PROCESSING("processing"),
        DUPLICATE("duplicate"),
        PROCESSED("processed"),
        FAILED("failed"),
        TIMEOUT("timeout"),
        INVALID("invalid");

        private final String value;

        UploadStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static UploadStatus fromValue(String value) {
            for (UploadStatus status : values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown upload status: " + value);
        }
    }

    /**
     * Upload timestamp.
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * Original testcase filename.
     */
    @Column(name = "filename", length = 500)
    @Size(max = 500, message = "Filename must not exceed 500 characters")
    private String filename;

    /**
     * Current status of the testcase upload.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UploadStatus status = UploadStatus.PENDING;

    /**
     * Uploader email address.
     */
    @Column(name = "uploader_email", length = 255)
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String uploaderEmail;

    /**
     * Name of the bot that ran analysis on this testcase.
     */
    @Column(name = "bot_name", length = 100)
    @Size(max = 100, message = "Bot name must not exceed 100 characters")
    private String botName;

    /**
     * ID of the associated testcase (after processing).
     */
    @Column(name = "testcase_id")
    private Long testcaseId;

    /**
     * ID of the testcase that this is marked as a duplicate of.
     */
    @Column(name = "duplicate_of")
    private Long duplicateOf;

    /**
     * Blobstore key for the testcase associated with this object.
     */
    @Column(name = "blobstore_key", length = 500)
    @Size(max = 500, message = "Blobstore key must not exceed 500 characters")
    private String blobstoreKey;

    /**
     * Testcase timeout in seconds.
     */
    @Column(name = "timeout")
    @Min(value = 0, message = "Timeout must be non-negative")
    private Integer timeout;

    /**
     * Is this a single testcase bundled in an archive?
     */
    @Column(name = "bundled", nullable = false)
    private Boolean bundled = false;

    /**
     * Path to the file in the archive (if bundled).
     */
    @Column(name = "path_in_archive", columnDefinition = "TEXT")
    private String pathInArchive;

    /**
     * Original blobstore key for this object (used for archives).
     */
    @Column(name = "original_blobstore_key", length = 500)
    @Size(max = 500, message = "Original blobstore key must not exceed 500 characters")
    private String originalBlobstoreKey;

    /**
     * Security flag indicating if this is a security-related testcase.
     */
    @Column(name = "security_flag", nullable = false)
    private Boolean securityFlag = false;

    /**
     * Number of retries for this testcase.
     */
    @Column(name = "retries", nullable = false)
    @Min(value = 0, message = "Retries must be non-negative")
    private Integer retries = 0;

    /**
     * Flag to indicate whether bug title should be updated or not.
     */
    @Column(name = "bug_summary_update_flag")
    private Boolean bugSummaryUpdateFlag;

    /**
     * Flag to indicate if we are running in quiet mode (e.g. bug updates).
     */
    @Column(name = "quiet_flag")
    private Boolean quietFlag;

    /**
     * Additional testcase metadata stored as JSON string.
     */
    @Column(name = "additional_metadata", columnDefinition = "TEXT")
    private String additionalMetadata;

    /**
     * Specified issue/bug information.
     */
    @Column(name = "bug_information", length = 1000)
    @Size(max = 1000, message = "Bug information must not exceed 1000 characters")
    private String bugInformation;

    /**
     * Processing start time.
     */
    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;

    /**
     * Processing completion time.
     */
    @Column(name = "processing_completed_at")
    private LocalDateTime processingCompletedAt;

    /**
     * Error message if processing failed.
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * File size in bytes.
     */
    @Column(name = "file_size")
    @Min(value = 0, message = "File size must be non-negative")
    private Long fileSize;

    /**
     * MIME type of the uploaded file.
     */
    @Column(name = "mime_type", length = 100)
    @Size(max = 100, message = "MIME type must not exceed 100 characters")
    private String mimeType;

    /**
     * Default constructor for JPA.
     */
    public TestcaseUploadMetadata() {
        super();
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor with required fields.
     */
    public TestcaseUploadMetadata(String filename, String uploaderEmail) {
        this();
        this.filename = filename;
        this.uploaderEmail = uploaderEmail;
    }

    /**
     * Mark the upload as started processing.
     */
    public void startProcessing(String botName) {
        this.status = UploadStatus.PROCESSING;
        this.botName = botName;
        this.processingStartedAt = LocalDateTime.now();
    }

    /**
     * Mark the upload as successfully processed.
     */
    public void markProcessed(Long testcaseId) {
        this.status = UploadStatus.PROCESSED;
        this.testcaseId = testcaseId;
        this.processingCompletedAt = LocalDateTime.now();
    }

    /**
     * Mark the upload as a duplicate.
     */
    public void markDuplicate(Long duplicateOfId) {
        this.status = UploadStatus.DUPLICATE;
        this.duplicateOf = duplicateOfId;
        this.processingCompletedAt = LocalDateTime.now();
    }

    /**
     * Mark the upload as failed.
     */
    public void markFailed(String errorMessage) {
        this.status = UploadStatus.FAILED;
        this.errorMessage = errorMessage;
        this.processingCompletedAt = LocalDateTime.now();
    }

    /**
     * Mark the upload as timed out.
     */
    public void markTimeout() {
        this.status = UploadStatus.TIMEOUT;
        this.errorMessage = "Processing timed out";
        this.processingCompletedAt = LocalDateTime.now();
    }

    /**
     * Mark the upload as invalid.
     */
    public void markInvalid(String reason) {
        this.status = UploadStatus.INVALID;
        this.errorMessage = reason;
        this.processingCompletedAt = LocalDateTime.now();
    }

    /**
     * Increment retry count.
     */
    public void incrementRetries() {
        this.retries = (this.retries != null ? this.retries : 0) + 1;
    }

    /**
     * Check if upload is completed (either successfully or with error).
     */
    public boolean isCompleted() {
        return status == UploadStatus.PROCESSED || 
               status == UploadStatus.DUPLICATE || 
               status == UploadStatus.FAILED || 
               status == UploadStatus.TIMEOUT || 
               status == UploadStatus.INVALID;
    }

    /**
     * Check if upload was successful.
     */
    public boolean isSuccessful() {
        return status == UploadStatus.PROCESSED;
    }

    /**
     * Check if upload is still being processed.
     */
    public boolean isProcessing() {
        return status == UploadStatus.PROCESSING;
    }

    /**
     * Check if upload is pending.
     */
    public boolean isPending() {
        return status == UploadStatus.PENDING;
    }

    /**
     * Get processing duration in seconds.
     */
    public Long getProcessingDurationSeconds() {
        if (processingStartedAt == null || processingCompletedAt == null) {
            return null;
        }
        return java.time.Duration.between(processingStartedAt, processingCompletedAt).getSeconds();
    }

    /**
     * Check if this upload has exceeded the maximum retry count.
     */
    public boolean hasExceededMaxRetries(int maxRetries) {
        return retries != null && retries >= maxRetries;
    }

    /**
     * Get the file extension from filename.
     */
    public String getFileExtension() {
        if (filename == null || !filename.contains(".")) {
            return null;
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    // Getters and Setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public UploadStatus getStatus() {
        return status;
    }

    public void setStatus(UploadStatus status) {
        this.status = status;
    }

    public String getUploaderEmail() {
        return uploaderEmail;
    }

    public void setUploaderEmail(String uploaderEmail) {
        this.uploaderEmail = uploaderEmail;
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public Long getTestcaseId() {
        return testcaseId;
    }

    public void setTestcaseId(Long testcaseId) {
        this.testcaseId = testcaseId;
    }

    public Long getDuplicateOf() {
        return duplicateOf;
    }

    public void setDuplicateOf(Long duplicateOf) {
        this.duplicateOf = duplicateOf;
    }

    public String getBlobstoreKey() {
        return blobstoreKey;
    }

    public void setBlobstoreKey(String blobstoreKey) {
        this.blobstoreKey = blobstoreKey;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Boolean getBundled() {
        return bundled;
    }

    public void setBundled(Boolean bundled) {
        this.bundled = bundled != null ? bundled : false;
    }

    public String getPathInArchive() {
        return pathInArchive;
    }

    public void setPathInArchive(String pathInArchive) {
        this.pathInArchive = pathInArchive;
    }

    public String getOriginalBlobstoreKey() {
        return originalBlobstoreKey;
    }

    public void setOriginalBlobstoreKey(String originalBlobstoreKey) {
        this.originalBlobstoreKey = originalBlobstoreKey;
    }

    public Boolean getSecurityFlag() {
        return securityFlag;
    }

    public void setSecurityFlag(Boolean securityFlag) {
        this.securityFlag = securityFlag != null ? securityFlag : false;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries != null ? retries : 0;
    }

    public Boolean getBugSummaryUpdateFlag() {
        return bugSummaryUpdateFlag;
    }

    public void setBugSummaryUpdateFlag(Boolean bugSummaryUpdateFlag) {
        this.bugSummaryUpdateFlag = bugSummaryUpdateFlag;
    }

    public Boolean getQuietFlag() {
        return quietFlag;
    }

    public void setQuietFlag(Boolean quietFlag) {
        this.quietFlag = quietFlag;
    }

    public String getAdditionalMetadata() {
        return additionalMetadata;
    }

    public void setAdditionalMetadata(String additionalMetadata) {
        this.additionalMetadata = additionalMetadata;
    }

    public String getBugInformation() {
        return bugInformation;
    }

    public void setBugInformation(String bugInformation) {
        this.bugInformation = bugInformation;
    }

    public LocalDateTime getProcessingStartedAt() {
        return processingStartedAt;
    }

    public void setProcessingStartedAt(LocalDateTime processingStartedAt) {
        this.processingStartedAt = processingStartedAt;
    }

    public LocalDateTime getProcessingCompletedAt() {
        return processingCompletedAt;
    }

    public void setProcessingCompletedAt(LocalDateTime processingCompletedAt) {
        this.processingCompletedAt = processingCompletedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TestcaseUploadMetadata that = (TestcaseUploadMetadata) o;
        return Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(filename, that.filename) &&
               Objects.equals(uploaderEmail, that.uploaderEmail) &&
               Objects.equals(blobstoreKey, that.blobstoreKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), timestamp, filename, uploaderEmail, blobstoreKey);
    }

    @Override
    public String toString() {
        return "TestcaseUploadMetadata{" +
               "id=" + getId() +
               ", timestamp=" + timestamp +
               ", filename='" + filename + '\'' +
               ", status=" + status +
               ", uploaderEmail='" + uploaderEmail + '\'' +
               ", testcaseId=" + testcaseId +
               ", duplicateOf=" + duplicateOf +
               ", securityFlag=" + securityFlag +
               ", retries=" + retries +
               '}';
    }
}