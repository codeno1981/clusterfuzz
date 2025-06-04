package com.google.clusterfuzz.datastore.model;

import com.google.clusterfuzz.search.SearchTokenizer;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents a single testcase.
 * Converted from Python Testcase model in data_types.py
 */
@Entity
@Table(name = "Testcase", indexes = {
    @Index(name = "idx_testcase_crash_type", columnList = "crash_type"),
    @Index(name = "idx_testcase_security_flag", columnList = "security_flag"),
    @Index(name = "idx_testcase_fuzzer_name", columnList = "fuzzer_name"),
    @Index(name = "idx_testcase_job_type", columnList = "job_type"),
    @Index(name = "idx_testcase_status", columnList = "status"),
    @Index(name = "idx_testcase_open", columnList = "open"),
    @Index(name = "idx_testcase_project_name", columnList = "project_name")
})
public class Testcase extends BaseModel {

    // Crash information
    @Column(name = "crash_type")
    private String crashType;

    @Column(name = "crash_address", columnDefinition = "TEXT")
    private String crashAddress;

    @Column(name = "crash_state")
    private String crashState;

    @Column(name = "crash_stacktrace", columnDefinition = "TEXT")
    private String crashStacktrace;

    @Column(name = "last_tested_crash_stacktrace", columnDefinition = "TEXT")
    private String lastTestedCrashStacktrace;

    // Blobstore keys for various artifacts
    @Column(name = "fuzzed_keys", columnDefinition = "TEXT")
    private String fuzzedKeys;

    @Column(name = "minimized_keys", columnDefinition = "TEXT")
    private String minimizedKeys;

    @Column(name = "minidump_keys", columnDefinition = "TEXT")
    private String minidumpKeys;

    // Bug tracking information
    @Column(name = "bug_information")
    private String bugInformation;

    @Column(name = "regression", columnDefinition = "TEXT")
    private String regression = "";

    @Column(name = "fixed", columnDefinition = "TEXT")
    private String fixed = "";

    // Security information
    @Column(name = "security_flag")
    private Boolean securityFlag = false;

    @Column(name = "security_severity")
    @Enumerated(EnumType.ORDINAL)
    private SecuritySeverity securitySeverity;

    // Crash characteristics
    @Column(name = "one_time_crasher_flag")
    private Boolean oneTimeCrasherFlag = false;

    @Column(name = "flaky_stack")
    private Boolean flakyStack = false;

    // Additional information
    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments = "";

    @Column(name = "crash_revision")
    private Integer crashRevision;

    @Column(name = "absolute_path", columnDefinition = "TEXT")
    private String absolutePath;

    @Column(name = "minimized_arguments", columnDefinition = "TEXT")
    private String minimizedArguments = "";

    @Column(name = "window_argument", columnDefinition = "TEXT")
    private String windowArgument = "";

    // Job and fuzzer information
    @Column(name = "job_type")
    private String jobType;

    @Column(name = "queue", columnDefinition = "TEXT")
    private String queue;

    @Column(name = "fuzzer_name")
    private String fuzzerName;

    @Column(name = "overridden_fuzzer_name")
    private String overriddenFuzzerName;

    // Archive information
    @Column(name = "archive_state")
    private Integer archiveState = 0;

    @Column(name = "archive_filename", columnDefinition = "TEXT")
    private String archiveFilename;

