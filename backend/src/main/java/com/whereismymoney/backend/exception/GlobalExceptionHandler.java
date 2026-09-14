package com.whereismymoney.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(
            ResponseStatusException ex) {

        logger.warn(
                "Request failed: status={}, reason={}",
                ex.getStatusCode().value(),
                ex.getReason());

        return buildResponse(
                ex.getStatusCode().value(),
                ex.getReason() != null
                        ? ex.getReason()
                        : "Request failed");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex) {

        logger.warn(
                "Access denied: {}",
                ex.getMessage());

        return buildResponse(
                HttpStatus.FORBIDDEN.value(),
                "You do not have permission to access this resource");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        logger.warn(
                "Request validation failed");

        Map<String, String> errors = new LinkedHashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(
                        error.getField(),
                        error.getDefaultMessage()));

        Map<String, Object> body = new LinkedHashMap<>();

        body.put(
                "timestamp",
                LocalDateTime.now());

        body.put(
                "status",
                HttpStatus.BAD_REQUEST.value());

        body.put(
                "error",
                "Validation failed");

        body.put(
                "details",
                errors);

        return ResponseEntity
                .badRequest()
                .body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex) {

        logger.error(
                "Application error: {}",
                ex.getMessage(),
                ex);

        return buildResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage() != null
                        ? ex.getMessage()
                        : "Request could not be processed");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex) {

        logger.error(
                "Unexpected application error",
                ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred");
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            int status,
            String message) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put(
                "timestamp",
                LocalDateTime.now());

        body.put(
                "status",
                status);

        body.put(
                "error",
                message);

        return ResponseEntity
                .status(status)
                .body(body);
    }
}