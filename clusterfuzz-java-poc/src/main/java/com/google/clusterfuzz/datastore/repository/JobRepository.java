package com.google.clusterfuzz.datastore.repository;

import com.google.clusterfuzz.datastore.model.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Job entities.
 * Provides data access methods for job management.
 */
@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    // Basic queries
    Optional<Job> findByName(String name);
    
    List<Job> findByPlatform(String platform);
    
    List<Job> findByProject(String project);
    
    // Template queries
    @Query("SELECT j FROM Job j JOIN j.templates t WHERE t = :templateName")
    List<Job> findByTemplate(@Param("templateName") String templateName);
    
    @Query("SELECT j FROM Job j WHERE SIZE(j.templates) > 0")
    List<Job> findJobsWithTemplates();
    
    @Query("SELECT j FROM Job j WHERE SIZE(j.templates) = 0")
    List<Job> findJobsWithoutTemplates();
    
    // External job queries
    @Query("SELECT j FROM Job j WHERE j.externalReproductionTopic IS NOT NULL AND j.externalReproductionTopic != ''")
    List<Job> findExternalJobs();
    
    @Query("SELECT j FROM Job j WHERE j.externalReproductionTopic IS NULL OR j.externalReproductionTopic = ''")
    List<Job> findInternalJobs();
    
    // Search queries
    @Query("SELECT j FROM Job j JOIN j.keywords k WHERE k IN :keywords")
    List<Job> findByKeywords(@Param("keywords") List<String> keywords);
    
    @Query("SELECT j FROM Job j WHERE " +
           "(:name IS NULL OR j.name LIKE %:name%) AND " +
           "(:platform IS NULL OR j.platform = :platform) AND " +
           "(:project IS NULL OR j.project = :project)")
    Page<Job> searchJobs(@Param("name") String name,
                         @Param("platform") String platform,
                         @Param("project") String project,
                         Pageable pageable);
    
    // Custom binary queries
    @Query("SELECT j FROM Job j WHERE j.customBinaryKey IS NOT NULL AND j.customBinaryKey != ''")
    List<Job> findJobsWithCustomBinary();
    
    @Query("SELECT j FROM Job j WHERE j.customBinaryRevision > :revision")
    List<Job> findJobsWithCustomBinaryRevisionGreaterThan(@Param("revision") Integer revision);
    
    // Statistics queries
    @Query("SELECT j.platform, COUNT(j) FROM Job j GROUP BY j.platform ORDER BY COUNT(j) DESC")
    List<Object[]> getPlatformStatistics();
    
    @Query("SELECT j.project, COUNT(j) FROM Job j GROUP BY j.project ORDER BY COUNT(j) DESC")
    List<Object[]> getProjectStatistics();
    
    @Query("SELECT COUNT(j) FROM Job j WHERE j.externalReproductionTopic IS NOT NULL")
    long countExternalJobs();
    
    @Query("SELECT COUNT(j) FROM Job j WHERE j.customBinaryKey IS NOT NULL")
    long countJobsWithCustomBinary();
    
    // Environment queries
    @Query("SELECT j FROM Job j WHERE j.environmentString LIKE %:environmentVar%")
    List<Job> findJobsWithEnvironmentVariable(@Param("environmentVar") String environmentVar);
    
    // Validation queries
    @Query("SELECT j FROM Job j WHERE j.name IS NULL OR j.name = ''")
    List<Job> findJobsWithInvalidNames();
    
    @Query("SELECT j FROM Job j WHERE j.platform IS NULL OR j.platform = ''")
    List<Job> findJobsWithoutPlatform();
    
    @Query("SELECT j FROM Job j WHERE j.project IS NULL OR j.project = ''")
    List<Job> findJobsWithoutProject();
    
    // Template statistics
    @Query("SELECT t, COUNT(j) FROM Job j JOIN j.templates t GROUP BY t ORDER BY COUNT(j) DESC")
    List<Object[]> getTemplateUsageStatistics();
    
    @Query("SELECT DISTINCT t FROM Job j JOIN j.templates t ORDER BY t")
    List<String> findAllTemplateNames();
    
    // Description search
    @Query("SELECT j FROM Job j WHERE j.description LIKE %:searchTerm%")
    List<Job> findJobsByDescriptionContaining(@Param("searchTerm") String searchTerm);
    
    // Complex environment queries
    @Query("SELECT j FROM Job j WHERE j.environmentString IS NOT NULL AND j.environmentString != ''")
    List<Job> findJobsWithEnvironmentString();
    
    @Query("SELECT j FROM Job j WHERE (j.environmentString IS NULL OR j.environmentString = '') AND SIZE(j.templates) = 0")
    List<Job> findJobsWithoutEnvironmentOrTemplates();
}