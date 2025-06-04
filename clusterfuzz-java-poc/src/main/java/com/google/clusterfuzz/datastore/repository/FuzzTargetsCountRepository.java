package com.google.clusterfuzz.datastore.repository;

import com.google.clusterfuzz.datastore.model.FuzzTargetsCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FuzzTargetsCount entities.
 * Provides data access methods for fuzz targets count management.
 */
@Repository
public interface FuzzTargetsCountRepository extends JpaRepository<FuzzTargetsCount, Long> {

    // Basic queries
    Optional<FuzzTargetsCount> findByJobName(String jobName);
    
    // Count-based queries
    List<FuzzTargetsCount> findByCountGreaterThan(Integer count);
    
    List<FuzzTargetsCount> findByCountLessThan(Integer count);
    
    List<FuzzTargetsCount> findByCountBetween(Integer minCount, Integer maxCount);
    
    List<FuzzTargetsCount> findByActiveCountGreaterThan(Integer activeCount);
    
    List<FuzzTargetsCount> findByEnabledCountGreaterThan(Integer enabledCount);
    
    // Zero count queries
    @Query("SELECT ftc FROM FuzzTargetsCount ftc WHERE ftc.count = 0")
    List<FuzzTargetsCount> findJobsWithNoTargets();
    
    @Query("SELECT ftc FROM FuzzTargetsCount ftc WHERE ftc.activeCount = 0 AND ftc.count > 0")
    List<FuzzTargetsCount> findJobsWithNoActiveTargets();
    
    @Query("SELECT ftc FROM FuzzTargetsCount ftc WHERE ftc.enabledCount = 0 AND ftc.count > 0")
    List<FuzzTargetsCount> findJobsWithNoEnabledTargets();
    
    // Time-based queries
    @Query("SELECT ftc FROM FuzzTargetsCount ftc WHERE ftc.lastUpdated < :cutoff")
    List<FuzzTargetsCount> findStaleRecords(@Param("cutoff") LocalDateTime cutoff);
    
    @Query("SELECT ftc FROM FuzzTargetsCount ftc WHERE ftc.lastUpdated >= :since ORDER BY ftc.lastUpdated DESC")
    List<FuzzTargetsCount> findRecentlyUpdated(@Param("since") LocalDateTime since);
    
    @Query("SELECT ftc FROM FuzzTargetsCount ftc ORDER BY ftc.lastUpdated ASC")
    List<FuzzTargetsCount> findOldestUpdated();
    
    // Statistics queries
    @Query("SELECT SUM(ftc.count) FROM FuzzTargetsCount ftc")
    Long getTotalTargetsCount();
    
    @Query("SELECT SUM(ftc.activeCount) FROM FuzzTargetsCount ftc")
    Long getTotalActiveTargetsCount();
    
    @Query("SELECT SUM(ftc.enabledCount) FROM FuzzTargetsCount ftc")
    Long getTotalEnabledTargetsCount();
    
    @Query("SELECT AVG(ftc.count) FROM FuzzTargetsCount ftc WHERE ftc.count > 0")
    Double getAverageTargetsPerJob();
    
    @Query("SELECT AVG(ftc.activeCount) FROM FuzzTargetsCount ftc WHERE ftc.activeCount > 0")
    Double getAverageActiveTargetsPerJob();
    
    @Query("SELECT MAX(ftc.count) FROM FuzzTargetsCount ftc")
    Integer getMaxTargetsCount();
    
    @Query("SELECT MIN(ftc.count) FROM FuzzTargetsCount ftc WHERE ftc.count > 0")
    Integer getMinTargetsCount();
    
    // Ranking queries
    @Query("SELECT ftc FROM FuzzTargetsCount ftc ORDER BY ftc.count DESC")
    List<FuzzTargetsCount> findJobsOrderByTargetCountDesc();
    
    @Query("SELECT ftc FROM FuzzTargetsCount ftc ORDER BY ftc.activeCount DESC")
    List<FuzzTargetsCount> findJobsOrderByActiveCountDesc();
    
    @Query("SELECT ftc FROM FuzzTargetsCount ftc WHERE ftc.count > 0 ORDER BY (ftc.activeCount * 100.0 / ftc.count) DESC")
    List<FuzzTargetsCount> findJobsOrderByActivePercentageDesc();
    
    // Top/Bottom queries
    @Query("SELECT ftc FROM FuzzTargetsCount ftc ORDER BY ftc.count DESC LIMIT :limit")
    List<FuzzTargetsCount> findTopJobsByTargetCount(@Param("limit") int limit);
    
    @Query("SELECT ftc FROM FuzzTargetsCount ftc WHERE ftc.count > 0 ORDER BY ftc.count ASC LIMIT :limit")
    List<FuzzTargetsCount> findBottomJobsByTargetCount(@Param("limit") int limit);
    
