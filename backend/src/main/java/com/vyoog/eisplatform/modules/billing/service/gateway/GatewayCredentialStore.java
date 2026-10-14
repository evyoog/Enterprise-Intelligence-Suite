package com.vyoog.eisplatform.modules.billing.service.gateway;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyoog.eisplatform.modules.billing.model.PaymentGatewayConfig;
import com.vyoog.eisplatform.modules.billing.repository.PaymentGatewayConfigRepository;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/** C98: reads and writes the encrypted credentials of a gateway. Values never leave the backend. Reads hit the database every time, so a
 * rotation takes effect at once and inside the rotating transaction. */
@Component
public class GatewayCredentialStore {

    private final PaymentGatewayConfigRepository repository;
    private final CredentialCipher cipher;
    private final ObjectMapper objectMapper;

    public GatewayCredentialStore(PaymentGatewayConfigRepository repository, CredentialCipher cipher, ObjectMapper objectMapper) {
        this.repository = repository;
        this.cipher = cipher;
        this.objectMapper = objectMapper;
    }

    /** The decrypted stored credentials, or an empty map when none are stored or they cannot be read. */
    public Map<String, String> stored(String providerCode) {
        return repository.findByProviderCode(providerCode).map(this::decrypt).orElseGet(Map::of);
    }

    public Map<String, String> decrypt(PaymentGatewayConfig config) {
        String plain = cipher.decrypt(config.getCredentialsEncrypted());
        if (plain == null) {
            return Map.of();
        }
        try {
            return new LinkedHashMap<>(objectMapper.readValue(plain, new TypeReference<Map<String, String>>() { }));
        } catch (Exception e) {
            return Map.of();
        }
    }

    /** True when ciphertext is stored but cannot be decrypted (the master key changed or is missing). */
    public boolean unreadable(PaymentGatewayConfig config) {
        return config.getCredentialsEncrypted() != null && !config.getCredentialsEncrypted().isBlank() && cipher.decrypt(config.getCredentialsEncrypted()) == null;
    }

    public void write(PaymentGatewayConfig config, Map<String, String> values) {
        try {
            config.setCredentialsEncrypted(values.isEmpty() ? null : cipher.encrypt(objectMapper.writeValueAsString(values)));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException("Could not store the credentials.", e);
        }
    }

    public boolean encryptionAvailable() {
        return cipher.available();
    }
}
