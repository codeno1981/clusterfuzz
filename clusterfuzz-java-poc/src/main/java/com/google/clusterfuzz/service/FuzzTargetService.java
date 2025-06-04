package com.google.clusterfuzz.service;

import com.google.clusterfuzz.datastore.model.FuzzTarget;
import com.google.clusterfuzz.datastore.model.FuzzTargetJob;
import com.google.clusterfuzz.datastore.model.FuzzTargetsCount;
import com.google.clusterfuzz.datastore.repository.FuzzTargetRepository;
import com.google.clusterfuzz.datastore.repository.FuzzTargetJobRepository;
import com.google.clusterfuzz.datastore.repository.FuzzTargetsCountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing fuzz targets and their job associations.
 * Implements business logic for fuzz target operations.
 */
@Service
@Transactional
public class FuzzTargetService {

    private final FuzzTargetRepository fuzzTargetRepository;
    private final FuzzTargetJobRepository fuzzTargetJobRepository;
    private final FuzzTargetsCountRepository fuzzTargetsCountRepository;

    @Autowired
    public FuzzTargetService(FuzzTargetRepository fuzzTargetRepository,
                             FuzzTargetJobRepository fuzzTargetJobRepository,
                             FuzzTargetsCountRepository fuzzTargetsCountRepository) {
        this.fuzzTargetRepository = fuzzTargetRepository;
        this.fuzzTargetJobRepository = fuzzTargetJobRepository;
        this.fuzzTargetsCountRepository = fuzzTargetsCountRepository;
    }

    /**
     * Create a new fuzz target.
     */
    public FuzzTarget createFuzzTarget(String engine, String project, String binary) {
        // Validate inputs
        if (engine == null || engine.trim().isEmpty()) {
            throw new IllegalArgumentException("Engine is required");
        }
        if (project == null || project.trim().isEmpty()) {
            throw new IllegalArgumentException("Project is required");
        }
        if (binary == null || binary.trim().isEmpty()) {
            throw new IllegalArgumentException("Binary is required");
        }

        // Check for existing target
        String fullyQualifiedName = FuzzTarget.generateFullyQualifiedName(engine, project, binary);
        if (fuzzTargetRepository.existsByFullyQualifiedName(fullyQualifiedName)) {
            throw new IllegalArgumentException("Fuzz target already exists: " + fullyQualifiedName);
        }

        FuzzTarget fuzzTarget = new FuzzTarget(engine, project, binary);
        return fuzzTargetRepository.save(fuzzTarget);
    }

    /**
     * Find fuzz target by fully qualified name.
     */
    @Transactional(readOnly = true)
    public Optional<FuzzTarget> findByFullyQualifiedName(String fullyQualifiedName) {
        return fuzzTargetRepository.findByFullyQualifiedName(fullyQualifiedName);
    }

    /**
     * Find fuzz target by engine, project, and binary.
     */
    @Transactional(readOnly = true)
    public Optional<FuzzTarget> findByComponents(String engine, String project, String binary) {
        return fuzzTargetRepository.findByEngineAndProjectAndBinary(engine, project, binary);
    }

    /**
     * Get all fuzz targets for an engine.
     */
    @Transactional(readOnly = true)
    public List<FuzzTarget> findByEngine(String engine) {
        return fuzzTargetRepository.findByEngine(engine);
    }

    /**
     * Get all fuzz targets for a project.
     */
    @Transactional(readOnly = true)
    public List<FuzzTarget> findByProject(String project) {
        return fuzzTargetRepository.findByProject(project);
    }

    /**
     * Search fuzz targets with filters.
     */
    @Transactional(readOnly = true)
    public Page<FuzzTarget> searchFuzzTargets(String engine, String project, String binary, Pageable pageable) {
        return fuzzTargetRepository.searchFuzzTargets(engine, project, binary, pageable);
    }

    /**
     * Associate a fuzz target with a job.
     */
    public FuzzTargetJob associateWithJob(String fuzzTargetName, String job, String engine, Double weight) {
        // Validate fuzz target exists
        Optional<FuzzTarget> fuzzTargetOpt = fuzzTargetRepository.findByFullyQualifiedName(fuzzTargetName);
        if (fuzzTargetOpt.isEmpty()) {
            throw new IllegalArgumentException("Fuzz target not found: " + fuzzTargetName);
        }

        // Check if association already exists
        String compositeKey = FuzzTargetJob.generateCompositeKey(fuzzTargetName, job);
        if (fuzzTargetJobRepository.existsByCompositeKey(compositeKey)) {
            throw new IllegalArgumentException("Fuzz target job association already exists: " + compositeKey);
        }

        FuzzTargetJob fuzzTargetJob = new FuzzTargetJob(fuzzTargetName, job, engine, weight);
        FuzzTargetJob saved = fuzzTargetJobRepository.save(fuzzTargetJob);

        // Update count for the job
        updateFuzzTargetsCount(job);

        return saved;
    }

