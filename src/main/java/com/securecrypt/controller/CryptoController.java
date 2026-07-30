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
 *   GET  /api/key-strength  - returns a strength rating for a given key
 *
 * The controller stays intentionally thin: validation happens via @Valid,
 * algorithm dispatch happens via CryptoServiceFactory, and all error
 * translation happens in GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // relaxed for local academic project use
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
    public CryptoResponse keyStrength(@RequestParam String key) {
        String strength = KeyGeneratorUtil.estimateStrength(key);
        CryptoResponse response = CryptoResponse.success(strength, "KEY_STRENGTH");
        response.setMessage("Strength evaluated.");
        return response;
    }
}
