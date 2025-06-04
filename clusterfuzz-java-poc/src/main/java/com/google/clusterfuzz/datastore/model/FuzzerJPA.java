package com.google.clusterfuzz.datastore.model;

import com.google.clusterfuzz.search.SearchTokenizer;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.*;

/**
 * JPA-based Fuzzer entity.
 * Represents a fuzzer with full JPA annotations for relational database storage.
 * Converted from Python Fuzzer model in data_types.py
 */
@Entity
@Table(name = "fuzzer", indexes = {
    @Index(name = "idx_fuzzer_name", columnList = "name"),
    @Index(name = "idx_fuzzer_builtin", columnList = "builtin"),
    @Index(name = "idx_fuzzer_differential", columnList = "differential")
})
public class FuzzerJPA extends BaseModel {

    // Valid name regex - allows alphanumeric, underscore, hyphen, dot, and @
    public static final String VALID_NAME_PATTERN = "^[a-zA-Z0-9_@.-]+$";
    
    // List of builtin fuzzers
    public static final Set<String> BUILTIN_FUZZERS = Set.of("afl", "libFuzzer");

    @Column(name = "name", nullable = false, unique = true)
    @Pattern(regexp = VALID_NAME_PATTERN, message = "Fuzzer name contains invalid characters")
    private String name;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "filename")
    private String filename;

    @Column(name = "blobstore_key")
    private String blobstoreKey;

    @Column(name = "file_size")
    private String fileSize;

    @Column(name = "executable_path")
    private String executablePath;

    @Column(name = "revision")
    private Integer revision;

    @Column(name = "source")
    private String source;

    @Column(name = "timeout")
    private Integer timeout;

    @Column(name = "supported_platforms")
    private String supportedPlatforms;

    @Column(name = "launcher_script")
    private String launcherScript;

    @Column(name = "result")
    private String result;

    @Column(name = "result_timestamp")
    private LocalDateTime resultTimestamp;

    @Column(name = "console_output", columnDefinition = "TEXT")
    private String consoleOutput;

    @Column(name = "return_code")
    private Integer returnCode;

    @Column(name = "sample_testcase")
    private String sampleTestcase;

    // Job types for this fuzzer (stored as collection)
    @ElementCollection
    @CollectionTable(name = "fuzzer_jobs", joinColumns = @JoinColumn(name = "fuzzer_id"))
    @Column(name = "job_type")
    private Set<String> jobs = new HashSet<>();

    @Column(name = "external_contribution")
    private Boolean externalContribution = false;

    @Column(name = "max_testcases")
    private Integer maxTestcases;

    @Column(name = "untrusted_content")
    private Boolean untrustedContent = false;

    @Column(name = "data_bundle_name")
    private String dataBundleName = "";

    @Column(name = "additional_environment_string", columnDefinition = "TEXT")
    private String additionalEnvironmentString;

    @Column(name = "stats_columns", columnDefinition = "TEXT")
    private String statsColumns;

    @Column(name = "stats_column_descriptions", columnDefinition = "TEXT")
    private String statsColumnDescriptions;

    @Column(name = "builtin")
    private Boolean builtin = false;

    @Column(name = "differential")
    private Boolean differential = false;

    @Column(name = "has_large_testcases")
    private Boolean hasLargeTestcases = false;

    // Search keywords for indexing
    @ElementCollection
    @CollectionTable(name = "fuzzer_keywords", joinColumns = @JoinColumn(name = "fuzzer_id"))
    @Column(name = "keyword")
    private Set<String> keywords = new HashSet<>();

    // Default constructor
    public FuzzerJPA() {
        super();
        this.timestamp = LocalDateTime.now();
    }

    // Constructor with name
    public FuzzerJPA(String name) {
        this();
        this.name = name;
        this.builtin = BUILTIN_FUZZERS.contains(name);
    }

    /**
     * Check if this fuzzer is a builtin fuzzer.
     */
    public boolean isBuiltinFuzzer() {
        return Boolean.TRUE.equals(builtin) || BUILTIN_FUZZERS.contains(name);
    }

    /**
     * Get supported platforms as a list.
     */
    public List<String> getSupportedPlatformsList() {
        if (supportedPlatforms == null || supportedPlatforms.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(supportedPlatforms.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /**
     * Set supported platforms from a list.
     */
    public void setSupportedPlatformsList(List<String> platforms) {
        if (platforms == null || platforms.isEmpty()) {
            this.supportedPlatforms = "";
        } else {
            this.supportedPlatforms = String.join(",", platforms);
        }
    }

    /**
     * Check if this fuzzer supports a specific platform.
     */
    public boolean supportsPlatform(String platform) {
        return getSupportedPlatformsList().contains(platform);
    }

    /**
     * Get additional environment variables as a Map.
     */
    public Map<String, String> getAdditionalEnvironment() {
        if (additionalEnvironmentString == null || additionalEnvironmentString.trim().isEmpty()) {
            return new HashMap<>();
        }
        
        Map<String, String> env = new HashMap<>();
        String[] lines = additionalEnvironmentString.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            
            int equalIndex = line.indexOf('=');
            if (equalIndex > 0) {
                String key = line.substring(0, equalIndex).trim();
                String value = line.substring(equalIndex + 1).trim();
                env.put(key, value);
            }
        }
        
        return env;
    }

    /**
     * Populate search keywords for this fuzzer.
     */
    public void populateKeywords() {
        Set<String> allKeywords = new HashSet<>();
        
        if (name != null) {
            allKeywords.addAll(SearchTokenizer.tokenize(name));
        }
        
        if (source != null) {
            allKeywords.addAll(SearchTokenizer.tokenize(source));
        }
        
        // Add job types as keywords
        if (jobs != null) {
            for (String job : jobs) {
                allKeywords.addAll(SearchTokenizer.tokenize(job));
            }
        }
        
        this.keywords = allKeywords;
    }

    /**
     * Pre-persist hook to set builtin flag and populate keywords.
     */
    @PrePersist
    @PreUpdate
    public void prePersist() {
        // Set builtin flag if this is a builtin fuzzer
        if (name != null && BUILTIN_FUZZERS.contains(name)) {
            this.builtin = true;
        }
        
        // Update timestamp
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        
        // Populate search keywords
        populateKeywords();
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getBlobstoreKey() {
        return blobstoreKey;
    }

    public void setBlobstoreKey(String blobstoreKey) {
        this.blobstoreKey = blobstoreKey;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getSupportedPlatforms() {
        return supportedPlatforms;
    }

    public void setSupportedPlatforms(String supportedPlatforms) {
        this.supportedPlatforms = supportedPlatforms;
    }

    public String getLauncherScript() {
        return launcherScript;
    }

    public void setLauncherScript(String launcherScript) {
        this.launcherScript = launcherScript;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public LocalDateTime getResultTimestamp() {
        return resultTimestamp;
    }

    public void setResultTimestamp(LocalDateTime resultTimestamp) {
        this.resultTimestamp = resultTimestamp;
    }

    public String getConsoleOutput() {
        return consoleOutput;
    }

    public void setConsoleOutput(String consoleOutput) {
        this.consoleOutput = consoleOutput;
    }

    public Integer getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(Integer returnCode) {
        this.returnCode = returnCode;
    }

    public String getSampleTestcase() {
        return sampleTestcase;
    }

    public void setSampleTestcase(String sampleTestcase) {
        this.sampleTestcase = sampleTestcase;
    }

    public Set<String> getJobs() {
        return jobs;
    }

    public void setJobs(Set<String> jobs) {
        this.jobs = jobs;
    }

    public Boolean getExternalContribution() {
        return externalContribution;
    }

    public void setExternalContribution(Boolean externalContribution) {
        this.externalContribution = externalContribution;
    }

    public Integer getMaxTestcases() {
        return maxTestcases;
    }

    public void setMaxTestcases(Integer maxTestcases) {
        this.maxTestcases = maxTestcases;
    }

    public Boolean getUntrustedContent() {
        return untrustedContent;
    }

    public void setUntrustedContent(Boolean untrustedContent) {
        this.untrustedContent = untrustedContent;
    }

    public String getDataBundleName() {
        return dataBundleName;
    }

    public void setDataBundleName(String dataBundleName) {
        this.dataBundleName = dataBundleName;
    }

    public String getAdditionalEnvironmentString() {
        return additionalEnvironmentString;
    }

    public void setAdditionalEnvironmentString(String additionalEnvironmentString) {
        this.additionalEnvironmentString = additionalEnvironmentString;
    }

    public String getStatsColumns() {
        return statsColumns;
    }

    public void setStatsColumns(String statsColumns) {
        this.statsColumns = statsColumns;
    }

    public String getStatsColumnDescriptions() {
        return statsColumnDescriptions;
    }

    public void setStatsColumnDescriptions(String statsColumnDescriptions) {
        this.statsColumnDescriptions = statsColumnDescriptions;
    }

    public Boolean getBuiltin() {
        return builtin;
    }

    public void setBuiltin(Boolean builtin) {
        this.builtin = builtin;
    }

    public Boolean getDifferential() {
        return differential;
    }

    public void setDifferential(Boolean differential) {
        this.differential = differential;
    }

    public Boolean getHasLargeTestcases() {
        return hasLargeTestcases;
    }

    public void setHasLargeTestcases(Boolean hasLargeTestcases) {
        this.hasLargeTestcases = hasLargeTestcases;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(Set<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public String toString() {
        return "FuzzerJPA{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", revision=" + revision +
                ", builtin=" + builtin +
                ", differential=" + differential +
                ", jobCount=" + (jobs != null ? jobs.size() : 0) +
                '}';
    }
}