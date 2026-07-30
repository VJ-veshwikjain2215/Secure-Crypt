package com.securecrypt.exception;

/**
 * Custom unchecked exception thrown by service classes whenever an
 * encryption/decryption operation cannot be completed - invalid key,
 * malformed cipher text, unsupported algorithm, etc.
 *
 * Using our own exception type (instead of letting raw JCA exceptions like
 * BadPaddingException bubble up) lets the GlobalExceptionHandler return a
 * clean, user-friendly JSON error instead of an ugly stack trace.
 */
public class CryptoException extends RuntimeException {

    public CryptoException(String message) {
        super(message);
    }

    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
