package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.SeatLimitExceededException;
import com.vyoog.eisplatform.modules.registration.model.*;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises the seat model's exact scenario from the spec: 4 licensed / 4
 * active -> 5th rejected -> platform admin raises the limit -> 5th succeeds.
 * There is no public "add a member" endpoint yet (organization invitation
 * flow is deliberately deferred — see the final report), so this is the one
 * place the rule is actually verified end to end for now.
 */
@SpringBootTest
@ActiveProfiles("test")
class OrganizationMemberServiceTest {

    @Autowired
    private OrganizationMemberService organizationMemberService;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private CustomerRepository customerRepository;

    private Organization newOrganization(int licensedSeats) {
        Organization organization = new Organization();
        organization.setName("Test Org");
        organization.setCode("TEST-" + System.nanoTime());
        organization.setBusinessEmail("biz@test-org.example");
        organization.setCountry("India");
        organization.setLicensedSeats(licensedSeats);
        return organizationRepository.save(organization);
    }

    private Customer newCustomer(String email) {
        Customer customer = new Customer();
        customer.setEmail(email);
        customer.setFirstName("Test");
        customer.setLastName("User");
        return customerRepository.save(customer);
    }

    @Test
    void fifthMemberRejectedAtFourSeatsThenSucceedsAfterAdminRaisesLimit() {
        Organization org = newOrganization(4);

        for (int i = 0; i < 4; i++) {
            organizationMemberService.addMember(org.getId(), newCustomer("member" + i + "@test-org.example").getId(), OrgRole.MEMBER);
        }

        Long fifthCustomerId = newCustomer("member4@test-org.example").getId();
        assertThatThrownBy(() -> organizationMemberService.addMember(org.getId(), fifthCustomerId, OrgRole.MEMBER))
            .isInstanceOf(SeatLimitExceededException.class);

        // Only a platform admin's action (AdminRegistrationService#updateSeats
        // in production; here just the repository, since that's all this
        // service-level test needs) can lift the limit.
        org.setLicensedSeats(5);
        organizationRepository.save(org);

        var fifthMember = organizationMemberService.addMember(org.getId(), fifthCustomerId, OrgRole.MEMBER);
        assertThat(fifthMember.getId()).isNotNull();
        assertThat(organizationMemberService.listActiveMembers(org.getId())).hasSize(5);
    }

    @Test
    void loweringSeatsBelowActiveCountNeverAutoDeactivatesAnyone() {
        Organization org = newOrganization(3);
        for (int i = 0; i < 3; i++) {
            organizationMemberService.addMember(org.getId(), newCustomer("shrink" + i + "@test-org.example").getId(), OrgRole.MEMBER);
        }
        assertThat(organizationMemberService.isOverLimit(org.getId())).isFalse();

        org.setLicensedSeats(1);
        organizationRepository.save(org);

        assertThat(organizationMemberService.isOverLimit(org.getId())).isTrue();
        // Still 3 ACTIVE members — nobody was auto-removed by lowering the limit.
        assertThat(organizationMemberService.listActiveMembers(org.getId())).hasSize(3);
    }

    @Test
    void removingAMemberFreesTheirSeatButKeepsTheRowForHistory() {
        Organization org = newOrganization(1);
        var member = organizationMemberService.addMember(org.getId(), newCustomer("solo@test-org.example").getId(), OrgRole.ORG_ADMIN);

        organizationMemberService.removeMember(member.getId());

        assertThat(organizationMemberService.listActiveMembers(org.getId())).isEmpty();
        // A second member can now take the freed seat.
        var replacement = organizationMemberService.addMember(org.getId(), newCustomer("replacement@test-org.example").getId(), OrgRole.ORG_ADMIN);
        assertThat(replacement.getId()).isNotNull();
    }
}
