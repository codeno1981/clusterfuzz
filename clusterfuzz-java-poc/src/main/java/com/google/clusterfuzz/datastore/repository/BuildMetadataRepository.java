package com.google.clusterfuzz.datastore.repository;

import com.google.clusterfuzz.datastore.model.BuildMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for BuildMetadata entity.
 * Provides data access methods for build information and artifact management.
 */
@Repository
public interface BuildMetadataRepository extends JpaRepository<BuildMetadata, Long> {

    // Basic queries
    List<BuildMetadata> findByJobType(String jobType);
    List<BuildMetadata> findByRevision(String revision);
    List<BuildMetadata> findByPlatform(String platform);
    List<BuildMetadata> findByBuildStatus(String buildStatus);
    
    Optional<BuildMetadata> findByJobTypeAndRevisionAndPlatform(
        String jobType, String revision, String platform);

    Optional<BuildMetadata> findByBuildId(String buildId);

    // Build status queries
    @Query("SELECT b FROM BuildMetadata b WHERE b.buildStatus = 'SUCCESS' ORDER BY b.buildTimestamp DESC")
    List<BuildMetadata> findSuccessfulBuilds();

    @Query("SELECT b FROM BuildMetadata b WHERE b.buildStatus = 'FAILED' ORDER BY b.buildTimestamp DESC")
    List<BuildMetadata> findFailedBuilds();

    @Query("SELECT b FROM BuildMetadata b WHERE b.buildStatus = 'IN_PROGRESS'")
    List<BuildMetadata> findInProgressBuilds();

    @Query("SELECT b FROM BuildMetadata b WHERE b.jobType = :jobType AND b.buildStatus = 'SUCCESS' " +
           "ORDER BY b.buildTimestamp DESC")
    List<BuildMetadata> findSuccessfulBuildsByJobType(@Param("jobType") String jobType);

    @Query("SELECT b FROM BuildMetadata b WHERE b.platform = :platform AND b.buildStatus = 'SUCCESS' " +
           "ORDER BY b.buildTimestamp DESC")
    List<BuildMetadata> findSuccessfulBuildsByPlatform(@Param("platform") String platform);

    // Latest build queries
    @Query("SELECT b FROM BuildMetadata b WHERE b.jobType = :jobType ORDER BY b.buildTimestamp DESC LIMIT 1")
    Optional<BuildMetadata> findLatestByJobType(@Param("jobType") String jobType);

    @Query("SELECT b FROM BuildMetadata b WHERE b.jobType = :jobType AND b.platform = :platform " +
           "ORDER BY b.buildTimestamp DESC LIMIT 1")
    Optional<BuildMetadata> findLatestByJobTypeAndPlatform(
        @Param("jobType") String jobType, @Param("platform") String platform);

    @Query("SELECT b FROM BuildMetadata b WHERE b.jobType = :jobType AND b.buildStatus = 'SUCCESS' " +
           "ORDER BY b.buildTimestamp DESC LIMIT 1")
    Optional<BuildMetadata> findLatestSuccessfulByJobType(@Param("jobType") String jobType);

    @Query("SELECT b FROM BuildMetadata b WHERE b.jobType = :jobType AND b.platform = :platform " +
           "AND b.buildStatus = 'SUCCESS' ORDER BY b.buildTimestamp DESC LIMIT 1")
    Optional<BuildMetadata> findLatestSuccessfulByJobTypeAndPlatform(
        @Param("jobType") String jobType, @Param("platform") String platform);

    // Date range queries
    @Query("SELECT b FROM BuildMetadata b WHERE b.buildTimestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY b.buildTimestamp DESC")
    List<BuildMetadata> findByBuildTimestampRange(
        @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b FROM BuildMetadata b WHERE b.buildTimestamp >= :sinceDate " +
           "ORDER BY b.buildTimestamp DESC")
    List<BuildMetadata> findRecentBuilds(@Param("sinceDate") LocalDateTime sinceDate);

