package com.vyoog.eisplatform.modules.authorization.service;

import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.service.OrganizationMemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 7: "optional organization-level MFA policy" + "administrative
 * enforcement" — verified entirely against real DB rows, no Keycloak call
 * needed, since the only Keycloak-sourced input this service reads (the
 * {@code amr} claim) is passed in directly by the caller.
 */
@SpringBootTest
@ActiveProfiles("test")
class MfaPolicyServiceTest {

    @Autowired
    private MfaPolicyService mfaPolicyService;
    @Autowired
    private OrganizationMemberService organizationMemberService;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private CustomerRepository customerRepository;

    private Organization newOrganization(boolean mfaRequired) {
        Organization organization = new Organization();
        organization.setName("Test Org");
        organization.setCode("MFA-" + System.nanoTime());
        organization.setBusinessEmail("biz@test-org.example");
        organization.setCountry("India");
        organization.setLicensedSeats(10);
        organization.setMfaRequired(mfaRequired);
        return organizationRepository.save(organization);
    }

    private Customer newCustomer(String email) {
        Customer customer = new Customer();
        customer.setEmail(email);
        customer.setFirstName("Test");
        customer.setLastName("User");
        customer.setKeycloakSub("sub-" + email);
        return customerRepository.save(customer);
    }

    @Test
    void tokenWithOtpInAmrAlwaysSatisfiesPolicyRegardlessOfOrg() {
        Organization org = newOrganization(true);
        var member = organizationMemberService.addMember(org.getId(), newCustomer("mfa-otp@test-org.example").getId(), OrgRole.MEMBER);
        Customer customer = customerRepository.findById(member.getCustomerId()).orElseThrow();

        assertThat(mfaPolicyService.isMfaSatisfied(customer.getKeycloakSub(), List.of("pwd", "otp"))).isTrue();
    }

    @Test
    void orgWithNoMfaPolicyIsSatisfiedEvenWithoutOtp() {
        Organization org = newOrganization(false);
        var member = organizationMemberService.addMember(org.getId(), newCustomer("mfa-nopolicy@test-org.example").getId(), OrgRole.MEMBER);
        Customer customer = customerRepository.findById(member.getCustomerId()).orElseThrow();

        assertThat(mfaPolicyService.isMfaSatisfied(customer.getKeycloakSub(), List.of("pwd"))).isTrue();
    }

    @Test
    void orgRequiringMfaBlocksALoginThatDidNotUseOtp() {
        Organization org = newOrganization(true);
        var member = organizationMemberService.addMember(org.getId(), newCustomer("mfa-blocked@test-org.example").getId(), OrgRole.MEMBER);
        Customer customer = customerRepository.findById(member.getCustomerId()).orElseThrow();

        assertThat(mfaPolicyService.isMfaSatisfied(customer.getKeycloakSub(), List.of("pwd"))).isFalse();
        assertThat(mfaPolicyService.isMfaSatisfied(customer.getKeycloakSub(), null)).isFalse();
    }

    @Test
    void aCustomerWithNoOrganizationMembershipHasNoPolicyToSatisfy() {
        Customer lone = newCustomer("mfa-lone@test-org.example");
        assertThat(mfaPolicyService.isMfaSatisfied(lone.getKeycloakSub(), List.of("pwd"))).isTrue();
    }

    @Test
    void anUnknownKeycloakSubHasNoPolicyToSatisfy() {
        assertThat(mfaPolicyService.isMfaSatisfied("no-such-sub", List.of("pwd"))).isTrue();
    }
}
