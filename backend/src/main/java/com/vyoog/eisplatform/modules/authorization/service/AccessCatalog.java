package com.vyoog.eisplatform.modules.authorization.service;

import java.util.List;
import java.util.Optional;

/**
 * The organization-scope feature permissions shown on the Access management screen (REQ-TEN-005, C90): plain-language
 * name, one-line description and area. The codes are the permission names seeded by {@code RbacSeeder}. An organization
 * administrator always holds every one of them (BR-6).
 */
public final class AccessCatalog {

    public enum Area { MEMBERS_ACCESS, SUBSCRIPTIONS, ORDERS, BILLING, SECURITY, ORGANIZATION }

    public record Entry(String code, String name, String description, Area area) {
    }

    public static final String MANAGE_ACCESS = "MANAGE_ACCESS";
    public static final String MANAGE_SUBSCRIPTIONS = "MANAGE_SUBSCRIPTIONS";
    public static final String MANAGE_ORGANIZATION_BILLING = "MANAGE_ORGANIZATION_BILLING";

    public static final List<Entry> ENTRIES = List.of(
        new Entry("MANAGE_USERS", "Manage members", "Change roles, suspend, reactivate, remove, review access, reset two-factor, manage groups", Area.MEMBERS_ACCESS),
        new Entry("MANAGE_PRODUCT_ACCESS", "Manage product access", "Give or remove members' access to the organization's products one by one", Area.MEMBERS_ACCESS),
        new Entry("INVITE_USERS", "Invite members", "Send, resend and revoke invitations to join the organization", Area.MEMBERS_ACCESS),
        new Entry(MANAGE_ACCESS, "Manage access", "Manage other members' feature permissions, product access and role defaults", Area.MEMBERS_ACCESS),
        new Entry(MANAGE_SUBSCRIPTIONS, "Manage subscriptions", "Suspend, reactivate, cancel, change plan and change seats of the organization's subscriptions", Area.SUBSCRIPTIONS),
        new Entry("MANAGE_ORDERS", "Approve orders", "Approve or reject members' orders", Area.ORDERS),
        new Entry(MANAGE_ORGANIZATION_BILLING, "Manage billing", "The organization's billing details, invoices and payments", Area.BILLING),
        new Entry("MANAGE_PRIVILEGED_ACCESS", "Approve privileged access", "Approve, reject and revoke members' temporary elevated access", Area.SECURITY),
        new Entry("MANAGE_ORGANIZATION", "Manage organization", "Two-factor policy, single sign-on providers, business dashboard, organization audit log", Area.ORGANIZATION));

    private AccessCatalog() {
    }

    public static Optional<Entry> find(String code) {
        return ENTRIES.stream().filter(e -> e.code().equals(code)).findFirst();
    }

    public static boolean isOrganizationPermission(String code) {
        return find(code).isPresent();
    }
}