    @Query("SELECT b FROM BuildMetadata b WHERE b.jobType = :jobType AND " +
           "b.buildTimestamp BETWEEN :startDate AND :endDate ORDER BY b.buildTimestamp DESC")
    List<BuildMetadata> findByJobTypeAndDateRange(
        @Param("jobType") String jobType, @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate);

    // Build type and configuration queries
    @Query("SELECT b FROM BuildMetadata b WHERE b.buildType = :buildType")
    List<BuildMetadata> findByBuildType(@Param("buildType") String buildType);

    @Query("SELECT b FROM BuildMetadata b WHERE b.isProductionBuild = true")
    List<BuildMetadata> findProductionBuilds();

    @Query("SELECT b FROM BuildMetadata b WHERE b.isDebugBuild = true")
    List<BuildMetadata> findDebugBuilds();

    @Query("SELECT b FROM BuildMetadata b WHERE b.hasSymbols = true")
    List<BuildMetadata> findBuildsWithSymbols();

    @Query("SELECT b FROM BuildMetadata b WHERE b.compilerVersion = :version")
    List<BuildMetadata> findByCompilerVersion(@Param("version") String version);

    // Repository and commit queries
    @Query("SELECT b FROM BuildMetadata b WHERE b.repositoryUrl = :repositoryUrl")
    List<BuildMetadata> findByRepositoryUrl(@Param("repositoryUrl") String repositoryUrl);

    @Query("SELECT b FROM BuildMetadata b WHERE b.branchName = :branchName")
    List<BuildMetadata> findByBranchName(@Param("branchName") String branchName);

    @Query("SELECT b FROM BuildMetadata b WHERE b.commitHash = :commitHash")
    List<BuildMetadata> findByCommitHash(@Param("commitHash") String commitHash);

    @Query("SELECT b FROM BuildMetadata b WHERE b.commitAuthor = :author")
    List<BuildMetadata> findByCommitAuthor(@Param("author") String author);

    // Quality and validation queries
    @Query("SELECT b FROM BuildMetadata b WHERE b.hasBuildWarnings = true")
    List<BuildMetadata> findBuildsWithWarnings();

    @Query("SELECT b FROM BuildMetadata b WHERE b.hasBuildErrors = true")
    List<BuildMetadata> findBuildsWithErrors();

    @Query("SELECT b FROM BuildMetadata b WHERE b.isValidated = true")
    List<BuildMetadata> findValidatedBuilds();

    @Query("SELECT b FROM BuildMetadata b WHERE b.isValidated = false")
    List<BuildMetadata> findUnvalidatedBuilds();

    @Query("SELECT b FROM BuildMetadata b WHERE b.warningCount > :maxWarnings")
    List<BuildMetadata> findBuildsWithExcessiveWarnings(@Param("maxWarnings") Integer maxWarnings);

    // Artifact and URL queries
    @Query("SELECT b FROM BuildMetadata b WHERE b.binaryUrl IS NOT NULL")
    List<BuildMetadata> findBuildsWithBinaries();

    @Query("SELECT b FROM BuildMetadata b WHERE b.symbolsUrl IS NOT NULL")
    List<BuildMetadata> findBuildsWithSymbolsUrl();

    @Query("SELECT b FROM BuildMetadata b WHERE b.sourceArchiveUrl IS NOT NULL")
    List<BuildMetadata> findBuildsWithSourceArchive();

    @Query("SELECT b FROM BuildMetadata b WHERE b.buildLogUrl IS NOT NULL")
    List<BuildMetadata> findBuildsWithLogs();

    // Test results queries
    @Query("SELECT b FROM BuildMetadata b WHERE b.testCount > 0")
    List<BuildMetadata> findBuildsWithTests();

    @Query("SELECT b FROM BuildMetadata b WHERE b.testFailedCount > 0")
    List<BuildMetadata> findBuildsWithFailedTests();

