package com.google.clusterfuzz.service;

import com.google.clusterfuzz.datastore.model.Config;
import com.google.clusterfuzz.datastore.repository.ConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Config entities.
 * Provides business logic for system configuration management.
 * Config is typically a singleton entity with only one record.
 */
@Service
@Transactional
public class ConfigService {

    private final ConfigRepository repository;

    @Autowired
    public ConfigService(ConfigRepository repository) {
        this.repository = repository;
    }

    /**
     * Get the singleton configuration.
     */
    @Transactional(readOnly = true)
    public Optional<Config> getConfig() {
        return repository.findSingletonConfig();
    }

    /**
     * Get or create the singleton configuration.
     */
    public Config getOrCreateConfig() {
        Optional<Config> configOpt = repository.findSingletonConfig();
        if (configOpt.isPresent()) {
            return configOpt.get();
        }
        
        // Create default config
        Config config = new Config();
        return repository.save(config);
    }

    /**
     * Update configuration.
     */
    public Config updateConfig(Config updates) {
        Config config = getOrCreateConfig();
        
        // Calculate hash of current config for change detection
        String currentHash = calculateConfigHash(config);
        config.setPreviousHash(currentHash);
        
        // Update fields
        if (updates.getUrl() != null) {
            config.setUrl(updates.getUrl());
        }
        if (updates.getClientCredentials() != null) {
            config.setClientCredentials(updates.getClientCredentials());
        }
        if (updates.getJiraUrl() != null) {
            config.setJiraUrl(updates.getJiraUrl());
        }
        if (updates.getJiraCredentials() != null) {
            config.setJiraCredentials(updates.getJiraCredentials());
        }
        if (updates.getBuildApiaryServiceAccountPrivateKey() != null) {
            config.setBuildApiaryServiceAccountPrivateKey(updates.getBuildApiaryServiceAccountPrivateKey());
        }
        if (updates.getTestAccountEmail() != null) {
            config.setTestAccountEmail(updates.getTestAccountEmail());
        }
        if (updates.getTestAccountPassword() != null) {
            config.setTestAccountPassword(updates.getTestAccountPassword());
        }
        if (updates.getPrivilegedUsers() != null) {
            config.setPrivilegedUsers(updates.getPrivilegedUsers());
        }
        if (updates.getBlacklistedUsers() != null) {
            config.setBlacklistedUsers(updates.getBlacklistedUsers());
        }
        if (updates.getContactString() != null) {
            config.setContactString(updates.getContactString());
        }
        if (updates.getComponentRepositoryMappings() != null) {
            config.setComponentRepositoryMappings(updates.getComponentRepositoryMappings());
        }
        if (updates.getReproductionHelpUrl() != null) {
            config.setReproductionHelpUrl(updates.getReproductionHelpUrl());
        }
        if (updates.getDocumentationUrl() != null) {
            config.setDocumentationUrl(updates.getDocumentationUrl());
        }
        if (updates.getBugReportUrl() != null) {
            config.setBugReportUrl(updates.getBugReportUrl());
        }
        if (updates.getPlatformGroupMappings() != null) {
            config.setPlatformGroupMappings(updates.getPlatformGroupMappings());
        }
        if (updates.getRelaxTestcaseRestrictions() != null) {
            config.setRelaxTestcaseRestrictions(updates.getRelaxTestcaseRestrictions());
        }
        if (updates.getRelaxSecurityBugRestrictions() != null) {
            config.setRelaxSecurityBugRestrictions(updates.getRelaxSecurityBugRestrictions());
        }
        if (updates.getCoverageReportsBucket() != null) {
            config.setCoverageReportsBucket(updates.getCoverageReportsBucket());
        }
        if (updates.getGithubCredentials() != null) {
            config.setGithubCredentials(updates.getGithubCredentials());
        }
        if (updates.getOssFuzzRobotGithubPersonalAccessToken() != null) {
            config.setOssFuzzRobotGithubPersonalAccessToken(updates.getOssFuzzRobotGithubPersonalAccessToken());
        }
        if (updates.getPredatorCrashTopic() != null) {
            config.setPredatorCrashTopic(updates.getPredatorCrashTopic());
        }
        if (updates.getPredatorResultTopic() != null) {
            config.setPredatorResultTopic(updates.getPredatorResultTopic());
        }
        if (updates.getWifiSsid() != null) {
            config.setWifiSsid(updates.getWifiSsid());
        }
        if (updates.getWifiPassword() != null) {
            config.setWifiPassword(updates.getWifiPassword());
        }
        if (updates.getSendgridApiKey() != null) {
            config.setSendgridApiKey(updates.getSendgridApiKey());
        }
        if (updates.getMaxTestcasesPerDay() != null) {
            config.setMaxTestcasesPerDay(updates.getMaxTestcasesPerDay());
        }
        if (updates.getDefaultTimeoutSeconds() != null) {
            config.setDefaultTimeoutSeconds(updates.getDefaultTimeoutSeconds());
        }
        if (updates.getMaxUploadSizeBytes() != null) {
            config.setMaxUploadSizeBytes(updates.getMaxUploadSizeBytes());
        }
        if (updates.getAutoBugFilingEnabled() != null) {
            config.setAutoBugFilingEnabled(updates.getAutoBugFilingEnabled());
        }
        if (updates.getSecurityNotificationsEnabled() != null) {
            config.setSecurityNotificationsEnabled(updates.getSecurityNotificationsEnabled());
        }
        
        return repository.save(config);
    }

