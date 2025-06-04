package com.google.clusterfuzz.service;

import com.google.clusterfuzz.datastore.model.Admin;
import com.google.clusterfuzz.datastore.repository.AdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Admin entity operations.
 * Provides business logic for admin user management and access control.
 */
@Service
@Transactional
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    
    private static final int INACTIVITY_WARNING_DAYS = 90;
    private static final int INACTIVITY_DEACTIVATION_DAYS = 180;

    @Autowired
    private AdminRepository adminRepository;

    // Admin creation and management

    /**
     * Create a new admin user.
     */
    public Admin createAdmin(String email, String role, String grantedBy) {
        return createAdmin(email, role, grantedBy, null, null);
    }

    /**
     * Create a new admin user with permissions and notes.
     */
    public Admin createAdmin(String email, String role, String grantedBy, String permissions, String notes) {
        logger.info("Creating admin user: {}", email);
        
        if (adminRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Admin with email '" + email + "' already exists");
        }
        
        Admin admin = new Admin(email, role, grantedBy);
        if (permissions != null) {
            admin.setPermissions(permissions);
        }
        if (notes != null) {
            admin.setNotes(notes);
        }
        
        Admin saved = adminRepository.save(admin);
        logger.info("Created admin user with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Get admin by email.
     */
    @Transactional(readOnly = true)
    public Optional<Admin> getAdminByEmail(String email) {
        return adminRepository.findByEmailIgnoreCase(email);
    }

    /**
     * Get admin by ID.
     */
    @Transactional(readOnly = true)
    public Optional<Admin> getAdminById(Long id) {
        return adminRepository.findById(id);
    }

    /**
     * Get all active admins.
     */
    @Transactional(readOnly = true)
    public List<Admin> getAllActiveAdmins() {
        return adminRepository.findAllActiveOrderByEmail();
    }

    /**
     * Get active admins ordered by last access.
     */
    @Transactional(readOnly = true)
    public List<Admin> getActiveAdminsByLastAccess() {
        return adminRepository.findAllActiveOrderByLastAccess();
    }

    // Admin authentication and access

    /**
     * Check if user is an active admin.
     */
    @Transactional(readOnly = true)
    public boolean isActiveAdmin(String email) {
        return adminRepository.isActiveAdmin(email);
    }

    /**
     * Check if user is a super admin.
     */
    @Transactional(readOnly = true)
    public boolean isSuperAdmin(String email) {
        return adminRepository.isSuperAdmin(email);
    }

    /**
     * Check if admin has specific permission.
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(String email, String permission) {
        return adminRepository.hasPermission(email, permission);
    }

    /**
     * Record admin access.
     */
    public void recordAccess(String email) {
        logger.debug("Recording access for admin: {}", email);
        
        int updated = adminRepository.recordAccess(email);
        if (updated == 0) {
            logger.warn("Admin not found for access recording: {}", email);
        }
    }

    /**
     * Authenticate admin and record access.
     */
    @Transactional(readOnly = true)
    public Optional<Admin> authenticateAndRecordAccess(String email) {
        Optional<Admin> admin = getActiveAdminByEmail(email);
        if (admin.isPresent()) {
            recordAccess(email);
            logger.info("Admin authenticated and access recorded: {}", email);
        } else {
            logger.warn("Authentication failed for email: {}", email);
        }
        return admin;
    }

    /**
     * Get active admin by email.
     */
    @Transactional(readOnly = true)
    public Optional<Admin> getActiveAdminByEmail(String email) {
        return adminRepository.findActiveByEmailIgnoreCase(email);
    }

    // Role management

    /**
     * Get admins by role.
     */
    @Transactional(readOnly = true)
    public List<Admin> getAdminsByRole(String role) {
        return adminRepository.findActiveByRoleIgnoreCase(role);
    }

    /**
     * Get super admins.
     */
    @Transactional(readOnly = true)
    public List<Admin> getSuperAdmins() {
        return adminRepository.findSuperAdmins();
    }

    /**
     * Get system admins.
     */
    @Transactional(readOnly = true)
    public List<Admin> getSystemAdmins() {
        return adminRepository.findSystemAdmins();
    }

    /**
     * Get all active roles.
     */
    @Transactional(readOnly = true)
    public List<String> getAllActiveRoles() {
        return adminRepository.findAllActiveRoles();
    }

    /**
     * Update admin role.
     */
    public void updateRole(String email, String newRole) {
        logger.info("Updating role for admin '{}' to '{}'", email, newRole);
        
        int updated = adminRepository.updateRole(email, newRole);
        if (updated > 0) {
            logger.info("Successfully updated role for admin: {}", email);
        } else {
            logger.warn("Admin not found for role update: {}", email);
        }
    }

    // Permission management

    /**
     * Update admin permissions.
     */
    public void updatePermissions(String email, String permissions) {
        logger.info("Updating permissions for admin: {}", email);
        
        int updated = adminRepository.updatePermissions(email, permissions);
        if (updated > 0) {
            logger.info("Successfully updated permissions for admin: {}", email);
        } else {
            logger.warn("Admin not found for permission update: {}", email);
        }
    }

    /**
     * Get admins by permission.
     */
    @Transactional(readOnly = true)
    public List<Admin> getAdminsByPermission(String permission) {
        return adminRepository.findActiveByPermission(permission);
    }

    /**
     * Get admins without permissions.
     */
    @Transactional(readOnly = true)
    public List<Admin> getAdminsWithoutPermissions() {
        return adminRepository.findAdminsWithoutPermissions();
    }

    // Admin activation/deactivation

    /**
     * Activate admin.
     */
    public void activateAdmin(String email) {
        logger.info("Activating admin: {}", email);
        
        int updated = adminRepository.activateAdmin(email);
        if (updated > 0) {
            logger.info("Successfully activated admin: {}", email);
        } else {
            logger.warn("Admin not found for activation: {}", email);
        }
    }

    /**
     * Deactivate admin.
     */
    public void deactivateAdmin(String email) {
        logger.info("Deactivating admin: {}", email);
        
        int updated = adminRepository.deactivateAdmin(email);
        if (updated > 0) {
            logger.info("Successfully deactivated admin: {}", email);
        } else {
            logger.warn("Admin not found for deactivation: {}", email);
        }
    }

    /**
     * Deactivate inactive admins.
     */
    public int deactivateInactiveAdmins() {
        logger.info("Deactivating admins inactive for more than {} days", INACTIVITY_DEACTIVATION_DAYS);
        
        LocalDateTime cutoff = LocalDateTime.now().minusDays(INACTIVITY_DEACTIVATION_DAYS);
        int deactivated = adminRepository.deactivateInactiveAdmins(cutoff);
        logger.info("Deactivated {} inactive admins", deactivated);
        return deactivated;
    }

    // Activity monitoring

    /**
     * Get recently active admins.
     */
    @Transactional(readOnly = true)
    public List<Admin> getRecentlyActiveAdmins(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        return adminRepository.findRecentlyActive(cutoff);
    }

    /**
     * Get inactive admins.
     */
    @Transactional(readOnly = true)
    public List<Admin> getInactiveAdmins(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        return adminRepository.findInactive(cutoff);
    }

    /**
     * Get admins nearing inactivity.
     */
    @Transactional(readOnly = true)
    public List<Admin> getAdminsNearingInactivity() {
        LocalDateTime warningTime = LocalDateTime.now().minusDays(INACTIVITY_WARNING_DAYS);
        return adminRepository.findAdminsNearingInactivity(warningTime);
    }

    /**
     * Get admins that never accessed the system.
     */
    @Transactional(readOnly = true)
    public List<Admin> getNeverAccessedAdmins() {
        return adminRepository.findNeverAccessed();
    }

    /**
     * Get frequent users.
     */
    @Transactional(readOnly = true)
    public List<Admin> getFrequentUsers(long minAccessCount) {
        return adminRepository.findFrequentUsers(minAccessCount);
    }

    /**
     * Get unused accounts.
     */
    @Transactional(readOnly = true)
    public List<Admin> getUnusedAccounts() {
        return adminRepository.findUnusedAccounts();
    }

    // Search and filtering

    /**
     * Search admins by term.
     */
    @Transactional(readOnly = true)
    public List<Admin> searchAdmins(String searchTerm) {
        return adminRepository.searchAdmins(searchTerm);
    }

    /**
     * Search active admins by term.
     */
    @Transactional(readOnly = true)
    public List<Admin> searchActiveAdmins(String searchTerm) {
        return adminRepository.searchActiveAdmins(searchTerm);
    }

    /**
     * Find admins by email pattern.
     */
    @Transactional(readOnly = true)
    public List<Admin> findAdminsByEmailPattern(String emailPattern) {
        return adminRepository.findByEmailPattern(emailPattern);
    }

    // Statistics and reporting

    /**
     * Get admin statistics.
     */
    @Transactional(readOnly = true)
    public AdminStatistics getAdminStatistics() {
        AdminStatistics stats = new AdminStatistics();
        stats.setActiveCount(adminRepository.countActiveAdmins());
        stats.setInactiveCount(adminRepository.countInactiveAdmins());
        stats.setRecentlyActiveCount(adminRepository.countRecentlyActive(
            LocalDateTime.now().minusDays(30)));
        stats.setTotalAccessCount(adminRepository.getTotalAccessCount());
        stats.setAverageAccessCount(adminRepository.getAverageAccessCount());
        stats.setMaxAccessCount(adminRepository.getMaxAccessCount());
        return stats;
    }

    /**
     * Get admin counts by role.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAdminCountsByRole() {
        return adminRepository.countAdminsByRole();
    }

    /**
     * Get admin counts by granter.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAdminCountsByGranter() {
        return adminRepository.countAdminsByGranter();
    }

    /**
     * Get admin activity statistics.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAdminActivityStats() {
        return adminRepository.getAdminActivityStats();
    }

    /**
     * Get daily active admin counts.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getDailyActiveAdminCounts(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return adminRepository.getDailyActiveAdminCounts(startDate);
    }

    /**
     * Get admin counts by email domain.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAdminCountsByEmailDomain() {
        return adminRepository.countAdminsByEmailDomain();
    }

    // Granter management

    /**
     * Get admins granted by a specific user.
     */
    @Transactional(readOnly = true)
    public List<Admin> getAdminsGrantedBy(String grantedBy) {
        return adminRepository.findAdminsGrantedBy(grantedBy);
    }

    /**
     * Get all granters.
     */
    @Transactional(readOnly = true)
    public List<String> getAllGranters() {
        return adminRepository.findAllGranters();
    }

    // Notes management

    /**
     * Update admin notes.
     */
    public void updateNotes(String email, String notes) {
        logger.info("Updating notes for admin: {}", email);
        
        int updated = adminRepository.updateNotes(email, notes);
        if (updated > 0) {
            logger.info("Successfully updated notes for admin: {}", email);
        } else {
            logger.warn("Admin not found for notes update: {}", email);
        }
    }

    /**
     * Get admins without notes.
     */
    @Transactional(readOnly = true)
    public List<Admin> getAdminsWithoutNotes() {
        return adminRepository.findAdminsWithoutNotes();
    }

    // Validation and maintenance

    /**
     * Find duplicate admins.
     */
    @Transactional(readOnly = true)
    public List<Admin> findDuplicateAdmins() {
        return adminRepository.findDuplicateAdmins();
    }

    /**
     * Find admins without role.
     */
    @Transactional(readOnly = true)
    public List<Admin> getAdminsWithoutRole() {
        return adminRepository.findAdminsWithoutRole();
    }

    /**
     * Find admins without granter.
     */
    @Transactional(readOnly = true)
    public List<Admin> getAdminsWithoutGranter() {
        return adminRepository.findAdminsWithoutGranter();
    }

    /**
     * Get unused old admins.
     */
    @Transactional(readOnly = true)
    public List<Admin> getUnusedOldAdmins(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        return adminRepository.findUnusedOldAdmins(cutoff);
    }

    /**
     * Delete old unused admins.
     */
    public int deleteOldUnusedAdmins(int days) {
        logger.info("Deleting unused admins older than {} days", days);
        
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        int deleted = adminRepository.deleteOldUnusedAdmins(cutoff);
        logger.info("Deleted {} old unused admins", deleted);
        return deleted;
    }

    // Helper methods

    /**
     * Update admin.
     */
    public Admin updateAdmin(Admin admin) {
        logger.info("Updating admin: {}", admin.getEmail());
        return adminRepository.save(admin);
    }

    /**
     * Delete admin.
     */
    public void deleteAdmin(Long id) {
        logger.info("Deleting admin with ID: {}", id);
        adminRepository.deleteById(id);
    }

    /**
     * Get total admin count.
     */
    @Transactional(readOnly = true)
    public long getTotalAdminCount() {
        return adminRepository.count();
    }

    /**
     * Get active admin count.
     */
    @Transactional(readOnly = true)
    public long getActiveAdminCount() {
        return adminRepository.countActiveAdmins();
    }

    // Inner class for statistics
    public static class AdminStatistics {
        private long activeCount;
        private long inactiveCount;
        private long recentlyActiveCount;
        private Long totalAccessCount;
        private Double averageAccessCount;
        private Long maxAccessCount;

        // Getters and setters
        public long getActiveCount() { return activeCount; }
        public void setActiveCount(long activeCount) { this.activeCount = activeCount; }

        public long getInactiveCount() { return inactiveCount; }
        public void setInactiveCount(long inactiveCount) { this.inactiveCount = inactiveCount; }

        public long getRecentlyActiveCount() { return recentlyActiveCount; }
        public void setRecentlyActiveCount(long recentlyActiveCount) { this.recentlyActiveCount = recentlyActiveCount; }

        public Long getTotalAccessCount() { return totalAccessCount; }
        public void setTotalAccessCount(Long totalAccessCount) { this.totalAccessCount = totalAccessCount; }

        public Double getAverageAccessCount() { return averageAccessCount; }
        public void setAverageAccessCount(Double averageAccessCount) { this.averageAccessCount = averageAccessCount; }

        public Long getMaxAccessCount() { return maxAccessCount; }
        public void setMaxAccessCount(Long maxAccessCount) { this.maxAccessCount = maxAccessCount; }

        public long getTotalCount() { return activeCount + inactiveCount; }
        
        public double getActivePercentage() {
            long total = getTotalCount();
            return total > 0 ? (double) activeCount / total * 100.0 : 0.0;
        }
        
        public double getRecentlyActivePercentage() {
            return activeCount > 0 ? (double) recentlyActiveCount / activeCount * 100.0 : 0.0;
        }
    }
}