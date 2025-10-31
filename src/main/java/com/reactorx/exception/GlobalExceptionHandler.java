package com.reactorx.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        // HTTP 404
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        // HTTP 400 (Used for business logic errors like "Cart is empty")
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // HTTP 400 (For DTO validation errors)
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // Handle authentication failures (e.g., bad credentials)
    @ExceptionHandler({ AuthenticationException.class })
    public ResponseEntity<?> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        // HTTP 401
        return new ResponseEntity<>("Authentication Failed: " + ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    // CRITICAL: Handle authorization failures (403 Forbidden)
    @ExceptionHandler({ AccessDeniedException.class })
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        // HTTP 403 (This catches the error thrown by OrderService when a user views another's order)
        return new ResponseEntity<>("Access Denied: " + ex.getMessage(), HttpStatus.FORBIDDEN);
    }


    // Generic Exception Handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
        System.err.println("Internal Server Error: " + ex.getMessage());
        ex.printStackTrace(); // Log full stack trace for debugging
        // HTTP 500
        return new ResponseEntity<>("An internal error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
