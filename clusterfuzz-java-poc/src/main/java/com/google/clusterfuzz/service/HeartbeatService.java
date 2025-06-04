package com.google.clusterfuzz.service;

import com.google.clusterfuzz.datastore.model.Heartbeat;
import com.google.clusterfuzz.datastore.repository.HeartbeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Heartbeat entity operations.
 * Provides business logic for bot health monitoring and management.
 */
@Service
@Transactional
public class HeartbeatService {

    private static final Logger logger = LoggerFactory.getLogger(HeartbeatService.class);
    
    private static final int DEFAULT_TIMEOUT_MINUTES = 10;
    private static final int ANDROID_TIMEOUT_MINUTES = 1;
    private static final int WARNING_MINUTES = 5;

    @Autowired
    private HeartbeatRepository heartbeatRepository;

    // Basic heartbeat operations

    /**
     * Record a heartbeat for a bot.
     */
    public Heartbeat recordHeartbeat(String botName) {
        logger.debug("Recording heartbeat for bot: {}", botName);
        
        Optional<Heartbeat> existing = heartbeatRepository.findByBotName(botName);
        Heartbeat heartbeat;
        
        if (existing.isPresent()) {
            heartbeat = existing.get();
            heartbeat.beat();
        } else {
            heartbeat = new Heartbeat(botName);
        }
        
        return heartbeatRepository.save(heartbeat);
    }

    /**
     * Record a heartbeat with task information.
     */
    public Heartbeat recordHeartbeat(String botName, String taskPayload, LocalDateTime taskEndTime) {
        logger.debug("Recording heartbeat with task for bot: {}", botName);
        
        Optional<Heartbeat> existing = heartbeatRepository.findByBotName(botName);
        Heartbeat heartbeat;
        
        if (existing.isPresent()) {
            heartbeat = existing.get();
            heartbeat.beat(taskPayload, taskEndTime);
        } else {
            heartbeat = new Heartbeat(botName);
            heartbeat.setTaskPayload(taskPayload);
            heartbeat.setTaskEndTime(taskEndTime);
            heartbeat.populateIndices();
        }
        
        return heartbeatRepository.save(heartbeat);
    }

    /**
     * Record heartbeat with platform and task information.
     */
    public Heartbeat recordHeartbeat(String botName, String platformId, String taskPayload, 
                                   LocalDateTime taskEndTime, String sourceVersion) {
        logger.debug("Recording detailed heartbeat for bot: {}", botName);
        
        Optional<Heartbeat> existing = heartbeatRepository.findByBotName(botName);
        Heartbeat heartbeat;
        
        if (existing.isPresent()) {
            heartbeat = existing.get();
            heartbeat.beat(taskPayload, taskEndTime);
        } else {
            heartbeat = new Heartbeat(botName, platformId);
            heartbeat.setTaskPayload(taskPayload);
            heartbeat.setTaskEndTime(taskEndTime);
        }
        
        heartbeat.setPlatformId(platformId);
        heartbeat.setSourceVersion(sourceVersion);
        heartbeat.populateIndices();
        
        return heartbeatRepository.save(heartbeat);
    }

    /**
     * Get heartbeat by bot name.
     */
    @Transactional(readOnly = true)
    public Optional<Heartbeat> getHeartbeatByBotName(String botName) {
        return heartbeatRepository.findByBotName(botName);
    }

    /**
     * Get heartbeat by ID.
     */
    @Transactional(readOnly = true)
    public Optional<Heartbeat> getHeartbeatById(Long id) {
        return heartbeatRepository.findById(id);
    }

    // Bot status monitoring

    /**
     * Get all alive bots.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> getAliveBots() {
        return heartbeatRepository.findAliveBots();
    }

    /**
     * Get all dead bots.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> getDeadBots() {
        return heartbeatRepository.findDeadBots();
    }

    /**
     * Get recently active bots.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> getRecentlyActiveBots(int minutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutes);
        return heartbeatRepository.findRecentlyActive(cutoff);
    }

    /**
     * Get recently active bots with default timeout.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> getRecentlyActiveBots() {
        return getRecentlyActiveBots(DEFAULT_TIMEOUT_MINUTES);
    }

    /**
     * Get stale bots.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> getStaleBots(int minutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutes);
        return heartbeatRepository.findStale(cutoff);
    }

    /**
     * Get bots nearing timeout.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> getBotsNearingTimeout(int warningMinutes) {
        LocalDateTime warningTime = LocalDateTime.now().minusMinutes(DEFAULT_TIMEOUT_MINUTES - warningMinutes);
        return heartbeatRepository.findBotsNearingTimeout(warningTime);
    }

    /**
     * Get bots nearing timeout with default warning time.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> getBotsNearingTimeout() {
        return getBotsNearingTimeout(WARNING_MINUTES);
    }

    // Platform-specific operations

    /**
     * Get alive bots by platform.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> getAliveBotsByPlatform(String platformId) {
        return heartbeatRepository.findAliveBotsByPlatform(platformId);
    }

    /**
     * Get recently active bots by platform.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> getRecentlyActiveBotsByPlatform(String platformId, int minutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutes);
        return heartbeatRepository.findRecentlyActiveBotsByPlatform(platformId, cutoff);
    }

    /**
     * Get active platforms.
     */
    @Transactional(readOnly = true)
    public List<String> getActivePlatforms() {
        return heartbeatRepository.findActivePlatforms();
    }

