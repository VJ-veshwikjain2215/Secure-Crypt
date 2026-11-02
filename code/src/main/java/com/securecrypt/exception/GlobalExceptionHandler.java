package com.securecrypt.exception;
import com.securecrypt.model.CryptoResponse;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private ResponseEntity<CryptoResponse> bad(String message) { return ResponseEntity.badRequest().body(CryptoResponse.failure(message)); }
    @ExceptionHandler(CryptoException.class) public ResponseEntity<CryptoResponse> crypto(CryptoException ex) { return bad(ex.getMessage()); }
    @ExceptionHandler(IllegalArgumentException.class) public ResponseEntity<CryptoResponse> argument(IllegalArgumentException ex) { return bad(ex.getMessage()); }
    @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<CryptoResponse> validation(MethodArgumentNotValidException ex) {
        return bad(ex.getBindingResult().getFieldErrors().stream().findFirst().map(e -> e.getDefaultMessage()).orElse("Invalid request."));
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<CryptoResponse> malformed(Exception ex) { return bad("Invalid request format or parameter type."); }
    @ExceptionHandler(Exception.class) public ResponseEntity<CryptoResponse> unexpected(Exception ex) {
        // Do not log request payloads, keys, or exception messages that may echo submitted content.
        LOG.error("Unhandled request failure: {}", ex.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CryptoResponse.failure("The operation could not be completed."));
    }
}
