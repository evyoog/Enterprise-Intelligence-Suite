package com.vyoog.eisplatform.modules.auth.service;

import com.vyoog.eisplatform.common.exception.InvalidCredentialsException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.auth.model.PasswordResetToken;
import com.vyoog.eisplatform.modules.auth.repository.PasswordResetTokenRepository;
import com.vyoog.eisplatform.modules.notification.model.NotificationCategory;
import com.vyoog.eisplatform.modules.notification.model.NotificationSeverity;
import com.vyoog.eisplatform.modules.notification.service.NotificationService;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.service.EmailService;
import com.vyoog.eisplatform.modules.registration.service.PasswordPolicy;
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
 * Phase 8: forgot-password / reset-password. Password storage and
 * validation stay entirely inside Keycloak — this class never persists a
 * password anywhere; the only thing the Vyoog database ever stores is a
 * HASH of a single-use reset token (same convention as
 * {@code EmailVerificationService}), not the credential itself.
 *
 * <p>Anti-enumeration: {@link #requestReset} always completes the same way
 * from the caller's perspective (the controller returns an identical 202
 * regardless) whether or not the email matches a real Keycloak account —
 * mirroring {@code RegistrationService}'s own duplicate-email handling. The
 * one difference from that precedent: there's no "someone tried to reset a
 * password for an email with no account" notice to send, because there's no
 * account owner to receive it — silence is the correct anti-enumeration
 * behavior here, not a missing feature.
 */
@Service
public class PasswordResetService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final PasswordResetTokenRepository tokenRepository;
    private final KeycloakAdminClient keycloakAdminClient;
    private final EmailService emailService;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final Duration tokenTtl;
    private final String frontendUrl;

    public PasswordResetService(
            PasswordResetTokenRepository tokenRepository,
            KeycloakAdminClient keycloakAdminClient,
            EmailService emailService,
            CustomerRepository customerRepository,
            NotificationService notificationService,
            AuditService auditService,
            @Value("${vyoog.auth.password-reset-token-ttl-minutes}") long tokenTtlMinutes,
            @Value("${app.frontend-url}") String frontendUrl) {
        this.tokenRepository = tokenRepository;
        this.keycloakAdminClient = keycloakAdminClient;
        this.emailService = emailService;
        this.customerRepository = customerRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
        this.tokenTtl = Duration.ofMinutes(tokenTtlMinutes);
        this.frontendUrl = frontendUrl;
    }

    /** Never throws for "no such account," never reveals it either — see
     * this class's own javadoc. */
    @Transactional
    public void requestReset(String email) {
        keycloakAdminClient.findUserIdByEmail(email).ifPresent(keycloakUserId -> {
            String rawToken = generateRawToken();
            PasswordResetToken token = new PasswordResetToken();
            token.setTokenHash(hash(rawToken));
            token.setKeycloakSub(keycloakUserId);
            token.setEmail(email);
            token.setExpiresAt(Instant.now().plus(tokenTtl));
            tokenRepository.save(token);
            emailService.sendPasswordResetEmail(email, frontendUrl + "/reset-password?token=" + rawToken);
            auditService.recordSuccess("PASSWORD_RESET_REQUESTED", keycloakUserId, null, email, "Customer", keycloakUserId, null, null);
        });
    }

    /**
     * Validates the token and the new password BEFORE marking anything used
     * or touching Keycloak — and, critically, only marks the token used
     * AFTER the Keycloak call actually succeeds. Getting this ordering
     * backwards (mark-used-then-call-Keycloak) would burn a valid link on a
     * transient Keycloak failure, leaving the user unable to retry it even
     * though their password never actually changed.
     */
    @Transactional
    public void resetPassword(String rawToken, String newPassword, String confirmPassword) {
        PasswordPolicy.validate(newPassword, confirmPassword);

        PasswordResetToken token = tokenRepository.findByTokenHash(hash(rawToken))
            .orElseThrow(() -> new InvalidCredentialsException("This password reset link is invalid."));
        if (token.getUsedAt() != null) {
            throw new InvalidCredentialsException("This password reset link has already been used.");
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidCredentialsException("This password reset link has expired.");
        }

        boolean succeeded = keycloakAdminClient.resetPassword(token.getKeycloakSub(), newPassword);
        if (!succeeded) {
            throw new IllegalStateException("Could not reset the password right now. Please try again shortly.");
        }

        token.setUsedAt(Instant.now());
        tokenRepository.save(token);

        // Best-effort, never blocks the reset itself from being considered
        // successful — the credential change above is what actually matters.
        keycloakAdminClient.logoutAllSessions(token.getKeycloakSub());
        emailService.sendPasswordChangedNotice(token.getEmail());

        // In-app record only — sendPasswordChangedNotice above already sent
        // the (more specific) email for this exact event.
        customerRepository.findByKeycloakSub(token.getKeycloakSub()).ifPresent(customer -> {
            notificationService.notifyInAppOnly(customer.getId(), NotificationCategory.SECURITY, NotificationSeverity.WARNING,
                "Your password was changed",
                "Your Vyoog account password was just changed and you've been signed out everywhere as a precaution.");
            auditService.recordSuccess("PASSWORD_RESET_COMPLETED", token.getKeycloakSub(), customer.getId(),
                token.getEmail(), "Customer", customer.getId().toString(), null, null);
        });
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
