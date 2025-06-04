package com.google.clusterfuzz.datastore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * Represents system configuration settings.
 * This is typically a singleton entity that stores global configuration parameters.
 */
@Entity
@Table(name = "config")
public class Config extends BaseModel {

    /**
     * Previous configuration hash for change detection.
     */
    @Column(name = "previous_hash", length = 128)
    @Size(max = 128, message = "Previous hash must not exceed 128 characters")
    private String previousHash = "";

    /**
     * Project's main URL.
     */
    @Column(name = "url", length = 500)
    @Size(max = 500, message = "URL must not exceed 500 characters")
    private String url = "";

    /**
     * Issue tracker client authentication parameters (JSON).
     */
    @Column(name = "client_credentials", columnDefinition = "TEXT")
    private String clientCredentials = "";

    /**
     * Jira URL for issue tracking.
     */
    @Column(name = "jira_url", length = 500)
    @Size(max = 500, message = "Jira URL must not exceed 500 characters")
    private String jiraUrl = "";

    /**
     * Jira credentials (JSON).
     */
    @Column(name = "jira_credentials", columnDefinition = "TEXT")
    private String jiraCredentials = "";

    /**
     * Build apiary service account private key.
     */
    @Column(name = "build_apiary_service_account_private_key", columnDefinition = "TEXT")
    private String buildApiaryServiceAccountPrivateKey = "";

    /**
     * Google test account email for login, GMS testing, etc.
     */
    @Column(name = "test_account_email", length = 255)
    @Email(message = "Invalid test account email format")
    @Size(max = 255, message = "Test account email must not exceed 255 characters")
    private String testAccountEmail = "";

    /**
     * Google test account password.
     */
    @Column(name = "test_account_password", length = 255)
    @Size(max = 255, message = "Test account password must not exceed 255 characters")
    private String testAccountPassword = "";

    /**
     * Privileged users list (comma-separated emails).
     */
    @Column(name = "privileged_users", columnDefinition = "TEXT")
    private String privilegedUsers = "";

    /**
     * Blacklisted users list (comma-separated emails).
     */
    @Column(name = "blacklisted_users", columnDefinition = "TEXT")
    private String blacklistedUsers = "";

    /**
     * Admin contact information.
     */
    @Column(name = "contact_string", length = 500)
    @Size(max = 500, message = "Contact string must not exceed 500 characters")
    private String contactString = "";

    /**
     * Component to repository mappings (JSON).
     */
    @Column(name = "component_repository_mappings", columnDefinition = "TEXT")
    private String componentRepositoryMappings = "";

    /**
     * URL for help page for reproducing issues.
     */
    @Column(name = "reproduction_help_url", length = 500)
    @Size(max = 500, message = "Reproduction help URL must not exceed 500 characters")
    private String reproductionHelpUrl = "";

    /**
     * Documentation URL.
     */
    @Column(name = "documentation_url", length = 500)
    @Size(max = 500, message = "Documentation URL must not exceed 500 characters")
    private String documentationUrl = "";

    /**
     * Bug report URL.
     */
    @Column(name = "bug_report_url", length = 500)
    @Size(max = 500, message = "Bug report URL must not exceed 500 characters")
    private String bugReportUrl = "";

    /**
     * Platform group mappings (JSON).
     */
    @Column(name = "platform_group_mappings", columnDefinition = "TEXT")
    private String platformGroupMappings = "";

    /**
     * More relaxed restrictions: allow CC'ed users and reporters of issues to view testcase details.
     */
    @Column(name = "relax_testcase_restrictions", nullable = false)
    private Boolean relaxTestcaseRestrictions = false;

    /**
     * More relaxed restrictions: allow domain users to access both security and functional bugs.
     */
    @Column(name = "relax_security_bug_restrictions", nullable = false)
    private Boolean relaxSecurityBugRestrictions = false;

    /**
     * Coverage reports bucket name.
     */
    @Column(name = "coverage_reports_bucket", length = 100)
    @Size(max = 100, message = "Coverage reports bucket must not exceed 100 characters")
    private String coverageReportsBucket = "";

    /**
     * GitHub API credentials.
     */
    @Column(name = "github_credentials", length = 1000)
    @Size(max = 1000, message = "GitHub credentials must not exceed 1000 characters")
    private String githubCredentials = "";

    /**
     * OSS-Fuzz robot GitHub personal access token.
     */
    @Column(name = "oss_fuzz_robot_github_personal_access_token", length = 500)
    @Size(max = 500, message = "GitHub PAT must not exceed 500 characters")
    private String ossFuzzRobotGithubPersonalAccessToken = "";

