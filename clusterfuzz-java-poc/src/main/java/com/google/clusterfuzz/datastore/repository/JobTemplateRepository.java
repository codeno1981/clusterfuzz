package com.google.clusterfuzz.datastore.repository;

import com.google.clusterfuzz.datastore.model.JobTemplate;
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
 * Repository interface for JobTemplate entity operations.
 * Provides data access methods for job template management.
 */
@Repository
public interface JobTemplateRepository extends JpaRepository<JobTemplate, Long> {

    // Basic queries
    Optional<JobTemplate> findByName(String name);
    
    List<JobTemplate> findByActiveTrue();
    
    List<JobTemplate> findByActiveFalse();
    
    boolean existsByName(String name);

    // Active template queries
    @Query("SELECT jt FROM JobTemplate jt WHERE jt.active = true ORDER BY jt.name")
    List<JobTemplate> findAllActiveOrderByName();

    @Query("SELECT jt FROM JobTemplate jt WHERE jt.active = true ORDER BY jt.usageCount DESC")
    List<JobTemplate> findAllActiveOrderByUsageDesc();

    @Query("SELECT jt FROM JobTemplate jt WHERE jt.active = true ORDER BY jt.usageCount DESC")
    Page<JobTemplate> findAllActiveOrderByUsageDesc(Pageable pageable);

    // Usage-based queries
    @Query("SELECT jt FROM JobTemplate jt WHERE jt.usageCount > :minUsage ORDER BY jt.usageCount DESC")
    List<JobTemplate> findByUsageCountGreaterThan(@Param("minUsage") Long minUsage);

    @Query("SELECT jt FROM JobTemplate jt WHERE jt.usageCount = 0 AND jt.active = true")
    List<JobTemplate> findUnusedActiveTemplates();

    @Query("SELECT jt FROM JobTemplate jt WHERE jt.usageCount > 10 ORDER BY jt.usageCount DESC")
    List<JobTemplate> findHeavilyUsedTemplates();

