package com.google.clusterfuzz.datastore.repository;

import com.google.clusterfuzz.datastore.model.AutoCCType;
import com.google.clusterfuzz.datastore.model.ExternalUserPermission;
import com.google.clusterfuzz.datastore.model.PermissionEntityKind;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ExternalUserPermission entity operations.
 * Provides data access methods for external user permission management.
 */
@Repository
public interface ExternalUserPermissionRepository extends JpaRepository<ExternalUserPermission, Long> {

    // Basic queries
    List<ExternalUserPermission> findByEmail(String email);
    
    List<ExternalUserPermission> findByEntityKind(PermissionEntityKind entityKind);
    
    List<ExternalUserPermission> findByEntityName(String entityName);
    
    Optional<ExternalUserPermission> findByEmailAndEntityKindAndEntityName(
        String email, PermissionEntityKind entityKind, String entityName);
    
    boolean existsByEmailAndEntityKindAndEntityName(
        String email, PermissionEntityKind entityKind, String entityName);

    // Active permission queries
    @Query("SELECT eup FROM ExternalUserPermission eup WHERE eup.active = true")
    List<ExternalUserPermission> findActivePermissions();

    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "eup.email = :email AND eup.active = true")
    List<ExternalUserPermission> findActivePermissionsByEmail(@Param("email") String email);

    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "eup.entityKind = :entityKind AND eup.active = true")
    List<ExternalUserPermission> findActivePermissionsByEntityKind(
        @Param("entityKind") PermissionEntityKind entityKind);

    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "eup.entityName = :entityName AND eup.active = true")
    List<ExternalUserPermission> findActivePermissionsByEntityName(@Param("entityName") String entityName);

    // Email-based queries
    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "LOWER(eup.email) = LOWER(:email)")
    List<ExternalUserPermission> findByEmailIgnoreCase(@Param("email") String email);

    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "LOWER(eup.email) = LOWER(:email) AND eup.active = true")
    List<ExternalUserPermission> findActiveByEmailIgnoreCase(@Param("email") String email);

    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "LOWER(eup.email) LIKE LOWER(CONCAT('%', :emailPattern, '%'))")
    List<ExternalUserPermission> findByEmailPattern(@Param("emailPattern") String emailPattern);

    @Query("SELECT DISTINCT eup.email FROM ExternalUserPermission eup WHERE eup.active = true")
    List<String> findAllActiveUserEmails();

    // Entity access queries
    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "eup.email = :email AND eup.entityKind = :entityKind AND eup.active = true AND " +
           "((eup.isPrefix = true AND :entityName LIKE CONCAT(eup.entityName, '%')) OR " +
           "(eup.isPrefix = false AND eup.entityName = :entityName))")
    List<ExternalUserPermission> findPermissionsForEntity(@Param("email") String email,
                                                          @Param("entityKind") PermissionEntityKind entityKind,
                                                          @Param("entityName") String entityName);

    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "eup.entityKind = :entityKind AND eup.active = true AND " +
           "((eup.isPrefix = true AND :entityName LIKE CONCAT(eup.entityName, '%')) OR " +
           "(eup.isPrefix = false AND eup.entityName = :entityName))")
    List<ExternalUserPermission> findAllPermissionsForEntity(@Param("entityKind") PermissionEntityKind entityKind,
                                                             @Param("entityName") String entityName);

    // Prefix-based queries
    @Query("SELECT eup FROM ExternalUserPermission eup WHERE eup.isPrefix = true")
    List<ExternalUserPermission> findPrefixPermissions();

    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "eup.isPrefix = true AND eup.active = true")
    List<ExternalUserPermission> findActivePrefixPermissions();

    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "eup.isPrefix = true AND eup.entityKind = :entityKind AND eup.active = true")
    List<ExternalUserPermission> findActivePrefixPermissionsByKind(
        @Param("entityKind") PermissionEntityKind entityKind);

    @Query("SELECT eup FROM ExternalUserPermission eup WHERE eup.isPrefix = false")
    List<ExternalUserPermission> findExactMatchPermissions();

    // Auto-CC queries
    @Query("SELECT eup FROM ExternalUserPermission eup WHERE eup.autoCc = :autoCcType")
    List<ExternalUserPermission> findByAutoCcType(@Param("autoCcType") AutoCCType autoCcType);

    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "eup.autoCc != :autoCcType AND eup.active = true")
    List<ExternalUserPermission> findByAutoCcTypeNot(@Param("autoCcType") AutoCCType autoCcType);

    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "eup.autoCc IN (:autoCcTypes) AND eup.active = true")
    List<ExternalUserPermission> findByAutoCcTypeIn(@Param("autoCcTypes") List<AutoCCType> autoCcTypes);

    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "eup.autoCc != 'NONE' AND eup.active = true")
    List<ExternalUserPermission> findUsersWithAutoCC();

    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "eup.autoCc = 'SECURITY' AND eup.active = true")
    List<ExternalUserPermission> findUsersWithSecurityAutoCC();

    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "eup.autoCc = 'ALL' AND eup.active = true")
    List<ExternalUserPermission> findUsersWithAllAutoCC();

    // Entity kind specific queries
    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "eup.entityKind = 'FUZZER' AND eup.active = true")
    List<ExternalUserPermission> findFuzzerPermissions();

    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "eup.entityKind = 'JOB' AND eup.active = true")
    List<ExternalUserPermission> findJobPermissions();

    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "eup.entityKind = 'UPLOADER' AND eup.active = true")
    List<ExternalUserPermission> findUploaderPermissions();

    // Statistics queries
    @Query("SELECT COUNT(eup) FROM ExternalUserPermission eup WHERE eup.active = true")
    long countActivePermissions();

    @Query("SELECT COUNT(DISTINCT eup.email) FROM ExternalUserPermission eup WHERE eup.active = true")
    long countActiveUsers();

    @Query("SELECT eup.entityKind, COUNT(eup) FROM ExternalUserPermission eup WHERE eup.active = true " +
           "GROUP BY eup.entityKind")
    List<Object[]> countPermissionsByEntityKind();

    @Query("SELECT eup.autoCc, COUNT(eup) FROM ExternalUserPermission eup WHERE eup.active = true " +
           "GROUP BY eup.autoCc")
    List<Object[]> countPermissionsByAutoCcType();

    @Query("SELECT eup.email, COUNT(eup) FROM ExternalUserPermission eup WHERE eup.active = true " +
           "GROUP BY eup.email ORDER BY COUNT(eup) DESC")
    List<Object[]> countPermissionsByUser();

    // Search queries
    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "LOWER(eup.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(eup.entityName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(eup.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<ExternalUserPermission> searchPermissions(@Param("searchTerm") String searchTerm);

    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "eup.active = true AND (" +
           "LOWER(eup.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(eup.entityName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<ExternalUserPermission> searchActivePermissions(@Param("searchTerm") String searchTerm);

    // Bulk operations
    @Modifying
    @Query("UPDATE ExternalUserPermission eup SET eup.active = false WHERE eup.email = :email")
    int deactivateAllPermissionsForUser(@Param("email") String email);

    @Modifying
    @Query("UPDATE ExternalUserPermission eup SET eup.active = :active WHERE eup.id = :id")
    int updateActiveStatus(@Param("id") Long id, @Param("active") boolean active);

    @Modifying
    @Query("UPDATE ExternalUserPermission eup SET eup.autoCc = :autoCcType WHERE eup.email = :email")
    int updateAutoCcForUser(@Param("email") String email, @Param("autoCcType") AutoCCType autoCcType);

    @Modifying
    @Query("UPDATE ExternalUserPermission eup SET eup.description = :description WHERE eup.id = :id")
    int updateDescription(@Param("id") Long id, @Param("description") String description);

    @Modifying
    @Query("DELETE FROM ExternalUserPermission eup WHERE eup.active = false")
    int deleteInactivePermissions();

    // Advanced permission checking
    @Query("SELECT CASE WHEN COUNT(eup) > 0 THEN true ELSE false END FROM ExternalUserPermission eup WHERE " +
           "eup.email = :email AND eup.entityKind = :entityKind AND eup.active = true AND " +
           "((eup.isPrefix = true AND :entityName LIKE CONCAT(eup.entityName, '%')) OR " +
           "(eup.isPrefix = false AND eup.entityName = :entityName))")
    boolean hasPermissionForEntity(@Param("email") String email,
                                  @Param("entityKind") PermissionEntityKind entityKind,
                                  @Param("entityName") String entityName);

    @Query("SELECT CASE WHEN COUNT(eup) > 0 THEN true ELSE false END FROM ExternalUserPermission eup WHERE " +
           "eup.email = :email AND eup.entityKind = :entityKind AND eup.active = true AND " +
           "eup.autoCc IN ('ALL', 'SECURITY') AND " +
           "((eup.isPrefix = true AND :entityName LIKE CONCAT(eup.entityName, '%')) OR " +
           "(eup.isPrefix = false AND eup.entityName = :entityName))")
    boolean shouldAutoCCForEntity(@Param("email") String email,
                                 @Param("entityKind") PermissionEntityKind entityKind,
                                 @Param("entityName") String entityName);

    // Conflict detection
    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "eup.email = :email AND eup.entityKind = :entityKind AND " +
           "eup.entityName = :entityName AND eup.id != :excludeId")
    List<ExternalUserPermission> findDuplicatePermissions(@Param("email") String email,
                                                          @Param("entityKind") PermissionEntityKind entityKind,
                                                          @Param("entityName") String entityName,
                                                          @Param("excludeId") Long excludeId);

    @Query("SELECT eup1 FROM ExternalUserPermission eup1, ExternalUserPermission eup2 WHERE " +
           "eup1.id != eup2.id AND eup1.email = eup2.email AND " +
           "eup1.entityKind = eup2.entityKind AND eup1.entityName = eup2.entityName AND " +
           "eup1.active = true AND eup2.active = true")
    List<ExternalUserPermission> findDuplicateActivePermissions();

    // Granted by queries
    @Query("SELECT eup FROM ExternalUserPermission eup WHERE eup.grantedBy = :grantedBy")
    List<ExternalUserPermission> findPermissionsGrantedBy(@Param("grantedBy") String grantedBy);

    @Query("SELECT eup.grantedBy, COUNT(eup) FROM ExternalUserPermission eup WHERE " +
           "eup.grantedBy IS NOT NULL AND eup.active = true " +
           "GROUP BY eup.grantedBy ORDER BY COUNT(eup) DESC")
    List<Object[]> countPermissionsByGranter();

    // Cleanup and maintenance
    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "eup.active = false AND eup.updatedAt < CURRENT_TIMESTAMP - INTERVAL :days DAY")
    List<ExternalUserPermission> findOldInactivePermissions(@Param("days") int days);

    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "eup.description IS NULL OR eup.description = ''")
    List<ExternalUserPermission> findPermissionsWithoutDescription();

    // Entity name pattern queries
    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "eup.entityName LIKE :pattern AND eup.active = true")
    List<ExternalUserPermission> findByEntityNamePattern(@Param("pattern") String pattern);

    @Query("SELECT eup FROM ExternalUserPermission eup WHERE " +
           "eup.isPrefix = true AND eup.active = true AND " +
           ":entityName LIKE CONCAT(eup.entityName, '%')")
    List<ExternalUserPermission> findPrefixPermissionsMatching(@Param("entityName") String entityName);
}