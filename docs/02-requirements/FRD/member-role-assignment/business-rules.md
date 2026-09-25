# Business rules — Member Role Assignment

These rules are **already enforced by the backend**. They are documented here from the code, not newly defined. The cited class and method are the source of truth.

| ID | Rule | Enforced in | Source |
|---|---|---|---|
| BR-IAM-002.1 | Listing the organization's members requires `MANAGE_USERS` (seeded for `ORG_ADMIN`), or an active ORGANIZATION-scope grant for it. Otherwise 403 "You do not have permission to do this". | backend | `OrganizationSelfService#listMyOrgUsers` → `#requirePermission` |
| BR-IAM-002.2 | Changing a role requires `MANAGE_USERS` **and** the target member must belong to the caller's organization. A wrong role and a cross-organization target return the same generic 403 "You do not have permission to do this", so the backend never reveals whether a member id exists elsewhere. | backend | `OrganizationSelfService#changeMemberRole` → `#requirePermissionOnMember`, `AuthorizationService#evaluate` |
| BR-IAM-002.3 | An active ORGANIZATION-scope privileged-access grant for `MANAGE_USERS` also allows the change, but only within the caller's own organization. | backend | `OrganizationSelfService#requirePermissionOnMember` |
| BR-IAM-002.4 | The only organization roles are `ORG_ADMIN` and `MEMBER`. `orgRole` is required. | backend | `OrgRole` enum, `ChangeMemberRoleRequest` (`@NotNull`) |
| BR-IAM-002.5 | The organization's last active `ORG_ADMIN` cannot be demoted: 400 "Cannot remove the organization's last administrator." | backend | `OrganizationSelfService#changeMemberRole` |
| BR-IAM-002.6 | An unknown member id returns 404 "Member not found". | backend | `OrganizationSelfService#requirePermissionOnMember` |
| BR-IAM-002.7 | After a change, the target member is notified ("Your organization role changed" / "Your role in your organization is now <role>.", category ORGANIZATION, severity INFO). | backend | `OrganizationSelfService#changeMemberRole` → `NotificationService#notify` |
| BR-IAM-002.8 | Every change is recorded in the audit log as `MEMBER_ROLE_CHANGED`. | backend | `OrganizationSelfService#changeMemberRole` → `AuditService#recordSuccess` |
| BR-IAM-002.9 | The caller must be an ACTIVE member of an organization. Otherwise the request fails with 404 "You are not a member of an organization". | backend | `OrganizationSelfService#resolveMembership` |
| BR-IAM-002.10 | The caller's organization must be in good standing. Otherwise the request fails with 403 "Your organization's account is not currently active". | backend | `OrganizationSelfService#resolveMembership`, `AuthorizationService#organizationInGoodStanding` |
| BR-IAM-002.11 | The organization is always the caller's own, resolved from the JWT. No organization id is accepted from the client. | backend | `OrganizationController` class javadoc, `OrganizationSelfService` class javadoc |
