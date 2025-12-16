package com.useractivity.exception;

import com.useractivity.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Validation error: ", ex);
        
        List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            Object rejectedValue = ((FieldError) error).getRejectedValue();
            
            fieldErrors.add(ErrorResponse.FieldError.builder()
                    .field(fieldName)
                    .message(errorMessage)
                    .rejectedValue(rejectedValue)
                    .build());
        });
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Input validation failed")
                .path(request.getDescription(false).replace("uri=", ""))
                .fieldErrors(fieldErrors)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        log.error("Runtime error: ", ex);
        
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String error = "Internal Server Error";
        
        if (ex.getMessage() != null && ex.getMessage().contains("not found")) {
            status = HttpStatus.NOT_FOUND;
            error = "Not Found";
        } else if (ex.getMessage() != null && ex.getMessage().contains("already deleted")) {
            status = HttpStatus.BAD_REQUEST;
            error = "Bad Request";
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        
        return ResponseEntity.status(status).body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error: ", ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}


