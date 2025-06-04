package com.google.clusterfuzz.datastore.repository;

import com.google.clusterfuzz.datastore.model.CoverageInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CoverageInformation entity.
 * Provides data access methods for coverage tracking and analysis.
 */
@Repository
public interface CoverageInformationRepository extends JpaRepository<CoverageInformation, Long> {

    // Basic queries
    List<CoverageInformation> findByFuzzer(String fuzzer);
    List<CoverageInformation> findByJobType(String jobType);
    List<CoverageInformation> findByPlatform(String platform);
    
    Optional<CoverageInformation> findByFuzzerAndJobTypeAndPlatformAndDate(
        String fuzzer, String jobType, String platform, LocalDateTime date);

    // Coverage analysis queries
    @Query("SELECT c FROM CoverageInformation c WHERE c.fuzzer = :fuzzer ORDER BY c.date DESC")
    List<CoverageInformation> findByFuzzerOrderByDateDesc(@Param("fuzzer") String fuzzer);

    @Query("SELECT c FROM CoverageInformation c WHERE c.jobType = :jobType ORDER BY c.date DESC")
    List<CoverageInformation> findByJobTypeOrderByDateDesc(@Param("jobType") String jobType);

    @Query("SELECT c FROM CoverageInformation c WHERE c.platform = :platform ORDER BY c.date DESC")
    List<CoverageInformation> findByPlatformOrderByDateDesc(@Param("platform") String platform);

    // Latest coverage queries
    @Query("SELECT c FROM CoverageInformation c WHERE c.fuzzer = :fuzzer AND c.jobType = :jobType " +
           "ORDER BY c.date DESC LIMIT 1")
    Optional<CoverageInformation> findLatestByFuzzerAndJobType(
        @Param("fuzzer") String fuzzer, @Param("jobType") String jobType);

    @Query("SELECT c FROM CoverageInformation c WHERE c.fuzzer = :fuzzer AND c.platform = :platform " +
           "ORDER BY c.date DESC LIMIT 1")
    Optional<CoverageInformation> findLatestByFuzzerAndPlatform(
        @Param("fuzzer") String fuzzer, @Param("platform") String platform);

    @Query("SELECT c FROM CoverageInformation c WHERE c.jobType = :jobType AND c.platform = :platform " +
           "ORDER BY c.date DESC LIMIT 1")
    Optional<CoverageInformation> findLatestByJobTypeAndPlatform(
        @Param("jobType") String jobType, @Param("platform") String platform);

