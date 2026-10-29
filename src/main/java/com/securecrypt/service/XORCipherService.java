package com.securecrypt.service;

import com.securecrypt.exception.CryptoException;
import com.securecrypt.utility.ValidationUtil;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * XOR Cipher - repeats the key over the plaintext bytes and XORs them
 * together. XOR is symmetric (encrypt == decrypt at the byte level), so the
 * same private helper is reused for both directions.
 *
 * Because XOR produces raw (often unprintable) bytes, the encrypted output
 * is Base64 encoded so it can be safely displayed/copied/downloaded as text.
 */
@Service
public class XORCipherService implements EncryptionService {

    @Override
    public String encrypt(String plainText, String key) {
        ValidationUtil.requireNonEmptyText(plainText);
        ValidationUtil.requireKey(key, "XOR Cipher");

        byte[] textBytes = plainText.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] cipherBytes = xor(textBytes, keyBytes);

        return Base64.getEncoder().encodeToString(cipherBytes);
    }

    @Override
    public String decrypt(String cipherText, String key) {
        ValidationUtil.requireCipherText(cipherText);
        ValidationUtil.requireKey(key, "XOR Cipher");

        try {
            byte[] cipherBytes = Base64.getDecoder().decode(cipherText.trim());
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            byte[] plainBytes = xor(cipherBytes, keyBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new CryptoException("Invalid XOR cipher text - expected Base64 encoded data.");
        }
    }

    private byte[] xor(byte[] data, byte[] key) {
        byte[] output = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            output[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return output;
    }
}
