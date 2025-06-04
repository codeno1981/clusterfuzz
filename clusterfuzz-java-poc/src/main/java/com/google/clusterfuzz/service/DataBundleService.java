package com.google.clusterfuzz.service;

import com.google.clusterfuzz.datastore.model.DataBundle;
import com.google.clusterfuzz.datastore.repository.DataBundleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing DataBundle entities.
 * Provides business logic for fuzzer data bundle management.
 */
@Service
@Transactional
public class DataBundleService {

    private final DataBundleRepository repository;
    
    // Configuration constants
    private static final int DEFAULT_STALE_DAYS = 30;
    private static final int DEFAULT_CLEANUP_DAYS = 90;
    private static final long DEFAULT_HIGH_USAGE_THRESHOLD = 100L;
    private static final long DEFAULT_LARGE_BUNDLE_THRESHOLD = 1024L * 1024L * 1024L; // 1GB

    @Autowired
    public DataBundleService(DataBundleRepository repository) {
        this.repository = repository;
    }

    /**
     * Create a new data bundle.
     */
    public DataBundle createDataBundle(String name, String bucketName, Boolean syncToWorker) {
        if (repository.existsByName(name)) {
            throw new IllegalArgumentException("Data bundle with name already exists: " + name);
        }
        
        DataBundle bundle = new DataBundle(name, bucketName, syncToWorker);
        return repository.save(bundle);
    }

    /**
     * Create a data bundle with full metadata.
     */
    public DataBundle createDataBundle(String name, String bucketName, Boolean syncToWorker,
                                       String description, String version, String tags) {
        DataBundle bundle = createDataBundle(name, bucketName, syncToWorker);
        bundle.setDescription(description);
        bundle.setVersion(version);
        bundle.setTags(tags);
        
        return repository.save(bundle);
    }

    /**
     * Get data bundle by name.
     */
    @Transactional(readOnly = true)
    public Optional<DataBundle> getDataBundleByName(String name) {
        return repository.findByName(name);
    }

    /**
     * Get data bundle by ID.
     */
    @Transactional(readOnly = true)
    public Optional<DataBundle> getDataBundleById(Long id) {
        return repository.findById(id);
    }

    /**
     * Get all active data bundles.
     */
    @Transactional(readOnly = true)
    public List<DataBundle> getActiveDataBundles() {
        return repository.findByActiveTrue();
    }

    /**
     * Get active data bundles by sync preference.
     */
    @Transactional(readOnly = true)
    public List<DataBundle> getActiveDataBundles(Boolean syncToWorker) {
        return repository.findActiveBySyncPreference(syncToWorker);
    }

    /**
     * Get data bundles by bucket.
     */
    @Transactional(readOnly = true)
    public List<DataBundle> getDataBundlesByBucket(String bucketName) {
        return repository.findByBucketName(bucketName);
    }

    /**
     * Search data bundles with filters.
     */
    @Transactional(readOnly = true)
    public Page<DataBundle> searchDataBundles(String name, String bucketName, Boolean active,
                                              Boolean syncToWorker, String version, Pageable pageable) {
        return repository.searchDataBundles(name, bucketName, active, syncToWorker, version, pageable);
    }

    /**
     * Search data bundles by keyword.
     */
    @Transactional(readOnly = true)
    public List<DataBundle> searchByKeyword(String keyword) {
        return repository.searchByKeyword(keyword);
    }

    /**
     * Update data bundle metadata after sync/upload.
     */
    public DataBundle updateBundleMetadata(Long bundleId, Long sizeBytes, Integer fileCount, String checksum) {
        Optional<DataBundle> bundleOpt = repository.findById(bundleId);
        if (bundleOpt.isEmpty()) {
            throw new IllegalArgumentException("Data bundle not found: " + bundleId);
        }
        
        DataBundle bundle = bundleOpt.get();
        bundle.updateMetadata(sizeBytes, fileCount, checksum);
        return repository.save(bundle);
    }

