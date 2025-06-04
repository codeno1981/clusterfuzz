package com.google.clusterfuzz.datastore.repository;

import com.google.clusterfuzz.datastore.model.Config;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Config entities.
 * Provides data access methods for system configuration management.
 * Note: Config is typically a singleton entity with only one record.
 */
@Repository
public interface ConfigRepository extends JpaRepository<Config, Long> {

    // Singleton access
    @Query("SELECT c FROM Config c ORDER BY c.id LIMIT 1")
    Optional<Config> findSingletonConfig();
    
    @Query("SELECT c FROM Config c ORDER BY c.id")
    List<Config> findAllConfigs();
    
    // URL and endpoint queries
    @Query("SELECT c FROM Config c WHERE c.url IS NOT NULL AND c.url != ''")
    List<Config> findWithUrl();
    
    @Query("SELECT c FROM Config c WHERE c.jiraUrl IS NOT NULL AND c.jiraUrl != ''")
    List<Config> findWithJiraUrl();
    
    @Query("SELECT c FROM Config c WHERE c.reproductionHelpUrl IS NOT NULL AND c.reproductionHelpUrl != ''")
    List<Config> findWithReproductionHelpUrl();
    
    @Query("SELECT c FROM Config c WHERE c.documentationUrl IS NOT NULL AND c.documentationUrl != ''")
    List<Config> findWithDocumentationUrl();
    
    @Query("SELECT c FROM Config c WHERE c.bugReportUrl IS NOT NULL AND c.bugReportUrl != ''")
    List<Config> findWithBugReportUrl();
    
    // Authentication and credentials queries
    @Query("SELECT c FROM Config c WHERE c.clientCredentials IS NOT NULL AND c.clientCredentials != ''")
    List<Config> findWithClientCredentials();
    
    @Query("SELECT c FROM Config c WHERE c.jiraCredentials IS NOT NULL AND c.jiraCredentials != ''")
    List<Config> findWithJiraCredentials();
    
    @Query("SELECT c FROM Config c WHERE c.githubCredentials IS NOT NULL AND c.githubCredentials != ''")
    List<Config> findWithGithubCredentials();
    
    @Query("SELECT c FROM Config c WHERE c.buildApiaryServiceAccountPrivateKey IS NOT NULL AND c.buildApiaryServiceAccountPrivateKey != ''")
    List<Config> findWithBuildApiaryKey();
    
    @Query("SELECT c FROM Config c WHERE c.ossFuzzRobotGithubPersonalAccessToken IS NOT NULL AND c.ossFuzzRobotGithubPersonalAccessToken != ''")
    List<Config> findWithGithubToken();
    
    @Query("SELECT c FROM Config c WHERE c.sendgridApiKey IS NOT NULL AND c.sendgridApiKey != ''")
    List<Config> findWithSendgridKey();
    
    // Test account queries
    @Query("SELECT c FROM Config c WHERE c.testAccountEmail IS NOT NULL AND c.testAccountEmail != ''")
    List<Config> findWithTestAccount();
    
    @Query("SELECT c FROM Config c WHERE c.testAccountEmail IS NOT NULL AND c.testAccountEmail != '' AND c.testAccountPassword IS NOT NULL AND c.testAccountPassword != ''")
    List<Config> findWithCompleteTestAccount();
    
    // User management queries
    @Query("SELECT c FROM Config c WHERE c.privilegedUsers IS NOT NULL AND c.privilegedUsers != ''")
    List<Config> findWithPrivilegedUsers();
    
    @Query("SELECT c FROM Config c WHERE c.blacklistedUsers IS NOT NULL AND c.blacklistedUsers != ''")
    List<Config> findWithBlacklistedUsers();
    
    @Query("SELECT c FROM Config c WHERE c.privilegedUsers LIKE %:email%")
    List<Config> findByPrivilegedUser(@Param("email") String email);
    
    @Query("SELECT c FROM Config c WHERE c.blacklistedUsers LIKE %:email%")
    List<Config> findByBlacklistedUser(@Param("email") String email);
    
    // Contact and support queries
    @Query("SELECT c FROM Config c WHERE c.contactString IS NOT NULL AND c.contactString != ''")
    List<Config> findWithContactInfo();
    
    // Integration queries
    @Query("SELECT c FROM Config c WHERE c.predatorCrashTopic IS NOT NULL AND c.predatorCrashTopic != ''")
    List<Config> findWithPredatorCrashTopic();
    
