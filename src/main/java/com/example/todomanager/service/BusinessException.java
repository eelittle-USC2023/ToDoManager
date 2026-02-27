package com.example.todomanager.service;

/**
 * Simple unchecked exception to denote business-logic validation/authorization errors.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}