    /**
     * Mark data bundle as accessed.
     */
    public DataBundle markAccessed(Long bundleId) {
        Optional<DataBundle> bundleOpt = repository.findById(bundleId);
        if (bundleOpt.isEmpty()) {
            throw new IllegalArgumentException("Data bundle not found: " + bundleId);
        }
        
        DataBundle bundle = bundleOpt.get();
        bundle.markAccessed();
        return repository.save(bundle);
    }

    /**
     * Mark data bundle as accessed by name.
     */
    public DataBundle markAccessed(String name) {
        Optional<DataBundle> bundleOpt = repository.findByName(name);
        if (bundleOpt.isEmpty()) {
            throw new IllegalArgumentException("Data bundle not found: " + name);
        }
        
        DataBundle bundle = bundleOpt.get();
        bundle.markAccessed();
        return repository.save(bundle);
    }

    /**
     * Activate/deactivate data bundle.
     */
    public DataBundle setActive(Long bundleId, boolean active) {
        Optional<DataBundle> bundleOpt = repository.findById(bundleId);
        if (bundleOpt.isEmpty()) {
            throw new IllegalArgumentException("Data bundle not found: " + bundleId);
        }
        
        DataBundle bundle = bundleOpt.get();
        bundle.setActive(active);
        return repository.save(bundle);
    }

    /**
     * Update data bundle sync preference.
     */
    public DataBundle setSyncToWorker(Long bundleId, boolean syncToWorker) {
        Optional<DataBundle> bundleOpt = repository.findById(bundleId);
        if (bundleOpt.isEmpty()) {
            throw new IllegalArgumentException("Data bundle not found: " + bundleId);
        }
        
        DataBundle bundle = bundleOpt.get();
        bundle.setSyncToWorker(syncToWorker);
        return repository.save(bundle);
    }

    /**
     * Add tag to data bundle.
     */
    public DataBundle addTag(Long bundleId, String tag) {
        Optional<DataBundle> bundleOpt = repository.findById(bundleId);
        if (bundleOpt.isEmpty()) {
            throw new IllegalArgumentException("Data bundle not found: " + bundleId);
        }
        
        DataBundle bundle = bundleOpt.get();
        bundle.addTag(tag);
        return repository.save(bundle);
    }

    /**
     * Remove tag from data bundle.
     */
    public DataBundle removeTag(Long bundleId, String tag) {
        Optional<DataBundle> bundleOpt = repository.findById(bundleId);
        if (bundleOpt.isEmpty()) {
            throw new IllegalArgumentException("Data bundle not found: " + bundleId);
        }
        
        DataBundle bundle = bundleOpt.get();
        bundle.removeTag(tag);
        return repository.save(bundle);
    }

    /**
     * Get data bundles by tag.
     */
    @Transactional(readOnly = true)
    public List<DataBundle> getDataBundlesByTag(String tag) {
        return repository.findByTag(tag);
    }

