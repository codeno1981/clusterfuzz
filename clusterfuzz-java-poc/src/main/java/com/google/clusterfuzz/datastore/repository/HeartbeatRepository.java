package com.google.clusterfuzz.datastore.repository;

import com.google.clusterfuzz.datastore.model.Heartbeat;
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
 * Repository interface for Heartbeat entity operations.
 * Provides data access methods for bot health monitoring and management.
 */
@Repository
public interface HeartbeatRepository extends JpaRepository<Heartbeat, Long> {

    // Basic queries
    Optional<Heartbeat> findByBotName(String botName);
    
    List<Heartbeat> findByPlatformId(String platformId);
    
    List<Heartbeat> findBySourceVersion(String sourceVersion);
    
    boolean existsByBotName(String botName);

    // Bot status queries
    @Query("SELECT h FROM Heartbeat h WHERE h.isAlive = true ORDER BY h.lastBeatTime DESC")
    List<Heartbeat> findAliveBots();

    @Query("SELECT h FROM Heartbeat h WHERE h.isAlive = false ORDER BY h.lastBeatTime DESC")
    List<Heartbeat> findDeadBots();

    @Query("SELECT h FROM Heartbeat h WHERE " +
           "h.lastBeatTime >= :cutoffTime ORDER BY h.lastBeatTime DESC")
    List<Heartbeat> findRecentlyActive(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT h FROM Heartbeat h WHERE " +
           "h.lastBeatTime < :cutoffTime ORDER BY h.lastBeatTime ASC")
    List<Heartbeat> findStale(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Platform-specific queries
    @Query("SELECT h FROM Heartbeat h WHERE h.platformId = :platformId AND h.isAlive = true")
    List<Heartbeat> findAliveBotsByPlatform(@Param("platformId") String platformId);

    @Query("SELECT h FROM Heartbeat h WHERE " +
           "h.platformId = :platformId AND h.lastBeatTime >= :cutoffTime")
    List<Heartbeat> findRecentlyActiveBotsByPlatform(@Param("platformId") String platformId, 
                                                    @Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT DISTINCT h.platformId FROM Heartbeat h WHERE h.isAlive = true")
    List<String> findActivePlatforms();

    // Task-related queries
    @Query("SELECT h FROM Heartbeat h WHERE " +
           "h.taskPayload IS NOT NULL AND h.taskEndTime > CURRENT_TIMESTAMP")
    List<Heartbeat> findBotsWithActiveTasks();

    @Query("SELECT h FROM Heartbeat h WHERE " +
           "h.taskEndTime IS NOT NULL AND h.taskEndTime < CURRENT_TIMESTAMP")
    List<Heartbeat> findBotsWithOverdueTasks();

    @Query("SELECT h FROM Heartbeat h WHERE " +
           "h.taskPayload IS NULL OR h.taskPayload = ''")
    List<Heartbeat> findIdleBots();

    @Query("SELECT h FROM Heartbeat h WHERE h.jobName = :jobName")
    List<Heartbeat> findBotsByJobName(@Param("jobName") String jobName);

    @Query("SELECT h FROM Heartbeat h WHERE h.taskType = :taskType")
    List<Heartbeat> findBotsByTaskType(@Param("taskType") String taskType);

    // Resource usage queries
    @Query("SELECT h FROM Heartbeat h WHERE " +
           "h.cpuUsage > :threshold OR h.memoryUsage > :threshold")
    List<Heartbeat> findBotsWithHighResourceUsage(@Param("threshold") Double threshold);

    @Query("SELECT h FROM Heartbeat h WHERE h.cpuUsage > :cpuThreshold")
    List<Heartbeat> findBotsWithHighCpuUsage(@Param("cpuThreshold") Double cpuThreshold);

    @Query("SELECT h FROM Heartbeat h WHERE h.memoryUsage > :memoryThreshold")
    List<Heartbeat> findBotsWithHighMemoryUsage(@Param("memoryThreshold") Double memoryThreshold);

    @Query("SELECT h FROM Heartbeat h WHERE h.diskUsage > :diskThreshold")
    List<Heartbeat> findBotsWithHighDiskUsage(@Param("diskThreshold") Double diskThreshold);

    // Search queries
    @Query("SELECT h FROM Heartbeat h WHERE " +
           "LOWER(h.botName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Heartbeat> searchByBotName(@Param("searchTerm") String searchTerm);

    @Query("SELECT h FROM Heartbeat h WHERE " +
           "LOWER(h.taskPayload) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Heartbeat> searchByTaskPayload(@Param("searchTerm") String searchTerm);

    @Query("SELECT h FROM Heartbeat h JOIN h.keywords k WHERE " +
           "LOWER(k) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Heartbeat> findByKeyword(@Param("keyword") String keyword);

    // Statistics queries
    @Query("SELECT COUNT(h) FROM Heartbeat h WHERE h.isAlive = true")
    long countAliveBots();

    @Query("SELECT COUNT(h) FROM Heartbeat h WHERE h.isAlive = false")
    long countDeadBots();

    @Query("SELECT COUNT(h) FROM Heartbeat h WHERE " +
           "h.lastBeatTime >= :cutoffTime")
    long countRecentlyActive(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT h.platformId, COUNT(h) FROM Heartbeat h WHERE h.isAlive = true " +
           "GROUP BY h.platformId")
    List<Object[]> countBotsByPlatform();

    @Query("SELECT h.jobName, COUNT(h) FROM Heartbeat h WHERE " +
           "h.jobName IS NOT NULL GROUP BY h.jobName")
    List<Object[]> countBotsByJob();

    @Query("SELECT AVG(h.cpuUsage) FROM Heartbeat h WHERE " +
           "h.cpuUsage IS NOT NULL AND h.isAlive = true")
    Double getAverageCpuUsage();

    @Query("SELECT AVG(h.memoryUsage) FROM Heartbeat h WHERE " +
           "h.memoryUsage IS NOT NULL AND h.isAlive = true")
    Double getAverageMemoryUsage();

    // Time-based queries
    @Query("SELECT h FROM Heartbeat h WHERE " +
           "h.lastBeatTime BETWEEN :startTime AND :endTime " +
           "ORDER BY h.lastBeatTime DESC")
    List<Heartbeat> findByLastBeatTimeBetween(@Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);

    @Query("SELECT h FROM Heartbeat h WHERE " +
           "h.taskEndTime BETWEEN :startTime AND :endTime " +
           "ORDER BY h.taskEndTime ASC")
    List<Heartbeat> findByTaskEndTimeBetween(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    // Bulk operations
    @Modifying
    @Query("UPDATE Heartbeat h SET h.isAlive = false WHERE " +
           "h.lastBeatTime < :cutoffTime")
    int markStaleBotsAsDead(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Modifying
    @Query("UPDATE Heartbeat h SET h.lastBeatTime = CURRENT_TIMESTAMP, " +
           "h.isAlive = true WHERE h.botName = :botName")
    int updateHeartbeat(@Param("botName") String botName);

    @Modifying
    @Query("UPDATE Heartbeat h SET h.taskPayload = NULL, h.taskEndTime = NULL, " +
           "h.taskType = NULL WHERE h.taskEndTime < CURRENT_TIMESTAMP")
    int clearOverdueTasks();

    @Modifying
    @Query("DELETE FROM Heartbeat h WHERE " +
           "h.lastBeatTime < :cutoffTime AND h.isAlive = false")
    int deleteOldDeadBots(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Advanced monitoring queries
    @Query("SELECT h FROM Heartbeat h WHERE " +
           "h.isAlive = true AND h.lastBeatTime < :warningTime " +
           "ORDER BY h.lastBeatTime ASC")
    List<Heartbeat> findBotsNearingTimeout(@Param("warningTime") LocalDateTime warningTime);

    @Query("SELECT h FROM Heartbeat h WHERE " +
           "h.sourceVersion != :currentVersion AND h.isAlive = true")
    List<Heartbeat> findBotsWithOldVersion(@Param("currentVersion") String currentVersion);

    @Query("SELECT h FROM Heartbeat h WHERE " +
           "h.taskEndTime IS NOT NULL AND " +
           "h.taskEndTime BETWEEN CURRENT_TIMESTAMP AND :nearFutureTime")
    List<Heartbeat> findBotsWithTasksEndingSoon(@Param("nearFutureTime") LocalDateTime nearFutureTime);

    // Health check queries
    @Query("SELECT h FROM Heartbeat h WHERE " +
           "h.isAlive = true AND (" +
           "h.cpuUsage > 90.0 OR h.memoryUsage > 90.0 OR h.diskUsage > 95.0)")
    List<Heartbeat> findUnhealthyBots();

    @Query("SELECT h FROM Heartbeat h WHERE " +
           "h.isAlive = true AND h.cpuUsage IS NULL AND h.memoryUsage IS NULL")
    List<Heartbeat> findBotsWithoutResourceData();

    // Platform distribution queries
    @Query("SELECT h.platformId, COUNT(h), AVG(h.cpuUsage), AVG(h.memoryUsage) " +
           "FROM Heartbeat h WHERE h.isAlive = true " +
           "GROUP BY h.platformId ORDER BY COUNT(h) DESC")
    List<Object[]> getPlatformHealthStats();

    @Query("SELECT h.jobName, COUNT(h), " +
           "SUM(CASE WHEN h.isAlive = true THEN 1 ELSE 0 END) as aliveBots " +
           "FROM Heartbeat h WHERE h.jobName IS NOT NULL " +
           "GROUP BY h.jobName ORDER BY COUNT(h) DESC")
    List<Object[]> getJobHealthStats();

    // Cleanup and maintenance
    @Query("SELECT h FROM Heartbeat h WHERE " +
           "h.lastBeatTime < :oldTime ORDER BY h.lastBeatTime ASC")
    Page<Heartbeat> findOldHeartbeats(@Param("oldTime") LocalDateTime oldTime, Pageable pageable);

    @Modifying
    @Query("UPDATE Heartbeat h SET h.cpuUsage = NULL, h.memoryUsage = NULL, " +
           "h.diskUsage = NULL, h.networkUsage = NULL WHERE h.isAlive = false")
    int clearResourceDataForDeadBots();

    // Bot name pattern queries
    @Query("SELECT h FROM Heartbeat h WHERE h.botName LIKE :pattern")
    List<Heartbeat> findByBotNamePattern(@Param("pattern") String pattern);

    @Query("SELECT h FROM Heartbeat h WHERE " +
           "h.botName LIKE :pattern AND h.isAlive = true")
    List<Heartbeat> findAliveBotsByNamePattern(@Param("pattern") String pattern);
}