    @Query("SELECT b FROM BuildMetadata b WHERE b.testCount > 0 AND b.testFailedCount = 0")
    List<BuildMetadata> findBuildsWithAllTestsPassed();

    @Query("SELECT b FROM BuildMetadata b WHERE " +
           "(CAST(b.testPassedCount AS double) / CAST(b.testCount AS double)) >= :minSuccessRate")
    List<BuildMetadata> findBuildsByTestSuccessRate(@Param("minSuccessRate") Double minSuccessRate);

    // Size and performance queries
    @Query("SELECT b FROM BuildMetadata b WHERE b.binarySizeBytes > :maxSize")
    List<BuildMetadata> findLargeBuilds(@Param("maxSize") Long maxSize);

    @Query("SELECT b FROM BuildMetadata b WHERE b.buildDurationSeconds > :maxDuration")
    List<BuildMetadata> findSlowBuilds(@Param("maxDuration") Integer maxDuration);

    @Query("SELECT AVG(b.buildDurationSeconds) FROM BuildMetadata b WHERE b.jobType = :jobType")
    Double getAverageBuildDurationByJobType(@Param("jobType") String jobType);

    @Query("SELECT AVG(b.binarySizeBytes) FROM BuildMetadata b WHERE b.jobType = :jobType")
    Double getAverageBinarySizeByJobType(@Param("jobType") String jobType);

    // Archival and lifecycle queries
    @Query("SELECT b FROM BuildMetadata b WHERE b.isArchived = false")
    List<BuildMetadata> findActiveBuilds();

    @Query("SELECT b FROM BuildMetadata b WHERE b.isArchived = true")
    List<BuildMetadata> findArchivedBuilds();

    @Query("SELECT b FROM BuildMetadata b WHERE b.isExpired = true")
    List<BuildMetadata> findExpiredBuilds();

