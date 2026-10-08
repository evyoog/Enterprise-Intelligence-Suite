package com.vyoog.eisplatform.modules.invitation.service;

import com.vyoog.eisplatform.common.exception.DuplicateResourceException;
import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.InvalidStateException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.common.exception.SeatLimitExceededException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.auth.service.FakeEmailService;
import com.vyoog.eisplatform.modules.auth.service.FakeKeycloakAdminClient;
import com.vyoog.eisplatform.modules.auth.service.KeycloakAdminClient;
import com.vyoog.eisplatform.modules.authorization.service.MemberAccessService;
import com.vyoog.eisplatform.modules.invitation.dto.InvitationDtos.*;
import com.vyoog.eisplatform.modules.invitation.model.InvitationStatus;
import com.vyoog.eisplatform.modules.invitation.model.OrganizationInvitation;
import com.vyoog.eisplatform.modules.invitation.repository.InvitationRepository;
import com.vyoog.eisplatform.modules.orghierarchy.dto.OrgHierarchyDtos;
import com.vyoog.eisplatform.modules.orghierarchy.service.OrgHierarchyService;
import com.vyoog.eisplatform.modules.registration.model.*;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.service.EmailService;
import com.vyoog.eisplatform.modules.registration.service.OrganizationMemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** REQ-TEN-008 Invite user (TC-TEN-045 to TC-TEN-056). */
@SpringBootTest
@ActiveProfiles("test")
@Import(InvitationServiceTest.TestConfig.class)
class InvitationServiceTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        KeycloakAdminClient fakeKeycloakAdminClient() {
            return new FakeKeycloakAdminClient();
        }

        @Bean
        @Primary
        EmailService fakeEmailService() {
            return new FakeEmailService();
        }
    }

    @Autowired private InvitationService service;
    @Autowired private InvitationRepository invitationRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMemberRepository memberRepository;
    @Autowired private OrganizationMemberService memberService;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private MemberAccessService memberAccessService;
    @Autowired private OrgHierarchyService hierarchyService;
    @Autowired private AuditService auditService;
    @Autowired private KeycloakAdminClient keycloak;
    @Autowired private EmailService email;

    private FakeEmailService mail() { return (FakeEmailService) email; }
    private FakeKeycloakAdminClient kc() { return (FakeKeycloakAdminClient) keycloak; }

    private Organization org(int seats) {
        Organization o = new Organization();
        o.setName("Inv Org");
        o.setCode("INV-" + System.nanoTime());
        o.setBusinessEmail("biz@inv.example");
        o.setCountry("India");
        o.setLicensedSeats(seats);
        return organizationRepository.save(o);
    }

    private Customer customer(String prefix) {
        Customer c = new Customer();
        c.setEmail(prefix + System.nanoTime() + "@inv.example");
        c.setFirstName("Pat");
        c.setLastName("Lee");
        c.setStatus(RegistrationStatus.COMPLETED);
        return customerRepository.save(c);
    }

    private OrganizationMember member(Organization o, OrgRole role) {
        return memberService.addMember(o.getId(), customer("m").getId(), role);
    }

    private String token() {
        String link = mail().lastInvitationLink;
        return link.substring(link.lastIndexOf('/') + 1);
    }

    private CreateInvitationRequest req(String email, String role) {
        return new CreateInvitationRequest(email, role, null);
    }

    @Test
    void anAdministratorInvitesAndAPlainMemberCannot() {
        Organization o = org(10);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        OrganizationMember plain = member(o, OrgRole.MEMBER);

        CreateResult r = service.create(admin.getCustomerId(), req("  New.Person@Inv.Example ", "member"));
        assertThat(r.invitation().status()).isEqualTo("PENDING");
        assertThat(r.invitation().email()).isEqualTo("new.person@inv.example");
        assertThat(r.emailSent()).isTrue();
        assertThat(mail().lastInvitationEmail).isEqualTo("new.person@inv.example");
        assertThat(r.invitation().expiresAt()).isAfter(Instant.now().plus(6, ChronoUnit.DAYS));

        assertThatThrownBy(() -> service.create(plain.getCustomerId(), req("x@inv.example", "MEMBER")))
            .isInstanceOf(ForbiddenException.class).hasMessage("You do not have permission to invite users.");
        assertThatThrownBy(() -> service.create(admin.getCustomerId(), req("not-an-email", "MEMBER")))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.create(admin.getCustomerId(), req("x@inv.example", "BOSS")))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theTokenIsStoredOnlyAsAHashAndNeverReturned() {
        Organization o = org(10);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        service.create(admin.getCustomerId(), req("hash@inv.example", "MEMBER"));
        String raw = token();
        OrganizationInvitation stored = invitationRepository.findByOrganizationIdAndNormalizedEmailAndStatus(
            o.getId(), "hash@inv.example", InvitationStatus.PENDING).orElseThrow();
        assertThat(stored.getTokenHash()).isNotEqualTo(raw).hasSize(64).isEqualTo(InvitationService.hash(raw));
        assertThat(raw.length()).isGreaterThanOrEqualTo(40);
    }

    @Test
    void delegationGivesInviteOnlyAsMemberAndSeesOnlyOwnInvitationsAndRemovingItStopsNewOnes() {
        Organization o = org(10);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        OrganizationMember john = member(o, OrgRole.MEMBER);

        assertThatThrownBy(() -> service.create(john.getCustomerId(), req("a@inv.example", "MEMBER"))).isInstanceOf(ForbiddenException.class);
        service.setInvitePermission(admin.getCustomerId(), john.getId(), true);
        assertThat(service.inviters(admin.getCustomerId()).memberIds()).containsExactly(john.getId());
        assertThat(memberAccessService.hasPermission(john, "MANAGE_USERS")).isFalse();
        assertThat(memberAccessService.listPermissions(john)).containsExactly("INVITE_USERS");

        service.create(john.getCustomerId(), req("by.john@inv.example", "MEMBER"));
        service.create(admin.getCustomerId(), req("by.admin@inv.example", "MEMBER"));
        assertThat(service.list(john.getCustomerId(), null)).extracting(InvitationDto::email).containsExactly("by.john@inv.example");
        assertThat(service.list(admin.getCustomerId(), null)).hasSize(2);
        assertThatThrownBy(() -> service.create(john.getCustomerId(), req("boss@inv.example", "ORG_ADMIN")))
            .isInstanceOf(ForbiddenException.class);
        Long adminsInvitation = service.list(admin.getCustomerId(), null).stream()
            .filter(i -> i.email().startsWith("by.admin")).findFirst().orElseThrow().id();
        assertThatThrownBy(() -> service.revoke(john.getCustomerId(), adminsInvitation)).isInstanceOf(ResourceNotFoundException.class);

        service.setInvitePermission(admin.getCustomerId(), john.getId(), false);
        assertThatThrownBy(() -> service.create(john.getCustomerId(), req("later@inv.example", "MEMBER"))).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> service.setInvitePermission(john.getCustomerId(), admin.getId(), true)).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> service.setInvitePermission(admin.getCustomerId(), admin.getId(), true)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aSecondPendingInvitationIsRefusedAndResendReplacesTheLink() {
        Organization o = org(10);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        service.create(admin.getCustomerId(), req("dup@inv.example", "MEMBER"));
        String first = token();
        assertThatThrownBy(() -> service.create(admin.getCustomerId(), req("DUP@inv.example", "MEMBER")))
            .isInstanceOf(DuplicateResourceException.class).hasMessageContaining("already pending");

        Long id = service.list(admin.getCustomerId(), null).get(0).id();
        CreateResult resent = service.resend(admin.getCustomerId(), id);
        assertThat(resent.invitation().sendCount()).isEqualTo(2);
        String second = token();
        assertThat(second).isNotEqualTo(first);
        assertThatThrownBy(() -> service.preview(first)).isInstanceOf(ResourceNotFoundException.class);
        assertThat(service.preview(second).status()).isEqualTo("PENDING");
        assertThat(invitationRepository.findAll().stream().filter(i -> i.getOrganizationId().equals(o.getId()))).hasSize(1);
    }

    @Test
    void activeAndSuspendedMembersAreRefusedAndPendingInvitationsHoldNoSeat() {
        Organization o = org(2);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        OrganizationMember active = member(o, OrgRole.MEMBER);
        String activeEmail = customerRepository.findById(active.getCustomerId()).orElseThrow().getEmail();
        assertThatThrownBy(() -> service.create(admin.getCustomerId(), req(activeEmail, "MEMBER")))
            .isInstanceOf(DuplicateResourceException.class).hasMessage("This user is already a member of this organization.");

        memberService.suspendMember(active.getId());
        assertThatThrownBy(() -> service.create(admin.getCustomerId(), req(activeEmail, "MEMBER")))
            .isInstanceOf(InvalidStateException.class).hasMessageContaining("suspended");
        memberService.removeMember(active.getId());

        // 1 of 2 seats used: two pending invitations are fine (they reserve nothing)
        service.create(admin.getCustomerId(), req("p1@inv.example", "MEMBER"));
        service.create(admin.getCustomerId(), req("p2@inv.example", "MEMBER"));
        // fill the last seat, then sending is refused
        member(o, OrgRole.MEMBER);
        assertThatThrownBy(() -> service.create(admin.getCustomerId(), req("p3@inv.example", "MEMBER")))
            .isInstanceOf(SeatLimitExceededException.class).hasMessage("There are no available seats in this organization.");
    }

    @Test
    void anExistingAccountAcceptsAfterSignInAndNeedsTheSameEmail() {
        Organization o = org(10);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        Customer existing = customer("exist");
        service.create(admin.getCustomerId(), req(existing.getEmail(), "MEMBER"));
        String t = token();
        assertThat(service.preview(t).accountExists()).isTrue();

        Customer other = customer("other");
        assertThatThrownBy(() -> service.accept(t, other.getId())).isInstanceOf(ForbiddenException.class);

        AcceptResult done = service.accept(t, existing.getId());
        assertThat(done.accepted()).isTrue();
        OrganizationMember joined = memberRepository.findByOrganizationIdAndCustomerId(o.getId(), existing.getId()).orElseThrow();
        assertThat(joined.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(joined.getOrgRole()).isEqualTo(OrgRole.MEMBER);
        assertThat(service.preview(t).status()).isEqualTo("ACCEPTED");
        assertThatThrownBy(() -> service.accept(t, existing.getId())).isInstanceOf(InvalidStateException.class);
    }

    @Test
    void aNewPersonCreatesAnAccountFromTheInvitationAndJoins() {
        Organization o = org(10);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        String address = "brand.new" + System.nanoTime() + "@inv.example";
        service.create(admin.getCustomerId(), req(address, "MEMBER"));
        String t = token();
        assertThat(service.preview(t).accountExists()).isFalse();

        assertThatThrownBy(() -> service.createAccount(t, new AccountRequest("Ann", "Ray", "short", "short"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.createAccount(t, new AccountRequest("", "Ray", "Str0ng!Passw0rd", "Str0ng!Passw0rd"))).isInstanceOf(IllegalArgumentException.class);

        int before = kc().createUserCalls.size();
        AcceptResult r = service.createAccount(t, new AccountRequest("Ann", "Ray", "Str0ng!Passw0rd", "Str0ng!Passw0rd"));
        assertThat(r.accepted()).isTrue();
        assertThat(kc().createUserCalls).hasSize(before + 1);
        assertThat(kc().createUserCalls.get(before).enabled()).isTrue();
        Customer created = customerRepository.findByEmailIgnoreCase(address).orElseThrow();
        assertThat(created.getFirstName()).isEqualTo("Ann");
        assertThat(created.getKeycloakSub()).isNotNull();
        assertThat(memberRepository.findFirstByCustomerIdAndStatus(created.getId(), MembershipStatus.ACTIVE)).isPresent();
        assertThatThrownBy(() -> service.createAccount(t, new AccountRequest("Ann", "Ray", "Str0ng!Passw0rd", "Str0ng!Passw0rd")))
            .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void acceptanceChecksSeatsAgainAndRefusesWithoutActivatingAnything() {
        Organization o = org(2);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        Customer invitee = customer("late");
        service.create(admin.getCustomerId(), req(invitee.getEmail(), "MEMBER"));
        String t = token();
        member(o, OrgRole.MEMBER); // takes the last seat after the invitation was sent

        assertThatThrownBy(() -> service.accept(t, invitee.getId())).isInstanceOf(SeatLimitExceededException.class)
            .hasMessage("There are no available seats in this organization.");
        assertThat(memberRepository.findByOrganizationIdAndCustomerId(o.getId(), invitee.getId())).isEmpty();
        assertThat(service.preview(t).status()).isEqualTo("PENDING");
        var failures = auditService.search(o.getId(), null, "INVITATION_ACCEPT_FAILED_SEAT", null, null, 0, 10);
        assertThat(failures.items()).hasSize(1);
        assertThat(failures.items().get(0).outcome()).isEqualTo("FAILURE");
    }

    @Test
    void aPersonActiveInAnotherOrganizationIsRefusedAndNotMoved() {
        Organization first = org(10);
        Organization second = org(10);
        OrganizationMember admin = member(second, OrgRole.ORG_ADMIN);
        Customer busy = customer("busy");
        memberService.addMember(first.getId(), busy.getId(), OrgRole.MEMBER);

        service.create(admin.getCustomerId(), req(busy.getEmail(), "MEMBER")); // sending reveals nothing about other organizations
        assertThatThrownBy(() -> service.accept(token(), busy.getId())).isInstanceOf(InvalidStateException.class)
            .hasMessage("This account already belongs to another organization.");
        assertThat(memberRepository.findByOrganizationIdAndCustomerId(second.getId(), busy.getId())).isEmpty();
        assertThat(memberRepository.findFirstByCustomerIdAndStatus(busy.getId(), MembershipStatus.ACTIVE).orElseThrow().getOrganizationId())
            .isEqualTo(first.getId());
    }

    @Test
    void aRemovedMemberIsReadmittedOnTheSameRowWithTheNewRole() {
        Organization o = org(10);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        OrganizationMember gone = member(o, OrgRole.MEMBER);
        memberService.removeMember(gone.getId());
        String address = customerRepository.findById(gone.getCustomerId()).orElseThrow().getEmail();

        service.create(admin.getCustomerId(), req(address, "ORG_ADMIN"));
        service.accept(token(), gone.getCustomerId());
        OrganizationMember back = memberRepository.findByOrganizationIdAndCustomerId(o.getId(), gone.getCustomerId()).orElseThrow();
        assertThat(back.getId()).isEqualTo(gone.getId());
        assertThat(back.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(back.getOrgRole()).isEqualTo(OrgRole.ORG_ADMIN);
        assertThat(back.getDeactivatedAt()).isNull();
        assertThat(memberRepository.findByOrganizationId(o.getId()).stream().filter(m -> m.getCustomerId().equals(gone.getCustomerId()))).hasSize(1);
    }

    @Test
    void revokedExpiredAndDeclinedInvitationsCannotBeAcceptedAndCanBeResent() {
        Organization o = org(10);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        Customer a = customer("rev");
        service.create(admin.getCustomerId(), req(a.getEmail(), "MEMBER"));
        String t1 = token();
        Long id = service.list(admin.getCustomerId(), null).get(0).id();
        assertThat(service.revoke(admin.getCustomerId(), id).status()).isEqualTo("REVOKED");
        assertThatThrownBy(() -> service.accept(t1, a.getId())).isInstanceOf(InvalidStateException.class).hasMessage("This invitation is no longer valid.");
        assertThatThrownBy(() -> service.revoke(admin.getCustomerId(), id)).isInstanceOf(InvalidStateException.class);

        service.resend(admin.getCustomerId(), id);
        String t2 = token();
        OrganizationInvitation inv = invitationRepository.findById(id).orElseThrow();
        inv.setExpiresAt(Instant.now().minusSeconds(5));
        invitationRepository.save(inv);
        assertThat(service.expireOverdue()).isGreaterThanOrEqualTo(1);
        assertThatThrownBy(() -> service.accept(t2, a.getId())).isInstanceOf(InvalidStateException.class)
            .hasMessage("This invitation has expired. Please request a new invitation.");
        assertThat(service.list(admin.getCustomerId(), "EXPIRED")).hasSize(1);

        service.resend(admin.getCustomerId(), id);
        String t3 = token();
        service.decline(t3);
        assertThat(service.list(admin.getCustomerId(), "DECLINED")).hasSize(1);
        assertThatThrownBy(() -> service.decline(t3)).isInstanceOf(InvalidStateException.class);
        assertThat(service.resend(admin.getCustomerId(), id).invitation().status()).isEqualTo("PENDING");
        assertThatThrownBy(() -> service.preview("garbage")).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void aSuspendedOrClosedOrganizationCannotInviteOrAccept() {
        Organization o = org(10);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        Customer invitee = customer("susp");
        service.create(admin.getCustomerId(), req(invitee.getEmail(), "MEMBER"));
        String t = token();

        o.setLifecycleStatus(OrganizationLifecycleStatus.SUSPENDED);
        organizationRepository.save(o);
        assertThatThrownBy(() -> service.create(admin.getCustomerId(), req("new@inv.example", "MEMBER")))
            .isInstanceOf(InvalidStateException.class).hasMessage("This organization is currently unavailable for new members.");
        assertThat(service.preview(t).organizationAvailable()).isFalse();
        assertThatThrownBy(() -> service.accept(t, invitee.getId())).isInstanceOf(InvalidStateException.class)
            .hasMessage("This organization is currently unavailable for new members.");
        assertThat(memberRepository.findByOrganizationIdAndCustomerId(o.getId(), invitee.getId())).isEmpty();
    }

    @Test
    void theStructureNodeIsOptionalActiveAndAppliedOnAcceptanceAndGrantsNothing() {
        Organization o = org(10);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        OrgHierarchyDtos.TreeDto tree = hierarchyService.tree(admin.getCustomerId());
        Long root = tree.nodes().get(0).id();
        var dept = hierarchyService.create(admin.getCustomerId(), new OrgHierarchyDtos.CreateNodeRequest(root, "Ops", "DEPARTMENT", null, null, null));

        assertThat(service.structureNodes(admin.getCustomerId())).extracting(NodeOption::path).contains(o.getName(), o.getName() + " › Ops");
        Organization other = org(10);
        OrganizationMember otherAdmin = member(other, OrgRole.ORG_ADMIN);
        assertThatThrownBy(() -> service.create(otherAdmin.getCustomerId(), new CreateInvitationRequest("x@inv.example", "MEMBER", dept.id())))
            .isInstanceOf(IllegalArgumentException.class);

        Customer invitee = customer("node");
        CreateResult r = service.create(admin.getCustomerId(), new CreateInvitationRequest(invitee.getEmail(), "MEMBER", dept.id()));
        assertThat(r.invitation().orgNodeName()).isEqualTo(o.getName() + " › Ops");
        service.accept(token(), invitee.getId());
        OrganizationMember joined = memberRepository.findByOrganizationIdAndCustomerId(o.getId(), invitee.getId()).orElseThrow();
        assertThat(joined.getOrgNodeId()).isEqualTo(dept.id());
        assertThat(memberAccessService.listPermissions(joined)).isEmpty();
    }

    @Test
    void anAdministratorCanReadTheOrganizationsInvitationsReadOnly() {
        Organization o = org(10);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        service.create(admin.getCustomerId(), req("ro@inv.example", "MEMBER"));
        assertThat(service.adminList(o.getId())).hasSize(1);
        assertThat(service.adminList(o.getId()).get(0).toString()).doesNotContain(token());
        assertThatThrownBy(() -> service.adminList(-5L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
