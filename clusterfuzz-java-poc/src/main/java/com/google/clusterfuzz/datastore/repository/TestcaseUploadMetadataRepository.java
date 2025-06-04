package com.google.clusterfuzz.datastore.repository;

import com.google.clusterfuzz.datastore.model.TestcaseUploadMetadata;
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
 * Repository interface for TestcaseUploadMetadata entities.
 * Provides data access methods for testcase upload tracking.
 */
@Repository
public interface TestcaseUploadMetadataRepository extends JpaRepository<TestcaseUploadMetadata, Long> {

    // Basic queries
    List<TestcaseUploadMetadata> findByUploaderEmail(String uploaderEmail);
    
    List<TestcaseUploadMetadata> findByStatus(TestcaseUploadMetadata.UploadStatus status);
    
    Optional<TestcaseUploadMetadata> findByTestcaseId(Long testcaseId);
    
    Optional<TestcaseUploadMetadata> findByBlobstoreKey(String blobstoreKey);
    
    List<TestcaseUploadMetadata> findByBotName(String botName);
    
    List<TestcaseUploadMetadata> findByDuplicateOf(Long duplicateOf);
    
    // Status-based queries
    List<TestcaseUploadMetadata> findByStatusOrderByTimestampDesc(TestcaseUploadMetadata.UploadStatus status);
    
    @Query("SELECT tum FROM TestcaseUploadMetadata tum WHERE tum.status IN :statuses ORDER BY tum.timestamp DESC")
    List<TestcaseUploadMetadata> findByStatusIn(@Param("statuses") List<TestcaseUploadMetadata.UploadStatus> statuses);
    
    @Query("SELECT tum FROM TestcaseUploadMetadata tum WHERE tum.status = 'PENDING' ORDER BY tum.timestamp ASC")
    List<TestcaseUploadMetadata> findPendingUploadsOrderByTimestamp();
    
    @Query("SELECT tum FROM TestcaseUploadMetadata tum WHERE tum.status = 'PROCESSING' ORDER BY tum.processingStartedAt ASC")
    List<TestcaseUploadMetadata> findProcessingUploadsOrderByStartTime();
    
    // Time-based queries
    @Query("SELECT tum FROM TestcaseUploadMetadata tum WHERE tum.timestamp >= :since ORDER BY tum.timestamp DESC")
    List<TestcaseUploadMetadata> findRecentUploads(@Param("since") LocalDateTime since);
    
    @Query("SELECT tum FROM TestcaseUploadMetadata tum WHERE tum.timestamp BETWEEN :start AND :end ORDER BY tum.timestamp DESC")
    List<TestcaseUploadMetadata> findUploadsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT tum FROM TestcaseUploadMetadata tum WHERE tum.processingStartedAt IS NOT NULL AND tum.processingCompletedAt IS NULL AND tum.processingStartedAt < :cutoff")
    List<TestcaseUploadMetadata> findStuckProcessing(@Param("cutoff") LocalDateTime cutoff);
    
    @Query("SELECT tum FROM TestcaseUploadMetadata tum WHERE tum.status = 'PENDING' AND tum.timestamp < :cutoff")
    List<TestcaseUploadMetadata> findStalePendingUploads(@Param("cutoff") LocalDateTime cutoff);
    
    // Security and bundled queries
    List<TestcaseUploadMetadata> findBySecurityFlagTrue();
    
    List<TestcaseUploadMetadata> findByBundledTrue();
    
    @Query("SELECT tum FROM TestcaseUploadMetadata tum WHERE tum.bundled = true AND tum.pathInArchive IS NOT NULL")
    List<TestcaseUploadMetadata> findBundledWithPath();
    
    @Query("SELECT tum FROM TestcaseUploadMetadata tum WHERE tum.securityFlag = true AND tum.status = :status")
    List<TestcaseUploadMetadata> findSecurityUploadsByStatus(@Param("status") TestcaseUploadMetadata.UploadStatus status);
    
    // Retry and error queries
    @Query("SELECT tum FROM TestcaseUploadMetadata tum WHERE tum.retries > :maxRetries")
    List<TestcaseUploadMetadata> findExcessiveRetries(@Param("maxRetries") Integer maxRetries);
    