    /**
     * Get bots by platform.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> getBotsByPlatform(String platformId) {
        return heartbeatRepository.findByPlatformId(platformId);
    }

    // Task management

    /**
     * Get bots with active tasks.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> getBotsWithActiveTasks() {
        return heartbeatRepository.findBotsWithActiveTasks();
    }

    /**
     * Get bots with overdue tasks.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> getBotsWithOverdueTasks() {
        return heartbeatRepository.findBotsWithOverdueTasks();
    }

    /**
     * Get idle bots.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> getIdleBots() {
        return heartbeatRepository.findIdleBots();
    }

    /**
     * Get bots by job name.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> getBotsByJobName(String jobName) {
        return heartbeatRepository.findBotsByJobName(jobName);
    }

    /**
     * Get bots by task type.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> getBotsByTaskType(String taskType) {
        return heartbeatRepository.findBotsByTaskType(taskType);
    }

    /**
     * Clear overdue tasks.
     */
    public int clearOverdueTasks() {
        logger.info("Clearing overdue tasks");
        int count = heartbeatRepository.clearOverdueTasks();
        logger.info("Cleared {} overdue tasks", count);
        return count;
    }

    // Resource monitoring

    /**
     * Get bots with high resource usage.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> getBotsWithHighResourceUsage(double threshold) {
        return heartbeatRepository.findBotsWithHighResourceUsage(threshold);
    }

    /**
     * Get bots with high CPU usage.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> getBotsWithHighCpuUsage(double threshold) {
        return heartbeatRepository.findBotsWithHighCpuUsage(threshold);
    }

    /**
     * Get bots with high memory usage.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> getBotsWithHighMemoryUsage(double threshold) {
        return heartbeatRepository.findBotsWithHighMemoryUsage(threshold);
    }

    /**
     * Get unhealthy bots.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> getUnhealthyBots() {
        return heartbeatRepository.findUnhealthyBots();
    }

    /**
     * Update resource usage for a bot.
     */
    public void updateResourceUsage(String botName, Double cpu, Double memory, Double disk, Double network) {
        logger.debug("Updating resource usage for bot: {}", botName);
        
        Optional<Heartbeat> heartbeat = heartbeatRepository.findByBotName(botName);
        if (heartbeat.isPresent()) {
            Heartbeat h = heartbeat.get();
            h.updateResourceUsage(cpu, memory, disk, network);
            heartbeatRepository.save(h);
        } else {
            logger.warn("Bot not found for resource update: {}", botName);
        }
    }

    // Search and filtering

    /**
     * Search bots by name.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> searchBotsByName(String searchTerm) {
        return heartbeatRepository.searchByBotName(searchTerm);
    }

    /**
     * Search bots by task payload.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> searchBotsByTaskPayload(String searchTerm) {
        return heartbeatRepository.searchByTaskPayload(searchTerm);
    }

    /**
     * Find bots by keyword.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> findBotsByKeyword(String keyword) {
        return heartbeatRepository.findByKeyword(keyword);
    }

    /**
     * Find bots by name pattern.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> findBotsByNamePattern(String pattern) {
        return heartbeatRepository.findAliveBotsByNamePattern(pattern);
    }

    // Statistics and monitoring

    /**
     * Get bot statistics.
     */
    @Transactional(readOnly = true)
    public BotStatistics getBotStatistics() {
        BotStatistics stats = new BotStatistics();
        stats.setAliveCount(heartbeatRepository.countAliveBots());
        stats.setDeadCount(heartbeatRepository.countDeadBots());
        stats.setRecentlyActiveCount(heartbeatRepository.countRecentlyActive(
            LocalDateTime.now().minusMinutes(DEFAULT_TIMEOUT_MINUTES)));
        stats.setAverageCpuUsage(heartbeatRepository.getAverageCpuUsage());
        stats.setAverageMemoryUsage(heartbeatRepository.getAverageMemoryUsage());
        return stats;
    }

