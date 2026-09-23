package com.example.orders.error;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex){

        int status = ex.getStatusCode().value();
        String message = ex.getReason();

        return ResponseEntity.status(status).body(new ErrorResponse(status, message, List.of()));

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex){
        List<String> errors = new ArrayList<>();

        for (FieldError fe: ex.getBindingResult().getFieldErrors()){
            errors.add(fe.getField() + ": " + fe.getDefaultMessage());
        }

        int status = HttpStatus.BAD_REQUEST.value();
        return ResponseEntity.status(status).body(new ErrorResponse(status, "Validation failed", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedBody(HttpMessageNotReadableException ex){
        int status = HttpStatus.BAD_REQUEST.value();
        return ResponseEntity.status(status).body(new ErrorResponse(status, "Malformed request body", List.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        int status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        return ResponseEntity.status(status).body(new ErrorResponse(status, "Internal server error", List.of()));

    }
}

