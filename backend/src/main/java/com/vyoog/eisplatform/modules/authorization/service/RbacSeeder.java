package com.vyoog.eisplatform.modules.authorization.service;

import com.vyoog.eisplatform.modules.authorization.model.Permission;
import com.vyoog.eisplatform.modules.authorization.model.Role;
import com.vyoog.eisplatform.modules.authorization.model.RoleScope;
import com.vyoog.eisplatform.modules.authorization.repository.PermissionRepository;
import com.vyoog.eisplatform.modules.authorization.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Seeds the fixed Role/Permission set this phase introduces, on every
 * startup, idempotently (checks-then-inserts by name — running this against
 * an already-seeded database is always a safe no-op). There's no migration
 * tool for data the way {@code schema.sql} is hand-maintained for structure
 * (see decision 11) — a small idempotent runner is simpler and more visible
 * than adding a second, inconsistent seeding mechanism (e.g. a raw
 * {@code data.sql}) for what is, today, a genuinely fixed, small set of rows.
 *
 * Deliberately encodes the roadmap's own example shape verbatim: "Organization
 * Admin -> Manage Organization / Manage Users / Manage Product Access /
 * Manage Assigned Applications" and today's real PLATFORM_ADMIN capabilities
 * (product/platform catalog management, the registrations back-office) —
 * nothing invented beyond what already has a real enforcement point, except
 * MANAGE_ORGANIZATION, which is seeded per the roadmap's own example shape
 * even though no self-service "edit organization details" action exists yet
 * to gate with it (see this phase's own report for why that wasn't built
 * speculatively).
 */
@Component
@RequiredArgsConstructor
public class RbacSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    private static final Map<String, List<String>> PLATFORM_ROLES = Map.of(
        // Phase 6: MANAGE_PRIVILEGED_ACCESS is deliberately its own permission,
        // not folded into MANAGE_REGISTRATIONS — approving a request for
        // elevated access is a distinct, sensitive responsibility, not a form
        // of registration back-office work.
        // Phase 25: VIEW_AUDIT_LOG is its own permission too, not folded into
        // an existing one — being ABLE to see every administrative action
        // across the platform is a distinct, sensitive capability from being
        // able to perform one.
        // Phase 3 (2026.3.3): MANAGE_ROLES/MANAGE_PERMISSIONS are two more
        // distinct permissions, not folded into anything above — being able
        // to redefine what every OTHER permission/role means is a more
        // foundational capability than any single administrative action.
        "ADMIN", List.of("MANAGE_CATALOG", "MANAGE_REGISTRATIONS", "MANAGE_PRIVILEGED_ACCESS", "VIEW_AUDIT_LOG",
            "MANAGE_ROLES", "MANAGE_PERMISSIONS",
            // C26 (2026-09-26): posting the interim service status page (REQ-PRT-001).
            "MANAGE_SERVICE_STATUS",
            // 15.01 Platform Administration (sprint 2026.4.2): currencies, regions, feature flags.
            "MANAGE_PLATFORM_SETTINGS",
            // 11.01 Knowledge Base (sprint 2027.1.1): its own permission — publishing
            // customer-facing knowledge content is distinct from catalog management.
            "MANAGE_KNOWLEDGE_BASE",
            // 12.01 Ticket Management (sprint 2027.1.2): its own permission —
            // handling support tickets is a distinct responsibility from
            // every other admin capability above.
            "MANAGE_SUPPORT_TICKETS",
            // 03.04 Reviews & Ratings (sprint 2027.1.3): moderating customer
            // reviews is its own permission, distinct from catalog management.
            "MANAGE_REVIEWS",
            // 14.01 Provider Onboarding (sprint 2027.2.1): verifying/approving/
            // activating a provider and managing its contract is its own
            // permission — a distinct, sensitive responsibility from every
            // other admin capability above (it commits the platform to a
            // business relationship with an external party).
            "MANAGE_PARTNERS",
            // 08 Billing & Payments (sprint 2026.4.3, C46): refunding a
            // payment and seeing every customer's invoices/payments is a
            // distinct, sensitive responsibility from every other admin
            // capability above.
            "MANAGE_BILLING",
            // Platform admin dashboard (C53): a read-only, platform-wide
            // overview spanning every domain above (organizations, catalog,
            // subscriptions, billing, support, reviews) — its own permission
            // since being able to SEE a cross-domain summary is distinct
            // from being able to manage any one of those domains.
            "VIEW_PLATFORM_DASHBOARD")
    );

    private static final Map<String, List<String>> ORGANIZATION_ROLES = Map.of(
        // 09.04 Approval Management (sprint 2027.1.1): deciding a member's
        // order is its own permission — distinct from MANAGE_PRODUCT_ACCESS
        // (assigning ALREADY-purchased access to a member), since an order
        // decision commits the organization to a new subscription.
        "ORG_ADMIN", List.of("MANAGE_ORGANIZATION", "MANAGE_USERS", "MANAGE_PRODUCT_ACCESS", "MANAGE_PRIVILEGED_ACCESS", "MANAGE_ORDERS"),
        "MEMBER", List.of()
    );

    /**
     * Phase 3 (2026.3.3) — role-naming reconciliation: the Excel workbook's
     * {@code User Roles} sheet names 15 roles (ROLE-001..015) with no
     * App 01/06 column, using vocabulary this codebase never adopted
     * (Customer Owner, Customer Admin, End User, Platform Admin, Security
     * Admin, etc.) instead of ADMIN/ORG_ADMIN/MEMBER. Per the master
     * instruction ("do not blindly rename existing roles"), nothing here is
     * renamed — these three names are load-bearing (Keycloak client-role
     * claim for ADMIN, the fixed {@code OrgRole} enum for the other two;
     * see RoleAdminService's own javadoc). What IS safe and done here is
     * recording the mapping as each seeded role's own description, so the
     * correspondence is documented rather than silently lost — most of the
     * other 12 Excel roles (Provider Admin, Publisher, Support Agent, AI
     * Agent, Finance Admin, Compliance Auditor, Partner Manager, Developer,
     * Operator) belong to future applications (Partner Mgmt, Support,
     * Billing, Governance, AI Advisor — all explicitly out of this sprint's
     * scope) and are deliberately NOT seeded here.
     */
    private static final Map<String, String> ROLE_DESCRIPTIONS = Map.of(
        "ADMIN", "Platform administrator. Corresponds to the source workbook's ROLE-011 'Platform Admin'.",
        "ORG_ADMIN", "Organization administrator. Corresponds to the source workbook's ROLE-001 'Customer Owner' / ROLE-002 'Customer Admin'.",
        "MEMBER", "Regular organization member. Corresponds to the source workbook's ROLE-006 'End User'."
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seed(PLATFORM_ROLES, RoleScope.PLATFORM);
        seed(ORGANIZATION_ROLES, RoleScope.ORGANIZATION);
    }

    private void seed(Map<String, List<String>> roles, RoleScope scope) {
        roles.forEach((roleName, permissionNames) -> {
            Role role = roleRepository.findByName(roleName).orElseGet(() -> {
                Role created = new Role();
                created.setName(roleName);
                created.setScope(scope);
                return created;
            });
            if (role.getDescription() == null) {
                role.setDescription(ROLE_DESCRIPTIONS.get(roleName));
            }
            for (String permissionName : permissionNames) {
                Permission permission = permissionRepository.findByName(permissionName).orElseGet(() -> {
                    Permission created = new Permission();
                    created.setName(permissionName);
                    return permissionRepository.save(created);
                });
                role.getPermissions().add(permission);
            }
            roleRepository.save(role);
        });
    }
}
