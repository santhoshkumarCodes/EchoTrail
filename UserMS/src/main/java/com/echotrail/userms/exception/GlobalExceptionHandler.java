package com.echotrail.userms.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

/**
 * Global exception handler for the application.
 * Provides consistent error responses across all controllers.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFoundException(
            EntityNotFoundException ex, WebRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> handleUserAlreadyExistsException(
            UserAlreadyExistsException ex, WebRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(
            Exception ex, WebRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
