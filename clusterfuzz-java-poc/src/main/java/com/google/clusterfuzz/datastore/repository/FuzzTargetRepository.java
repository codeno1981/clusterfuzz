package com.google.clusterfuzz.datastore.repository;

import com.google.clusterfuzz.datastore.model.FuzzTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FuzzTarget entities.
 * Provides data access methods for fuzz target management.
 */
@Repository
public interface FuzzTargetRepository extends JpaRepository<FuzzTarget, Long> {

    // Basic queries
    Optional<FuzzTarget> findByFullyQualifiedName(String fullyQualifiedName);
    
    List<FuzzTarget> findByEngine(String engine);
    
    List<FuzzTarget> findByProject(String project);
    
    List<FuzzTarget> findByBinary(String binary);
    
    List<FuzzTarget> findByProjectQualifiedName(String projectQualifiedName);
    
    // Combined queries
    Optional<FuzzTarget> findByEngineAndProjectAndBinary(String engine, String project, String binary);
    
    List<FuzzTarget> findByEngineAndProject(String engine, String project);
    
    List<FuzzTarget> findByProjectAndBinary(String project, String binary);
    
    // Search queries
    @Query("SELECT f FROM FuzzTarget f WHERE " +
           "(:engine IS NULL OR f.engine = :engine) AND " +
           "(:project IS NULL OR f.project LIKE %:project%) AND " +
           "(:binary IS NULL OR f.binary LIKE %:binary%)")
    Page<FuzzTarget> searchFuzzTargets(@Param("engine") String engine,
                                       @Param("project") String project,
                                       @Param("binary") String binary,
                                       Pageable pageable);
    
    // Pattern matching queries
    @Query("SELECT f FROM FuzzTarget f WHERE f.binary LIKE %:pattern%")
    List<FuzzTarget> findByBinaryContaining(@Param("pattern") String pattern);
    
    @Query("SELECT f FROM FuzzTarget f WHERE f.project LIKE %:pattern%")
    List<FuzzTarget> findByProjectContaining(@Param("pattern") String pattern);
    
    @Query("SELECT f FROM FuzzTarget f WHERE f.fullyQualifiedName LIKE %:pattern%")
    List<FuzzTarget> findByFullyQualifiedNameContaining(@Param("pattern") String pattern);
    
    // Statistics queries
    @Query("SELECT f.engine, COUNT(f) FROM FuzzTarget f GROUP BY f.engine ORDER BY COUNT(f) DESC")
    List<Object[]> getEngineStatistics();
    
    @Query("SELECT f.project, COUNT(f) FROM FuzzTarget f GROUP BY f.project ORDER BY COUNT(f) DESC")
    List<Object[]> getProjectStatistics();
    
    @Query("SELECT COUNT(f) FROM FuzzTarget f WHERE f.engine = :engine")
    long countByEngine(@Param("engine") String engine);
    
    @Query("SELECT COUNT(f) FROM FuzzTarget f WHERE f.project = :project")
    long countByProject(@Param("project") String project);
    
    // Validation queries
    @Query("SELECT f FROM FuzzTarget f WHERE f.engine IS NULL OR f.engine = ''")
    List<FuzzTarget> findTargetsWithoutEngine();
    
    @Query("SELECT f FROM FuzzTarget f WHERE f.project IS NULL OR f.project = ''")
    List<FuzzTarget> findTargetsWithoutProject();
    
    @Query("SELECT f FROM FuzzTarget f WHERE f.binary IS NULL OR f.binary = ''")
    List<FuzzTarget> findTargetsWithoutBinary();
    
    // Duplicate detection
    @Query("SELECT f FROM FuzzTarget f WHERE f.fullyQualifiedName = :fqn AND f.id != :excludeId")
    List<FuzzTarget> findDuplicatesByFullyQualifiedName(@Param("fqn") String fullyQualifiedName,
                                                         @Param("excludeId") Long excludeId);
    
    // Default project queries
    @Query("SELECT f FROM FuzzTarget f WHERE f.project = 'clusterfuzz' OR f.project IS NULL OR f.project = ''")
    List<FuzzTarget> findDefaultProjectTargets();
    
    @Query("SELECT f FROM FuzzTarget f WHERE f.project != 'clusterfuzz' AND f.project IS NOT NULL AND f.project != ''")
    List<FuzzTarget> findNonDefaultProjectTargets();
    
    // Normalized name queries
    @Query("SELECT f FROM FuzzTarget f WHERE " +
           "REPLACE(REPLACE(REPLACE(f.binary, '/', '-'), ':', '-'), ' ', '-') LIKE %:normalizedPattern%")
    List<FuzzTarget> findByNormalizedBinaryPattern(@Param("normalizedPattern") String normalizedPattern);
    
    // Existence checks
    boolean existsByFullyQualifiedName(String fullyQualifiedName);
    
    boolean existsByEngineAndProjectAndBinary(String engine, String project, String binary);
    
    // Bulk operations support
    @Query("SELECT f.fullyQualifiedName FROM FuzzTarget f WHERE f.engine = :engine")
    List<String> findFullyQualifiedNamesByEngine(@Param("engine") String engine);
    
    @Query("SELECT f.fullyQualifiedName FROM FuzzTarget f WHERE f.project = :project")
    List<String> findFullyQualifiedNamesByProject(@Param("project") String project);
    
    // Advanced filtering
    @Query("SELECT f FROM FuzzTarget f WHERE f.engine IN :engines")
    List<FuzzTarget> findByEngineIn(@Param("engines") List<String> engines);
    
    @Query("SELECT f FROM FuzzTarget f WHERE f.project IN :projects")
    List<FuzzTarget> findByProjectIn(@Param("projects") List<String> projects);
    
    // Ordering queries
    List<FuzzTarget> findByEngineOrderByProjectAscBinaryAsc(String engine);
    
    List<FuzzTarget> findByProjectOrderByEngineAscBinaryAsc(String project);
    
    // Recent targets (based on timestamp from BaseModel)
    @Query("SELECT f FROM FuzzTarget f WHERE f.timestamp >= :since ORDER BY f.timestamp DESC")
    List<FuzzTarget> findRecentTargets(@Param("since") java.time.LocalDateTime since);
    
    // Custom utility queries
    @Query("SELECT DISTINCT f.engine FROM FuzzTarget f ORDER BY f.engine")
    List<String> findAllEngines();
    
    @Query("SELECT DISTINCT f.project FROM FuzzTarget f WHERE f.project IS NOT NULL AND f.project != '' ORDER BY f.project")
    List<String> findAllProjects();
    
    @Query("SELECT DISTINCT f.binary FROM FuzzTarget f ORDER BY f.binary")
    List<String> findAllBinaries();
}