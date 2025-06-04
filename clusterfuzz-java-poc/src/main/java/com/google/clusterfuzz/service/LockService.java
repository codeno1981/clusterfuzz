package com.google.clusterfuzz.service;

import com.google.clusterfuzz.datastore.model.Lock;
import com.google.clusterfuzz.datastore.repository.LockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Lock entity operations.
 * Provides business logic for distributed locking mechanism.
 */
@Service
@Transactional
public class LockService {

    private static final Logger logger = LoggerFactory.getLogger(LockService.class);
    
    private static final int DEFAULT_LOCK_DURATION_MINUTES = 30;
    private static final int MAX_RENEWAL_COUNT = 10;

    @Autowired
    private LockRepository lockRepository;

    // Lock acquisition and management

    /**
     * Acquire a lock for a resource.
     */
    public Optional<Lock> acquireLock(String resourceName, String holder) {
        return acquireLock(resourceName, holder, DEFAULT_LOCK_DURATION_MINUTES, null);
    }

    /**
     * Acquire a lock with custom duration.
     */
    public Optional<Lock> acquireLock(String resourceName, String holder, int durationMinutes) {
        return acquireLock(resourceName, holder, durationMinutes, null);
    }

    /**
     * Acquire a lock with purpose and custom duration.
     */
    public Optional<Lock> acquireLock(String resourceName, String holder, int durationMinutes, String purpose) {
        logger.debug("Attempting to acquire lock for resource '{}' by holder '{}'", resourceName, holder);
        
        // Check if resource is already locked
        if (isResourceLocked(resourceName)) {
            logger.debug("Resource '{}' is already locked", resourceName);
            return Optional.empty();
        }
        
        // Create new lock
        Lock lock = new Lock(resourceName, holder, durationMinutes);
        if (purpose != null) {
            lock.setPurpose(purpose);
        }
        
        try {
            Lock savedLock = lockRepository.save(lock);
            logger.info("Successfully acquired lock for resource '{}' by holder '{}'", resourceName, holder);
            return Optional.of(savedLock);
        } catch (Exception e) {
            logger.error("Failed to acquire lock for resource '{}' by holder '{}'", resourceName, holder, e);
            return Optional.empty();
        }
    }

    /**
     * Acquire a global lock (no specific resource).
     */
    public Optional<Lock> acquireGlobalLock(String holder, String lockType) {
        return acquireGlobalLock(holder, lockType, DEFAULT_LOCK_DURATION_MINUTES, null);
    }

    /**
     * Acquire a global lock with custom duration and purpose.
     */
    public Optional<Lock> acquireGlobalLock(String holder, String lockType, int durationMinutes, String purpose) {
        logger.debug("Attempting to acquire global lock of type '{}' by holder '{}'", lockType, holder);
        
        Lock lock = new Lock(holder, durationMinutes);
        lock.setLockType(lockType);
        if (purpose != null) {
            lock.setPurpose(purpose);
        }
        
        try {
            Lock savedLock = lockRepository.save(lock);
            logger.info("Successfully acquired global lock of type '{}' by holder '{}'", lockType, holder);
            return Optional.of(savedLock);
        } catch (Exception e) {
            logger.error("Failed to acquire global lock of type '{}' by holder '{}'", lockType, holder, e);
            return Optional.empty();
        }
    }

    // Lock renewal

    /**
     * Renew a lock by extending its expiration time.
     */
    public boolean renewLock(Long lockId, int additionalMinutes) {
        logger.debug("Attempting to renew lock with ID: {}", lockId);
        
        Optional<Lock> lockOpt = lockRepository.findById(lockId);
        if (lockOpt.isEmpty()) {
            logger.warn("Lock not found for renewal: {}", lockId);
            return false;
        }
        
        Lock lock = lockOpt.get();
        
        // Check if lock is still active and not expired
        if (!lock.isActive() || lock.isExpired()) {
            logger.warn("Cannot renew inactive or expired lock: {}", lockId);
            return false;
        }
        
        // Check renewal limits
        if (lock.getMaxRenewals() != null && lock.getRenewalCount() >= lock.getMaxRenewals()) {
            logger.warn("Lock has reached maximum renewal count: {}", lockId);
            return false;
        }
        
        LocalDateTime newExpiration = lock.getExpirationTime().plusMinutes(additionalMinutes);
        int updated = lockRepository.renewLock(lockId, newExpiration);
        
        if (updated > 0) {
            logger.info("Successfully renewed lock: {}", lockId);
            return true;
        } else {
            logger.warn("Failed to renew lock: {}", lockId);
            return false;
        }
    }

