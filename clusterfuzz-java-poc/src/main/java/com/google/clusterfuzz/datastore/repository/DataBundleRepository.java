package com.google.clusterfuzz.datastore.repository;

import com.google.clusterfuzz.datastore.model.DataBundle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for DataBundle entities.
 * Provides data access methods for fuzzer data bundle management.
 */
@Repository
public interface DataBundleRepository extends JpaRepository<DataBundle, Long> {

    // Basic queries
    Optional<DataBundle> findByName(String name);
    
    List<DataBundle> findByBucketName(String bucketName);
    
    List<DataBundle> findByActiveTrue();
    
    List<DataBundle> findByActiveFalse();
    
    List<DataBundle> findBySyncToWorkerTrue();
    
    List<DataBundle> findBySyncToWorkerFalse();
    
    // Name-based queries
    @Query("SELECT db FROM DataBundle db WHERE db.name LIKE %:pattern% ORDER BY db.name")
    List<DataBundle> findByNameContaining(@Param("pattern") String pattern);
    
    @Query("SELECT db FROM DataBundle db WHERE db.name LIKE :prefix% ORDER BY db.name")
    List<DataBundle> findByNameStartingWith(@Param("prefix") String prefix);
    
    boolean existsByName(String name);
    
    // Bucket-based queries
    @Query("SELECT db FROM DataBundle db WHERE db.bucketName LIKE %:pattern%")
    List<DataBundle> findByBucketNameContaining(@Param("pattern") String pattern);
    
    @Query("SELECT DISTINCT db.bucketName FROM DataBundle db WHERE db.bucketName IS NOT NULL ORDER BY db.bucketName")
    List<String> findDistinctBucketNames();
    
    long countByBucketName(String bucketName);
    
    // Active and sync preferences
    @Query("SELECT db FROM DataBundle db WHERE db.active = true AND db.syncToWorker = :syncToWorker ORDER BY db.name")
    List<DataBundle> findActiveBySyncPreference(@Param("syncToWorker") Boolean syncToWorker);
    
    @Query("SELECT db FROM DataBundle db WHERE db.active = true ORDER BY db.usageCount DESC, db.name")
    List<DataBundle> findActiveOrderByUsage();
    
    @Query("SELECT db FROM DataBundle db WHERE db.active = true ORDER BY db.lastAccessed DESC NULLS LAST, db.name")
    List<DataBundle> findActiveOrderByLastAccessed();
    
    // Time-based queries
    @Query("SELECT db FROM DataBundle db WHERE db.timestamp >= :since ORDER BY db.timestamp DESC")
    List<DataBundle> findCreatedSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT db FROM DataBundle db WHERE db.lastUpdated >= :since ORDER BY db.lastUpdated DESC")
    List<DataBundle> findUpdatedSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT db FROM DataBundle db WHERE db.lastAccessed >= :since ORDER BY db.lastAccessed DESC")
    List<DataBundle> findAccessedSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT db FROM DataBundle db WHERE db.lastAccessed IS NULL OR db.lastAccessed < :cutoff")
    List<DataBundle> findStaleDataBundles(@Param("cutoff") LocalDateTime cutoff);
    
    @Query("SELECT db FROM DataBundle db WHERE db.timestamp BETWEEN :start AND :end ORDER BY db.timestamp")
    List<DataBundle> findCreatedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Usage statistics
    @Query("SELECT db FROM DataBundle db ORDER BY db.usageCount DESC")
    List<DataBundle> findOrderByUsageCount();
    
    @Query("SELECT db FROM DataBundle db WHERE db.usageCount > :threshold ORDER BY db.usageCount DESC")
    List<DataBundle> findHighUsageBundles(@Param("threshold") Long threshold);
    
    @Query("SELECT db FROM DataBundle db WHERE db.usageCount = 0 OR db.usageCount IS NULL")
    List<DataBundle> findUnusedBundles();
    
    @Query("SELECT AVG(db.usageCount) FROM DataBundle db WHERE db.usageCount IS NOT NULL")
    Double getAverageUsageCount();
    
