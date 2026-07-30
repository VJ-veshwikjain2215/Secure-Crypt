package com.securecrypt.utility;

import java.security.SecureRandom;

/**
 * Generates cryptographically strong random keys/passwords.
 * Used by the "Generate Random Key" button on the front-end (GET /api/generate-key).
 */
public final class KeyGeneratorUtil {

    private static final String CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final SecureRandom RANDOM = new SecureRandom();

    private KeyGeneratorUtil() {
    }

    /** Generates a random alphanumeric+symbol password of the given length (default 16). */
    public static String generateRandomKey(int length) {
        if (length <= 0) {
            length = 16;
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * Very simple heuristic "strength meter" used to give the user live feedback
     * in the UI. Not a substitute for a real password-strength library, but
     * perfectly adequate for a college project demo.
     */
    public static String estimateStrength(String key) {
        if (key == null || key.isBlank()) {
            return "NONE";
        }
        int score = 0;
        if (key.length() >= 8) score++;
        if (key.length() >= 12) score++;
        if (key.matches(".*[a-z].*")) score++;
        if (key.matches(".*[A-Z].*")) score++;
        if (key.matches(".*[0-9].*")) score++;
        if (key.matches(".*[^a-zA-Z0-9].*")) score++;

        if (score <= 2) return "WEAK";
        if (score <= 4) return "MEDIUM";
        return "STRONG";
    }
}