    /**
     * Pub/Sub topic for the Predator crash service.
     */
    @Column(name = "predator_crash_topic", length = 200)
    @Size(max = 200, message = "Predator crash topic must not exceed 200 characters")
    private String predatorCrashTopic = "";

    /**
     * Pub/Sub topic for the Predator result service.
     */
    @Column(name = "predator_result_topic", length = 200)
    @Size(max = 200, message = "Predator result topic must not exceed 200 characters")
    private String predatorResultTopic = "";

    /**
     * WiFi SSID for device testing.
     */
    @Column(name = "wifi_ssid", length = 100)
    @Size(max = 100, message = "WiFi SSID must not exceed 100 characters")
    private String wifiSsid = "";

    /**
     * WiFi password for device testing.
     */
    @Column(name = "wifi_password", length = 255)
    @Size(max = 255, message = "WiFi password must not exceed 255 characters")
    private String wifiPassword = "";

    /**
     * SendGrid API key for email notifications.
     */
    @Column(name = "sendgrid_api_key", length = 500)
    @Size(max = 500, message = "SendGrid API key must not exceed 500 characters")
    private String sendgridApiKey = "";

    /**
     * Maximum number of testcases to process per day.
     */
    @Column(name = "max_testcases_per_day")
    private Integer maxTestcasesPerDay;

    /**
     * Default timeout for testcase execution in seconds.
     */
    @Column(name = "default_timeout_seconds")
    private Integer defaultTimeoutSeconds;

    /**
     * Maximum file size for uploads in bytes.
     */
    @Column(name = "max_upload_size_bytes")
    private Long maxUploadSizeBytes;

    /**
     * Enable/disable automatic bug filing.
     */
    @Column(name = "auto_bug_filing_enabled", nullable = false)
    private Boolean autoBugFilingEnabled = true;

    /**
     * Enable/disable security bug notifications.
     */
    @Column(name = "security_notifications_enabled", nullable = false)
    private Boolean securityNotificationsEnabled = true;

    /**
     * Default constructor for JPA.
     */
    public Config() {
        super();
    }

