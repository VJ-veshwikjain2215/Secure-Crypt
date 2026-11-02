package com.securecrypt.service;

import com.securecrypt.utility.ValidationUtil;
import org.springframework.stereotype.Service;

/**
 * Classical Caesar Cipher - shifts every alphabetic character by a fixed
 * number of positions ("shift"). Non-alphabetic characters (digits, spaces,
 * punctuation) are left untouched so the message stays readable/structured.
 *
 * Implemented manually (no library) as required by the assignment.
 */
@Service
public class CaesarCipherService implements EncryptionService {

    @Override
    public String encrypt(String plainText, String key) {
        ValidationUtil.requireNonEmptyText(plainText);
        int shift = ValidationUtil.parseShift(key);
        return shiftText(plainText, shift);
    }

    @Override
    public String decrypt(String cipherText, String key) {
        ValidationUtil.requireCipherText(cipherText);
        int shift = ValidationUtil.parseShift(key);
        // Decryption is simply an encryption with the negative (complementary) shift
        return shiftText(cipherText, 26 - shift);
    }

    private String shiftText(String text, int shift) {
        StringBuilder result = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                result.append((char) ('A' + (c - 'A' + shift) % 26));
            } else if (c >= 'a' && c <= 'z') {
                result.append((char) ('a' + (c - 'a' + shift) % 26));
            } else {
                result.append(c); // digits, spaces, punctuation stay as-is
            }
        }
        return result.toString();
    }
}
