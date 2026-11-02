package com.securecrypt.service;

/**
 * Strategy interface implemented by every cipher (Caesar, Vigenere, XOR,
 * AES, DES, 3DES). CryptoServiceFactory picks the right implementation at
 * runtime based on the algorithm requested by the client, so the controller
 * never needs an if/else chain.
 */
public interface EncryptionService {

    /**
     * Encrypts plainText using the supplied key/password.
     * @param plainText the text entered by the user
     * @param key       shift value / keyword / password depending on algorithm
     * @return the cipher text (Base64 encoded for binary algorithms)
     */
    String encrypt(String plainText, String key);

    /**
     * Decrypts cipherText back to the original plain text using the supplied key/password.
     * @throws com.securecrypt.exception.CryptoException if the key is wrong or text is malformed
     */
    String decrypt(String cipherText, String key);
}
