package com.coopcredit.creditapplication.domain.exception;

/**
 * Exception thrown when authentication or authorization fails.
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException() {
        super("Authentication required");
    }
}