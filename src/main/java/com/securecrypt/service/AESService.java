package com.securecrypt.service;
import com.securecrypt.exception.CryptoException;
import com.securecrypt.utility.ValidationUtil;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/** SC1: AES-128-GCM, PBKDF2-HMAC-SHA256, random salt/nonce, authenticated header. */
@Service
public class AESService implements EncryptionService {
    public static final int ITERATIONS = 600_000;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    @Override public String encrypt(String text, String password) {
        ValidationUtil.requireNonEmptyText(text); ValidationUtil.requireKey(password, "AES");
        byte[] salt = new byte[16], nonce = new byte[12]; RANDOM.nextBytes(salt); RANDOM.nextBytes(nonce);
        String header = "SC1." + ITERATIONS + "." + ENCODER.encodeToString(salt) + "." + ENCODER.encodeToString(nonce);
        byte[] key = derive(password, salt);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, nonce));
            cipher.updateAAD(header.getBytes(StandardCharsets.UTF_8));
            return header + "." + ENCODER.encodeToString(cipher.doFinal(text.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException ex) { throw new CryptoException("Encryption could not be completed."); }
        finally { Arrays.fill(key, (byte)0); }
    }
    @Override public String decrypt(String envelope, String password) {
        ValidationUtil.requireCipherText(envelope); ValidationUtil.requireKey(password, "AES");
        String[] parts = envelope.trim().split("\\.", -1);
        if (parts.length != 5 || !parts[0].equals("SC1") || !parts[1].equals(String.valueOf(ITERATIONS)))
            throw new CryptoException("Unsupported AES format. Select legacy AES-CBC only for old unversioned outputs.");
        byte[] key = null;
        try {
            byte[] salt = decode(parts[2]), nonce = decode(parts[3]), ciphertext = decode(parts[4]);
            if (salt.length != 16 || nonce.length != 12 || ciphertext.length < 16 || ciphertext.length > 80016) throw new IllegalArgumentException();
            String header = String.join(".", Arrays.copyOf(parts, 4)); key = derive(password, salt);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, nonce));
            cipher.updateAAD(header.getBytes(StandardCharsets.UTF_8));
            String result = new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
            ValidationUtil.requireNonEmptyText(result); return result;
        } catch (GeneralSecurityException | IllegalArgumentException ex) { throw new CryptoException("Decryption failed: wrong passphrase or invalid/modified message."); }
        finally { if (key != null) Arrays.fill(key, (byte)0); }
    }
    private static byte[] decode(String value) {
        byte[] bytes = Base64.getUrlDecoder().decode(value);
        if (!ENCODER.encodeToString(bytes).equals(value)) throw new IllegalArgumentException();
        return bytes;
    }
    private static byte[] derive(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, 128);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        catch (GeneralSecurityException ex) { throw new CryptoException("Key derivation is unavailable."); }
        finally { spec.clearPassword(); }
    }
}
