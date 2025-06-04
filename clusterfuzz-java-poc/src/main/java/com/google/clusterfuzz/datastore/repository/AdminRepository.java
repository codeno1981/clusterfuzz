package com.google.clusterfuzz.datastore.repository;

import com.google.clusterfuzz.datastore.model.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Admin entity operations.
 * Provides data access methods for admin user management.
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    // Basic queries
    Optional<Admin> findByEmail(String email);
    
    List<Admin> findByRole(String role);
    
    List<Admin> findByActiveTrue();
    
    List<Admin> findByActiveFalse();
    
    boolean existsByEmail(String email);

    // Email-based queries
    @Query("SELECT a FROM Admin a WHERE LOWER(a.email) = LOWER(:email)")
    Optional<Admin> findByEmailIgnoreCase(@Param("email") String email);

    @Query("SELECT a FROM Admin a WHERE " +
           "LOWER(a.email) LIKE LOWER(CONCAT('%', :emailPattern, '%'))")
    List<Admin> findByEmailPattern(@Param("emailPattern") String emailPattern);

    @Query("SELECT a FROM Admin a WHERE " +
           "a.active = true AND LOWER(a.email) = LOWER(:email)")
    Optional<Admin> findActiveByEmailIgnoreCase(@Param("email") String email);

    // Active admin queries
    @Query("SELECT a FROM Admin a WHERE a.active = true ORDER BY a.email")
    List<Admin> findAllActiveOrderByEmail();

    @Query("SELECT a FROM Admin a WHERE a.active = true ORDER BY a.lastAccessTime DESC")
    List<Admin> findAllActiveOrderByLastAccess();

    @Query("SELECT a FROM Admin a WHERE a.active = true ORDER BY a.accessCount DESC")
    List<Admin> findAllActiveOrderByAccessCount();

    // Role-based queries
    @Query("SELECT a FROM Admin a WHERE " +
           "LOWER(a.role) = LOWER(:role) AND a.active = true")
    List<Admin> findActiveByRoleIgnoreCase(@Param("role") String role);

    @Query("SELECT a FROM Admin a WHERE " +
           "LOWER(a.role) LIKE LOWER(CONCAT('%', :rolePattern, '%')) AND a.active = true")
    List<Admin> findActiveByRolePattern(@Param("rolePattern") String rolePattern);

    @Query("SELECT DISTINCT a.role FROM Admin a WHERE " +
           "a.role IS NOT NULL AND a.active = true")
    List<String> findAllActiveRoles();

    @Query("SELECT a FROM Admin a WHERE " +
           "a.role IN ('super', 'superadmin') AND a.active = true")
    List<Admin> findSuperAdmins();

    @Query("SELECT a FROM Admin a WHERE " +
           "a.role IN ('system', 'sysadmin') AND a.active = true")
    List<Admin> findSystemAdmins();

    // Access-based queries
    @Query("SELECT a FROM Admin a WHERE " +
           "a.lastAccessTime >= :cutoffTime ORDER BY a.lastAccessTime DESC")
    List<Admin> findRecentlyActive(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT a FROM Admin a WHERE " +
           "a.lastAccessTime < :cutoffTime OR a.lastAccessTime IS NULL " +
           "ORDER BY a.lastAccessTime ASC")
    List<Admin> findInactive(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT a FROM Admin a WHERE a.lastAccessTime IS NULL")
    List<Admin> findNeverAccessed();

    @Query("SELECT a FROM Admin a WHERE " +
           "a.accessCount > :minCount ORDER BY a.accessCount DESC")
    List<Admin> findFrequentUsers(@Param("minCount") Long minCount);

    @Query("SELECT a FROM Admin a WHERE a.accessCount = 0 OR a.accessCount IS NULL")
    List<Admin> findUnusedAccounts();

    // Time-based queries
    @Query("SELECT a FROM Admin a WHERE " +
           "a.grantedTime BETWEEN :startTime AND :endTime " +
           "ORDER BY a.grantedTime DESC")
    List<Admin> findByGrantedTimeBetween(@Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    @Query("SELECT a FROM Admin a WHERE " +
           "a.lastAccessTime BETWEEN :startTime AND :endTime " +
           "ORDER BY a.lastAccessTime DESC")
    List<Admin> findByLastAccessTimeBetween(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    @Query("SELECT a FROM Admin a WHERE " +
           "a.grantedTime >= :cutoffTime ORDER BY a.grantedTime DESC")
    List<Admin> findNewAdmins(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT a FROM Admin a WHERE " +
           "a.grantedTime < :cutoffTime ORDER BY a.grantedTime ASC")
    List<Admin> findOldAdmins(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Statistics queries
    @Query("SELECT COUNT(a) FROM Admin a WHERE a.active = true")
    long countActiveAdmins();

    @Query("SELECT COUNT(a) FROM Admin a WHERE a.active = false")
    long countInactiveAdmins();

    @Query("SELECT COUNT(a) FROM Admin a WHERE " +
           "a.lastAccessTime >= :cutoffTime")
    long countRecentlyActive(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT a.role, COUNT(a) FROM Admin a WHERE " +
           "a.role IS NOT NULL AND a.active = true " +
           "GROUP BY a.role")
    List<Object[]> countAdminsByRole();

    @Query("SELECT SUM(a.accessCount) FROM Admin a WHERE a.active = true")
    Long getTotalAccessCount();

    @Query("SELECT AVG(a.accessCount) FROM Admin a WHERE " +
           "a.accessCount > 0 AND a.active = true")
    Double getAverageAccessCount();

    @Query("SELECT MAX(a.accessCount) FROM Admin a")
    Long getMaxAccessCount();

    // Granted by queries
    @Query("SELECT a FROM Admin a WHERE a.grantedBy = :grantedBy")
    List<Admin> findAdminsGrantedBy(@Param("grantedBy") String grantedBy);

    @Query("SELECT a.grantedBy, COUNT(a) FROM Admin a WHERE " +
           "a.grantedBy IS NOT NULL AND a.active = true " +
           "GROUP BY a.grantedBy ORDER BY COUNT(a) DESC")
    List<Object[]> countAdminsByGranter();

    @Query("SELECT DISTINCT a.grantedBy FROM Admin a WHERE " +
           "a.grantedBy IS NOT NULL")
    List<String> findAllGranters();

    // Permission-based queries
    @Query("SELECT a FROM Admin a WHERE " +
           "a.permissions IS NOT NULL AND " +
           "LOWER(a.permissions) LIKE LOWER(CONCAT('%', :permission, '%'))")
    List<Admin> findByPermission(@Param("permission") String permission);

    @Query("SELECT a FROM Admin a WHERE " +
           "a.active = true AND a.permissions IS NOT NULL AND " +
           "LOWER(a.permissions) LIKE LOWER(CONCAT('%', :permission, '%'))")
    List<Admin> findActiveByPermission(@Param("permission") String permission);

    @Query("SELECT a FROM Admin a WHERE " +
           "a.permissions IS NULL OR a.permissions = ''")
    List<Admin> findAdminsWithoutPermissions();

    // Search queries
    @Query("SELECT a FROM Admin a WHERE " +
           "LOWER(a.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.role) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Admin> searchAdmins(@Param("searchTerm") String searchTerm);

    @Query("SELECT a FROM Admin a WHERE " +
           "a.active = true AND (" +
           "LOWER(a.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.role) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Admin> searchActiveAdmins(@Param("searchTerm") String searchTerm);

    // Bulk operations
    @Modifying
    @Query("UPDATE Admin a SET a.lastAccessTime = CURRENT_TIMESTAMP, " +
           "a.accessCount = a.accessCount + 1 WHERE a.email = :email")
    int recordAccess(@Param("email") String email);

    @Modifying
    @Query("UPDATE Admin a SET a.active = false WHERE a.email = :email")
    int deactivateAdmin(@Param("email") String email);

    @Modifying
    @Query("UPDATE Admin a SET a.active = true WHERE a.email = :email")
    int activateAdmin(@Param("email") String email);

    @Modifying
    @Query("UPDATE Admin a SET a.role = :role WHERE a.email = :email")
    int updateRole(@Param("email") String email, @Param("role") String role);

    @Modifying
    @Query("UPDATE Admin a SET a.permissions = :permissions WHERE a.email = :email")
    int updatePermissions(@Param("email") String email, @Param("permissions") String permissions);

    @Modifying
    @Query("UPDATE Admin a SET a.notes = :notes WHERE a.email = :email")
    int updateNotes(@Param("email") String email, @Param("notes") String notes);

    // Advanced queries
    @Query("SELECT a FROM Admin a WHERE " +
           "a.active = true AND a.lastAccessTime IS NOT NULL AND " +
           "a.lastAccessTime < :warningTime ORDER BY a.lastAccessTime ASC")
    List<Admin> findAdminsNearingInactivity(@Param("warningTime") LocalDateTime warningTime);

    @Query("SELECT a FROM Admin a WHERE " +
           "a.active = true AND a.accessCount > :threshold " +
           "ORDER BY a.accessCount DESC")
    List<Admin> findHighActivityAdmins(@Param("threshold") Long threshold);

    @Query("SELECT a FROM Admin a WHERE " +
           "a.active = true AND (a.accessCount = 0 OR a.accessCount IS NULL) AND " +
           "a.grantedTime < :cutoffTime")
    List<Admin> findUnusedOldAdmins(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Validation queries
    @Query("SELECT a FROM Admin a WHERE " +
           "a.notes IS NULL OR a.notes = ''")
    List<Admin> findAdminsWithoutNotes();

    @Query("SELECT a FROM Admin a WHERE " +
           "a.role IS NULL OR a.role = ''")
    List<Admin> findAdminsWithoutRole();

    @Query("SELECT a FROM Admin a WHERE " +
           "a.grantedBy IS NULL OR a.grantedBy = ''")
    List<Admin> findAdminsWithoutGranter();

    // Duplicate detection
    @Query("SELECT a FROM Admin a WHERE " +
           "LOWER(a.email) = LOWER(:email) AND a.id != :excludeId")
    List<Admin> findDuplicateEmails(@Param("email") String email, @Param("excludeId") Long excludeId);

    @Query("SELECT a1 FROM Admin a1, Admin a2 WHERE " +
           "a1.id != a2.id AND LOWER(a1.email) = LOWER(a2.email)")
    List<Admin> findDuplicateAdmins();

    // Cleanup and maintenance
    @Query("SELECT a FROM Admin a WHERE " +
           "a.active = false AND a.updatedAt < :cutoffTime")
    Page<Admin> findOldInactiveAdmins(@Param("cutoffTime") LocalDateTime cutoffTime, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Admin a WHERE " +
           "a.active = false AND (a.accessCount = 0 OR a.accessCount IS NULL) AND " +
           "a.updatedAt < :cutoffTime")
    int deleteOldUnusedAdmins(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Modifying
    @Query("UPDATE Admin a SET a.active = false WHERE " +
           "a.lastAccessTime < :cutoffTime AND a.active = true")
    int deactivateInactiveAdmins(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Activity analysis
    @Query("SELECT a.email, a.accessCount, a.lastAccessTime FROM Admin a WHERE " +
           "a.active = true ORDER BY a.accessCount DESC")
    List<Object[]> getAdminActivityStats();

    @Query("SELECT DATE(a.lastAccessTime), COUNT(DISTINCT a.email) FROM Admin a WHERE " +
           "a.lastAccessTime >= :startDate GROUP BY DATE(a.lastAccessTime) " +
           "ORDER BY DATE(a.lastAccessTime)")
    List<Object[]> getDailyActiveAdminCounts(@Param("startDate") LocalDateTime startDate);

    // Email domain analysis
    @Query("SELECT SUBSTRING(a.email, LOCATE('@', a.email) + 1), COUNT(a) FROM Admin a WHERE " +
           "a.active = true GROUP BY SUBSTRING(a.email, LOCATE('@', a.email) + 1) " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> countAdminsByEmailDomain();

    // Check admin status
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Admin a WHERE " +
           "LOWER(a.email) = LOWER(:email) AND a.active = true")
    boolean isActiveAdmin(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Admin a WHERE " +
           "LOWER(a.email) = LOWER(:email) AND a.active = true AND " +
           "a.role IN ('super', 'superadmin')")
    boolean isSuperAdmin(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Admin a WHERE " +
           "LOWER(a.email) = LOWER(:email) AND a.active = true AND " +
           "a.permissions IS NOT NULL AND " +
           "LOWER(a.permissions) LIKE LOWER(CONCAT('%', :permission, '%'))")
    boolean hasPermission(@Param("email") String email, @Param("permission") String permission);
}