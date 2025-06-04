package com.google.clusterfuzz.service;

import com.google.clusterfuzz.datastore.model.CoverageInformation;
import com.google.clusterfuzz.datastore.repository.CoverageInformationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for CoverageInformation entity operations.
 * Provides business logic for coverage tracking, analysis, and reporting.
 */
@Service
@Transactional
public class CoverageInformationService {

    private static final Logger logger = LoggerFactory.getLogger(CoverageInformationService.class);
    
    private static final double EXCELLENT_COVERAGE_THRESHOLD = 90.0;
    private static final double GOOD_COVERAGE_THRESHOLD = 70.0;
    private static final double POOR_COVERAGE_THRESHOLD = 50.0;
    private static final int DEFAULT_RECENT_DAYS = 30;

    @Autowired
    private CoverageInformationRepository coverageRepository;

    // Coverage creation and management

    /**
     * Create new coverage information.
     */
    public CoverageInformation createCoverage(String fuzzer, String jobType, String platform, 
                                            LocalDateTime date) {
        logger.info("Creating coverage information for fuzzer '{}', job '{}', platform '{}'", 
                   fuzzer, jobType, platform);
        
        CoverageInformation coverage = new CoverageInformation(fuzzer, jobType, platform, date);
        CoverageInformation saved = coverageRepository.save(coverage);
        logger.info("Created coverage information with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Create coverage with metrics.
     */
    public CoverageInformation createCoverageWithMetrics(String fuzzer, String jobType, String platform,
                                                       LocalDateTime date, Integer linesCovered, 
                                                       Integer linesTotal, Integer functionsCovered, 
                                                       Integer functionsTotal) {
        CoverageInformation coverage = createCoverage(fuzzer, jobType, platform, date);
        coverage.setLinesCovered(linesCovered);
        coverage.setLinesTotal(linesTotal);
        coverage.setFunctionsCovered(functionsCovered);
        coverage.setFunctionsTotal(functionsTotal);
        coverage.calculateCoveragePercentages();
        
        return coverageRepository.save(coverage);
    }

    /**
     * Update coverage metrics.
     */
    public CoverageInformation updateCoverageMetrics(Long coverageId, Integer linesCovered, 
                                                   Integer linesTotal, Integer functionsCovered, 
                                                   Integer functionsTotal, Integer edgesCovered, 
                                                   Integer edgesTotal) {
        logger.info("Updating coverage metrics for ID: {}", coverageId);
        
        Optional<CoverageInformation> coverageOpt = coverageRepository.findById(coverageId);
        if (coverageOpt.isEmpty()) {
            throw new IllegalArgumentException("Coverage not found: " + coverageId);
        }
        
        CoverageInformation coverage = coverageOpt.get();
        coverage.setLinesCovered(linesCovered);
        coverage.setLinesTotal(linesTotal);
        coverage.setFunctionsCovered(functionsCovered);
        coverage.setFunctionsTotal(functionsTotal);
        coverage.setEdgesCovered(edgesCovered);
        coverage.setEdgesTotal(edgesTotal);
        coverage.calculateCoveragePercentages();
        
        return coverageRepository.save(coverage);
    }

    // Coverage retrieval

    /**
     * Get coverage by ID.
     */
    @Transactional(readOnly = true)
    public Optional<CoverageInformation> getCoverageById(Long id) {
        return coverageRepository.findById(id);
    }

    /**
     * Get latest coverage for fuzzer and job type.
     */
    @Transactional(readOnly = true)
    public Optional<CoverageInformation> getLatestCoverage(String fuzzer, String jobType) {
        return coverageRepository.findLatestByFuzzerAndJobType(fuzzer, jobType);
    }

    /**
     * Get latest coverage for fuzzer, job type, and platform.
     */
    @Transactional(readOnly = true)
    public Optional<CoverageInformation> getLatestCoverage(String fuzzer, String jobType, String platform) {
        return coverageRepository.findLatestByJobTypeAndPlatform(jobType, platform);
    }

    /**
     * Get coverage history for a fuzzer.
     */
    @Transactional(readOnly = true)
    public List<CoverageInformation> getCoverageHistory(String fuzzer) {
        return coverageRepository.findByFuzzerOrderByDateDesc(fuzzer);
    }

    /**
     * Get coverage history for a job type.
     */
    @Transactional(readOnly = true)
    public List<CoverageInformation> getCoverageHistoryByJobType(String jobType) {
        return coverageRepository.findByJobTypeOrderByDateDesc(jobType);
    }

    /**
     * Get recent coverage data.
     */
    @Transactional(readOnly = true)
    public List<CoverageInformation> getRecentCoverage(int days) {
        LocalDateTime sinceDate = LocalDateTime.now().minusDays(days);
        return coverageRepository.findRecentCoverage(sinceDate);
    }

    /**
     * Get recent coverage data with default timeframe.
     */
    @Transactional(readOnly = true)
    public List<CoverageInformation> getRecentCoverage() {
        return getRecentCoverage(DEFAULT_RECENT_DAYS);
    }

    // Coverage analysis

    /**
     * Get coverage by quality level.
     */
    @Transactional(readOnly = true)
    public List<CoverageInformation> getExcellentCoverage() {
        return coverageRepository.findExcellentCoverage();
    }

    /**
     * Get good coverage (70-90%).
     */
    @Transactional(readOnly = true)
    public List<CoverageInformation> getGoodCoverage() {
        return coverageRepository.findGoodCoverage();
    }

    /**
     * Get poor coverage (<50%).
     */
    @Transactional(readOnly = true)
    public List<CoverageInformation> getPoorCoverage() {
        return coverageRepository.findPoorCoverage();
    }

    /**
     * Get coverage by minimum line coverage percentage.
     */
    @Transactional(readOnly = true)
    public List<CoverageInformation> getCoverageByMinLinePercentage(double minPercentage) {
        return coverageRepository.findByMinLineCoverage(minPercentage);
    }

    /**
     * Get coverage improvements for a fuzzer.
     */
    @Transactional(readOnly = true)
    public List<CoverageInformation> getCoverageImprovements(String fuzzer) {
        return coverageRepository.findCoverageImprovements(fuzzer);
    }

    /**
     * Analyze coverage trends.
     */
    @Transactional(readOnly = true)
    public CoverageTrendAnalysis analyzeCoverageTrends(String fuzzer, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<CoverageInformation> recentCoverage = coverageRepository
            .findByFuzzerAndDateRange(fuzzer, startDate, LocalDateTime.now());
        
        CoverageTrendAnalysis analysis = new CoverageTrendAnalysis();
        analysis.setFuzzer(fuzzer);
        analysis.setAnalysisPeriodDays(days);
        analysis.setTotalDataPoints(recentCoverage.size());
        
        if (!recentCoverage.isEmpty()) {
            // Calculate trend metrics
            double avgLineCoverage = recentCoverage.stream()
                .filter(c -> c.getLineCoveragePercentage() != null)
                .mapToDouble(CoverageInformation::getLineCoveragePercentage)
                .average().orElse(0.0);
            
            double maxLineCoverage = recentCoverage.stream()
                .filter(c -> c.getLineCoveragePercentage() != null)
                .mapToDouble(CoverageInformation::getLineCoveragePercentage)
                .max().orElse(0.0);
            
            double minLineCoverage = recentCoverage.stream()
                .filter(c -> c.getLineCoveragePercentage() != null)
                .mapToDouble(CoverageInformation::getLineCoveragePercentage)
                .min().orElse(0.0);
            
            analysis.setAverageLineCoverage(avgLineCoverage);
            analysis.setMaxLineCoverage(maxLineCoverage);
            analysis.setMinLineCoverage(minLineCoverage);
            analysis.setCoverageRange(maxLineCoverage - minLineCoverage);
            
            // Determine trend direction
            if (recentCoverage.size() >= 2) {
                CoverageInformation latest = recentCoverage.get(0);
                CoverageInformation previous = recentCoverage.get(recentCoverage.size() - 1);
                
                if (latest.getLineCoveragePercentage() != null && 
                    previous.getLineCoveragePercentage() != null) {
                    double improvement = latest.getLineCoveragePercentage() - 
                                       previous.getLineCoveragePercentage();
                    analysis.setCoverageImprovement(improvement);
                    
                    if (improvement > 1.0) {
                        analysis.setTrend("IMPROVING");
                    } else if (improvement < -1.0) {
                        analysis.setTrend("DECLINING");
                    } else {
                        analysis.setTrend("STABLE");
                    }
                }
            }
        }
        
        return analysis;
    }

    // Coverage statistics

    /**
     * Get coverage statistics by fuzzer.
     */
    @Transactional(readOnly = true)
    public CoverageStatistics getCoverageStatistics(String fuzzer) {
        Object[] stats = coverageRepository.getCoverageStatsByFuzzer(fuzzer);
        
        CoverageStatistics statistics = new CoverageStatistics();
        statistics.setFuzzer(fuzzer);
        
        if (stats != null && stats.length >= 4) {
            statistics.setTotalCoverageReports(((Number) stats[0]).longValue());
            statistics.setAverageLineCoverage((Double) stats[1]);
            statistics.setMaxLineCoverage((Double) stats[2]);
            statistics.setMinLineCoverage((Double) stats[3]);
        }
        
        return statistics;
    }

    /**
     * Get coverage statistics by job type.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getCoverageByJobType() {
        return coverageRepository.getCoverageByJobType();
    }

    /**
     * Get coverage statistics by platform.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getCoverageByPlatform() {
        return coverageRepository.getCoverageByPlatform();
    }

    /**
     * Get daily coverage trend.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getDailyCoverageTrend(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return coverageRepository.getDailyCoverageTrend(startDate);
    }

    // Coverage validation and quality

    /**
     * Get coverage with quality issues.
     */
    @Transactional(readOnly = true)
    public List<CoverageInformation> getCoverageWithQualityIssues() {
        return coverageRepository.findWithQualityIssues();
    }

    /**
     * Get incomplete coverage reports.
     */
    @Transactional(readOnly = true)
    public List<CoverageInformation> getIncompleteCoverage() {
        return coverageRepository.findIncomplete();
    }

    /**
     * Validate coverage data integrity.
     */
    @Transactional(readOnly = true)
    public CoverageValidationReport validateCoverageData() {
        CoverageValidationReport report = new CoverageValidationReport();
        
        List<CoverageInformation> invalidFunction = coverageRepository.findInvalidFunctionCoverage();
        List<CoverageInformation> invalidLine = coverageRepository.findInvalidLineCoverage();
        List<CoverageInformation> invalidEdge = coverageRepository.findInvalidEdgeCoverage();
        List<CoverageInformation> inconsistent = coverageRepository.findInconsistentCoverage();
        List<CoverageInformation> duplicates = coverageRepository.findDuplicateCoverage();
        
        report.setInvalidFunctionCoverage(invalidFunction);
        report.setInvalidLineCoverage(invalidLine);
        report.setInvalidEdgeCoverage(invalidEdge);
        report.setInconsistentCoverage(inconsistent);
        report.setDuplicateCoverage(duplicates);
        
        report.setTotalIssues(invalidFunction.size() + invalidLine.size() + 
                             invalidEdge.size() + inconsistent.size() + duplicates.size());
        
        return report;
    }

    // Coverage management operations

    /**
     * Mark coverage as incomplete.
     */
    public void markAsIncomplete(Long coverageId, String reason) {
        logger.info("Marking coverage as incomplete: {}", coverageId);
        
        int updated = coverageRepository.markAsIncomplete(coverageId, reason);
        if (updated > 0) {
            logger.info("Successfully marked coverage as incomplete: {}", coverageId);
        } else {
            logger.warn("Coverage not found or not updated: {}", coverageId);
        }
    }

    /**
     * Add quality issues to coverage.
     */
    public void addQualityIssues(Long coverageId, String issues) {
        logger.info("Adding quality issues to coverage: {}", coverageId);
        
        int updated = coverageRepository.addQualityIssues(coverageId, issues);
        if (updated > 0) {
            logger.info("Successfully added quality issues to coverage: {}", coverageId);
        } else {
            logger.warn("Coverage not found or not updated: {}", coverageId);
        }
    }

    /**
     * Update HTML report URL.
     */
    public void updateHtmlReportUrl(Long coverageId, String url) {
        logger.info("Updating HTML report URL for coverage: {}", coverageId);
        
        int updated = coverageRepository.updateHtmlReportUrl(coverageId, url);
        if (updated > 0) {
            logger.info("Successfully updated HTML report URL: {}", coverageId);
        } else {
            logger.warn("Coverage not found or not updated: {}", coverageId);
        }
    }

    /**
     * Archive old coverage data.
     */
    public int archiveOldCoverage(int days) {
        logger.info("Archiving coverage data older than {} days", days);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        int archived = coverageRepository.archiveOldCoverage(cutoffDate);
        logger.info("Archived {} coverage records", archived);
        return archived;
    }

    /**
     * Delete archived coverage data.
     */
    public int deleteArchivedCoverage(int days) {
        logger.info("Deleting archived coverage data older than {} days", days);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        int deleted = coverageRepository.deleteArchivedCoverage(cutoffDate);
        logger.info("Deleted {} archived coverage records", deleted);
        return deleted;
    }

    /**
     * Delete incomplete coverage data.
     */
    public int deleteIncompleteCoverage(int days) {
        logger.info("Deleting incomplete coverage data older than {} days", days);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        int deleted = coverageRepository.deleteIncompleteCoverage(cutoffDate);
        logger.info("Deleted {} incomplete coverage records", deleted);
        return deleted;
    }

    // Search and filtering

    /**
     * Search coverage data.
     */
    @Transactional(readOnly = true)
    public List<CoverageInformation> searchCoverage(String searchTerm) {
        return coverageRepository.searchCoverage(searchTerm);
    }

    /**
     * Search active coverage data.
     */
    @Transactional(readOnly = true)
    public List<CoverageInformation> searchActiveCoverage(String searchTerm) {
        return coverageRepository.searchActiveCoverage(searchTerm);
    }

    /**
     * Get coverage by build ID.
     */
    @Transactional(readOnly = true)
    public List<CoverageInformation> getCoverageByBuildId(String buildId) {
        return coverageRepository.findByBuildId(buildId);
    }

    /**
     * Get coverage by revision.
     */
    @Transactional(readOnly = true)
    public List<CoverageInformation> getCoverageByRevision(String revision) {
        return coverageRepository.findByRevision(revision);
    }

    // Differential coverage

    /**
     * Get differential coverage reports.
     */
    @Transactional(readOnly = true)
    public List<CoverageInformation> getDifferentialCoverage() {
        return coverageRepository.findDifferentialCoverage();
    }

    /**
     * Get coverage by baseline.
     */
    @Transactional(readOnly = true)
    public List<CoverageInformation> getCoverageByBaseline(String baselineId) {
        return coverageRepository.findByBaselineCoverageId(baselineId);
    }

    // Performance analysis

    /**
     * Get slow analysis reports.
     */
    @Transactional(readOnly = true)
    public List<CoverageInformation> getSlowAnalysis(int maxSeconds) {
        return coverageRepository.findSlowAnalysis(maxSeconds);
    }

    /**
     * Get average analysis duration by fuzzer.
     */
    @Transactional(readOnly = true)
    public Double getAverageAnalysisDuration(String fuzzer) {
        return coverageRepository.getAverageAnalysisDurationByFuzzer(fuzzer);
    }

    // Helper methods

    /**
     * Get total coverage count.
     */
    @Transactional(readOnly = true)
    public long getTotalCoverageCount() {
        return coverageRepository.count();
    }

    /**
     * Get coverage count by fuzzer.
     */
    @Transactional(readOnly = true)
    public long getCoverageCountByFuzzer(String fuzzer) {
        return coverageRepository.countByFuzzer(fuzzer);
    }

    /**
     * Update coverage information.
     */
    public CoverageInformation updateCoverage(CoverageInformation coverage) {
        logger.info("Updating coverage information: {}", coverage.getId());
        return coverageRepository.save(coverage);
    }

    // Inner classes for analysis results

    public static class CoverageTrendAnalysis {
        private String fuzzer;
        private int analysisPeriodDays;
        private int totalDataPoints;
        private Double averageLineCoverage;
        private Double maxLineCoverage;
        private Double minLineCoverage;
        private Double coverageRange;
        private Double coverageImprovement;
        private String trend; // IMPROVING, DECLINING, STABLE

        // Getters and setters
        public String getFuzzer() { return fuzzer; }
        public void setFuzzer(String fuzzer) { this.fuzzer = fuzzer; }

        public int getAnalysisPeriodDays() { return analysisPeriodDays; }
        public void setAnalysisPeriodDays(int analysisPeriodDays) { this.analysisPeriodDays = analysisPeriodDays; }

        public int getTotalDataPoints() { return totalDataPoints; }
        public void setTotalDataPoints(int totalDataPoints) { this.totalDataPoints = totalDataPoints; }

        public Double getAverageLineCoverage() { return averageLineCoverage; }
        public void setAverageLineCoverage(Double averageLineCoverage) { this.averageLineCoverage = averageLineCoverage; }

        public Double getMaxLineCoverage() { return maxLineCoverage; }
        public void setMaxLineCoverage(Double maxLineCoverage) { this.maxLineCoverage = maxLineCoverage; }

        public Double getMinLineCoverage() { return minLineCoverage; }
        public void setMinLineCoverage(Double minLineCoverage) { this.minLineCoverage = minLineCoverage; }

        public Double getCoverageRange() { return coverageRange; }
        public void setCoverageRange(Double coverageRange) { this.coverageRange = coverageRange; }

        public Double getCoverageImprovement() { return coverageImprovement; }
        public void setCoverageImprovement(Double coverageImprovement) { this.coverageImprovement = coverageImprovement; }

        public String getTrend() { return trend; }
        public void setTrend(String trend) { this.trend = trend; }
    }

    public static class CoverageStatistics {
        private String fuzzer;
        private Long totalCoverageReports;
        private Double averageLineCoverage;
        private Double maxLineCoverage;
        private Double minLineCoverage;

        // Getters and setters
        public String getFuzzer() { return fuzzer; }
        public void setFuzzer(String fuzzer) { this.fuzzer = fuzzer; }

        public Long getTotalCoverageReports() { return totalCoverageReports; }
        public void setTotalCoverageReports(Long totalCoverageReports) { this.totalCoverageReports = totalCoverageReports; }

        public Double getAverageLineCoverage() { return averageLineCoverage; }
        public void setAverageLineCoverage(Double averageLineCoverage) { this.averageLineCoverage = averageLineCoverage; }

        public Double getMaxLineCoverage() { return maxLineCoverage; }
        public void setMaxLineCoverage(Double maxLineCoverage) { this.maxLineCoverage = maxLineCoverage; }

        public Double getMinLineCoverage() { return minLineCoverage; }
        public void setMinLineCoverage(Double minLineCoverage) { this.minLineCoverage = minLineCoverage; }
    }

    public static class CoverageValidationReport {
        private List<CoverageInformation> invalidFunctionCoverage;
        private List<CoverageInformation> invalidLineCoverage;
        private List<CoverageInformation> invalidEdgeCoverage;
        private List<CoverageInformation> inconsistentCoverage;
        private List<CoverageInformation> duplicateCoverage;
        private int totalIssues;

        // Getters and setters
        public List<CoverageInformation> getInvalidFunctionCoverage() { return invalidFunctionCoverage; }
        public void setInvalidFunctionCoverage(List<CoverageInformation> invalidFunctionCoverage) { 
            this.invalidFunctionCoverage = invalidFunctionCoverage; 
        }

        public List<CoverageInformation> getInvalidLineCoverage() { return invalidLineCoverage; }
        public void setInvalidLineCoverage(List<CoverageInformation> invalidLineCoverage) { 
            this.invalidLineCoverage = invalidLineCoverage; 
        }

        public List<CoverageInformation> getInvalidEdgeCoverage() { return invalidEdgeCoverage; }
        public void setInvalidEdgeCoverage(List<CoverageInformation> invalidEdgeCoverage) { 
            this.invalidEdgeCoverage = invalidEdgeCoverage; 
        }

        public List<CoverageInformation> getInconsistentCoverage() { return inconsistentCoverage; }
        public void setInconsistentCoverage(List<CoverageInformation> inconsistentCoverage) { 
            this.inconsistentCoverage = inconsistentCoverage; 
        }

        public List<CoverageInformation> getDuplicateCoverage() { return duplicateCoverage; }
        public void setDuplicateCoverage(List<CoverageInformation> duplicateCoverage) { 
            this.duplicateCoverage = duplicateCoverage; 
        }

        public int getTotalIssues() { return totalIssues; }
        public void setTotalIssues(int totalIssues) { this.totalIssues = totalIssues; }

        public boolean hasIssues() { return totalIssues > 0; }
    }
}