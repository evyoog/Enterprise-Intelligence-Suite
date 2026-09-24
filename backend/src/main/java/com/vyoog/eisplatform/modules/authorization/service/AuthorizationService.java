package com.vyoog.eisplatform.modules.authorization.service;

import com.vyoog.eisplatform.modules.authorization.model.OrganizationOwnedResource;
import com.vyoog.eisplatform.modules.authorization.model.Permission;
import com.vyoog.eisplatform.modules.authorization.model.Role;
import com.vyoog.eisplatform.modules.authorization.model.RoleScope;
import com.vyoog.eisplatform.modules.authorization.repository.RoleRepository;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The single place every real permission decision in this app gets made —
 * replaces the two authorization checks that used to be hardcoded strings:
 * {@code SecurityConfig}'s {@code hasRole("ADMIN")} (see {@link #hasPlatformPermission})
 * and {@code OrganizationSelfService}'s raw {@code orgRole != ORG_ADMIN}
 * comparison (see {@link #hasOrganizationPermission}).
 *
 * Deliberately asymmetric in WHERE "who holds this role" comes from, and this
 * is a considered choice, not an inconsistency:
 * <ul>
 *   <li>PLATFORM scope: still decided by Keycloak's client-role claim
 *   (unchanged — see {@code KeycloakJwtAuthenticationConverter}). Making this
 *   DB-driven instead would require a Customer row for every real Keycloak
 *   admin, and {@code CurrentCustomerResolver}'s own javadoc confirms the
 *   platform admin's account may have none (never registered through this
 *   app's own flow) — silently switching this would risk locking out the
 *   real admin, which the Global Rules explicitly forbid ("preserve all
 *   existing working functionality"). What DOES become real, DB-editable
 *   data here is what PLATFORM_ADMIN is allowed to do (its Permission set) —
 *   not who holds it.</li>
 *   <li>ORGANIZATION scope: who holds ORG_ADMIN/ORG_MEMBER was already fully
 *   DB-driven before this phase ({@code OrganizationMember.orgRole}, built in
 *   Phase 2) — so this scope's checks are entirely database-backed end to
 *   end, no Keycloak claim involved at all.</li>
 * </ul>
 *
 * <p>Phase 4 (ABAC) adds two attribute-based rules on top of the above,
 * composing with it rather than duplicating it — see {@link #canActOnOrganizationResource}
 * and {@link #organizationInGoodStanding}. Of the roadmap's broader attribute
 * list (organization, user, application, role, department/unit, user
 * attributes, resource attributes, environment/context, ownership, status),
 * only the ones with real, existing data in this codebase are implemented:
 * organization (ownership match) and status (Organization.status,
 * Product.status). "Department/unit" and "environment/context" have no
 * corresponding field anywhere in this app and were not fabricated.
 *
 * <p>Phase 5 (resource/policy-based) generalizes {@link #canActOnOrganizationResource}
 * from one bespoke method taking raw ids into a reusable pair of named
 * {@link AccessPolicy} rules — {@link #permissionPolicy} and
 * {@link #ownershipPolicy} — composed via {@link #evaluate}. Any resource
 * this app has (or a future one gains) needs only to implement the small
 * {@link OrganizationOwnedResource} marker interface to be checkable this
 * way; nothing about the policy logic is specific to any one resource type
 * or module. {@code canActOnOrganizationResource} itself is kept, unchanged,
 * for callers that only have raw organization ids on hand rather than a full
 * resource object — both call the same underlying {@link #hasOrganizationPermission},
 * so there is exactly one real implementation of the permission check either way.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthorizationService {

    private final RoleRepository roleRepository;

    /** {@code authorityNames} are Spring Security authority strings, e.g.
     * "ROLE_ADMIN" (exactly what {@code KeycloakJwtAuthenticationConverter}
     * already produces from the JWT's client-role claim) — the "ROLE_" prefix
     * is stripped before matching against {@link Role#getName()}. */
    public boolean hasPlatformPermission(Collection<String> authorityNames, String permissionName) {
        return rolesFor(roleNamesFromAuthorities(authorityNames), RoleScope.PLATFORM).stream()
            .anyMatch(role -> grants(role, permissionName));
    }

    public List<String> listPlatformPermissions(Collection<String> authorityNames) {
        return permissionNames(rolesFor(roleNamesFromAuthorities(authorityNames), RoleScope.PLATFORM));
    }

    public boolean hasOrganizationPermission(OrgRole orgRole, String permissionName) {
        return rolesFor(Set.of(orgRole.name()), RoleScope.ORGANIZATION).stream()
            .anyMatch(role -> grants(role, permissionName));
    }

    public List<String> listOrganizationPermissions(OrgRole orgRole) {
        return permissionNames(rolesFor(Set.of(orgRole.name()), RoleScope.ORGANIZATION));
    }

    /**
     * Phase 4 (ABAC): the roadmap's own example rule, implemented literally —
     * "user has role AND resource belongs to Organization A -> allow.
     * Otherwise deny." Tenant isolation is checked FIRST and short-circuits
     * to a flat deny before the permission lookup even runs: an org id
     * mismatch is never something a role can override, no matter how
     * permissive. Replaces the two separate, sequential checks
     * ({@code requirePermission} then a standalone equality comparison) that
     * used to live in {@code OrganizationSelfService} — same two facts,
     * composed into one decision instead of duplicated call sites.
     */
    public boolean canActOnOrganizationResource(OrgRole orgRole, String permissionName, Long callerOrganizationId, Long resourceOrganizationId) {
        if (callerOrganizationId == null || !callerOrganizationId.equals(resourceOrganizationId)) {
            return false;
        }
        return hasOrganizationPermission(orgRole, permissionName);
    }

    /**
     * Phase 4 (ABAC): the "status" attribute the roadmap names, applied to
     * an organization's own registration status. No admin action sets
     * CANCELLED/EXPIRED yet (that's Phase 11's "manage organization status"
     * job, not this phase's), so this is dormant in production today — but
     * the check itself is real, not speculative: RegistrationStatus already
     * has both values, and organization self-service is exactly where this
     * needs enforcing the moment an admin action can set them.
     */
    public boolean organizationInGoodStanding(RegistrationStatus status) {
        return status != RegistrationStatus.CANCELLED && status != RegistrationStatus.EXPIRED;
    }

    /** Phase 5: the RBAC half of a resource-based decision, as a named,
     * reusable policy rather than an inline lambda repeated at every call
     * site — "does the caller's role grant this permission," ignoring the
     * resource entirely (that's {@link #ownershipPolicy}'s job). */
    public AccessPolicy permissionPolicy(String permissionName) {
        return (caller, resource) -> hasOrganizationPermission(caller.getOrgRole(), permissionName);
    }

    /** Phase 5: the ABAC half of a resource-based decision, as a named,
     * reusable policy — "does this resource actually belong to the caller's
     * own organization," ignoring role/permission entirely (that's
     * {@link #permissionPolicy}'s job). Generalizes the org-id comparison
     * {@link #canActOnOrganizationResource} does inline to work against ANY
     * {@link OrganizationOwnedResource}, not just one hardcoded pair of ids. */
    public AccessPolicy ownershipPolicy() {
        return (caller, resource) -> caller.getOrganizationId() != null
            && caller.getOrganizationId().equals(resource.organizationId());
    }

    /**
     * Phase 5: the one call most resource-scoped actions should make — "can
     * this caller do this, to this resource" — composing {@link #permissionPolicy}
     * and {@link #ownershipPolicy} via {@link AccessPolicy#and}, the same two
     * facts {@link #canActOnOrganizationResource} checks, just expressed as
     * composable named rules instead of one flat method, and working against
     * any resource type that implements {@link OrganizationOwnedResource}
     * rather than being hardcoded to a pair of raw ids.
     */
    public boolean evaluate(OrganizationMember caller, OrganizationOwnedResource resource, String permissionName) {
        return permissionPolicy(permissionName).and(ownershipPolicy()).check(caller, resource);
    }

    private Set<String> roleNamesFromAuthorities(Collection<String> authorityNames) {
        return authorityNames.stream()
            .filter(a -> a.startsWith("ROLE_"))
            .map(a -> a.substring("ROLE_".length()))
            .collect(Collectors.toSet());
    }

    private List<Role> rolesFor(Collection<String> names, RoleScope scope) {
        return names.isEmpty() ? List.of() : roleRepository.findByNameInAndScope(names, scope);
    }

    private boolean grants(Role role, String permissionName) {
        return role.getPermissions().stream().anyMatch(p -> p.getName().equals(permissionName));
    }

    private List<String> permissionNames(List<Role> roles) {
        return roles.stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(Permission::getName)
            .distinct()
            .sorted()
            .toList();
    }
}