    /**
     * Renew lock with default additional time.
     */
    public boolean renewLock(Long lockId) {
        return renewLock(lockId, DEFAULT_LOCK_DURATION_MINUTES);
    }

    /**
     * Renew lock by resource name and holder.
     */
    public boolean renewLock(String resourceName, String holder, int additionalMinutes) {
        Optional<Lock> lock = lockRepository.findValidLock(holder, resourceName);
        if (lock.isPresent()) {
            return renewLock(lock.get().getId(), additionalMinutes);
        }
        return false;
    }

    // Lock release

    /**
     * Release a specific lock.
     */
    public boolean releaseLock(Long lockId) {
        logger.debug("Releasing lock with ID: {}", lockId);
        
        Optional<Lock> lockOpt = lockRepository.findById(lockId);
        if (lockOpt.isEmpty()) {
            logger.warn("Lock not found for release: {}", lockId);
            return false;
        }
        
        Lock lock = lockOpt.get();
        lock.release();
        
        try {
            lockRepository.save(lock);
            logger.info("Successfully released lock: {}", lockId);
            return true;
        } catch (Exception e) {
            logger.error("Failed to release lock: {}", lockId, e);
            return false;
        }
    }

    /**
     * Release lock by resource name and holder.
     */
    public boolean releaseLock(String resourceName, String holder) {
        logger.debug("Releasing lock for resource '{}' by holder '{}'", resourceName, holder);
        
        int released = lockRepository.releaseLockForResource(resourceName, holder);
        if (released > 0) {
            logger.info("Successfully released lock for resource '{}' by holder '{}'", resourceName, holder);
            return true;
        } else {
            logger.warn("No lock found to release for resource '{}' by holder '{}'", resourceName, holder);
            return false;
        }
    }

    /**
     * Release all locks for a holder.
     */
    public int releaseAllLocks(String holder) {
        logger.info("Releasing all locks for holder: {}", holder);
        
        int released = lockRepository.releaseAllLocksForHolder(holder);
        logger.info("Released {} locks for holder: {}", released, holder);
        return released;
    }

    // Lock validation and checking

    /**
     * Check if a resource is currently locked.
     */
    @Transactional(readOnly = true)
    public boolean isResourceLocked(String resourceName) {
        return lockRepository.isResourceLocked(resourceName);
    }

    /**
     * Check if a resource is locked by a specific holder.
     */
    @Transactional(readOnly = true)
    public boolean isResourceLockedByHolder(String resourceName, String holder) {
        return lockRepository.isResourceLockedByHolder(resourceName, holder);
    }

    /**
     * Get active lock for a resource.
     */
    @Transactional(readOnly = true)
    public Optional<Lock> getActiveLock(String resourceName) {
        return lockRepository.findActiveLockByResource(resourceName);
    }

    /**
     * Validate if a holder can access a resource.
     */
    @Transactional(readOnly = true)
    public boolean canAccessResource(String resourceName, String holder) {
        Optional<Lock> activeLock = getActiveLock(resourceName);
        return activeLock.isEmpty() || activeLock.get().getHolder().equals(holder);
    }

    // Lock monitoring and statistics

    /**
     * Get all active locks.
     */
    @Transactional(readOnly = true)
    public List<Lock> getActiveLocks() {
        return lockRepository.findActiveLocks();
    }

    /**
     * Get active locks for a holder.
     */
    @Transactional(readOnly = true)
    public List<Lock> getActiveLocksForHolder(String holder) {
        return lockRepository.findActiveLocksForHolder(holder);
    }

    /**
     * Get locks expiring soon.
     */
    @Transactional(readOnly = true)
    public List<Lock> getLocksExpiringSoon(int warningMinutes) {
        LocalDateTime warningTime = LocalDateTime.now().plusMinutes(warningMinutes);
        return lockRepository.findLocksExpiringSoon(warningTime);
    }

    /**
     * Get locks nearing expiration with default warning time.
     */
    @Transactional(readOnly = true)
    public List<Lock> getLocksNearingExpiration() {
        LocalDateTime warningTime = LocalDateTime.now().plusMinutes(5);
        return lockRepository.findLocksNearingExpiration(warningTime);
    }

