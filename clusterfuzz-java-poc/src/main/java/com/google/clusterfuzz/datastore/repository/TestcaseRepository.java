package com.google.clusterfuzz.datastore.repository;

import com.google.clusterfuzz.datastore.model.Testcase;
import com.google.clusterfuzz.datastore.model.SecuritySeverity;
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
 * Repository interface for Testcase entities.
 * Provides data access methods for testcase management.
 */
@Repository
public interface TestcaseRepository extends JpaRepository<Testcase, Long> {

    // Basic queries
    List<Testcase> findByFuzzerName(String fuzzerName);
    
    List<Testcase> findByJobType(String jobType);
    
    List<Testcase> findByProjectName(String projectName);
    
    List<Testcase> findByStatus(String status);
    
    List<Testcase> findByOpenTrue();
    
    List<Testcase> findBySecurityFlagTrue();
    
    List<Testcase> findBySecuritySeverity(SecuritySeverity severity);
    
    // Complex queries
    @Query("SELECT t FROM Testcase t WHERE t.crashType = :crashType AND t.open = true")
    List<Testcase> findOpenTestcasesByCrashType(@Param("crashType") String crashType);
    
    @Query("SELECT t FROM Testcase t WHERE t.securityFlag = true AND t.securitySeverity = :severity AND t.open = true")
    List<Testcase> findOpenSecurityTestcasesBySeverity(@Param("severity") SecuritySeverity severity);
    
    @Query("SELECT t FROM Testcase t WHERE t.fuzzerName = :fuzzerName AND t.timestamp >= :since")
    List<Testcase> findRecentTestcasesByFuzzer(@Param("fuzzerName") String fuzzerName, 
                                               @Param("since") LocalDateTime since);
    
    @Query("SELECT t FROM Testcase t WHERE t.projectName = :projectName AND t.open = true ORDER BY t.timestamp DESC")
    Page<Testcase> findOpenTestcasesByProject(@Param("projectName") String projectName, 
                                              Pageable pageable);
    
    // Search queries
    @Query("SELECT t FROM Testcase t JOIN t.keywords k WHERE k IN :keywords")
    List<Testcase> findByKeywords(@Param("keywords") List<String> keywords);
    
    @Query("SELECT t FROM Testcase t WHERE " +
           "(:crashType IS NULL OR t.crashType LIKE %:crashType%) AND " +
           "(:fuzzerName IS NULL OR t.fuzzerName LIKE %:fuzzerName%) AND " +
           "(:projectName IS NULL OR t.projectName = :projectName) AND " +
           "(:securityFlag IS NULL OR t.securityFlag = :securityFlag) AND " +
           "(:open IS NULL OR t.open = :open)")
    Page<Testcase> searchTestcases(@Param("crashType") String crashType,
                                   @Param("fuzzerName") String fuzzerName,
                                   @Param("projectName") String projectName,
                                   @Param("securityFlag") Boolean securityFlag,
                                   @Param("open") Boolean open,
                                   Pageable pageable);
    
    // Statistics queries
    @Query("SELECT COUNT(t) FROM Testcase t WHERE t.fuzzerName = :fuzzerName AND t.timestamp >= :since")
    long countTestcasesByFuzzerSince(@Param("fuzzerName") String fuzzerName, 
                                     @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(t) FROM Testcase t WHERE t.securityFlag = true AND t.open = true")
    long countOpenSecurityTestcases();
    
    @Query("SELECT t.crashType, COUNT(t) FROM Testcase t WHERE t.open = true GROUP BY t.crashType ORDER BY COUNT(t) DESC")
    List<Object[]> getCrashTypeStatistics();
    
    @Query("SELECT t.fuzzerName, COUNT(t) FROM Testcase t WHERE t.timestamp >= :since GROUP BY t.fuzzerName ORDER BY COUNT(t) DESC")
    List<Object[]> getFuzzerStatisticsSince(@Param("since") LocalDateTime since);
    
    // Duplicate detection
    @Query("SELECT t FROM Testcase t WHERE t.crashState = :crashState AND t.crashType = :crashType AND t.open = true AND t.id != :excludeId")
    List<Testcase> findPotentialDuplicates(@Param("crashState") String crashState,
                                           @Param("crashType") String crashType,
                                           @Param("excludeId") Long excludeId);
    
    // Group queries
    List<Testcase> findByGroupId(Integer groupId);
    
    @Query("SELECT t FROM Testcase t WHERE t.groupId = :groupId AND t.isLeader = true")
    Optional<Testcase> findGroupLeader(@Param("groupId") Integer groupId);
    
    // Bug tracking
    @Query("SELECT t FROM Testcase t WHERE t.bugInformation IS NOT NULL AND t.bugInformation != ''")
    List<Testcase> findTestcasesWithBugs();
    
    @Query("SELECT t FROM Testcase t WHERE t.hasBugFlag = false AND t.open = true AND t.timestamp <= :deadline")
    List<Testcase> findTestcasesNeedingBugFiling(@Param("deadline") LocalDateTime deadline);
    
    // Impact analysis
    @Query("SELECT t FROM Testcase t WHERE t.isImpactSetFlag = false AND t.open = true")
    List<Testcase> findTestcasesNeedingImpactAnalysis();
    
    @Query("SELECT t FROM Testcase t WHERE " +
           "t.impactStableVersion IS NOT NULL OR " +
           "t.impactBetaVersion IS NOT NULL OR " +
           "t.impactExtendedStableVersion IS NOT NULL")
    List<Testcase> findTestcasesWithVersionImpact();
    
    // Triage queries
    @Query("SELECT t FROM Testcase t WHERE t.triaged = false AND t.open = true ORDER BY t.timestamp ASC")
    List<Testcase> findUntriagedTestcases();
    
    @Query("SELECT t FROM Testcase t WHERE t.stuckInTriage = true")
    List<Testcase> findTestcasesStuckInTriage();
    
    // Platform queries
    List<Testcase> findByPlatform(String platform);
    
    @Query("SELECT t.platform, COUNT(t) FROM Testcase t WHERE t.open = true GROUP BY t.platform")
    List<Object[]> getPlatformStatistics();
}