    @Query("SELECT c FROM Config c WHERE c.predatorResultTopic IS NOT NULL AND c.predatorResultTopic != ''")
    List<Config> findWithPredatorResultTopic();
    
    @Query("SELECT c FROM Config c WHERE c.predatorCrashTopic IS NOT NULL AND c.predatorCrashTopic != '' AND c.predatorResultTopic IS NOT NULL AND c.predatorResultTopic != ''")
    List<Config> findWithCompletePredatorIntegration();
    
    @Query("SELECT c FROM Config c WHERE c.coverageReportsBucket IS NOT NULL AND c.coverageReportsBucket != ''")
    List<Config> findWithCoverageReportsBucket();
    
    // WiFi configuration queries
    @Query("SELECT c FROM Config c WHERE c.wifiSsid IS NOT NULL AND c.wifiSsid != ''")
    List<Config> findWithWifiSsid();
    
    @Query("SELECT c FROM Config c WHERE c.wifiSsid IS NOT NULL AND c.wifiSsid != '' AND c.wifiPassword IS NOT NULL AND c.wifiPassword != ''")
    List<Config> findWithCompleteWifiConfig();
    
    // Mapping and configuration queries
    @Query("SELECT c FROM Config c WHERE c.componentRepositoryMappings IS NOT NULL AND c.componentRepositoryMappings != ''")
    List<Config> findWithComponentMappings();
    
    @Query("SELECT c FROM Config c WHERE c.platformGroupMappings IS NOT NULL AND c.platformGroupMappings != ''")
    List<Config> findWithPlatformMappings();
    
    // Security and restriction queries
    List<Config> findByRelaxTestcaseRestrictionsTrue();
    
    List<Config> findByRelaxSecurityBugRestrictionsTrue();
    
    @Query("SELECT c FROM Config c WHERE c.relaxTestcaseRestrictions = true OR c.relaxSecurityBugRestrictions = true")
    List<Config> findWithRelaxedRestrictions();
    
    @Query("SELECT c FROM Config c WHERE c.relaxTestcaseRestrictions = false AND c.relaxSecurityBugRestrictions = false")
    List<Config> findWithStrictRestrictions();
    
    // Feature flag queries
    List<Config> findByAutoBugFilingEnabledTrue();
    
    List<Config> findByAutoBugFilingEnabledFalse();
    
    List<Config> findBySecurityNotificationsEnabledTrue();
    
    List<Config> findBySecurityNotificationsEnabledFalse();
    
    @Query("SELECT c FROM Config c WHERE c.autoBugFilingEnabled = true AND c.securityNotificationsEnabled = true")
    List<Config> findWithAllNotificationsEnabled();
    
    // Limits and thresholds queries
    @Query("SELECT c FROM Config c WHERE c.maxTestcasesPerDay IS NOT NULL")
    List<Config> findWithTestcaseLimit();
    
    @Query("SELECT c FROM Config c WHERE c.defaultTimeoutSeconds IS NOT NULL")
    List<Config> findWithDefaultTimeout();
    
    @Query("SELECT c FROM Config c WHERE c.maxUploadSizeBytes IS NOT NULL")
    List<Config> findWithUploadSizeLimit();
    
    @Query("SELECT c FROM Config c WHERE c.maxTestcasesPerDay > :threshold")
    List<Config> findWithHighTestcaseLimit(@Param("threshold") Integer threshold);
    
    @Query("SELECT c FROM Config c WHERE c.defaultTimeoutSeconds > :threshold")
    List<Config> findWithHighTimeout(@Param("threshold") Integer threshold);
    
    @Query("SELECT c FROM Config c WHERE c.maxUploadSizeBytes > :threshold")
    List<Config> findWithHighUploadLimit(@Param("threshold") Long threshold);
    
    // Hash and change tracking
    @Query("SELECT c FROM Config c WHERE c.previousHash IS NOT NULL AND c.previousHash != ''")
    List<Config> findWithPreviousHash();
    
    @Query("SELECT c FROM Config c WHERE c.previousHash = :hash")
    List<Config> findByPreviousHash(@Param("hash") String hash);
    
    // Validation queries
    @Query("SELECT c FROM Config c WHERE c.url IS NULL OR c.url = ''")
    List<Config> findWithoutUrl();
    