    // Search queries
    @Query("SELECT jt FROM JobTemplate jt WHERE " +
           "LOWER(jt.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(jt.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<JobTemplate> searchByNameOrDescription(@Param("searchTerm") String searchTerm);

    @Query("SELECT jt FROM JobTemplate jt WHERE " +
           "jt.active = true AND (" +
           "LOWER(jt.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(jt.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<JobTemplate> searchActiveByNameOrDescription(@Param("searchTerm") String searchTerm);

    @Query("SELECT jt FROM JobTemplate jt WHERE " +
           "LOWER(jt.environmentString) LIKE LOWER(CONCAT('%', :envVar, '%'))")
    List<JobTemplate> findByEnvironmentVariable(@Param("envVar") String envVar);

    // Statistics queries
    @Query("SELECT COUNT(jt) FROM JobTemplate jt WHERE jt.active = true")
    long countActiveTemplates();

    @Query("SELECT COUNT(jt) FROM JobTemplate jt WHERE jt.active = false")
    long countInactiveTemplates();

    @Query("SELECT SUM(jt.usageCount) FROM JobTemplate jt WHERE jt.active = true")
    Long getTotalUsageCount();

    @Query("SELECT AVG(jt.usageCount) FROM JobTemplate jt WHERE jt.active = true")
    Double getAverageUsageCount();

    @Query("SELECT MAX(jt.usageCount) FROM JobTemplate jt")
    Long getMaxUsageCount();

    // Template validation queries
    @Query("SELECT jt FROM JobTemplate jt WHERE " +
           "jt.environmentString IS NULL OR jt.environmentString = ''")
    List<JobTemplate> findTemplatesWithoutEnvironment();

    @Query("SELECT jt FROM JobTemplate jt WHERE " +
           "jt.description IS NULL OR jt.description = ''")
    List<JobTemplate> findTemplatesWithoutDescription();

    @Query("SELECT jt FROM JobTemplate jt WHERE " +
           "LENGTH(jt.environmentString) > :maxLength")
    List<JobTemplate> findTemplatesWithLongEnvironment(@Param("maxLength") int maxLength);

    // Bulk operations
    @Modifying
    @Query("UPDATE JobTemplate jt SET jt.usageCount = jt.usageCount + 1 WHERE jt.id = :id")
    int incrementUsageCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE JobTemplate jt SET jt.usageCount = jt.usageCount + 1 WHERE jt.name = :name")
    int incrementUsageCountByName(@Param("name") String name);

    @Modifying
    @Query("UPDATE JobTemplate jt SET jt.active = false WHERE jt.usageCount = 0")
    int deactivateUnusedTemplates();

    @Modifying
    @Query("UPDATE JobTemplate jt SET jt.active = :active WHERE jt.id = :id")
    int updateActiveStatus(@Param("id") Long id, @Param("active") boolean active);

    @Modifying
    @Query("UPDATE JobTemplate jt SET jt.description = :description WHERE jt.id = :id")
    int updateDescription(@Param("id") Long id, @Param("description") String description);

    // Advanced queries
    @Query("SELECT jt FROM JobTemplate jt WHERE " +
           "jt.active = true AND jt.usageCount BETWEEN :minUsage AND :maxUsage " +
           "ORDER BY jt.usageCount DESC")
    List<JobTemplate> findByUsageRange(@Param("minUsage") Long minUsage, 
                                      @Param("maxUsage") Long maxUsage);

    @Query("SELECT jt.name, jt.usageCount FROM JobTemplate jt WHERE jt.active = true " +
           "ORDER BY jt.usageCount DESC")
    List<Object[]> getTemplateUsageStats();

    @Query("SELECT jt FROM JobTemplate jt WHERE " +
           "jt.active = true AND jt.environmentString IS NOT NULL " +
           "ORDER BY LENGTH(jt.environmentString) DESC")
    List<JobTemplate> findActiveOrderByEnvironmentLength();

    // Name pattern queries
    @Query("SELECT jt FROM JobTemplate jt WHERE jt.name LIKE :pattern")
    List<JobTemplate> findByNamePattern(@Param("pattern") String pattern);

    @Query("SELECT jt FROM JobTemplate jt WHERE " +
           "jt.active = true AND jt.name LIKE :pattern")
    List<JobTemplate> findActiveByNamePattern(@Param("pattern") String pattern);

    // Recent activity queries
    @Query("SELECT jt FROM JobTemplate jt WHERE " +
           "jt.updatedAt >= CURRENT_TIMESTAMP - INTERVAL :days DAY " +
           "ORDER BY jt.updatedAt DESC")
    List<JobTemplate> findRecentlyUpdated(@Param("days") int days);

    @Query("SELECT jt FROM JobTemplate jt WHERE " +
           "jt.createdAt >= CURRENT_TIMESTAMP - INTERVAL :days DAY " +
           "ORDER BY jt.createdAt DESC")
    List<JobTemplate> findRecentlyCreated(@Param("days") int days);

    // Duplicate detection
    @Query("SELECT jt FROM JobTemplate jt WHERE " +
           "jt.environmentString = :envString AND jt.id != :excludeId")
    List<JobTemplate> findDuplicateEnvironments(@Param("envString") String envString, 
                                               @Param("excludeId") Long excludeId);

    @Query("SELECT jt1 FROM JobTemplate jt1, JobTemplate jt2 WHERE " +
           "jt1.id != jt2.id AND jt1.environmentString = jt2.environmentString")
    List<JobTemplate> findTemplatesWithDuplicateEnvironments();

    // Cleanup queries
    @Query("SELECT jt FROM JobTemplate jt WHERE " +
           "jt.active = false AND jt.updatedAt < CURRENT_TIMESTAMP - INTERVAL :days DAY")
    List<JobTemplate> findOldInactiveTemplates(@Param("days") int days);

    @Modifying
    @Query("DELETE FROM JobTemplate jt WHERE " +
           "jt.active = false AND jt.usageCount = 0 AND " +
           "jt.updatedAt < CURRENT_TIMESTAMP - INTERVAL :days DAY")
    int deleteOldUnusedTemplates(@Param("days") int days);
}