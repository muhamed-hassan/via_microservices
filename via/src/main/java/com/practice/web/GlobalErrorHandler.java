package com.practice.web;

import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.practice.exceptions.DbConstraintViolationException;
import com.practice.exceptions.EntityModificationException;
import com.practice.exceptions.EntityNotFoundException;

@RestControllerAdvice
public class GlobalErrorHandler {

    private static final String ERROR_KEY = "error";

    private Logger logger = LoggerFactory.getLogger(GlobalErrorHandler.class);

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleUnsupportedOperationException(final UnsupportedOperationException exception) {
        logger.error(exception.toString());
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                                .body(Map.of(ERROR_KEY, exception.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(final IllegalArgumentException exception) {
        logger.error(exception.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of(ERROR_KEY, exception.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleEntityNotFoundException(final EntityNotFoundException exception) {
        logger.error(exception.toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of(ERROR_KEY, exception.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleEntityModificationException(final EntityModificationException exception) {
        logger.error(exception.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of(ERROR_KEY, exception.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(final ConstraintViolationException exception) {
        logger.error(exception.toString());
        String message = exception.getConstraintViolations()
                                    .stream()
                                    .map(ConstraintViolation::getMessage)
                                    .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of(ERROR_KEY, message));
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(final MethodArgumentNotValidException exception) {
        logger.error(exception.toString());
        String message = exception.getBindingResult()
                                    .getAllErrors()
                                    .stream()
                                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                                    .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of(ERROR_KEY, message));
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleDbConstraintViolationException(final DbConstraintViolationException exception) {
        logger.error(exception.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of(ERROR_KEY, exception.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleGeneralException(final Exception exception) {
        logger.error(exception.toString());
        String message = exception.getMessage() == null ? "Unable to process this request." : exception.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of(ERROR_KEY, message));
    }

}
