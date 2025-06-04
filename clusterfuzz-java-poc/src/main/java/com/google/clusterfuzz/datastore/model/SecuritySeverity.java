package com.google.clusterfuzz.datastore.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Enum for Security Severity levels.
 * Java equivalent of the Python SecuritySeverity class.
 */
public enum SecuritySeverity {
    CRITICAL(0, "Critical"),
    HIGH(1, "High", true),  // default
    MEDIUM(2, "Medium"),
    LOW(3, "Low"),
    MISSING(4, "Missing");
    
    private final int value;
    private final String name;
    private final boolean isDefault;
    
    SecuritySeverity(int value, String name) {
        this(value, name, false);
    }
    
    SecuritySeverity(int value, String name, boolean isDefault) {
        this.value = value;
        this.name = name;
        this.isDefault = isDefault;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getDisplayName() {
        return name;
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    /**
     * Check if a severity value is valid (excludes MISSING).
     */
    public static boolean isValid(int securitySeverity) {
        return securitySeverity == CRITICAL.value || 
               securitySeverity == HIGH.value || 
               securitySeverity == MEDIUM.value || 
               securitySeverity == LOW.value;
    }
    
    /**
     * Check if a severity enum is valid (excludes MISSING).
     */
    public static boolean isValid(SecuritySeverity severity) {
        return severity != null && severity != MISSING;
    }
    
    /**
     * Get severity by value.
     */
    public static SecuritySeverity fromValue(int value) {
        return Arrays.stream(values())
                .filter(s -> s.value == value)
                .findFirst()
                .orElse(MISSING);
    }
    
    /**
     * Return the list of severities for a dropdown menu.
     * Equivalent to the Python list() method.
     */
    public static List<Map<String, Object>> getDropdownList() {
        return Arrays.asList(
            Map.of("value", CRITICAL.value, "name", CRITICAL.name),
            Map.of("value", HIGH.value, "name", HIGH.name, "default", true),
            Map.of("value", MEDIUM.value, "name", MEDIUM.name),
            Map.of("value", LOW.value, "name", LOW.name),
            Map.of("value", MISSING.value, "name", MISSING.name)
        );
    }
}