    @Query("SELECT MAX(db.usageCount) FROM DataBundle db WHERE db.usageCount IS NOT NULL")
    Long getMaxUsageCount();
    
    // Size and file statistics
    @Query("SELECT db FROM DataBundle db WHERE db.sizeBytes IS NOT NULL ORDER BY db.sizeBytes DESC")
    List<DataBundle> findOrderBySize();
    
    @Query("SELECT db FROM DataBundle db WHERE db.sizeBytes > :threshold ORDER BY db.sizeBytes DESC")
    List<DataBundle> findLargeBundles(@Param("threshold") Long threshold);
    
    @Query("SELECT SUM(db.sizeBytes) FROM DataBundle db WHERE db.sizeBytes IS NOT NULL AND db.active = true")
    Long getTotalActiveBundleSize();
    
    @Query("SELECT AVG(db.sizeBytes) FROM DataBundle db WHERE db.sizeBytes IS NOT NULL")
    Double getAverageBundleSize();
    
    @Query("SELECT AVG(db.fileCount) FROM DataBundle db WHERE db.fileCount IS NOT NULL")
    Double getAverageFileCount();
    
    @Query("SELECT db FROM DataBundle db WHERE db.fileCount > :threshold ORDER BY db.fileCount DESC")
    List<DataBundle> findBundlesWithManyFiles(@Param("threshold") Integer threshold);
    
    // Version and description queries
    @Query("SELECT db FROM DataBundle db WHERE db.version IS NOT NULL ORDER BY db.version")
    List<DataBundle> findWithVersion();
    
    @Query("SELECT db FROM DataBundle db WHERE db.version = :version")
    List<DataBundle> findByVersion(@Param("version") String version);
    
    @Query("SELECT db FROM DataBundle db WHERE db.description LIKE %:keyword%")
    List<DataBundle> findByDescriptionContaining(@Param("keyword") String keyword);
    
    @Query("SELECT DISTINCT db.version FROM DataBundle db WHERE db.version IS NOT NULL ORDER BY db.version")
    List<String> findDistinctVersions();
    
    // Tag-based queries
    @Query("SELECT db FROM DataBundle db WHERE db.tags LIKE %:tag%")
    List<DataBundle> findByTag(@Param("tag") String tag);
    
    @Query("SELECT db FROM DataBundle db WHERE db.tags IS NOT NULL AND db.tags != ''")
    List<DataBundle> findWithTags();
    
    @Query("SELECT db FROM DataBundle db WHERE db.tags IS NULL OR db.tags = ''")
    List<DataBundle> findWithoutTags();
    
    // Checksum and integrity
    @Query("SELECT db FROM DataBundle db WHERE db.checksum IS NOT NULL")
    List<DataBundle> findWithChecksum();
    
    @Query("SELECT db FROM DataBundle db WHERE db.checksum IS NULL")
    List<DataBundle> findWithoutChecksum();
    
    Optional<DataBundle> findByChecksum(String checksum);
    
    boolean existsByChecksum(String checksum);
    
    // Search and filtering
    @Query("SELECT db FROM DataBundle db WHERE " +
           "(:name IS NULL OR db.name LIKE %:name%) AND " +
           "(:bucketName IS NULL OR db.bucketName LIKE %:bucketName%) AND " +
           "(:active IS NULL OR db.active = :active) AND " +
           "(:syncToWorker IS NULL OR db.syncToWorker = :syncToWorker) AND " +
           "(:version IS NULL OR db.version = :version)")
    Page<DataBundle> searchDataBundles(@Param("name") String name,
                                       @Param("bucketName") String bucketName,
                                       @Param("active") Boolean active,
                                       @Param("syncToWorker") Boolean syncToWorker,
                                       @Param("version") String version,
                                       Pageable pageable);
    
    @Query("SELECT db FROM DataBundle db WHERE " +
           "db.name LIKE %:query% OR " +
           "db.description LIKE %:query% OR " +
           "db.tags LIKE %:query% OR " +
           "db.version LIKE %:query%")
    List<DataBundle> searchByKeyword(@Param("query") String query);
    