    @Query("SELECT tum FROM TestcaseUploadMetadata tum WHERE tum.status = 'FAILED' AND tum.retries < :maxRetries")
    List<TestcaseUploadMetadata> findRetryableFailures(@Param("maxRetries") Integer maxRetries);
    
    @Query("SELECT tum FROM TestcaseUploadMetadata tum WHERE tum.errorMessage IS NOT NULL AND tum.errorMessage != ''")
    List<TestcaseUploadMetadata> findUploadsWithErrors();
    
    // Statistics queries
    @Query("SELECT tum.status, COUNT(tum) FROM TestcaseUploadMetadata tum GROUP BY tum.status ORDER BY COUNT(tum) DESC")
    List<Object[]> getStatusStatistics();
    
    @Query("SELECT tum.uploaderEmail, COUNT(tum) FROM TestcaseUploadMetadata tum WHERE tum.uploaderEmail IS NOT NULL GROUP BY tum.uploaderEmail ORDER BY COUNT(tum) DESC")
    List<Object[]> getUploaderStatistics();
    
    @Query("SELECT tum.botName, COUNT(tum) FROM TestcaseUploadMetadata tum WHERE tum.botName IS NOT NULL GROUP BY tum.botName ORDER BY COUNT(tum) DESC")
    List<Object[]> getBotStatistics();
    
    @Query("SELECT AVG(EXTRACT(EPOCH FROM (tum.processingCompletedAt - tum.processingStartedAt))) FROM TestcaseUploadMetadata tum WHERE tum.processingStartedAt IS NOT NULL AND tum.processingCompletedAt IS NOT NULL")
    Double getAverageProcessingTimeSeconds();
    
    // Counting queries
    long countByStatus(TestcaseUploadMetadata.UploadStatus status);
    
    long countByUploaderEmail(String uploaderEmail);
    
    long countBySecurityFlagTrue();
    
    long countByBundledTrue();
    