    @Query("SELECT c FROM Config c WHERE c.contactString IS NULL OR c.contactString = ''")
    List<Config> findWithoutContactInfo();
    
    @Query("SELECT c FROM Config c WHERE (c.jiraUrl IS NOT NULL AND c.jiraUrl != '') AND (c.jiraCredentials IS NULL OR c.jiraCredentials = '')")
    List<Config> findWithIncompleteJiraConfig();
    
    @Query("SELECT c FROM Config c WHERE (c.wifiSsid IS NOT NULL AND c.wifiSsid != '') AND (c.wifiPassword IS NULL OR c.wifiPassword = '')")
    List<Config> findWithIncompleteWifiConfig();
    
    @Query("SELECT c FROM Config c WHERE (c.testAccountEmail IS NOT NULL AND c.testAccountEmail != '') AND (c.testAccountPassword IS NULL OR c.testAccountPassword = '')")
    List<Config> findWithIncompleteTestAccount();
    
    // Integration completeness checks
    @Query("SELECT c FROM Config c WHERE " +
           "(c.jiraUrl IS NOT NULL AND c.jiraUrl != '' AND c.jiraCredentials IS NOT NULL AND c.jiraCredentials != '') OR " +
           "(c.githubCredentials IS NOT NULL AND c.githubCredentials != '') OR " +
           "(c.sendgridApiKey IS NOT NULL AND c.sendgridApiKey != '')")
    List<Config> findWithAnyIntegration();
    
    @Query("SELECT c FROM Config c WHERE " +
           "(c.jiraUrl IS NULL OR c.jiraUrl = '' OR c.jiraCredentials IS NULL OR c.jiraCredentials = '') AND " +
           "(c.githubCredentials IS NULL OR c.githubCredentials = '') AND " +
           "(c.sendgridApiKey IS NULL OR c.sendgridApiKey = '')")
    List<Config> findWithoutIntegrations();
    
    // Statistics and analysis
    @Query("SELECT COUNT(c) FROM Config c WHERE c.relaxTestcaseRestrictions = true")
    long countWithRelaxedTestcaseRestrictions();
    
    @Query("SELECT COUNT(c) FROM Config c WHERE c.relaxSecurityBugRestrictions = true")
    long countWithRelaxedSecurityRestrictions();
    
    @Query("SELECT COUNT(c) FROM Config c WHERE c.autoBugFilingEnabled = true")
    long countWithAutoBugFiling();
    
    @Query("SELECT COUNT(c) FROM Config c WHERE c.securityNotificationsEnabled = true")
    long countWithSecurityNotifications();
    
    @Query("SELECT COUNT(c) FROM Config c WHERE c.jiraUrl IS NOT NULL AND c.jiraUrl != ''")
    long countWithJiraIntegration();
    
    @Query("SELECT COUNT(c) FROM Config c WHERE c.githubCredentials IS NOT NULL AND c.githubCredentials != ''")
    long countWithGithubIntegration();
    
    @Query("SELECT COUNT(c) FROM Config c WHERE c.sendgridApiKey IS NOT NULL AND c.sendgridApiKey != ''")
    long countWithEmailIntegration();
    
    // Bulk operations and utilities
    @Query("SELECT c.url FROM Config c WHERE c.url IS NOT NULL AND c.url != ''")
    List<String> findAllUrls();
    
    @Query("SELECT c.contactString FROM Config c WHERE c.contactString IS NOT NULL AND c.contactString != ''")
    List<String> findAllContactStrings();
    
    @Query("SELECT c.coverageReportsBucket FROM Config c WHERE c.coverageReportsBucket IS NOT NULL AND c.coverageReportsBucket != ''")
    List<String> findAllCoverageReportsBuckets();
    
    // Existence checks
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Config c WHERE c.privilegedUsers LIKE %:email%")
    boolean isPrivilegedUser(@Param("email") String email);
    
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Config c WHERE c.blacklistedUsers LIKE %:email%")
    boolean isBlacklistedUser(@Param("email") String email);
    
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Config c WHERE c.jiraUrl IS NOT NULL AND c.jiraUrl != ''")
    boolean hasJiraIntegration();
    
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Config c WHERE c.githubCredentials IS NOT NULL AND c.githubCredentials != ''")
    boolean hasGithubIntegration();
    
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Config c WHERE c.sendgridApiKey IS NOT NULL AND c.sendgridApiKey != ''")
    boolean hasEmailIntegration();
}