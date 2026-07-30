package com.securecrypt.service;

import com.securecrypt.exception.CryptoException;
import com.securecrypt.utility.ValidationUtil;
import org.springframework.stereotype.Service;

/**
 * Classical Vigenere Cipher - uses a repeating keyword to shift each letter
 * by a different amount, making it far stronger against frequency analysis
 * than a single-shift Caesar cipher. Implemented manually as required.
 */
@Service
public class VigenereCipherService implements EncryptionService {

    @Override
    public String encrypt(String plainText, String key) {
        ValidationUtil.requireNonEmptyText(plainText);
        String cleanKey = sanitizeKey(key);
        return process(plainText, cleanKey, true);
    }

    @Override
    public String decrypt(String cipherText, String key) {
        ValidationUtil.requireNonEmptyText(cipherText);
        String cleanKey = sanitizeKey(key);
        return process(cipherText, cleanKey, false);
    }

    private String sanitizeKey(String key) {
        ValidationUtil.requireKey(key, "Vigenere Cipher");
        String cleanKey = key.replaceAll("[^a-zA-Z]", "");
        if (cleanKey.isEmpty()) {
            throw new CryptoException("Vigenere key must contain at least one alphabetic character.");
        }
        return cleanKey.toUpperCase();
    }

    private String process(String text, String key, boolean encrypting) {
        StringBuilder result = new StringBuilder(text.length());
        int keyIndex = 0;

        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                boolean isUpper = Character.isUpperCase(c);
                char base = isUpper ? 'A' : 'a';

                int textVal = Character.toUpperCase(c) - 'A';
                int keyVal = key.charAt(keyIndex % key.length()) - 'A';

                int shifted = encrypting
                        ? (textVal + keyVal) % 26
                        : (textVal - keyVal + 26) % 26;

                result.append((char) (base + shifted));
                keyIndex++; // only advance the keyword pointer on alphabetic characters
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
