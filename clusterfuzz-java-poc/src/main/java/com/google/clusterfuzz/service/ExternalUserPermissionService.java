package com.google.clusterfuzz.service;

import com.google.clusterfuzz.datastore.model.AutoCCType;
import com.google.clusterfuzz.datastore.model.ExternalUserPermission;
import com.google.clusterfuzz.datastore.model.PermissionEntityKind;
import com.google.clusterfuzz.datastore.repository.ExternalUserPermissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for ExternalUserPermission entity operations.
 * Provides business logic for external user permission management.
 */
@Service
@Transactional
public class ExternalUserPermissionService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalUserPermissionService.class);

    @Autowired
    private ExternalUserPermissionRepository permissionRepository;

    // Permission creation and management

    /**
     * Grant permission to a user for an entity.
     */
    public ExternalUserPermission grantPermission(String email, PermissionEntityKind entityKind, 
                                                 String entityName, String grantedBy) {
        return grantPermission(email, entityKind, entityName, grantedBy, AutoCCType.NONE, false, null);
    }

    /**
     * Grant permission with auto-CC preference.
     */
    public ExternalUserPermission grantPermission(String email, PermissionEntityKind entityKind, 
                                                 String entityName, String grantedBy, AutoCCType autoCc) {
        return grantPermission(email, entityKind, entityName, grantedBy, autoCc, false, null);
    }

    /**
     * Grant permission with full configuration.
     */
    public ExternalUserPermission grantPermission(String email, PermissionEntityKind entityKind, 
                                                 String entityName, String grantedBy, AutoCCType autoCc,
                                                 boolean isPrefix, String description) {
        logger.info("Granting permission for user '{}' on entity '{}' of kind '{}'", 
                   email, entityName, entityKind);
        
        // Check for existing permission
        Optional<ExternalUserPermission> existing = permissionRepository
            .findByEmailAndEntityKindAndEntityName(email, entityKind, entityName);
        
        if (existing.isPresent()) {
            ExternalUserPermission permission = existing.get();
            if (permission.isActive()) {
                logger.warn("Permission already exists for user '{}' on entity '{}' of kind '{}'", 
                           email, entityName, entityKind);
                return permission;
            } else {
                // Reactivate existing permission
                permission.setActive(true);
                permission.setGrantedBy(grantedBy);
                permission.setAutoCc(autoCc);
                permission.setDescription(description);
                return permissionRepository.save(permission);
            }
        }
        
        // Create new permission
        ExternalUserPermission permission = new ExternalUserPermission(
            email, entityKind, entityName, grantedBy);
        permission.setAutoCc(autoCc);
        permission.setIsPrefix(isPrefix);
        permission.setDescription(description);
        
        ExternalUserPermission saved = permissionRepository.save(permission);
        logger.info("Successfully granted permission with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Grant prefix permission (matches entity names starting with the given prefix).
     */
    public ExternalUserPermission grantPrefixPermission(String email, PermissionEntityKind entityKind, 
                                                       String entityPrefix, String grantedBy) {
        return grantPermission(email, entityKind, entityPrefix, grantedBy, AutoCCType.NONE, true, 
                             "Prefix permission for: " + entityPrefix);
    }

    // Permission checking

    /**
     * Check if user has permission for a specific entity.
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(String email, PermissionEntityKind entityKind, String entityName) {
        return permissionRepository.hasPermissionForEntity(email, entityKind, entityName);
    }

    /**
     * Check if user should be auto-CC'd for an entity.
     */
    @Transactional(readOnly = true)
    public boolean shouldAutoCC(String email, PermissionEntityKind entityKind, String entityName) {
        return permissionRepository.shouldAutoCCForEntity(email, entityKind, entityName);
    }

    /**
     * Get all permissions for a user.
     */
    @Transactional(readOnly = true)
    public List<ExternalUserPermission> getUserPermissions(String email) {
        return permissionRepository.findActivePermissionsByEmail(email);
    }

    /**
     * Get permissions for an entity.
     */
    @Transactional(readOnly = true)
    public List<ExternalUserPermission> getEntityPermissions(PermissionEntityKind entityKind, String entityName) {
        return permissionRepository.findAllPermissionsForEntity(entityKind, entityName);
    }

    /**
     * Get users with permissions for an entity.
     */
    @Transactional(readOnly = true)
    public List<ExternalUserPermission> getUsersWithPermission(PermissionEntityKind entityKind, String entityName) {
        return permissionRepository.findPermissionsForEntity(null, entityKind, entityName);
    }

    // Permission modification

    /**
     * Update auto-CC preference for a user.
     */
    public void updateAutoCC(String email, AutoCCType autoCcType) {
        logger.info("Updating auto-CC preference for user '{}' to '{}'", email, autoCcType);
        
        int updated = permissionRepository.updateAutoCcForUser(email, autoCcType);
        logger.info("Updated auto-CC for {} permissions", updated);
    }

    /**
     * Update permission description.
     */
    public void updatePermissionDescription(Long permissionId, String description) {
        logger.info("Updating description for permission ID: {}", permissionId);
        
        int updated = permissionRepository.updateDescription(permissionId, description);
        if (updated > 0) {
            logger.info("Successfully updated permission description");
        } else {
            logger.warn("Permission not found or not updated: {}", permissionId);
        }
    }

    /**
     * Activate permission.
     */
    public void activatePermission(Long permissionId) {
        logger.info("Activating permission ID: {}", permissionId);
        
        int updated = permissionRepository.updateActiveStatus(permissionId, true);
        if (updated > 0) {
            logger.info("Successfully activated permission");
        } else {
            logger.warn("Permission not found or not updated: {}", permissionId);
        }
    }

    /**
     * Deactivate permission.
     */
    public void deactivatePermission(Long permissionId) {
        logger.info("Deactivating permission ID: {}", permissionId);
        
        int updated = permissionRepository.updateActiveStatus(permissionId, false);
        if (updated > 0) {
            logger.info("Successfully deactivated permission");
        } else {
            logger.warn("Permission not found or not updated: {}", permissionId);
        }
    }

    // Bulk operations

    /**
     * Revoke all permissions for a user.
     */
    public int revokeAllPermissions(String email) {
        logger.info("Revoking all permissions for user: {}", email);
        
        int revoked = permissionRepository.deactivateAllPermissionsForUser(email);
        logger.info("Revoked {} permissions for user: {}", revoked, email);
        return revoked;
    }

    /**
     * Delete inactive permissions.
     */
    public int deleteInactivePermissions() {
        logger.info("Deleting inactive permissions");
        
        int deleted = permissionRepository.deleteInactivePermissions();
        logger.info("Deleted {} inactive permissions", deleted);
        return deleted;
    }

    // Search and filtering

    /**
     * Search permissions by term.
     */
    @Transactional(readOnly = true)
    public List<ExternalUserPermission> searchPermissions(String searchTerm) {
        return permissionRepository.searchPermissions(searchTerm);
    }

    /**
     * Search active permissions by term.
     */
    @Transactional(readOnly = true)
    public List<ExternalUserPermission> searchActivePermissions(String searchTerm) {
        return permissionRepository.searchActivePermissions(searchTerm);
    }

    /**
     * Find permissions by entity kind.
     */
    @Transactional(readOnly = true)
    public List<ExternalUserPermission> getPermissionsByEntityKind(PermissionEntityKind entityKind) {
        return permissionRepository.findActivePermissionsByEntityKind(entityKind);
    }

    /**
     * Find permissions by auto-CC type.
     */
    @Transactional(readOnly = true)
    public List<ExternalUserPermission> getPermissionsByAutoCC(AutoCCType autoCcType) {
        return permissionRepository.findByAutoCcType(autoCcType);
    }

    /**
     * Get users with auto-CC enabled.
     */
    @Transactional(readOnly = true)
    public List<ExternalUserPermission> getUsersWithAutoCC() {
        return permissionRepository.findUsersWithAutoCC();
    }

    /**
     * Get users with security auto-CC.
     */
    @Transactional(readOnly = true)
    public List<ExternalUserPermission> getUsersWithSecurityAutoCC() {
        return permissionRepository.findUsersWithSecurityAutoCC();
    }

    // Entity-specific permissions

    /**
     * Get fuzzer permissions.
     */
    @Transactional(readOnly = true)
    public List<ExternalUserPermission> getFuzzerPermissions() {
        return permissionRepository.findFuzzerPermissions();
    }

    /**
     * Get job permissions.
     */
    @Transactional(readOnly = true)
    public List<ExternalUserPermission> getJobPermissions() {
        return permissionRepository.findJobPermissions();
    }

    /**
     * Get uploader permissions.
     */
    @Transactional(readOnly = true)
    public List<ExternalUserPermission> getUploaderPermissions() {
        return permissionRepository.findUploaderPermissions();
    }

    // Prefix permissions

    /**
     * Get prefix permissions.
     */
    @Transactional(readOnly = true)
    public List<ExternalUserPermission> getPrefixPermissions() {
        return permissionRepository.findActivePrefixPermissions();
    }

    /**
     * Get prefix permissions by entity kind.
     */
    @Transactional(readOnly = true)
    public List<ExternalUserPermission> getPrefixPermissionsByKind(PermissionEntityKind entityKind) {
        return permissionRepository.findActivePrefixPermissionsByKind(entityKind);
    }

    /**
     * Find prefix permissions matching an entity name.
     */
    @Transactional(readOnly = true)
    public List<ExternalUserPermission> findMatchingPrefixPermissions(String entityName) {
        return permissionRepository.findPrefixPermissionsMatching(entityName);
    }

    // Statistics and reporting

    /**
     * Get permission statistics.
     */
    @Transactional(readOnly = true)
    public PermissionStatistics getPermissionStatistics() {
        PermissionStatistics stats = new PermissionStatistics();
        stats.setActivePermissionCount(permissionRepository.countActivePermissions());
        stats.setActiveUserCount(permissionRepository.countActiveUsers());
        return stats;
    }

    /**
     * Get permission counts by entity kind.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getPermissionCountsByEntityKind() {
        return permissionRepository.countPermissionsByEntityKind();
    }

    /**
     * Get permission counts by auto-CC type.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getPermissionCountsByAutoCC() {
        return permissionRepository.countPermissionsByAutoCcType();
    }

    /**
     * Get permission counts by user.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getPermissionCountsByUser() {
        return permissionRepository.countPermissionsByUser();
    }

    /**
     * Get permissions granted by a specific user.
     */
    @Transactional(readOnly = true)
    public List<ExternalUserPermission> getPermissionsGrantedBy(String grantedBy) {
        return permissionRepository.findPermissionsGrantedBy(grantedBy);
    }

    /**
     * Get permission counts by granter.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getPermissionCountsByGranter() {
        return permissionRepository.countPermissionsByGranter();
    }

    // Validation and maintenance

    /**
     * Find duplicate permissions.
     */
    @Transactional(readOnly = true)
    public List<ExternalUserPermission> findDuplicatePermissions(String email, PermissionEntityKind entityKind, 
                                                                String entityName, Long excludeId) {
        return permissionRepository.findDuplicatePermissions(email, entityKind, entityName, excludeId);
    }

    /**
     * Find all duplicate active permissions.
     */
    @Transactional(readOnly = true)
    public List<ExternalUserPermission> findDuplicateActivePermissions() {
        return permissionRepository.findDuplicateActivePermissions();
    }

    /**
     * Find permissions without description.
     */
    @Transactional(readOnly = true)
    public List<ExternalUserPermission> findPermissionsWithoutDescription() {
        return permissionRepository.findPermissionsWithoutDescription();
    }

    /**
     * Get all active user emails.
     */
    @Transactional(readOnly = true)
    public List<String> getAllActiveUserEmails() {
        return permissionRepository.findAllActiveUserEmails();
    }

    // Helper methods

    /**
     * Get permission by ID.
     */
    @Transactional(readOnly = true)
    public Optional<ExternalUserPermission> getPermissionById(Long id) {
        return permissionRepository.findById(id);
    }

    /**
     * Get total permission count.
     */
    @Transactional(readOnly = true)
    public long getTotalPermissionCount() {
        return permissionRepository.count();
    }

    /**
     * Check if user exists in permission system.
     */
    @Transactional(readOnly = true)
    public boolean userHasAnyPermissions(String email) {
        List<ExternalUserPermission> permissions = getUserPermissions(email);
        return !permissions.isEmpty();
    }

    // Inner class for statistics
    public static class PermissionStatistics {
        private long activePermissionCount;
        private long activeUserCount;

        // Getters and setters
        public long getActivePermissionCount() { return activePermissionCount; }
        public void setActivePermissionCount(long activePermissionCount) { 
            this.activePermissionCount = activePermissionCount; 
        }

        public long getActiveUserCount() { return activeUserCount; }
        public void setActiveUserCount(long activeUserCount) { 
            this.activeUserCount = activeUserCount; 
        }

        public double getAveragePermissionsPerUser() {
            return activeUserCount > 0 ? (double) activePermissionCount / activeUserCount : 0.0;
        }
    }
}