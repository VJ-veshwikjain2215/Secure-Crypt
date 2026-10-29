package com.securecrypt.utility;

import java.security.SecureRandom;

/**
 * Generates cryptographically strong random keys/passwords.
 * Available through the bounded GET /api/generate-key endpoint. The UI generates keys locally.
 */
public final class KeyGeneratorUtil {

    private static final String CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final SecureRandom RANDOM = new SecureRandom();

    private KeyGeneratorUtil() {
    }

    /** Generates a random alphanumeric+symbol password of the given length (default 16). */
    public static String generateRandomKey(int length) {
        if (length < 8 || length > 128) throw new IllegalArgumentException("Key length must be between 8 and 128.");
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

}