    /**
     * Add a privileged user.
     */
    public Config addPrivilegedUser(String email) {
        Config config = getOrCreateConfig();
        config.addPrivilegedUser(email);
        return repository.save(config);
    }

    /**
     * Remove a privileged user.
     */
    public Config removePrivilegedUser(String email) {
        Config config = getOrCreateConfig();
        config.removePrivilegedUser(email);
        return repository.save(config);
    }

    /**
     * Add a blacklisted user.
     */
    public Config addBlacklistedUser(String email) {
        Config config = getOrCreateConfig();
        config.addBlacklistedUser(email);
        return repository.save(config);
    }

    /**
     * Remove a blacklisted user.
     */
    public Config removeBlacklistedUser(String email) {
        Config config = getOrCreateConfig();
        config.removeBlacklistedUser(email);
        return repository.save(config);
    }

    /**
     * Check if a user is privileged.
     */
    @Transactional(readOnly = true)
    public boolean isPrivilegedUser(String email) {
        return repository.isPrivilegedUser(email);
    }

    /**
     * Check if a user is blacklisted.
     */
    @Transactional(readOnly = true)
    public boolean isBlacklistedUser(String email) {
        return repository.isBlacklistedUser(email);
    }

    /**
     * Check if Jira integration is configured.
     */
    @Transactional(readOnly = true)
    public boolean hasJiraIntegration() {
        return repository.hasJiraIntegration();
    }

    /**
     * Check if GitHub integration is configured.
     */
    @Transactional(readOnly = true)
    public boolean hasGithubIntegration() {
        return repository.hasGithubIntegration();
    }

    /**
     * Check if email integration is configured.
     */
    @Transactional(readOnly = true)
    public boolean hasEmailIntegration() {
        return repository.hasEmailIntegration();
    }

    /**
     * Update URL configuration.
     */
    public Config updateUrls(String url, String jiraUrl, String reproductionHelpUrl,
                             String documentationUrl, String bugReportUrl) {
        Config config = getOrCreateConfig();
        
        if (url != null) config.setUrl(url);
        if (jiraUrl != null) config.setJiraUrl(jiraUrl);
        if (reproductionHelpUrl != null) config.setReproductionHelpUrl(reproductionHelpUrl);
        if (documentationUrl != null) config.setDocumentationUrl(documentationUrl);
        if (bugReportUrl != null) config.setBugReportUrl(bugReportUrl);
        
        return repository.save(config);
    }

    /**
     * Update authentication configuration.
     */
    public Config updateAuthentication(String clientCredentials, String jiraCredentials,
                                       String githubCredentials, String buildApiaryKey,
                                       String githubToken, String sendgridApiKey) {
        Config config = getOrCreateConfig();
        
        if (clientCredentials != null) config.setClientCredentials(clientCredentials);
        if (jiraCredentials != null) config.setJiraCredentials(jiraCredentials);
        if (githubCredentials != null) config.setGithubCredentials(githubCredentials);
        if (buildApiaryKey != null) config.setBuildApiaryServiceAccountPrivateKey(buildApiaryKey);
        if (githubToken != null) config.setOssFuzzRobotGithubPersonalAccessToken(githubToken);
        if (sendgridApiKey != null) config.setSendgridApiKey(sendgridApiKey);
        
        return repository.save(config);
    }

    /**
     * Update test account configuration.
     */
    public Config updateTestAccount(String email, String password) {
        Config config = getOrCreateConfig();
        
        if (email != null) config.setTestAccountEmail(email);
        if (password != null) config.setTestAccountPassword(password);
        
        return repository.save(config);
    }

    /**
     * Update WiFi configuration.
     */
    public Config updateWifiConfig(String ssid, String password) {
        Config config = getOrCreateConfig();
        
        if (ssid != null) config.setWifiSsid(ssid);
        if (password != null) config.setWifiPassword(password);
        
        return repository.save(config);
    }

