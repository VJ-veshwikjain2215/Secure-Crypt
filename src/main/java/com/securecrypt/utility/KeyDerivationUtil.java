package com.securecrypt.utility;

import com.securecrypt.exception.CryptoException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

/**
 * Derives fixed-length cryptographic keys from a user supplied password.
 *
 * Real-world systems should use a random salt persisted alongside the cipher
 * text (PBKDF2 with per-message salt). For the purposes of this academic
 * project we use a fixed application-level salt so that encrypting the same
 * text with the same password is always reproducible for demonstration -
 * this trade-off is called out explicitly in the project report's
 * "Limitations" section.
 */
public final class KeyDerivationUtil {

    // Fixed salt - documented as a simplification for demo/teaching purposes.
    private static final byte[] SALT = "SecureCrypt$Salt".getBytes(StandardCharsets.UTF_8);
    private static final int ITERATIONS = 65536;

    private KeyDerivationUtil() {
    }

    /**
     * Derives an AES key of the requested bit length (128) from a password using PBKDF2.
     */
    public static SecretKeySpec deriveAesKey(String password, int keyBits) {
        byte[] keyBytes = pbkdf2(password, keyBits);
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Derives a 64-bit DES key from a password.
     * DES ignores 1 parity bit per byte, so JCA still expects an 8-byte (64-bit) key array.
     */
    public static SecretKeySpec deriveDesKey(String password) {
        byte[] keyBytes = pbkdf2(password, 64);
        return new SecretKeySpec(keyBytes, "DES");
    }

    /**
     * Derives a 192-bit (24 byte) Triple DES key from a password.
     */
    public static SecretKeySpec deriveTripleDesKey(String password) {
        byte[] keyBytes = pbkdf2(password, 192);
        return new SecretKeySpec(keyBytes, "DESede");
    }

    private static byte[] pbkdf2(String password, int keyBits) {
        if (password == null || password.isBlank()) {
            throw new CryptoException("A password/key is required for this algorithm.");
        }
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), SALT, ITERATIONS, keyBits);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] key = factory.generateSecret(spec).getEncoded();
            spec.clearPassword(); // wipe sensitive char[] as soon as we're done with it
            return key;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new CryptoException("Failed to derive key from password.", ex);
        }
    }

    /** Overwrites a char array with zeros - best-effort clearing of sensitive data. */
    public static void wipe(char[] sensitive) {
        if (sensitive != null) {
            Arrays.fill(sensitive, '\0');
        }
    }

    /** Overwrites a byte array with zeros - best-effort clearing of sensitive data. */
    public static void wipe(byte[] sensitive) {
        if (sensitive != null) {
            Arrays.fill(sensitive, (byte) 0);
        }
    }
}
