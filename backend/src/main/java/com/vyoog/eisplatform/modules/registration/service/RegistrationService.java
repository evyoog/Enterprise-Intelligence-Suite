package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.DuplicateResourceException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.auth.service.KeycloakAdminClient;
import com.vyoog.eisplatform.modules.registration.dto.*;
import com.vyoog.eisplatform.modules.registration.model.*;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/**
 * Orchestrates organization registration — the ONLY public registration path
 * (individual self-registration was removed; every Vyoog customer is now a
 * member of some organization, per explicit product direction). The one rule
 * that shapes almost everything below: a duplicate EMAIL must never produce
 * a response distinguishable from a fresh, successful registration — see
 * {@link #registerOrganization}'s own comments. An organization CODE
 * duplicate is treated as an ordinary, immediately-surfaced conflict instead
 * (a business code isn't a personal identifier the way an email is, so
 * there's nothing to protect there).
 */
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int DEFAULT_LICENSED_SEATS = 5;
    private static final String DEFAULT_COUNTRY = "India";

    private final CustomerRepository customerRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final OrganizationMemberService organizationMemberService;
    private final ProductSubscriptionRepository subscriptionRepository;
    private final EmailVerificationService verificationService;
    private final EmailService emailService;
    private final AuditService auditService;
    private final KeycloakAdminClient keycloakAdminClient;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // ---------------------------------------------------------------
    // Organization
    // ---------------------------------------------------------------

    /**
     * The real registration form collects only: organization name, GSTIN,
     * business email, phone, and the admin's password — everything else
     * below is either optional (unused unless a future, richer form sends
     * it) or defaulted here rather than being asked for:
     * <ul>
     *   <li>{@code code}: auto-generated from the name (see
     *   {@link #generateOrganizationCode}) if not supplied.</li>
     *   <li>{@code country}: defaults to "India" (GSTIN is an Indian tax
     *   identifier — a form that asks for one but not a country would be a
     *   strange mix) if not supplied.</li>
     *   <li>{@code licensedSeats}: defaults to {@value #DEFAULT_LICENSED_SEATS}
     *   — a Vyoog admin can adjust it afterward (see the admin seat-editing
     *   UI).</li>
     *   <li>{@code productIds}: defaults to none — the org starts with no
     *   subscriptions; there is currently no self-service "browse and
     *   subscribe" flow for organizations (only for individuals), so a
     *   Vyoog admin needs to add the org's first subscription manually
     *   today. Flagged as a real, known gap, not silently worked around.</li>
     *   <li>{@code firstAdmin.firstName}/{@code lastName}: this realm's own
     *   Keycloak User Profile configuration REQUIRES both to be non-blank —
     *   confirmed live (an account missing either fails login with "Account
     *   is not fully set up" even with a correct password). Since the form
     *   doesn't collect a personal name, the organization's own name is used
     *   as a stand-in (sanitized to characters Keycloak's own name validator
     *   accepts), not a fabricated person's name.</li>
     * </ul>
     *
     * <p>Immediately creates a REAL Keycloak user for the admin — disabled
     * (see {@link KeycloakAdminClient#createUser}) until email verification
     * completes (see {@link #activateOrganization}), with the exact password
     * submitted here (never persisted anywhere in the Vyoog database itself —
     * only Keycloak ever stores it, same rule as everywhere else in this
     * app). If Keycloak user creation fails, the whole registration is
     * rolled back (this method is {@code @Transactional}) rather than
     * leaving an organization/customer row with no way to ever log in.
     */
    @Transactional
    public RegistrationAcceptedResponse registerOrganization(OrganizationRegistrationRequest request) {
        String code = (request.code() == null || request.code().isBlank())
            ? generateOrganizationCode(request.name())
            : request.code();
        // Org code is checked (and rejected loudly) BEFORE anything
        // email-sensitive — a business code isn't personally identifying,
        // so there's no reason to hide this one behind a generic response.
        // Only relevant when a caller supplied an explicit code — a
        // generated one is already guaranteed unique.
        if (request.code() != null && !request.code().isBlank() && organizationRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("Organization code \"" + code + "\" is already in use.");
        }

        PasswordPolicy.validate(request.firstAdmin().password(), request.firstAdmin().confirmPassword());

        if (customerRepository.existsByEmailIgnoreCase(request.firstAdmin().email())) {
            emailService.sendDuplicateRegistrationNotice(request.firstAdmin().email(), request.name());
            return fakeAcceptedResponse();
        }

        String country = blankToNull(request.country()) == null ? DEFAULT_COUNTRY : request.country();
        int licensedSeats = request.licensedSeats() == null ? DEFAULT_LICENSED_SEATS : request.licensedSeats();
        String adminFirstName = blankToNull(request.firstAdmin().firstName()) == null
            ? sanitizePersonName(request.name()) : request.firstAdmin().firstName();
        String adminLastName = blankToNull(request.firstAdmin().lastName()) == null
            ? "Admin" : request.firstAdmin().lastName();
        List<Long> productIds = request.productIds() == null ? List.of() : request.productIds();

        Organization organization = new Organization();
        organization.setName(request.name());
        organization.setCode(code);
        organization.setType(request.type());
        organization.setIndustry(request.industry());
        organization.setWebsite(request.website());
        organization.setBusinessEmail(request.businessEmail());
        organization.setPhone(request.phone());
        organization.setCountry(country);
        organization.setState(request.state());
        organization.setCity(request.city());
        organization.setAddress(request.address());
        organization.setGstin(request.gstin());
        organization.setPan(request.pan());
        organization.setCompanyRegistrationNumber(request.companyRegistrationNumber());
        organization.setTaxVatNumber(request.taxVatNumber());
        organization.setBillingSameAsAddress(request.billingSameAsAddress());
        organization.setBillingAddress(request.billingSameAsAddress() ? request.address() : request.billingAddress());
        organization.setBillingCountry(request.billingSameAsAddress() ? country : request.billingCountry());
        organization.setBillingState(request.billingSameAsAddress() ? request.state() : request.billingState());
        organization.setBillingCity(request.billingSameAsAddress() ? request.city() : request.billingCity());
        // parentOrganizationId is deliberately never set from the request —
        // see Organization's own javadoc; public registration has no field
        // for it at all.
        organization.setLicensedSeats(licensedSeats);
        organization.setStatus(RegistrationStatus.PENDING_EMAIL_VERIFICATION);
        organization = organizationRepository.save(organization);

        // Disabled until email verification completes (see
        // #activateOrganization) — the ONLY thing that makes the account
        // actually usable is Keycloak's own enabled flag, not just our DB
        // status, so an unverified admin genuinely cannot log in yet.
        Optional<String> keycloakSub = keycloakAdminClient.createUser(
            request.firstAdmin().email(), adminFirstName, adminLastName, request.firstAdmin().password(), false);
        if (keycloakSub.isEmpty()) {
            throw new IllegalStateException("Could not create your account right now. Please try again shortly.");
        }

        Customer admin = new Customer();
        admin.setEmail(request.firstAdmin().email());
        admin.setFirstName(adminFirstName);
        admin.setLastName(adminLastName);
        admin.setMobile(request.firstAdmin().mobile());
        admin.setCountry(country);
        admin.setStatus(RegistrationStatus.PENDING_EMAIL_VERIFICATION);
        admin.setKeycloakSub(keycloakSub.get());
        admin = customerRepository.save(admin);

        // Created now, not deferred to email verification — this is the
        // FIRST member of a brand-new org, so assertSeatAvailable trivially
        // passes (0 active members < licensedSeats) as long as the org
        // registered with at least 1 seat. Doing it here (rather than
        // threading the admin's id through the verification token, which
        // only ever points at one entity) keeps the pairing simple: both
        // ids are already in hand at this exact point.
        organizationMemberService.addMember(organization.getId(), admin.getId(), OrgRole.ORG_ADMIN);

        // Selected products are recorded as PENDING_SUBSCRIPTION now (the org
        // has requested them, no payment has happened) — see
        // #activateOrganization for where they become ACTIVE. Usually empty
        // today (see this method's own javadoc on why).
        for (Long productId : productIds) {
            ProductSubscription subscription = new ProductSubscription();
            subscription.setProductId(productId);
            subscription.setOwnerType(RegistrationOwnerType.ORGANIZATION);
            subscription.setOwnerOrganizationId(organization.getId());
            subscription.setStatus(SubscriptionStatus.PENDING_SUBSCRIPTION);
            // REQ-SUB-003 (C63) default: seats start at the organization's licensed seats.
            subscription.setQuantity(Math.max(1, Math.min(SubscriptionSeatService.MAX_SEATS, organization.getLicensedSeats())));
            subscriptionRepository.save(subscription);
        }

        String rawToken = verificationService.issueForOrganization(organization.getId());
        emailService.sendVerificationEmail(admin.getEmail(), admin.getFirstName(), verificationLink(rawToken));

        return new RegistrationAcceptedResponse(
            String.valueOf(organization.getId()),
            "Check your email to verify your organization and finish setting up your account."
        );
    }

    /** A compact, human-legible, guaranteed-unique code — not shown to the
     * registrant during this simplified flow, but still used internally
     * (admin screens, uniqueness) the same way an explicitly-chosen one
     * would be. */
    private String generateOrganizationCode(String name) {
        String base = name == null ? "" : name.toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (base.isEmpty()) {
            base = "ORG";
        } else if (base.length() > 12) {
            base = base.substring(0, 12);
        }
        String code;
        do {
            byte[] suffix = new byte[3];
            RANDOM.nextBytes(suffix);
            code = base + "-" + HexFormat.of().formatHex(suffix).toUpperCase();
        } while (organizationRepository.existsByCodeIgnoreCase(code));
        return code;
    }

    /** Keeps only characters Keycloak's own "person name" User Profile
     * validator accepts, since this is used as a Keycloak firstName when no
     * real personal name was collected (see #registerOrganization). */
    private static String sanitizePersonName(String value) {
        String cleaned = value == null ? "" : value.replaceAll("[^\\p{L}\\p{N} '-]", "").trim();
        return cleaned.isEmpty() ? "Organization" : cleaned;
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    // ---------------------------------------------------------------
    // Verification / status
    // ---------------------------------------------------------------

    @Transactional
    public RegistrationStatusResponse verifyEmail(String rawToken) {
        EmailVerificationToken token = verificationService.consume(rawToken);

        Organization organization = organizationRepository.findById(token.getOrganizationId())
            .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));
        organization.setStatus(RegistrationStatus.EMAIL_VERIFIED);
        organizationRepository.save(organization);
        activateOrganization(organization);
        return new RegistrationStatusResponse(String.valueOf(organization.getId()), organization.getStatus());
    }

    /**
     * The seam a real payment integration slots into later: today this runs
     * synchronously right after email verification (no payment gateway
     * exists yet), but it's a separate method precisely so a future flow can
     * call it from a payment-success webhook instead, with nothing else
     * about registration needing to change. Membership for the first
     * ORG_ADMIN is already created at registration time (see
     * #registerOrganization) — this method only ever needs to move the
     * organization's and its subscriptions' own status forward, plus the
     * admin customer's own status (the individual identity underneath the
     * org membership has its own lifecycle too, mirrored here rather than
     * left stuck at PENDING_EMAIL_VERIFICATION forever).
     */
    private void activateOrganization(Organization organization) {
        organization.setStatus(RegistrationStatus.PENDING_SUBSCRIPTION);
        for (ProductSubscription subscription : subscriptionRepository.findByOwnerOrganizationId(organization.getId())) {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setStartedAt(Instant.now());
            subscriptionRepository.save(subscription);
            auditService.recordSuccess("SUBSCRIPTION_CREATED", null, null, null,
                "ProductSubscription", subscription.getProductId().toString(), organization.getId(),
                "Organization subscribed to product " + subscription.getProductId());
        }
        organization.setStatus(RegistrationStatus.COMPLETED);
        organizationRepository.save(organization);
        auditService.recordSuccess("ORGANIZATION_CREATED", null, null, organization.getBusinessEmail(),
            "Organization", organization.getId().toString(), organization.getId(),
            "Organization \"" + organization.getName() + "\" completed registration");

        organizationMemberRepository.findByOrganizationId(organization.getId()).stream()
            .filter(m -> m.getOrgRole() == OrgRole.ORG_ADMIN)
            .findFirst()
            .flatMap(member -> customerRepository.findById(member.getCustomerId()))
            .ifPresent(admin -> {
                admin.setStatus(RegistrationStatus.COMPLETED);
                customerRepository.save(admin);
                // The account was created DISABLED at registration time (see
                // #registerOrganization) — this is the one place it actually
                // becomes usable. Best-effort: HttpKeycloakAdminClient logs
                // its own failures, same convention as logoutAllSessions;
                // not throwing here avoids stranding an otherwise-complete
                // registration on a transient Keycloak hiccup, though it does
                // mean a failure here needs a manual retry (no automatic one
                // exists yet — a real, documented gap, not a silent one).
                if (admin.getKeycloakSub() != null) {
                    keycloakAdminClient.setEnabled(admin.getKeycloakSub(), true);
                }
            });
    }

    @Transactional(readOnly = true)
    public RegistrationStatusResponse getStatus(String registrationId) {
        // Same anti-enumeration posture as registration itself: a
        // fabricated/unknown id returns a plausible, generic
        // PENDING_EMAIL_VERIFICATION status rather than a 404 — a 404-vs-200
        // split here would itself become an oracle for guessing real ids.
        Long id = parseIdOrNull(registrationId);
        if (id != null) {
            var organization = organizationRepository.findById(id);
            if (organization.isPresent()) {
                return new RegistrationStatusResponse(registrationId, organization.get().getStatus());
            }
        }
        return new RegistrationStatusResponse(registrationId, RegistrationStatus.PENDING_EMAIL_VERIFICATION);
    }

    @Transactional
    public void resendVerification(String registrationId) {
        Long id = parseIdOrNull(registrationId);
        if (id == null) return; // Same non-committal posture as getStatus — no error either way.

        organizationRepository.findById(id).ifPresent(organization -> {
            if (organization.getStatus() != RegistrationStatus.PENDING_EMAIL_VERIFICATION) {
                return;
            }
            // Resend goes to the admin's own inbox, same as the original
            // verification email — not the org's business-contact address,
            // which can be a different mailbox entirely (e.g. a shared
            // accounts@ address nobody with the link would see).
            organizationMemberRepository.findByOrganizationId(organization.getId()).stream()
                .filter(m -> m.getOrgRole() == OrgRole.ORG_ADMIN)
                .findFirst()
                .flatMap(member -> customerRepository.findById(member.getCustomerId()))
                .ifPresent(admin -> {
                    String rawToken = verificationService.issueForOrganization(organization.getId());
                    emailService.sendVerificationEmail(admin.getEmail(), admin.getFirstName(), verificationLink(rawToken));
                });
        });
    }

    private String verificationLink(String rawToken) {
        return frontendUrl + "/register/verify?token=" + rawToken;
    }

    /** Must read identically to {@link #registerOrganization}'s own real
     * success message (the only registration path left) — a wording
     * mismatch between the real and fake response would itself be a subtle
     * enumeration signal. */
    private RegistrationAcceptedResponse fakeAcceptedResponse() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        String fakeId = "p_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return new RegistrationAcceptedResponse(fakeId, "Check your email to verify your organization and finish setting up your account.");
    }

    private Long parseIdOrNull(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
