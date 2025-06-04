package com.google.clusterfuzz.datastore.model;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;

/**
 * Base class for all datastore models.
 * Equivalent to the Python Model class that extends ndb.Model.
 */
public abstract class BaseModel {
    
    private Key key;
    
    public BaseModel() {
        // Default constructor
    }
    
    public BaseModel(Entity entity) {
        this.key = entity.getKey();
    }
    
    /**
     * Convert this model to a Datastore Entity.
     */
    public abstract Entity toEntity(KeyFactory keyFactory);
    
    /**
     * Get the kind name for this entity type.
     */
    public abstract String getKind();
    
    // Key management
    public Key getKey() { return key; }
    public void setKey(Key key) { this.key = key; }
    
    public Long getId() {
        return key != null && key.hasId() ? key.getId() : null;
    }
    
    public String getName() {
        return key != null && key.hasName() ? key.getName() : null;
    }
}