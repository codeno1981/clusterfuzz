package com.google.clusterfuzz.service;

import com.google.clusterfuzz.datastore.model.TestcaseUploadMetadata;
import com.google.clusterfuzz.datastore.repository.TestcaseUploadMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing TestcaseUploadMetadata entities.
 * Provides business logic for testcase upload tracking and processing.
 */
@Service
@Transactional
public class TestcaseUploadMetadataService {

    private final TestcaseUploadMetadataRepository repository;
    
    // Configuration constants
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final int DEFAULT_PROCESSING_TIMEOUT_HOURS = 24;
    private static final int DEFAULT_STALE_PENDING_HOURS = 6;
    private static final int DEFAULT_CLEANUP_DAYS = 30;

    @Autowired
    public TestcaseUploadMetadataService(TestcaseUploadMetadataRepository repository) {
        this.repository = repository;
    }

    /**
     * Create a new upload metadata record.
     */
    public TestcaseUploadMetadata createUpload(String filename, String uploaderEmail, 
                                               String blobstoreKey, Long fileSize, String mimeType) {
        TestcaseUploadMetadata upload = new TestcaseUploadMetadata(filename, uploaderEmail);
        upload.setBlobstoreKey(blobstoreKey);
        upload.setFileSize(fileSize);
        upload.setMimeType(mimeType);
        
        return repository.save(upload);
    }

    /**
     * Create a bundled upload metadata record.
     */
    public TestcaseUploadMetadata createBundledUpload(String filename, String uploaderEmail,
                                                      String originalBlobstoreKey, String pathInArchive) {
        TestcaseUploadMetadata upload = new TestcaseUploadMetadata(filename, uploaderEmail);
        upload.setBundled(true);
        upload.setOriginalBlobstoreKey(originalBlobstoreKey);
        upload.setPathInArchive(pathInArchive);
        
        return repository.save(upload);
    }

    /**
     * Start processing an upload.
     */
    public TestcaseUploadMetadata startProcessing(Long uploadId, String botName) {
        Optional<TestcaseUploadMetadata> uploadOpt = repository.findById(uploadId);
        if (uploadOpt.isEmpty()) {
            throw new IllegalArgumentException("Upload not found: " + uploadId);
        }
        
        TestcaseUploadMetadata upload = uploadOpt.get();
        if (!upload.isPending()) {
            throw new IllegalStateException("Upload is not in pending state: " + upload.getStatus());
        }
        
        upload.startProcessing(botName);
        return repository.save(upload);
    }

    /**
     * Mark upload as successfully processed.
     */
    public TestcaseUploadMetadata markProcessed(Long uploadId, Long testcaseId) {
        Optional<TestcaseUploadMetadata> uploadOpt = repository.findById(uploadId);
        if (uploadOpt.isEmpty()) {
            throw new IllegalArgumentException("Upload not found: " + uploadId);
        }
        
        TestcaseUploadMetadata upload = uploadOpt.get();
        upload.markProcessed(testcaseId);
        return repository.save(upload);
    }

    /**
     * Mark upload as duplicate.
     */
    public TestcaseUploadMetadata markDuplicate(Long uploadId, Long duplicateOfId) {
        Optional<TestcaseUploadMetadata> uploadOpt = repository.findById(uploadId);
        if (uploadOpt.isEmpty()) {
            throw new IllegalArgumentException("Upload not found: " + uploadId);
        }
        
        TestcaseUploadMetadata upload = uploadOpt.get();
        upload.markDuplicate(duplicateOfId);
        return repository.save(upload);
    }

    /**
     * Mark upload as failed with retry logic.
     */
    public TestcaseUploadMetadata markFailed(Long uploadId, String errorMessage, boolean allowRetry) {
        Optional<TestcaseUploadMetadata> uploadOpt = repository.findById(uploadId);
        if (uploadOpt.isEmpty()) {
            throw new IllegalArgumentException("Upload not found: " + uploadId);
        }
        
        TestcaseUploadMetadata upload = uploadOpt.get();
        
        if (allowRetry && !upload.hasExceededMaxRetries(DEFAULT_MAX_RETRIES)) {
            upload.incrementRetries();
            upload.setStatus(TestcaseUploadMetadata.UploadStatus.PENDING);
            upload.setErrorMessage(errorMessage);
        } else {
            upload.markFailed(errorMessage);
        }
        
        return repository.save(upload);
    }

    /**
     * Mark upload as timed out.
     */
    public TestcaseUploadMetadata markTimeout(Long uploadId) {
        Optional<TestcaseUploadMetadata> uploadOpt = repository.findById(uploadId);
        if (uploadOpt.isEmpty()) {
            throw new IllegalArgumentException("Upload not found: " + uploadId);
        }
        
        TestcaseUploadMetadata upload = uploadOpt.get();
        upload.markTimeout();
        return repository.save(upload);
    }