    // Statistics and aggregations
    @Query("SELECT db.syncToWorker, COUNT(db) FROM DataBundle db GROUP BY db.syncToWorker")
    List<Object[]> getSyncPreferenceStatistics();
    
    @Query("SELECT db.active, COUNT(db) FROM DataBundle db GROUP BY db.active")
    List<Object[]> getActiveStatusStatistics();
    
    @Query("SELECT db.bucketName, COUNT(db) FROM DataBundle db WHERE db.bucketName IS NOT NULL GROUP BY db.bucketName ORDER BY COUNT(db) DESC")
    List<Object[]> getBucketStatistics();
    
    @Query("SELECT db.version, COUNT(db) FROM DataBundle db WHERE db.version IS NOT NULL GROUP BY db.version ORDER BY COUNT(db) DESC")
    List<Object[]> getVersionStatistics();
    
    // Counting queries
    long countByActiveTrue();
    
    long countByActiveFalse();
    
    long countBySyncToWorkerTrue();
    
    long countBySyncToWorkerFalse();
    
    @Query("SELECT COUNT(db) FROM DataBundle db WHERE db.lastAccessed IS NULL")
    long countNeverAccessed();
    
    @Query("SELECT COUNT(db) FROM DataBundle db WHERE db.sizeBytes IS NOT NULL")
    long countWithSizeInfo();
    
    @Query("SELECT COUNT(db) FROM DataBundle db WHERE db.checksum IS NOT NULL")
    long countWithChecksum();
    
    // Maintenance and cleanup
    @Query("SELECT db FROM DataBundle db WHERE db.active = false AND db.lastAccessed < :cutoff")
    List<DataBundle> findInactiveAndStale(@Param("cutoff") LocalDateTime cutoff);
    
    @Query("SELECT db FROM DataBundle db WHERE db.usageCount = 0 AND db.timestamp < :cutoff")
    List<DataBundle> findUnusedAndOld(@Param("cutoff") LocalDateTime cutoff);
    
    @Query("SELECT db FROM DataBundle db WHERE db.sizeBytes IS NULL OR db.fileCount IS NULL OR db.checksum IS NULL")
    List<DataBundle> findIncompleteMetadata();
    
    // Validation queries
    @Query("SELECT db FROM DataBundle db WHERE db.name IS NULL OR db.name = ''")
    List<DataBundle> findWithoutName();
    
    @Query("SELECT db FROM DataBundle db WHERE db.bucketName IS NULL OR db.bucketName = ''")
    List<DataBundle> findWithoutBucketName();
    
    @Query("SELECT db FROM DataBundle db WHERE NOT (db.name ~ '^[a-zA-Z0-9_-]+$')")
    List<DataBundle> findWithInvalidNames();
    
    // Trend analysis
    @Query("SELECT DATE(db.timestamp), COUNT(db) FROM DataBundle db WHERE db.timestamp >= :since GROUP BY DATE(db.timestamp) ORDER BY DATE(db.timestamp)")
    List<Object[]> getCreationTrend(@Param("since") LocalDateTime since);
    
    @Query("SELECT DATE(db.lastAccessed), COUNT(db) FROM DataBundle db WHERE db.lastAccessed >= :since GROUP BY DATE(db.lastAccessed) ORDER BY DATE(db.lastAccessed)")
    List<Object[]> getAccessTrend(@Param("since") LocalDateTime since);
    
    // Bulk operations
    @Query("SELECT db.name FROM DataBundle db WHERE db.active = true ORDER BY db.name")
    List<String> findActiveDataBundleNames();
    
    @Query("SELECT db.bucketName FROM DataBundle db WHERE db.active = true AND db.bucketName IS NOT NULL")
    List<String> findActiveBucketNames();
    
    @Query("SELECT db FROM DataBundle db WHERE db.name IN :names")
    List<DataBundle> findByNameIn(@Param("names") List<String> names);
    
    @Query("SELECT db FROM DataBundle db WHERE db.bucketName IN :bucketNames")
    List<DataBundle> findByBucketNameIn(@Param("bucketNames") List<String> bucketNames);
}