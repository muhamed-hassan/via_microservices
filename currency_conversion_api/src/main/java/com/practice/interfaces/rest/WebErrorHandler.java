package com.practice.interfaces.rest;

import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.practice.application.ServiceNotAvailableException;

@RestControllerAdvice
public class WebErrorHandler {

    private static final String ERROR_KEY = "error";

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleServiceNotAvailable(ServiceNotAvailableException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(Map.of(ERROR_KEY, exception.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleMissingServletRequestParameterException(MissingServletRequestParameterException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of(ERROR_KEY, exception.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations()
                                    .stream()
                                    .map(ConstraintViolation::getMessage)
                                    .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of(ERROR_KEY, message));
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception exception) {
        String message = exception.getMessage() == null ? "Unable to process this request." : exception.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of(ERROR_KEY, message));
    }

}