    @Query("SELECT COUNT(tum) FROM TestcaseUploadMetadata tum WHERE tum.timestamp >= :since")
    long countRecentUploads(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(tum) FROM TestcaseUploadMetadata tum WHERE tum.retries > 0")
    long countUploadsWithRetries();
    
    // Search and filtering
    @Query("SELECT tum FROM TestcaseUploadMetadata tum WHERE " +
           "(:uploaderEmail IS NULL OR tum.uploaderEmail LIKE %:uploaderEmail%) AND " +
           "(:filename IS NULL OR tum.filename LIKE %:filename%) AND " +
           "(:status IS NULL OR tum.status = :status) AND " +
           "(:securityFlag IS NULL OR tum.securityFlag = :securityFlag) AND " +
           "(:bundled IS NULL OR tum.bundled = :bundled)")
    Page<TestcaseUploadMetadata> searchUploads(@Param("uploaderEmail") String uploaderEmail,
                                               @Param("filename") String filename,
                                               @Param("status") TestcaseUploadMetadata.UploadStatus status,
                                               @Param("securityFlag") Boolean securityFlag,
                                               @Param("bundled") Boolean bundled,
                                               Pageable pageable);
    
    @Query("SELECT tum FROM TestcaseUploadMetadata tum WHERE tum.filename LIKE %:pattern%")
    List<TestcaseUploadMetadata> findByFilenameContaining(@Param("pattern") String pattern);
    
    @Query("SELECT tum FROM TestcaseUploadMetadata tum WHERE tum.errorMessage LIKE %:pattern%")
    List<TestcaseUploadMetadata> findByErrorMessageContaining(@Param("pattern") String pattern);
    
    // Validation queries
    @Query("SELECT tum FROM TestcaseUploadMetadata tum WHERE tum.uploaderEmail IS NULL OR tum.uploaderEmail = ''")
    List<TestcaseUploadMetadata> findUploadsWithoutUploader();
    
    @Query("SELECT tum FROM TestcaseUploadMetadata tum WHERE tum.filename IS NULL OR tum.filename = ''")
    List<TestcaseUploadMetadata> findUploadsWithoutFilename();
    
    @Query("SELECT tum FROM TestcaseUploadMetadata tum WHERE tum.blobstoreKey IS NULL OR tum.blobstoreKey = ''")
    List<TestcaseUploadMetadata> findUploadsWithoutBlobstoreKey();
    
    // Duplicate detection
    @Query("SELECT tum FROM TestcaseUploadMetadata tum WHERE tum.status = 'DUPLICATE' ORDER BY tum.timestamp DESC")
    List<TestcaseUploadMetadata> findDuplicateUploads();
    
    @Query("SELECT tum FROM TestcaseUploadMetadata tum WHERE tum.duplicateOf IS NOT NULL")
    List<TestcaseUploadMetadata> findUploadsMarkedAsDuplicate();
    
    @Query("SELECT tum.duplicateOf, COUNT(tum) FROM TestcaseUploadMetadata tum WHERE tum.duplicateOf IS NOT NULL GROUP BY tum.duplicateOf ORDER BY COUNT(tum) DESC")
    List<Object[]> getDuplicateStatistics();
    
    // File type analysis
    @Query("SELECT LOWER(SUBSTRING(tum.filename, LENGTH(tum.filename) - POSITION('.' IN REVERSE(tum.filename)) + 2)), COUNT(tum) " +
           "FROM TestcaseUploadMetadata tum WHERE tum.filename LIKE '%.%' GROUP BY LOWER(SUBSTRING(tum.filename, LENGTH(tum.filename) - POSITION('.' IN REVERSE(tum.filename)) + 2)) ORDER BY COUNT(tum) DESC")
    List<Object[]> getFileExtensionStatistics();
    
    @Query("SELECT tum.mimeType, COUNT(tum) FROM TestcaseUploadMetadata tum WHERE tum.mimeType IS NOT NULL GROUP BY tum.mimeType ORDER BY COUNT(tum) DESC")
    List<Object[]> getMimeTypeStatistics();
    
    // Size analysis
    @Query("SELECT AVG(tum.fileSize) FROM TestcaseUploadMetadata tum WHERE tum.fileSize IS NOT NULL")
    Double getAverageFileSize();
    
    @Query("SELECT MAX(tum.fileSize) FROM TestcaseUploadMetadata tum WHERE tum.fileSize IS NOT NULL")
    Long getMaxFileSize();
    
    @Query("SELECT MIN(tum.fileSize) FROM TestcaseUploadMetadata tum WHERE tum.fileSize IS NOT NULL AND tum.fileSize > 0")
    Long getMinFileSize();
    
    // Maintenance queries
    @Query("SELECT tum FROM TestcaseUploadMetadata tum WHERE tum.timestamp < :cutoff AND tum.status IN ('PROCESSED', 'FAILED', 'INVALID')")
    List<TestcaseUploadMetadata> findOldCompletedUploads(@Param("cutoff") LocalDateTime cutoff);
    
    @Query("SELECT tum FROM TestcaseUploadMetadata tum WHERE tum.testcaseId IS NULL AND tum.status = 'PROCESSED'")
    List<TestcaseUploadMetadata> findProcessedWithoutTestcaseId();
    
    // Existence checks
    boolean existsByBlobstoreKey(String blobstoreKey);
    
    boolean existsByTestcaseId(Long testcaseId);
    
    boolean existsByOriginalBlobstoreKey(String originalBlobstoreKey);
    
    // Bulk operations
    @Query("SELECT tum.blobstoreKey FROM TestcaseUploadMetadata tum WHERE tum.status = :status AND tum.blobstoreKey IS NOT NULL")
    List<String> findBlobstoreKeysByStatus(@Param("status") TestcaseUploadMetadata.UploadStatus status);
    
    @Query("SELECT tum.testcaseId FROM TestcaseUploadMetadata tum WHERE tum.testcaseId IS NOT NULL AND tum.uploaderEmail = :email")
    List<Long> findTestcaseIdsByUploader(@Param("email") String email);
    
    // Trend analysis
    @Query("SELECT DATE(tum.timestamp), COUNT(tum) FROM TestcaseUploadMetadata tum WHERE tum.timestamp >= :since GROUP BY DATE(tum.timestamp) ORDER BY DATE(tum.timestamp)")
    List<Object[]> getUploadTrend(@Param("since") LocalDateTime since);
    
    @Query("SELECT DATE(tum.timestamp), tum.status, COUNT(tum) FROM TestcaseUploadMetadata tum WHERE tum.timestamp >= :since GROUP BY DATE(tum.timestamp), tum.status ORDER BY DATE(tum.timestamp), tum.status")
    List<Object[]> getUploadTrendByStatus(@Param("since") LocalDateTime since);
}