package com.tcu.projectpulse.shared.api;

import com.tcu.projectpulse.shared.exception.ConflictException;
import com.tcu.projectpulse.shared.exception.InvalidArgumentException;
import com.tcu.projectpulse.shared.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.Comparator;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Result<Void>> handleNotFound(ResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.failure(StatusCode.NOT_FOUND, exception.getMessage()));
    }

    @ExceptionHandler({
            InvalidArgumentException.class
    })
    public ResponseEntity<Result<Void>> handleBadRequest(InvalidArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(Result.failure(StatusCode.INVALID_ARGUMENT, exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest()
                .body(Result.failure(StatusCode.INVALID_ARGUMENT, formatValidationErrors(exception)));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolation(ConstraintViolationException exception) {
        return ResponseEntity.badRequest()
                .body(Result.failure(StatusCode.INVALID_ARGUMENT, formatConstraintViolations(exception)));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Result<Void>> handleConflict(ConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Result.failure(StatusCode.CONFLICT, exception.getMessage()));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Result<Void>> handleLocked(DisabledException exception) {
        return ResponseEntity.status(HttpStatus.LOCKED)
                .body(Result.failure(StatusCode.LOCKED, exception.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result<Void>> handleUnauthorized(AuthenticationException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.failure(StatusCode.UNAUTHORIZED, exception.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleForbidden(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.failure(StatusCode.FORBIDDEN, "Access denied"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnexpected(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.failure(StatusCode.INTERNAL_SERVER_ERROR, exception.getMessage()));
    }

    private String formatValidationErrors(MethodArgumentNotValidException exception) {
        String messages = exception.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(error -> error.getField()))
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        if (!messages.isBlank()) {
            return messages;
        }

        messages = exception.getBindingResult().getGlobalErrors().stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        return messages.isBlank() ? "Request validation failed" : messages;
    }

    private String formatConstraintViolations(ConstraintViolationException exception) {
        String messages = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        return messages.isBlank() ? "Request validation failed" : messages;
    }
}
