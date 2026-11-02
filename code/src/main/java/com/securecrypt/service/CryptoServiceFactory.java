package com.securecrypt.service;

import com.securecrypt.model.AlgorithmType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Factory (Strategy pattern) that maps an AlgorithmType to its
 * corresponding EncryptionService implementation. Spring injects every
 * @Service bean here once at startup; the controller then simply asks the
 * factory for the right strategy instead of maintaining its own if/else
 * ladder - this is what "no code duplication / modular architecture" means
 * in the assignment brief.
 */
@Component
public class CryptoServiceFactory {

    private final Map<AlgorithmType, EncryptionService> serviceMap = new EnumMap<>(AlgorithmType.class);

    public CryptoServiceFactory(CaesarCipherService caesarCipherService,
                                 VigenereCipherService vigenereCipherService,
                                 XORCipherService xorCipherService,
                                 AESService aesService,
                                 LegacyAESService legacyAESService,
                                 DESService desService,
                                 TripleDESService tripleDESService) {
        serviceMap.put(AlgorithmType.CAESAR, caesarCipherService);
        serviceMap.put(AlgorithmType.VIGENERE, vigenereCipherService);
        serviceMap.put(AlgorithmType.XOR, xorCipherService);
        serviceMap.put(AlgorithmType.AES, aesService);
        serviceMap.put(AlgorithmType.AES_LEGACY, legacyAESService);
        serviceMap.put(AlgorithmType.DES, desService);
        serviceMap.put(AlgorithmType.TRIPLE_DES, tripleDESService);
    }

    public EncryptionService getService(AlgorithmType algorithmType) {
        EncryptionService service = serviceMap.get(algorithmType);
        if (service == null) {
            throw new IllegalArgumentException("No service registered for algorithm: " + algorithmType);
        }
        return service;
    }
}
