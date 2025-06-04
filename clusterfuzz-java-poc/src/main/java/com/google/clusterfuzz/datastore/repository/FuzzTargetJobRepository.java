package com.google.clusterfuzz.datastore.repository;

import com.google.clusterfuzz.datastore.model.FuzzTargetJob;
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
 * Repository interface for FuzzTargetJob entities.
 * Provides data access methods for fuzz target job mapping management.
 */
@Repository
public interface FuzzTargetJobRepository extends JpaRepository<FuzzTargetJob, Long> {

    // Basic queries
    Optional<FuzzTargetJob> findByCompositeKey(String compositeKey);
    
    Optional<FuzzTargetJob> findByFuzzTargetNameAndJob(String fuzzTargetName, String job);
    
    List<FuzzTargetJob> findByFuzzTargetName(String fuzzTargetName);
    
    List<FuzzTargetJob> findByJob(String job);
    
    List<FuzzTargetJob> findByEngine(String engine);
    
    // Active/inactive queries
    List<FuzzTargetJob> findByActiveTrue();
    
    List<FuzzTargetJob> findByActiveFalse();
    
    List<FuzzTargetJob> findByJobAndActiveTrue(String job);
    
    List<FuzzTargetJob> findByFuzzTargetNameAndActiveTrue(String fuzzTargetName);
    
    // Weight-based queries
    @Query("SELECT ftj FROM FuzzTargetJob ftj WHERE ftj.weight > :minWeight AND ftj.active = true")
    List<FuzzTargetJob> findActiveByMinWeight(@Param("minWeight") Double minWeight);
    
    @Query("SELECT ftj FROM FuzzTargetJob ftj WHERE ftj.job = :job AND ftj.weight > :minWeight AND ftj.active = true ORDER BY ftj.weight DESC")
    List<FuzzTargetJob> findActiveByJobAndMinWeightOrderByWeightDesc(@Param("job") String job, 
                                                                      @Param("minWeight") Double minWeight);
    
    // Execution time queries
    @Query("SELECT ftj FROM FuzzTargetJob ftj WHERE ftj.lastRun IS NULL AND ftj.active = true")
    List<FuzzTargetJob> findNeverExecuted();
    
    @Query("SELECT ftj FROM FuzzTargetJob ftj WHERE ftj.lastRun < :cutoff AND ftj.active = true ORDER BY ftj.lastRun ASC")
    List<FuzzTargetJob> findStaleExecutions(@Param("cutoff") LocalDateTime cutoff);
    
    @Query("SELECT ftj FROM FuzzTargetJob ftj WHERE ftj.lastRun >= :since ORDER BY ftj.lastRun DESC")
    List<FuzzTargetJob> findRecentExecutions(@Param("since") LocalDateTime since);
    
    // Selection queries for fuzzing
    @Query("SELECT ftj FROM FuzzTargetJob ftj WHERE ftj.job = :job AND ftj.active = true ORDER BY " +
           "(ftj.weight * CASE WHEN ftj.successRate IS NULL THEN 1.0 ELSE ftj.successRate/100.0 END) DESC")
    List<FuzzTargetJob> findEligibleForJob(@Param("job") String job);
    
    @Query("SELECT ftj FROM FuzzTargetJob ftj WHERE ftj.engine = :engine AND ftj.active = true " +
           "ORDER BY ftj.weight DESC, ftj.lastRun ASC NULLS FIRST")
    List<FuzzTargetJob> findEligibleForEngine(@Param("engine") String engine);
    
    // Statistics queries
    @Query("SELECT ftj.job, COUNT(ftj) FROM FuzzTargetJob ftj WHERE ftj.active = true GROUP BY ftj.job ORDER BY COUNT(ftj) DESC")
    List<Object[]> getJobStatistics();
    
    @Query("SELECT ftj.engine, COUNT(ftj) FROM FuzzTargetJob ftj WHERE ftj.active = true GROUP BY ftj.engine ORDER BY COUNT(ftj) DESC")
    List<Object[]> getEngineStatistics();
    
    @Query("SELECT AVG(ftj.weight) FROM FuzzTargetJob ftj WHERE ftj.job = :job AND ftj.active = true")
    Double getAverageWeightForJob(@Param("job") String job);
    
    @Query("SELECT AVG(ftj.averageExecutionTime) FROM FuzzTargetJob ftj WHERE ftj.job = :job AND ftj.averageExecutionTime IS NOT NULL")
    Double getAverageExecutionTimeForJob(@Param("job") String job);
    
    @Query("SELECT AVG(ftj.successRate) FROM FuzzTargetJob ftj WHERE ftj.job = :job AND ftj.successRate IS NOT NULL")
    Double getAverageSuccessRateForJob(@Param("job") String job);
    
