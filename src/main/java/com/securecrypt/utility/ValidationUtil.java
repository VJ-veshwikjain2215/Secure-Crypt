package com.securecrypt.utility;

import com.securecrypt.exception.CryptoException;

/**
 * Reusable input-validation helpers shared across all cipher services.
 * Centralising these checks avoids duplicating the same null/blank/size
 * checks in six different service classes.
 */
public final class ValidationUtil {

    // Reasonable upper bound so a user can't crash the server with a
    // multi-megabyte paste. Adjust freely for your assignment.
    public static final int MAX_INPUT_LENGTH = 20_000;
    public static final int MAX_CIPHER_LENGTH = 120_000;

    private ValidationUtil() {
        // utility class - no instances
    }

    public static void requireNonEmptyText(String text) {
        if (text == null || text.isBlank()) {
            throw new CryptoException("Input text cannot be empty.");
        }
        if (text.length() > MAX_INPUT_LENGTH) {
            throw new CryptoException("Input text is too large (max " + MAX_INPUT_LENGTH + " characters).");
        }
    }

    public static void requireCipherText(String text) {
        if (text == null || text.isBlank() || text.length() > MAX_CIPHER_LENGTH)
            throw new CryptoException("Ciphertext must contain 1 to 120000 characters.");
    }

    public static void requireKey(String key, String algorithmName) {
        if (key != null && key.length() > 1024) throw new CryptoException("Key is too long (max 1024 characters).");
        if (key == null || key.isBlank()) {
            throw new CryptoException(algorithmName + " requires a key/password to be provided.");
        }
    }

    /** Caesar cipher shift must be a valid integer. */
    public static int parseShift(String key) {
        requireKey(key, "Caesar Cipher");
        try {
            int shift = Integer.parseInt(key.trim());
            return ((shift % 26) + 26) % 26; // normalise into 0-25
        } catch (NumberFormatException ex) {
            throw new CryptoException("Caesar Cipher key must be a whole number (e.g. 3).");
        }
    }
}
