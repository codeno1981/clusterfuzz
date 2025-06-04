package com.google.clusterfuzz.datastore.model;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;

/**
 * Represents a fuzzer entity in the datastore.
 * Java equivalent of the Python Fuzzer model from ClusterFuzz.
 */
public class Fuzzer extends BaseModel {
    
    // Additionally allows '.' and '@' over NAME_CHECK_REGEX.
    private static final Pattern VALID_NAME_REGEX = Pattern.compile("^[a-zA-Z0-9_@.-]+$");
    
    private LocalDateTime timestamp;
    
    @NotNull
    private String name;
    
    private String filename;
    private String blobstoreKey;
    private String fileSize;
    private String executablePath;
    private Integer revision;
    private String source;
    private Integer timeout;
    private String supportedPlatforms;
    private String launcherScript;
    
    // Default constructor
    public Fuzzer() {
        super();
    }
    
    // Constructor from Datastore Entity
    public Fuzzer(Entity entity) {
        super(entity);
        this.timestamp = entity.contains("timestamp") ? 
            entity.getTimestamp("timestamp").toSqlTimestamp().toLocalDateTime() : null;
        this.name = entity.getString("name");
        this.filename = entity.getString("filename");
        this.blobstoreKey = entity.getString("blobstore_key");
        this.fileSize = entity.getString("file_size");
        this.executablePath = entity.getString("executable_path");
        this.revision = entity.contains("revision") ? 
            Math.toIntExact(entity.getLong("revision")) : null;
        this.source = entity.getString("source");
        this.timeout = entity.contains("timeout") ? 
            Math.toIntExact(entity.getLong("timeout")) : null;
        this.supportedPlatforms = entity.getString("supported_platforms");
        this.launcherScript = entity.getString("launcher_script");
    }
    
    @Override
    public Entity toEntity(KeyFactory keyFactory) {
        Key key = getKey() != null ? getKey() : keyFactory.newKey();
        Entity.Builder builder = Entity.newBuilder(key);
        
        if (timestamp != null) {
            builder.set("timestamp", com.google.cloud.Timestamp.of(
                java.sql.Timestamp.valueOf(timestamp)));
        }
        if (name != null) builder.set("name", name);
        if (filename != null) builder.set("filename", filename);
        if (blobstoreKey != null) builder.set("blobstore_key", blobstoreKey);
        if (fileSize != null) builder.set("file_size", fileSize);
        if (executablePath != null) builder.set("executable_path", executablePath);
        if (revision != null) builder.set("revision", revision.longValue());
        if (source != null) builder.set("source", source);
        if (timeout != null) builder.set("timeout", timeout.longValue());
        if (supportedPlatforms != null) builder.set("supported_platforms", supportedPlatforms);
        if (launcherScript != null) builder.set("launcher_script", launcherScript);
        
        return builder.build();
    }
    
    @Override
    public String getKind() {
        return "Fuzzer";
    }
    
    // Validation method
    public boolean isValidName() {
        return name != null && VALID_NAME_REGEX.matcher(name).matches();
    }
    
    // Getters and Setters
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    
    public String getBlobstoreKey() { return blobstoreKey; }
    public void setBlobstoreKey(String blobstoreKey) { this.blobstoreKey = blobstoreKey; }
    
    public String getFileSize() { return fileSize; }
    public void setFileSize(String fileSize) { this.fileSize = fileSize; }
    
    public String getExecutablePath() { return executablePath; }
    public void setExecutablePath(String executablePath) { this.executablePath = executablePath; }
    
    public Integer getRevision() { return revision; }
    public void setRevision(Integer revision) { this.revision = revision; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public Integer getTimeout() { return timeout; }
    public void setTimeout(Integer timeout) { this.timeout = timeout; }
    
    public String getSupportedPlatforms() { return supportedPlatforms; }
    public void setSupportedPlatforms(String supportedPlatforms) { 
        this.supportedPlatforms = supportedPlatforms; 
    }
    
    public String getLauncherScript() { return launcherScript; }
    public void setLauncherScript(String launcherScript) { this.launcherScript = launcherScript; }
    
    @Override
    public String toString() {
        return String.format("Fuzzer{name='%s', revision=%d, source='%s'}", 
                           name, revision, source);
    }
}