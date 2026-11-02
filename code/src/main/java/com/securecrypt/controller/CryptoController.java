package com.securecrypt.controller;

import com.securecrypt.model.AlgorithmType;
import com.securecrypt.model.CryptoRequest;
import com.securecrypt.model.CryptoResponse;
import com.securecrypt.service.CryptoServiceFactory;
import com.securecrypt.service.EncryptionService;
import com.securecrypt.utility.KeyGeneratorUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * REST API layer for SecureCrypt.
 *
 * Endpoints:
 *   POST /api/encrypt       - encrypts text with the chosen algorithm
 *   POST /api/decrypt       - decrypts text with the chosen algorithm
 *   GET  /api/generate-key  - returns a random strong key/password
 *   GET  /api/key-strength  - retired endpoint (410); do not send keys in URLs
 *
 * The controller stays intentionally thin: validation happens via @Valid,
 * algorithm dispatch happens via CryptoServiceFactory, and all error
 * translation happens in GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api")

public class CryptoController {

    private final CryptoServiceFactory cryptoServiceFactory;

    public CryptoController(CryptoServiceFactory cryptoServiceFactory) {
        this.cryptoServiceFactory = cryptoServiceFactory;
    }

    @PostMapping("/encrypt")
    public CryptoResponse encrypt(@Valid @RequestBody CryptoRequest request) {
        AlgorithmType algorithmType = AlgorithmType.fromString(request.getAlgorithm());
        EncryptionService service = cryptoServiceFactory.getService(algorithmType);

        String result = service.encrypt(request.getText(), request.getKey());
        return CryptoResponse.success(result, algorithmType.name());
    }

    @PostMapping("/decrypt")
    public CryptoResponse decrypt(@Valid @RequestBody CryptoRequest request) {
        AlgorithmType algorithmType = AlgorithmType.fromString(request.getAlgorithm());
        EncryptionService service = cryptoServiceFactory.getService(algorithmType);

        String result = service.decrypt(request.getText(), request.getKey());
        return CryptoResponse.success(result, algorithmType.name());
    }

    @GetMapping("/generate-key")
    public CryptoResponse generateKey(@RequestParam(defaultValue = "16") int length) {
        String key = KeyGeneratorUtil.generateRandomKey(length);
        CryptoResponse response = CryptoResponse.success(key, "KEY_GENERATOR");
        response.setMessage("Random key generated successfully.");
        return response;
    }

    @GetMapping("/key-strength")
    public org.springframework.http.ResponseEntity<CryptoResponse> retiredStrength() {
        return org.springframework.http.ResponseEntity.status(410).body(CryptoResponse.failure("This endpoint is retired. Never put a password in a URL."));
    }
}