    @Query("SELECT b FROM BuildMetadata b WHERE b.retentionDays IS NOT NULL AND " +
           "b.buildTimestamp < :cutoffDate")
    List<BuildMetadata> findBuildsForExpiration(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Usage tracking queries
    @Query("SELECT b FROM BuildMetadata b WHERE b.downloadCount > :minDownloads " +
           "ORDER BY b.downloadCount DESC")
    List<BuildMetadata> findPopularBuilds(@Param("minDownloads") Integer minDownloads);

    @Query("SELECT b FROM BuildMetadata b WHERE b.lastUsedTimestamp >= :sinceDate")
    List<BuildMetadata> findRecentlyUsedBuilds(@Param("sinceDate") LocalDateTime sinceDate);

    @Query("SELECT b FROM BuildMetadata b WHERE b.downloadCount = 0 OR b.downloadCount IS NULL")
    List<BuildMetadata> findUnusedBuilds();

    // Statistics queries
    @Query("SELECT COUNT(b) FROM BuildMetadata b WHERE b.jobType = :jobType")
    long countByJobType(@Param("jobType") String jobType);

    @Query("SELECT COUNT(b) FROM BuildMetadata b WHERE b.platform = :platform")
    long countByPlatform(@Param("platform") String platform);

    @Query("SELECT COUNT(b) FROM BuildMetadata b WHERE b.buildStatus = :status")
    long countByBuildStatus(@Param("status") String status);

    @Query("SELECT b.jobType, COUNT(b) FROM BuildMetadata b GROUP BY b.jobType ORDER BY COUNT(b) DESC")
    List<Object[]> countBuildsByJobType();

    @Query("SELECT b.platform, COUNT(b) FROM BuildMetadata b GROUP BY b.platform ORDER BY COUNT(b) DESC")
    List<Object[]> countBuildsByPlatform();

    @Query("SELECT b.buildStatus, COUNT(b) FROM BuildMetadata b GROUP BY b.buildStatus")
    List<Object[]> countBuildsByStatus();

    @Query("SELECT b.buildType, COUNT(b) FROM BuildMetadata b GROUP BY b.buildType ORDER BY COUNT(b) DESC")
    List<Object[]> countBuildsByType();

    // Build success rate queries
    @Query("SELECT " +
           "COUNT(CASE WHEN b.buildStatus = 'SUCCESS' THEN 1 END) * 100.0 / COUNT(b) " +
           "FROM BuildMetadata b WHERE b.jobType = :jobType")
    Double getBuildSuccessRateByJobType(@Param("jobType") String jobType);

    @Query("SELECT " +
           "COUNT(CASE WHEN b.buildStatus = 'SUCCESS' THEN 1 END) * 100.0 / COUNT(b) " +
           "FROM BuildMetadata b WHERE b.platform = :platform")
    Double getBuildSuccessRateByPlatform(@Param("platform") String platform);

    // Trend analysis
    @Query("SELECT DATE(b.buildTimestamp), COUNT(b), " +
           "COUNT(CASE WHEN b.buildStatus = 'SUCCESS' THEN 1 END) " +
           "FROM BuildMetadata b WHERE b.buildTimestamp >= :startDate " +
           "GROUP BY DATE(b.buildTimestamp) ORDER BY DATE(b.buildTimestamp)")
    List<Object[]> getDailyBuildStats(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT b.jobType, DATE(b.buildTimestamp), COUNT(b) FROM BuildMetadata b " +
           "WHERE b.buildTimestamp >= :startDate GROUP BY b.jobType, DATE(b.buildTimestamp) " +
           "ORDER BY b.jobType, DATE(b.buildTimestamp)")
    List<Object[]> getDailyBuildStatsByJobType(@Param("startDate") LocalDateTime startDate);

    // Search queries
    @Query("SELECT b FROM BuildMetadata b WHERE " +
           "LOWER(b.jobType) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.revision) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.platform) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.buildId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.commitAuthor) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<BuildMetadata> searchBuilds(@Param("searchTerm") String searchTerm);

    @Query("SELECT b FROM BuildMetadata b WHERE b.isArchived = false AND (" +
           "LOWER(b.jobType) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.revision) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.platform) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<BuildMetadata> searchActiveBuilds(@Param("searchTerm") String searchTerm);

    // Environment and infrastructure queries
    @Query("SELECT b FROM BuildMetadata b WHERE b.dockerImage = :dockerImage")
    List<BuildMetadata> findByDockerImage(@Param("dockerImage") String dockerImage);

    @Query("SELECT b FROM BuildMetadata b WHERE b.buildMachine = :buildMachine")
    List<BuildMetadata> findByBuildMachine(@Param("buildMachine") String buildMachine);

    @Query("SELECT b.dockerImage, COUNT(b) FROM BuildMetadata b WHERE b.dockerImage IS NOT NULL " +
           "GROUP BY b.dockerImage ORDER BY COUNT(b) DESC")
    List<Object[]> countBuildsByDockerImage();

    @Query("SELECT b.buildMachine, COUNT(b) FROM BuildMetadata b WHERE b.buildMachine IS NOT NULL " +
           "GROUP BY b.buildMachine ORDER BY COUNT(b) DESC")
    List<Object[]> countBuildsByMachine();

    // Update operations
    @Modifying
    @Query("UPDATE BuildMetadata b SET b.downloadCount = b.downloadCount + 1, " +
           "b.lastUsedTimestamp = CURRENT_TIMESTAMP WHERE b.id = :id")
    int recordDownload(@Param("id") Long id);

    @Modifying
    @Query("UPDATE BuildMetadata b SET b.isArchived = true, b.archiveTimestamp = CURRENT_TIMESTAMP " +
           "WHERE b.buildTimestamp < :cutoffDate")
    int archiveOldBuilds(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Modifying
    @Query("UPDATE BuildMetadata b SET b.isExpired = true WHERE " +
           "b.retentionDays IS NOT NULL AND b.buildTimestamp < :cutoffDate")
    int markExpiredBuilds(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Modifying
    @Query("UPDATE BuildMetadata b SET b.isValidated = true, b.validationNotes = :notes " +
           "WHERE b.id = :id")
    int markAsValidated(@Param("id") Long id, @Param("notes") String notes);

    @Modifying
    @Query("UPDATE BuildMetadata b SET b.buildStatus = :status WHERE b.id = :id")
    int updateBuildStatus(@Param("id") Long id, @Param("status") String status);

    // Cleanup operations
    @Modifying
    @Query("DELETE FROM BuildMetadata b WHERE b.isArchived = true AND b.archiveTimestamp < :cutoffDate")
    int deleteArchivedBuilds(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Modifying
    @Query("DELETE FROM BuildMetadata b WHERE b.buildStatus = 'FAILED' AND b.buildTimestamp < :cutoffDate")
    int deleteOldFailedBuilds(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Modifying
    @Query("DELETE FROM BuildMetadata b WHERE b.downloadCount = 0 AND b.buildTimestamp < :cutoffDate")
    int deleteUnusedOldBuilds(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Validation queries
    @Query("SELECT b FROM BuildMetadata b WHERE b.testPassedCount + b.testFailedCount != b.testCount")
    List<BuildMetadata> findInconsistentTestCounts();

    @Query("SELECT b FROM BuildMetadata b WHERE b.buildTimestamp > b.commitTimestamp")
    List<BuildMetadata> findBuildsWithInvalidTimestamps();

    @Query("SELECT b FROM BuildMetadata b WHERE b.binarySizeBytes <= 0")
    List<BuildMetadata> findBuildsWithInvalidSize();

    // Duplicate detection
    @Query("SELECT b FROM BuildMetadata b WHERE " +
           "EXISTS (SELECT b2 FROM BuildMetadata b2 WHERE " +
           "b2.jobType = b.jobType AND b2.revision = b.revision AND b2.platform = b.platform AND " +
           "b2.id != b.id)")
    List<BuildMetadata> findDuplicateBuilds();

    // Performance analysis
    @Query("SELECT b.jobType, AVG(b.buildDurationSeconds), AVG(b.binarySizeBytes) " +
           "FROM BuildMetadata b WHERE b.buildStatus = 'SUCCESS' " +
           "GROUP BY b.jobType ORDER BY AVG(b.buildDurationSeconds)")
    List<Object[]> getBuildPerformanceByJobType();

    @Query("SELECT b.platform, AVG(b.buildDurationSeconds), COUNT(b) FROM BuildMetadata b " +
           "WHERE b.buildStatus = 'SUCCESS' GROUP BY b.platform ORDER BY AVG(b.buildDurationSeconds)")
    List<Object[]> getBuildPerformanceByPlatform();

    // Recent activity
    @Query("SELECT b FROM BuildMetadata b WHERE b.jobType = :jobType " +
           "ORDER BY b.buildTimestamp DESC LIMIT :limit")
    List<BuildMetadata> findRecentBuildsByJobType(
        @Param("jobType") String jobType, @Param("limit") int limit);

    @Query("SELECT b FROM BuildMetadata b WHERE b.platform = :platform " +
           "ORDER BY b.buildTimestamp DESC LIMIT :limit")
    List<BuildMetadata> findRecentBuildsByPlatform(
        @Param("platform") String platform, @Param("limit") int limit);

    // Aggregation queries
    @Query("SELECT COUNT(b), AVG(b.buildDurationSeconds), MAX(b.binarySizeBytes), " +
           "MIN(b.buildTimestamp) FROM BuildMetadata b WHERE b.jobType = :jobType")
    Object[] getBuildStatsByJobType(@Param("jobType") String jobType);

    @Query("SELECT b.jobType, COUNT(b), AVG(b.buildDurationSeconds) FROM BuildMetadata b " +
           "WHERE b.buildTimestamp >= :startDate GROUP BY b.jobType " +
           "ORDER BY AVG(b.buildDurationSeconds)")
    List<Object[]> getBuildStatsSince(@Param("startDate") LocalDateTime startDate);
}