    /**
     * Get bot counts by platform.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getBotCountsByPlatform() {
        return heartbeatRepository.countBotsByPlatform();
    }

    /**
     * Get bot counts by job.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getBotCountsByJob() {
        return heartbeatRepository.countBotsByJob();
    }

    /**
     * Get platform health statistics.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getPlatformHealthStats() {
        return heartbeatRepository.getPlatformHealthStats();
    }

    /**
     * Get job health statistics.
     */
    @Transactional(readOnly = true)
    public List<Object[]> getJobHealthStats() {
        return heartbeatRepository.getJobHealthStats();
    }

    // Maintenance operations

    /**
     * Mark stale bots as dead.
     */
    public int markStaleBotsAsDead(int timeoutMinutes) {
        logger.info("Marking stale bots as dead (timeout: {} minutes)", timeoutMinutes);
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(timeoutMinutes);
        int count = heartbeatRepository.markStaleBotsAsDead(cutoff);
        logger.info("Marked {} bots as dead", count);
        return count;
    }

    /**
     * Mark stale bots as dead with default timeout.
     */
    public int markStaleBotsAsDead() {
        return markStaleBotsAsDead(DEFAULT_TIMEOUT_MINUTES);
    }

    /**
     * Delete old dead bots.
     */
    public int deleteOldDeadBots(int days) {
        logger.info("Deleting dead bots older than {} days", days);
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        int count = heartbeatRepository.deleteOldDeadBots(cutoff);
        logger.info("Deleted {} old dead bots", count);
        return count;
    }

    /**
     * Clear resource data for dead bots.
     */
    public int clearResourceDataForDeadBots() {
        logger.info("Clearing resource data for dead bots");
        int count = heartbeatRepository.clearResourceDataForDeadBots();
        logger.info("Cleared resource data for {} dead bots", count);
        return count;
    }

    // Version management

    /**
     * Get bots with old version.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> getBotsWithOldVersion(String currentVersion) {
        return heartbeatRepository.findBotsWithOldVersion(currentVersion);
    }

    /**
     * Get bots by source version.
     */
    @Transactional(readOnly = true)
    public List<Heartbeat> getBotsBySourceVersion(String sourceVersion) {
        return heartbeatRepository.findBySourceVersion(sourceVersion);
    }

    // Bot lifecycle management

    /**
     * Mark bot as dead.
     */
    public void markBotAsDead(String botName) {
        logger.info("Marking bot as dead: {}", botName);
        
        Optional<Heartbeat> heartbeat = heartbeatRepository.findByBotName(botName);
        if (heartbeat.isPresent()) {
            Heartbeat h = heartbeat.get();
            h.markDead();
            heartbeatRepository.save(h);
        } else {
            logger.warn("Bot not found to mark as dead: {}", botName);
        }
    }

    /**
     * Clear task for bot.
     */
    public void clearBotTask(String botName) {
        logger.debug("Clearing task for bot: {}", botName);
        
        Optional<Heartbeat> heartbeat = heartbeatRepository.findByBotName(botName);
        if (heartbeat.isPresent()) {
            Heartbeat h = heartbeat.get();
            h.clearTask();
            heartbeatRepository.save(h);
        } else {
            logger.warn("Bot not found to clear task: {}", botName);
        }
    }

    /**
     * Check if bot exists.
     */
    @Transactional(readOnly = true)
    public boolean botExists(String botName) {
        return heartbeatRepository.existsByBotName(botName);
    }

    /**
     * Get total bot count.
     */
    @Transactional(readOnly = true)
    public long getTotalBotCount() {
        return heartbeatRepository.count();
    }

    // Inner class for statistics
    public static class BotStatistics {
        private long aliveCount;
        private long deadCount;
        private long recentlyActiveCount;
        private Double averageCpuUsage;
        private Double averageMemoryUsage;

        // Getters and setters
        public long getAliveCount() { return aliveCount; }
        public void setAliveCount(long aliveCount) { this.aliveCount = aliveCount; }

        public long getDeadCount() { return deadCount; }
        public void setDeadCount(long deadCount) { this.deadCount = deadCount; }

        public long getRecentlyActiveCount() { return recentlyActiveCount; }
        public void setRecentlyActiveCount(long recentlyActiveCount) { this.recentlyActiveCount = recentlyActiveCount; }

        public Double getAverageCpuUsage() { return averageCpuUsage; }
        public void setAverageCpuUsage(Double averageCpuUsage) { this.averageCpuUsage = averageCpuUsage; }

        public Double getAverageMemoryUsage() { return averageMemoryUsage; }
        public void setAverageMemoryUsage(Double averageMemoryUsage) { this.averageMemoryUsage = averageMemoryUsage; }

        public long getTotalCount() { return aliveCount + deadCount; }
        
        public double getAlivePercentage() {
            long total = getTotalCount();
            return total > 0 ? (double) aliveCount / total * 100.0 : 0.0;
        }
    }
}