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
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
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
 * REQ-INT-001.1/.4/.5 (C61, C92): create, list, revoke and authenticate API keys. Keys belong to an organization and
 * are created by its administrators (API-1 B); a key is READ (read-only) or READ_WRITE (API-4 B). Key format
 * {@code eis_<8>_<40>}; the full key is returned once and stored only as a SHA-256 hash (BR-1).
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
    private final OrganizationMemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final AuditService auditService;

    /** Legacy personal keys of a person (created before C92); organization keys are listed through the organization. */
    public List<ApiKeyDto> listOwn(Long customerId) {
        Instant now = Instant.now();
        return apiKeyRepository.findByOwnerCustomerIdOrderByCreatedAtDesc(customerId).stream()
            .filter(k -> k.getOrganizationId() == null)
            .map(k -> toDto(k, now, null)).toList();
    }

    public List<ApiKeyDto> listForOrganization(Long organizationId) {
        Instant now = Instant.now();
        return apiKeyRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId).stream()
            .map(k -> toDto(k, now, null)).toList();
    }

    /** C92 (API-1 B): an organization administrator creates a key for the organization's integrations. */
    @Transactional
    public ApiKeyDto createForOrganization(Customer creator, String keycloakSub, Collection<String> authorities,
                                           Long organizationId, CreateApiKeyRequest request) {
        Instant now = Instant.now();
        String name = request.name() == null ? "" : request.name().trim();
        if (name.isEmpty() || name.length() > 100) {
            throw new IllegalArgumentException("The key name must be 1 to 100 characters.");
        }
        if (request.expiresAt() != null && !request.expiresAt().isAfter(now)) {
            throw new IllegalArgumentException("The expiry date must be in the future.");
        }
        String scope = parseScope(request.scope());
        if (apiKeyRepository.countActiveForOrganization(organizationId, now) >= MAX_ACTIVE_KEYS) {
            throw new IllegalArgumentException("Your organization already has " + MAX_ACTIVE_KEYS + " active API keys. Revoke one first.");
        }
        String prefix;
        do {
            prefix = "eis_" + random(8);
        } while (apiKeyRepository.findByKeyPrefix(prefix).isPresent());
        String fullKey = prefix + "_" + random(40);

        ApiKey key = new ApiKey();
        key.setOwnerCustomerId(creator.getId());
        key.setCreatedByCustomerId(creator.getId());
        key.setOrganizationId(organizationId);
        key.setScope(scope);
        key.setOwnerKeycloakSub(keycloakSub);
        key.setOwnerAuthorities(authorities.stream().filter(a -> !EXCLUDED_AUTHORITY.equals(a)).distinct()
            .collect(Collectors.joining(",")));
        key.setName(name);
        key.setKeyPrefix(prefix);
        key.setKeyHash(sha256(fullKey));
        key.setExpiresAt(request.expiresAt());
        key.setCreatedAt(now);
        key = apiKeyRepository.save(key);
        auditService.recordSuccess("API_KEY_CREATED", keycloakSub, creator.getId(), creator.getEmail(),
            "ApiKey", key.getId().toString(), organizationId, "API key " + prefix + " \"" + name + "\" (" + scope + ") created");
        return toDto(key, now, fullKey);
    }

    /** An organization administrator revokes one of the organization's keys. */
    @Transactional
    public ApiKeyDto revokeForOrganization(Long organizationId, Long actorCustomerId, String keycloakSub, Long keyId) {
        ApiKey key = apiKeyRepository.findById(keyId)
            .filter(k -> organizationId.equals(k.getOrganizationId()))
            .orElseThrow(() -> new ResourceNotFoundException("API key not found"));
        return revoke(key, actorCustomerId, keycloakSub);
    }

    /** A person revokes one of their own legacy personal keys. */
    @Transactional
    public ApiKeyDto revoke(Long customerId, String keycloakSub, Long keyId) {
        ApiKey key = apiKeyRepository.findById(keyId)
            .filter(k -> k.getOrganizationId() == null && k.getOwnerCustomerId().equals(customerId))
            .orElseThrow(() -> new ResourceNotFoundException("API key not found"));
        return revoke(key, customerId, keycloakSub);
    }

    private ApiKeyDto revoke(ApiKey key, Long actorCustomerId, String keycloakSub) {
        if (key.getRevokedAt() != null) {
            throw new InvalidStateException("This API key is already revoked.");
        }
        Instant now = Instant.now();
        key.setRevokedAt(now);
        auditService.recordSuccess("API_KEY_REVOKED", keycloakSub, actorCustomerId, null,
            "ApiKey", key.getId().toString(), key.getOrganizationId(), "API key " + key.getKeyPrefix() + " revoked");
        return toDto(key, now, null);
    }

    static String parseScope(String raw) {
        if (raw == null || raw.isBlank()) {
            return ApiKey.SCOPE_READ;
        }
        String scope = raw.trim().toUpperCase(java.util.Locale.ROOT).replace('-', '_');
        if (!ApiKey.SCOPE_READ.equals(scope) && !ApiKey.SCOPE_READ_WRITE.equals(scope)) {
            throw new IllegalArgumentException("The scope must be READ (read-only) or READ_WRITE.");
        }
        return scope;
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
        // C92 (API-1 B): an organization key acts as an active administrator of the organization, so it keeps working
        // when the person who created it leaves. A legacy personal key acts as its owner.
        Customer actor;
        if (key.getOrganizationId() != null) {
            Optional<Customer> admin = actingAdministrator(key.getOrganizationId());
            if (admin.isEmpty()) {
                auditService.record("API_KEY_NO_ADMINISTRATOR", key.getOwnerKeycloakSub(), key.getOwnerCustomerId(), null,
                    "ApiKey", key.getId().toString(), key.getOrganizationId(), "FAILURE",
                    "API key " + key.getKeyPrefix() + " was refused: the organization has no active administrator");
                return Optional.empty();
            }
            actor = admin.get();
        } else {
            actor = customerRepository.findById(key.getOwnerCustomerId()).orElse(null);
        }
        apiKeyRepository.recordUse(key.getId(), now);
        List<String> authorities = key.getOwnerAuthorities() == null || key.getOwnerAuthorities().isBlank() ? List.of()
            : Arrays.stream(key.getOwnerAuthorities().split(",")).filter(a -> !a.isBlank() && !EXCLUDED_AUTHORITY.equals(a)).toList();
        Long actorId = actor == null ? key.getOwnerCustomerId() : actor.getId();
        String actorSub = actor == null || actor.getKeycloakSub() == null ? key.getOwnerKeycloakSub() : actor.getKeycloakSub();
        return Optional.of(new ApiKeyPrincipal(key.getId(), key.getKeyPrefix(), actorId, actorSub,
            actor == null ? null : actor.getEmail(), authorities, key.getOrganizationId(), key.getScope()));
    }

    /** The oldest active administrator of the organization who has a login. */
    private Optional<Customer> actingAdministrator(Long organizationId) {
        return memberRepository.findByOrganizationIdAndOrgRoleAndStatus(organizationId, OrgRole.ORG_ADMIN, MembershipStatus.ACTIVE).stream()
            .sorted(java.util.Comparator.comparing(OrganizationMember::getId))
            .map(m -> customerRepository.findById(m.getCustomerId()).orElse(null))
            .filter(c -> c != null && c.getKeycloakSub() != null)
            .findFirst();
    }

    public AdminApiKeyPageDto adminList(int page) {
        int safePage = Math.max(page, 0);
        Instant now = Instant.now();
        Page<ApiKey> result = apiKeyRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(safePage, ADMIN_PAGE_SIZE));
        List<AdminApiKeyDto> items = result.getContent().stream().map(k -> new AdminApiKeyDto(k.getId(), k.getOwnerCustomerId(),
            customerRepository.findById(k.getOwnerCustomerId()).map(Customer::getEmail).orElse(null),
            k.getOrganizationId(),
            k.getOrganizationId() == null ? null : organizationRepository.findById(k.getOrganizationId()).map(Organization::getName).orElse(null),
            k.getScope(), k.getName(), k.getKeyPrefix(), k.status(now), k.getCreatedAt(), k.getExpiresAt(), k.getLastUsedAt(), k.getRequestCount()))
            .toList();
        return new AdminApiKeyPageDto(items, result.getTotalElements(), safePage, ADMIN_PAGE_SIZE);
    }

    private ApiKeyDto toDto(ApiKey k, Instant now, String fullKey) {
        String createdBy = k.getCreatedByCustomerId() == null ? null : customerRepository.findById(k.getCreatedByCustomerId())
            .map(c -> ((c.getFirstName() == null ? "" : c.getFirstName()) + " " + (c.getLastName() == null ? "" : c.getLastName())).trim())
            .orElse(null);
        return new ApiKeyDto(k.getId(), k.getName(), k.getKeyPrefix(), k.status(now), k.getScope(), k.getOrganizationId() == null,
            createdBy, k.getCreatedAt(), k.getExpiresAt(), k.getLastUsedAt(), k.getRequestCount(), fullKey);
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
