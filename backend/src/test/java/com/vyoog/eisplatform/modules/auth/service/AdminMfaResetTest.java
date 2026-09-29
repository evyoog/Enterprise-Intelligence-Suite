package com.vyoog.eisplatform.modules.auth.service;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.repository.AuditLogRepository;
import com.vyoog.eisplatform.modules.auth.model.MfaChallengeKind;
import com.vyoog.eisplatform.modules.auth.repository.MfaLoginChallengeRepository;
import com.vyoog.eisplatform.modules.auth.repository.MfaRecoveryCodeRepository;
import com.vyoog.eisplatform.modules.notification.repository.NotificationRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.service.AdminRegistrationService;
import com.vyoog.eisplatform.modules.registration.service.OrganizationMemberService;
import com.vyoog.eisplatform.modules.registration.service.OrganizationSelfService;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** C30 (2026-09-26): organization admins reset their own members' MFA,
 * platform admins reset anyone's; audited, notified, never your own. */
@SpringBootTest
@ActiveProfiles("test")
class AdminMfaResetTest {

    @Autowired private PlatformMfaService platformMfaService;
    @Autowired private OrganizationSelfService organizationSelfService;
    @Autowired private AdminRegistrationService adminRegistrationService;
    @Autowired private OrganizationMemberService memberService;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private MfaRecoveryCodeRepository recoveryCodeRepository;
    @Autowired private MfaLoginChallengeRepository challengeRepository;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private NotificationRepository notificationRepository;

    private Organization org() {
        Organization org = new Organization();
        org.setName("Reset Org");
        org.setCode("RST-" + System.nanoTime());
        org.setBusinessEmail("biz@reset.example");
        org.setCountry("India");
        org.setLicensedSeats(5);
        return organizationRepository.save(org);
    }

    private Customer customer(String name) {
        Customer c = new Customer();
        c.setEmail(name + "-" + System.nanoTime() + "@reset.example");
        c.setFirstName("Reset");
        c.setLastName("User");
        c.setKeycloakSub("sub-" + name + "-" + System.nanoTime());
        return customerRepository.save(c);
    }

    private void enroll(Customer c) throws Exception {
        String id = platformMfaService.createLoginChallenge(c.getId(), c.getKeycloakSub(), "at", "rt", MfaChallengeKind.ENROLL, false);
        String secret = platformMfaService.startChallengeEnrollment(id).secret();
        platformMfaService.completeChallengeEnrollment(id, new DefaultCodeGenerator().generate(secret, System.currentTimeMillis() / 1000 / 30));
    }

    @Test
    void anOrganizationAdminResetsAMembersMfaWhichIsAuditedAndNotified() throws Exception {
        Organization org = org();
        Customer admin = customer("admin");
        Customer member = customer("member");
        memberService.addMember(org.getId(), admin.getId(), OrgRole.ORG_ADMIN);
        OrganizationMember m = memberService.addMember(org.getId(), member.getId(), OrgRole.MEMBER);
        enroll(member);
        platformMfaService.createLoginChallenge(member.getId(), member.getKeycloakSub(), "at", "rt");

        organizationSelfService.resetMemberMfa(admin.getId(), m.getId());

        assertThat(platformMfaService.isEnabledFor(member.getId())).isFalse();
        assertThat(recoveryCodeRepository.countByCustomerIdAndUsedAtIsNull(member.getId())).isZero();
        assertThat(challengeRepository.findAll()).noneMatch(c -> c.getCustomerId().equals(member.getId()));
        assertThat(auditLogRepository.findAll()).anySatisfy(log -> {
            assertThat(log.getAction()).isEqualTo("MFA_RESET_BY_ADMIN");
            assertThat(log.getActorCustomerId()).isEqualTo(admin.getId());
            assertThat(log.getOrganizationId()).isEqualTo(org.getId());
        });
        assertThat(notificationRepository.findAll()).anySatisfy(n -> {
            assertThat(n.getCustomerId()).isEqualTo(member.getId());
            assertThat(n.getTitle()).isEqualTo("Two-factor authentication was reset");
        });
    }

    @Test
    void anOrganizationAdminCannotResetAnotherOrganizationsMemberOrWithoutManageUsers() throws Exception {
        Organization org = org();
        Organization other = org();
        Customer admin = customer("admin2");
        Customer plain = customer("plain");
        Customer outsider = customer("outsider");
        memberService.addMember(org.getId(), admin.getId(), OrgRole.ORG_ADMIN);
        OrganizationMember plainMember = memberService.addMember(org.getId(), plain.getId(), OrgRole.MEMBER);
        OrganizationMember outsiderMember = memberService.addMember(other.getId(), outsider.getId(), OrgRole.MEMBER);
        enroll(outsider);
        enroll(plain);

        assertThatThrownBy(() -> organizationSelfService.resetMemberMfa(admin.getId(), outsiderMember.getId()))
            .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> organizationSelfService.resetMemberMfa(plain.getId(), plainMember.getId()))
            .isInstanceOf(ForbiddenException.class);
        assertThat(platformMfaService.isEnabledFor(outsider.getId())).isTrue();
    }

    @Test
    void nobodyResetsTheirOwnMfaHereAndThereMustBeSomethingToReset() throws Exception {
        Organization org = org();
        Customer admin = customer("self");
        Customer fresh = customer("fresh");
        OrganizationMember self = memberService.addMember(org.getId(), admin.getId(), OrgRole.ORG_ADMIN);
        OrganizationMember freshMember = memberService.addMember(org.getId(), fresh.getId(), OrgRole.MEMBER);
        enroll(admin);

        assertThatThrownBy(() -> organizationSelfService.resetMemberMfa(admin.getId(), self.getId()))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("your own");
        assertThatThrownBy(() -> organizationSelfService.resetMemberMfa(admin.getId(), freshMember.getId()))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("has not set up");
        assertThat(platformMfaService.isEnabledFor(admin.getId())).isTrue();
    }

    @Test
    void aPlatformAdminResetsAnyAccountByEmail() throws Exception {
        Customer individual = customer("individual");
        enroll(individual);
        var platformAdmin = new AdminRegistrationService.Actor("platform-admin-sub", null, "admin@vyoog.example");

        adminRegistrationService.resetMfaByEmail(individual.getEmail().toUpperCase(), platformAdmin);
        assertThat(platformMfaService.isEnabledFor(individual.getId())).isFalse();

        assertThatThrownBy(() -> adminRegistrationService.resetMfaByEmail("nobody@reset.example", platformAdmin))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
