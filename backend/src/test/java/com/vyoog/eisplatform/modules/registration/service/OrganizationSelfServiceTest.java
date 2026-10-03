package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.administration.dto.CreateFeatureFlagRequest;
import com.vyoog.eisplatform.modules.administration.dto.UpdateFeatureFlagRequest;
import com.vyoog.eisplatform.modules.administration.service.PlatformAdministrationService;
import com.vyoog.eisplatform.modules.registration.dto.GroupDto;
import com.vyoog.eisplatform.modules.registration.dto.MemberStatusAction;
import com.vyoog.eisplatform.modules.registration.dto.OrgMemberDto;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.*;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Phase 2: this is the test proving "which applications can this user
 * access?" is actually answerable now — before this phase, nothing anywhere
 * wrote an OrganizationProductAccess row, so listMyOrgProducts/listMemberProducts
 * would have reported every member as having no access to anything, forever.
 */
@SpringBootTest
@ActiveProfiles("test")
class OrganizationSelfServiceTest {

    @Autowired
    private OrganizationSelfService organizationSelfService;
    @Autowired
    private OrganizationMemberService organizationMemberService;
    @Autowired
    private PlatformAdministrationService platformAdministrationService;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductSubscriptionRepository subscriptionRepository;

    private Organization newOrganization() {
        Organization organization = new Organization();
        organization.setName("Test Org");
        organization.setCode("TEST-" + System.nanoTime());
        organization.setBusinessEmail("biz@test-org.example");
        organization.setCountry("India");
        organization.setLicensedSeats(10);
        return organizationRepository.save(organization);
    }

    private Customer newCustomer(String email) {
        Customer customer = new Customer();
        customer.setEmail(email);
        customer.setFirstName("Test");
        customer.setLastName("User");
        return customerRepository.save(customer);
    }

