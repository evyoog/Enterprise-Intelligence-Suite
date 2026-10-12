package com.vyoog.eisplatform.modules.access.service;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.SeatLimitExceededException;
import com.vyoog.eisplatform.modules.access.dto.AccessDtos.*;
import com.vyoog.eisplatform.modules.authorization.service.MemberAccessService;
import com.vyoog.eisplatform.modules.billing.service.BillingOwnerResolver;
import com.vyoog.eisplatform.modules.notification.service.NotificationService;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.dto.ChangePlanRequest;
import com.vyoog.eisplatform.modules.registration.model.*;
import com.vyoog.eisplatform.modules.registration.repository.*;
import com.vyoog.eisplatform.modules.registration.service.OrganizationMemberService;
import com.vyoog.eisplatform.modules.registration.service.OrganizationSelfService;
import com.vyoog.eisplatform.modules.registration.service.ProductSeatService;
import com.vyoog.eisplatform.modules.registration.service.SubscriptionSeatService;
import com.vyoog.eisplatform.modules.registration.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** REQ-TEN-005 Access management (TC-TEN-068 to TC-TEN-077): role defaults, overrides, guard rails, named seats (C90). */
@SpringBootTest
@ActiveProfiles("test")
class AccessManagementServiceTest {

    @Autowired private AccessManagementService service;
    @Autowired private AccessSyncService syncService;
    @Autowired private MemberAccessService memberAccessService;
    @Autowired private OrganizationSelfService selfService;
    @Autowired private OrganizationMemberService memberService;
    @Autowired private SubscriptionSeatService seatService;
    @Autowired private SubscriptionService subscriptionService;
    @Autowired private ProductSeatService productSeatService;
    @Autowired private BillingOwnerResolver billingOwnerResolver;
    @Autowired private NotificationService notificationService;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMemberRepository memberRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductSubscriptionRepository subscriptionRepository;
    @Autowired private OrganizationProductAccessRepository accessRepository;

    private Organization org(int licensed) {
        Organization o = new Organization();
        o.setName("Acc Org");
        o.setCode("ACC-" + System.nanoTime());
        o.setBusinessEmail("biz@acc.example");
        o.setCountry("India");
        o.setLicensedSeats(licensed);
        return organizationRepository.save(o);
    }

    private OrganizationMember member(Organization o, OrgRole role) {
        Customer c = new Customer();
        c.setEmail("m" + System.nanoTime() + "@acc.example");
        c.setFirstName("Pat");
        c.setLastName("Lee");
        c = customerRepository.save(c);
        return memberService.addMember(o.getId(), c.getId(), role);
    }

    private Product product(String name) {
        Product p = new Product();
        p.setName(name + System.nanoTime());
        p.setPrice(BigDecimal.TEN);
        p.setStatus(ProductStatus.ACTIVE);
        return productRepository.save(p);
    }

    private ProductSubscription subscribe(Organization o, Product p, int seats) {
        ProductSubscription s = new ProductSubscription();
        s.setProductId(p.getId());
        s.setOwnerType(RegistrationOwnerType.ORGANIZATION);
        s.setOwnerOrganizationId(o.getId());
        s.setQuantity(seats);
        s.setStatus(SubscriptionStatus.ACTIVE);
        s.setStartedAt(Instant.now());
        return subscriptionRepository.save(s);
    }

    private boolean hasAccess(OrganizationMember m, Product p) {
        return accessRepository.findByOrganizationMemberIdAndProductId(m.getId(), p.getId())
            .filter(a -> a.getStatus() == MembershipStatus.ACTIVE).isPresent();
    }

    private OrganizationMember fresh(OrganizationMember m) {
        return memberRepository.findById(m.getId()).orElseThrow();
    }

    private SetOverridesRequest perm(String code, boolean on) {
        return new SetOverridesRequest(Map.of(code, on), null, null);
    }

    private SetOverridesRequest prod(Product p, boolean on) {
        return new SetOverridesRequest(null, Map.of(p.getId(), on), null);
    }

