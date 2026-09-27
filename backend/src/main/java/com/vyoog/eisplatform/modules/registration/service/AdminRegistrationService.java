package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.DuplicateResourceException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.administration.repository.PlatformRegionRepository;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.auth.service.KeycloakAdminClient;
import com.vyoog.eisplatform.modules.auth.service.PlatformMfaService;
import com.vyoog.eisplatform.modules.registration.dto.CustomerAdminDto;
import com.vyoog.eisplatform.modules.registration.dto.OrganizationAdminDto;
import com.vyoog.eisplatform.modules.registration.dto.OrganizationDto;
import com.vyoog.eisplatform.modules.registration.dto.OrganizationLifecycleResultDto;
import com.vyoog.eisplatform.modules.registration.dto.OrganizationSummaryDto;
import com.vyoog.eisplatform.modules.registration.dto.PendingProvisioningDto;
import com.vyoog.eisplatform.modules.registration.dto.UpdateOrganizationRequest;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationLifecycleStatus;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Platform-admin-only actions (see SecurityConfig — these endpoints require
 * the existing "ADMIN" Keycloak client role, reused as this phase's
 * PLATFORM_ADMIN rather than inventing a second global role). The
 * "provisioning" queue exists purely because automatic Keycloak user
 * creation is deliberately out of scope for this phase — see
 * {@link #linkKeycloakUser} for the exact seam a future automatic-provisioning
 * flow can replace this manual step with.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminRegistrationService {

    private final CustomerRepository customerRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final AuditService auditService;
    private final KeycloakAdminClient keycloakAdminClient;
    private final PlatformMfaService platformMfaService;
    private final PlatformRegionRepository regionRepository;

    public List<PendingProvisioningDto> listPendingProvisioning() {
        return customerRepository.findByKeycloakSubIsNullAndStatusNot(RegistrationStatus.PENDING_EMAIL_VERIFICATION)
            .stream()
            .map(customer -> {
                String organizationName = resolveOrganizationName(customer.getId());
                return new PendingProvisioningDto(
                    customer.getId(),
                    organizationName == null ? "INDIVIDUAL" : "ORG_ADMIN",
                    customer.getFirstName(),
                    customer.getLastName(),
                    customer.getEmail(),
                    organizationName
                );
            })
            .toList();
    }

    /** Every registered organization, full company details, newest first —
     * the general admin directory (distinct from {@link #listPendingProvisioning},
     * which only shows the ones still waiting on a Keycloak link). */
    public List<OrganizationAdminDto> listAllOrganizations() {
        return organizationRepository.findAll().stream()
            .map(this::toAdminDto)
            .sorted(Comparator.comparing(OrganizationAdminDto::createdAt, Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
    }

    private OrganizationAdminDto toAdminDto(Organization org) {
        List<OrganizationMember> members = memberRepository.findByOrganizationId(org.getId());
        long activeMembers = members.stream().filter(m -> m.getStatus() == MembershipStatus.ACTIVE).count();
        Customer admin = members.stream()
            .filter(m -> m.getOrgRole() == OrgRole.ORG_ADMIN)
            .findFirst()
            .flatMap(m -> customerRepository.findById(m.getCustomerId()))
            .orElse(null);

        String regionName = org.getRegionId() == null ? null
            : regionRepository.findById(org.getRegionId()).map(r -> r.getName()).orElse(null);

        return new OrganizationAdminDto(
            org.getId(), org.getName(), org.getCode(), org.getType(), org.getIndustry(), org.getWebsite(),
            org.getBusinessEmail(), org.getPhone(), org.getCountry(), org.getState(), org.getCity(), org.getAddress(),
            org.getGstin(), org.getPan(), org.getCompanyRegistrationNumber(), org.getTaxVatNumber(),
            org.isBillingSameAsAddress(), org.getBillingAddress(), org.getBillingCountry(), org.getBillingState(), org.getBillingCity(),
            org.getParentOrganizationId(), org.getLicensedSeats(), activeMembers, org.getStatus(), org.getLifecycleStatus(),
            admin == null ? null : admin.getFirstName(),
            admin == null ? null : admin.getLastName(),
            admin == null ? null : admin.getEmail(),
            admin != null && admin.getKeycloakSub() != null,
            org.getCreatedAt(),
            org.getRegionId(), regionName, org.isAllowSeatOverage()
        );
    }

    /** Every individual customer who has never joined an organization (an
     * org's own admin/members show up in {@link #listAllOrganizations} instead
     * — this avoids listing the same person twice under two different views). */
    public List<CustomerAdminDto> listAllIndividuals() {
        Set<Long> orgMemberCustomerIds = memberRepository.findAll().stream()
            .map(OrganizationMember::getCustomerId)
            .collect(Collectors.toSet());

        return customerRepository.findAll().stream()
            .filter(c -> !orgMemberCustomerIds.contains(c.getId()))
            .map(c -> new CustomerAdminDto(
                c.getId(), c.getEmail(), c.getFirstName(), c.getLastName(), c.getMobile(), c.getCountry(),
                c.getCompanyName(), c.getJobTitle(), c.getIndustry(), c.getStatus(),
                c.getKeycloakSub() != null, c.getCreatedAt()
            ))
            .sorted(Comparator.comparing(CustomerAdminDto::createdAt, Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
    }

    /** GET /register/organization/parents from the spec's API list — admin/
     * authorized-flows only, never reachable from public registration. */
    public List<OrganizationSummaryDto> listOrganizationsForParentSelection() {
        return organizationRepository.findAll().stream()
            .map(org -> new OrganizationSummaryDto(org.getId(), org.getName(), org.getCode()))
            .toList();
    }

    private String resolveOrganizationName(Long customerId) {
        return memberRepository.findFirstByCustomerIdAndStatus(customerId, MembershipStatus.ACTIVE)
            .filter(member -> member.getOrgRole() == OrgRole.ORG_ADMIN)
            .flatMap(member -> organizationRepository.findById(member.getOrganizationId()))
            .map(Organization::getName)
            .orElse(null);
    }

    /**
     * Called after a platform admin has manually created the matching
     * Keycloak user in the console. {@code keycloakSub} must be the
     * Keycloak user's immutable subject id — never their email (this is the
     * exact seam an automatic Keycloak-provisioning flow would replace: same
     * method, called by that flow with the sub it just got back from
     * Keycloak's Admin API instead of a platform admin typing it in).
     */
    @Transactional
    public void linkKeycloakUser(Long customerId, String keycloakSub) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));
        if (customerRepository.findByKeycloakSub(keycloakSub).isPresent()) {
            throw new DuplicateResourceException("That Keycloak user is already linked to a different Vyoog account.");
        }
        customer.setKeycloakSub(keycloakSub);
        customerRepository.save(customer);
    }

    @Transactional
    public OrganizationDto updateSeats(Long organizationId, int licensedSeats) {
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        int previousSeats = organization.getLicensedSeats();
        organization.setLicensedSeats(licensedSeats);
        organization = organizationRepository.save(organization);
        auditService.recordSuccess("SEAT_COUNT_CHANGED", null, null, null,
            "Organization", organizationId.toString(), organizationId,
            "Licensed seats changed from " + previousSeats + " to " + licensedSeats);
        long activeMembers = memberRepository.findByOrganizationId(organizationId).stream()
            .filter(m -> m.getStatus() == MembershipStatus.ACTIVE)
            .count();
        return new OrganizationDto(
            organization.getId(), organization.getName(), organization.getCode(), organization.getType(),
            organization.getIndustry(), organization.getWebsite(), organization.getBusinessEmail(),
            organization.getCountry(), organization.getLicensedSeats(), activeMembers, organization.getStatus(),
            organization.isMfaRequired()
        );
    }

    /** Who performed an admin action, for the audit log. {@code customerId}
     * is null for a platform admin with no customer row (the usual case). */
    public record Actor(String keycloakSub, Long customerId, String email) {
    }

    /**
     * REQ-TEN-001 / 05.01.01.02 Update organization. Replaces the editable
     * company details (see UpdateOrganizationRequest for what is excluded).
     * BR-TEN-004: a CLOSED organization must be reactivated first.
     */
    @Transactional
    public OrganizationAdminDto updateOrganization(Long organizationId, UpdateOrganizationRequest request, Actor actor) {
        Organization org = findOrganization(organizationId);
        if (org.getLifecycleStatus() == OrganizationLifecycleStatus.CLOSED) {
            throw new IllegalArgumentException("This organization is closed. Activate it before editing its details.");
        }
        org.setName(request.name().trim());
        org.setType(blankToNull(request.type()));
        org.setIndustry(blankToNull(request.industry()));
        org.setWebsite(blankToNull(request.website()));
        org.setBusinessEmail(request.businessEmail().trim());
        org.setPhone(blankToNull(request.phone()));
        org.setCountry(request.country().trim());
        org.setState(blankToNull(request.state()));
        org.setCity(blankToNull(request.city()));
        org.setAddress(blankToNull(request.address()));
        org.setGstin(blankToNull(request.gstin()));
        org.setPan(blankToNull(request.pan()));
        org.setCompanyRegistrationNumber(blankToNull(request.companyRegistrationNumber()));
        org.setTaxVatNumber(blankToNull(request.taxVatNumber()));
        org.setBillingSameAsAddress(request.billingSameAsAddress());
        // BR-TEN-003: "same as address" means no separate billing address is kept.
        org.setBillingAddress(request.billingSameAsAddress() ? null : blankToNull(request.billingAddress()));
        org.setBillingCountry(request.billingSameAsAddress() ? null : blankToNull(request.billingCountry()));
        org.setBillingState(request.billingSameAsAddress() ? null : blankToNull(request.billingState()));
        org.setBillingCity(request.billingSameAsAddress() ? null : blankToNull(request.billingCity()));
        // 05.02.01.03 Assign region, 05.02.01.05 Configure tenant policies
        // (sprint 2026.4.2, carried from 2026.4.1): platform-admin-only, like
        // every other field this method edits.
        if (request.regionId() != null && !regionRepository.existsById(request.regionId())) {
            throw new ResourceNotFoundException("Region not found: " + request.regionId());
        }
        org.setRegionId(request.regionId());
        org.setAllowSeatOverage(request.allowSeatOverage());
        org = organizationRepository.save(org);
        auditService.recordSuccess("ORGANIZATION_UPDATED", actor.keycloakSub(), actor.customerId(), actor.email(),
            "Organization", organizationId.toString(), organizationId, "Organization details updated");
        return toAdminDto(org);
    }

    /** REQ-TEN-001 / 05.01.01.03. BR-TEN-001: not from CLOSED; repeating it
     * on a SUSPENDED organization retries the Keycloak step (BR-TEN-005). */
    @Transactional
    public OrganizationLifecycleResultDto suspendOrganization(Long organizationId, String reason, Actor actor) {
        Organization org = findOrganization(organizationId);
        if (org.getLifecycleStatus() == OrganizationLifecycleStatus.CLOSED) {
            throw new IllegalArgumentException("A closed organization cannot be suspended. Activate it first.");
        }
        return changeLifecycle(org, OrganizationLifecycleStatus.SUSPENDED, "ORGANIZATION_SUSPENDED", reason, actor);
    }

    /** REQ-TEN-001 / 05.01.01.05. Soft close: nothing is deleted (BR-TEN-002). */
    @Transactional
    public OrganizationLifecycleResultDto closeOrganization(Long organizationId, String reason, Actor actor) {
        return changeLifecycle(findOrganization(organizationId), OrganizationLifecycleStatus.CLOSED,
            "ORGANIZATION_CLOSED", reason, actor);
    }

    /** REQ-TEN-001 / 05.01.01.04. From SUSPENDED or CLOSED; repeating it on
     * an ACTIVE organization retries the Keycloak step (BR-TEN-005). */
    @Transactional
    public OrganizationLifecycleResultDto activateOrganization(Long organizationId, String reason, Actor actor) {
        return changeLifecycle(findOrganization(organizationId), OrganizationLifecycleStatus.ACTIVE,
            "ORGANIZATION_ACTIVATED", reason, actor);
    }

    private OrganizationLifecycleResultDto changeLifecycle(Organization org, OrganizationLifecycleStatus target,
                                                           String auditAction, String reason, Actor actor) {
        boolean disabling = target != OrganizationLifecycleStatus.ACTIVE;
        List<Customer> activeMembers = memberRepository.findByOrganizationId(org.getId()).stream()
            .filter(m -> m.getStatus() == MembershipStatus.ACTIVE)
            .map(m -> customerRepository.findById(m.getCustomerId()).orElse(null))
            .filter(c -> c != null)
            .toList();

        // BR-TEN-006: an admin cannot lock themselves out through their own organization.
        if (disabling && actor.customerId() != null
            && activeMembers.stream().anyMatch(c -> c.getId().equals(actor.customerId()))) {
            throw new IllegalArgumentException("You are a member of this organization and cannot suspend or close it yourself.");
        }

        OrganizationLifecycleStatus previous = org.getLifecycleStatus();
        org.setLifecycleStatus(target);
        org = organizationRepository.save(org);

        // BR-TEN-007: a registration still awaiting email verification keeps
        // its admin login disabled; activating the organization does not skip that step.
        boolean keycloakStep = disabling || org.getStatus() != RegistrationStatus.PENDING_EMAIL_VERIFICATION;
        int updated = 0;
        List<String> notUpdated = new ArrayList<>();
        if (keycloakStep) {
            for (Customer member : activeMembers) {
                if (member.getKeycloakSub() == null) {
                    continue;
                }
                if (keycloakAdminClient.setEnabled(member.getKeycloakSub(), !disabling)) {
                    if (disabling) {
                        keycloakAdminClient.logoutAllSessions(member.getKeycloakSub());
                    }
                    updated++;
                } else {
                    notUpdated.add(member.getEmail());
                }
            }
        }

        String detail = "Lifecycle " + previous + " -> " + target
            + "; member logins " + (disabling ? "disabled" : "enabled") + ": " + updated
            + (notUpdated.isEmpty() ? "" : "; Keycloak failed for: " + String.join(", ", notUpdated))
            + (reason == null || reason.isBlank() ? "" : "; reason: " + reason.trim());
        auditService.recordSuccess(auditAction, actor.keycloakSub(), actor.customerId(), actor.email(),
            "Organization", org.getId().toString(), org.getId(), detail);
        return new OrganizationLifecycleResultDto(toAdminDto(org), updated, notUpdated);
    }

    /** C30: a platform admin resets anyone's two-factor authentication, found
     * by email. See PlatformMfaService#resetByAdmin for what is removed. */
    @Transactional
    public void resetMfaByEmail(String email, Actor actor) {
        Customer target = customerRepository.findByEmailIgnoreCase(email.trim())
            .orElseThrow(() -> new ResourceNotFoundException("No account uses that email address."));
        Long organizationId = memberRepository.findFirstByCustomerIdAndStatus(target.getId(), MembershipStatus.ACTIVE)
            .map(OrganizationMember::getOrganizationId).orElse(null);
        platformMfaService.resetByAdmin(target.getId(), actor.keycloakSub(), actor.customerId(), actor.email(), organizationId);
    }

    private Organization findOrganization(Long organizationId) {
        return organizationRepository.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
