package com.google.clusterfuzz.datastore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing code coverage information for fuzzing targets.
 * Tracks coverage metrics, file-level coverage data, and analysis results.
 */
@Entity
@Table(name = "coverage_information", 
       indexes = {
           @Index(name = "idx_coverage_fuzzer_date", columnList = "fuzzer, date"),
           @Index(name = "idx_coverage_job_type", columnList = "job_type"),
           @Index(name = "idx_coverage_platform", columnList = "platform"),
           @Index(name = "idx_coverage_date", columnList = "date"),
           @Index(name = "idx_coverage_functions_covered", columnList = "functions_covered"),
           @Index(name = "idx_coverage_lines_covered", columnList = "lines_covered"),
           @Index(name = "idx_coverage_edges_covered", columnList = "edges_covered")
       })
public class CoverageInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "fuzzer", nullable = false)
    private String fuzzer;

    @NotBlank
    @Size(max = 255)
    @Column(name = "job_type", nullable = false)
    private String jobType;

    @NotBlank
    @Size(max = 100)
    @Column(name = "platform", nullable = false)
    private String platform;

    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    // Coverage metrics
    @Min(0)
    @Column(name = "functions_covered")
    private Integer functionsCovered;

    @Min(0)
    @Column(name = "functions_total")
    private Integer functionsTotal;

    @Min(0)
    @Column(name = "lines_covered")
    private Integer linesCovered;

    @Min(0)
    @Column(name = "lines_total")
    private Integer linesTotal;

    @Min(0)
    @Column(name = "edges_covered")
    private Integer edgesCovered;

    @Min(0)
    @Column(name = "edges_total")
    private Integer edgesTotal;

    @Min(0)
    @Column(name = "branches_covered")
    private Integer branchesCovered;

    @Min(0)
    @Column(name = "branches_total")
    private Integer branchesTotal;

    // Coverage percentages (calculated fields)
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Column(name = "function_coverage_percentage", precision = 5, scale = 2)
    private Double functionCoveragePercentage;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Column(name = "line_coverage_percentage", precision = 5, scale = 2)
    private Double lineCoveragePercentage;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Column(name = "edge_coverage_percentage", precision = 5, scale = 2)
    private Double edgeCoveragePercentage;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Column(name = "branch_coverage_percentage", precision = 5, scale = 2)
    private Double branchCoveragePercentage;

    // File and directory information
    @Size(max = 1000)
    @Column(name = "html_report_url", length = 1000)
    private String htmlReportUrl;

    @Size(max = 1000)
    @Column(name = "report_summary_path", length = 1000)
    private String reportSummaryPath;

    @Size(max = 1000)
    @Column(name = "report_info_path", length = 1000)
    private String reportInfoPath;

    // Build and revision information
    @Size(max = 255)
    @Column(name = "build_id")
    private String buildId;

    @Size(max = 255)
    @Column(name = "revision")
    private String revision;

    @Size(max = 255)
    @Column(name = "repository_url")
    private String repositoryUrl;

    // Analysis metadata
    @Column(name = "is_differential")
    private Boolean isDifferential = false;

    @Size(max = 255)
    @Column(name = "baseline_coverage_id")
    private String baselineCoverageId;

    @Column(name = "analysis_duration_seconds")
    private Integer analysisDurationSeconds;

    @Size(max = 500)
    @Column(name = "analysis_notes", length = 500)
    private String analysisNotes;

    // Quality metrics
    @Column(name = "has_quality_issues")
    private Boolean hasQualityIssues = false;

    @Size(max = 1000)
    @Column(name = "quality_issues", length = 1000)
    private String qualityIssues;

    @Column(name = "is_complete")
    private Boolean isComplete = true;

    @Column(name = "is_archived")
    private Boolean isArchived = false;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public CoverageInformation() {}

    public CoverageInformation(String fuzzer, String jobType, String platform, LocalDateTime date) {
        this.fuzzer = fuzzer;
        this.jobType = jobType;
        this.platform = platform;
        this.date = date;
    }

    // Business methods
    public void calculateCoveragePercentages() {
        if (functionsTotal != null && functionsTotal > 0 && functionsCovered != null) {
            this.functionCoveragePercentage = (double) functionsCovered / functionsTotal * 100.0;
        }
        if (linesTotal != null && linesTotal > 0 && linesCovered != null) {
            this.lineCoveragePercentage = (double) linesCovered / linesTotal * 100.0;
        }
        if (edgesTotal != null && edgesTotal > 0 && edgesCovered != null) {
            this.edgeCoveragePercentage = (double) edgesCovered / edgesTotal * 100.0;
        }
        if (branchesTotal != null && branchesTotal > 0 && branchesCovered != null) {
            this.branchCoveragePercentage = (double) branchesCovered / branchesTotal * 100.0;
        }
    }

    public boolean hasGoodCoverage() {
        return lineCoveragePercentage != null && lineCoveragePercentage >= 70.0;
    }

    public boolean hasExcellentCoverage() {
        return lineCoveragePercentage != null && lineCoveragePercentage >= 90.0;
    }

    public boolean isRecentCoverage(int daysThreshold) {
        return date != null && date.isAfter(LocalDateTime.now().minusDays(daysThreshold));
    }

    public void markAsArchived() {
        this.isArchived = true;
    }

    public void markAsIncomplete(String reason) {
        this.isComplete = false;
        this.analysisNotes = reason;
    }

    public void addQualityIssue(String issue) {
        this.hasQualityIssues = true;
        if (this.qualityIssues == null) {
            this.qualityIssues = issue;
        } else {
            this.qualityIssues += "; " + issue;
        }
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFuzzer() { return fuzzer; }
    public void setFuzzer(String fuzzer) { this.fuzzer = fuzzer; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public Integer getFunctionsCovered() { return functionsCovered; }
    public void setFunctionsCovered(Integer functionsCovered) { 
        this.functionsCovered = functionsCovered;
        calculateCoveragePercentages();
    }

    public Integer getFunctionsTotal() { return functionsTotal; }
    public void setFunctionsTotal(Integer functionsTotal) { 
        this.functionsTotal = functionsTotal;
        calculateCoveragePercentages();
    }

    public Integer getLinesCovered() { return linesCovered; }
    public void setLinesCovered(Integer linesCovered) { 
        this.linesCovered = linesCovered;
        calculateCoveragePercentages();
    }

    public Integer getLinesTotal() { return linesTotal; }
    public void setLinesTotal(Integer linesTotal) { 
        this.linesTotal = linesTotal;
        calculateCoveragePercentages();
    }

    public Integer getEdgesCovered() { return edgesCovered; }
    public void setEdgesCovered(Integer edgesCovered) { 
        this.edgesCovered = edgesCovered;
        calculateCoveragePercentages();
    }

    public Integer getEdgesTotal() { return edgesTotal; }
    public void setEdgesTotal(Integer edgesTotal) { 
        this.edgesTotal = edgesTotal;
        calculateCoveragePercentages();
    }

    public Integer getBranchesCovered() { return branchesCovered; }
    public void setBranchesCovered(Integer branchesCovered) { 
        this.branchesCovered = branchesCovered;
        calculateCoveragePercentages();
    }

    public Integer getBranchesTotal() { return branchesTotal; }
    public void setBranchesTotal(Integer branchesTotal) { 
        this.branchesTotal = branchesTotal;
        calculateCoveragePercentages();
    }

    public Double getFunctionCoveragePercentage() { return functionCoveragePercentage; }
    public void setFunctionCoveragePercentage(Double functionCoveragePercentage) { 
        this.functionCoveragePercentage = functionCoveragePercentage; 
    }

    public Double getLineCoveragePercentage() { return lineCoveragePercentage; }
    public void setLineCoveragePercentage(Double lineCoveragePercentage) { 
        this.lineCoveragePercentage = lineCoveragePercentage; 
    }

    public Double getEdgeCoveragePercentage() { return edgeCoveragePercentage; }
    public void setEdgeCoveragePercentage(Double edgeCoveragePercentage) { 
        this.edgeCoveragePercentage = edgeCoveragePercentage; 
    }

    public Double getBranchCoveragePercentage() { return branchCoveragePercentage; }
    public void setBranchCoveragePercentage(Double branchCoveragePercentage) { 
        this.branchCoveragePercentage = branchCoveragePercentage; 
    }

    public String getHtmlReportUrl() { return htmlReportUrl; }
    public void setHtmlReportUrl(String htmlReportUrl) { this.htmlReportUrl = htmlReportUrl; }

    public String getReportSummaryPath() { return reportSummaryPath; }
    public void setReportSummaryPath(String reportSummaryPath) { this.reportSummaryPath = reportSummaryPath; }

    public String getReportInfoPath() { return reportInfoPath; }
    public void setReportInfoPath(String reportInfoPath) { this.reportInfoPath = reportInfoPath; }

    public String getBuildId() { return buildId; }
    public void setBuildId(String buildId) { this.buildId = buildId; }

    public String getRevision() { return revision; }
    public void setRevision(String revision) { this.revision = revision; }

    public String getRepositoryUrl() { return repositoryUrl; }
    public void setRepositoryUrl(String repositoryUrl) { this.repositoryUrl = repositoryUrl; }

    public Boolean getIsDifferential() { return isDifferential; }
    public void setIsDifferential(Boolean isDifferential) { this.isDifferential = isDifferential; }

    public String getBaselineCoverageId() { return baselineCoverageId; }
    public void setBaselineCoverageId(String baselineCoverageId) { this.baselineCoverageId = baselineCoverageId; }

    public Integer getAnalysisDurationSeconds() { return analysisDurationSeconds; }
    public void setAnalysisDurationSeconds(Integer analysisDurationSeconds) { 
        this.analysisDurationSeconds = analysisDurationSeconds; 
    }

    public String getAnalysisNotes() { return analysisNotes; }
    public void setAnalysisNotes(String analysisNotes) { this.analysisNotes = analysisNotes; }

    public Boolean getHasQualityIssues() { return hasQualityIssues; }
    public void setHasQualityIssues(Boolean hasQualityIssues) { this.hasQualityIssues = hasQualityIssues; }

    public String getQualityIssues() { return qualityIssues; }
    public void setQualityIssues(String qualityIssues) { this.qualityIssues = qualityIssues; }

    public Boolean getIsComplete() { return isComplete; }
    public void setIsComplete(Boolean isComplete) { this.isComplete = isComplete; }

    public Boolean getIsArchived() { return isArchived; }
    public void setIsArchived(Boolean isArchived) { this.isArchived = isArchived; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoverageInformation that = (CoverageInformation) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(fuzzer, that.fuzzer) &&
               Objects.equals(jobType, that.jobType) &&
               Objects.equals(platform, that.platform) &&
               Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fuzzer, jobType, platform, date);
    }

    @Override
    public String toString() {
        return "CoverageInformation{" +
               "id=" + id +
               ", fuzzer='" + fuzzer + '\'' +
               ", jobType='" + jobType + '\'' +
               ", platform='" + platform + '\'' +
               ", date=" + date +
               ", lineCoveragePercentage=" + lineCoveragePercentage +
               ", functionCoveragePercentage=" + functionCoveragePercentage +
               '}';
    }
}