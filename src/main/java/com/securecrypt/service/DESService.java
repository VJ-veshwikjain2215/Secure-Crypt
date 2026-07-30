package com.securecrypt.service;

import com.securecrypt.exception.CryptoException;
import com.securecrypt.utility.KeyDerivationUtil;
import com.securecrypt.utility.ValidationUtil;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * DES (Data Encryption Standard) in CBC mode with PKCS5 padding, implemented
 * using JCA. DES is included for academic/comparison purposes only - its
 * 56-bit effective key length is considered cryptographically weak by modern
 * standards (see the "Limitations" section of the project report).
 */
@Service
public class DESService implements EncryptionService {

    private static final String TRANSFORMATION = "DES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 8; // DES block size = 8 bytes

    @Override
    public String encrypt(String plainText, String password) {
        ValidationUtil.requireNonEmptyText(plainText);
        SecretKeySpec key = KeyDerivationUtil.deriveDesKey(password);

        try {
            byte[] iv = generateIv();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

            byte[] cipherBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + cipherBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherBytes, 0, combined, iv.length, cipherBytes.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (GeneralSecurityException ex) {
            throw new CryptoException("DES encryption failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String decrypt(String cipherText, String password) {
        ValidationUtil.requireNonEmptyText(cipherText);
        SecretKeySpec key = KeyDerivationUtil.deriveDesKey(password);

        try {
            byte[] combined = Base64.getDecoder().decode(cipherText.trim());
            if (combined.length < IV_LENGTH) {
                throw new CryptoException("Cipher text is malformed or too short.");
            }

            byte[] iv = new byte[IV_LENGTH];
            byte[] encryptedBytes = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

            byte[] plainBytes = cipher.doFinal(encryptedBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new CryptoException("Invalid DES cipher text - expected Base64 encoded data.");
        } catch (BadPaddingException | IllegalBlockSizeException ex) {
            throw new CryptoException("Decryption failed - incorrect password or corrupted cipher text.");
        } catch (GeneralSecurityException ex) {
            throw new CryptoException("DES decryption failed: " + ex.getMessage(), ex);
        }
    }

    private byte[] generateIv() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}