    // Weight-based queries
    @Query("SELECT ftc FROM FuzzTargetsCount ftc WHERE ftc.averageWeight IS NOT NULL ORDER BY ftc.averageWeight DESC")
    List<FuzzTargetsCount> findJobsOrderByAverageWeightDesc();
    
    @Query("SELECT ftc FROM FuzzTargetsCount ftc WHERE ftc.averageWeight > :minWeight")
    List<FuzzTargetsCount> findJobsWithHighAverageWeight(@Param("minWeight") Double minWeight);
    
    @Query("SELECT AVG(ftc.averageWeight) FROM FuzzTargetsCount ftc WHERE ftc.averageWeight IS NOT NULL")
    Double getGlobalAverageWeight();
    
    // Validation queries
    @Query("SELECT ftc FROM FuzzTargetsCount ftc WHERE ftc.jobName IS NULL OR ftc.jobName = ''")
    List<FuzzTargetsCount> findRecordsWithInvalidJobNames();
    
    @Query("SELECT ftc FROM FuzzTargetsCount ftc WHERE ftc.count < 0")
    List<FuzzTargetsCount> findRecordsWithNegativeCount();
    
    @Query("SELECT ftc FROM FuzzTargetsCount ftc WHERE ftc.activeCount > ftc.count")
    List<FuzzTargetsCount> findRecordsWithInvalidActiveCount();
    
    @Query("SELECT ftc FROM FuzzTargetsCount ftc WHERE ftc.enabledCount > ftc.count")
    List<FuzzTargetsCount> findRecordsWithInvalidEnabledCount();
    
    // Search queries
    @Query("SELECT ftc FROM FuzzTargetsCount ftc WHERE ftc.jobName LIKE %:pattern%")
    List<FuzzTargetsCount> findByJobNameContaining(@Param("pattern") String pattern);
    
    @Query("SELECT ftc FROM FuzzTargetsCount ftc WHERE " +
           "(:minCount IS NULL OR ftc.count >= :minCount) AND " +
           "(:maxCount IS NULL OR ftc.count <= :maxCount) AND " +
           "(:minActiveCount IS NULL OR ftc.activeCount >= :minActiveCount) AND " +
           "(:maxActiveCount IS NULL OR ftc.activeCount <= :maxActiveCount)")
    List<FuzzTargetsCount> findByCountRanges(@Param("minCount") Integer minCount,
                                             @Param("maxCount") Integer maxCount,
                                             @Param("minActiveCount") Integer minActiveCount,
                                             @Param("maxActiveCount") Integer maxActiveCount);
    
    // Existence and counting
    boolean existsByJobName(String jobName);
    
    long countByCountGreaterThan(Integer count);
    
    long countByActiveCountGreaterThan(Integer activeCount);
    
    @Query("SELECT COUNT(ftc) FROM FuzzTargetsCount ftc WHERE ftc.count = 0")
    long countJobsWithNoTargets();
    
    @Query("SELECT COUNT(ftc) FROM FuzzTargetsCount ftc WHERE ftc.activeCount = 0 AND ftc.count > 0")
    long countJobsWithNoActiveTargets();
    
    // Maintenance queries
    @Query("SELECT ftc FROM FuzzTargetsCount ftc WHERE ftc.lastUpdated < :cutoff")
    List<FuzzTargetsCount> findRecordsToRefresh(@Param("cutoff") LocalDateTime cutoff);
    
    @Query("SELECT COUNT(ftc) FROM FuzzTargetsCount ftc WHERE ftc.lastUpdated < :cutoff")
    long countStaleRecords(@Param("cutoff") LocalDateTime cutoff);
    
    // Bulk operations support
    @Query("SELECT ftc.jobName FROM FuzzTargetsCount ftc WHERE ftc.count > :minCount")
    List<String> findJobNamesWithMinTargets(@Param("minCount") Integer minCount);
    
    @Query("SELECT ftc.jobName FROM FuzzTargetsCount ftc WHERE ftc.activeCount > 0")
    List<String> findJobNamesWithActiveTargets();
    
    // Trend analysis
    @Query("SELECT DATE(ftc.lastUpdated), AVG(ftc.count) FROM FuzzTargetsCount ftc WHERE ftc.lastUpdated >= :since GROUP BY DATE(ftc.lastUpdated) ORDER BY DATE(ftc.lastUpdated)")
    List<Object[]> getAverageCountTrend(@Param("since") LocalDateTime since);
    
    @Query("SELECT DATE(ftc.lastUpdated), COUNT(ftc) FROM FuzzTargetsCount ftc WHERE ftc.lastUpdated >= :since GROUP BY DATE(ftc.lastUpdated) ORDER BY DATE(ftc.lastUpdated)")
    List<Object[]> getUpdateFrequencyTrend(@Param("since") LocalDateTime since);
}