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
 * AES-128 (Advanced Encryption Standard) in CBC mode with PKCS5 padding,
 * implemented using the Java Cryptography Architecture (JCA) as required.
 *
 * The key is derived from the user's password via PBKDF2 (see
 * KeyDerivationUtil). A fresh random 16-byte IV is generated for every
 * encryption call and prepended to the cipher text so decryption can
 * recover it - this is standard practice and keeps identical plaintexts
 * from producing identical cipher texts (semantic security).
 */
@Service
public class AESService implements EncryptionService {

    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int KEY_BITS = 128;
    private static final int IV_LENGTH = 16;

    @Override
    public String encrypt(String plainText, String password) {
        ValidationUtil.requireNonEmptyText(plainText);
        SecretKeySpec key = KeyDerivationUtil.deriveAesKey(password, KEY_BITS);

        try {
            byte[] iv = generateIv();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

            byte[] cipherBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Prepend IV so it travels together with the cipher text
            byte[] combined = new byte[iv.length + cipherBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherBytes, 0, combined, iv.length, cipherBytes.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (GeneralSecurityException ex) {
            throw new CryptoException("AES encryption failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String decrypt(String cipherText, String password) {
        ValidationUtil.requireNonEmptyText(cipherText);
        SecretKeySpec key = KeyDerivationUtil.deriveAesKey(password, KEY_BITS);

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
            throw new CryptoException("Invalid AES cipher text - expected Base64 encoded data.");
        } catch (BadPaddingException | IllegalBlockSizeException ex) {
            throw new CryptoException("Decryption failed - incorrect password or corrupted cipher text.");
        } catch (GeneralSecurityException ex) {
            throw new CryptoException("AES decryption failed: " + ex.getMessage(), ex);
        }
    }

    private byte[] generateIv() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}
