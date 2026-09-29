package com.vyoog.eisplatform.modules.auth.service;

import com.vyoog.eisplatform.common.exception.InvalidCredentialsException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.auth.dto.MfaEnrollResponse;
import com.vyoog.eisplatform.modules.auth.dto.MfaRecoveryCodesResponse;
import com.vyoog.eisplatform.modules.auth.dto.MfaStatusDto;
import com.vyoog.eisplatform.modules.auth.model.CustomerMfaCredential;
import com.vyoog.eisplatform.modules.auth.model.MfaChallengeKind;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.auth.model.MfaLoginChallenge;
import com.vyoog.eisplatform.modules.auth.model.MfaRecoveryCode;
import com.vyoog.eisplatform.modules.auth.repository.CustomerMfaCredentialRepository;
import com.vyoog.eisplatform.modules.auth.repository.MfaLoginChallengeRepository;
import com.vyoog.eisplatform.modules.auth.repository.MfaRecoveryCodeRepository;
import com.vyoog.eisplatform.modules.notification.model.NotificationCategory;
import com.vyoog.eisplatform.modules.notification.model.NotificationSeverity;
import com.vyoog.eisplatform.modules.notification.service.NotificationService;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.recovery.RecoveryCodeGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
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
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Platform-owned TOTP second factor, layered on top of (never replacing)
 * Keycloak's own username/password authentication.
 *
 * <p><b>Why this exists instead of Keycloak-native TOTP</b>: empirically
 * verified against the real realm (see this phase's own report) that there
 * is no headless way to enroll Keycloak-native TOTP — Keycloak's own Account
 * REST API reports the OTP credential type's {@code createAction} as
 * {@code CONFIGURE_TOTP}, a browser required-action flow that renders a
 * Keycloak-hosted page; and directly writing a {@code CredentialRepresentation}
 * of type {@code otp} via the Admin API (undocumented for use against an
 * already-existing user, only intended for realm import) was tested and
 * found to silently corrupt the account's login entirely. Given the explicit
 * rule to never show Keycloak's hosted UI, Platform TOTP is the deliberate
 * alternative: Keycloak remains the sole identity/password authority
 * (nothing here ever touches a Keycloak credential), and this service is
 * simply an additional factor gate this app enforces before it will
 * consider a login session complete — see AuthController#login /
 * #verifyPlatformMfa for the two halves of that gate.
 */
@Service
public class PlatformMfaService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Pattern SIX_DIGIT = Pattern.compile("^\\d{6}$");

    private final CustomerMfaCredentialRepository credentialRepository;
    private final MfaRecoveryCodeRepository recoveryCodeRepository;
    private final MfaLoginChallengeRepository challengeRepository;
    private final TotpSecretCipher cipher;
    private final CurrentPasswordVerifier currentPasswordVerifier;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final CustomerRepository customerRepository;
    private final String issuer;
    private final Duration challengeTtl;
    private final int maxChallengeAttempts;
    private final int recoveryCodeCount;

    private final CodeVerifier codeVerifier;
    private final DefaultSecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final QrGenerator qrGenerator = new ZxingPngQrGenerator();
    private final RecoveryCodeGenerator recoveryCodeGenerator = new RecoveryCodeGenerator();

    public PlatformMfaService(
            CustomerMfaCredentialRepository credentialRepository,
            MfaRecoveryCodeRepository recoveryCodeRepository,
            MfaLoginChallengeRepository challengeRepository,
            TotpSecretCipher cipher,
            CurrentPasswordVerifier currentPasswordVerifier,
            AuditService auditService,
            NotificationService notificationService,
            CustomerRepository customerRepository,
            @Value("${vyoog.auth.platform-mfa.issuer}") String issuer,
            @Value("${vyoog.auth.platform-mfa.challenge-ttl-minutes}") long challengeTtlMinutes,
            @Value("${vyoog.auth.platform-mfa.max-challenge-attempts}") int maxChallengeAttempts,
            @Value("${vyoog.auth.platform-mfa.recovery-code-count}") int recoveryCodeCount) {
        this.credentialRepository = credentialRepository;
        this.recoveryCodeRepository = recoveryCodeRepository;
        this.challengeRepository = challengeRepository;
        this.cipher = cipher;
        this.currentPasswordVerifier = currentPasswordVerifier;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.customerRepository = customerRepository;
        this.issuer = issuer;
        this.challengeTtl = Duration.ofMinutes(challengeTtlMinutes);
        this.maxChallengeAttempts = maxChallengeAttempts;
        this.recoveryCodeCount = recoveryCodeCount;

        DefaultCodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());
        verifier.setTimePeriod(30);
        // Allows the code from one 30s step before/after the server's own
        // clock — the library's own supported mechanism for tolerating
        // reasonable client/server clock skew, not a hand-rolled window.
        verifier.setAllowedTimePeriodDiscrepancy(1);
        this.codeVerifier = verifier;
    }

    // ---------------------------------------------------------------
    // Status
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public MfaStatusDto getStatus(Long customerId) {
        return credentialRepository.findById(customerId)
            .filter(CustomerMfaCredential::isEnabled)
            .map(c -> new MfaStatusDto(true, c.getEnrolledAt(), recoveryCodeRepository.countByCustomerIdAndUsedAtIsNull(customerId)))
            .orElseGet(() -> new MfaStatusDto(false, null, 0));
    }

    @Transactional(readOnly = true)
    public boolean isEnabledFor(Long customerId) {
        return credentialRepository.findById(customerId).map(CustomerMfaCredential::isEnabled).orElse(false);
    }

    // ---------------------------------------------------------------
    // Enrollment
    // ---------------------------------------------------------------

    /** Starts (or restarts) enrollment — always requires a fresh password
     * check against Keycloak first (see this class's own javadoc on why
     * every state-changing MFA action re-verifies the password rather than
     * trusting an already-authenticated session alone). A previous, never
     * verified pending secret (if any) is simply overwritten — it was
     * inert (enabled stayed false) and never usable to log in. */
    @Transactional
    public MfaEnrollResponse enroll(Long customerId, String email, String currentPassword) {
        requireFreshPassword(email, currentPassword);
        return beginEnrollment(customerId, email);
    }

    /** Generates a new, not-yet-enabled secret and its QR code. Callers have
     * already re-authenticated the user: a fresh password ({@link #enroll})
     * or a sign-in that just succeeded ({@link #startChallengeEnrollment}). */
    private MfaEnrollResponse beginEnrollment(Long customerId, String email) {
        String secret = secretGenerator.generate();

        CustomerMfaCredential credential = credentialRepository.findById(customerId).orElseGet(() -> {
            CustomerMfaCredential fresh = new CustomerMfaCredential();
            fresh.setCustomerId(customerId);
            fresh.setCreatedAt(Instant.now());
            return fresh;
        });
        credential.setEnabled(false);
        credential.setEncryptedSecret(cipher.encrypt(secret));
        credential.setSecretSetAt(Instant.now());
        credential.setUpdatedAt(Instant.now());
        credentialRepository.save(credential);

        auditService.recordSuccess("MFA_ENROLLMENT_STARTED", null, customerId, email, "Customer", String.valueOf(customerId), null, null);

        QrData data = new QrData.Builder()
            .label(email)
            .secret(secret)
            .issuer(issuer)
            .algorithm(HashingAlgorithm.SHA1)
            .digits(6)
            .period(30)
            .build();

        String qrBase64;
        try {
            qrBase64 = Base64.getEncoder().encodeToString(qrGenerator.generate(data));
        } catch (QrGenerationException e) {
            throw new IllegalStateException("Could not generate QR code", e);
        }

        return new MfaEnrollResponse(secret, data.getUri(), qrBase64);
    }

    /** Only after this succeeds does {@code enabled} become true — a secret
     * that was merely generated is never sufficient on its own. */
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public MfaRecoveryCodesResponse verifyEnrollment(Long customerId, String email, String code) {
        CustomerMfaCredential credential = credentialRepository.findById(customerId)
            .filter(c -> c.getEncryptedSecret() != null)
            .orElseThrow(() -> new InvalidCredentialsException("No MFA enrollment is in progress"));

        String secret = cipher.decrypt(credential.getEncryptedSecret());
        if (!codeVerifier.isValidCode(secret, code)) {
            auditService.recordFailure("MFA_ENROLLMENT_FAILED", email, "Invalid verification code");
            throw new InvalidCredentialsException("Invalid verification code");
        }

        credential.setEnabled(true);
        credential.setEnrolledAt(Instant.now());
        credential.setLastVerifiedAt(Instant.now());
        credential.setUpdatedAt(Instant.now());
        credentialRepository.save(credential);

        List<String> rawCodes = regenerateRecoveryCodesFor(customerId);

        auditService.recordSuccess("MFA_ENROLLMENT_COMPLETED", null, customerId, email, "Customer", String.valueOf(customerId), null, null);
        notificationService.notify(customerId, email, NotificationCategory.SECURITY, NotificationSeverity.INFO,
            "Two-factor authentication enabled",
            "Two-factor authentication was just turned on for your account. If this wasn't you, contact your administrator immediately.");

        return new MfaRecoveryCodesResponse(rawCodes);
    }

    // ---------------------------------------------------------------
    // Disable / regenerate — both require a fresh password AND a
    // currently-valid factor (TOTP or a recovery code).
    // ---------------------------------------------------------------

    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public void disable(Long customerId, String email, String currentPassword, String code) {
        requireFreshPassword(email, currentPassword);
        requireValidFactor(customerId, email, code);

        credentialRepository.deleteById(customerId);
        recoveryCodeRepository.deleteByCustomerId(customerId);

        auditService.recordSuccess("MFA_DISABLED", null, customerId, email, "Customer", String.valueOf(customerId), null, null);
        notificationService.notify(customerId, email, NotificationCategory.SECURITY, NotificationSeverity.WARNING,
            "Two-factor authentication disabled",
            "Two-factor authentication was just turned off for your account. If this wasn't you, contact your administrator immediately.");
    }

    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public MfaRecoveryCodesResponse regenerateRecoveryCodes(Long customerId, String email, String currentPassword, String code) {
        requireFreshPassword(email, currentPassword);
        requireValidFactor(customerId, email, code);

        List<String> rawCodes = regenerateRecoveryCodesFor(customerId);

        auditService.recordSuccess("MFA_RECOVERY_CODES_REGENERATED", null, customerId, email, "Customer", String.valueOf(customerId), null, null);
        notificationService.notify(customerId, email, NotificationCategory.SECURITY, NotificationSeverity.INFO,
            "Recovery codes regenerated",
            "Your two-factor recovery codes were just regenerated — your old codes no longer work. If this wasn't you, contact your administrator immediately.");

        return new MfaRecoveryCodesResponse(rawCodes);
    }

    // ---------------------------------------------------------------
    // C30 (2026-09-26): an administrator resets another user's MFA (lost
    // device and recovery codes). Authority is checked by the caller
    // (OrganizationSelfService for org admins, the MANAGE_REGISTRATIONS gate
    // for platform admins). Removes the authenticator, the recovery codes and
    // any sign-in parked on them; if the organization requires MFA, the user
    // sets up a new authenticator at their next sign-in (C29).
    // ---------------------------------------------------------------

    @Transactional
    public void resetByAdmin(Long targetCustomerId, String actorKeycloakSub, Long actorCustomerId, String actorEmail,
                             Long organizationId) {
        if (actorCustomerId != null && actorCustomerId.equals(targetCustomerId)) {
            throw new IllegalArgumentException(
                "You cannot reset your own two-factor authentication here. Use Security settings instead.");
        }
        if (credentialRepository.findById(targetCustomerId).isEmpty()) {
            throw new IllegalArgumentException("This user has not set up two-factor authentication.");
        }
        credentialRepository.deleteById(targetCustomerId);
        recoveryCodeRepository.deleteByCustomerId(targetCustomerId);
        challengeRepository.deleteByCustomerId(targetCustomerId);

        String targetEmail = emailOf(targetCustomerId);
        auditService.recordSuccess("MFA_RESET_BY_ADMIN", actorKeycloakSub, actorCustomerId, actorEmail,
            "Customer", String.valueOf(targetCustomerId), organizationId,
            "Two-factor authentication reset for " + targetEmail);
        notificationService.notify(targetCustomerId, targetEmail, NotificationCategory.SECURITY, NotificationSeverity.WARNING,
            "Two-factor authentication was reset",
            "An administrator reset two-factor authentication on your account. Set up your authenticator app again "
                + "the next time you sign in. If you did not ask for this, contact your administrator immediately.");
    }

    // ---------------------------------------------------------------
    // Login-time challenge — created by AuthController right after a real
    // Keycloak grant succeeds for a customer with Platform MFA enabled;
    // consumed by AuthController#verifyPlatformMfa.
    // ---------------------------------------------------------------

    public record LoginChallengeResult(Long customerId, String keycloakSub, String accessToken, String refreshToken,
                                       boolean impersonated) {
    }

    /** C29: a sign-in completed by enrolling; the recovery codes are shown once. */
    public record ChallengeEnrollmentResult(LoginChallengeResult login, List<String> recoveryCodes) {
    }

    @Transactional
    public String createLoginChallenge(Long customerId, String keycloakSub, String accessToken, String refreshToken) {
        return createLoginChallenge(customerId, keycloakSub, accessToken, refreshToken, MfaChallengeKind.VERIFY, false);
    }

    /** C29: also used for ENROLL challenges and for federated (impersonated) sign-ins. */
    @Transactional
    public String createLoginChallenge(Long customerId, String keycloakSub, String accessToken, String refreshToken,
                                       MfaChallengeKind kind, boolean impersonated) {
        String id = generateOpaqueId();
        MfaLoginChallenge challenge = new MfaLoginChallenge();
        challenge.setId(id);
        challenge.setCustomerId(customerId);
        challenge.setKeycloakSub(keycloakSub);
        challenge.setAccessToken(accessToken);
        challenge.setRefreshToken(refreshToken);
        challenge.setAttempts(0);
        challenge.setCreatedAt(Instant.now());
        challenge.setExpiresAt(Instant.now().plus(challengeTtl));
        challenge.setKind(kind);
        challenge.setImpersonated(impersonated);
        challengeRepository.save(challenge);
        return id;
    }

    /** Single-use: the challenge row is deleted the moment it's satisfied,
     * so it can never be replayed into a second session, and a challenge
     * that failed too many times is deleted outright (forcing a fresh
     * login) rather than left available for unlimited guessing. */
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public LoginChallengeResult verifyLoginChallenge(String challengeId, String code) {
        MfaLoginChallenge challenge = requireUsableChallenge(challengeId, MfaChallengeKind.VERIFY);

        Long customerId = challenge.getCustomerId();
        boolean valid = verifyFactor(customerId, code);
        if (!valid) {
            challenge.setAttempts(challenge.getAttempts() + 1);
            challengeRepository.save(challenge);
            auditService.recordFailure("MFA_VERIFICATION_FAILURE", challenge.getKeycloakSub(), "Invalid code submitted for login challenge");
            throw new InvalidCredentialsException("Invalid verification code");
        }

        challengeRepository.deleteById(challengeId);
        credentialRepository.findById(customerId).ifPresent(c -> {
            c.setLastVerifiedAt(Instant.now());
            credentialRepository.save(c);
        });
        auditService.recordSuccess("MFA_VERIFICATION_SUCCESS", challenge.getKeycloakSub(), customerId, null, "Customer", String.valueOf(customerId), null, null);

        return new LoginChallengeResult(customerId, challenge.getKeycloakSub(), challenge.getAccessToken(),
            challenge.getRefreshToken(), challenge.isImpersonated());
    }

    // ---------------------------------------------------------------
    // C29: set up an authenticator during sign-in. The user has just signed
    // in (password or federated), so no password is asked here; the held
    // tokens become a session only after a valid first code.
    // ---------------------------------------------------------------

    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public MfaEnrollResponse startChallengeEnrollment(String challengeId) {
        MfaLoginChallenge challenge = requireUsableChallenge(challengeId, MfaChallengeKind.ENROLL);
        return beginEnrollment(challenge.getCustomerId(), emailOf(challenge.getCustomerId()));
    }

    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public ChallengeEnrollmentResult completeChallengeEnrollment(String challengeId, String code) {
        MfaLoginChallenge challenge = requireUsableChallenge(challengeId, MfaChallengeKind.ENROLL);
        Long customerId = challenge.getCustomerId();
        MfaRecoveryCodesResponse codes;
        try {
            codes = verifyEnrollment(customerId, emailOf(customerId), code);
        } catch (InvalidCredentialsException e) {
            challenge.setAttempts(challenge.getAttempts() + 1);
            challengeRepository.save(challenge);
            throw e;
        }
        challengeRepository.deleteById(challengeId);
        auditService.recordSuccess("MFA_ENROLLED_DURING_SIGN_IN", challenge.getKeycloakSub(), customerId, null,
            "Customer", String.valueOf(customerId), null, null);
        return new ChallengeEnrollmentResult(
            new LoginChallengeResult(customerId, challenge.getKeycloakSub(), challenge.getAccessToken(),
                challenge.getRefreshToken(), challenge.isImpersonated()),
            codes.codes());
    }

    /** Found, of the expected kind, not expired and not out of attempts;
     * otherwise deleted (when stale) and refused with a "sign in again" message. */
    private MfaLoginChallenge requireUsableChallenge(String challengeId, MfaChallengeKind expectedKind) {
        MfaLoginChallenge challenge = challengeRepository.findById(challengeId)
            .filter(c -> c.getKind() == expectedKind)
            .orElseThrow(() -> new InvalidCredentialsException("This verification request is no longer valid. Please sign in again."));
        if (challenge.getExpiresAt().isBefore(Instant.now())) {
            challengeRepository.deleteById(challengeId);
            throw new InvalidCredentialsException("This verification request has expired. Please sign in again.");
        }
        if (challenge.getAttempts() >= maxChallengeAttempts) {
            challengeRepository.deleteById(challengeId);
            throw new InvalidCredentialsException("Too many attempts. Please sign in again.");
        }
        return challenge;
    }

    private String emailOf(Long customerId) {
        return customerRepository.findById(customerId).map(c -> c.getEmail()).orElse("account-" + customerId);
    }

    // ---------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------

    /** Tries the submitted value as a TOTP code first when it looks like one
     * (exactly 6 digits), otherwise as a recovery code — one field, two
     * acceptable shapes, matching the login challenge's own "TOTP or
     * recovery code" branch (see this class's own javadoc). */
    private boolean verifyFactor(Long customerId, String code) {
        if (SIX_DIGIT.matcher(code.trim()).matches()) {
            return credentialRepository.findById(customerId)
                .filter(CustomerMfaCredential::isEnabled)
                .map(c -> codeVerifier.isValidCode(cipher.decrypt(c.getEncryptedSecret()), code.trim()))
                .orElse(false);
        }
        return consumeRecoveryCode(customerId, code);
    }

    private void requireValidFactor(Long customerId, String email, String code) {
        if (!verifyFactor(customerId, code)) {
            auditService.recordFailure("MFA_VERIFICATION_FAILURE", email, "Invalid code submitted for a management action");
            throw new InvalidCredentialsException("Invalid verification code");
        }
    }

    private boolean consumeRecoveryCode(Long customerId, String rawCode) {
        String hash = hash(normalizeRecoveryCode(rawCode));
        Optional<MfaRecoveryCode> match = recoveryCodeRepository.findByCustomerIdAndCodeHashAndUsedAtIsNull(customerId, hash);
        if (match.isEmpty()) {
            return false;
        }
        MfaRecoveryCode recoveryCode = match.get();
        recoveryCode.setUsedAt(Instant.now());
        recoveryCodeRepository.save(recoveryCode);
        auditService.recordSuccess("MFA_RECOVERY_CODE_USED", null, customerId, null, "Customer", String.valueOf(customerId), null, null);
        return true;
    }

    private List<String> regenerateRecoveryCodesFor(Long customerId) {
        recoveryCodeRepository.deleteByCustomerId(customerId);
        String[] rawCodes = recoveryCodeGenerator.generateCodes(recoveryCodeCount);
        Instant now = Instant.now();
        for (String raw : rawCodes) {
            MfaRecoveryCode entity = new MfaRecoveryCode();
            entity.setCustomerId(customerId);
            entity.setCodeHash(hash(normalizeRecoveryCode(raw)));
            entity.setCreatedAt(now);
            recoveryCodeRepository.save(entity);
        }
        return List.of(rawCodes);
    }

    /** Re-authentication before any state change to MFA — reuses the exact
     * same Keycloak password grant every normal login already goes through
     * (see KeycloakPasswordGrantService), discarding the resulting tokens;
     * this call's only purpose is proving "you still know the current
     * password right now", not establishing a session. */
    private void requireFreshPassword(String email, String currentPassword) {
        currentPasswordVerifier.verify(email, currentPassword);
    }

    private static String normalizeRecoveryCode(String raw) {
        return raw.trim().replace("-", "").replace(" ", "").toUpperCase();
    }

    private static String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String generateOpaqueId() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
