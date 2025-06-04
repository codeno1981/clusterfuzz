package com.google.clusterfuzz.datastore.repository;

import com.google.clusterfuzz.datastore.model.FuzzerJPA;
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
 * Repository interface for Fuzzer entities.
 * Provides data access methods for fuzzer management.
 */
@Repository
public interface FuzzerRepository extends JpaRepository<FuzzerJPA, Long> {

    // Basic queries
    Optional<FuzzerJPA> findByName(String name);
    
    List<FuzzerJPA> findByBuiltinTrue();
    
    List<FuzzerJPA> findByDifferentialTrue();
    
    List<FuzzerJPA> findByExternalContributionTrue();
    
    List<FuzzerJPA> findByUntrustedContentTrue();
    
    // Platform queries
    @Query("SELECT f FROM FuzzerJPA f WHERE f.supportedPlatforms LIKE %:platform%")
    List<FuzzerJPA> findBySupportedPlatform(@Param("platform") String platform);
    
    // Job-related queries
    @Query("SELECT f FROM FuzzerJPA f JOIN f.jobs j WHERE j = :jobType")
    List<FuzzerJPA> findByJobType(@Param("jobType") String jobType);
    
    @Query("SELECT f FROM FuzzerJPA f WHERE SIZE(f.jobs) > 0")
    List<FuzzerJPA> findFuzzersWithJobs();
    
    @Query("SELECT f FROM FuzzerJPA f WHERE SIZE(f.jobs) = 0")
    List<FuzzerJPA> findFuzzersWithoutJobs();
    
    // Search queries
    @Query("SELECT f FROM FuzzerJPA f JOIN f.keywords k WHERE k IN :keywords")
    List<FuzzerJPA> findByKeywords(@Param("keywords") List<String> keywords);
    
    @Query("SELECT f FROM FuzzerJPA f WHERE " +
           "(:name IS NULL OR f.name LIKE %:name%) AND " +
           "(:source IS NULL OR f.source LIKE %:source%) AND " +
           "(:builtin IS NULL OR f.builtin = :builtin) AND " +
           "(:differential IS NULL OR f.differential = :differential)")
    Page<FuzzerJPA> searchFuzzers(@Param("name") String name,
                                  @Param("source") String source,
                                  @Param("builtin") Boolean builtin,
                                  @Param("differential") Boolean differential,
                                  Pageable pageable);
    
    // Statistics queries
    @Query("SELECT COUNT(f) FROM FuzzerJPA f WHERE f.builtin = true")
    long countBuiltinFuzzers();
    
    @Query("SELECT COUNT(f) FROM FuzzerJPA f WHERE f.differential = true")
    long countDifferentialFuzzers();
    
    @Query("SELECT COUNT(f) FROM FuzzerJPA f WHERE f.externalContribution = true")
    long countExternalContributionFuzzers();
    
    // Recent activity
    @Query("SELECT f FROM FuzzerJPA f WHERE f.resultTimestamp >= :since ORDER BY f.resultTimestamp DESC")
    List<FuzzerJPA> findRecentlyActive(@Param("since") LocalDateTime since);
    
    @Query("SELECT f FROM FuzzerJPA f WHERE f.timestamp >= :since ORDER BY f.timestamp DESC")
    List<FuzzerJPA> findRecentlyCreated(@Param("since") LocalDateTime since);
    
    // Performance queries
    @Query("SELECT f FROM FuzzerJPA f WHERE f.returnCode = 0 AND f.resultTimestamp >= :since")
    List<FuzzerJPA> findSuccessfulFuzzersSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT f FROM FuzzerJPA f WHERE f.returnCode != 0 AND f.resultTimestamp >= :since")
    List<FuzzerJPA> findFailedFuzzersSince(@Param("since") LocalDateTime since);
    
    // Configuration queries
    @Query("SELECT f FROM FuzzerJPA f WHERE f.timeout > :timeout")
    List<FuzzerJPA> findFuzzersWithTimeoutGreaterThan(@Param("timeout") Integer timeout);
    
    @Query("SELECT f FROM FuzzerJPA f WHERE f.maxTestcases > :maxTestcases")
    List<FuzzerJPA> findFuzzersWithMaxTestcasesGreaterThan(@Param("maxTestcases") Integer maxTestcases);
    
    @Query("SELECT f FROM FuzzerJPA f WHERE f.hasLargeTestcases = true")
    List<FuzzerJPA> findFuzzersWithLargeTestcases();
    
    // Data bundle queries
    @Query("SELECT f FROM FuzzerJPA f WHERE f.dataBundleName = :bundleName")
    List<FuzzerJPA> findByDataBundle(@Param("bundleName") String bundleName);
    
    @Query("SELECT DISTINCT f.dataBundleName FROM FuzzerJPA f WHERE f.dataBundleName IS NOT NULL AND f.dataBundleName != ''")
    List<String> findAllDataBundleNames();
    
    // Source tracking
    @Query("SELECT f.source, COUNT(f) FROM FuzzerJPA f WHERE f.source IS NOT NULL GROUP BY f.source ORDER BY COUNT(f) DESC")
    List<Object[]> getSourceStatistics();
    
    // Revision tracking
    @Query("SELECT f FROM FuzzerJPA f WHERE f.revision IS NOT NULL ORDER BY f.revision DESC")
    List<FuzzerJPA> findByRevisionOrderByRevisionDesc();
    
    @Query("SELECT MAX(f.revision) FROM FuzzerJPA f WHERE f.name = :name")
    Optional<Integer> findLatestRevisionByName(@Param("name") String name);
    
    // Custom validation queries
    @Query("SELECT f FROM FuzzerJPA f WHERE f.name IS NULL OR f.name = ''")
    List<FuzzerJPA> findFuzzersWithInvalidNames();
    
    @Query("SELECT f FROM FuzzerJPA f WHERE f.executablePath IS NULL OR f.executablePath = ''")
    List<FuzzerJPA> findFuzzersWithoutExecutablePath();
    
    // Cleanup queries
    @Query("SELECT f FROM FuzzerJPA f WHERE f.resultTimestamp < :cutoffDate")
    List<FuzzerJPA> findStaleResults(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT f FROM FuzzerJPA f WHERE f.blobstoreKey IS NULL OR f.blobstoreKey = ''")
    List<FuzzerJPA> findFuzzersWithoutBlobstoreKey();
}