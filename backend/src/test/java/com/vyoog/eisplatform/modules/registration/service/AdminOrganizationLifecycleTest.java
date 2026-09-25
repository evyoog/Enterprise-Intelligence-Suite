package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.repository.AuditLogRepository;
import com.vyoog.eisplatform.modules.auth.service.FakeKeycloakAdminClient;
import com.vyoog.eisplatform.modules.auth.service.KeycloakAdminClient;
import com.vyoog.eisplatform.modules.registration.dto.UpdateOrganizationRequest;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationLifecycleStatus;
import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** REQ-TEN-001 Organization lifecycle: BR-TEN-001..007, via the real service
 * and database, with Keycloak replaced by an in-memory fake. */
@SpringBootTest
@ActiveProfiles("test")
@Import(AdminOrganizationLifecycleTest.TestConfig.class)
class AdminOrganizationLifecycleTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        KeycloakAdminClient fakeKeycloakAdminClient() {
            return new FakeKeycloakAdminClient();
        }
    }

    private static final AdminRegistrationService.Actor PLATFORM_ADMIN =
        new AdminRegistrationService.Actor("platform-admin-sub", null, "admin@vyoog.example");

    @Autowired private AdminRegistrationService service;
    @Autowired private OrganizationMemberService memberService;
    @Autowired private OrganizationSelfService selfService;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private KeycloakAdminClient keycloakAdminClient;

    private FakeKeycloakAdminClient keycloak() {
        return (FakeKeycloakAdminClient) keycloakAdminClient;
    }

    @BeforeEach
    void resetFake() {
        keycloak().reset();
    }

    private Organization newOrganization(RegistrationStatus status) {
        Organization org = new Organization();
        org.setName("Lifecycle Org");
        org.setCode("LC-" + System.nanoTime());
        org.setBusinessEmail("biz@lifecycle.example");
        org.setCountry("India");
        org.setLicensedSeats(5);
        org.setStatus(status);
        return organizationRepository.save(org);
    }

    private Customer newMember(Organization org, String sub, OrgRole role) {
        Customer customer = new Customer();
        customer.setEmail(sub + "-" + System.nanoTime() + "@lifecycle.example");
        customer.setFirstName("Test");
        customer.setLastName("User");
        customer.setKeycloakSub(sub + "-" + System.nanoTime());
        customer = customerRepository.save(customer);
        memberService.addMember(org.getId(), customer.getId(), role);
        keycloak().setEnabled(customer.getKeycloakSub(), true);
        return customer;
    }

    private UpdateOrganizationRequest update(String name, boolean billingSame) {
        return new UpdateOrganizationRequest(name, "Private", "Software", " ", "new@lifecycle.example", "+91 1",
            "India", "TN", "Chennai", "1 Main St", "GST1", null, null, null,
            billingSame, "Billing St", "India", "TN", "Chennai");
    }

    @Test
    void suspendDisablesEveryActiveMemberAndEndsTheirSessionsAndActivateReEnablesThem() {
        Organization org = newOrganization(RegistrationStatus.COMPLETED);
        Customer admin = newMember(org, "admin", OrgRole.ORG_ADMIN);
        Customer member = newMember(org, "member", OrgRole.MEMBER);

        var suspended = service.suspendOrganization(org.getId(), "Unpaid invoice", PLATFORM_ADMIN);
        assertThat(suspended.organization().lifecycleStatus()).isEqualTo(OrganizationLifecycleStatus.SUSPENDED);
        assertThat(suspended.accountsUpdated()).isEqualTo(2);
        assertThat(suspended.accountsNotUpdated()).isEmpty();
        assertThat(keycloak().isEnabled(admin.getKeycloakSub())).isFalse();
        assertThat(keycloak().isEnabled(member.getKeycloakSub())).isFalse();
        assertThat(keycloak().logoutCalls).contains(admin.getKeycloakSub(), member.getKeycloakSub());

        var activated = service.activateOrganization(org.getId(), null, PLATFORM_ADMIN);
        assertThat(activated.organization().lifecycleStatus()).isEqualTo(OrganizationLifecycleStatus.ACTIVE);
        assertThat(keycloak().isEnabled(admin.getKeycloakSub())).isTrue();
        assertThat(keycloak().isEnabled(member.getKeycloakSub())).isTrue();

        assertThat(auditLogRepository.findAll()).anySatisfy(log -> {
            assertThat(log.getAction()).isEqualTo("ORGANIZATION_SUSPENDED");
            assertThat(log.getOrganizationId()).isEqualTo(org.getId());
            assertThat(log.getDetail()).contains("reason: Unpaid invoice");
        });
    }

    @Test
    void closeIsSoftAndCanBeReversed() {
        Organization org = newOrganization(RegistrationStatus.COMPLETED);
        Customer admin = newMember(org, "closeadmin", OrgRole.ORG_ADMIN);

        var closed = service.closeOrganization(org.getId(), null, PLATFORM_ADMIN);
        assertThat(closed.organization().lifecycleStatus()).isEqualTo(OrganizationLifecycleStatus.CLOSED);
        assertThat(organizationRepository.findById(org.getId())).isPresent();
        assertThat(customerRepository.findById(admin.getId())).isPresent();
        assertThat(keycloak().isEnabled(admin.getKeycloakSub())).isFalse();

        assertThat(service.activateOrganization(org.getId(), null, PLATFORM_ADMIN).organization().lifecycleStatus())
            .isEqualTo(OrganizationLifecycleStatus.ACTIVE);
        assertThat(keycloak().isEnabled(admin.getKeycloakSub())).isTrue();
    }

    @Test
    void aClosedOrganizationCannotBeSuspendedOrEdited() {
        Organization org = newOrganization(RegistrationStatus.COMPLETED);
        service.closeOrganization(org.getId(), null, PLATFORM_ADMIN);

        assertThatThrownBy(() -> service.suspendOrganization(org.getId(), null, PLATFORM_ADMIN))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.updateOrganization(org.getId(), update("X", true), PLATFORM_ADMIN))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aKeycloakFailureIsReportedAndTheActionCanBeRepeatedToRetry() {
        Organization org = newOrganization(RegistrationStatus.COMPLETED);
        Customer admin = newMember(org, "retry", OrgRole.ORG_ADMIN);
        keycloak().setEnabledFailsFor.add(admin.getKeycloakSub());

        var first = service.suspendOrganization(org.getId(), null, PLATFORM_ADMIN);
        assertThat(first.organization().lifecycleStatus()).isEqualTo(OrganizationLifecycleStatus.SUSPENDED);
        assertThat(first.accountsUpdated()).isZero();
        assertThat(first.accountsNotUpdated()).containsExactly(admin.getEmail());

        keycloak().setEnabledFailsFor.clear();
        var retry = service.suspendOrganization(org.getId(), null, PLATFORM_ADMIN);
        assertThat(retry.accountsUpdated()).isEqualTo(1);
        assertThat(keycloak().isEnabled(admin.getKeycloakSub())).isFalse();
    }

    @Test
    void anAdminWhoIsAMemberCannotSuspendOrCloseTheirOwnOrganization() {
        Organization org = newOrganization(RegistrationStatus.COMPLETED);
        Customer admin = newMember(org, "self", OrgRole.ORG_ADMIN);
        var self = new AdminRegistrationService.Actor(admin.getKeycloakSub(), admin.getId(), admin.getEmail());

        assertThatThrownBy(() -> service.suspendOrganization(org.getId(), null, self))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.closeOrganization(org.getId(), null, self))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(organizationRepository.findById(org.getId()).orElseThrow().getLifecycleStatus())
            .isEqualTo(OrganizationLifecycleStatus.ACTIVE);
    }

    @Test
    void activatingDoesNotEnableAnAdminStillAwaitingEmailVerification() {
        Organization org = newOrganization(RegistrationStatus.PENDING_EMAIL_VERIFICATION);
        Customer admin = newMember(org, "pending", OrgRole.ORG_ADMIN);
        keycloak().setEnabled(admin.getKeycloakSub(), false);

        service.suspendOrganization(org.getId(), null, PLATFORM_ADMIN);
        var activated = service.activateOrganization(org.getId(), null, PLATFORM_ADMIN);

        assertThat(activated.accountsUpdated()).isZero();
        assertThat(keycloak().isEnabled(admin.getKeycloakSub())).isFalse();
    }

    @Test
    void updateReplacesTheEditableDetailsAndDropsBillingWhenSameAsAddress() {
        Organization org = newOrganization(RegistrationStatus.COMPLETED);
        String code = org.getCode();

        var updated = service.updateOrganization(org.getId(), update("  Renamed Org ", true), PLATFORM_ADMIN);

        assertThat(updated.name()).isEqualTo("Renamed Org");
        assertThat(updated.code()).isEqualTo(code);
        assertThat(updated.businessEmail()).isEqualTo("new@lifecycle.example");
        assertThat(updated.website()).isNull();
        assertThat(updated.billingSameAsAddress()).isTrue();
        assertThat(updated.billingAddress()).isNull();
        assertThat(updated.licensedSeats()).isEqualTo(5);

        var separate = service.updateOrganization(org.getId(), update("Renamed Org", false), PLATFORM_ADMIN);
        assertThat(separate.billingAddress()).isEqualTo("Billing St");
        assertThat(auditLogRepository.findAll()).anySatisfy(log -> {
            assertThat(log.getAction()).isEqualTo("ORGANIZATION_UPDATED");
            assertThat(log.getOrganizationId()).isEqualTo(org.getId());
        });
    }

    @Test
    void unknownOrganizationIsNotFound() {
        assertThatThrownBy(() -> service.suspendOrganization(-1L, null, PLATFORM_ADMIN))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void membersOfASuspendedOrganizationAreRefusedSelfServiceUntilItIsActivated() {
        Organization org = newOrganization(RegistrationStatus.COMPLETED);
        Customer member = newMember(org, "selfservice", OrgRole.ORG_ADMIN);
        assertThat(selfService.getMyOrganization(member.getId()).id()).isEqualTo(org.getId());

        service.suspendOrganization(org.getId(), null, PLATFORM_ADMIN);
        assertThatThrownBy(() -> selfService.getMyOrganization(member.getId()))
            .isInstanceOf(ForbiddenException.class);

        service.activateOrganization(org.getId(), null, PLATFORM_ADMIN);
        assertThat(selfService.getMyOrganization(member.getId()).id()).isEqualTo(org.getId());
    }
}