    /**
     * Get long-running locks.
     */
    @Transactional(readOnly = true)
    public List<Lock> getLongRunningLocks(int hours) {
        return lockRepository.findLongRunningLocks(hours);
    }

    /**
     * Get lock statistics.
     */
    @Transactional(readOnly = true)
    public LockStatistics getLockStatistics() {
        LockStatistics stats = new LockStatistics();
        stats.setActiveCount(lockRepository.countActiveLocks());
        stats.setExpiredCount(lockRepository.countExpiredLocks());
        stats.setAverageRenewalCount(lockRepository.getAverageRenewalCount());
        return stats;
    }

    /**
     * Get lock counts by type.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getLockCountsByType() {
        return lockRepository.countActiveLocksByType();
    }

    /**
     * Get lock counts by holder.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getLockCountsByHolder() {
        return lockRepository.countActiveLocksByHolder();
    }

    // Maintenance operations

    /**
     * Clean up expired locks.
     */
    public int cleanupExpiredLocks() {
        logger.info("Cleaning up expired locks");
        
        int deactivated = lockRepository.deactivateExpiredLocks();
        logger.info("Deactivated {} expired locks", deactivated);
        
        return deactivated;
    }

    /**
     * Delete old inactive locks.
     */
    public int deleteOldInactiveLocks(int days) {
        logger.info("Deleting inactive locks older than {} days", days);
        
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        int deleted = lockRepository.deleteOldInactiveLocks(cutoff);
        logger.info("Deleted {} old inactive locks", deleted);
        
        return deleted;
    }

    /**
     * Find and resolve conflicting locks.
     */
    @Transactional(readOnly = true)
    public List<Lock> findConflictingLocks(String resourceName, String holder) {
        return lockRepository.findConflictingLocks(resourceName, holder);
    }

    /**
     * Find duplicate active locks.
     */
    @Transactional(readOnly = true)
    public List<Lock> findDuplicateActiveLocks() {
        return lockRepository.findDuplicateActiveLocks();
    }

    // Search and filtering

    /**
     * Search locks by term.
     */
    @Transactional(readOnly = true)
    public List<Lock> searchLocks(String searchTerm) {
        return lockRepository.searchLocks(searchTerm);
    }

    /**
     * Search active locks by term.
     */
    @Transactional(readOnly = true)
    public List<Lock> searchActiveLocks(String searchTerm) {
        return lockRepository.searchActiveLocks(searchTerm);
    }

    /**
     * Find locks by purpose.
     */
    @Transactional(readOnly = true)
    public List<Lock> findLocksByPurpose(String purposePattern) {
        return lockRepository.findLocksByPurpose(purposePattern);
    }

    /**
     * Get locks by type.
     */
    @Transactional(readOnly = true)
    public List<Lock> getLocksByType(String lockType) {
        return lockRepository.findByLockType(lockType);
    }

    // Lock performance monitoring

    /**
     * Get average lock duration by type.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAverageLockDurationByType() {
        return lockRepository.getAverageLockDurationByType();
    }

    /**
     * Get lock usage statistics by holder.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getLockUsageStatsByHolder() {
        return lockRepository.getLockUsageStatsByHolder();
    }

    // Helper methods

    /**
     * Get lock by ID.
     */
    @Transactional(readOnly = true)
    public Optional<Lock> getLockById(Long id) {
        return lockRepository.findById(id);
    }

    /**
     * Get total lock count.
     */
    @Transactional(readOnly = true)
    public long getTotalLockCount() {
        return lockRepository.count();
    }

    // Inner class for statistics
    public static class LockStatistics {
        private long activeCount;
        private long expiredCount;
        private Double averageRenewalCount;

        // Getters and setters
        public long getActiveCount() { return activeCount; }
        public void setActiveCount(long activeCount) { this.activeCount = activeCount; }

        public long getExpiredCount() { return expiredCount; }
        public void setExpiredCount(long expiredCount) { this.expiredCount = expiredCount; }

        public Double getAverageRenewalCount() { return averageRenewalCount; }
        public void setAverageRenewalCount(Double averageRenewalCount) { this.averageRenewalCount = averageRenewalCount; }

        public long getTotalCount() { return activeCount + expiredCount; }
        
        public double getActivePercentage() {
            long total = getTotalCount();
            return total > 0 ? (double) activeCount / total * 100.0 : 0.0;
        }
    }
}