    /**
     * Remove association between fuzz target and job.
     */
    public void removeJobAssociation(String fuzzTargetName, String job) {
        Optional<FuzzTargetJob> associationOpt = fuzzTargetJobRepository.findByFuzzTargetNameAndJob(fuzzTargetName, job);
        if (associationOpt.isEmpty()) {
            throw new IllegalArgumentException("Fuzz target job association not found");
        }

        fuzzTargetJobRepository.delete(associationOpt.get());
        updateFuzzTargetsCount(job);
    }

    /**
     * Update execution statistics for a fuzz target job.
     */
    public void updateExecutionStats(String fuzzTargetName, String job, double executionTime, boolean success) {
        Optional<FuzzTargetJob> associationOpt = fuzzTargetJobRepository.findByFuzzTargetNameAndJob(fuzzTargetName, job);
        if (associationOpt.isEmpty()) {
            throw new IllegalArgumentException("Fuzz target job association not found");
        }

        FuzzTargetJob association = associationOpt.get();
        association.updateExecutionStats(executionTime, success);
        fuzzTargetJobRepository.save(association);
    }

    /**
     * Get eligible fuzz targets for a job based on weights and activity.
     */
    @Transactional(readOnly = true)
    public List<FuzzTargetJob> getEligibleTargetsForJob(String job) {
        return fuzzTargetJobRepository.findEligibleForJob(job);
    }

    /**
     * Select a fuzz target for execution using weighted random selection.
     */
    @Transactional(readOnly = true)
    public Optional<FuzzTargetJob> selectTargetForExecution(String job) {
        List<FuzzTargetJob> eligibleTargets = getEligibleTargetsForJob(job);
        
        if (eligibleTargets.isEmpty()) {
            return Optional.empty();
        }

        // Calculate total effective weight
        double totalWeight = eligibleTargets.stream()
                .mapToDouble(FuzzTargetJob::getEffectiveWeight)
                .sum();

        if (totalWeight <= 0) {
            // If no positive weights, select randomly
            int randomIndex = (int) (Math.random() * eligibleTargets.size());
            return Optional.of(eligibleTargets.get(randomIndex));
        }

        // Weighted random selection
        double random = Math.random() * totalWeight;
        double currentWeight = 0;

        for (FuzzTargetJob target : eligibleTargets) {
            currentWeight += target.getEffectiveWeight();
            if (random <= currentWeight) {
                return Optional.of(target);
            }
        }

        // Fallback to last target
        return Optional.of(eligibleTargets.get(eligibleTargets.size() - 1));
    }

    /**
     * Update the fuzz targets count for a job.
     */
    public void updateFuzzTargetsCount(String job) {
        long totalCount = fuzzTargetJobRepository.countByJob(job);
        long activeCount = fuzzTargetJobRepository.countByJobAndActiveTrue(job);
        
        Double averageWeight = fuzzTargetJobRepository.getAverageWeightForJob(job);

        Optional<FuzzTargetsCount> countOpt = fuzzTargetsCountRepository.findByJobName(job);
        FuzzTargetsCount count;
        
        if (countOpt.isPresent()) {
            count = countOpt.get();
            count.updateCounts((int) totalCount, (int) activeCount, (int) activeCount); // Assuming enabled = active
            count.updateAverageWeight(averageWeight);
        } else {
            count = new FuzzTargetsCount(job, (int) totalCount, (int) activeCount, (int) activeCount);
            count.setAverageWeight(averageWeight);
        }

        fuzzTargetsCountRepository.save(count);
    }

    /**
     * Get fuzz targets count for a job.
     */
    @Transactional(readOnly = true)
    public Optional<FuzzTargetsCount> getFuzzTargetsCount(String job) {
        return fuzzTargetsCountRepository.findByJobName(job);
    }

