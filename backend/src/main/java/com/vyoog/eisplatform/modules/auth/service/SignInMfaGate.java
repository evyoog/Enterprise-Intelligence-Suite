package com.vyoog.eisplatform.modules.auth.service;

import com.vyoog.eisplatform.modules.auth.model.MfaChallengeKind;
import com.vyoog.eisplatform.modules.authorization.service.MfaPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * C29 (2026-09-26): the one place that decides whether a sign-in may become a
 * session now, for password, SAML and OIDC sign-in alike.
 *
 * <ul>
 *   <li>The member has a platform authenticator: VERIFY challenge (unchanged
 *   behaviour for password sign-in, now also for federated sign-in).</li>
 *   <li>Their organization requires MFA and they have none: ENROLL challenge,
 *   so they set one up during sign-in. On password sign-in a Keycloak OTP
 *   (the token's {@code amr} contains {@code otp}) still satisfies the policy;
 *   a federated sign-in always needs the platform step.</li>
 *   <li>Otherwise: no step, the caller finalizes the session.</li>
 * </ul>
 *
 * The held tokens stay server-side in the challenge row; the browser only
 * gets the opaque challenge id.
 */
@Service
@RequiredArgsConstructor
public class SignInMfaGate {

    private final PlatformMfaService platformMfaService;
    private final MfaPolicyService mfaPolicyService;

    public record PendingStep(MfaChallengeKind kind, String challengeId) {
    }

    public Optional<PendingStep> check(Long customerId, String keycloakSub, List<String> amr, boolean federated,
                                       String accessToken, String refreshToken) {
        if (customerId == null) {
            return Optional.empty();
        }
        if (platformMfaService.isEnabledFor(customerId)) {
            return Optional.of(new PendingStep(MfaChallengeKind.VERIFY, platformMfaService.createLoginChallenge(
                customerId, keycloakSub, accessToken, refreshToken, MfaChallengeKind.VERIFY, federated)));
        }
        boolean satisfied = federated
            ? !mfaPolicyService.isMfaRequiredFor(keycloakSub)
            : mfaPolicyService.isMfaSatisfied(keycloakSub, amr);
        if (satisfied) {
            return Optional.empty();
        }
        return Optional.of(new PendingStep(MfaChallengeKind.ENROLL, platformMfaService.createLoginChallenge(
            customerId, keycloakSub, accessToken, refreshToken, MfaChallengeKind.ENROLL, federated)));
    }
}
