package com.vyoog.eisplatform.modules.auth.service;

import com.vyoog.eisplatform.modules.auth.model.MfaChallengeKind;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.service.OrganizationMemberService;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** C29: which sign-ins become a session straight away, and which need an
 * authenticator code or authenticator set-up first. */
@SpringBootTest
@ActiveProfiles("test")
class SignInMfaGateTest {

    @Autowired private SignInMfaGate gate;
    @Autowired private PlatformMfaService platformMfaService;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private OrganizationMemberService memberService;

    private Customer member(boolean orgRequiresMfa) {
        Organization org = new Organization();
        org.setName("Gate Org");
        org.setCode("GATE-" + System.nanoTime());
        org.setBusinessEmail("biz@gate.example");
        org.setCountry("India");
        org.setLicensedSeats(5);
        org.setMfaRequired(orgRequiresMfa);
        org = organizationRepository.save(org);
        Customer c = new Customer();
        c.setEmail("gate-" + System.nanoTime() + "@example.com");
        c.setFirstName("Gate");
        c.setLastName("User");
        c.setKeycloakSub("gate-sub-" + System.nanoTime());
        c = customerRepository.save(c);
        memberService.addMember(org.getId(), c.getId(), OrgRole.MEMBER);
        return c;
    }

    @Test
    void noPolicyAndNoAuthenticatorMeansNoStep() {
        Customer c = member(false);
        assertThat(gate.check(c.getId(), c.getKeycloakSub(), List.of(), false, "at", "rt")).isEmpty();
        assertThat(gate.check(c.getId(), c.getKeycloakSub(), List.of(), true, "at", "rt")).isEmpty();
    }

    @Test
    void policyWithoutAuthenticatorAsksForSetUpOnPasswordAndFederatedSignIn() {
        Customer c = member(true);
        assertThat(gate.check(c.getId(), c.getKeycloakSub(), List.of(), false, "at", "rt")).get()
            .extracting(SignInMfaGate.PendingStep::kind).isEqualTo(MfaChallengeKind.ENROLL);
        assertThat(gate.check(c.getId(), c.getKeycloakSub(), List.of("pwd"), true, "at", "rt")).get()
            .extracting(SignInMfaGate.PendingStep::kind).isEqualTo(MfaChallengeKind.ENROLL);
    }

    @Test
    void aKeycloakOtpStillSatisfiesThePolicyOnPasswordSignInButNotOnFederatedSignIn() {
        Customer c = member(true);
        assertThat(gate.check(c.getId(), c.getKeycloakSub(), List.of("otp"), false, "at", "rt")).isEmpty();
        assertThat(gate.check(c.getId(), c.getKeycloakSub(), List.of("otp"), true, "at", "rt")).isPresent();
    }

    @Test
    void anEnrolledMemberAlwaysGetsTheCodeStep() throws Exception {
        Customer c = member(false);
        String enrollId = platformMfaService.createLoginChallenge(c.getId(), c.getKeycloakSub(), "at", "rt", MfaChallengeKind.ENROLL, false);
        String secret = platformMfaService.startChallengeEnrollment(enrollId).secret();
        platformMfaService.completeChallengeEnrollment(enrollId,
            new DefaultCodeGenerator().generate(secret, System.currentTimeMillis() / 1000 / 30));

        assertThat(gate.check(c.getId(), c.getKeycloakSub(), List.of(), false, "at", "rt")).get()
            .extracting(SignInMfaGate.PendingStep::kind).isEqualTo(MfaChallengeKind.VERIFY);
        assertThat(gate.check(c.getId(), c.getKeycloakSub(), List.of(), true, "at", "rt")).get()
            .extracting(SignInMfaGate.PendingStep::kind).isEqualTo(MfaChallengeKind.VERIFY);
    }

    @Test
    void aSignInWithNoCustomerAccountIsNeverHeld() {
        assertThat(gate.check(null, "platform-admin", List.of(), false, "at", "rt")).isEmpty();
    }
}
