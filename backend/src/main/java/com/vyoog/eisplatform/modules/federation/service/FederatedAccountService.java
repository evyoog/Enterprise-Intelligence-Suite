package com.vyoog.eisplatform.modules.federation.service;

import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.auth.model.MfaChallengeKind;
import com.vyoog.eisplatform.modules.auth.service.ImpersonationExchangeService;
import com.vyoog.eisplatform.modules.auth.service.KeycloakAdminClient;
import com.vyoog.eisplatform.modules.auth.service.SessionCookieService;
import com.vyoog.eisplatform.modules.auth.service.SignInMfaGate;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.service.OrganizationMemberService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * What SAML and OIDC sign-in share once the identity provider's answer has
 * been validated (REQ-IAM-005/006, C27): link to or JIT-provision the Vyoog
 * account, keep organization membership MEMBER-only, and turn the sign-in into
 * a session through the same MFA gate as password sign-in (C29).
 *
 * <p>Callers pass their own exception factory so each protocol keeps its own
 * exception type (the SAML and OIDC controllers turn it into {@code ?ssoError=}).
 */
@Service
@RequiredArgsConstructor
public class FederatedAccountService {

    private final CustomerRepository customerRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final OrganizationMemberService organizationMemberService;
    private final KeycloakAdminClient keycloakAdminClient;
    private final ImpersonationExchangeService impersonationExchangeService;
    private final SessionCookieService sessionCookieService;
    private final SignInMfaGate signInMfaGate;
    private final AuditService auditService;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${vyoog.keycloak.ropc-client-id}")
    private String ownClientId;

    /**
     * No stable external identity is linked yet: link to an existing Customer
     * by email, or JIT-provision a real (never password-known) Keycloak user
     * and Customer row — never a duplicate.
     */
    public Customer linkOrProvision(Organization organization, String email, String firstName, String lastName,
                                    Function<String, RuntimeException> error) {
        Customer customer = customerRepository.findByEmailIgnoreCase(email).orElse(null);
        if (customer != null) {
            if (customer.getKeycloakSub() == null || customer.getKeycloakSub().isBlank()) {
                throw error.apply("An account already exists for this email but has not finished setup. "
                    + "Please complete that registration first, or contact your administrator.");
            }
            return customer;
        }
        // enabled=true immediately, unlike registration's disabled-until-
        // email-verified pattern — a validated answer from the organization's
        // own trusted identity provider IS the verification here.
        Optional<String> keycloakSub = keycloakAdminClient.createUser(email, firstName, lastName, generateRandomPassword(), true);
        if (keycloakSub.isEmpty()) {
            throw error.apply("Could not create your account right now. Please try again shortly.");
        }
        customer = new Customer();
        customer.setEmail(email);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setCountry(organization.getCountry());
        customer.setStatus(RegistrationStatus.COMPLETED);
        customer.setKeycloakSub(keycloakSub.get());
        return customerRepository.save(customer);
    }

    /**
     * Never grants {@link OrgRole#ORG_ADMIN}: a federated member always starts
     * as (or must already be) a plain MEMBER; no identity-provider claim decides
     * organization administrator access. A membership an org admin removed
     * (INACTIVE) is refused rather than silently reactivated.
     */
    public void ensureActiveMembership(Long organizationId, Long customerId, Function<String, RuntimeException> error) {
        Optional<OrganizationMember> existing =
            organizationMemberRepository.findByOrganizationIdAndCustomerId(organizationId, customerId);
        if (existing.isPresent()) {
            if (existing.get().getStatus() != MembershipStatus.ACTIVE) {
                throw error.apply("Your membership in this organization has been deactivated. Contact your administrator.");
            }
            return;
        }
        organizationMemberService.addMember(organizationId, customerId, OrgRole.MEMBER);
    }

    /**
     * Exchanges for this app's own tokens and either finalizes the session or,
     * under the organization MFA policy (C29), holds it and sends the browser
     * to {@code /?mfaEnroll=} or {@code /?mfaChallenge=}. Returns the redirect.
     */
    public String finishSignIn(Customer customer, Long organizationId, String email, String providerDescription,
                               String successAuditAction, HttpServletResponse response,
                               Function<String, RuntimeException> error) {
        var tokenResult = impersonationExchangeService.exchangeForUser(customer.getKeycloakSub(), ownClientId)
            .orElseThrow(() -> error.apply("Could not create a session for this account"));

        var step = signInMfaGate.check(customer.getId(), customer.getKeycloakSub(), List.of(), true,
            tokenResult.accessToken(), tokenResult.refreshToken());
        if (step.isPresent()) {
            boolean enroll = step.get().kind() == MfaChallengeKind.ENROLL;
            auditService.recordSuccess(successAuditAction.replace("_SUCCESS", "_MFA_PENDING"), customer.getKeycloakSub(),
                customer.getId(), email, "Customer", customer.getKeycloakSub(), organizationId,
                providerDescription + "; waiting for " + (enroll ? "authenticator set-up" : "authenticator code"));
            return frontendUrl + "/?" + (enroll ? "mfaEnroll" : "mfaChallenge") + "="
                + URLEncoder.encode(step.get().challengeId(), StandardCharsets.UTF_8);
        }

        sessionCookieService.finalizeBrowserSession(response, customer.getKeycloakSub(), tokenResult.refreshToken(), true);
        auditService.recordSuccess(successAuditAction, customer.getKeycloakSub(), customer.getId(), email,
            "Customer", customer.getKeycloakSub(), organizationId, providerDescription);
        return frontendUrl;
    }

    /** Never disclosed anywhere — this account only ever authenticates via SSO.
     * 32 random bytes, base64-encoded, satisfies the platform and realm
     * password policies. */
    private String generateRandomPassword() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return java.util.Base64.getEncoder().encodeToString(bytes) + "Aa1!";
    }
}