    // TC-TEN-068
    @Test
    void anEntitledProductIsOnForMembersByDefaultWithTheDefaultRoleAndAdminsAreLockedOn() {
        Organization o = org(10);
        Product p = product("Valam");
        subscribe(o, p, 10);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        OrganizationMember m = member(o, OrgRole.MEMBER);

        assertThat(hasAccess(m, p)).isTrue();
        assertThat(accessRepository.findByOrganizationMemberIdAndProductId(m.getId(), p.getId()).orElseThrow().getProductRole()).isEqualTo("USER");
        assertThat(memberAccessService.listPermissions(m)).isEmpty();
        assertThat(memberAccessService.listPermissions(admin)).contains("MANAGE_ACCESS", "MANAGE_SUBSCRIPTIONS", "MANAGE_ORGANIZATION_BILLING", "MANAGE_USERS");

        MemberDetail detail = service.memberDetail(admin.getCustomerId(), m.getId());
        assertThat(detail.products()).hasSize(1);
        assertThat(detail.products().get(0).enabled()).isTrue();
        assertThat(detail.products().get(0).source()).isEqualTo("ROLE");
        assertThat(detail.permissions()).anyMatch(i -> i.code().equals("MANAGE_ORDERS") && !i.enabled());
        assertThat(service.memberDetail(admin.getCustomerId(), admin.getId()).canChange()).isFalse();
    }

    // TC-TEN-069
    @Test
    void anAdminDelegatesManageSubscriptionsAndTheMemberCanThenActOnTheSubscription() {
        Organization o = org(10);
        Product p = product("Delegate");
        ProductSubscription s = subscribe(o, p, 10);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        OrganizationMember m = member(o, OrgRole.MEMBER);

        assertThatThrownBy(() -> seatService.suspend(m.getCustomerId(), s.getId())).isInstanceOf(ForbiddenException.class);

        service.setOverrides(admin.getCustomerId(), m.getId(), perm("MANAGE_SUBSCRIPTIONS", true));
        assertThat(memberAccessService.hasPermission(fresh(m), "MANAGE_SUBSCRIPTIONS")).isTrue();
        assertThat(seatService.suspend(m.getCustomerId(), s.getId()).status()).isEqualTo(SubscriptionStatus.SUSPENDED);
        assertThat(seatService.reactivate(m.getCustomerId(), s.getId()).status()).isEqualTo(SubscriptionStatus.ACTIVE);

        // removing the grant stops it at once
        service.setOverrides(admin.getCustomerId(), m.getId(), perm("MANAGE_SUBSCRIPTIONS", false));
        assertThatThrownBy(() -> seatService.cancel(m.getCustomerId(), s.getId())).isInstanceOf(ForbiddenException.class);
        // and the plan change needs the same permission
        assertThatThrownBy(() -> seatService.changePlan(m.getCustomerId(), s.getId(), new ChangePlanRequest(null))).isInstanceOf(ForbiddenException.class);
    }

    // TC-TEN-070
    @Test
    void aProductIsTurnedOffAndOnIndividuallyAndResetReturnsToTheRoleDefault() {
        Organization o = org(10);
        Product p = product("Toggle");
        subscribe(o, p, 10);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        OrganizationMember m = member(o, OrgRole.MEMBER);

        MemberDetail off = service.setOverrides(admin.getCustomerId(), m.getId(), prod(p, false));
        assertThat(hasAccess(m, p)).isFalse();
        assertThat(off.products().get(0).enabled()).isFalse();
        assertThat(off.products().get(0).source()).isEqualTo("OVERRIDE");
        assertThat(syncService.recomputeMember(fresh(m), null)).isEmpty(); // the override holds

        MemberDetail reset = service.setOverrides(admin.getCustomerId(), m.getId(),
            new SetOverridesRequest(null, null, List.of("product:" + p.getId())));
        assertThat(hasAccess(m, p)).isTrue();
        assertThat(reset.products().get(0).source()).isEqualTo("ROLE");

        service.setOverrides(admin.getCustomerId(), m.getId(), prod(p, false));
        service.setOverrides(admin.getCustomerId(), m.getId(), perm("MANAGE_ORDERS", true));
        MemberDetail all = service.resetAll(admin.getCustomerId(), m.getId());
        assertThat(hasAccess(m, p)).isTrue();
        assertThat(all.permissions()).noneMatch(i -> i.source().equals("OVERRIDE"));
    }

