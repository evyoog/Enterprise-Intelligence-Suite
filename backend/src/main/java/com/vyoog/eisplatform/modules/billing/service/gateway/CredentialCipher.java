package com.vyoog.eisplatform.modules.billing.service.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * C98 (REQ-BIL-003.5): encrypts the credentials an administrator stores for a payment provider, with AES-256-GCM. The master key comes from the
 * environment ({@code PAYMENT_GATEWAY_ENCRYPTION_KEY}, a base64 string of 32 bytes, see config/secrets.env.example), never from the database or
 * source control. Without it nothing can be stored, and credentials already in the secrets file keep working.
 */
@Component
public class CredentialCipher {

    private static final Logger log = LoggerFactory.getLogger(CredentialCipher.class);
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public CredentialCipher(@Value("${vyoog.billing.gateway.encryption-key:}") String base64Key) {
        SecretKeySpec parsed = null;
        if (base64Key != null && !base64Key.isBlank()) {
            try {
                byte[] bytes = Base64.getDecoder().decode(base64Key.trim());
                if (bytes.length == 32) {
                    parsed = new SecretKeySpec(bytes, "AES");
                } else {
                    log.warn("PAYMENT_GATEWAY_ENCRYPTION_KEY must decode to 32 bytes; stored gateway credentials are disabled.");
                }
            } catch (IllegalArgumentException e) {
                log.warn("PAYMENT_GATEWAY_ENCRYPTION_KEY is not valid base64; stored gateway credentials are disabled.");
            }
        }
        this.key = parsed;
    }

    public boolean available() {
        return key != null;
    }

    public String encrypt(String plain) {
        if (key == null) {
            throw new IllegalStateException("The credential encryption key is not configured.");
        }
        try {
            byte[] iv = new byte[IV_BYTES];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] cipherText = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ByteBuffer.allocate(iv.length + cipherText.length).put(iv).put(cipherText).array());
        } catch (Exception e) {
            throw new IllegalStateException("Could not encrypt the credentials.", e);
        }
    }

    /** Returns null when the value cannot be decrypted (wrong or missing master key, tampered data); never throws, never logs the value. */
    public String decrypt(String encoded) {
        if (key == null || encoded == null || encoded.isBlank()) {
            return null;
        }
        try {
            byte[] all = Base64.getDecoder().decode(encoded);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, all, 0, IV_BYTES));
            return new String(cipher.doFinal(all, IV_BYTES, all.length - IV_BYTES), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }
}
