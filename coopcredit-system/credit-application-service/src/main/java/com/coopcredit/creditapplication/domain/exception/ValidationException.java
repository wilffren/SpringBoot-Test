package com.coopcredit.creditapplication.domain.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when validation fails.
 */
public class ValidationException extends RuntimeException {
    
    private final Map<String, String> errors;
    
    public ValidationException(String message) {
        super(message);
        this.errors = new HashMap<>();
    }
    
    public ValidationException(Map<String, String> errors) {
        super("Validation failed");
        this.errors = errors;
    }
    
    public ValidationException(String field, String message) {
        super("Validation failed for field: " + field);
        this.errors = new HashMap<>();
        this.errors.put(field, message);
    }
    
    public Map<String, String> getErrors() {
        return errors;
    }
}