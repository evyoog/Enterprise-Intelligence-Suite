package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.InvalidCredentialsException;
import com.vyoog.eisplatform.modules.registration.model.EmailVerificationToken;
import com.vyoog.eisplatform.modules.registration.model.RegistrationOwnerType;
import com.vyoog.eisplatform.modules.registration.repository.EmailVerificationTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Single-use, expiring email verification tokens. Only a HASH of the raw
 * token is ever persisted — the raw value exists only in the emailed link
 * and in this class's return value on generation, never logged, never
 * stored. {@link EmailVerificationToken#getUsedAt()} is set once and never
 * cleared, so a reused link fails instead of silently succeeding again.
 */
@Service
public class EmailVerificationService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final EmailVerificationTokenRepository tokenRepository;
    private final Duration tokenTtl;

    public EmailVerificationService(
            EmailVerificationTokenRepository tokenRepository,
            @Value("${vyoog.registration.verification-token-ttl-hours}") long tokenTtlHours) {
        this.tokenRepository = tokenRepository;
        this.tokenTtl = Duration.ofHours(tokenTtlHours);
    }

    /** Returns the RAW token (only ever exposed here, for the caller to put
     * in an emailed link) — the stored row only ever has its hash. */
    @Transactional
    public String issueForCustomer(Long customerId) {
        return issue(RegistrationOwnerType.INDIVIDUAL, customerId, null);
    }

    @Transactional
    public String issueForOrganization(Long organizationId) {
        return issue(RegistrationOwnerType.ORGANIZATION, null, organizationId);
    }

    private String issue(RegistrationOwnerType type, Long customerId, Long organizationId) {
        String rawToken = generateRawToken();
        EmailVerificationToken token = new EmailVerificationToken();
        token.setTokenHash(hash(rawToken));
        token.setRegistrantType(type);
        token.setCustomerId(customerId);
        token.setOrganizationId(organizationId);
        token.setExpiresAt(Instant.now().plus(tokenTtl));
        tokenRepository.save(token);
        return rawToken;
    }

    /** Marks the token used and returns it — throws if it's missing,
     * expired, or already used. Callers use the returned token's
     * customerId/organizationId to know which registration to advance. */
    @Transactional
    public EmailVerificationToken consume(String rawToken) {
        EmailVerificationToken token = tokenRepository.findByTokenHash(hash(rawToken))
            .orElseThrow(() -> new InvalidCredentialsException("This verification link is invalid."));
        if (token.getUsedAt() != null) {
            throw new InvalidCredentialsException("This verification link has already been used.");
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidCredentialsException("This verification link has expired.");
        }
        token.setUsedAt(Instant.now());
        tokenRepository.save(token);
        return token;
    }

    private static String generateRawToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
