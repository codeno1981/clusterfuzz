package com.google.clusterfuzz.datastore.repository;

import com.google.clusterfuzz.datastore.model.Lock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Lock entity operations.
 * Provides data access methods for distributed locking mechanism.
 */
@Repository
public interface LockRepository extends JpaRepository<Lock, Long> {

    // Basic queries
    Optional<Lock> findByHolder(String holder);
    
    Optional<Lock> findByResourceName(String resourceName);
    
    List<Lock> findByLockType(String lockType);
    
    boolean existsByResourceName(String resourceName);
    
    boolean existsByHolder(String holder);

    // Active lock queries
    @Query("SELECT l FROM Lock l WHERE l.active = true AND l.expirationTime > CURRENT_TIMESTAMP")
    List<Lock> findActiveLocks();

    @Query("SELECT l FROM Lock l WHERE " +
           "l.active = true AND l.expirationTime > CURRENT_TIMESTAMP " +
           "ORDER BY l.expirationTime ASC")
    List<Lock> findActiveLocksOrderByExpiration();

    @Query("SELECT l FROM Lock l WHERE " +
           "l.resourceName = :resourceName AND l.active = true AND " +
           "l.expirationTime > CURRENT_TIMESTAMP")
    Optional<Lock> findActiveLockByResource(@Param("resourceName") String resourceName);

    @Query("SELECT l FROM Lock l WHERE " +
           "l.holder = :holder AND l.active = true AND " +
           "l.expirationTime > CURRENT_TIMESTAMP")
    List<Lock> findActiveLocksForHolder(@Param("holder") String holder);

    // Expired lock queries
    @Query("SELECT l FROM Lock l WHERE l.expirationTime <= CURRENT_TIMESTAMP")
    List<Lock> findExpiredLocks();

    @Query("SELECT l FROM Lock l WHERE " +
           "l.active = true AND l.expirationTime <= CURRENT_TIMESTAMP")
    List<Lock> findActiveExpiredLocks();

    @Query("SELECT l FROM Lock l WHERE " +
           "l.expirationTime BETWEEN CURRENT_TIMESTAMP AND :nearFutureTime")
    List<Lock> findLocksExpiringSoon(@Param("nearFutureTime") LocalDateTime nearFutureTime);

    // Holder-based queries
    @Query("SELECT l FROM Lock l WHERE l.holder = :holder ORDER BY l.expirationTime DESC")
    List<Lock> findAllLocksByHolder(@Param("holder") String holder);

    @Query("SELECT l FROM Lock l WHERE " +
           "LOWER(l.holder) LIKE LOWER(CONCAT('%', :holderPattern, '%'))")
    List<Lock> findLocksByHolderPattern(@Param("holderPattern") String holderPattern);

    @Query("SELECT DISTINCT l.holder FROM Lock l WHERE " +
           "l.active = true AND l.expirationTime > CURRENT_TIMESTAMP")
    List<String> findActiveHolders();

    // Resource-based queries
    @Query("SELECT l FROM Lock l WHERE l.resourceName IS NOT NULL ORDER BY l.resourceName")
    List<Lock> findResourceLocks();

    @Query("SELECT l FROM Lock l WHERE l.resourceName IS NULL")
    List<Lock> findGlobalLocks();

    @Query("SELECT l FROM Lock l WHERE " +
           "LOWER(l.resourceName) LIKE LOWER(CONCAT('%', :resourcePattern, '%'))")
    List<Lock> findLocksByResourcePattern(@Param("resourcePattern") String resourcePattern);

    @Query("SELECT DISTINCT l.resourceName FROM Lock l WHERE " +
           "l.resourceName IS NOT NULL AND l.active = true AND " +
           "l.expirationTime > CURRENT_TIMESTAMP")
    List<String> findActiveResources();

    // Time-based queries
    @Query("SELECT l FROM Lock l WHERE " +
           "l.acquiredTime BETWEEN :startTime AND :endTime " +
           "ORDER BY l.acquiredTime DESC")
    List<Lock> findLocksByAcquiredTimeBetween(@Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);

