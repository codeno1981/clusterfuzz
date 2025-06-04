package com.google.clusterfuzz.datastore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Lock entity for distributed locking mechanism.
 * Provides coordination between multiple bot instances and prevents race conditions.
 */
@Entity
@Table(name = "locks", indexes = {
    @Index(name = "idx_lock_holder", columnList = "holder"),
    @Index(name = "idx_lock_expiration_time", columnList = "expirationTime"),
    @Index(name = "idx_lock_resource_name", columnList = "resourceName"),
    @Index(name = "idx_lock_active", columnList = "active"),
    @Index(name = "idx_lock_lock_type", columnList = "lockType")
})
public class Lock extends BaseModel {

    @NotNull(message = "Expiration time is required")
    @Column(name = "expiration_time", nullable = false)
    private LocalDateTime expirationTime;

    @NotBlank(message = "Holder is required")
    @Size(max = 255, message = "Holder must not exceed 255 characters")
    @Column(name = "holder", nullable = false)
    private String holder;

    @Size(max = 255, message = "Resource name must not exceed 255 characters")
    @Column(name = "resource_name")
    private String resourceName;

    @Column(name = "lock_type")
    private String lockType;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "acquired_time", nullable = false)
    private LocalDateTime acquiredTime;

    @Column(name = "renewal_count", nullable = false)
    private Integer renewalCount = 0;

    @Column(name = "max_renewals")
    private Integer maxRenewals;

    @Column(name = "purpose", columnDefinition = "TEXT")
    private String purpose;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    // Constructors
    public Lock() {
        super();
        this.acquiredTime = LocalDateTime.now();
    }

    public Lock(String holder, LocalDateTime expirationTime) {
        this();
        this.holder = holder;
        this.expirationTime = expirationTime;
    }

    public Lock(String holder, LocalDateTime expirationTime, String resourceName) {
        this(holder, expirationTime);
        this.resourceName = resourceName;
    }

    public Lock(String holder, Duration duration) {
        this();
        this.holder = holder;
        this.expirationTime = LocalDateTime.now().plus(duration);
    }

    public Lock(String holder, Duration duration, String resourceName) {
        this(holder, duration);
        this.resourceName = resourceName;
    }

    // Getters and Setters
    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }

    public String getHolder() {
        return holder;
    }

    public void setHolder(String holder) {
        this.holder = holder;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getLockType() {
        return lockType;
    }

    public void setLockType(String lockType) {
        this.lockType = lockType;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getAcquiredTime() {
        return acquiredTime;
    }

    public void setAcquiredTime(LocalDateTime acquiredTime) {
        this.acquiredTime = acquiredTime;
    }

    public Integer getRenewalCount() {
        return renewalCount;
    }

    public void setRenewalCount(Integer renewalCount) {
        this.renewalCount = renewalCount;
    }

    public Integer getMaxRenewals() {
        return maxRenewals;
    }

    public void setMaxRenewals(Integer maxRenewals) {
        this.maxRenewals = maxRenewals;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    // Business methods

    /**
     * Check if lock is currently valid (not expired and active).
     */
    public boolean isValid() {
        return active != null && active && 
               expirationTime != null && 
               LocalDateTime.now().isBefore(expirationTime);
    }

    /**
     * Check if lock is expired.
     */
    public boolean isExpired() {
        return expirationTime != null && LocalDateTime.now().isAfter(expirationTime);
    }

    /**
     * Get remaining time until expiration.
     */
    public Duration getTimeUntilExpiration() {
        if (expirationTime == null) {
            return Duration.ZERO;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expirationTime)) {
            return Duration.ZERO;
        }
        return Duration.between(now, expirationTime);
    }

    /**
     * Get duration since lock was acquired.
     */
    public Duration getHoldDuration() {
        if (acquiredTime == null) {
            return Duration.ZERO;
        }
        return Duration.between(acquiredTime, LocalDateTime.now());
    }

    /**
     * Extend lock expiration time.
     */
    public boolean extend(Duration additionalTime) {
        if (!isValid()) {
            return false;
        }
        
        if (maxRenewals != null && renewalCount >= maxRenewals) {
            return false;
        }
        
        this.expirationTime = this.expirationTime.plus(additionalTime);
        this.renewalCount = (this.renewalCount != null ? this.renewalCount : 0) + 1;
        return true;
    }

    /**
     * Renew lock for specified duration from now.
     */
    public boolean renew(Duration duration) {
        if (!isValid()) {
            return false;
        }
        
        if (maxRenewals != null && renewalCount >= maxRenewals) {
            return false;
        }
        
        this.expirationTime = LocalDateTime.now().plus(duration);
        this.renewalCount = (this.renewalCount != null ? this.renewalCount : 0) + 1;
        return true;
    }

    /**
     * Release the lock.
     */
    public void release() {
        this.active = false;
        this.expirationTime = LocalDateTime.now(); // Set to now to mark as expired
    }

    /**
     * Check if lock is owned by specified holder.
     */
    public boolean isOwnedBy(String holderName) {
        return holder != null && holder.equals(holderName);
    }

    /**
     * Check if lock can be renewed.
     */
    public boolean canBeRenewed() {
        return isValid() && (maxRenewals == null || renewalCount < maxRenewals);
    }

    /**
     * Check if lock is about to expire (within specified minutes).
     */
    public boolean isAboutToExpire(int minutes) {
        if (expirationTime == null) {
            return true;
        }
        LocalDateTime threshold = LocalDateTime.now().plusMinutes(minutes);
        return expirationTime.isBefore(threshold);
    }

    /**
     * Check if lock is about to expire (within 5 minutes).
     */
    public boolean isAboutToExpire() {
        return isAboutToExpire(5);
    }

    /**
     * Get lock status summary.
     */
    public String getStatusSummary() {
        StringBuilder status = new StringBuilder();
        
        if (resourceName != null) {
            status.append("Lock on ").append(resourceName);
        } else {
            status.append("Lock");
        }
        
        status.append(" held by ").append(holder);
        
        if (isValid()) {
            status.append(" - ACTIVE");
            Duration remaining = getTimeUntilExpiration();
            status.append(" (expires in ").append(formatDuration(remaining)).append(")");
        } else if (isExpired()) {
            status.append(" - EXPIRED");
        } else {
            status.append(" - INACTIVE");
        }
        
        if (renewalCount > 0) {
            status.append(" [renewed ").append(renewalCount).append(" times]");
        }
        
        return status.toString();
    }

    /**
     * Format duration for display.
     */
    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }

    /**
     * Create a lock key for resource-based locking.
     */
    public static String createLockKey(String resourceType, String resourceId) {
        return resourceType + ":" + resourceId;
    }

    /**
     * Check if this is a resource-specific lock.
     */
    public boolean isResourceLock() {
        return resourceName != null && !resourceName.trim().isEmpty();
    }

    /**
     * Check if this is a global lock.
     */
    public boolean isGlobalLock() {
        return !isResourceLock();
    }

    /**
     * Get lock age.
     */
    public Duration getLockAge() {
        return getHoldDuration();
    }

    /**
     * Check if lock is long-running (held for more than specified hours).
     */
    public boolean isLongRunning(int hours) {
        return getHoldDuration().toHours() > hours;
    }

    /**
     * Check if lock is long-running (held for more than 1 hour).
     */
    public boolean isLongRunning() {
        return isLongRunning(1);
    }

    /**
     * Force expire the lock.
     */
    public void forceExpire() {
        this.expirationTime = LocalDateTime.now().minusSeconds(1);
        this.active = false;
    }

    @Override
    public String toString() {
        return "Lock{" +
                "id=" + getId() +
                ", holder='" + holder + '\'' +
                ", resourceName='" + resourceName + '\'' +
                ", expirationTime=" + expirationTime +
                ", active=" + active +
                ", valid=" + isValid() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lock)) return false;
        if (!super.equals(o)) return false;

        Lock lock = (Lock) o;
        
        if (holder != null ? !holder.equals(lock.holder) : lock.holder != null) return false;
        return resourceName != null ? resourceName.equals(lock.resourceName) : lock.resourceName == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (holder != null ? holder.hashCode() : 0);
        result = 31 * result + (resourceName != null ? resourceName.hashCode() : 0);
        return result;
    }
}