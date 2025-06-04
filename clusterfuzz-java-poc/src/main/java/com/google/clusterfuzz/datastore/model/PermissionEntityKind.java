package com.google.clusterfuzz.datastore.model;

/**
 * Enum representing the type of entity for external user permissions.
 * Used by ExternalUserPermission to specify what type of entity the permission applies to.
 */
public enum PermissionEntityKind {
    FUZZER(0, "Fuzzer"),
    JOB(1, "Job"),
    UPLOADER(2, "Uploader");

    private final int value;
    private final String displayName;

    PermissionEntityKind(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public int getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get enum from integer value.
     */
    public static PermissionEntityKind fromValue(int value) {
        for (PermissionEntityKind kind : values()) {
            if (kind.value == value) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Unknown PermissionEntityKind value: " + value);
    }

    /**
     * Get enum from string name (case-insensitive).
     */
    public static PermissionEntityKind fromString(String name) {
        if (name == null) {
            return null;
        }
        
        for (PermissionEntityKind kind : values()) {
            if (kind.name().equalsIgnoreCase(name) || 
                kind.displayName.equalsIgnoreCase(name)) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Unknown PermissionEntityKind name: " + name);
    }

    @Override
    public String toString() {
        return displayName;
    }
}