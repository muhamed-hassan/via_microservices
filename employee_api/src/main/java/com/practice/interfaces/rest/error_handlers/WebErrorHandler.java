package com.practice.interfaces.rest.error_handlers;

import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.practice.application.employee.EntityNotFoundException;
import com.practice.application.employee.NoResultException;

@RestControllerAdvice
public class WebErrorHandler {

    @ExceptionHandler
    public ResponseEntity<Error> handleUnsupportedOperationException(UnsupportedOperationException exception) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                                .body(new Error(exception.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Error> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new Error(exception.getMessage()));
    }

    @ExceptionHandler({ NoResultException.class, EntityNotFoundException.class })
    public ResponseEntity<Error> handleDataNotFoundException(Exception exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(new Error(exception.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Error> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        var message = exception.getBindingResult()
                                        .getAllErrors()
                                        .stream()
                                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                                        .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new Error(message));
    }

    @ExceptionHandler
    public ResponseEntity<Error> handleConstraintViolationException(ConstraintViolationException exception) {
        var message = exception.getConstraintViolations()
                                        .stream()
                                        .map(ConstraintViolation::getMessage)
                                        .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new Error(message));
    }

    @ExceptionHandler
    public ResponseEntity<Error> handleMissingServletRequestParameterException(MissingServletRequestParameterException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new Error(exception.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Error> handleGeneralException(Exception exception) {
        var message = exception.getMessage() == null ? "Unable to process this request." : exception.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new Error(message));
    }

}
