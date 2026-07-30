package com.securecrypt.exception;

import com.securecrypt.model.CryptoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralised exception handling for every REST controller in the app.
 * This keeps controller/service code clean - they just throw, and this
 * class is responsible for turning any failure into a proper JSON
 * CryptoResponse with an appropriate HTTP status code.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Thrown by our own service layer for crypto specific failures. */
    @ExceptionHandler(CryptoException.class)
    public ResponseEntity<CryptoResponse> handleCryptoException(CryptoException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CryptoResponse.failure(ex.getMessage()));
    }

    /** Thrown when @Valid fails on the incoming request body (blank fields etc). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CryptoResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse("Invalid input provided.");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CryptoResponse.failure(message));
    }

    /** Bad arguments, e.g. unsupported algorithm string. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CryptoResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CryptoResponse.failure(ex.getMessage()));
    }

    /** Catch-all safety net so the client never sees a raw 500 stack trace. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CryptoResponse> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CryptoResponse.failure("Something went wrong on the server: " + ex.getMessage()));
    }
}
