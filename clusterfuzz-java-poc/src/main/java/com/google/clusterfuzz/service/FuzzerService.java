package com.google.clusterfuzz.service;

import com.google.clusterfuzz.datastore.model.Fuzzer;
import com.google.clusterfuzz.datastore.repository.FuzzerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for managing fuzzers.
 * Contains business logic for fuzzer operations.
 */
@Service
public class FuzzerService {

    private final FuzzerRepository fuzzerRepository;
    private final StorageService storageService;

    @Autowired
    public FuzzerService(FuzzerRepository fuzzerRepository, StorageService storageService) {
        this.fuzzerRepository = fuzzerRepository;
        this.storageService = storageService;
    }

    /**
     * Get all fuzzers ordered by name.
     */
    public List<Fuzzer> getAllFuzzers() {
        return fuzzerRepository.findAllOrderByName();
    }

    /**
     * Get a fuzzer by name.
     */
    public Fuzzer getFuzzerByName(String name) {
        return fuzzerRepository.findByName(name);
    }

    /**
     * Create a new fuzzer.
     */
    public Fuzzer createFuzzer(Fuzzer fuzzer) {
        fuzzer.setTimestamp(LocalDateTime.now());
        return fuzzerRepository.save(fuzzer);
    }

    /**
     * Update an existing fuzzer.
     */
    public Fuzzer updateFuzzer(Fuzzer fuzzer) {
        Fuzzer existing = fuzzerRepository.findByName(fuzzer.getName());
        if (existing == null) {
            return null;
        }
        
        // Update fields
        existing.setTimestamp(LocalDateTime.now());
        existing.setFilename(fuzzer.getFilename());
        existing.setExecutablePath(fuzzer.getExecutablePath());
        existing.setRevision(fuzzer.getRevision());
        existing.setSource(fuzzer.getSource());
        existing.setTimeout(fuzzer.getTimeout());
        existing.setSupportedPlatforms(fuzzer.getSupportedPlatforms());
        existing.setLauncherScript(fuzzer.getLauncherScript());
        
        return fuzzerRepository.save(existing);
    }

    /**
     * Delete a fuzzer by name.
     */
    public boolean deleteFuzzer(String name) {
        Fuzzer fuzzer = fuzzerRepository.findByName(name);
        if (fuzzer == null) {
            return false;
        }
        
        // Clean up associated blob storage
        if (fuzzer.getBlobstoreKey() != null) {
            storageService.deleteBlob(fuzzer.getBlobstoreKey());
        }
        
        fuzzerRepository.delete(fuzzer);
        return true;
    }

    /**
     * Upload a fuzzer archive and return the blob key.
     */
    public String uploadFuzzerArchive(String fuzzerName, MultipartFile file) throws Exception {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        // Check file size (16MB limit like in Python version)
        if (file.getSize() > 16 * 1024 * 1024) {
            throw new IllegalArgumentException("File too large. Maximum size is 16MB");
        }
        
        // Upload to cloud storage
        String blobKey = storageService.uploadFile(
            "fuzzer-archives", 
            fuzzerName + "/" + file.getOriginalFilename(),
            file.getInputStream(),
            file.getContentType()
        );
        
        // Update fuzzer record
        Fuzzer fuzzer = fuzzerRepository.findByName(fuzzerName);
        if (fuzzer != null) {
            fuzzer.setBlobstoreKey(blobKey);
            fuzzer.setFilename(file.getOriginalFilename());
            fuzzer.setFileSize(String.valueOf(file.getSize()));
            fuzzer.setTimestamp(LocalDateTime.now());
            fuzzerRepository.save(fuzzer);
        }
        
        return blobKey;
    }

    /**
     * Validate fuzzer configuration.
     */
    public boolean validateFuzzer(Fuzzer fuzzer) {
        if (fuzzer.getName() == null || !fuzzer.isValidName()) {
            return false;
        }
        
        // Add more validation logic as needed
        return true;
    }
}