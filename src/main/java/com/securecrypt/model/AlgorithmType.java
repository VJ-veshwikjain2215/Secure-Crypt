package com.securecrypt.model;

/**
 * Enumerates all encryption algorithms supported by SecureCrypt.
 * Keeping this centralised avoids "magic strings" scattered across the code base.
 */
public enum AlgorithmType {
    CAESAR,
    VIGENERE,
    XOR,
    AES,
    DES,
    TRIPLE_DES;

    /**
     * Safely converts a raw string (as received from the front-end JSON body)
     * into an AlgorithmType, throwing a friendly error on bad input instead of
     * letting an IllegalArgumentException leak a stack trace to the client.
     */
    public static AlgorithmType fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Algorithm must be selected.");
        }
        try {
            return AlgorithmType.valueOf(value.trim().toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported algorithm: " + value);
        }
    }

    /** True for algorithms that need a text-based key/password (not raw key bytes). */
    public boolean requiresPassword() {
        return this == AES || this == DES || this == TRIPLE_DES
                || this == VIGENERE || this == CAESAR || this == XOR;
    }
}
