package com.google.clusterfuzz.service;

import com.google.clusterfuzz.datastore.model.BuildMetadata;
import com.google.clusterfuzz.datastore.repository.BuildMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for BuildMetadata entity operations.
 * Provides business logic for build information management and artifact tracking.
 */
@Service
@Transactional
public class BuildMetadataService {

    private static final Logger logger = LoggerFactory.getLogger(BuildMetadataService.class);
    
    private static final int DEFAULT_RETENTION_DAYS = 90;
    private static final int DEFAULT_ARCHIVE_DAYS = 30;
    private static final double MIN_TEST_SUCCESS_RATE = 95.0;

    @Autowired
    private BuildMetadataRepository buildRepository;

    // Build creation and management

    /**
     * Create new build metadata.
     */
    public BuildMetadata createBuild(String jobType, String revision, String platform, 
                                   LocalDateTime buildTimestamp) {
        logger.info("Creating build metadata for job '{}', revision '{}', platform '{}'", 
                   jobType, revision, platform);
        
        BuildMetadata build = new BuildMetadata(jobType, revision, platform, buildTimestamp);
        build.setBuildStatus("IN_PROGRESS");
        build.setRetentionDays(DEFAULT_RETENTION_DAYS);
        
        BuildMetadata saved = buildRepository.save(build);
        logger.info("Created build metadata with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Create build with basic configuration.
     */
    public BuildMetadata createBuildWithConfig(String jobType, String revision, String platform,
                                             LocalDateTime buildTimestamp, String buildType,
                                             String compilerVersion, String buildFlags) {
        BuildMetadata build = createBuild(jobType, revision, platform, buildTimestamp);
        build.setBuildType(buildType);
        build.setCompilerVersion(compilerVersion);
        build.setBuildFlags(buildFlags);
        
        // Set build characteristics based on type
        if ("DEBUG".equals(buildType)) {
            build.setIsDebugBuild(true);
        } else if ("RELEASE".equals(buildType)) {
            build.setIsProductionBuild(true);
        }
        
        return buildRepository.save(build);
    }

    /**
     * Update build status.
     */
    public void updateBuildStatus(Long buildId, String status) {
        logger.info("Updating build status for ID {} to '{}'", buildId, status);
        
        int updated = buildRepository.updateBuildStatus(buildId, status);
        if (updated > 0) {
            logger.info("Successfully updated build status: {}", buildId);
        } else {
            logger.warn("Build not found or not updated: {}", buildId);
        }
    }

    /**
     * Mark build as successful.
     */
    public void markBuildSuccessful(Long buildId, String binaryUrl, Long binarySize, 
                                  Integer buildDuration) {
        logger.info("Marking build as successful: {}", buildId);
        
        Optional<BuildMetadata> buildOpt = buildRepository.findById(buildId);
        if (buildOpt.isEmpty()) {
            throw new IllegalArgumentException("Build not found: " + buildId);
        }
        
        BuildMetadata build = buildOpt.get();
        build.setBuildStatus("SUCCESS");
        build.setBinaryUrl(binaryUrl);
        build.setBinarySizeBytes(binarySize);
        build.setBuildDurationSeconds(buildDuration);
        
        buildRepository.save(build);
        logger.info("Successfully marked build as successful: {}", buildId);
    }

    /**
     * Mark build as failed.
     */
    public void markBuildFailed(Long buildId, String errorMessage) {
        logger.info("Marking build as failed: {}", buildId);
        
        Optional<BuildMetadata> buildOpt = buildRepository.findById(buildId);
        if (buildOpt.isEmpty()) {
            throw new IllegalArgumentException("Build not found: " + buildId);
        }
        
        BuildMetadata build = buildOpt.get();
        build.setBuildStatus("FAILED");
        build.addBuildError(errorMessage);
        
        buildRepository.save(build);
        logger.info("Successfully marked build as failed: {}", buildId);
    }

    // Build retrieval

    /**
     * Get build by ID.
     */
    @Transactional(readOnly = true)
    public Optional<BuildMetadata> getBuildById(Long id) {
        return buildRepository.findById(id);
    }

    /**
     * Get build by build ID.
     */
    @Transactional(readOnly = true)
    public Optional<BuildMetadata> getBuildByBuildId(String buildId) {
        return buildRepository.findByBuildId(buildId);
    }

    /**
     * Get latest build for job type.
     */
    @Transactional(readOnly = true)
    public Optional<BuildMetadata> getLatestBuild(String jobType) {
        return buildRepository.findLatestByJobType(jobType);
    }

    /**
     * Get latest successful build for job type.
     */
    @Transactional(readOnly = true)
    public Optional<BuildMetadata> getLatestSuccessfulBuild(String jobType) {
        return buildRepository.findLatestSuccessfulByJobType(jobType);
    }

    /**
     * Get latest successful build for job type and platform.
     */
    @Transactional(readOnly = true)
    public Optional<BuildMetadata> getLatestSuccessfulBuild(String jobType, String platform) {
        return buildRepository.findLatestSuccessfulByJobTypeAndPlatform(jobType, platform);
    }

    /**
     * Get recent builds.
     */
    @Transactional(readOnly = true)
    public List<BuildMetadata> getRecentBuilds(int days) {
        LocalDateTime sinceDate = LocalDateTime.now().minusDays(days);
        return buildRepository.findRecentBuilds(sinceDate);
    }

    /**
     * Get builds by status.
     */
    @Transactional(readOnly = true)
    public List<BuildMetadata> getBuildsByStatus(String status) {
        return buildRepository.findByBuildStatus(status);
    }

    // Build analysis

    /**
     * Get build success rate for job type.
     */
    @Transactional(readOnly = true)
    public Double getBuildSuccessRate(String jobType) {
        return buildRepository.getBuildSuccessRateByJobType(jobType);
    }

    /**
     * Get build success rate for platform.
     */
    @Transactional(readOnly = true)
    public Double getBuildSuccessRateByPlatform(String platform) {
        return buildRepository.getBuildSuccessRateByPlatform(platform);
    }

    /**
     * Get build statistics for job type.
     */
    @Transactional(readOnly = true)
    public BuildStatistics getBuildStatistics(String jobType) {
        Object[] stats = buildRepository.getBuildStatsByJobType(jobType);
        
        BuildStatistics statistics = new BuildStatistics();
        statistics.setJobType(jobType);
        
        if (stats != null && stats.length >= 4) {
            statistics.setTotalBuilds(((Number) stats[0]).longValue());
            statistics.setAverageBuildDuration((Double) stats[1]);
            statistics.setMaxBinarySize(((Number) stats[2]).longValue());
            statistics.setOldestBuildDate((LocalDateTime) stats[3]);
        }
        
        // Get success rate separately
        statistics.setSuccessRate(getBuildSuccessRate(jobType));
        
        return statistics;
    }

    /**
     * Get daily build statistics.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getDailyBuildStats(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return buildRepository.getDailyBuildStats(startDate);
    }

    /**
     * Analyze build performance trends.
     */
    @Transactional(readOnly = true)
    public BuildPerformanceAnalysis analyzeBuildPerformance(String jobType, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<BuildMetadata> recentBuilds = buildRepository
            .findByJobTypeAndDateRange(jobType, startDate, LocalDateTime.now());
        
        BuildPerformanceAnalysis analysis = new BuildPerformanceAnalysis();
        analysis.setJobType(jobType);
        analysis.setAnalysisPeriodDays(days);
        analysis.setTotalBuilds(recentBuilds.size());
        
        if (!recentBuilds.isEmpty()) {
            // Calculate performance metrics
            long successfulBuilds = recentBuilds.stream()
                .filter(BuildMetadata::isSuccessfulBuild)
                .count();
            
            double avgDuration = recentBuilds.stream()
                .filter(b -> b.getBuildDurationSeconds() != null)
                .mapToInt(BuildMetadata::getBuildDurationSeconds)
                .average().orElse(0.0);
            
            double avgSize = recentBuilds.stream()
                .filter(b -> b.getBinarySizeBytes() != null)
                .mapToLong(BuildMetadata::getBinarySizeBytes)
                .average().orElse(0.0);
            
            analysis.setSuccessfulBuilds(successfulBuilds);
            analysis.setSuccessRate((double) successfulBuilds / recentBuilds.size() * 100.0);
            analysis.setAverageBuildDuration(avgDuration);
            analysis.setAverageBinarySize(avgSize);
            
            // Analyze trends
            if (recentBuilds.size() >= 10) {
                List<BuildMetadata> firstHalf = recentBuilds.subList(recentBuilds.size() / 2, recentBuilds.size());
                List<BuildMetadata> secondHalf = recentBuilds.subList(0, recentBuilds.size() / 2);
                
                double firstHalfSuccess = firstHalf.stream()
                    .filter(BuildMetadata::isSuccessfulBuild)
                    .count() / (double) firstHalf.size() * 100.0;
                
                double secondHalfSuccess = secondHalf.stream()
                    .filter(BuildMetadata::isSuccessfulBuild)
                    .count() / (double) secondHalf.size() * 100.0;
                
                analysis.setSuccessRateImprovement(secondHalfSuccess - firstHalfSuccess);
                
                if (analysis.getSuccessRateImprovement() > 5.0) {
                    analysis.setTrend("IMPROVING");
                } else if (analysis.getSuccessRateImprovement() < -5.0) {
                    analysis.setTrend("DECLINING");
                } else {
                    analysis.setTrend("STABLE");
                }
            }
        }
        
        return analysis;
    }

    // Build quality and validation

    /**
     * Get builds with quality issues.
     */
    @Transactional(readOnly = true)
    public List<BuildMetadata> getBuildsWithQualityIssues() {
        List<BuildMetadata> buildsWithWarnings = buildRepository.findBuildsWithWarnings();
        List<BuildMetadata> buildsWithErrors = buildRepository.findBuildsWithErrors();
        
        buildsWithWarnings.addAll(buildsWithErrors);
        return buildsWithWarnings.stream().distinct().toList();
    }

    /**
     * Get builds with test failures.
     */
    @Transactional(readOnly = true)
    public List<BuildMetadata> getBuildsWithTestFailures() {
        return buildRepository.findBuildsWithFailedTests();
    }

    /**
     * Get builds with low test success rate.
     */
    @Transactional(readOnly = true)
    public List<BuildMetadata> getBuildsWithLowTestSuccessRate() {
        return buildRepository.findBuildsByTestSuccessRate(MIN_TEST_SUCCESS_RATE);
    }

    /**
     * Validate build.
     */
    public void validateBuild(Long buildId, String validationNotes) {
        logger.info("Validating build: {}", buildId);
        
        int updated = buildRepository.markAsValidated(buildId, validationNotes);
        if (updated > 0) {
            logger.info("Successfully validated build: {}", buildId);
        } else {
            logger.warn("Build not found or not updated: {}", buildId);
        }
    }

    /**
     * Get validation report.
     */
    @Transactional(readOnly = true)
    public BuildValidationReport getBuildValidationReport() {
        BuildValidationReport report = new BuildValidationReport();
        
        List<BuildMetadata> inconsistentTests = buildRepository.findInconsistentTestCounts();
        List<BuildMetadata> invalidTimestamps = buildRepository.findBuildsWithInvalidTimestamps();
        List<BuildMetadata> invalidSizes = buildRepository.findBuildsWithInvalidSize();
        List<BuildMetadata> duplicates = buildRepository.findDuplicateBuilds();
        
        report.setInconsistentTestCounts(inconsistentTests);
        report.setInvalidTimestamps(invalidTimestamps);
        report.setInvalidSizes(invalidSizes);
        report.setDuplicateBuilds(duplicates);
        
        report.setTotalIssues(inconsistentTests.size() + invalidTimestamps.size() + 
                             invalidSizes.size() + duplicates.size());
        
        return report;
    }

    // Build lifecycle management

    /**
     * Record build download.
     */
    public void recordDownload(Long buildId) {
        logger.debug("Recording download for build: {}", buildId);
        
        int updated = buildRepository.recordDownload(buildId);
        if (updated == 0) {
            logger.warn("Build not found for download recording: {}", buildId);
        }
    }

    /**
     * Archive old builds.
     */
    public int archiveOldBuilds(int days) {
        logger.info("Archiving builds older than {} days", days);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        int archived = buildRepository.archiveOldBuilds(cutoffDate);
        logger.info("Archived {} builds", archived);
        return archived;
    }

    /**
     * Mark expired builds.
     */
    public int markExpiredBuilds() {
        logger.info("Marking expired builds based on retention policy");
        
        LocalDateTime now = LocalDateTime.now();
        int marked = buildRepository.markExpiredBuilds(now);
        logger.info("Marked {} builds as expired", marked);
        return marked;
    }

    /**
     * Delete archived builds.
     */
    public int deleteArchivedBuilds(int days) {
        logger.info("Deleting archived builds older than {} days", days);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        int deleted = buildRepository.deleteArchivedBuilds(cutoffDate);
        logger.info("Deleted {} archived builds", deleted);
        return deleted;
    }

    /**
     * Delete old failed builds.
     */
    public int deleteOldFailedBuilds(int days) {
        logger.info("Deleting failed builds older than {} days", days);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        int deleted = buildRepository.deleteOldFailedBuilds(cutoffDate);
        logger.info("Deleted {} old failed builds", deleted);
        return deleted;
    }

    /**
     * Delete unused old builds.
     */
    public int deleteUnusedOldBuilds(int days) {
        logger.info("Deleting unused builds older than {} days", days);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        int deleted = buildRepository.deleteUnusedOldBuilds(cutoffDate);
        logger.info("Deleted {} unused old builds", deleted);
        return deleted;
    }

    // Search and filtering

    /**
     * Search builds.
     */
    @Transactional(readOnly = true)
    public List<BuildMetadata> searchBuilds(String searchTerm) {
        return buildRepository.searchBuilds(searchTerm);
    }

    /**
     * Search active builds.
     */
    @Transactional(readOnly = true)
    public List<BuildMetadata> searchActiveBuilds(String searchTerm) {
        return buildRepository.searchActiveBuilds(searchTerm);
    }

    /**
     * Get builds by revision.
     */
    @Transactional(readOnly = true)
    public List<BuildMetadata> getBuildsByRevision(String revision) {
        return buildRepository.findByRevision(revision);
    }

    /**
     * Get builds by commit hash.
     */
    @Transactional(readOnly = true)
    public List<BuildMetadata> getBuildsByCommitHash(String commitHash) {
        return buildRepository.findByCommitHash(commitHash);
    }

    /**
     * Get builds by author.
     */
    @Transactional(readOnly = true)
    public List<BuildMetadata> getBuildsByAuthor(String author) {
        return buildRepository.findByCommitAuthor(author);
    }

    // Performance monitoring

    /**
     * Get slow builds.
     */
    @Transactional(readOnly = true)
    public List<BuildMetadata> getSlowBuilds(int maxDurationSeconds) {
        return buildRepository.findSlowBuilds(maxDurationSeconds);
    }

    /**
     * Get large builds.
     */
    @Transactional(readOnly = true)
    public List<BuildMetadata> getLargeBuilds(long maxSizeBytes) {
        return buildRepository.findLargeBuilds(maxSizeBytes);
    }

    /**
     * Get build performance by job type.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getBuildPerformanceByJobType() {
        return buildRepository.getBuildPerformanceByJobType();
    }

    /**
     * Get build performance by platform.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getBuildPerformanceByPlatform() {
        return buildRepository.getBuildPerformanceByPlatform();
    }

    // Statistics and reporting

    /**
     * Get build counts by job type.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getBuildCountsByJobType() {
        return buildRepository.countBuildsByJobType();
    }

    /**
     * Get build counts by platform.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getBuildCountsByPlatform() {
        return buildRepository.countBuildsByPlatform();
    }

    /**
     * Get build counts by status.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getBuildCountsByStatus() {
        return buildRepository.countBuildsByStatus();
    }

    /**
     * Get popular builds.
     */
    @Transactional(readOnly = true)
    public List<BuildMetadata> getPopularBuilds(int minDownloads) {
        return buildRepository.findPopularBuilds(minDownloads);
    }

    /**
     * Get unused builds.
     */
    @Transactional(readOnly = true)
    public List<BuildMetadata> getUnusedBuilds() {
        return buildRepository.findUnusedBuilds();
    }

    // Helper methods

    /**
     * Get total build count.
     */
    @Transactional(readOnly = true)
    public long getTotalBuildCount() {
        return buildRepository.count();
    }

    /**
     * Get build count by job type.
     */
    @Transactional(readOnly = true)
    public long getBuildCountByJobType(String jobType) {
        return buildRepository.countByJobType(jobType);
    }

    /**
     * Update build metadata.
     */
    public BuildMetadata updateBuild(BuildMetadata build) {
        logger.info("Updating build metadata: {}", build.getId());
        return buildRepository.save(build);
    }

    // Inner classes for analysis results

    public static class BuildStatistics {
        private String jobType;
        private Long totalBuilds;
        private Double successRate;
        private Double averageBuildDuration;
        private Long maxBinarySize;
        private LocalDateTime oldestBuildDate;

        // Getters and setters
        public String getJobType() { return jobType; }
        public void setJobType(String jobType) { this.jobType = jobType; }

        public Long getTotalBuilds() { return totalBuilds; }
        public void setTotalBuilds(Long totalBuilds) { this.totalBuilds = totalBuilds; }

        public Double getSuccessRate() { return successRate; }
        public void setSuccessRate(Double successRate) { this.successRate = successRate; }

        public Double getAverageBuildDuration() { return averageBuildDuration; }
        public void setAverageBuildDuration(Double averageBuildDuration) { this.averageBuildDuration = averageBuildDuration; }

        public Long getMaxBinarySize() { return maxBinarySize; }
        public void setMaxBinarySize(Long maxBinarySize) { this.maxBinarySize = maxBinarySize; }

        public LocalDateTime getOldestBuildDate() { return oldestBuildDate; }
        public void setOldestBuildDate(LocalDateTime oldestBuildDate) { this.oldestBuildDate = oldestBuildDate; }
    }

    public static class BuildPerformanceAnalysis {
        private String jobType;
        private int analysisPeriodDays;
        private int totalBuilds;
        private long successfulBuilds;
        private Double successRate;
        private Double averageBuildDuration;
        private Double averageBinarySize;
        private Double successRateImprovement;
        private String trend; // IMPROVING, DECLINING, STABLE

        // Getters and setters
        public String getJobType() { return jobType; }
        public void setJobType(String jobType) { this.jobType = jobType; }

        public int getAnalysisPeriodDays() { return analysisPeriodDays; }
        public void setAnalysisPeriodDays(int analysisPeriodDays) { this.analysisPeriodDays = analysisPeriodDays; }

        public int getTotalBuilds() { return totalBuilds; }
        public void setTotalBuilds(int totalBuilds) { this.totalBuilds = totalBuilds; }

        public long getSuccessfulBuilds() { return successfulBuilds; }
        public void setSuccessfulBuilds(long successfulBuilds) { this.successfulBuilds = successfulBuilds; }

        public Double getSuccessRate() { return successRate; }
        public void setSuccessRate(Double successRate) { this.successRate = successRate; }

        public Double getAverageBuildDuration() { return averageBuildDuration; }
        public void setAverageBuildDuration(Double averageBuildDuration) { this.averageBuildDuration = averageBuildDuration; }

        public Double getAverageBinarySize() { return averageBinarySize; }
        public void setAverageBinarySize(Double averageBinarySize) { this.averageBinarySize = averageBinarySize; }

        public Double getSuccessRateImprovement() { return successRateImprovement; }
        public void setSuccessRateImprovement(Double successRateImprovement) { this.successRateImprovement = successRateImprovement; }

        public String getTrend() { return trend; }
        public void setTrend(String trend) { this.trend = trend; }
    }

    public static class BuildValidationReport {
        private List<BuildMetadata> inconsistentTestCounts;
        private List<BuildMetadata> invalidTimestamps;
        private List<BuildMetadata> invalidSizes;
        private List<BuildMetadata> duplicateBuilds;
        private int totalIssues;

        // Getters and setters
        public List<BuildMetadata> getInconsistentTestCounts() { return inconsistentTestCounts; }
        public void setInconsistentTestCounts(List<BuildMetadata> inconsistentTestCounts) { 
            this.inconsistentTestCounts = inconsistentTestCounts; 
        }

        public List<BuildMetadata> getInvalidTimestamps() { return invalidTimestamps; }
        public void setInvalidTimestamps(List<BuildMetadata> invalidTimestamps) { 
            this.invalidTimestamps = invalidTimestamps; 
        }

        public List<BuildMetadata> getInvalidSizes() { return invalidSizes; }
        public void setInvalidSizes(List<BuildMetadata> invalidSizes) { 
            this.invalidSizes = invalidSizes; 
        }

        public List<BuildMetadata> getDuplicateBuilds() { return duplicateBuilds; }
        public void setDuplicateBuilds(List<BuildMetadata> duplicateBuilds) { 
            this.duplicateBuilds = duplicateBuilds; 
        }

        public int getTotalIssues() { return totalIssues; }
        public void setTotalIssues(int totalIssues) { this.totalIssues = totalIssues; }

        public boolean hasIssues() { return totalIssues > 0; }
    }
}