    /**
     * Mark upload as invalid.
     */
    public TestcaseUploadMetadata markInvalid(Long uploadId, String reason) {
        Optional<TestcaseUploadMetadata> uploadOpt = repository.findById(uploadId);
        if (uploadOpt.isEmpty()) {
            throw new IllegalArgumentException("Upload not found: " + uploadId);
        }
        
        TestcaseUploadMetadata upload = uploadOpt.get();
        upload.markInvalid(reason);
        return repository.save(upload);
    }

    /**
     * Get next pending upload for processing.
     */
    @Transactional(readOnly = true)
    public Optional<TestcaseUploadMetadata> getNextPendingUpload() {
        List<TestcaseUploadMetadata> pending = repository.findPendingUploadsOrderByTimestamp();
        return pending.isEmpty() ? Optional.empty() : Optional.of(pending.get(0));
    }

    /**
     * Get uploads that are stuck in processing state.
     */
    @Transactional(readOnly = true)
    public List<TestcaseUploadMetadata> getStuckProcessingUploads() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(DEFAULT_PROCESSING_TIMEOUT_HOURS);
        return repository.findStuckProcessing(cutoff);
    }

    /**
     * Get stale pending uploads.
     */
    @Transactional(readOnly = true)
    public List<TestcaseUploadMetadata> getStalePendingUploads() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(DEFAULT_STALE_PENDING_HOURS);
        return repository.findStalePendingUploads(cutoff);
    }

    /**
     * Get uploads that can be retried.
     */
    @Transactional(readOnly = true)
    public List<TestcaseUploadMetadata> getRetryableFailures() {
        return repository.findRetryableFailures(DEFAULT_MAX_RETRIES);
    }

    /**
     * Retry a failed upload.
     */
    public TestcaseUploadMetadata retryUpload(Long uploadId) {
        Optional<TestcaseUploadMetadata> uploadOpt = repository.findById(uploadId);
        if (uploadOpt.isEmpty()) {
            throw new IllegalArgumentException("Upload not found: " + uploadId);
        }
        
        TestcaseUploadMetadata upload = uploadOpt.get();
        if (upload.getStatus() != TestcaseUploadMetadata.UploadStatus.FAILED) {
            throw new IllegalStateException("Upload is not in failed state");
        }
        
        if (upload.hasExceededMaxRetries(DEFAULT_MAX_RETRIES)) {
            throw new IllegalStateException("Upload has exceeded maximum retries");
        }
        
        upload.incrementRetries();
        upload.setStatus(TestcaseUploadMetadata.UploadStatus.PENDING);
        upload.setErrorMessage(null);
        upload.setProcessingStartedAt(null);
        upload.setProcessingCompletedAt(null);
        
        return repository.save(upload);
    }

    /**
     * Search uploads with filters.
     */
    @Transactional(readOnly = true)
    public Page<TestcaseUploadMetadata> searchUploads(String uploaderEmail, String filename,
                                                      TestcaseUploadMetadata.UploadStatus status,
                                                      Boolean securityFlag, Boolean bundled,
                                                      Pageable pageable) {
        return repository.searchUploads(uploaderEmail, filename, status, securityFlag, bundled, pageable);
    }

    /**
     * Get uploads by uploader.
     */
    @Transactional(readOnly = true)
    public List<TestcaseUploadMetadata> getUploadsByUploader(String uploaderEmail) {
        return repository.findByUploaderEmail(uploaderEmail);
    }

    /**
     * Get recent uploads.
     */
    @Transactional(readOnly = true)
    public List<TestcaseUploadMetadata> getRecentUploads(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return repository.findRecentUploads(since);
    }

    /**
     * Get upload statistics.
     */
    @Transactional(readOnly = true)
    public UploadStatistics getUploadStatistics() {
        List<Object[]> statusStats = repository.getStatusStatistics();
        List<Object[]> uploaderStats = repository.getUploaderStatistics();
        List<Object[]> botStats = repository.getBotStatistics();
        Double avgProcessingTime = repository.getAverageProcessingTimeSeconds();
        
        long totalUploads = repository.count();
        long securityUploads = repository.countBySecurityFlagTrue();
        long bundledUploads = repository.countByBundledTrue();
        long uploadsWithRetries = repository.countUploadsWithRetries();
        
        return new UploadStatistics(statusStats, uploaderStats, botStats, 
                                    avgProcessingTime, totalUploads, securityUploads, 
                                    bundledUploads, uploadsWithRetries);
    }

    /**
     * Clean up old completed uploads.
     */
    public int cleanupOldUploads() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(DEFAULT_CLEANUP_DAYS);
        List<TestcaseUploadMetadata> oldUploads = repository.findOldCompletedUploads(cutoff);
        
        repository.deleteAll(oldUploads);
        return oldUploads.size();
    }

    /**
     * Handle stuck processing uploads.
     */
    public int handleStuckProcessing() {
        List<TestcaseUploadMetadata> stuckUploads = getStuckProcessingUploads();
        int handled = 0;
        
        for (TestcaseUploadMetadata upload : stuckUploads) {
            if (!upload.hasExceededMaxRetries(DEFAULT_MAX_RETRIES)) {
                upload.incrementRetries();
                upload.setStatus(TestcaseUploadMetadata.UploadStatus.PENDING);
                upload.setProcessingStartedAt(null);
                upload.setErrorMessage("Processing timed out, retrying");
                repository.save(upload);
                handled++;
            } else {
                upload.markTimeout();
                repository.save(upload);
                handled++;
            }
        }
        
        return handled;
    }

    /**
     * Get upload by testcase ID.
     */
    @Transactional(readOnly = true)
    public Optional<TestcaseUploadMetadata> getUploadByTestcaseId(Long testcaseId) {
        return repository.findByTestcaseId(testcaseId);
    }

    /**
     * Get upload by blobstore key.
     */
    @Transactional(readOnly = true)
    public Optional<TestcaseUploadMetadata> getUploadByBlobstoreKey(String blobstoreKey) {
        return repository.findByBlobstoreKey(blobstoreKey);
    }

    /**
     * Check if blobstore key exists.
     */
    @Transactional(readOnly = true)
    public boolean blobstoreKeyExists(String blobstoreKey) {
        return repository.existsByBlobstoreKey(blobstoreKey);
    }

    /**
     * Get duplicate uploads.
     */
    @Transactional(readOnly = true)
    public List<TestcaseUploadMetadata> getDuplicateUploads() {
        return repository.findDuplicateUploads();
    }

    /**
     * Get file extension statistics.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getFileExtensionStatistics() {
        return repository.getFileExtensionStatistics();
    }

    /**
     * Get upload trend data.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getUploadTrend(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return repository.getUploadTrend(since);
    }

    /**
     * Update upload metadata.
     */
    public TestcaseUploadMetadata updateUpload(Long uploadId, TestcaseUploadMetadata updates) {
        Optional<TestcaseUploadMetadata> uploadOpt = repository.findById(uploadId);
        if (uploadOpt.isEmpty()) {
            throw new IllegalArgumentException("Upload not found: " + uploadId);
        }
        
        TestcaseUploadMetadata upload = uploadOpt.get();
        
        // Update allowed fields
        if (updates.getTimeout() != null) {
            upload.setTimeout(updates.getTimeout());
        }
        if (updates.getBugSummaryUpdateFlag() != null) {
            upload.setBugSummaryUpdateFlag(updates.getBugSummaryUpdateFlag());
        }
        if (updates.getQuietFlag() != null) {
            upload.setQuietFlag(updates.getQuietFlag());
        }
        if (updates.getAdditionalMetadata() != null) {
            upload.setAdditionalMetadata(updates.getAdditionalMetadata());
        }
        if (updates.getBugInformation() != null) {
            upload.setBugInformation(updates.getBugInformation());
        }
        
        return repository.save(upload);
    }

    /**
     * Delete upload.
     */
    public void deleteUpload(Long uploadId) {
        if (!repository.existsById(uploadId)) {
            throw new IllegalArgumentException("Upload not found: " + uploadId);
        }
        repository.deleteById(uploadId);
    }

    /**
     * Get upload by ID.
     */
    @Transactional(readOnly = true)
    public Optional<TestcaseUploadMetadata> getUploadById(Long uploadId) {
        return repository.findById(uploadId);
    }

    /**
     * Get all uploads with pagination.
     */
    @Transactional(readOnly = true)
    public Page<TestcaseUploadMetadata> getAllUploads(Pageable pageable) {
        return repository.findAll(pageable);
    }

    /**
     * Statistics container class.
     */
    public static class UploadStatistics {
        private final List<Object[]> statusStatistics;
        private final List<Object[]> uploaderStatistics;
        private final List<Object[]> botStatistics;
        private final Double averageProcessingTimeSeconds;
        private final long totalUploads;
        private final long securityUploads;
        private final long bundledUploads;
        private final long uploadsWithRetries;

        public UploadStatistics(List<Object[]> statusStatistics, List<Object[]> uploaderStatistics,
                                List<Object[]> botStatistics, Double averageProcessingTimeSeconds,
                                long totalUploads, long securityUploads, long bundledUploads,
                                long uploadsWithRetries) {
            this.statusStatistics = statusStatistics;
            this.uploaderStatistics = uploaderStatistics;
            this.botStatistics = botStatistics;
            this.averageProcessingTimeSeconds = averageProcessingTimeSeconds;
            this.totalUploads = totalUploads;
            this.securityUploads = securityUploads;
            this.bundledUploads = bundledUploads;
            this.uploadsWithRetries = uploadsWithRetries;
        }

        // Getters
        public List<Object[]> getStatusStatistics() { return statusStatistics; }
        public List<Object[]> getUploaderStatistics() { return uploaderStatistics; }
        public List<Object[]> getBotStatistics() { return botStatistics; }
        public Double getAverageProcessingTimeSeconds() { return averageProcessingTimeSeconds; }
        public long getTotalUploads() { return totalUploads; }
        public long getSecurityUploads() { return securityUploads; }
        public long getBundledUploads() { return bundledUploads; }
        public long getUploadsWithRetries() { return uploadsWithRetries; }
    }
}