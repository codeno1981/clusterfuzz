package com.google.clusterfuzz.datastore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing build metadata for fuzzing targets.
 * Tracks build information, artifacts, and compilation details.
 */
@Entity
@Table(name = "build_metadata", 
       indexes = {
           @Index(name = "idx_build_job_type", columnList = "job_type"),
           @Index(name = "idx_build_revision", columnList = "revision"),
           @Index(name = "idx_build_platform", columnList = "platform"),
           @Index(name = "idx_build_timestamp", columnList = "build_timestamp"),
           @Index(name = "idx_build_status", columnList = "build_status"),
           @Index(name = "idx_build_is_production", columnList = "is_production_build"),
           @Index(name = "idx_build_symbols_url", columnList = "symbols_url")
       })
public class BuildMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "job_type", nullable = false)
    private String jobType;

    @NotBlank
    @Size(max = 255)
    @Column(name = "revision", nullable = false)
    private String revision;

    @NotBlank
    @Size(max = 100)
    @Column(name = "platform", nullable = false)
    private String platform;

    @NotNull
    @Column(name = "build_timestamp", nullable = false)
    private LocalDateTime buildTimestamp;

    // Build identification
    @Size(max = 255)
    @Column(name = "build_id")
    private String buildId;

    @Size(max = 255)
    @Column(name = "build_tag")
    private String buildTag;

    @Size(max = 255)
    @Column(name = "build_version")
    private String buildVersion;

    @Size(max = 100)
    @Column(name = "build_status")
    private String buildStatus; // SUCCESS, FAILED, IN_PROGRESS, CANCELLED

    // Repository information
    @Size(max = 500)
    @Column(name = "repository_url")
    private String repositoryUrl;

    @Size(max = 255)
    @Column(name = "branch_name")
    private String branchName;

    @Size(max = 255)
    @Column(name = "commit_hash")
    private String commitHash;

    @Size(max = 255)
    @Column(name = "commit_author")
    private String commitAuthor;

    @Size(max = 1000)
    @Column(name = "commit_message", length = 1000)
    private String commitMessage;

    @Column(name = "commit_timestamp")
    private LocalDateTime commitTimestamp;

    // Build configuration
    @Size(max = 255)
    @Column(name = "compiler_version")
    private String compilerVersion;

    @Size(max = 255)
    @Column(name = "build_type")
    private String buildType; // DEBUG, RELEASE, ASAN, MSAN, UBSAN, etc.

    @Size(max = 1000)
    @Column(name = "build_flags", length = 1000)
    private String buildFlags;

    @Size(max = 1000)
    @Column(name = "sanitizer_options", length = 1000)
    private String sanitizerOptions;

    @Column(name = "is_production_build")
    private Boolean isProductionBuild = false;

    @Column(name = "is_debug_build")
    private Boolean isDebugBuild = false;

    @Column(name = "has_symbols")
    private Boolean hasSymbols = false;

    // Artifact URLs and paths
    @Size(max = 1000)
    @Column(name = "binary_url", length = 1000)
    private String binaryUrl;

    @Size(max = 1000)
    @Column(name = "symbols_url", length = 1000)
    private String symbolsUrl;

    @Size(max = 1000)
    @Column(name = "source_archive_url", length = 1000)
    private String sourceArchiveUrl;

    @Size(max = 1000)
    @Column(name = "build_log_url", length = 1000)
    private String buildLogUrl;

    @Size(max = 1000)
    @Column(name = "test_results_url", length = 1000)
    private String testResultsUrl;

    // Build metrics
    @Column(name = "build_duration_seconds")
    private Integer buildDurationSeconds;

    @Column(name = "binary_size_bytes")
    private Long binarySizeBytes;

    @Column(name = "symbols_size_bytes")
    private Long symbolsSizeBytes;

    @Column(name = "test_count")
    private Integer testCount;

    @Column(name = "test_passed_count")
    private Integer testPassedCount;

    @Column(name = "test_failed_count")
    private Integer testFailedCount;

    // Quality and validation
    @Column(name = "has_build_warnings")
    private Boolean hasBuildWarnings = false;

    @Column(name = "warning_count")
    private Integer warningCount;

    @Size(max = 2000)
    @Column(name = "build_warnings", length = 2000)
    private String buildWarnings;

    @Column(name = "has_build_errors")
    private Boolean hasBuildErrors = false;

    @Size(max = 2000)
    @Column(name = "build_errors", length = 2000)
    private String buildErrors;

    @Column(name = "is_validated")
    private Boolean isValidated = false;

    @Size(max = 1000)
    @Column(name = "validation_notes", length = 1000)
    private String validationNotes;

    // Dependencies and environment
    @Size(max = 2000)
    @Column(name = "dependencies", length = 2000)
    private String dependencies;

    @Size(max = 1000)
    @Column(name = "build_environment", length = 1000)
    private String buildEnvironment;

    @Size(max = 255)
    @Column(name = "docker_image")
    private String dockerImage;

    @Size(max = 255)
    @Column(name = "build_machine")
    private String buildMachine;

    // Archival and lifecycle
    @Column(name = "is_archived")
    private Boolean isArchived = false;

    @Column(name = "archive_timestamp")
    private LocalDateTime archiveTimestamp;

    @Column(name = "retention_days")
    private Integer retentionDays;

    @Column(name = "is_expired")
    private Boolean isExpired = false;

    // Usage tracking
    @Column(name = "download_count")
    private Integer downloadCount = 0;

    @Column(name = "last_used_timestamp")
    private LocalDateTime lastUsedTimestamp;

    @Size(max = 1000)
    @Column(name = "usage_notes", length = 1000)
    private String usageNotes;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public BuildMetadata() {}

    public BuildMetadata(String jobType, String revision, String platform, LocalDateTime buildTimestamp) {
        this.jobType = jobType;
        this.revision = revision;
        this.platform = platform;
        this.buildTimestamp = buildTimestamp;
    }

    // Business methods
    public boolean isSuccessfulBuild() {
        return "SUCCESS".equals(buildStatus);
    }

    public boolean isFailedBuild() {
        return "FAILED".equals(buildStatus);
    }

    public boolean isInProgress() {
        return "IN_PROGRESS".equals(buildStatus);
    }

    public boolean isRecentBuild(int daysThreshold) {
        return buildTimestamp != null && buildTimestamp.isAfter(LocalDateTime.now().minusDays(daysThreshold));
    }

    public boolean hasQualityIssues() {
        return Boolean.TRUE.equals(hasBuildWarnings) || Boolean.TRUE.equals(hasBuildErrors);
    }

    public void markAsArchived() {
        this.isArchived = true;
        this.archiveTimestamp = LocalDateTime.now();
    }

    public void markAsExpired() {
        this.isExpired = true;
    }

    public void recordDownload() {
        this.downloadCount = (this.downloadCount == null ? 0 : this.downloadCount) + 1;
        this.lastUsedTimestamp = LocalDateTime.now();
    }

    public void markAsValidated(String notes) {
        this.isValidated = true;
        this.validationNotes = notes;
    }

    public void addBuildWarning(String warning) {
        this.hasBuildWarnings = true;
        this.warningCount = (this.warningCount == null ? 0 : this.warningCount) + 1;
        if (this.buildWarnings == null) {
            this.buildWarnings = warning;
        } else {
            this.buildWarnings += "; " + warning;
        }
    }

    public void addBuildError(String error) {
        this.hasBuildErrors = true;
        if (this.buildErrors == null) {
            this.buildErrors = error;
        } else {
            this.buildErrors += "; " + error;
        }
    }

    public double getTestSuccessRate() {
        if (testCount == null || testCount == 0) return 0.0;
        int passed = testPassedCount != null ? testPassedCount : 0;
        return (double) passed / testCount * 100.0;
    }

    public boolean shouldBeRetained() {
        if (retentionDays == null) return true;
        return buildTimestamp != null && 
               buildTimestamp.isAfter(LocalDateTime.now().minusDays(retentionDays));
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public String getRevision() { return revision; }
    public void setRevision(String revision) { this.revision = revision; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public LocalDateTime getBuildTimestamp() { return buildTimestamp; }
    public void setBuildTimestamp(LocalDateTime buildTimestamp) { this.buildTimestamp = buildTimestamp; }

    public String getBuildId() { return buildId; }
    public void setBuildId(String buildId) { this.buildId = buildId; }

    public String getBuildTag() { return buildTag; }
    public void setBuildTag(String buildTag) { this.buildTag = buildTag; }

    public String getBuildVersion() { return buildVersion; }
    public void setBuildVersion(String buildVersion) { this.buildVersion = buildVersion; }

    public String getBuildStatus() { return buildStatus; }
    public void setBuildStatus(String buildStatus) { this.buildStatus = buildStatus; }

    public String getRepositoryUrl() { return repositoryUrl; }
    public void setRepositoryUrl(String repositoryUrl) { this.repositoryUrl = repositoryUrl; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public String getCommitHash() { return commitHash; }
    public void setCommitHash(String commitHash) { this.commitHash = commitHash; }

    public String getCommitAuthor() { return commitAuthor; }
    public void setCommitAuthor(String commitAuthor) { this.commitAuthor = commitAuthor; }

    public String getCommitMessage() { return commitMessage; }
    public void setCommitMessage(String commitMessage) { this.commitMessage = commitMessage; }

    public LocalDateTime getCommitTimestamp() { return commitTimestamp; }
    public void setCommitTimestamp(LocalDateTime commitTimestamp) { this.commitTimestamp = commitTimestamp; }

    public String getCompilerVersion() { return compilerVersion; }
    public void setCompilerVersion(String compilerVersion) { this.compilerVersion = compilerVersion; }

    public String getBuildType() { return buildType; }
    public void setBuildType(String buildType) { this.buildType = buildType; }

    public String getBuildFlags() { return buildFlags; }
    public void setBuildFlags(String buildFlags) { this.buildFlags = buildFlags; }

    public String getSanitizerOptions() { return sanitizerOptions; }
    public void setSanitizerOptions(String sanitizerOptions) { this.sanitizerOptions = sanitizerOptions; }

    public Boolean getIsProductionBuild() { return isProductionBuild; }
    public void setIsProductionBuild(Boolean isProductionBuild) { this.isProductionBuild = isProductionBuild; }

    public Boolean getIsDebugBuild() { return isDebugBuild; }
    public void setIsDebugBuild(Boolean isDebugBuild) { this.isDebugBuild = isDebugBuild; }

    public Boolean getHasSymbols() { return hasSymbols; }
    public void setHasSymbols(Boolean hasSymbols) { this.hasSymbols = hasSymbols; }

    public String getBinaryUrl() { return binaryUrl; }
    public void setBinaryUrl(String binaryUrl) { this.binaryUrl = binaryUrl; }

    public String getSymbolsUrl() { return symbolsUrl; }
    public void setSymbolsUrl(String symbolsUrl) { this.symbolsUrl = symbolsUrl; }

    public String getSourceArchiveUrl() { return sourceArchiveUrl; }
    public void setSourceArchiveUrl(String sourceArchiveUrl) { this.sourceArchiveUrl = sourceArchiveUrl; }

    public String getBuildLogUrl() { return buildLogUrl; }
    public void setBuildLogUrl(String buildLogUrl) { this.buildLogUrl = buildLogUrl; }

    public String getTestResultsUrl() { return testResultsUrl; }
    public void setTestResultsUrl(String testResultsUrl) { this.testResultsUrl = testResultsUrl; }

    public Integer getBuildDurationSeconds() { return buildDurationSeconds; }
    public void setBuildDurationSeconds(Integer buildDurationSeconds) { this.buildDurationSeconds = buildDurationSeconds; }

    public Long getBinarySizeBytes() { return binarySizeBytes; }
    public void setBinarySizeBytes(Long binarySizeBytes) { this.binarySizeBytes = binarySizeBytes; }

    public Long getSymbolsSizeBytes() { return symbolsSizeBytes; }
    public void setSymbolsSizeBytes(Long symbolsSizeBytes) { this.symbolsSizeBytes = symbolsSizeBytes; }

    public Integer getTestCount() { return testCount; }
    public void setTestCount(Integer testCount) { this.testCount = testCount; }

    public Integer getTestPassedCount() { return testPassedCount; }
    public void setTestPassedCount(Integer testPassedCount) { this.testPassedCount = testPassedCount; }

    public Integer getTestFailedCount() { return testFailedCount; }
    public void setTestFailedCount(Integer testFailedCount) { this.testFailedCount = testFailedCount; }

    public Boolean getHasBuildWarnings() { return hasBuildWarnings; }
    public void setHasBuildWarnings(Boolean hasBuildWarnings) { this.hasBuildWarnings = hasBuildWarnings; }

    public Integer getWarningCount() { return warningCount; }
    public void setWarningCount(Integer warningCount) { this.warningCount = warningCount; }

    public String getBuildWarnings() { return buildWarnings; }
    public void setBuildWarnings(String buildWarnings) { this.buildWarnings = buildWarnings; }

    public Boolean getHasBuildErrors() { return hasBuildErrors; }
    public void setHasBuildErrors(Boolean hasBuildErrors) { this.hasBuildErrors = hasBuildErrors; }

    public String getBuildErrors() { return buildErrors; }
    public void setBuildErrors(String buildErrors) { this.buildErrors = buildErrors; }

    public Boolean getIsValidated() { return isValidated; }
    public void setIsValidated(Boolean isValidated) { this.isValidated = isValidated; }

    public String getValidationNotes() { return validationNotes; }
    public void setValidationNotes(String validationNotes) { this.validationNotes = validationNotes; }

    public String getDependencies() { return dependencies; }
    public void setDependencies(String dependencies) { this.dependencies = dependencies; }

    public String getBuildEnvironment() { return buildEnvironment; }
    public void setBuildEnvironment(String buildEnvironment) { this.buildEnvironment = buildEnvironment; }

    public String getDockerImage() { return dockerImage; }
    public void setDockerImage(String dockerImage) { this.dockerImage = dockerImage; }

    public String getBuildMachine() { return buildMachine; }
    public void setBuildMachine(String buildMachine) { this.buildMachine = buildMachine; }

    public Boolean getIsArchived() { return isArchived; }
    public void setIsArchived(Boolean isArchived) { this.isArchived = isArchived; }

    public LocalDateTime getArchiveTimestamp() { return archiveTimestamp; }
    public void setArchiveTimestamp(LocalDateTime archiveTimestamp) { this.archiveTimestamp = archiveTimestamp; }

    public Integer getRetentionDays() { return retentionDays; }
    public void setRetentionDays(Integer retentionDays) { this.retentionDays = retentionDays; }

    public Boolean getIsExpired() { return isExpired; }
    public void setIsExpired(Boolean isExpired) { this.isExpired = isExpired; }

    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }

    public LocalDateTime getLastUsedTimestamp() { return lastUsedTimestamp; }
    public void setLastUsedTimestamp(LocalDateTime lastUsedTimestamp) { this.lastUsedTimestamp = lastUsedTimestamp; }

    public String getUsageNotes() { return usageNotes; }
    public void setUsageNotes(String usageNotes) { this.usageNotes = usageNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuildMetadata that = (BuildMetadata) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(jobType, that.jobType) &&
               Objects.equals(revision, that.revision) &&
               Objects.equals(platform, that.platform) &&
               Objects.equals(buildTimestamp, that.buildTimestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, jobType, revision, platform, buildTimestamp);
    }

    @Override
    public String toString() {
        return "BuildMetadata{" +
               "id=" + id +
               ", jobType='" + jobType + '\'' +
               ", revision='" + revision + '\'' +
               ", platform='" + platform + '\'' +
               ", buildStatus='" + buildStatus + '\'' +
               ", buildTimestamp=" + buildTimestamp +
               '}';
    }
}