    /**
     * Update Predator integration configuration.
     */
    public Config updatePredatorConfig(String crashTopic, String resultTopic) {
        Config config = getOrCreateConfig();
        
        if (crashTopic != null) config.setPredatorCrashTopic(crashTopic);
        if (resultTopic != null) config.setPredatorResultTopic(resultTopic);
        
        return repository.save(config);
    }

    /**
     * Update limits and thresholds.
     */
    public Config updateLimits(Integer maxTestcasesPerDay, Integer defaultTimeoutSeconds,
                               Long maxUploadSizeBytes) {
        Config config = getOrCreateConfig();
        
        if (maxTestcasesPerDay != null) config.setMaxTestcasesPerDay(maxTestcasesPerDay);
        if (defaultTimeoutSeconds != null) config.setDefaultTimeoutSeconds(defaultTimeoutSeconds);
        if (maxUploadSizeBytes != null) config.setMaxUploadSizeBytes(maxUploadSizeBytes);
        
        return repository.save(config);
    }

    /**
     * Update feature flags.
     */
    public Config updateFeatureFlags(Boolean autoBugFilingEnabled, Boolean securityNotificationsEnabled,
                                     Boolean relaxTestcaseRestrictions, Boolean relaxSecurityBugRestrictions) {
        Config config = getOrCreateConfig();
        
        if (autoBugFilingEnabled != null) config.setAutoBugFilingEnabled(autoBugFilingEnabled);
        if (securityNotificationsEnabled != null) config.setSecurityNotificationsEnabled(securityNotificationsEnabled);
        if (relaxTestcaseRestrictions != null) config.setRelaxTestcaseRestrictions(relaxTestcaseRestrictions);
        if (relaxSecurityBugRestrictions != null) config.setRelaxSecurityBugRestrictions(relaxSecurityBugRestrictions);
        
        return repository.save(config);
    }

    /**
     * Update mappings configuration.
     */
    public Config updateMappings(String componentRepositoryMappings, String platformGroupMappings) {
        Config config = getOrCreateConfig();
        
        if (componentRepositoryMappings != null) config.setComponentRepositoryMappings(componentRepositoryMappings);
        if (platformGroupMappings != null) config.setPlatformGroupMappings(platformGroupMappings);
        
        return repository.save(config);
    }

    /**
     * Get configuration statistics.
     */
    @Transactional(readOnly = true)
    public ConfigStatistics getConfigStatistics() {
        long totalConfigs = repository.count();
        long withRelaxedTestcaseRestrictions = repository.countWithRelaxedTestcaseRestrictions();
        long withRelaxedSecurityRestrictions = repository.countWithRelaxedSecurityRestrictions();
        long withAutoBugFiling = repository.countWithAutoBugFiling();
        long withSecurityNotifications = repository.countWithSecurityNotifications();
        long withJiraIntegration = repository.countWithJiraIntegration();
        long withGithubIntegration = repository.countWithGithubIntegration();
        long withEmailIntegration = repository.countWithEmailIntegration();
        
        return new ConfigStatistics(totalConfigs, withRelaxedTestcaseRestrictions,
                                    withRelaxedSecurityRestrictions, withAutoBugFiling,
                                    withSecurityNotifications, withJiraIntegration,
                                    withGithubIntegration, withEmailIntegration);
    }

    /**
     * Validate configuration completeness.
     */
    @Transactional(readOnly = true)
    public ConfigValidation validateConfig() {
        List<Config> withoutUrl = repository.findWithoutUrl();
        List<Config> withoutContact = repository.findWithoutContactInfo();
        List<Config> incompleteJira = repository.findWithIncompleteJiraConfig();
        List<Config> incompleteWifi = repository.findWithIncompleteWifiConfig();
        List<Config> incompleteTestAccount = repository.findWithIncompleteTestAccount();
        List<Config> withoutIntegrations = repository.findWithoutIntegrations();
        
        boolean isValid = withoutUrl.isEmpty() && withoutContact.isEmpty() &&
                          incompleteJira.isEmpty() && incompleteWifi.isEmpty() &&
                          incompleteTestAccount.isEmpty();
        
        return new ConfigValidation(isValid, withoutUrl, withoutContact, incompleteJira,
                                    incompleteWifi, incompleteTestAccount, withoutIntegrations);
    }

    /**
     * Reset configuration to defaults.
     */
    public Config resetToDefaults() {
        Config config = getOrCreateConfig();
        
        // Store current hash
        String currentHash = calculateConfigHash(config);
        
        // Reset to defaults
        Config defaultConfig = new Config();
        defaultConfig.setId(config.getId());
        defaultConfig.setPreviousHash(currentHash);
        
        return repository.save(defaultConfig);
    }

