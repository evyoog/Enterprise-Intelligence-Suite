package com.vyoog.eisplatform.modules.authorization.service;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.modules.authorization.dto.PrivilegedAccessRequestDto;
import com.vyoog.eisplatform.modules.authorization.model.PrivilegedAccessEventType;
import com.vyoog.eisplatform.modules.authorization.model.PrivilegedAccessRequest;
import com.vyoog.eisplatform.modules.authorization.model.RoleScope;
import com.vyoog.eisplatform.modules.authorization.repository.PrivilegedAccessRequestRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.service.OrganizationMemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Phase 6: the roadmap's own target flow, end to end — "Normal user requests
 * temporary admin access -> approval -> temporary role granted -> access
 * expires -> audit record retained" — plus the guards that keep it from
 * becoming a self-escalation loop (no requesting MANAGE_PRIVILEGED_ACCESS
 * itself, no self-approval, no cross-organization approval).
 */
@SpringBootTest
@ActiveProfiles("test")
class PrivilegedAccessServiceTest {

    @Autowired
    private PrivilegedAccessService privilegedAccessService;
    @Autowired
    private PrivilegedAccessRequestRepository requestRepository;
    @Autowired
    private OrganizationMemberService organizationMemberService;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private CustomerRepository customerRepository;

    private Organization newOrganization() {
        Organization organization = new Organization();
        organization.setName("Test Org");
        organization.setCode("PAM-" + System.nanoTime());
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
        customer.setKeycloakSub("sub-" + email);
        return customerRepository.save(customer);
    }