    @Query("SELECT l FROM Lock l WHERE " +
           "l.expirationTime BETWEEN :startTime AND :endTime " +
           "ORDER BY l.expirationTime ASC")
    List<Lock> findLocksByExpirationTimeBetween(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    @Query("SELECT l FROM Lock l WHERE " +
           "l.acquiredTime < :cutoffTime ORDER BY l.acquiredTime ASC")
    List<Lock> findOldLocks(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Renewal-based queries
    @Query("SELECT l FROM Lock l WHERE l.renewalCount > :minRenewals")
    List<Lock> findHeavilyRenewedLocks(@Param("minRenewals") Integer minRenewals);

    @Query("SELECT l FROM Lock l WHERE " +
           "l.maxRenewals IS NOT NULL AND l.renewalCount >= l.maxRenewals")
    List<Lock> findLocksAtMaxRenewals();

    @Query("SELECT l FROM Lock l WHERE " +
           "l.active = true AND l.expirationTime > CURRENT_TIMESTAMP AND " +
           "(l.maxRenewals IS NULL OR l.renewalCount < l.maxRenewals)")
    List<Lock> findRenewableLocks();

    // Statistics queries
    @Query("SELECT COUNT(l) FROM Lock l WHERE " +
           "l.active = true AND l.expirationTime > CURRENT_TIMESTAMP")
    long countActiveLocks();

    @Query("SELECT COUNT(l) FROM Lock l WHERE l.expirationTime <= CURRENT_TIMESTAMP")
    long countExpiredLocks();

    @Query("SELECT l.lockType, COUNT(l) FROM Lock l WHERE " +
           "l.lockType IS NOT NULL AND l.active = true AND " +
           "l.expirationTime > CURRENT_TIMESTAMP " +
           "GROUP BY l.lockType")
    List<Object[]> countActiveLocksByType();

    @Query("SELECT l.holder, COUNT(l) FROM Lock l WHERE " +
           "l.active = true AND l.expirationTime > CURRENT_TIMESTAMP " +
           "GROUP BY l.holder ORDER BY COUNT(l) DESC")
    List<Object[]> countActiveLocksByHolder();

    @Query("SELECT AVG(l.renewalCount) FROM Lock l WHERE l.renewalCount > 0")
    Double getAverageRenewalCount();

    // Conflict detection queries
    @Query("SELECT l FROM Lock l WHERE " +
           "l.resourceName = :resourceName AND l.holder != :holder AND " +
           "l.active = true AND l.expirationTime > CURRENT_TIMESTAMP")
    List<Lock> findConflictingLocks(@Param("resourceName") String resourceName, 
                                   @Param("holder") String holder);

    @Query("SELECT l1 FROM Lock l1, Lock l2 WHERE " +
           "l1.id != l2.id AND l1.resourceName = l2.resourceName AND " +
           "l1.active = true AND l2.active = true AND " +
           "l1.expirationTime > CURRENT_TIMESTAMP AND l2.expirationTime > CURRENT_TIMESTAMP")
    List<Lock> findDuplicateActiveLocks();

    // Bulk operations
    @Modifying
    @Query("UPDATE Lock l SET l.active = false WHERE l.expirationTime <= CURRENT_TIMESTAMP")
    int deactivateExpiredLocks();

    @Modifying
    @Query("UPDATE Lock l SET l.expirationTime = :newExpirationTime, " +
           "l.renewalCount = l.renewalCount + 1 WHERE l.id = :id")
    int renewLock(@Param("id") Long id, @Param("newExpirationTime") LocalDateTime newExpirationTime);

    @Modifying
    @Query("UPDATE Lock l SET l.active = false WHERE l.holder = :holder")
    int releaseAllLocksForHolder(@Param("holder") String holder);

    @Modifying
    @Query("UPDATE Lock l SET l.active = false WHERE " +
           "l.resourceName = :resourceName AND l.holder = :holder")
    int releaseLockForResource(@Param("resourceName") String resourceName, 
                              @Param("holder") String holder);

    @Modifying
    @Query("DELETE FROM Lock l WHERE " +
           "l.active = false AND l.expirationTime < :cutoffTime")
    int deleteOldInactiveLocks(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Advanced queries
    @Query("SELECT l FROM Lock l WHERE " +
           "l.active = true AND l.expirationTime > CURRENT_TIMESTAMP AND " +
           "l.expirationTime < :warningTime ORDER BY l.expirationTime ASC")
    List<Lock> findLocksNearingExpiration(@Param("warningTime") LocalDateTime warningTime);

    @Query("SELECT l FROM Lock l WHERE " +
           "l.active = true AND l.expirationTime > CURRENT_TIMESTAMP AND " +
           "TIMESTAMPDIFF(HOUR, l.acquiredTime, CURRENT_TIMESTAMP) > :hours")
    List<Lock> findLongRunningLocks(@Param("hours") int hours);

    @Query("SELECT l FROM Lock l WHERE " +
           "l.purpose IS NOT NULL AND " +
           "LOWER(l.purpose) LIKE LOWER(CONCAT('%', :purposePattern, '%'))")
    List<Lock> findLocksByPurpose(@Param("purposePattern") String purposePattern);

    // Lock validation queries
    @Query("SELECT l FROM Lock l WHERE " +
           "l.active = true AND l.expirationTime > CURRENT_TIMESTAMP AND " +
           "l.holder = :holder AND l.resourceName = :resourceName")
    Optional<Lock> findValidLock(@Param("holder") String holder, 
                                @Param("resourceName") String resourceName);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Lock l WHERE " +
           "l.resourceName = :resourceName AND l.active = true AND " +
           "l.expirationTime > CURRENT_TIMESTAMP")
    boolean isResourceLocked(@Param("resourceName") String resourceName);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Lock l WHERE " +
           "l.resourceName = :resourceName AND l.holder = :holder AND " +
           "l.active = true AND l.expirationTime > CURRENT_TIMESTAMP")
    boolean isResourceLockedByHolder(@Param("resourceName") String resourceName, 
                                    @Param("holder") String holder);

    // Cleanup and maintenance
    @Query("SELECT l FROM Lock l WHERE " +
           "l.active = false AND l.updatedAt < :cutoffTime")
    Page<Lock> findOldInactiveLocks(@Param("cutoffTime") LocalDateTime cutoffTime, 
                                   Pageable pageable);

    @Query("SELECT l FROM Lock l WHERE " +
           "l.expirationTime < :cutoffTime ORDER BY l.expirationTime ASC")
    Page<Lock> findExpiredLocksForCleanup(@Param("cutoffTime") LocalDateTime cutoffTime, 
                                         Pageable pageable);

    // Performance monitoring
    @Query("SELECT l.lockType, AVG(TIMESTAMPDIFF(SECOND, l.acquiredTime, l.expirationTime)) " +
           "FROM Lock l WHERE l.lockType IS NOT NULL " +
           "GROUP BY l.lockType")
    List<Object[]> getAverageLockDurationByType();

    @Query("SELECT l.holder, COUNT(l), AVG(l.renewalCount) FROM Lock l " +
           "GROUP BY l.holder ORDER BY COUNT(l) DESC")
    List<Object[]> getLockUsageStatsByHolder();

    // Search queries
    @Query("SELECT l FROM Lock l WHERE " +
           "LOWER(l.holder) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(l.resourceName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(l.purpose) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Lock> searchLocks(@Param("searchTerm") String searchTerm);

    @Query("SELECT l FROM Lock l WHERE " +
           "l.active = true AND l.expirationTime > CURRENT_TIMESTAMP AND (" +
           "LOWER(l.holder) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(l.resourceName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Lock> searchActiveLocks(@Param("searchTerm") String searchTerm);
}