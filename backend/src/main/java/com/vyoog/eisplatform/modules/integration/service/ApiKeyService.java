package com.vyoog.eisplatform.modules.integration.service;

import com.vyoog.eisplatform.common.exception.InvalidStateException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.integration.dto.AdminApiKeyDto;
import com.vyoog.eisplatform.modules.integration.dto.AdminApiKeyPageDto;
import com.vyoog.eisplatform.modules.integration.dto.ApiKeyDto;
import com.vyoog.eisplatform.modules.integration.dto.CreateApiKeyRequest;
import com.vyoog.eisplatform.modules.integration.model.ApiKey;
import com.vyoog.eisplatform.modules.integration.repository.ApiKeyRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REQ-INT-001.1/.4/.5 (C61): create, list, revoke and authenticate API keys.
 * Key format {@code eis_<8>_<40>}; the full key is returned once and stored
 * only as a SHA-256 hash (BR-1).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApiKeyService {

    public static final int MAX_ACTIVE_KEYS = 10;
    public static final int ADMIN_PAGE_SIZE = 20;
    /** C61 default: platform administration is never reachable with a key. */
    static final String EXCLUDED_AUTHORITY = "ROLE_ADMIN";

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ApiKeyRepository apiKeyRepository;
    private final CustomerRepository customerRepository;
    private final AuditService auditService;

    public List<ApiKeyDto> listOwn(Long customerId) {
        Instant now = Instant.now();
        return apiKeyRepository.findByOwnerCustomerIdOrderByCreatedAtDesc(customerId).stream()
            .map(k -> toDto(k, now, null)).toList();
    }

    @Transactional
    public ApiKeyDto create(Customer owner, String keycloakSub, Collection<String> authorities, CreateApiKeyRequest request) {
        Instant now = Instant.now();
        String name = request.name() == null ? "" : request.name().trim();
        if (name.isEmpty() || name.length() > 100) {
            throw new IllegalArgumentException("The key name must be 1 to 100 characters.");
        }
        if (request.expiresAt() != null && !request.expiresAt().isAfter(now)) {
            throw new IllegalArgumentException("The expiry date must be in the future.");
        }
        if (apiKeyRepository.countActive(owner.getId(), now) >= MAX_ACTIVE_KEYS) {
            throw new IllegalArgumentException("You already have " + MAX_ACTIVE_KEYS + " active API keys. Revoke one first.");
        }
        String prefix;
        do {
            prefix = "eis_" + random(8);
        } while (apiKeyRepository.findByKeyPrefix(prefix).isPresent());
        String fullKey = prefix + "_" + random(40);

        ApiKey key = new ApiKey();
        key.setOwnerCustomerId(owner.getId());
        key.setOwnerKeycloakSub(keycloakSub);
        key.setOwnerAuthorities(authorities.stream().filter(a -> !EXCLUDED_AUTHORITY.equals(a)).distinct()
            .collect(Collectors.joining(",")));
        key.setName(name);
        key.setKeyPrefix(prefix);
        key.setKeyHash(sha256(fullKey));
        key.setExpiresAt(request.expiresAt());
        key.setCreatedAt(now);
        key = apiKeyRepository.save(key);
        auditService.recordSuccess("API_KEY_CREATED", keycloakSub, owner.getId(), owner.getEmail(),
            "ApiKey", key.getId().toString(), null, "API key " + prefix + " \"" + name + "\" created");
        return toDto(key, now, fullKey);
    }

    @Transactional
    public ApiKeyDto revoke(Long customerId, String keycloakSub, Long keyId) {
        ApiKey key = apiKeyRepository.findById(keyId)
            .filter(k -> k.getOwnerCustomerId().equals(customerId))
            .orElseThrow(() -> new ResourceNotFoundException("API key not found"));
        if (key.getRevokedAt() != null) {
            throw new InvalidStateException("This API key is already revoked.");
        }
        Instant now = Instant.now();
        key.setRevokedAt(now);
        auditService.recordSuccess("API_KEY_REVOKED", keycloakSub, customerId, null,
            "ApiKey", key.getId().toString(), null, "API key " + key.getKeyPrefix() + " revoked");
        return toDto(key, now, null);
    }

    /**
     * BR-2/BR-3/BR-8: the principal for a presented key, or empty when the
     * key is unknown, revoked or expired. A revoked key's use is audited.
     */
    @Transactional
    public Optional<ApiKeyPrincipal> authenticate(String presentedKey) {
        if (presentedKey == null) {
            return Optional.empty();
        }
        String[] parts = presentedKey.trim().split("_");
        if (parts.length != 3 || !"eis".equals(parts[0])) {
            return Optional.empty();
        }
        Optional<ApiKey> found = apiKeyRepository.findByKeyPrefix(parts[0] + "_" + parts[1]);
        if (found.isEmpty() || !MessageDigest.isEqual(found.get().getKeyHash().getBytes(StandardCharsets.UTF_8),
                sha256(presentedKey.trim()).getBytes(StandardCharsets.UTF_8))) {
            return Optional.empty();
        }
        ApiKey key = found.get();
        Instant now = Instant.now();
        String status = key.status(now);
        if (!"ACTIVE".equals(status)) {
            if ("REVOKED".equals(status)) {
                auditService.record("API_KEY_REVOKED_USED", key.getOwnerKeycloakSub(), key.getOwnerCustomerId(), null,
                    "ApiKey", key.getId().toString(), null, "FAILURE", "Revoked API key " + key.getKeyPrefix() + " was presented");
            }
            return Optional.empty();
        }
        apiKeyRepository.recordUse(key.getId(), now);
        String email = customerRepository.findById(key.getOwnerCustomerId()).map(Customer::getEmail).orElse(null);
        List<String> authorities = key.getOwnerAuthorities() == null || key.getOwnerAuthorities().isBlank() ? List.of()
            : Arrays.stream(key.getOwnerAuthorities().split(",")).filter(a -> !a.isBlank() && !EXCLUDED_AUTHORITY.equals(a)).toList();
        return Optional.of(new ApiKeyPrincipal(key.getId(), key.getKeyPrefix(), key.getOwnerCustomerId(),
            key.getOwnerKeycloakSub(), email, authorities));
    }

    public AdminApiKeyPageDto adminList(int page) {
        int safePage = Math.max(page, 0);
        Instant now = Instant.now();
        Page<ApiKey> result = apiKeyRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(safePage, ADMIN_PAGE_SIZE));
        List<AdminApiKeyDto> items = result.getContent().stream().map(k -> new AdminApiKeyDto(k.getId(), k.getOwnerCustomerId(),
            customerRepository.findById(k.getOwnerCustomerId()).map(Customer::getEmail).orElse(null),
            k.getName(), k.getKeyPrefix(), k.status(now), k.getCreatedAt(), k.getExpiresAt(), k.getLastUsedAt(), k.getRequestCount()))
            .toList();
        return new AdminApiKeyPageDto(items, result.getTotalElements(), safePage, ADMIN_PAGE_SIZE);
    }

    private static ApiKeyDto toDto(ApiKey k, Instant now, String fullKey) {
        return new ApiKeyDto(k.getId(), k.getName(), k.getKeyPrefix(), k.status(now), k.getCreatedAt(), k.getExpiresAt(),
            k.getLastUsedAt(), k.getRequestCount(), fullKey);
    }

    private static String random(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