    // TC-TEN-071
    @Test
    void theGuardRailsRefuseSelfChangeAdminTargetsLockedPermissionsAndPermissionsTheCallerDoesNotHold() {
        Organization o = org(10);
        Product p = product("Guard");
        subscribe(o, p, 10);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        OrganizationMember delegate = member(o, OrgRole.MEMBER);
        OrganizationMember other = member(o, OrgRole.MEMBER);
        service.setOverrides(admin.getCustomerId(), delegate.getId(), perm("MANAGE_ACCESS", true));

        // BR-3: nobody changes their own access, admin or delegate
        assertThatThrownBy(() -> service.setOverrides(delegate.getCustomerId(), delegate.getId(), perm("MANAGE_ORDERS", true)))
            .isInstanceOf(ForbiddenException.class).hasMessageContaining("own access");
        assertThatThrownBy(() -> service.setOverrides(admin.getCustomerId(), admin.getId(), prod(p, false)))
            .isInstanceOf(ForbiddenException.class).hasMessageContaining("own access");
        // BR-4: only an admin changes an admin
        assertThatThrownBy(() -> service.setOverrides(delegate.getCustomerId(), admin.getId(), prod(p, false)))
            .isInstanceOf(ForbiddenException.class).hasMessageContaining("administrator");
        // BR-6: an admin's permissions are locked on
        OrganizationMember admin2 = member(o, OrgRole.ORG_ADMIN);
        assertThatThrownBy(() -> service.setOverrides(admin.getCustomerId(), admin2.getId(), perm("MANAGE_ORDERS", false)))
            .isInstanceOf(ForbiddenException.class).hasMessageContaining("locked");
        // BR-2: a delegate cannot grant what they do not hold, but can grant what they hold
        assertThatThrownBy(() -> service.setOverrides(delegate.getCustomerId(), other.getId(), perm("MANAGE_ORDERS", true)))
            .isInstanceOf(ForbiddenException.class).hasMessageContaining("do not hold");
        service.setOverrides(delegate.getCustomerId(), other.getId(), perm("MANAGE_ACCESS", true));
        assertThat(memberAccessService.hasPermission(fresh(other), "MANAGE_ACCESS")).isTrue();
        // an ordinary member cannot use the screen at all (AM-10: Manage members does not include Manage access)
        OrganizationMember plain = member(o, OrgRole.MEMBER);
        assertThatThrownBy(() -> service.memberDetail(plain.getCustomerId(), other.getId())).isInstanceOf(ForbiddenException.class);
        // a member of another organization is not found
        Organization o2 = org(5);
        OrganizationMember foreign = member(o2, OrgRole.MEMBER);
        assertThatThrownBy(() -> service.memberDetail(admin.getCustomerId(), foreign.getId()))
            .isInstanceOf(com.vyoog.eisplatform.common.exception.ResourceNotFoundException.class);
    }

    // TC-TEN-071 (role change guards)
    @Test
    void roleChangesFollowTheSameGuardRailsAndRecalculateAccessKeepingOverrides() {
        Organization o = org(10);
        Product p = product("Role");
        subscribe(o, p, 10);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        OrganizationMember delegate = member(o, OrgRole.MEMBER);
        OrganizationMember m = member(o, OrgRole.MEMBER);
        service.setOverrides(admin.getCustomerId(), m.getId(), prod(p, false));
        service.setOverrides(admin.getCustomerId(), delegate.getId(), perm("MANAGE_ACCESS", true));
        service.setOverrides(admin.getCustomerId(), delegate.getId(), perm("MANAGE_USERS", true));

        assertThatThrownBy(() -> selfService.changeMemberRole(delegate.getCustomerId(), delegate.getId(), OrgRole.ORG_ADMIN))
            .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> selfService.changeMemberRole(delegate.getCustomerId(), m.getId(), OrgRole.ORG_ADMIN))
            .isInstanceOf(ForbiddenException.class).hasMessageContaining("administrator");
        assertThatThrownBy(() -> selfService.changeMemberRole(delegate.getCustomerId(), admin.getId(), OrgRole.MEMBER))
            .isInstanceOf(ForbiddenException.class);

