package com.google.clusterfuzz.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for handling environment variables and configuration.
 * Converted from Python environment utilities.
 */
public class Environment {

    private static final String DEFAULT_PROJECT_NAME = "clusterfuzz";

    /**
     * Parse environment definition string into a Map.
     * Format: "KEY1 = value1\nKEY2 = value2\n"
     */
    public static Map<String, String> parseEnvironmentDefinition(String environmentString) {
        Map<String, String> environment = new HashMap<>();
        
        if (environmentString == null || environmentString.trim().isEmpty()) {
            return environment;
        }
        
        String[] lines = environmentString.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue; // Skip empty lines and comments
            }
            
            int equalIndex = line.indexOf('=');
            if (equalIndex > 0) {
                String key = line.substring(0, equalIndex).trim();
                String value = line.substring(equalIndex + 1).trim();
                environment.put(key, value);
            }
        }
        
        return environment;
    }

    /**
     * Get the default project name.
     */
    public static String getDefaultProjectName() {
        return System.getProperty("clusterfuzz.default.project", DEFAULT_PROJECT_NAME);
    }

    /**
     * Get environment variable with default value.
     */
    public static String getEnvironmentVariable(String name, String defaultValue) {
        String value = System.getenv(name);
        return value != null ? value : defaultValue;
    }

    /**
     * Check if running in production environment.
     */
    public static boolean isProduction() {
        return "production".equals(System.getProperty("clusterfuzz.environment", "development"));
    }

    /**
     * Get the current platform.
     */
    public static String getCurrentPlatform() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return "WINDOWS";
        } else if (osName.contains("mac")) {
            return "MAC";
        } else if (osName.contains("linux")) {
            return "LINUX";
        } else {
            return "UNKNOWN";
        }
    }
}