    /**
     * Check if a user is privileged.
     */
    public boolean isPrivilegedUser(String email) {
        if (email == null || privilegedUsers == null || privilegedUsers.isEmpty()) {
            return false;
        }
        String[] users = privilegedUsers.split(",");
        for (String user : users) {
            if (user.trim().equalsIgnoreCase(email.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a user is blacklisted.
     */
    public boolean isBlacklistedUser(String email) {
        if (email == null || blacklistedUsers == null || blacklistedUsers.isEmpty()) {
            return false;
        }
        String[] users = blacklistedUsers.split(",");
        for (String user : users) {
            if (user.trim().equalsIgnoreCase(email.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add a privileged user.
     */
    public void addPrivilegedUser(String email) {
        if (email == null || email.trim().isEmpty()) {
            return;
        }
        
        if (isPrivilegedUser(email)) {
            return; // Already exists
        }
        
        if (privilegedUsers == null || privilegedUsers.isEmpty()) {
            privilegedUsers = email.trim();
        } else {
            privilegedUsers = privilegedUsers + "," + email.trim();
        }
    }

    /**
     * Remove a privileged user.
     */
    public void removePrivilegedUser(String email) {
        if (email == null || privilegedUsers == null) {
            return;
        }
        
        String[] users = privilegedUsers.split(",");
        StringBuilder newUsers = new StringBuilder();
        
        for (String user : users) {
            if (!user.trim().equalsIgnoreCase(email.trim())) {
                if (newUsers.length() > 0) {
                    newUsers.append(",");
                }
                newUsers.append(user.trim());
            }
        }
        
        privilegedUsers = newUsers.toString();
    }

    /**
     * Add a blacklisted user.
     */
    public void addBlacklistedUser(String email) {
        if (email == null || email.trim().isEmpty()) {
            return;
        }
        
        if (isBlacklistedUser(email)) {
            return; // Already exists
        }
        
        if (blacklistedUsers == null || blacklistedUsers.isEmpty()) {
            blacklistedUsers = email.trim();
        } else {
            blacklistedUsers = blacklistedUsers + "," + email.trim();
        }
    }

    /**
     * Remove a blacklisted user.
     */
    public void removeBlacklistedUser(String email) {
        if (email == null || blacklistedUsers == null) {
            return;
        }
        
        String[] users = blacklistedUsers.split(",");
        StringBuilder newUsers = new StringBuilder();
        
        for (String user : users) {
            if (!user.trim().equalsIgnoreCase(email.trim())) {
                if (newUsers.length() > 0) {
                    newUsers.append(",");
                }
                newUsers.append(user.trim());
            }
        }
        
        blacklistedUsers = newUsers.toString();
    }

    /**
     * Check if external integrations are configured.
     */
    public boolean hasJiraIntegration() {
        return jiraUrl != null && !jiraUrl.isEmpty() && 
               jiraCredentials != null && !jiraCredentials.isEmpty();
    }

    /**
     * Check if GitHub integration is configured.
     */
    public boolean hasGithubIntegration() {
        return githubCredentials != null && !githubCredentials.isEmpty();
    }

    /**
     * Check if email notifications are configured.
     */
    public boolean hasEmailNotifications() {
        return sendgridApiKey != null && !sendgridApiKey.isEmpty();
    }

    /**
     * Check if Predator integration is configured.
     */
    public boolean hasPredatorIntegration() {
        return predatorCrashTopic != null && !predatorCrashTopic.isEmpty() &&
               predatorResultTopic != null && !predatorResultTopic.isEmpty();
    }

    // Getters and Setters
    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash != null ? previousHash : "";
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url != null ? url : "";
    }

    public String getClientCredentials() {
        return clientCredentials;
    }

    public void setClientCredentials(String clientCredentials) {
        this.clientCredentials = clientCredentials != null ? clientCredentials : "";
    }

    public String getJiraUrl() {
        return jiraUrl;
    }

    public void setJiraUrl(String jiraUrl) {
        this.jiraUrl = jiraUrl != null ? jiraUrl : "";
    }

    public String getJiraCredentials() {
        return jiraCredentials;
    }

    public void setJiraCredentials(String jiraCredentials) {
        this.jiraCredentials = jiraCredentials != null ? jiraCredentials : "";
    }

    public String getBuildApiaryServiceAccountPrivateKey() {
        return buildApiaryServiceAccountPrivateKey;
    }

    public void setBuildApiaryServiceAccountPrivateKey(String buildApiaryServiceAccountPrivateKey) {
        this.buildApiaryServiceAccountPrivateKey = buildApiaryServiceAccountPrivateKey != null ? buildApiaryServiceAccountPrivateKey : "";
    }

    public String getTestAccountEmail() {
        return testAccountEmail;
    }

    public void setTestAccountEmail(String testAccountEmail) {
        this.testAccountEmail = testAccountEmail != null ? testAccountEmail : "";
    }

    public String getTestAccountPassword() {
        return testAccountPassword;
    }

    public void setTestAccountPassword(String testAccountPassword) {
        this.testAccountPassword = testAccountPassword != null ? testAccountPassword : "";
    }

    public String getPrivilegedUsers() {
        return privilegedUsers;
    }

    public void setPrivilegedUsers(String privilegedUsers) {
        this.privilegedUsers = privilegedUsers != null ? privilegedUsers : "";
    }

    public String getBlacklistedUsers() {
        return blacklistedUsers;
    }

    public void setBlacklistedUsers(String blacklistedUsers) {
        this.blacklistedUsers = blacklistedUsers != null ? blacklistedUsers : "";
    }

    public String getContactString() {
        return contactString;
    }

    public void setContactString(String contactString) {
        this.contactString = contactString != null ? contactString : "";
    }

    public String getComponentRepositoryMappings() {
        return componentRepositoryMappings;
    }

    public void setComponentRepositoryMappings(String componentRepositoryMappings) {
        this.componentRepositoryMappings = componentRepositoryMappings != null ? componentRepositoryMappings : "";
    }

    public String getReproductionHelpUrl() {
        return reproductionHelpUrl;
    }

    public void setReproductionHelpUrl(String reproductionHelpUrl) {
        this.reproductionHelpUrl = reproductionHelpUrl != null ? reproductionHelpUrl : "";
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl != null ? documentationUrl : "";
    }

    public String getBugReportUrl() {
        return bugReportUrl;
    }

    public void setBugReportUrl(String bugReportUrl) {
        this.bugReportUrl = bugReportUrl != null ? bugReportUrl : "";
    }

    public String getPlatformGroupMappings() {
        return platformGroupMappings;
    }

    public void setPlatformGroupMappings(String platformGroupMappings) {
        this.platformGroupMappings = platformGroupMappings != null ? platformGroupMappings : "";
    }

    public Boolean getRelaxTestcaseRestrictions() {
        return relaxTestcaseRestrictions;
    }

    public void setRelaxTestcaseRestrictions(Boolean relaxTestcaseRestrictions) {
        this.relaxTestcaseRestrictions = relaxTestcaseRestrictions != null ? relaxTestcaseRestrictions : false;
    }

    public Boolean getRelaxSecurityBugRestrictions() {
        return relaxSecurityBugRestrictions;
    }

    public void setRelaxSecurityBugRestrictions(Boolean relaxSecurityBugRestrictions) {
        this.relaxSecurityBugRestrictions = relaxSecurityBugRestrictions != null ? relaxSecurityBugRestrictions : false;
    }

    public String getCoverageReportsBucket() {
        return coverageReportsBucket;
    }

    public void setCoverageReportsBucket(String coverageReportsBucket) {
        this.coverageReportsBucket = coverageReportsBucket != null ? coverageReportsBucket : "";
    }

    public String getGithubCredentials() {
        return githubCredentials;
    }

    public void setGithubCredentials(String githubCredentials) {
        this.githubCredentials = githubCredentials != null ? githubCredentials : "";
    }

    public String getOssFuzzRobotGithubPersonalAccessToken() {
        return ossFuzzRobotGithubPersonalAccessToken;
    }

    public void setOssFuzzRobotGithubPersonalAccessToken(String ossFuzzRobotGithubPersonalAccessToken) {
        this.ossFuzzRobotGithubPersonalAccessToken = ossFuzzRobotGithubPersonalAccessToken != null ? ossFuzzRobotGithubPersonalAccessToken : "";
    }

    public String getPredatorCrashTopic() {
        return predatorCrashTopic;
    }

    public void setPredatorCrashTopic(String predatorCrashTopic) {
        this.predatorCrashTopic = predatorCrashTopic != null ? predatorCrashTopic : "";
    }

    public String getPredatorResultTopic() {
        return predatorResultTopic;
    }

    public void setPredatorResultTopic(String predatorResultTopic) {
        this.predatorResultTopic = predatorResultTopic != null ? predatorResultTopic : "";
    }

    public String getWifiSsid() {
        return wifiSsid;
    }

    public void setWifiSsid(String wifiSsid) {
        this.wifiSsid = wifiSsid != null ? wifiSsid : "";
    }

    public String getWifiPassword() {
        return wifiPassword;
    }

    public void setWifiPassword(String wifiPassword) {
        this.wifiPassword = wifiPassword != null ? wifiPassword : "";
    }

    public String getSendgridApiKey() {
        return sendgridApiKey;
    }

    public void setSendgridApiKey(String sendgridApiKey) {
        this.sendgridApiKey = sendgridApiKey != null ? sendgridApiKey : "";
    }

    public Integer getMaxTestcasesPerDay() {
        return maxTestcasesPerDay;
    }

    public void setMaxTestcasesPerDay(Integer maxTestcasesPerDay) {
        this.maxTestcasesPerDay = maxTestcasesPerDay;
    }

    public Integer getDefaultTimeoutSeconds() {
        return defaultTimeoutSeconds;
    }

    public void setDefaultTimeoutSeconds(Integer defaultTimeoutSeconds) {
        this.defaultTimeoutSeconds = defaultTimeoutSeconds;
    }

    public Long getMaxUploadSizeBytes() {
        return maxUploadSizeBytes;
    }

    public void setMaxUploadSizeBytes(Long maxUploadSizeBytes) {
        this.maxUploadSizeBytes = maxUploadSizeBytes;
    }

    public Boolean getAutoBugFilingEnabled() {
        return autoBugFilingEnabled;
    }

    public void setAutoBugFilingEnabled(Boolean autoBugFilingEnabled) {
        this.autoBugFilingEnabled = autoBugFilingEnabled != null ? autoBugFilingEnabled : true;
    }

    public Boolean getSecurityNotificationsEnabled() {
        return securityNotificationsEnabled;
    }

    public void setSecurityNotificationsEnabled(Boolean securityNotificationsEnabled) {
        this.securityNotificationsEnabled = securityNotificationsEnabled != null ? securityNotificationsEnabled : true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Config config = (Config) o;
        return Objects.equals(url, config.url) &&
               Objects.equals(contactString, config.contactString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), url, contactString);
    }

    @Override
    public String toString() {
        return "Config{" +
               "id=" + getId() +
               ", url='" + url + '\'' +
               ", contactString='" + contactString + '\'' +
               ", relaxTestcaseRestrictions=" + relaxTestcaseRestrictions +
               ", relaxSecurityBugRestrictions=" + relaxSecurityBugRestrictions +
               ", autoBugFilingEnabled=" + autoBugFilingEnabled +
               ", securityNotificationsEnabled=" + securityNotificationsEnabled +
               '}';
    }
}