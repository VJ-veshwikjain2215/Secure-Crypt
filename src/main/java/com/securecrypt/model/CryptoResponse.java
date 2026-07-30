package com.securecrypt.model;

/**
 * Standard response wrapper returned by every /api/** endpoint.
 * Keeping a single shape makes life easy on the JavaScript side.
 */
public class CryptoResponse {

    private boolean success;
    private String result;
    private String algorithm;
    private String message;
    private long processedAt;

    public CryptoResponse() {
        this.processedAt = System.currentTimeMillis();
    }

    public static CryptoResponse success(String result, String algorithm) {
        CryptoResponse response = new CryptoResponse();
        response.setSuccess(true);
        response.setResult(result);
        response.setAlgorithm(algorithm);
        response.setMessage("Operation completed successfully.");
        return response;
    }

    public static CryptoResponse failure(String message) {
        CryptoResponse response = new CryptoResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(long processedAt) {
        this.processedAt = processedAt;
    }
}