    // Timestamps
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "created")
    private LocalDateTime created;

    // Flags and status
    @Column(name = "http_flag")
    private Boolean httpFlag = false;

    @Column(name = "status")
    private String status = "Processed";

    @Column(name = "duplicate_of")
    private Long duplicateOf;

    @Column(name = "symbolized")
    private Boolean symbolized = false;

    @Column(name = "open")
    private Boolean open = true;

    @Column(name = "triaged")
    private Boolean triaged = false;

    // Group information
    @Column(name = "group_id")
    private Integer groupId = 0;

    @Column(name = "group_bug_information")
    private Integer groupBugInformation = 0;

    @Column(name = "is_leader")
    private Boolean isLeader = false;

    @Column(name = "is_a_duplicate_flag")
    private Boolean isADuplicateFlag;

    // Platform information
    @Column(name = "platform")
    private String platform;

    @Column(name = "platform_id")
    private String platformId;

    // Project information
    @Column(name = "project_name")
    private String projectName;

    // Gestures and interaction
    @ElementCollection
    @CollectionTable(name = "testcase_gestures", joinColumns = @JoinColumn(name = "testcase_id"))
    @Column(name = "gesture")
    private List<String> gestures = new ArrayList<>();

    // Configuration
    @Column(name = "redzone")
    private Integer redzone = 128;

    @Column(name = "disable_ubsan")
    private Boolean disableUbsan = false;

    @Column(name = "timeout_multiplier")
    private Double timeoutMultiplier = 1.0;

    // Search and indexing
    @ElementCollection
    @CollectionTable(name = "testcase_keywords", joinColumns = @JoinColumn(name = "testcase_id"))
    @Column(name = "keyword")
    private Set<String> keywords = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "testcase_bug_indices", joinColumns = @JoinColumn(name = "testcase_id"))
    @Column(name = "bug_index")
    private Set<String> bugIndices = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "testcase_impact_indices", joinColumns = @JoinColumn(name = "testcase_id"))
    @Column(name = "impact_index")
    private Set<String> impactIndices = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "testcase_fuzzer_name_indices", joinColumns = @JoinColumn(name = "testcase_id"))
    @Column(name = "fuzzer_name_index")
    private Set<String> fuzzerNameIndices = new HashSet<>();

    // Bug tracking flags
    @Column(name = "has_bug_flag")
    private Boolean hasBugFlag;

    // Impact version tracking
    @ElementCollection
    @CollectionTable(name = "testcase_impact_version_indices", joinColumns = @JoinColumn(name = "testcase_id"))
    @Column(name = "impact_version_index")
    private Set<String> impactVersionIndices = new HashSet<>();

    @Column(name = "impact_extended_stable_version")
    private String impactExtendedStableVersion;

    @ElementCollection
    @CollectionTable(name = "testcase_impact_extended_stable_version_indices", joinColumns = @JoinColumn(name = "testcase_id"))
    @Column(name = "impact_extended_stable_version_index")
    private Set<String> impactExtendedStableVersionIndices = new HashSet<>();

    @Column(name = "impact_extended_stable_version_likely")
    private Boolean impactExtendedStableVersionLikely;

    @Column(name = "impact_stable_version")
    private String impactStableVersion;

    @ElementCollection
    @CollectionTable(name = "testcase_impact_stable_version_indices", joinColumns = @JoinColumn(name = "testcase_id"))
    @Column(name = "impact_stable_version_index")
    private Set<String> impactStableVersionIndices = new HashSet<>();

    @Column(name = "impact_stable_version_likely")
    private Boolean impactStableVersionLikely;

    @Column(name = "impact_beta_version")
    private String impactBetaVersion;

    @ElementCollection
    @CollectionTable(name = "testcase_impact_beta_version_indices", joinColumns = @JoinColumn(name = "testcase_id"))
    @Column(name = "impact_beta_version_index")
    private Set<String> impactBetaVersionIndices = new HashSet<>();

    @Column(name = "impact_beta_version_likely")
    private Boolean impactBetaVersionLikely;

    @Column(name = "impact_head_version")
    private String impactHeadVersion;

    @ElementCollection
    @CollectionTable(name = "testcase_impact_head_version_indices", joinColumns = @JoinColumn(name = "testcase_id"))
    @Column(name = "impact_head_version_index")
    private Set<String> impactHeadVersionIndices = new HashSet<>();

    @Column(name = "impact_head_version_likely")
    private Boolean impactHeadVersionLikely;

    @Column(name = "is_impact_set_flag")
    private Boolean isImpactSetFlag;

    // Upload and trust information
    @Column(name = "uploader_email")
    private String uploaderEmail;

    @Column(name = "trusted")
    private Boolean trusted = false;

    // GitHub integration
    @Column(name = "github_repo_id")
    private Integer githubRepoId;

    @Column(name = "github_issue_num")
    private Integer githubIssueNum;

    // Triage tracking
    @Column(name = "stuck_in_triage")
    private Boolean stuckInTriage = false;

    @Column(name = "analyze_pending")
    private Boolean analyzePending = false;

    // Additional metadata as JSON
    @Column(name = "additional_metadata", columnDefinition = "TEXT")
    private String additionalMetadata;

    // Default constructor
    public Testcase() {
        super();
        this.timestamp = LocalDateTime.now();
        this.created = LocalDateTime.now();
    }

    // Business logic methods

    /**
     * Check if this testcase is from a Chromium project.
     */
    public boolean isChromium() {
        return "chromium".equals(projectName) || "chromium-testing".equals(projectName);
    }

    /**
     * Check if this testcase has blame information available.
     */
    public boolean hasBlame() {
        return isChromium();
    }

    /**
     * Check if this testcase has impact information available.
     */
    public boolean hasImpacts() {
        return isChromium() && !Boolean.TRUE.equals(oneTimeCrasherFlag);
    }

    /**
     * Check if this testcase is a security issue.
     */
    public boolean isSecurityIssue() {
        return Boolean.TRUE.equals(securityFlag) && 
               securitySeverity != null && 
               securitySeverity != SecuritySeverity.MISSING;
    }

    /**
     * Get the effective fuzzer name (overridden or original).
     */
    public String getEffectiveFuzzerName() {
        return overriddenFuzzerName != null ? overriddenFuzzerName : fuzzerName;
    }

    /**
     * Check if this testcase has an associated bug.
     */
    public boolean hasBug() {
        return (bugInformation != null && !bugInformation.isEmpty()) ||
               (groupBugInformation != null && groupBugInformation > 0);
    }

    /**
     * Populate search indices for this testcase.
     */
    public void populateIndices() {
        Set<String> allKeywords = new HashSet<>();
        
        // Add crash type keywords
        if (crashType != null) {
            allKeywords.addAll(SearchTokenizer.tokenize(crashType));
        }
        
        // Add fuzzer name keywords
        if (fuzzerName != null) {
            allKeywords.addAll(SearchTokenizer.tokenize(fuzzerName));
        }
        
        // Add project name keywords
        if (projectName != null) {
            allKeywords.addAll(SearchTokenizer.tokenize(projectName));
        }
        
        this.keywords = allKeywords;
        
        // Update bug flag
        this.hasBugFlag = hasBug();
        
        // Update duplicate flag
        this.isADuplicateFlag = (duplicateOf != null);
    }

    /**
     * Pre-persist hook to populate indices.
     */
    @PrePersist
    @PreUpdate
    public void prePersist() {
        populateIndices();
        
        // Set created timestamp if not set
        if (created == null) {
            created = LocalDateTime.now();
        }
        
        // Set timestamp if not set
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    // Getters and Setters (abbreviated for space - in real implementation, include all)
    
    public String getCrashType() {
        return crashType;
    }

    public void setCrashType(String crashType) {
        this.crashType = crashType;
    }

    public String getCrashAddress() {
        return crashAddress;
    }

    public void setCrashAddress(String crashAddress) {
        this.crashAddress = crashAddress;
    }

    public String getCrashState() {
        return crashState;
    }

    public void setCrashState(String crashState) {
        this.crashState = crashState;
    }

    public String getCrashStacktrace() {
        return crashStacktrace;
    }

    public void setCrashStacktrace(String crashStacktrace) {
        this.crashStacktrace = crashStacktrace;
    }

    public Boolean getSecurityFlag() {
        return securityFlag;
    }

    public void setSecurityFlag(Boolean securityFlag) {
        this.securityFlag = securityFlag;
    }

    public SecuritySeverity getSecuritySeverity() {
        return securitySeverity;
    }

    public void setSecuritySeverity(SecuritySeverity securitySeverity) {
        this.securitySeverity = securitySeverity;
    }

    public String getFuzzerName() {
        return fuzzerName;
    }

    public void setFuzzerName(String fuzzerName) {
        this.fuzzerName = fuzzerName;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getOpen() {
        return open;
    }

    public void setOpen(Boolean open) {
        this.open = open;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    // ... (additional getters/setters would be included in full implementation)

    @Override
    public String toString() {
        return "Testcase{" +
                "id=" + getId() +
                ", crashType='" + crashType + '\'' +
                ", fuzzerName='" + fuzzerName + '\'' +
                ", jobType='" + jobType + '\'' +
                ", status='" + status + '\'' +
                ", securityFlag=" + securityFlag +
                ", open=" + open +
                '}';
    }
}