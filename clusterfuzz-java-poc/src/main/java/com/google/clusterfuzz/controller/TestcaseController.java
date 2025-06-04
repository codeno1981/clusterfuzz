package com.google.clusterfuzz.controller;

import com.google.clusterfuzz.datastore.model.Testcase;
import com.google.clusterfuzz.datastore.model.SecuritySeverity;
import com.google.clusterfuzz.service.TestcaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for testcase management.
 * Provides HTTP endpoints for testcase operations.
 */
@RestController
@RequestMapping("/api/testcases")
@CrossOrigin(origins = "*")
public class TestcaseController {

    private final TestcaseService testcaseService;

    @Autowired
    public TestcaseController(TestcaseService testcaseService) {
        this.testcaseService = testcaseService;
    }

    /**
     * Create a new testcase.
     */
    @PostMapping
    public ResponseEntity<Testcase> createTestcase(@Valid @RequestBody Testcase testcase) {
        try {
            Testcase created = testcaseService.createTestcase(testcase);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get testcase by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Testcase> getTestcase(@PathVariable Long id) {
        Optional<Testcase> testcase = testcaseService.findById(id);
        return testcase.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update testcase.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Testcase> updateTestcase(@PathVariable Long id, 
                                                   @Valid @RequestBody Testcase testcase) {
        try {
            testcase.setId(id);
            Testcase updated = testcaseService.updateTestcase(testcase);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Search testcases with pagination and filtering.
     */
    @GetMapping
    public ResponseEntity<Page<Testcase>> searchTestcases(
            @RequestParam(required = false) String crashType,
            @RequestParam(required = false) String fuzzerName,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) Boolean securityFlag,
            @RequestParam(required = false) Boolean open,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                                   Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Testcase> testcases = testcaseService.searchTestcases(
            crashType, fuzzerName, projectName, securityFlag, open, pageable);

        return ResponseEntity.ok(testcases);
    }

    /**
     * Get open testcases.
     */
    @GetMapping("/open")
    public ResponseEntity<List<Testcase>> getOpenTestcases() {
        List<Testcase> testcases = testcaseService.findOpenTestcases();
        return ResponseEntity.ok(testcases);
    }

    /**
     * Get security testcases.
     */
    @GetMapping("/security")
    public ResponseEntity<List<Testcase>> getSecurityTestcases(
            @RequestParam(required = false) SecuritySeverity severity) {
        List<Testcase> testcases = testcaseService.findSecurityTestcases(severity);
        return ResponseEntity.ok(testcases);
    }

    /**
     * Get testcases by fuzzer.
     */
    @GetMapping("/fuzzer/{fuzzerName}")
    public ResponseEntity<List<Testcase>> getTestcasesByFuzzer(@PathVariable String fuzzerName) {
        List<Testcase> testcases = testcaseService.findByFuzzer(fuzzerName);
        return ResponseEntity.ok(testcases);
    }

    /**
     * Get recent testcases for a fuzzer.
     */
    @GetMapping("/fuzzer/{fuzzerName}/recent")
    public ResponseEntity<List<Testcase>> getRecentTestcases(
            @PathVariable String fuzzerName,
            @RequestParam(defaultValue = "24") int hours) {
        List<Testcase> testcases = testcaseService.findRecentTestcases(fuzzerName, hours);
        return ResponseEntity.ok(testcases);
    }

    /**
     * Mark testcase as duplicate.
     */
    @PostMapping("/{id}/duplicate")
    public ResponseEntity<Void> markAsDuplicate(@PathVariable Long id, 
                                                @RequestParam Long duplicateOf) {
        try {
            testcaseService.markAsDuplicate(id, duplicateOf);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Close testcase.
     */
    @PostMapping("/{id}/close")
    public ResponseEntity<Void> closeTestcase(@PathVariable Long id, 
                                              @RequestParam String reason) {
        try {
            testcaseService.closeTestcase(id, reason);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update security information.
     */
    @PostMapping("/{id}/security")
    public ResponseEntity<Void> updateSecurityInfo(@PathVariable Long id,
                                                    @RequestParam boolean isSecurityIssue,
                                                    @RequestParam(required = false) SecuritySeverity severity) {
        try {
            testcaseService.updateSecurityInfo(id, isSecurityIssue, severity);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Find potential duplicates.
     */
    @GetMapping("/{id}/duplicates")
    public ResponseEntity<List<Testcase>> findPotentialDuplicates(@PathVariable Long id) {
        try {
            List<Testcase> duplicates = testcaseService.findPotentialDuplicates(id);
            return ResponseEntity.ok(duplicates);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get testcase statistics.
     */
    @GetMapping("/statistics")
    public ResponseEntity<TestcaseService.TestcaseStatistics> getStatistics() {
        TestcaseService.TestcaseStatistics stats = testcaseService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get testcases needing triage.
     */
    @GetMapping("/triage")
    public ResponseEntity<List<Testcase>> getTestcasesNeedingTriage() {
        List<Testcase> testcases = testcaseService.findTestcasesNeedingTriage();
        return ResponseEntity.ok(testcases);
    }

    /**
     * Mark testcase as triaged.
     */
    @PostMapping("/{id}/triage")
    public ResponseEntity<Void> markAsTriaged(@PathVariable Long id) {
        try {
            testcaseService.markAsTriaged(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete testcase.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestcase(@PathVariable Long id) {
        try {
            testcaseService.deleteTestcase(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Exception handler for validation errors.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(IllegalArgumentException e) {
        ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Error response class.
     */
    public static class ErrorResponse {
        private String code;
        private String message;

        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}