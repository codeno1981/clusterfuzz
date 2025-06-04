package com.google.clusterfuzz.service;

import com.google.clusterfuzz.datastore.model.Testcase;
import com.google.clusterfuzz.datastore.model.SecuritySeverity;
import com.google.clusterfuzz.datastore.repository.TestcaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing testcases.
 * Implements business logic for testcase operations.
 */
@Service
@Transactional
public class TestcaseService {

    private final TestcaseRepository testcaseRepository;

    @Autowired
    public TestcaseService(TestcaseRepository testcaseRepository) {
        this.testcaseRepository = testcaseRepository;
    }

    /**
     * Create a new testcase.
     */
    public Testcase createTestcase(Testcase testcase) {
        // Validate required fields
        if (testcase.getFuzzerName() == null || testcase.getFuzzerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Fuzzer name is required");
        }
        
        if (testcase.getJobType() == null || testcase.getJobType().trim().isEmpty()) {
            throw new IllegalArgumentException("Job type is required");
        }

        // Set default values
        if (testcase.getStatus() == null) {
            testcase.setStatus("Processed");
        }
        
        if (testcase.getOpen() == null) {
            testcase.setOpen(true);
        }

        // Populate search indices
        testcase.populateIndices();

        return testcaseRepository.save(testcase);
    }

    /**
     * Update an existing testcase.
     */
    public Testcase updateTestcase(Testcase testcase) {
        if (testcase.getId() == null) {
            throw new IllegalArgumentException("Testcase ID is required for update");
        }

        // Verify testcase exists
        if (!testcaseRepository.existsById(testcase.getId())) {
            throw new IllegalArgumentException("Testcase not found: " + testcase.getId());
        }

        // Update search indices
        testcase.populateIndices();

        return testcaseRepository.save(testcase);
    }

    /**
     * Find testcase by ID.
     */
    @Transactional(readOnly = true)
    public Optional<Testcase> findById(Long id) {
        return testcaseRepository.findById(id);
    }

    /**
     * Find all open testcases.
     */
    @Transactional(readOnly = true)
    public List<Testcase> findOpenTestcases() {
        return testcaseRepository.findByOpenTrue();
    }

    /**
     * Find testcases by fuzzer name.
     */
    @Transactional(readOnly = true)
    public List<Testcase> findByFuzzer(String fuzzerName) {
        return testcaseRepository.findByFuzzerName(fuzzerName);
    }

    /**
     * Find security testcases by severity.
     */
    @Transactional(readOnly = true)
    public List<Testcase> findSecurityTestcases(SecuritySeverity severity) {
        if (severity != null) {
            return testcaseRepository.findOpenSecurityTestcasesBySeverity(severity);
        } else {
            return testcaseRepository.findBySecurityFlagTrue();
        }
    }

    /**
     * Search testcases with filters.
     */
    @Transactional(readOnly = true)
    public Page<Testcase> searchTestcases(String crashType, String fuzzerName, 
                                          String projectName, Boolean securityFlag, 
                                          Boolean open, Pageable pageable) {
        return testcaseRepository.searchTestcases(crashType, fuzzerName, projectName, 
                                                  securityFlag, open, pageable);
    }

    /**
     * Find recent testcases for a fuzzer.
     */
    @Transactional(readOnly = true)
    public List<Testcase> findRecentTestcases(String fuzzerName, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return testcaseRepository.findRecentTestcasesByFuzzer(fuzzerName, since);
    }

    /**
     * Mark testcase as duplicate.
     */
    public void markAsDuplicate(Long testcaseId, Long duplicateOfId) {
        Optional<Testcase> testcaseOpt = testcaseRepository.findById(testcaseId);
        Optional<Testcase> duplicateOfOpt = testcaseRepository.findById(duplicateOfId);

        if (testcaseOpt.isEmpty()) {
            throw new IllegalArgumentException("Testcase not found: " + testcaseId);
        }
        
        if (duplicateOfOpt.isEmpty()) {
            throw new IllegalArgumentException("Duplicate target testcase not found: " + duplicateOfId);
        }

        Testcase testcase = testcaseOpt.get();
        testcase.setDuplicateOf(duplicateOfId);
        testcase.setOpen(false);
        testcase.setStatus("Duplicate");
        
        testcaseRepository.save(testcase);
    }

