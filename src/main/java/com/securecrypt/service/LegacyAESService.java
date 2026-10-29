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
import java.util.Base64;

/** Decrypt-only compatibility for the original unauthenticated AES-CBC format. */
@Service
public class LegacyAESService implements EncryptionService {

    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int KEY_BITS = 128;
    private static final int IV_LENGTH = 16;

    @Override
    public String encrypt(String plainText, String password) {
        throw new CryptoException("Legacy AES-CBC is decrypt-only. Use AES-GCM for new messages.");
    }

    @Override
    public String decrypt(String cipherText, String password) {
        ValidationUtil.requireCipherText(cipherText);
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
            throw new CryptoException("AES decryption failed.", ex);
        }
    }

}