    /**
     * Calculate configuration hash for change detection.
     */
    private String calculateConfigHash(Config config) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            StringBuilder sb = new StringBuilder();
            
            // Include key configuration fields in hash
            sb.append(config.getUrl() != null ? config.getUrl() : "");
            sb.append(config.getContactString() != null ? config.getContactString() : "");
            sb.append(config.getPrivilegedUsers() != null ? config.getPrivilegedUsers() : "");
            sb.append(config.getBlacklistedUsers() != null ? config.getBlacklistedUsers() : "");
            sb.append(config.getRelaxTestcaseRestrictions());
            sb.append(config.getRelaxSecurityBugRestrictions());
            sb.append(config.getAutoBugFilingEnabled());
            sb.append(config.getSecurityNotificationsEnabled());
            
            byte[] hash = md.digest(sb.toString().getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Get all URLs from configuration.
     */
    @Transactional(readOnly = true)
    public List<String> getAllUrls() {
        return repository.findAllUrls();
    }

    /**
     * Get all contact strings from configuration.
     */
    @Transactional(readOnly = true)
    public List<String> getAllContactStrings() {
        return repository.findAllContactStrings();
    }

    /**
     * Statistics container class.
     */
    public static class ConfigStatistics {
        private final long totalConfigs;
        private final long withRelaxedTestcaseRestrictions;
        private final long withRelaxedSecurityRestrictions;
        private final long withAutoBugFiling;
        private final long withSecurityNotifications;
        private final long withJiraIntegration;
        private final long withGithubIntegration;
        private final long withEmailIntegration;

        public ConfigStatistics(long totalConfigs, long withRelaxedTestcaseRestrictions,
                                long withRelaxedSecurityRestrictions, long withAutoBugFiling,
                                long withSecurityNotifications, long withJiraIntegration,
                                long withGithubIntegration, long withEmailIntegration) {
            this.totalConfigs = totalConfigs;
            this.withRelaxedTestcaseRestrictions = withRelaxedTestcaseRestrictions;
            this.withRelaxedSecurityRestrictions = withRelaxedSecurityRestrictions;
            this.withAutoBugFiling = withAutoBugFiling;
            this.withSecurityNotifications = withSecurityNotifications;
            this.withJiraIntegration = withJiraIntegration;
            this.withGithubIntegration = withGithubIntegration;
            this.withEmailIntegration = withEmailIntegration;
        }

        // Getters
        public long getTotalConfigs() { return totalConfigs; }
        public long getWithRelaxedTestcaseRestrictions() { return withRelaxedTestcaseRestrictions; }
        public long getWithRelaxedSecurityRestrictions() { return withRelaxedSecurityRestrictions; }
        public long getWithAutoBugFiling() { return withAutoBugFiling; }
        public long getWithSecurityNotifications() { return withSecurityNotifications; }
        public long getWithJiraIntegration() { return withJiraIntegration; }
        public long getWithGithubIntegration() { return withGithubIntegration; }
        public long getWithEmailIntegration() { return withEmailIntegration; }
    }

    /**
     * Validation container class.
     */
    public static class ConfigValidation {
        private final boolean isValid;
        private final List<Config> withoutUrl;
        private final List<Config> withoutContact;
        private final List<Config> incompleteJira;
        private final List<Config> incompleteWifi;
        private final List<Config> incompleteTestAccount;
        private final List<Config> withoutIntegrations;

        public ConfigValidation(boolean isValid, List<Config> withoutUrl, List<Config> withoutContact,
                                List<Config> incompleteJira, List<Config> incompleteWifi,
                                List<Config> incompleteTestAccount, List<Config> withoutIntegrations) {
            this.isValid = isValid;
            this.withoutUrl = withoutUrl;
            this.withoutContact = withoutContact;
            this.incompleteJira = incompleteJira;
            this.incompleteWifi = incompleteWifi;
            this.incompleteTestAccount = incompleteTestAccount;
            this.withoutIntegrations = withoutIntegrations;
        }

        // Getters
        public boolean isValid() { return isValid; }
        public List<Config> getWithoutUrl() { return withoutUrl; }
        public List<Config> getWithoutContact() { return withoutContact; }
        public List<Config> getIncompleteJira() { return incompleteJira; }
        public List<Config> getIncompleteWifi() { return incompleteWifi; }
        public List<Config> getIncompleteTestAccount() { return incompleteTestAccount; }
        public List<Config> getWithoutIntegrations() { return withoutIntegrations; }
    }
}