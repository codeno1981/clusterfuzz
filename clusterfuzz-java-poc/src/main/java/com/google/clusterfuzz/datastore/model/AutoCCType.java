package com.google.clusterfuzz.datastore.model;

/**
 * Enum representing auto-CC types for external user permissions.
 * Determines when users should be automatically CC'd on bug reports.
 */
public enum AutoCCType {
    NONE(0, "None", "Don't auto-CC user"),
    ALL(1, "All", "Auto-CC user for all issues"),
    SECURITY(2, "Security", "Auto-CC only for security issues");

    private final int value;
    private final String displayName;
    private final String description;

    AutoCCType(int value, String displayName, String description) {
        this.value = value;
        this.displayName = displayName;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get enum from integer value.
     */
    public static AutoCCType fromValue(int value) {
        for (AutoCCType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown AutoCCType value: " + value);
    }

    /**
     * Get enum from string name (case-insensitive).
     */
    public static AutoCCType fromString(String name) {
        if (name == null) {
            return null;
        }
        
        for (AutoCCType type : values()) {
            if (type.name().equalsIgnoreCase(name) || 
                type.displayName.equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown AutoCCType name: " + name);
    }

    /**
     * Check if this type should CC for security issues.
     */
    public boolean shouldCCForSecurity() {
        return this == ALL || this == SECURITY;
    }

    /**
     * Check if this type should CC for non-security issues.
     */
    public boolean shouldCCForNonSecurity() {
        return this == ALL;
    }

    /**
     * Check if this type should CC for any issues.
     */
    public boolean shouldCC() {
        return this != NONE;
    }

    @Override
    public String toString() {
        return displayName;
    }
}