    // Date range queries
    @Query("SELECT c FROM CoverageInformation c WHERE c.date BETWEEN :startDate AND :endDate " +
           "ORDER BY c.date DESC")
    List<CoverageInformation> findByDateRange(
        @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c FROM CoverageInformation c WHERE c.fuzzer = :fuzzer AND " +
           "c.date BETWEEN :startDate AND :endDate ORDER BY c.date DESC")
    List<CoverageInformation> findByFuzzerAndDateRange(
        @Param("fuzzer") String fuzzer, @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c FROM CoverageInformation c WHERE c.date >= :sinceDate ORDER BY c.date DESC")
    List<CoverageInformation> findRecentCoverage(@Param("sinceDate") LocalDateTime sinceDate);

    // Coverage percentage queries
    @Query("SELECT c FROM CoverageInformation c WHERE c.lineCoveragePercentage >= :minPercentage " +
           "ORDER BY c.lineCoveragePercentage DESC")
    List<CoverageInformation> findByMinLineCoverage(@Param("minPercentage") Double minPercentage);

    @Query("SELECT c FROM CoverageInformation c WHERE c.functionCoveragePercentage >= :minPercentage " +
           "ORDER BY c.functionCoveragePercentage DESC")
    List<CoverageInformation> findByMinFunctionCoverage(@Param("minPercentage") Double minPercentage);

    @Query("SELECT c FROM CoverageInformation c WHERE c.edgeCoveragePercentage >= :minPercentage " +
           "ORDER BY c.edgeCoveragePercentage DESC")
    List<CoverageInformation> findByMinEdgeCoverage(@Param("minPercentage") Double minPercentage);

    @Query("SELECT c FROM CoverageInformation c WHERE c.lineCoveragePercentage >= 90.0 " +
           "ORDER BY c.lineCoveragePercentage DESC")
    List<CoverageInformation> findExcellentCoverage();

    @Query("SELECT c FROM CoverageInformation c WHERE c.lineCoveragePercentage >= 70.0 " +
           "AND c.lineCoveragePercentage < 90.0 ORDER BY c.lineCoveragePercentage DESC")
    List<CoverageInformation> findGoodCoverage();

    @Query("SELECT c FROM CoverageInformation c WHERE c.lineCoveragePercentage < 50.0 " +
           "ORDER BY c.lineCoveragePercentage ASC")
    List<CoverageInformation> findPoorCoverage();

    // Build and revision queries
    @Query("SELECT c FROM CoverageInformation c WHERE c.buildId = :buildId")
    List<CoverageInformation> findByBuildId(@Param("buildId") String buildId);

    @Query("SELECT c FROM CoverageInformation c WHERE c.revision = :revision")
    List<CoverageInformation> findByRevision(@Param("revision") String revision);

    @Query("SELECT c FROM CoverageInformation c WHERE c.repositoryUrl = :repositoryUrl")
    List<CoverageInformation> findByRepositoryUrl(@Param("repositoryUrl") String repositoryUrl);

    // Quality and status queries
    @Query("SELECT c FROM CoverageInformation c WHERE c.hasQualityIssues = true")
    List<CoverageInformation> findWithQualityIssues();

    @Query("SELECT c FROM CoverageInformation c WHERE c.isComplete = false")
    List<CoverageInformation> findIncomplete();

    @Query("SELECT c FROM CoverageInformation c WHERE c.isArchived = false")
    List<CoverageInformation> findActive();

    @Query("SELECT c FROM CoverageInformation c WHERE c.isArchived = true")
    List<CoverageInformation> findArchived();

    // Differential coverage queries
    @Query("SELECT c FROM CoverageInformation c WHERE c.isDifferential = true")
    List<CoverageInformation> findDifferentialCoverage();

    @Query("SELECT c FROM CoverageInformation c WHERE c.baselineCoverageId = :baselineId")
    List<CoverageInformation> findByBaselineCoverageId(@Param("baselineId") String baselineId);

    // Statistics queries
    @Query("SELECT COUNT(c) FROM CoverageInformation c WHERE c.fuzzer = :fuzzer")
    long countByFuzzer(@Param("fuzzer") String fuzzer);

    @Query("SELECT COUNT(c) FROM CoverageInformation c WHERE c.jobType = :jobType")
    long countByJobType(@Param("jobType") String jobType);

    @Query("SELECT COUNT(c) FROM CoverageInformation c WHERE c.platform = :platform")
    long countByPlatform(@Param("platform") String platform);

    @Query("SELECT AVG(c.lineCoveragePercentage) FROM CoverageInformation c WHERE c.fuzzer = :fuzzer")
    Double getAverageLineCoverageByFuzzer(@Param("fuzzer") String fuzzer);

    @Query("SELECT AVG(c.functionCoveragePercentage) FROM CoverageInformation c WHERE c.jobType = :jobType")
    Double getAverageFunctionCoverageByJobType(@Param("jobType") String jobType);

    @Query("SELECT MAX(c.lineCoveragePercentage) FROM CoverageInformation c WHERE c.fuzzer = :fuzzer")
    Double getMaxLineCoverageByFuzzer(@Param("fuzzer") String fuzzer);

    @Query("SELECT MIN(c.lineCoveragePercentage) FROM CoverageInformation c WHERE c.fuzzer = :fuzzer")
    Double getMinLineCoverageByFuzzer(@Param("fuzzer") String fuzzer);

    // Coverage trends
    @Query("SELECT c.fuzzer, AVG(c.lineCoveragePercentage) FROM CoverageInformation c " +
           "GROUP BY c.fuzzer ORDER BY AVG(c.lineCoveragePercentage) DESC")
    List<Object[]> getCoverageByFuzzer();

    @Query("SELECT c.jobType, AVG(c.lineCoveragePercentage) FROM CoverageInformation c " +
           "GROUP BY c.jobType ORDER BY AVG(c.lineCoveragePercentage) DESC")
    List<Object[]> getCoverageByJobType();

    @Query("SELECT c.platform, AVG(c.lineCoveragePercentage) FROM CoverageInformation c " +
           "GROUP BY c.platform ORDER BY AVG(c.lineCoveragePercentage) DESC")
    List<Object[]> getCoverageByPlatform();

    @Query("SELECT DATE(c.date), AVG(c.lineCoveragePercentage) FROM CoverageInformation c " +
           "WHERE c.date >= :startDate GROUP BY DATE(c.date) ORDER BY DATE(c.date)")
    List<Object[]> getDailyCoverageTrend(@Param("startDate") LocalDateTime startDate);

    // Coverage improvement tracking
    @Query("SELECT c FROM CoverageInformation c WHERE c.fuzzer = :fuzzer " +
           "ORDER BY c.date DESC LIMIT :limit")
    List<CoverageInformation> findRecentCoverageByFuzzer(
        @Param("fuzzer") String fuzzer, @Param("limit") int limit);

    @Query("SELECT c1 FROM CoverageInformation c1 WHERE c1.fuzzer = :fuzzer AND " +
           "c1.lineCoveragePercentage > (SELECT c2.lineCoveragePercentage FROM CoverageInformation c2 " +
           "WHERE c2.fuzzer = :fuzzer AND c2.date < c1.date ORDER BY c2.date DESC LIMIT 1)")
    List<CoverageInformation> findCoverageImprovements(@Param("fuzzer") String fuzzer);

    // Search queries
    @Query("SELECT c FROM CoverageInformation c WHERE " +
           "LOWER(c.fuzzer) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.jobType) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.platform) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.buildId) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<CoverageInformation> searchCoverage(@Param("searchTerm") String searchTerm);

    @Query("SELECT c FROM CoverageInformation c WHERE c.isArchived = false AND (" +
           "LOWER(c.fuzzer) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.jobType) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.platform) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<CoverageInformation> searchActiveCoverage(@Param("searchTerm") String searchTerm);

    // Analysis duration queries
    @Query("SELECT c FROM CoverageInformation c WHERE c.analysisDurationSeconds > :maxSeconds")
    List<CoverageInformation> findSlowAnalysis(@Param("maxSeconds") Integer maxSeconds);

    @Query("SELECT AVG(c.analysisDurationSeconds) FROM CoverageInformation c WHERE c.fuzzer = :fuzzer")
    Double getAverageAnalysisDurationByFuzzer(@Param("fuzzer") String fuzzer);

    // Report URL queries
    @Query("SELECT c FROM CoverageInformation c WHERE c.htmlReportUrl IS NOT NULL")
    List<CoverageInformation> findWithHtmlReports();

    @Query("SELECT c FROM CoverageInformation c WHERE c.reportSummaryPath IS NOT NULL")
    List<CoverageInformation> findWithSummaryReports();

    // Update operations
    @Modifying
    @Query("UPDATE CoverageInformation c SET c.isArchived = true WHERE c.date < :cutoffDate")
    int archiveOldCoverage(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Modifying
    @Query("UPDATE CoverageInformation c SET c.isComplete = false, c.analysisNotes = :reason " +
           "WHERE c.id = :id")
    int markAsIncomplete(@Param("id") Long id, @Param("reason") String reason);

    @Modifying
    @Query("UPDATE CoverageInformation c SET c.hasQualityIssues = true, c.qualityIssues = :issues " +
           "WHERE c.id = :id")
    int addQualityIssues(@Param("id") Long id, @Param("issues") String issues);

    @Modifying
    @Query("UPDATE CoverageInformation c SET c.htmlReportUrl = :url WHERE c.id = :id")
    int updateHtmlReportUrl(@Param("id") Long id, @Param("url") String url);

    // Cleanup operations
    @Modifying
    @Query("DELETE FROM CoverageInformation c WHERE c.isArchived = true AND c.date < :cutoffDate")
    int deleteArchivedCoverage(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Modifying
    @Query("DELETE FROM CoverageInformation c WHERE c.isComplete = false AND c.createdAt < :cutoffDate")
    int deleteIncompleteCoverage(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Validation queries
    @Query("SELECT c FROM CoverageInformation c WHERE c.functionsCovered > c.functionsTotal")
    List<CoverageInformation> findInvalidFunctionCoverage();

    @Query("SELECT c FROM CoverageInformation c WHERE c.linesCovered > c.linesTotal")
    List<CoverageInformation> findInvalidLineCoverage();

    @Query("SELECT c FROM CoverageInformation c WHERE c.edgesCovered > c.edgesTotal")
    List<CoverageInformation> findInvalidEdgeCoverage();

    @Query("SELECT c FROM CoverageInformation c WHERE " +
           "(c.functionsCovered IS NULL AND c.functionsTotal IS NOT NULL) OR " +
           "(c.linesCovered IS NULL AND c.linesTotal IS NOT NULL) OR " +
           "(c.edgesCovered IS NULL AND c.edgesTotal IS NOT NULL)")
    List<CoverageInformation> findInconsistentCoverage();

    // Duplicate detection
    @Query("SELECT c FROM CoverageInformation c WHERE " +
           "EXISTS (SELECT c2 FROM CoverageInformation c2 WHERE " +
           "c2.fuzzer = c.fuzzer AND c2.jobType = c.jobType AND c2.platform = c.platform AND " +
           "c2.date = c.date AND c2.id != c.id)")
    List<CoverageInformation> findDuplicateCoverage();

    // Performance queries
    @Query("SELECT c.fuzzer, c.jobType, COUNT(c) FROM CoverageInformation c " +
           "GROUP BY c.fuzzer, c.jobType HAVING COUNT(c) > :minCount")
    List<Object[]> findFrequentCombinations(@Param("minCount") long minCount);

    @Query("SELECT c FROM CoverageInformation c WHERE c.fuzzer = :fuzzer AND c.jobType = :jobType " +
           "AND c.platform = :platform ORDER BY c.date DESC LIMIT :limit")
    List<CoverageInformation> findRecentCoverageForTarget(
        @Param("fuzzer") String fuzzer, @Param("jobType") String jobType, 
        @Param("platform") String platform, @Param("limit") int limit);

    // Coverage comparison
    @Query("SELECT c1, c2 FROM CoverageInformation c1, CoverageInformation c2 WHERE " +
           "c1.fuzzer = c2.fuzzer AND c1.jobType = c2.jobType AND c1.platform = c2.platform AND " +
           "c1.date > c2.date AND c1.lineCoveragePercentage > c2.lineCoveragePercentage")
    List<Object[]> findCoverageImprovementPairs();

    // Aggregation queries
    @Query("SELECT COUNT(c), AVG(c.lineCoveragePercentage), MAX(c.lineCoveragePercentage), " +
           "MIN(c.lineCoveragePercentage) FROM CoverageInformation c WHERE c.fuzzer = :fuzzer")
    Object[] getCoverageStatsByFuzzer(@Param("fuzzer") String fuzzer);

    @Query("SELECT c.fuzzer, COUNT(c), AVG(c.lineCoveragePercentage) FROM CoverageInformation c " +
           "WHERE c.date >= :startDate GROUP BY c.fuzzer ORDER BY AVG(c.lineCoveragePercentage) DESC")
    List<Object[]> getCoverageStatsSince(@Param("startDate") LocalDateTime startDate);
}