    /**
     * Get stale data bundles.
     */
    @Transactional(readOnly = true)
    public List<DataBundle> getStaleDataBundles() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(DEFAULT_STALE_DAYS);
        return repository.findStaleDataBundles(cutoff);
    }

    /**
     * Get stale data bundles with custom threshold.
     */
    @Transactional(readOnly = true)
    public List<DataBundle> getStaleDataBundles(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        return repository.findStaleDataBundles(cutoff);
    }

    /**
     * Get unused data bundles.
     */
    @Transactional(readOnly = true)
    public List<DataBundle> getUnusedDataBundles() {
        return repository.findUnusedBundles();
    }

    /**
     * Get high usage data bundles.
     */
    @Transactional(readOnly = true)
    public List<DataBundle> getHighUsageDataBundles() {
        return repository.findHighUsageBundles(DEFAULT_HIGH_USAGE_THRESHOLD);
    }

    /**
     * Get large data bundles.
     */
    @Transactional(readOnly = true)
    public List<DataBundle> getLargeDataBundles() {
        return repository.findLargeBundles(DEFAULT_LARGE_BUNDLE_THRESHOLD);
    }

    /**
     * Get data bundle statistics.
     */
    @Transactional(readOnly = true)
    public DataBundleStatistics getDataBundleStatistics() {
        long totalBundles = repository.count();
        long activeBundles = repository.countByActiveTrue();
        long inactiveBundles = repository.countByActiveFalse();
        long syncToWorkerBundles = repository.countBySyncToWorkerTrue();
        long syncToHostBundles = repository.countBySyncToWorkerFalse();
        long neverAccessedBundles = repository.countNeverAccessed();
        long withSizeInfo = repository.countWithSizeInfo();
        long withChecksum = repository.countWithChecksum();
        
        Long totalSize = repository.getTotalActiveBundleSize();
        Double averageSize = repository.getAverageBundleSize();
        Double averageFileCount = repository.getAverageFileCount();
        Double averageUsage = repository.getAverageUsageCount();
        Long maxUsage = repository.getMaxUsageCount();
        
        List<Object[]> bucketStats = repository.getBucketStatistics();
        List<Object[]> versionStats = repository.getVersionStatistics();
        List<Object[]> syncStats = repository.getSyncPreferenceStatistics();
        List<Object[]> activeStats = repository.getActiveStatusStatistics();
        
        return new DataBundleStatistics(totalBundles, activeBundles, inactiveBundles,
                                        syncToWorkerBundles, syncToHostBundles, neverAccessedBundles,
                                        withSizeInfo, withChecksum, totalSize, averageSize,
                                        averageFileCount, averageUsage, maxUsage, bucketStats,
                                        versionStats, syncStats, activeStats);
    }

    /**
     * Get data bundles created recently.
     */
    @Transactional(readOnly = true)
    public List<DataBundle> getRecentDataBundles(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return repository.findCreatedSince(since);
    }

    /**
     * Get data bundles updated recently.
     */
    @Transactional(readOnly = true)
    public List<DataBundle> getRecentlyUpdatedDataBundles(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return repository.findUpdatedSince(since);
    }

    /**
     * Get data bundles accessed recently.
     */
    @Transactional(readOnly = true)
    public List<DataBundle> getRecentlyAccessedDataBundles(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return repository.findAccessedSince(since);
    }

    /**
     * Get creation trend data.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getCreationTrend(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return repository.getCreationTrend(since);
    }

    /**
     * Get access trend data.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAccessTrend(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return repository.getAccessTrend(since);
    }

    /**
     * Clean up old inactive data bundles.
     */
    public int cleanupOldDataBundles() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(DEFAULT_CLEANUP_DAYS);
        List<DataBundle> oldBundles = repository.findInactiveAndStale(cutoff);
        
        repository.deleteAll(oldBundles);
        return oldBundles.size();
    }

    /**
     * Clean up unused old data bundles.
     */
    public int cleanupUnusedDataBundles() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(DEFAULT_CLEANUP_DAYS);
        List<DataBundle> unusedBundles = repository.findUnusedAndOld(cutoff);
        
        repository.deleteAll(unusedBundles);
        return unusedBundles.size();
    }

    /**
     * Get data bundles with incomplete metadata.
     */
    @Transactional(readOnly = true)
    public List<DataBundle> getDataBundlesWithIncompleteMetadata() {
        return repository.findIncompleteMetadata();
    }

    /**
     * Update data bundle.
     */
    public DataBundle updateDataBundle(Long bundleId, DataBundle updates) {
        Optional<DataBundle> bundleOpt = repository.findById(bundleId);
        if (bundleOpt.isEmpty()) {
            throw new IllegalArgumentException("Data bundle not found: " + bundleId);
        }
        
        DataBundle bundle = bundleOpt.get();
        
        // Update allowed fields
        if (updates.getDescription() != null) {
            bundle.setDescription(updates.getDescription());
        }
        if (updates.getVersion() != null) {
            bundle.setVersion(updates.getVersion());
        }
        if (updates.getTags() != null) {
            bundle.setTags(updates.getTags());
        }
        if (updates.getActive() != null) {
            bundle.setActive(updates.getActive());
        }
        if (updates.getSyncToWorker() != null) {
            bundle.setSyncToWorker(updates.getSyncToWorker());
        }
        
        return repository.save(bundle);
    }

    /**
     * Delete data bundle.
     */
    public void deleteDataBundle(Long bundleId) {
        if (!repository.existsById(bundleId)) {
            throw new IllegalArgumentException("Data bundle not found: " + bundleId);
        }
        repository.deleteById(bundleId);
    }

    /**
     * Check if data bundle name exists.
     */
    @Transactional(readOnly = true)
    public boolean dataBundle NameExists(String name) {
        return repository.existsByName(name);
    }

    /**
     * Get all data bundles with pagination.
     */
    @Transactional(readOnly = true)
    public Page<DataBundle> getAllDataBundles(Pageable pageable) {
        return repository.findAll(pageable);
    }

    /**
     * Get distinct bucket names.
     */
    @Transactional(readOnly = true)
    public List<String> getDistinctBucketNames() {
        return repository.findDistinctBucketNames();
    }

    /**
     * Get distinct versions.
     */
    @Transactional(readOnly = true)
    public List<String> getDistinctVersions() {
        return repository.findDistinctVersions();
    }

    /**
     * Statistics container class.
     */
    public static class DataBundleStatistics {
        private final long totalBundles;
        private final long activeBundles;
        private final long inactiveBundles;
        private final long syncToWorkerBundles;
        private final long syncToHostBundles;
        private final long neverAccessedBundles;
        private final long withSizeInfo;
        private final long withChecksum;
        private final Long totalSize;
        private final Double averageSize;
        private final Double averageFileCount;
        private final Double averageUsage;
        private final Long maxUsage;
        private final List<Object[]> bucketStatistics;
        private final List<Object[]> versionStatistics;
        private final List<Object[]> syncStatistics;
        private final List<Object[]> activeStatistics;

        public DataBundleStatistics(long totalBundles, long activeBundles, long inactiveBundles,
                                    long syncToWorkerBundles, long syncToHostBundles, long neverAccessedBundles,
                                    long withSizeInfo, long withChecksum, Long totalSize, Double averageSize,
                                    Double averageFileCount, Double averageUsage, Long maxUsage,
                                    List<Object[]> bucketStatistics, List<Object[]> versionStatistics,
                                    List<Object[]> syncStatistics, List<Object[]> activeStatistics) {
            this.totalBundles = totalBundles;
            this.activeBundles = activeBundles;
            this.inactiveBundles = inactiveBundles;
            this.syncToWorkerBundles = syncToWorkerBundles;
            this.syncToHostBundles = syncToHostBundles;
            this.neverAccessedBundles = neverAccessedBundles;
            this.withSizeInfo = withSizeInfo;
            this.withChecksum = withChecksum;
            this.totalSize = totalSize;
            this.averageSize = averageSize;
            this.averageFileCount = averageFileCount;
            this.averageUsage = averageUsage;
            this.maxUsage = maxUsage;
            this.bucketStatistics = bucketStatistics;
            this.versionStatistics = versionStatistics;
            this.syncStatistics = syncStatistics;
            this.activeStatistics = activeStatistics;
        }

        // Getters
        public long getTotalBundles() { return totalBundles; }
        public long getActiveBundles() { return activeBundles; }
        public long getInactiveBundles() { return inactiveBundles; }
        public long getSyncToWorkerBundles() { return syncToWorkerBundles; }
        public long getSyncToHostBundles() { return syncToHostBundles; }
        public long getNeverAccessedBundles() { return neverAccessedBundles; }
        public long getWithSizeInfo() { return withSizeInfo; }
        public long getWithChecksum() { return withChecksum; }
        public Long getTotalSize() { return totalSize; }
        public Double getAverageSize() { return averageSize; }
        public Double getAverageFileCount() { return averageFileCount; }
        public Double getAverageUsage() { return averageUsage; }
        public Long getMaxUsage() { return maxUsage; }
        public List<Object[]> getBucketStatistics() { return bucketStatistics; }
        public List<Object[]> getVersionStatistics() { return versionStatistics; }
        public List<Object[]> getSyncStatistics() { return syncStatistics; }
        public List<Object[]> getActiveStatistics() { return activeStatistics; }
    }
}