package com.vyoog.eisplatform.modules.authorization.service;

import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Phase 7: "optional organization-level MFA policy" + "administrative
 * enforcement" — entirely reachable without any Keycloak Admin API, using
 * two things that already exist: {@code Organization.mfaRequired} (a plain
 * DB flag an ORG_ADMIN sets) and the {@code amr} (Authentication Methods
 * Reference) claim Keycloak already puts on every token that completed an
 * OTP challenge — a standard OIDC claim, not something this app has to ask
 * Keycloak for separately.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MfaPolicyService {

    private final CustomerRepository customerRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final OrganizationRepository organizationRepository;

    /**
     * True if either (a) this login's own token already shows {@code otp} in
     * its {@code amr} claim, or (b) the account's organization has no
     * MFA-required policy at all (including "not a member of any
     * organization" — a lone individual customer has no org policy to
     * satisfy). False only when the org actually requires it and this
     * particular login didn't use OTP.
     */
    public boolean isMfaSatisfied(String keycloakSub, List<String> amr) {
        if (amr != null && amr.contains("otp")) {
            return true;
        }
        return customerRepository.findByKeycloakSub(keycloakSub)
            .flatMap(customer -> organizationMemberRepository.findFirstByCustomerIdAndStatus(customer.getId(), MembershipStatus.ACTIVE))
            .flatMap(member -> organizationRepository.findById(member.getOrganizationId()))
            .map(organization -> !organization.isMfaRequired())
            .orElse(true);
    }
}