    /**
     * Close testcase.
     */
    public void closeTestcase(Long testcaseId, String reason) {
        Optional<Testcase> testcaseOpt = testcaseRepository.findById(testcaseId);
        
        if (testcaseOpt.isEmpty()) {
            throw new IllegalArgumentException("Testcase not found: " + testcaseId);
        }

        Testcase testcase = testcaseOpt.get();
        testcase.setOpen(false);
        testcase.setStatus("Closed");
        
        // Add closure reason to comments
        String currentComments = testcase.getComments() != null ? testcase.getComments() : "";
        String closureComment = String.format("\n[%s] Closed: %s", 
                                              LocalDateTime.now(), reason);
        testcase.setComments(currentComments + closureComment);
        
        testcaseRepository.save(testcase);
    }

    /**
     * Update security information.
     */
    public void updateSecurityInfo(Long testcaseId, boolean isSecurityIssue, 
                                   SecuritySeverity severity) {
        Optional<Testcase> testcaseOpt = testcaseRepository.findById(testcaseId);
        
        if (testcaseOpt.isEmpty()) {
            throw new IllegalArgumentException("Testcase not found: " + testcaseId);
        }

        Testcase testcase = testcaseOpt.get();
        testcase.setSecurityFlag(isSecurityIssue);
        
        if (isSecurityIssue && severity != null) {
            testcase.setSecuritySeverity(severity);
        } else if (!isSecurityIssue) {
            testcase.setSecuritySeverity(null);
        }
        
        testcaseRepository.save(testcase);
    }

    /**
     * Find potential duplicates for a testcase.
     */
    @Transactional(readOnly = true)
    public List<Testcase> findPotentialDuplicates(Long testcaseId) {
        Optional<Testcase> testcaseOpt = testcaseRepository.findById(testcaseId);
        
        if (testcaseOpt.isEmpty()) {
            throw new IllegalArgumentException("Testcase not found: " + testcaseId);
        }

        Testcase testcase = testcaseOpt.get();
        return testcaseRepository.findPotentialDuplicates(
            testcase.getCrashState(), 
            testcase.getCrashType(), 
            testcaseId
        );
    }

    /**
     * Get testcase statistics.
     */
    @Transactional(readOnly = true)
    public TestcaseStatistics getStatistics() {
        long totalTestcases = testcaseRepository.count();
        long openTestcases = testcaseRepository.findByOpenTrue().size();
        long securityTestcases = testcaseRepository.countOpenSecurityTestcases();
        
        List<Object[]> crashTypeStats = testcaseRepository.getCrashTypeStatistics();
        List<Object[]> platformStats = testcaseRepository.getPlatformStatistics();
        
        return new TestcaseStatistics(totalTestcases, openTestcases, securityTestcases, 
                                      crashTypeStats, platformStats);
    }

    /**
     * Find testcases needing triage.
     */
    @Transactional(readOnly = true)
    public List<Testcase> findTestcasesNeedingTriage() {
        return testcaseRepository.findUntriagedTestcases();
    }

    /**
     * Mark testcase as triaged.
     */
    public void markAsTriaged(Long testcaseId) {
        Optional<Testcase> testcaseOpt = testcaseRepository.findById(testcaseId);
        
        if (testcaseOpt.isEmpty()) {
            throw new IllegalArgumentException("Testcase not found: " + testcaseId);
        }

        Testcase testcase = testcaseOpt.get();
        testcase.setTriaged(true);
        testcase.setStuckInTriage(false);
        
        testcaseRepository.save(testcase);
    }

    /**
     * Delete testcase (soft delete by closing).
     */
    public void deleteTestcase(Long testcaseId) {
        closeTestcase(testcaseId, "Deleted by user");
    }

    /**
     * Statistics data class.
     */
    public static class TestcaseStatistics {
        private final long totalTestcases;
        private final long openTestcases;
        private final long securityTestcases;
        private final List<Object[]> crashTypeStatistics;
        private final List<Object[]> platformStatistics;

        public TestcaseStatistics(long totalTestcases, long openTestcases, 
                                  long securityTestcases, List<Object[]> crashTypeStatistics,
                                  List<Object[]> platformStatistics) {
            this.totalTestcases = totalTestcases;
            this.openTestcases = openTestcases;
            this.securityTestcases = securityTestcases;
            this.crashTypeStatistics = crashTypeStatistics;
            this.platformStatistics = platformStatistics;
        }

        // Getters
        public long getTotalTestcases() { return totalTestcases; }
        public long getOpenTestcases() { return openTestcases; }
        public long getSecurityTestcases() { return securityTestcases; }
        public List<Object[]> getCrashTypeStatistics() { return crashTypeStatistics; }
        public List<Object[]> getPlatformStatistics() { return platformStatistics; }
    }
}