    // Performance queries
    @Query("SELECT ftj FROM FuzzTargetJob ftj WHERE ftj.successRate IS NOT NULL AND ftj.successRate < :threshold AND ftj.active = true")
    List<FuzzTargetJob> findLowPerformanceTargets(@Param("threshold") Double threshold);
    
    @Query("SELECT ftj FROM FuzzTargetJob ftj WHERE ftj.averageExecutionTime IS NOT NULL AND ftj.averageExecutionTime > :threshold")
    List<FuzzTargetJob> findSlowExecutionTargets(@Param("threshold") Double threshold);
    
    @Query("SELECT ftj FROM FuzzTargetJob ftj WHERE ftj.executionCount > :minExecutions ORDER BY ftj.successRate DESC NULLS LAST")
    List<FuzzTargetJob> findHighPerformanceTargets(@Param("minExecutions") Long minExecutions);
    
    // Search and filtering
    @Query("SELECT ftj FROM FuzzTargetJob ftj WHERE " +
           "(:fuzzTargetName IS NULL OR ftj.fuzzTargetName LIKE %:fuzzTargetName%) AND " +
           "(:job IS NULL OR ftj.job LIKE %:job%) AND " +
           "(:engine IS NULL OR ftj.engine = :engine) AND " +
           "(:active IS NULL OR ftj.active = :active)")
    Page<FuzzTargetJob> searchFuzzTargetJobs(@Param("fuzzTargetName") String fuzzTargetName,
                                             @Param("job") String job,
                                             @Param("engine") String engine,
                                             @Param("active") Boolean active,
                                             Pageable pageable);
    
    // Maintenance queries
    @Query("SELECT ftj FROM FuzzTargetJob ftj WHERE ftj.lastRun < :cutoff")
    List<FuzzTargetJob> findOldExecutions(@Param("cutoff") LocalDateTime cutoff);
    
    @Query("SELECT ftj FROM FuzzTargetJob ftj WHERE ftj.executionCount = 0")
    List<FuzzTargetJob> findUnusedTargetJobs();
    
    @Query("SELECT ftj FROM FuzzTargetJob ftj WHERE ftj.weight <= 0")
    List<FuzzTargetJob> findZeroWeightTargets();
    
    // Validation queries
    @Query("SELECT ftj FROM FuzzTargetJob ftj WHERE ftj.fuzzTargetName IS NULL OR ftj.fuzzTargetName = ''")
    List<FuzzTargetJob> findInvalidFuzzTargetNames();
    
    @Query("SELECT ftj FROM FuzzTargetJob ftj WHERE ftj.job IS NULL OR ftj.job = ''")
    List<FuzzTargetJob> findInvalidJobs();
    
    @Query("SELECT ftj FROM FuzzTargetJob ftj WHERE ftj.engine IS NULL OR ftj.engine = ''")
    List<FuzzTargetJob> findInvalidEngines();
    
    // Counting queries
    long countByJob(String job);
    
    long countByFuzzTargetName(String fuzzTargetName);
    
    long countByEngine(String engine);
    
    long countByActiveTrue();
    
    long countByJobAndActiveTrue(String job);
    
    // Existence checks
    boolean existsByCompositeKey(String compositeKey);
    
    boolean existsByFuzzTargetNameAndJob(String fuzzTargetName, String job);
    
    // Priority-based queries
    @Query("SELECT ftj FROM FuzzTargetJob ftj WHERE ftj.priority > :minPriority AND ftj.active = true ORDER BY ftj.priority DESC")
    List<FuzzTargetJob> findHighPriorityTargets(@Param("minPriority") Integer minPriority);
    
    @Query("SELECT ftj FROM FuzzTargetJob ftj WHERE ftj.job = :job AND ftj.active = true ORDER BY ftj.priority DESC, ftj.weight DESC")
    List<FuzzTargetJob> findByJobOrderByPriorityAndWeight(@Param("job") String job);
    
    // Bulk operations
    @Query("SELECT ftj.compositeKey FROM FuzzTargetJob ftj WHERE ftj.job = :job")
    List<String> findCompositeKeysByJob(@Param("job") String job);
    
    @Query("SELECT ftj.fuzzTargetName FROM FuzzTargetJob ftj WHERE ftj.job = :job AND ftj.active = true")
    List<String> findActiveFuzzTargetNamesByJob(@Param("job") String job);
    
    // Time-based analysis
    @Query("SELECT ftj FROM FuzzTargetJob ftj WHERE ftj.lastRun BETWEEN :start AND :end")
    List<FuzzTargetJob> findExecutionsBetween(@Param("start") LocalDateTime start, 
                                              @Param("end") LocalDateTime end);
    
    @Query("SELECT DATE(ftj.lastRun), COUNT(ftj) FROM FuzzTargetJob ftj WHERE ftj.lastRun >= :since GROUP BY DATE(ftj.lastRun) ORDER BY DATE(ftj.lastRun)")
    List<Object[]> getExecutionCountByDate(@Param("since") LocalDateTime since);
}