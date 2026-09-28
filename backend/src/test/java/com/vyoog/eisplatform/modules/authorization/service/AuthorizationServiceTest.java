package com.vyoog.eisplatform.modules.authorization.service;

import com.vyoog.eisplatform.modules.authorization.model.OrganizationOwnedResource;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the two things Phase 3 actually changed: (1) RbacSeeder really does
 * seed real, queryable Role/Permission rows on startup (not just entities
 * that exist on paper), and (2) the exact permission sets this phase's report
 * claims — PLATFORM_ADMIN (Keycloak's "ADMIN" authority) getting MANAGE_CATALOG
 * + MANAGE_REGISTRATIONS, ORG_ADMIN getting the roadmap's own example set,
 * plain MEMBER getting none — are what a real lookup actually returns.
 */
@SpringBootTest
@ActiveProfiles("test")
class AuthorizationServiceTest {

    @Autowired
    private AuthorizationService authorizationService;

    @Test
    void keycloakAdminAuthorityGrantsCatalogAndRegistrationsManagement() {
        assertThat(authorizationService.hasPlatformPermission(Set.of("ROLE_ADMIN"), "MANAGE_CATALOG")).isTrue();
        assertThat(authorizationService.hasPlatformPermission(Set.of("ROLE_ADMIN"), "MANAGE_REGISTRATIONS")).isTrue();
        // Phase 6 (PAM): MANAGE_PRIVILEGED_ACCESS joined the ADMIN role's
        // seeded set — approving elevated-access requests is its own
        // permission, not folded into either of the other two.
        assertThat(authorizationService.hasPlatformPermission(Set.of("ROLE_ADMIN"), "MANAGE_PRIVILEGED_ACCESS")).isTrue();
        // Phase 3 (2026.3.3): MANAGE_ROLES/MANAGE_PERMISSIONS joined the
        // ADMIN role's seeded set — RBAC administration itself.
        // C26 (2026-09-26): MANAGE_SERVICE_STATUS, posting the service status page.
        // 15.01 Platform Administration (sprint 2026.4.2): MANAGE_PLATFORM_SETTINGS.
        // 11.01 Knowledge Base (sprint 2027.1.1): MANAGE_KNOWLEDGE_BASE.
        assertThat(authorizationService.listPlatformPermissions(Set.of("ROLE_ADMIN")))
            .containsExactlyInAnyOrder("MANAGE_CATALOG", "MANAGE_REGISTRATIONS", "MANAGE_PRIVILEGED_ACCESS", "VIEW_AUDIT_LOG",
                "MANAGE_ROLES", "MANAGE_PERMISSIONS", "MANAGE_SERVICE_STATUS", "MANAGE_PLATFORM_SETTINGS", "MANAGE_KNOWLEDGE_BASE");
    }

    @Test
    void anAuthorityThatIsNotASeededPlatformRoleGrantsNothing() {
        assertThat(authorizationService.hasPlatformPermission(Set.of("ROLE_TASK_USER"), "MANAGE_CATALOG")).isFalse();
        assertThat(authorizationService.hasPlatformPermission(Set.of(), "MANAGE_CATALOG")).isFalse();
    }

    @Test
    void orgAdminHasTheRoadmapsExampleShapeOfPermissions() {
        assertThat(authorizationService.hasOrganizationPermission(OrgRole.ORG_ADMIN, "MANAGE_ORGANIZATION")).isTrue();
        assertThat(authorizationService.hasOrganizationPermission(OrgRole.ORG_ADMIN, "MANAGE_USERS")).isTrue();
        assertThat(authorizationService.hasOrganizationPermission(OrgRole.ORG_ADMIN, "MANAGE_PRODUCT_ACCESS")).isTrue();
        // Phase 6 (PAM): joined ORG_ADMIN's seeded set alongside the other three.
        assertThat(authorizationService.hasOrganizationPermission(OrgRole.ORG_ADMIN, "MANAGE_PRIVILEGED_ACCESS")).isTrue();
        // 09.04 Approval Management (sprint 2027.1.1): MANAGE_ORDERS.
        assertThat(authorizationService.hasOrganizationPermission(OrgRole.ORG_ADMIN, "MANAGE_ORDERS")).isTrue();
        assertThat(authorizationService.listOrganizationPermissions(OrgRole.ORG_ADMIN))
            .containsExactlyInAnyOrder("MANAGE_ORGANIZATION", "MANAGE_USERS", "MANAGE_PRODUCT_ACCESS", "MANAGE_PRIVILEGED_ACCESS", "MANAGE_ORDERS");
    }

    @Test
    void plainMemberHasNoOrganizationPermissions() {
        assertThat(authorizationService.hasOrganizationPermission(OrgRole.MEMBER, "MANAGE_USERS")).isFalse();
        assertThat(authorizationService.listOrganizationPermissions(OrgRole.MEMBER)).isEmpty();
    }

    @Test
    void platformAndOrganizationScopesNeverCrossOver() {
        // A platform-scoped authority must never satisfy an organization-scoped
        // check and vice versa — same-named roles in different scopes must not
        // be conflatable ("keep organization-level roles and application-level
        // roles kept distinct" — scope is the one thing enforcing that here).
        assertThat(authorizationService.hasPlatformPermission(Set.of("ROLE_ORG_ADMIN"), "MANAGE_USERS")).isFalse();
        List<String> nothing = authorizationService.listPlatformPermissions(Set.of("ROLE_MEMBER"));
        assertThat(nothing).isEmpty();
    }

    @Test
    void canActOnOrganizationResourceRequiresBothTheRoleAndTheOrganizationMatch() {
        // Role holds the permission, but the resource belongs to a different
        // organization — the roadmap's own example rule, the deny branch.
        assertThat(authorizationService.canActOnOrganizationResource(OrgRole.ORG_ADMIN, "MANAGE_PRODUCT_ACCESS", 1L, 2L)).isFalse();
        // Same organization, but the role doesn't hold the permission.
        assertThat(authorizationService.canActOnOrganizationResource(OrgRole.MEMBER, "MANAGE_PRODUCT_ACCESS", 1L, 1L)).isFalse();
        // Both hold — the allow branch.
        assertThat(authorizationService.canActOnOrganizationResource(OrgRole.ORG_ADMIN, "MANAGE_PRODUCT_ACCESS", 1L, 1L)).isTrue();
    }

    @Test
    void organizationGoodStandingRejectsOnlyCancelledOrExpired() {
        assertThat(authorizationService.organizationInGoodStanding(RegistrationStatus.COMPLETED)).isTrue();
        assertThat(authorizationService.organizationInGoodStanding(RegistrationStatus.PENDING_EMAIL_VERIFICATION)).isTrue();
        assertThat(authorizationService.organizationInGoodStanding(RegistrationStatus.CANCELLED)).isFalse();
        assertThat(authorizationService.organizationInGoodStanding(RegistrationStatus.EXPIRED)).isFalse();
    }

    /** A throwaway resource type — deliberately NOT OrganizationMember, the
     * only type this service's other tests exercise — to prove Phase 5's
     * {@code evaluate}/{@code AccessPolicy} pipeline is genuinely reusable
     * for any resource, not secretly coupled to one entity. */
    private record FakeProject(Long organizationId) implements OrganizationOwnedResource {
    }

    private OrganizationMember memberOf(Long organizationId, OrgRole orgRole) {
        OrganizationMember member = new OrganizationMember();
        member.setOrganizationId(organizationId);
        member.setOrgRole(orgRole);
        return member;
    }

    @Test
    void evaluateWorksAgainstAnyResourceTypeNotJustOrganizationMember() {
        FakeProject sameOrgProject = new FakeProject(1L);
        FakeProject otherOrgProject = new FakeProject(2L);

        assertThat(authorizationService.evaluate(memberOf(1L, OrgRole.ORG_ADMIN), sameOrgProject, "MANAGE_USERS")).isTrue();
        assertThat(authorizationService.evaluate(memberOf(1L, OrgRole.MEMBER), sameOrgProject, "MANAGE_USERS")).isFalse();
        assertThat(authorizationService.evaluate(memberOf(1L, OrgRole.ORG_ADMIN), otherOrgProject, "MANAGE_USERS")).isFalse();
    }
}
