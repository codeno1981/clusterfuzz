package com.google.clusterfuzz.datastore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Admin entity for recording admin users in the system.
 * Tracks users with administrative privileges and their access history.
 */
@Entity
@Table(name = "admins", indexes = {
    @Index(name = "idx_admin_email", columnList = "email", unique = true),
    @Index(name = "idx_admin_active", columnList = "active"),
    @Index(name = "idx_admin_last_access", columnList = "lastAccessTime"),
    @Index(name = "idx_admin_role", columnList = "role")
})
public class Admin extends BaseModel {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "role")
    private String role;

    @Column(name = "last_access_time")
    private LocalDateTime lastAccessTime;

    @Column(name = "access_count", nullable = false)
    private Long accessCount = 0L;

    @Column(name = "granted_by")
    private String grantedBy;

    @Column(name = "granted_time")
    private LocalDateTime grantedTime;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "permissions", columnDefinition = "TEXT")
    private String permissions;

    // Constructors
    public Admin() {
        super();
        this.grantedTime = LocalDateTime.now();
    }

    public Admin(String email) {
        this();
        this.email = email;
    }

    public Admin(String email, String grantedBy) {
        this(email);
        this.grantedBy = grantedBy;
    }

    public Admin(String email, String role, String grantedBy) {
        this(email, grantedBy);
        this.role = role;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(LocalDateTime lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public Long getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(Long accessCount) {
        this.accessCount = accessCount;
    }

    public String getGrantedBy() {
        return grantedBy;
    }

    public void setGrantedBy(String grantedBy) {
        this.grantedBy = grantedBy;
    }

    public LocalDateTime getGrantedTime() {
        return grantedTime;
    }

    public void setGrantedTime(LocalDateTime grantedTime) {
        this.grantedTime = grantedTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    // Business methods

    /**
     * Check if admin is currently active.
     */
    public boolean isActive() {
        return active != null && active;
    }

    /**
     * Record admin access.
     */
    public void recordAccess() {
        this.lastAccessTime = LocalDateTime.now();
        this.accessCount = (this.accessCount != null ? this.accessCount : 0L) + 1;
    }

    /**
     * Check if admin has accessed the system recently (within specified days).
     */
    public boolean hasRecentAccess(int days) {
        if (lastAccessTime == null) {
            return false;
        }
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        return lastAccessTime.isAfter(cutoff);
    }

    /**
     * Check if admin has accessed the system recently (within 30 days).
     */
    public boolean hasRecentAccess() {
        return hasRecentAccess(30);
    }

    /**
     * Check if admin is a frequent user (more than specified access count).
     */
    public boolean isFrequentUser(long minAccessCount) {
        return accessCount != null && accessCount >= minAccessCount;
    }

    /**
     * Check if admin is a frequent user (more than 10 accesses).
     */
    public boolean isFrequentUser() {
        return isFrequentUser(10);
    }

    /**
     * Check if admin has never accessed the system.
     */
    public boolean hasNeverAccessed() {
        return lastAccessTime == null || (accessCount != null && accessCount == 0);
    }

    /**
     * Check if admin is inactive (not accessed recently).
     */
    public boolean isInactive(int days) {
        return !hasRecentAccess(days);
    }

    /**
     * Check if admin is inactive (not accessed in 90 days).
     */
    public boolean isInactive() {
        return isInactive(90);
    }

    /**
     * Deactivate admin.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Activate admin.
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Check if admin has specific role.
     */
    public boolean hasRole(String roleName) {
        return role != null && role.equalsIgnoreCase(roleName);
    }

    /**
     * Check if admin is super admin.
     */
    public boolean isSuperAdmin() {
        return hasRole("super") || hasRole("superadmin");
    }

    /**
     * Check if admin is system admin.
     */
    public boolean isSystemAdmin() {
        return hasRole("system") || hasRole("sysadmin");
    }

    /**
     * Check if admin has specific permission.
     */
    public boolean hasPermission(String permission) {
        if (permissions == null || permission == null) {
            return false;
        }
        return permissions.toLowerCase().contains(permission.toLowerCase());
    }

    /**
     * Add permission to admin.
     */
    public void addPermission(String permission) {
        if (permission == null || permission.trim().isEmpty()) {
            return;
        }
        
        if (permissions == null || permissions.trim().isEmpty()) {
            permissions = permission;
        } else if (!hasPermission(permission)) {
            permissions += "," + permission;
        }
    }

    /**
     * Remove permission from admin.
     */
    public void removePermission(String permission) {
        if (permissions == null || permission == null) {
            return;
        }
        
        String[] perms = permissions.split(",");
        StringBuilder newPermissions = new StringBuilder();
        
        for (String perm : perms) {
            if (!perm.trim().equalsIgnoreCase(permission.trim())) {
                if (newPermissions.length() > 0) {
                    newPermissions.append(",");
                }
                newPermissions.append(perm.trim());
            }
        }
        
        permissions = newPermissions.toString();
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

    /**
     * Get admin status summary.
     */
    public String getStatusSummary() {
        StringBuilder status = new StringBuilder();
        status.append("Admin: ").append(email);
        
        if (role != null) {
            status.append(" (").append(role).append(")");
        }
        
        if (isActive()) {
            status.append(" - ACTIVE");
        } else {
            status.append(" - INACTIVE");
        }
        
        if (hasRecentAccess()) {
            status.append(" - RECENT ACCESS");
        } else if (hasNeverAccessed()) {
            status.append(" - NEVER ACCESSED");
        } else {
            status.append(" - STALE");
        }
        
        if (accessCount != null && accessCount > 0) {
            status.append(" (").append(accessCount).append(" accesses)");
        }
        
        return status.toString();
    }

    /**
     * Get days since last access.
     */
    public long getDaysSinceLastAccess() {
        if (lastAccessTime == null) {
            return Long.MAX_VALUE;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(lastAccessTime, LocalDateTime.now());
    }

    /**
     * Get days since granted admin privileges.
     */
    public long getDaysSinceGranted() {
        if (grantedTime == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(grantedTime, LocalDateTime.now());
    }

    /**
     * Check if admin account is new (granted within specified days).
     */
    public boolean isNewAdmin(int days) {
        return getDaysSinceGranted() <= days;
    }

    /**
     * Check if admin account is new (granted within 7 days).
     */
    public boolean isNewAdmin() {
        return isNewAdmin(7);
    }

    /**
     * Update admin notes.
     */
    public void updateNotes(String newNotes) {
        this.notes = newNotes;
    }

    /**
     * Append to admin notes.
     */
    public void appendNotes(String additionalNotes) {
        if (additionalNotes == null || additionalNotes.trim().isEmpty()) {
            return;
        }
        
        if (notes == null || notes.trim().isEmpty()) {
            notes = additionalNotes;
        } else {
            notes += "\n" + additionalNotes;
        }
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id=" + getId() +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                ", accessCount=" + accessCount +
                ", lastAccess=" + lastAccessTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Admin)) return false;
        if (!super.equals(o)) return false;

        Admin admin = (Admin) o;
        return email != null ? email.equalsIgnoreCase(admin.email) : admin.email == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (email != null ? email.toLowerCase().hashCode() : 0);
        return result;
    }
}