    /**
     * Get statistics for all fuzz targets.
     */
    @Transactional(readOnly = true)
    public FuzzTargetStatistics getStatistics() {
        long totalTargets = fuzzTargetRepository.count();
        long totalAssociations = fuzzTargetJobRepository.count();
        long activeAssociations = fuzzTargetJobRepository.countByActiveTrue();
        
        List<Object[]> engineStats = fuzzTargetRepository.getEngineStatistics();
        List<Object[]> projectStats = fuzzTargetRepository.getProjectStatistics();
        List<Object[]> jobStats = fuzzTargetJobRepository.getJobStatistics();
        
        return new FuzzTargetStatistics(totalTargets, totalAssociations, activeAssociations,
                                        engineStats, projectStats, jobStats);
    }

    /**
     * Find targets that haven't been executed recently.
     */
    @Transactional(readOnly = true)
    public List<FuzzTargetJob> findStaleTargets(int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        return fuzzTargetJobRepository.findStaleExecutions(cutoff);
    }

    /**
     * Find targets that have never been executed.
     */
    @Transactional(readOnly = true)
    public List<FuzzTargetJob> findNeverExecutedTargets() {
        return fuzzTargetJobRepository.findNeverExecuted();
    }

    /**
     * Activate or deactivate a fuzz target job association.
     */
    public void setTargetJobActive(String fuzzTargetName, String job, boolean active) {
        Optional<FuzzTargetJob> associationOpt = fuzzTargetJobRepository.findByFuzzTargetNameAndJob(fuzzTargetName, job);
        if (associationOpt.isEmpty()) {
            throw new IllegalArgumentException("Fuzz target job association not found");
        }

        FuzzTargetJob association = associationOpt.get();
        association.setActive(active);
        fuzzTargetJobRepository.save(association);

        // Update count
        updateFuzzTargetsCount(job);
    }

    /**
     * Update weight for a fuzz target job association.
     */
    public void updateTargetJobWeight(String fuzzTargetName, String job, Double weight) {
        Optional<FuzzTargetJob> associationOpt = fuzzTargetJobRepository.findByFuzzTargetNameAndJob(fuzzTargetName, job);
        if (associationOpt.isEmpty()) {
            throw new IllegalArgumentException("Fuzz target job association not found");
        }

        FuzzTargetJob association = associationOpt.get();
        association.setWeight(weight);
        fuzzTargetJobRepository.save(association);

        // Update average weight in count
        updateFuzzTargetsCount(job);
    }

    /**
     * Delete a fuzz target and all its associations.
     */
    public void deleteFuzzTarget(String fullyQualifiedName) {
        Optional<FuzzTarget> fuzzTargetOpt = fuzzTargetRepository.findByFullyQualifiedName(fullyQualifiedName);
        if (fuzzTargetOpt.isEmpty()) {
            throw new IllegalArgumentException("Fuzz target not found: " + fullyQualifiedName);
        }

        // Get all jobs associated with this target
        List<FuzzTargetJob> associations = fuzzTargetJobRepository.findByFuzzTargetName(fullyQualifiedName);
        List<String> affectedJobs = associations.stream()
                .map(FuzzTargetJob::getJob)
                .distinct()
                .collect(Collectors.toList());

        // Delete all associations
        fuzzTargetJobRepository.deleteAll(associations);

        // Delete the target
        fuzzTargetRepository.delete(fuzzTargetOpt.get());

        // Update counts for affected jobs
        affectedJobs.forEach(this::updateFuzzTargetsCount);
    }

    /**
     * Statistics data class.
     */
    public static class FuzzTargetStatistics {
        private final long totalTargets;
        private final long totalAssociations;
        private final long activeAssociations;
        private final List<Object[]> engineStatistics;
        private final List<Object[]> projectStatistics;
        private final List<Object[]> jobStatistics;

        public FuzzTargetStatistics(long totalTargets, long totalAssociations, long activeAssociations,
                                    List<Object[]> engineStatistics, List<Object[]> projectStatistics,
                                    List<Object[]> jobStatistics) {
            this.totalTargets = totalTargets;
            this.totalAssociations = totalAssociations;
            this.activeAssociations = activeAssociations;
            this.engineStatistics = engineStatistics;
            this.projectStatistics = projectStatistics;
            this.jobStatistics = jobStatistics;
        }

        // Getters
        public long getTotalTargets() { return totalTargets; }
        public long getTotalAssociations() { return totalAssociations; }
        public long getActiveAssociations() { return activeAssociations; }
        public List<Object[]> getEngineStatistics() { return engineStatistics; }
        public List<Object[]> getProjectStatistics() { return projectStatistics; }
        public List<Object[]> getJobStatistics() { return jobStatistics; }
    }
}