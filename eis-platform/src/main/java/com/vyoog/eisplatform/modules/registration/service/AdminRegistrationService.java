package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.DuplicateResourceException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.registration.dto.CustomerAdminDto;
import com.vyoog.eisplatform.modules.registration.dto.OrganizationAdminDto;
import com.vyoog.eisplatform.modules.registration.dto.OrganizationDto;
import com.vyoog.eisplatform.modules.registration.dto.OrganizationSummaryDto;
import com.vyoog.eisplatform.modules.registration.dto.PendingProvisioningDto;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        return new OrganizationAdminDto(
            org.getId(), org.getName(), org.getCode(), org.getType(), org.getIndustry(), org.getWebsite(),
            org.getBusinessEmail(), org.getPhone(), org.getCountry(), org.getState(), org.getCity(), org.getAddress(),
            org.getGstin(), org.getPan(), org.getCompanyRegistrationNumber(), org.getTaxVatNumber(),
            org.isBillingSameAsAddress(), org.getBillingAddress(), org.getBillingCountry(), org.getBillingState(), org.getBillingCity(),
            org.getParentOrganizationId(), org.getLicensedSeats(), activeMembers, org.getStatus(),
            admin == null ? null : admin.getFirstName(),
            admin == null ? null : admin.getLastName(),
            admin == null ? null : admin.getEmail(),
            admin != null && admin.getKeycloakSub() != null,
            org.getCreatedAt()
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
}
