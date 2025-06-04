package com.google.clusterfuzz.datastore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * ExternalUserPermission entity for managing permissions for external users.
 * Controls what entities external users can view and their auto-CC preferences.
 */
@Entity
@Table(name = "external_user_permissions", indexes = {
    @Index(name = "idx_external_user_email", columnList = "email"),
    @Index(name = "idx_external_user_entity_kind", columnList = "entityKind"),
    @Index(name = "idx_external_user_entity_name", columnList = "entityName"),
    @Index(name = "idx_external_user_email_entity", columnList = "email, entityKind, entityName", unique = true),
    @Index(name = "idx_external_user_auto_cc", columnList = "autoCc"),
    @Index(name = "idx_external_user_prefix", columnList = "isPrefix")
})
public class ExternalUserPermission extends BaseModel {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column(name = "email", nullable = false)
    private String email;

    @NotNull(message = "Entity kind is required")
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "entity_kind", nullable = false)
    private PermissionEntityKind entityKind;

    @NotBlank(message = "Entity name is required")
    @Size(max = 255, message = "Entity name must not exceed 255 characters")
    @Column(name = "entity_name", nullable = false)
    private String entityName;

    @Column(name = "is_prefix", nullable = false)
    private Boolean isPrefix = false;

    @NotNull(message = "Auto CC type is required")
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "auto_cc", nullable = false)
    private AutoCCType autoCc = AutoCCType.NONE;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "granted_by")
    private String grantedBy;

    // Constructors
    public ExternalUserPermission() {
        super();
    }

    public ExternalUserPermission(String email, PermissionEntityKind entityKind, String entityName) {
        super();
        this.email = email;
        this.entityKind = entityKind;
        this.entityName = entityName;
    }

    public ExternalUserPermission(String email, PermissionEntityKind entityKind, String entityName, 
                                 Boolean isPrefix, AutoCCType autoCc) {
        this(email, entityKind, entityName);
        this.isPrefix = isPrefix;
        this.autoCc = autoCc;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public PermissionEntityKind getEntityKind() {
        return entityKind;
    }

    public void setEntityKind(PermissionEntityKind entityKind) {
        this.entityKind = entityKind;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Boolean getIsPrefix() {
        return isPrefix;
    }

    public void setIsPrefix(Boolean isPrefix) {
        this.isPrefix = isPrefix;
    }

    public AutoCCType getAutoCc() {
        return autoCc;
    }

    public void setAutoCc(AutoCCType autoCc) {
        this.autoCc = autoCc;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGrantedBy() {
        return grantedBy;
    }

    public void setGrantedBy(String grantedBy) {
        this.grantedBy = grantedBy;
    }

    // Business methods

    /**
     * Check if this permission allows access to the specified entity.
     */
    public boolean allowsAccessTo(String targetEntityName) {
        if (!isActive()) {
            return false;
        }
        
        if (targetEntityName == null || entityName == null) {
            return false;
        }
        
        if (isPrefix != null && isPrefix) {
            return targetEntityName.startsWith(entityName);
        } else {
            return targetEntityName.equals(entityName);
        }
    }

    /**
     * Check if this permission is active.
     */
    public boolean isActive() {
        return active != null && active;
    }

    /**
     * Check if this permission should auto-CC for security issues.
     */
    public boolean shouldAutoCCForSecurity() {
        return isActive() && autoCc != null && autoCc.shouldCCForSecurity();
    }

    /**
     * Check if this permission should auto-CC for non-security issues.
     */
    public boolean shouldAutoCCForNonSecurity() {
        return isActive() && autoCc != null && autoCc.shouldCCForNonSecurity();
    }

    /**
     * Check if this permission should auto-CC for any issues.
     */
    public boolean shouldAutoCC() {
        return isActive() && autoCc != null && autoCc.shouldCC();
    }

    /**
     * Check if this permission should auto-CC for the specified issue type.
     */
    public boolean shouldAutoCCForIssue(boolean isSecurityIssue) {
        if (!isActive()) {
            return false;
        }
        
        if (isSecurityIssue) {
            return shouldAutoCCForSecurity();
        } else {
            return shouldAutoCCForNonSecurity();
        }
    }

    /**
     * Check if this is a prefix-based permission.
     */
    public boolean isPrefixPermission() {
        return isPrefix != null && isPrefix;
    }

    /**
     * Check if this is an exact match permission.
     */
    public boolean isExactMatchPermission() {
        return !isPrefixPermission();
    }

    /**
     * Get permission scope description.
     */
    public String getScopeDescription() {
        StringBuilder scope = new StringBuilder();
        
        if (entityKind != null) {
            scope.append(entityKind.getDisplayName()).append(": ");
        }
        
        scope.append(entityName);
        
        if (isPrefixPermission()) {
            scope.append("*");
        }
        
        return scope.toString();
    }

    /**
     * Get auto-CC description.
     */
    public String getAutoCCDescription() {
        if (autoCc == null) {
            return "No auto-CC";
        }
        return autoCc.getDescription();
    }

    /**
     * Get permission summary.
     */
    public String getPermissionSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(email).append(" can access ");
        summary.append(getScopeDescription());
        
        if (shouldAutoCC()) {
            summary.append(" (Auto-CC: ").append(autoCc.getDisplayName()).append(")");
        }
        
        if (!isActive()) {
            summary.append(" [INACTIVE]");
        }
        
        return summary.toString();
    }

    /**
     * Validate entity name format based on entity kind.
     */
    public boolean isValidEntityName() {
        if (entityName == null || entityName.trim().isEmpty()) {
            return false;
        }
        
        if (entityKind == null) {
            return false;
        }
        
        // Basic validation - could be enhanced based on specific entity kind requirements
        switch (entityKind) {
            case FUZZER:
                return entityName.matches("^[a-zA-Z0-9_-]+$");
            case JOB:
                return entityName.matches("^[a-zA-Z0-9_-]+$");
            case UPLOADER:
                return true; // More flexible for uploader names
            default:
                return true;
        }
    }

    /**
     * Check if permission covers multiple entities (prefix-based).
     */
    public boolean coversMultipleEntities() {
        return isPrefixPermission();
    }

    /**
     * Deactivate permission.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Activate permission.
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Update auto-CC preference.
     */
    public void updateAutoCCPreference(AutoCCType newAutoCc) {
        this.autoCc = newAutoCc != null ? newAutoCc : AutoCCType.NONE;
    }

    /**
     * Check if this permission is more permissive than another.
     */
    public boolean isMorePermissiveThan(ExternalUserPermission other) {
        if (other == null || !this.entityKind.equals(other.entityKind)) {
            return false;
        }
        
        // Prefix permissions are generally more permissive
        if (this.isPrefixPermission() && !other.isPrefixPermission()) {
            return other.entityName.startsWith(this.entityName);
        }
        
        return false;
    }

    /**
     * Get normalized email (lowercase).
     */
    public String getNormalizedEmail() {
        return email != null ? email.toLowerCase().trim() : null;
    }

    /**
     * Check if email matches (case-insensitive).
     */
    public boolean hasEmail(String emailToCheck) {
        if (emailToCheck == null || email == null) {
            return false;
        }
        return email.equalsIgnoreCase(emailToCheck.trim());
    }

    @Override
    public String toString() {
        return "ExternalUserPermission{" +
                "id=" + getId() +
                ", email='" + email + '\'' +
                ", entityKind=" + entityKind +
                ", entityName='" + entityName + '\'' +
                ", isPrefix=" + isPrefix +
                ", autoCc=" + autoCc +
                ", active=" + active +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExternalUserPermission)) return false;
        if (!super.equals(o)) return false;

        ExternalUserPermission that = (ExternalUserPermission) o;
        
        if (email != null ? !email.equalsIgnoreCase(that.email) : that.email != null) return false;
        if (entityKind != that.entityKind) return false;
        return entityName != null ? entityName.equals(that.entityName) : that.entityName == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (email != null ? email.toLowerCase().hashCode() : 0);
        result = 31 * result + (entityKind != null ? entityKind.hashCode() : 0);
        result = 31 * result + (entityName != null ? entityName.hashCode() : 0);
        return result;
    }
}