        selfService.changeMemberRole(admin.getCustomerId(), m.getId(), OrgRole.ORG_ADMIN);
        assertThat(memberAccessService.hasPermission(fresh(m), "MANAGE_ORDERS")).isTrue(); // new role's permissions
        assertThat(hasAccess(m, p)).isFalse(); // the individual override is kept
    }

    // TC-TEN-072
    @Test
    void roleDefaultsAreChangedWithAPreviewAndOnlyAffectMembersWithoutAnOverride() {
        Organization o = org(10);
        Product p = product("Defaults");
        subscribe(o, p, 10);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        OrganizationMember a = member(o, OrgRole.MEMBER);
        OrganizationMember b = member(o, OrgRole.MEMBER);
        service.setOverrides(admin.getCustomerId(), b.getId(), prod(p, true)); // equals the default: no override stored
        service.setOverrides(admin.getCustomerId(), b.getId(), perm("MANAGE_ORDERS", true));

        RoleDefaultsRequest request = new RoleDefaultsRequest(Map.of("MANAGE_ORDERS", true), Map.of(p.getId(), false));
        RoleDefaultsResult preview = service.saveRoleDefaults(admin.getCustomerId(), "MEMBER", request, true);
        assertThat(preview.preview()).isTrue();
        assertThat(preview.items()).hasSize(2);
        assertThat(hasAccess(a, p)).isTrue(); // nothing saved by a preview
        assertThat(service.roleDefaults(admin.getCustomerId(), "MEMBER").permissions())
            .anyMatch(i -> i.code().equals("MANAGE_ORDERS") && !i.enabled());

        RoleDefaultsResult saved = service.saveRoleDefaults(admin.getCustomerId(), "MEMBER", request, false);
        assertThat(saved.affectedMembers()).isEqualTo(2);
        assertThat(hasAccess(a, p)).isFalse();
        assertThat(hasAccess(b, p)).isFalse(); // b's "on" matched the old default, so it was no override: it follows the role
        assertThat(memberAccessService.hasPermission(fresh(a), "MANAGE_ORDERS")).isTrue();
        assertThat(service.roleDefaults(admin.getCustomerId(), "MEMBER").products()).anyMatch(i -> !i.enabled() && i.customized());

        // a new member of the role follows the organization's defaults
        OrganizationMember c = member(o, OrgRole.MEMBER);
        assertThat(hasAccess(c, p)).isFalse();
        assertThat(memberAccessService.hasPermission(c, "MANAGE_ORDERS")).isTrue();

        // an administrator's permissions cannot be switched off; only an admin edits the admin role's defaults
        assertThatThrownBy(() -> service.saveRoleDefaults(admin.getCustomerId(), "ORG_ADMIN",
            new RoleDefaultsRequest(Map.of("MANAGE_ORDERS", false), null), false)).isInstanceOf(ForbiddenException.class);
        OrganizationMember delegate = member(o, OrgRole.MEMBER);
        service.setOverrides(admin.getCustomerId(), delegate.getId(), perm("MANAGE_ACCESS", true));
        assertThatThrownBy(() -> service.saveRoleDefaults(delegate.getCustomerId(), "ORG_ADMIN",
            new RoleDefaultsRequest(null, Map.of(p.getId(), false)), false)).isInstanceOf(ForbiddenException.class);
        assertThat(service.roleDefaults(admin.getCustomerId(), "ORG_ADMIN").permissions()).allMatch(i -> i.locked() && i.enabled());
    }

    // TC-TEN-073
    @Test
    void namedSeatsLimitAProductAndAFreedSeatIsFilledByTheNextRecalculation() {
        Organization o = org(10);
        Product p = product("Seats");
        ProductSubscription s = subscribe(o, p, 2);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        OrganizationMember a = member(o, OrgRole.MEMBER);
        OrganizationMember b = member(o, OrgRole.MEMBER); // no seat left

        assertThat(hasAccess(admin, p)).isTrue();
        assertThat(hasAccess(a, p)).isTrue();
        assertThat(hasAccess(b, p)).isFalse();
        assertThat(productSeatService.seatsInUse(o.getId(), p.getId())).isEqualTo(2);
        MemberDetail detail = service.memberDetail(admin.getCustomerId(), b.getId());
        assertThat(detail.products().get(0).lockedReason()).contains("No free seats");
        assertThat(detail.products().get(0).seats().quantity()).isEqualTo(2);

        assertThatThrownBy(() -> service.setOverrides(admin.getCustomerId(), b.getId(), prod(p, true)))
            .isInstanceOf(SeatLimitExceededException.class).hasMessageContaining("No free seats on");
        assertThat(hasAccess(b, p)).isFalse();

        service.setOverrides(admin.getCustomerId(), a.getId(), prod(p, false)); // frees a seat
        service.setOverrides(admin.getCustomerId(), b.getId(), prod(p, true));
        assertThat(hasAccess(b, p)).isTrue();

        // suspending a member frees the seat
        memberService.suspendMember(b.getId());
        assertThat(productSeatService.seatsInUse(o.getId(), p.getId())).isEqualTo(1);
        // the platform administrator's overage switch lifts the limit
        Organization stored = organizationRepository.findById(o.getId()).orElseThrow();
        stored.setAllowSeatOverage(true);
        organizationRepository.save(stored);
        assertThat(productSeatService.freeSeats(o.getId(), p.getId())).isEqualTo(Long.MAX_VALUE);
        assertThat(s.getQuantity()).isEqualTo(2);
    }

    // TC-TEN-074
    @Test
    void bulkChangesApplyMemberByMemberAndReportWhySomeWereSkipped() {
        Organization o = org(10);
        Product p = product("Bulk");
        subscribe(o, p, 3);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        OrganizationMember a = member(o, OrgRole.MEMBER);
        OrganizationMember b = member(o, OrgRole.MEMBER);
        OrganizationMember c = member(o, OrgRole.MEMBER); // no seat
        OrganizationMember d = member(o, OrgRole.MEMBER); // no seat
        service.setOverrides(admin.getCustomerId(), a.getId(), prod(p, false));
        service.setOverrides(admin.getCustomerId(), b.getId(), prod(p, false));
        // seats now: admin, c, d
        service.setOverrides(admin.getCustomerId(), c.getId(), prod(p, false));
        service.setOverrides(admin.getCustomerId(), d.getId(), prod(p, false));
        // free seats: 2 of 3 (admin holds one)
        List<Long> ids = List.of(a.getId(), b.getId(), c.getId(), admin.getId());

        BulkResult preview = service.bulk(admin.getCustomerId(), new BulkRequest("GRANT", "product:" + p.getId(), ids, true));
        assertThat(preview.preview()).isTrue();
        assertThat(preview.applied()).isEqualTo(2);
        assertThat(preview.skipped()).hasSize(2);
        assertThat(hasAccess(a, p)).isFalse(); // a preview changes nothing

        BulkResult result = service.bulk(admin.getCustomerId(), new BulkRequest("GRANT", "product:" + p.getId(), ids, false));
        assertThat(result.applied()).isEqualTo(2);
        assertThat(result.skipped()).extracting(BulkSkipped::reason)
            .anyMatch(r -> r.contains("No free seats")).anyMatch(r -> r.contains("own access"));
        assertThat(hasAccess(a, p)).isTrue();
        assertThat(hasAccess(b, p)).isTrue();
        assertThat(hasAccess(c, p)).isFalse();

        BulkResult perms = service.bulk(admin.getCustomerId(), new BulkRequest("GRANT", "permission:MANAGE_ORDERS", List.of(a.getId(), b.getId()), false));
        assertThat(perms.applied()).isEqualTo(2);
        assertThat(memberAccessService.hasPermission(fresh(a), "MANAGE_ORDERS")).isTrue();
        BulkResult removed = service.bulk(admin.getCustomerId(), new BulkRequest("REMOVE", "permission:MANAGE_ORDERS", List.of(a.getId(), b.getId(), a.getId()), false));
        assertThat(removed.applied()).isEqualTo(2);
        assertThatThrownBy(() -> service.bulk(admin.getCustomerId(), new BulkRequest("GRANT", "thing:1", ids, false)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // TC-TEN-075
    @Test
    void everyChangeIsAuditedAndTheMemberIsToldInTheApp() {
        Organization o = org(10);
        Product p = product("Audit");
        subscribe(o, p, 10);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        OrganizationMember m = member(o, OrgRole.MEMBER);
        int before = notificationService.listNotifications(m.getCustomerId()).notifications().size();

        service.setOverrides(admin.getCustomerId(), m.getId(), new SetOverridesRequest(Map.of("MANAGE_ORDERS", true), Map.of(p.getId(), false), null));

        var notices = notificationService.listNotifications(m.getCustomerId()).notifications();
        assertThat(notices).hasSize(before + 1); // one notice per change request
        assertThat(notices).anyMatch(n -> n.message().contains("Approve orders") && n.message().contains("no longer open"));
        var activity = service.activity(admin.getCustomerId(), 0, 50);
        assertThat(activity.items()).extracting(i -> i.action()).contains("ACCESS_PERMISSION_CHANGED", "ACCESS_PRODUCT_CHANGED");
        assertThat(activity.items()).anyMatch(i -> i.detail() != null
            && i.detail().contains("off -> on") && i.detail().contains("organization admin"));
        service.setOverrides(admin.getCustomerId(), m.getId(), new SetOverridesRequest(null, null, List.of("permission:MANAGE_ORDERS")));
        assertThat(service.activity(admin.getCustomerId(), 0, 50).items()).extracting(i -> i.action()).contains("ACCESS_OVERRIDE_RESET");
        assertThatThrownBy(() -> service.activity(m.getCustomerId(), 0, 10)).isInstanceOf(ForbiddenException.class);
    }

    // TC-TEN-076
    @Test
    void anEndedSubscriptionRemovesTheProductFromEffectiveAccessAndReactivatingBringsItBack() {
        Organization o = org(10);
        Product p = product("Entitle");
        ProductSubscription s = subscribe(o, p, 10);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        OrganizationMember m = member(o, OrgRole.MEMBER);
        assertThat(hasAccess(m, p)).isTrue();

        seatService.suspend(admin.getCustomerId(), s.getId());
        assertThat(hasAccess(m, p)).isFalse();
        assertThat(service.memberDetail(admin.getCustomerId(), m.getId()).products()).isEmpty();
        seatService.reactivate(admin.getCustomerId(), s.getId());
        assertThat(hasAccess(m, p)).isTrue();
        seatService.cancel(admin.getCustomerId(), s.getId());
        assertThat(hasAccess(m, p)).isFalse();
        assertThat(service.roleDefaults(admin.getCustomerId(), "MEMBER").products()).isEmpty();
        assertThatThrownBy(() -> service.saveRoleDefaults(admin.getCustomerId(), "MEMBER",
            new RoleDefaultsRequest(null, Map.of(p.getId(), false)), false)).isInstanceOf(IllegalArgumentException.class);
    }

    // TC-TEN-077
    @Test
    void manageBillingIsItsOwnPermissionAndManageOrganizationNoLongerOpensBilling() {
        Organization o = org(10);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        OrganizationMember m = member(o, OrgRole.MEMBER);
        assertThat(billingOwnerResolver.requireManageOrganizationBilling(admin.getCustomerId())).isNotNull();

        service.setOverrides(admin.getCustomerId(), m.getId(), perm("MANAGE_ORGANIZATION", true));
        assertThatThrownBy(() -> billingOwnerResolver.requireManageOrganizationBilling(m.getCustomerId())).isInstanceOf(ForbiddenException.class);
        assertThat(selfService.requireOrganizationManagement(m.getCustomerId())).isEqualTo(o.getId());

        service.setOverrides(admin.getCustomerId(), m.getId(), perm("MANAGE_ORGANIZATION_BILLING", true));
        assertThat(billingOwnerResolver.requireManageOrganizationBilling(m.getCustomerId())).isNotNull();
        assertThat(selfService.listMyOrganizationPermissions(m.getCustomerId())).contains("MANAGE_ORGANIZATION", "MANAGE_ORGANIZATION_BILLING");
    }

    @Test
    void theMemberListFiltersAndPagesAndTheCatalogueShowsWhatTheCallerMayGrant() {
        Organization o = org(10);
        Product p = product("List");
        subscribe(o, p, 10);
        OrganizationMember admin = member(o, OrgRole.ORG_ADMIN);
        OrganizationMember a = member(o, OrgRole.MEMBER);
        member(o, OrgRole.MEMBER);
        service.setOverrides(admin.getCustomerId(), a.getId(), prod(p, false));

        MemberPage all = service.listMembers(admin.getCustomerId(), null, null, null, null, null, 0, 2);
        assertThat(all.totalElements()).isEqualTo(3);
        assertThat(all.items()).hasSize(2);
        assertThat(service.listMembers(admin.getCustomerId(), null, "ORG_ADMIN", null, null, null, 0, 20).items()).hasSize(1);
        assertThat(service.listMembers(admin.getCustomerId(), null, null, null, true, null, 0, 20).items())
            .extracting(MemberRow::memberId).containsExactly(a.getId());
        assertThat(service.listMembers(admin.getCustomerId(), null, null, p.getId(), null, null, 0, 20).totalElements()).isEqualTo(2);
        assertThat(service.listMembers(admin.getCustomerId(), "nomatch", null, null, null, null, 0, 20).items()).isEmpty();
        assertThat(service.catalogue(admin.getCustomerId())).hasSize(9).allMatch(PermissionCatalogEntry::callerCanGrant);
    }
}
