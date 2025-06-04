package com.google.clusterfuzz.web.controller;

import com.google.clusterfuzz.datastore.model.Fuzzer;
import com.google.clusterfuzz.service.FuzzerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for managing fuzzers.
 * Java equivalent of the Python fuzzers.py handler.
 */
@RestController
@RequestMapping("/api/fuzzers")
public class FuzzerController {

    private final FuzzerService fuzzerService;

    @Autowired
    public FuzzerController(FuzzerService fuzzerService) {
        this.fuzzerService = fuzzerService;
    }

    /**
     * Get all fuzzers.
     */
    @GetMapping
    public ResponseEntity<List<Fuzzer>> getAllFuzzers() {
        List<Fuzzer> fuzzers = fuzzerService.getAllFuzzers();
        return ResponseEntity.ok(fuzzers);
    }

    /**
     * Get a specific fuzzer by name.
     */
    @GetMapping("/{name}")
    public ResponseEntity<Fuzzer> getFuzzer(@PathVariable String name) {
        Fuzzer fuzzer = fuzzerService.getFuzzerByName(name);
        if (fuzzer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(fuzzer);
    }

    /**
     * Create a new fuzzer.
     */
    @PostMapping
    public ResponseEntity<Fuzzer> createFuzzer(@RequestBody Fuzzer fuzzer) {
        if (!fuzzer.isValidName()) {
            return ResponseEntity.badRequest().build();
        }
        
        Fuzzer created = fuzzerService.createFuzzer(fuzzer);
        return ResponseEntity.ok(created);
    }

    /**
     * Upload a fuzzer archive.
     */
    @PostMapping("/{name}/upload")
    public ResponseEntity<String> uploadFuzzer(
            @PathVariable String name,
            @RequestParam("file") MultipartFile file) {
        
        try {
            String blobKey = fuzzerService.uploadFuzzerArchive(name, file);
            return ResponseEntity.ok(blobKey);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }

    /**
     * Delete a fuzzer.
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteFuzzer(@PathVariable String name) {
        boolean deleted = fuzzerService.deleteFuzzer(name);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Update fuzzer configuration.
     */
    @PutMapping("/{name}")
    public ResponseEntity<Fuzzer> updateFuzzer(
            @PathVariable String name,
            @RequestBody Fuzzer fuzzer) {
        
        fuzzer.setName(name);
        Fuzzer updated = fuzzerService.updateFuzzer(fuzzer);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }
}