    private Product newProduct(String name) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(BigDecimal.TEN);
        return productRepository.save(product);
    }

    private void subscribeOrgTo(Organization org, Product product) {
        ProductSubscription subscription = new ProductSubscription();
        subscription.setProductId(product.getId());
        subscription.setOwnerType(RegistrationOwnerType.ORGANIZATION);
        subscription.setOwnerOrganizationId(org.getId());
        // REQ-SUB-003: seats follow the organization's licensed seats.
        subscription.setQuantity(Math.max(1, org.getLicensedSeats()));
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartedAt(Instant.now());
        subscriptionRepository.save(subscription);
    }

    @Test
    void orgAdminCanGrantAndRevokeATeammatesAccessToASubscribedProduct() {
        Organization org = newOrganization();
        Product product = newProduct("PMS");
        subscribeOrgTo(org, product);

        var admin = organizationMemberService.addMember(org.getId(), newCustomer("admin@test-org.example").getId(), OrgRole.ORG_ADMIN);
        var teammate = organizationMemberService.addMember(org.getId(), newCustomer("teammate@test-org.example").getId(), OrgRole.MEMBER);

        // Before assignment: the org has the subscription, but this specific
        // member has no access yet — the exact two-tier distinction this
        // phase exists to make real.
        var before = organizationSelfService.listMemberProducts(admin.getCustomerId(), teammate.getId());
        assertThat(before).hasSize(1);
        assertThat(before.get(0).myAccessAssigned()).isFalse();

        var granted = organizationSelfService.assignProductAccess(admin.getCustomerId(), teammate.getId(), product.getId(), "PMS_USER");
        assertThat(granted.myAccessAssigned()).isTrue();
        assertThat(granted.myProductRole()).isEqualTo("PMS_USER");

        var after = organizationSelfService.listMemberProducts(admin.getCustomerId(), teammate.getId());
        assertThat(after.get(0).myAccessAssigned()).isTrue();
        assertThat(after.get(0).myProductRole()).isEqualTo("PMS_USER");

        organizationSelfService.revokeProductAccess(admin.getCustomerId(), teammate.getId(), product.getId());

        var afterRevoke = organizationSelfService.listMemberProducts(admin.getCustomerId(), teammate.getId());
        assertThat(afterRevoke.get(0).myAccessAssigned()).isFalse();
    }

    @Test
    void cannotAssignAccessToAProductTheOrgHasNotSubscribedTo() {
        Organization org = newOrganization();
        Product unsubscribedProduct = newProduct("Requirements");

        var admin = organizationMemberService.addMember(org.getId(), newCustomer("admin2@test-org.example").getId(), OrgRole.ORG_ADMIN);
        var teammate = organizationMemberService.addMember(org.getId(), newCustomer("teammate2@test-org.example").getId(), OrgRole.MEMBER);

        assertThatThrownBy(() ->
            organizationSelfService.assignProductAccess(admin.getCustomerId(), teammate.getId(), unsubscribedProduct.getId(), "RMS_USER")
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aPlainMemberCannotAssignAccess() {
        Organization org = newOrganization();
        Product product = newProduct("Ticketing");
        subscribeOrgTo(org, product);

        var member = organizationMemberService.addMember(org.getId(), newCustomer("plain@test-org.example").getId(), OrgRole.MEMBER);
        var teammate = organizationMemberService.addMember(org.getId(), newCustomer("teammate3@test-org.example").getId(), OrgRole.MEMBER);

        assertThatThrownBy(() ->
            organizationSelfService.assignProductAccess(member.getCustomerId(), teammate.getId(), product.getId(), "TICKET_USER")
        ).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void orgAdminCannotReachAMemberOfADifferentOrganization() {
        Organization orgA = newOrganization();
        Organization orgB = newOrganization();
        Product product = newProduct("Shared Product Name");
        subscribeOrgTo(orgA, product);
        subscribeOrgTo(orgB, product);

        var adminA = organizationMemberService.addMember(orgA.getId(), newCustomer("adminA@test-org.example").getId(), OrgRole.ORG_ADMIN);
        var memberB = organizationMemberService.addMember(orgB.getId(), newCustomer("memberB@test-org.example").getId(), OrgRole.MEMBER);

        assertThatThrownBy(() ->
            organizationSelfService.assignProductAccess(adminA.getCustomerId(), memberB.getId(), product.getId(), "SOME_ROLE")
        ).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void aCancelledOrganizationLosesSelfServiceEvenForAnActiveMember() {
        Organization org = newOrganization();
        var admin = organizationMemberService.addMember(org.getId(), newCustomer("cancelledorg-admin@test-org.example").getId(), OrgRole.ORG_ADMIN);

        // Sanity check: works fine while the org is in its normal registered state.
        assertThat(organizationSelfService.getMyOrganization(admin.getCustomerId())).isNotNull();

        org.setStatus(RegistrationStatus.CANCELLED);
        organizationRepository.save(org);

        assertThatThrownBy(() -> organizationSelfService.getMyOrganization(admin.getCustomerId()))
            .isInstanceOf(ForbiddenException.class);
        // The membership row itself is untouched (still ACTIVE) — it's the
        // organization's own standing that blocks access, not the membership.
        assertThat(admin.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
    }

    @Test
    void cannotGrantAccessToAProductTheCatalogHasMarkedInactive() {
        Organization org = newOrganization();
        Product product = newProduct("Discontinued App");
        product.setStatus(com.vyoog.eisplatform.modules.product.model.ProductStatus.INACTIVE);
        productRepository.save(product);
        subscribeOrgTo(org, product);

        var admin = organizationMemberService.addMember(org.getId(), newCustomer("inactive-admin@test-org.example").getId(), OrgRole.ORG_ADMIN);
        var teammate = organizationMemberService.addMember(org.getId(), newCustomer("inactive-teammate@test-org.example").getId(), OrgRole.MEMBER);

        assertThatThrownBy(() ->
            organizationSelfService.assignProductAccess(admin.getCustomerId(), teammate.getId(), product.getId(), "SOME_ROLE")
        ).isInstanceOf(IllegalArgumentException.class);
    }

    // ------------------------------------------------------------------
    // Phase 3 (2026.3.3): changeMemberRole
    // ------------------------------------------------------------------

    @Test
    void orgAdminCanPromoteATeammateToOrgAdmin() {
        Organization org = newOrganization();
        var admin = organizationMemberService.addMember(org.getId(), newCustomer("promote-admin@test-org.example").getId(), OrgRole.ORG_ADMIN);
        var teammate = organizationMemberService.addMember(org.getId(), newCustomer("promote-teammate@test-org.example").getId(), OrgRole.MEMBER);

        var result = organizationSelfService.changeMemberRole(admin.getCustomerId(), teammate.getId(), OrgRole.ORG_ADMIN);

        assertThat(result.orgRole()).isEqualTo(OrgRole.ORG_ADMIN);
    }

    @Test
    void orgAdminCanDemoteAnotherAdminWhenAThirdAdminRemains() {
        Organization org = newOrganization();
        var admin1 = organizationMemberService.addMember(org.getId(), newCustomer("demote-admin1@test-org.example").getId(), OrgRole.ORG_ADMIN);
        var admin2 = organizationMemberService.addMember(org.getId(), newCustomer("demote-admin2@test-org.example").getId(), OrgRole.ORG_ADMIN);

        var result = organizationSelfService.changeMemberRole(admin1.getCustomerId(), admin2.getId(), OrgRole.MEMBER);

        assertThat(result.orgRole()).isEqualTo(OrgRole.MEMBER);
    }

    @Test
    void cannotDemoteTheOrganizationsLastAdmin() {
        Organization org = newOrganization();
        var onlyAdmin = organizationMemberService.addMember(org.getId(), newCustomer("only-admin@test-org.example").getId(), OrgRole.ORG_ADMIN);

        assertThatThrownBy(() -> organizationSelfService.changeMemberRole(onlyAdmin.getCustomerId(), onlyAdmin.getId(), OrgRole.MEMBER))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void regularMemberCannotChangeAnyonesRole() {
        Organization org = newOrganization();
        var admin = organizationMemberService.addMember(org.getId(), newCustomer("norights-admin@test-org.example").getId(), OrgRole.ORG_ADMIN);
        var member = organizationMemberService.addMember(org.getId(), newCustomer("norights-member@test-org.example").getId(), OrgRole.MEMBER);

        assertThatThrownBy(() -> organizationSelfService.changeMemberRole(member.getCustomerId(), admin.getId(), OrgRole.MEMBER))
            .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void cannotChangeARoleForAMemberOfAnotherOrganization() {
        Organization orgA = newOrganization();
        Organization orgB = newOrganization();
        var adminA = organizationMemberService.addMember(orgA.getId(), newCustomer("cross-admin-a@test-org.example").getId(), OrgRole.ORG_ADMIN);
        var memberB = organizationMemberService.addMember(orgB.getId(), newCustomer("cross-member-b@test-org.example").getId(), OrgRole.MEMBER);

        assertThatThrownBy(() -> organizationSelfService.changeMemberRole(adminA.getCustomerId(), memberB.getId(), OrgRole.ORG_ADMIN))
            .isInstanceOf(ForbiddenException.class);
    }

    // ------------------------------------------------------------------
    // 05.03.01 User Lifecycle, 05.03.02 Review access (sprint 2026.4.1)
    // ------------------------------------------------------------------

    @Test
    void orgAdminCanSuspendThenReactivateATeammate() {
        Organization org = newOrganization();
        var admin = organizationMemberService.addMember(org.getId(), newCustomer("suspend-admin@test-org.example").getId(), OrgRole.ORG_ADMIN);
        var teammate = organizationMemberService.addMember(org.getId(), newCustomer("suspend-teammate@test-org.example").getId(), OrgRole.MEMBER);

        var suspended = organizationSelfService.changeMemberStatus(admin.getCustomerId(), teammate.getId(), MemberStatusAction.SUSPEND);
        assertThat(suspended.status()).isEqualTo(MembershipStatus.SUSPENDED);

        var reactivated = organizationSelfService.changeMemberStatus(admin.getCustomerId(), teammate.getId(), MemberStatusAction.REACTIVATE);
        assertThat(reactivated.status()).isEqualTo(MembershipStatus.ACTIVE);
    }

    @Test
    void suspendedMemberLosesSelfServiceImmediately() {
        Organization org = newOrganization();
        var admin = organizationMemberService.addMember(org.getId(), newCustomer("lockout-admin@test-org.example").getId(), OrgRole.ORG_ADMIN);
        var teammate = organizationMemberService.addMember(org.getId(), newCustomer("lockout-teammate@test-org.example").getId(), OrgRole.MEMBER);

        organizationSelfService.changeMemberStatus(admin.getCustomerId(), teammate.getId(), MemberStatusAction.SUSPEND);

        assertThatThrownBy(() -> organizationSelfService.getMyOrganization(teammate.getCustomerId()))
            .isInstanceOf(com.vyoog.eisplatform.common.exception.ResourceNotFoundException.class);
    }

    @Test
    void removedMemberCannotBeReactivated() {
        Organization org = newOrganization();
        var admin = organizationMemberService.addMember(org.getId(), newCustomer("remove-admin@test-org.example").getId(), OrgRole.ORG_ADMIN);
        var teammate = organizationMemberService.addMember(org.getId(), newCustomer("remove-teammate@test-org.example").getId(), OrgRole.MEMBER);

        organizationSelfService.changeMemberStatus(admin.getCustomerId(), teammate.getId(), MemberStatusAction.REMOVE);

        assertThatThrownBy(() -> organizationSelfService.changeMemberStatus(admin.getCustomerId(), teammate.getId(), MemberStatusAction.REACTIVATE))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cannotSuspendOrRemoveTheOrganizationsLastAdmin() {
        Organization org = newOrganization();
        var onlyAdmin = organizationMemberService.addMember(org.getId(), newCustomer("only-admin-suspend@test-org.example").getId(), OrgRole.ORG_ADMIN);

        assertThatThrownBy(() -> organizationSelfService.changeMemberStatus(onlyAdmin.getCustomerId(), onlyAdmin.getId(), MemberStatusAction.SUSPEND))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> organizationSelfService.changeMemberStatus(onlyAdmin.getCustomerId(), onlyAdmin.getId(), MemberStatusAction.REMOVE))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reactivatingAMemberRespectsTheSeatLimit() {
        Organization org = newOrganization();
        org.setLicensedSeats(2);
        organizationRepository.save(org);
        var admin = organizationMemberService.addMember(org.getId(), newCustomer("seat-admin@test-org.example").getId(), OrgRole.ORG_ADMIN);
        var teammate = organizationMemberService.addMember(org.getId(), newCustomer("seat-teammate@test-org.example").getId(), OrgRole.MEMBER);
        organizationSelfService.changeMemberStatus(admin.getCustomerId(), teammate.getId(), MemberStatusAction.SUSPEND);

        // Seats are lowered after the suspension frees one — an org shrinking
        // its plan is allowed (see OrganizationMemberService#isOverLimit's own
        // javadoc), so now only the admin's own seat is left.
        org.setLicensedSeats(1);
        organizationRepository.save(org);

        assertThatThrownBy(() -> organizationSelfService.changeMemberStatus(admin.getCustomerId(), teammate.getId(), MemberStatusAction.REACTIVATE))
            .isInstanceOf(com.vyoog.eisplatform.common.exception.SeatLimitExceededException.class);
    }

    @Test
    void orgAdminCanRecordAnAccessReview() {
        Organization org = newOrganization();
        var admin = organizationMemberService.addMember(org.getId(), newCustomer("review-admin@test-org.example").getId(), OrgRole.ORG_ADMIN);
        var teammate = organizationMemberService.addMember(org.getId(), newCustomer("review-teammate@test-org.example").getId(), OrgRole.MEMBER);

        var result = organizationSelfService.reviewMemberAccess(admin.getCustomerId(), teammate.getId());

        assertThat(result.lastReviewedAt()).isNotNull();
    }

    // ------------------------------------------------------------------
    // 05.04.01 Groups (sprint 2026.4.1)
    // ------------------------------------------------------------------

    @Test
    void orgAdminCanCreateAGroupAndAddOrRemoveMembers() {
        Organization org = newOrganization();
        var admin = organizationMemberService.addMember(org.getId(), newCustomer("group-admin@test-org.example").getId(), OrgRole.ORG_ADMIN);
        var teammate = organizationMemberService.addMember(org.getId(), newCustomer("group-teammate@test-org.example").getId(), OrgRole.MEMBER);

        var group = organizationSelfService.createGroup(admin.getCustomerId(), "Engineering");
        assertThat(group.name()).isEqualTo("Engineering");
        assertThat(group.members()).isEmpty();

        var withMember = organizationSelfService.addGroupMember(admin.getCustomerId(), group.id(), teammate.getId());
        assertThat(withMember.members()).extracting(OrgMemberDto::organizationMemberId).containsExactly(teammate.getId());

        // Adding the same member twice does not duplicate the row.
        organizationSelfService.addGroupMember(admin.getCustomerId(), group.id(), teammate.getId());
        assertThat(organizationSelfService.listMyOrgGroups(admin.getCustomerId()))
            .filteredOn(g -> g.id().equals(group.id()))
            .flatExtracting(GroupDto::members)
            .hasSize(1);

        var withoutMember = organizationSelfService.removeGroupMember(admin.getCustomerId(), group.id(), teammate.getId());
        assertThat(withoutMember.members()).isEmpty();
    }

    @Test
    void cannotAddAMemberOfAnotherOrganizationToAGroup() {
        Organization orgA = newOrganization();
        Organization orgB = newOrganization();
        var adminA = organizationMemberService.addMember(orgA.getId(), newCustomer("group-cross-admin-a@test-org.example").getId(), OrgRole.ORG_ADMIN);
        var memberB = organizationMemberService.addMember(orgB.getId(), newCustomer("group-cross-member-b@test-org.example").getId(), OrgRole.MEMBER);
        var group = organizationSelfService.createGroup(adminA.getCustomerId(), "Cross-org test");

        assertThatThrownBy(() -> organizationSelfService.addGroupMember(adminA.getCustomerId(), group.id(), memberB.getId()))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deletingAGroupRemovesItFromTheListing() {
        Organization org = newOrganization();
        var admin = organizationMemberService.addMember(org.getId(), newCustomer("group-delete-admin@test-org.example").getId(), OrgRole.ORG_ADMIN);
        var group = organizationSelfService.createGroup(admin.getCustomerId(), "Temporary");

        organizationSelfService.deleteGroup(admin.getCustomerId(), group.id());

        assertThat(organizationSelfService.listMyOrgGroups(admin.getCustomerId())).isEmpty();
    }

    // ------------------------------------------------------------------
    // 15.01.01 Configure feature flags (sprint 2026.4.2): "groups_enabled"
    // ------------------------------------------------------------------

    @Test
    void groupsAreUnreachableWhileTheFeatureFlagIsDisabled() {
        Organization org = newOrganization();
        var admin = organizationMemberService.addMember(org.getId(), newCustomer("flag-admin@test-org.example").getId(), OrgRole.ORG_ADMIN);
        organizationSelfService.createGroup(admin.getCustomerId(), "Before disabling");

        platformAdministrationService.updateFeatureFlag("groups_enabled", new UpdateFeatureFlagRequest(false, null), "test-admin");
        try {
            assertThatThrownBy(() -> organizationSelfService.listMyOrgGroups(admin.getCustomerId()))
                .isInstanceOf(ForbiddenException.class);
            assertThatThrownBy(() -> organizationSelfService.createGroup(admin.getCustomerId(), "While disabled"))
                .isInstanceOf(ForbiddenException.class);
        } finally {
            platformAdministrationService.updateFeatureFlag("groups_enabled", new UpdateFeatureFlagRequest(true, null), "test-admin");
        }

        assertThat(organizationSelfService.listMyOrgGroups(admin.getCustomerId())).hasSize(1);
    }

    // ------------------------------------------------------------------
    // 05.02.01.05 Configure tenant policies (sprint 2026.4.2, carried from
    // 2026.4.1): allowSeatOverage
    // ------------------------------------------------------------------

    @Test
    void allowSeatOverageBypassesTheSeatLimit() {
        Organization org = newOrganization();
        org.setLicensedSeats(1);
        org.setAllowSeatOverage(true);
        organizationRepository.save(org);

        organizationMemberService.addMember(org.getId(), newCustomer("overage-admin@test-org.example").getId(), OrgRole.ORG_ADMIN);
        var teammate = organizationMemberService.addMember(org.getId(), newCustomer("overage-teammate@test-org.example").getId(), OrgRole.MEMBER);

        assertThat(teammate.getId()).isNotNull();
        assertThat(organizationMemberService.isOverLimit(org.getId())).isTrue();
    }

    @Test
    void seatOverageIsRefusedWhenTheOrganizationDoesNotAllowIt() {
        Organization org = newOrganization();
        org.setLicensedSeats(1);
        organizationRepository.save(org);

        organizationMemberService.addMember(org.getId(), newCustomer("no-overage-admin@test-org.example").getId(), OrgRole.ORG_ADMIN);

        assertThatThrownBy(() -> organizationMemberService.addMember(
            org.getId(), newCustomer("no-overage-teammate@test-org.example").getId(), OrgRole.MEMBER))
            .isInstanceOf(com.vyoog.eisplatform.common.exception.SeatLimitExceededException.class);
    }
}