    @Test
    void fullLifecycle_requestApproveGrantThenExpire() {
        Organization org = newOrganization();
        var admin = organizationMemberService.addMember(org.getId(), newCustomer("pam-admin@test-org.example").getId(), OrgRole.ORG_ADMIN);
        Customer adminCustomer = customerRepository.findById(admin.getCustomerId()).orElseThrow();
        var member = organizationMemberService.addMember(org.getId(), newCustomer("pam-member@test-org.example").getId(), OrgRole.MEMBER);
        Customer memberCustomer = customerRepository.findById(member.getCustomerId()).orElseThrow();

        // Before any request: no grant.
        assertThat(privilegedAccessService.hasActiveOrganizationGrant(memberCustomer.getId(), org.getId(), "MANAGE_USERS")).isFalse();

        PrivilegedAccessRequestDto created = privilegedAccessService.request(
            memberCustomer.getKeycloakSub(), memberCustomer.getId(), "MANAGE_USERS", "Covering for the admin this week", 60);
        assertThat(created.status()).isEqualTo("PENDING");
        assertThat(created.scope()).isEqualTo(RoleScope.ORGANIZATION);
        assertThat(created.organizationId()).isEqualTo(org.getId());
        assertThat(created.auditTrail()).hasSize(1);
        assertThat(created.auditTrail().get(0).eventType()).isEqualTo(PrivilegedAccessEventType.REQUESTED);

        // Still not active while PENDING.
        assertThat(privilegedAccessService.hasActiveOrganizationGrant(memberCustomer.getId(), org.getId(), "MANAGE_USERS")).isFalse();

        PrivilegedAccessRequestDto approved = privilegedAccessService.approve(
            adminCustomer.getKeycloakSub(), created.id(), RoleScope.ORGANIZATION, org.getId(), "Approved for coverage");
        assertThat(approved.status()).isEqualTo("APPROVED");
        assertThat(approved.effectiveStatus()).isEqualTo("APPROVED");
        assertThat(approved.expiresAt()).isAfter(Instant.now());
        assertThat(approved.auditTrail()).extracting(a -> a.eventType())
            .containsExactly(PrivilegedAccessEventType.REQUESTED, PrivilegedAccessEventType.APPROVED);

        // Now genuinely active.
        assertThat(privilegedAccessService.hasActiveOrganizationGrant(memberCustomer.getId(), org.getId(), "MANAGE_USERS")).isTrue();

        // Simulate time passing past expiry (no scheduler exists — expiry is
        // computed at check time, so backdating expiresAt directly is the
        // correct way to exercise it, exactly as production time simply
        // advancing would).
        PrivilegedAccessRequest raw = requestRepository.findById(created.id()).orElseThrow();
        raw.setExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES));
        requestRepository.save(raw);

        assertThat(privilegedAccessService.hasActiveOrganizationGrant(memberCustomer.getId(), org.getId(), "MANAGE_USERS")).isFalse();
        PrivilegedAccessRequestDto expired = privilegedAccessService.listMyRequests(memberCustomer.getKeycloakSub()).get(0);
        assertThat(expired.status()).isEqualTo("APPROVED"); // stored status unchanged...
        assertThat(expired.effectiveStatus()).isEqualTo("EXPIRED"); // ...but reported as expired
    }

    @Test
    void cannotRequestTheApprovalPermissionItself() {
        Organization org = newOrganization();
        var member = organizationMemberService.addMember(org.getId(), newCustomer("noescalate@test-org.example").getId(), OrgRole.MEMBER);
        Customer customer = customerRepository.findById(member.getCustomerId()).orElseThrow();

        assertThatThrownBy(() ->
            privilegedAccessService.request(customer.getKeycloakSub(), customer.getId(), "MANAGE_PRIVILEGED_ACCESS", "let me approve my own stuff", 30)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requestingAnOrganizationPermissionWithNoOrganizationMembershipFails() {
        Customer lone = newCustomer("no-org@test-org.example");
        assertThatThrownBy(() ->
            privilegedAccessService.request(lone.getKeycloakSub(), lone.getId(), "MANAGE_USERS", "why not", 30)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void unrecognizedPermissionNameIsRejected() {
        Organization org = newOrganization();
        var member = organizationMemberService.addMember(org.getId(), newCustomer("badperm@test-org.example").getId(), OrgRole.MEMBER);
        Customer customer = customerRepository.findById(member.getCustomerId()).orElseThrow();

        assertThatThrownBy(() ->
            privilegedAccessService.request(customer.getKeycloakSub(), customer.getId(), "DELETE_EVERYTHING", "trust me", 30)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void durationOutsideAllowedRangeIsRejected() {
        Organization org = newOrganization();
        var member = organizationMemberService.addMember(org.getId(), newCustomer("badduration@test-org.example").getId(), OrgRole.MEMBER);
        Customer customer = customerRepository.findById(member.getCustomerId()).orElseThrow();

        assertThatThrownBy(() ->
            privilegedAccessService.request(customer.getKeycloakSub(), customer.getId(), "MANAGE_USERS", "too long", 10_000)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anApproverCannotApproveTheirOwnRequest() {
        Organization org = newOrganization();
        var admin = organizationMemberService.addMember(org.getId(), newCustomer("selfapprove@test-org.example").getId(), OrgRole.ORG_ADMIN);
        Customer adminCustomer = customerRepository.findById(admin.getCustomerId()).orElseThrow();

        PrivilegedAccessRequestDto created = privilegedAccessService.request(
            adminCustomer.getKeycloakSub(), adminCustomer.getId(), "MANAGE_USERS", "why not, I'm already admin", 30);

        assertThatThrownBy(() ->
            privilegedAccessService.approve(adminCustomer.getKeycloakSub(), created.id(), RoleScope.ORGANIZATION, org.getId(), null)
        ).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void anotherOrganizationsAdminCannotApproveOrReachThisRequest() {
        Organization orgA = newOrganization();
        Organization orgB = newOrganization();
        var memberA = organizationMemberService.addMember(orgA.getId(), newCustomer("crossorg-member@test-org.example").getId(), OrgRole.MEMBER);
        Customer memberACustomer = customerRepository.findById(memberA.getCustomerId()).orElseThrow();
        var adminB = organizationMemberService.addMember(orgB.getId(), newCustomer("crossorg-admin@test-org.example").getId(), OrgRole.ORG_ADMIN);
        Customer adminBCustomer = customerRepository.findById(adminB.getCustomerId()).orElseThrow();

        PrivilegedAccessRequestDto created = privilegedAccessService.request(
            memberACustomer.getKeycloakSub(), memberACustomer.getId(), "MANAGE_USERS", "org A business", 30);

        assertThatThrownBy(() ->
            privilegedAccessService.approve(adminBCustomer.getKeycloakSub(), created.id(), RoleScope.ORGANIZATION, orgB.getId(), null)
        ).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void rejectedRequestNeverGrantsAccessAndRevokedGrantStopsCounting() {
        Organization org = newOrganization();
        var admin = organizationMemberService.addMember(org.getId(), newCustomer("reject-admin@test-org.example").getId(), OrgRole.ORG_ADMIN);
        Customer adminCustomer = customerRepository.findById(admin.getCustomerId()).orElseThrow();
        var member = organizationMemberService.addMember(org.getId(), newCustomer("reject-member@test-org.example").getId(), OrgRole.MEMBER);
        Customer memberCustomer = customerRepository.findById(member.getCustomerId()).orElseThrow();

        PrivilegedAccessRequestDto rejected = privilegedAccessService.request(
            memberCustomer.getKeycloakSub(), memberCustomer.getId(), "MANAGE_USERS", "let me try", 30);
        privilegedAccessService.reject(adminCustomer.getKeycloakSub(), rejected.id(), RoleScope.ORGANIZATION, org.getId(), "not now");
        assertThat(privilegedAccessService.hasActiveOrganizationGrant(memberCustomer.getId(), org.getId(), "MANAGE_USERS")).isFalse();

        PrivilegedAccessRequestDto approved = privilegedAccessService.request(
            memberCustomer.getKeycloakSub(), memberCustomer.getId(), "MANAGE_USERS", "second try", 30);
        privilegedAccessService.approve(adminCustomer.getKeycloakSub(), approved.id(), RoleScope.ORGANIZATION, org.getId(), "ok");
        assertThat(privilegedAccessService.hasActiveOrganizationGrant(memberCustomer.getId(), org.getId(), "MANAGE_USERS")).isTrue();

        privilegedAccessService.revoke(adminCustomer.getKeycloakSub(), approved.id(), RoleScope.ORGANIZATION, org.getId(), "changed my mind");
        assertThat(privilegedAccessService.hasActiveOrganizationGrant(memberCustomer.getId(), org.getId(), "MANAGE_USERS")).isFalse();
    }

    @Test
    void requesterCanWithdrawTheirOwnPendingRequest() {
        Organization org = newOrganization();
        var member = organizationMemberService.addMember(org.getId(), newCustomer("withdraw@test-org.example").getId(), OrgRole.MEMBER);
        Customer customer = customerRepository.findById(member.getCustomerId()).orElseThrow();

        PrivilegedAccessRequestDto created = privilegedAccessService.request(
            customer.getKeycloakSub(), customer.getId(), "MANAGE_USERS", "changed my mind already", 30);
        PrivilegedAccessRequestDto revoked = privilegedAccessService.revokeOwn(customer.getKeycloakSub(), created.id(), "no longer needed");
        assertThat(revoked.status()).isEqualTo("REVOKED");
    }

    @Test
    void platformScopeGrantWorksTheSameWayAsOrganizationScope() {
        Customer requester = newCustomer("platform-requester@test-org.example");
        Customer approver = newCustomer("platform-approver@test-org.example");

        assertThat(privilegedAccessService.hasActivePlatformGrant(requester.getKeycloakSub(), "MANAGE_CATALOG")).isFalse();

        PrivilegedAccessRequestDto created = privilegedAccessService.request(
            requester.getKeycloakSub(), requester.getId(), "MANAGE_CATALOG", "need to fix a listing", 15);
        assertThat(created.scope()).isEqualTo(RoleScope.PLATFORM);
        assertThat(created.organizationId()).isNull();

        privilegedAccessService.approve(approver.getKeycloakSub(), created.id(), RoleScope.PLATFORM, null, "ok, temporarily");
        assertThat(privilegedAccessService.hasActivePlatformGrant(requester.getKeycloakSub(), "MANAGE_CATALOG")).isTrue();
    }

    // ------------------------------------------------------------------
    // Decision C24 (REQ-IAM-004): requestable-permissions list
    // ------------------------------------------------------------------

    @Test
    void requestablePermissionsNeverIncludeTheApprovalPermission() {
        Organization org = newOrganization();
        var member = organizationMemberService.addMember(org.getId(), newCustomer("req-list-guard@test-org.example").getId(), OrgRole.MEMBER);

        assertThat(privilegedAccessService.listRequestablePermissions(member.getCustomerId()))
            .extracting(p -> p.permissionName())
            .doesNotContain("MANAGE_PRIVILEGED_ACCESS")
            .contains("MANAGE_USERS", "MANAGE_CATALOG");
    }

    @Test
    void organizationScopePermissionsAreListedOnlyForOrganizationMembers() {
        Customer lone = newCustomer("req-list-lone@test-org.example");

        var list = privilegedAccessService.listRequestablePermissions(lone.getId());
        assertThat(list).isNotEmpty();
        assertThat(list).allMatch(p -> p.scope() == RoleScope.PLATFORM);
        assertThat(list).extracting(p -> p.permissionName()).doesNotContain("MANAGE_USERS");

        // A caller with no customer row at all (e.g. a platform admin) is treated the same way.
        assertThat(privilegedAccessService.listRequestablePermissions(null)).allMatch(p -> p.scope() == RoleScope.PLATFORM);
    }

    @Test
    void everyListedPermissionIsAcceptedByRequest() {
        Organization org = newOrganization();
        var member = organizationMemberService.addMember(org.getId(), newCustomer("req-list-consistent@test-org.example").getId(), OrgRole.MEMBER);
        Customer customer = customerRepository.findById(member.getCustomerId()).orElseThrow();

        var list = privilegedAccessService.listRequestablePermissions(customer.getId());
        assertThat(list).extracting(p -> p.scope()).contains(RoleScope.ORGANIZATION, RoleScope.PLATFORM);
        for (var permission : list) {
            PrivilegedAccessRequestDto created = privilegedAccessService.request(
                customer.getKeycloakSub(), customer.getId(), permission.permissionName(), "consistency check", 5);
            assertThat(created.scope()).isEqualTo(permission.scope());
        }
    }
}
