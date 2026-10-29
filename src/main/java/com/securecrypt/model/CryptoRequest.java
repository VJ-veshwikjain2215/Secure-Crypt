package com.securecrypt.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Single request DTO reused by both /api/encrypt and /api/decrypt.
 *
 * text      -> plain text (for encrypt) or cipher text (for decrypt)
 * algorithm -> one of AlgorithmType (sent as a plain string from the UI)
 * key       -> password / shift value / keyword depending on algorithm
 */
public class CryptoRequest {

    @NotBlank(message = "Input text must not be empty.")
    @Size(max = 120000, message = "Input exceeds the maximum ciphertext size.")
    private String text;

    @NotBlank(message = "Please select an algorithm.")
    @Size(max = 32, message = "Algorithm name is too long.")
    private String algorithm;

    // Key can legitimately be blank for algorithms that don't need one (none currently),
    // so it is validated manually inside each service instead of with @NotBlank.
    @Size(max = 1024, message = "Key is too long (max 1024 characters).")
    private String key;

    public CryptoRequest() {
    }

    public CryptoRequest(String text, String algorithm, String key) {
        this.text = text;
        this.algorithm = algorithm;
        this.key = key;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
