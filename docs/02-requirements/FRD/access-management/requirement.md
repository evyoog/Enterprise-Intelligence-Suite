# REQ-TEN-005 — Access management

**Status:** Approved (2026-10-09, [C86](../../../01-business/roadmap/open-decisions.md#c86), answer Q11 "approve as drafted")
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-10-09
**Decision:** [C65](../../../01-business/roadmap/open-decisions.md#c65) (answer to D16, options A and B combined)
**Built:** Not built (approved 2026-10-09; scheduled with the sprint the product owner sets). Functional test cases: written after approval in `test-cases/functional/access-management/`; UAT: [access-management UAT scripts](../../../../test-cases/UAT/access-management/README.md).

| Field | Value |
|---|---|
| Sprint | [2026.4.2](../../../01-business/roadmap/sprints/SPRINT-2026.4.2.md) (application 05); the organization-subscription part (09.02.01) also in [2027.1.1](../../../01-business/roadmap/sprints/SPRINT-2027.1.1.md) |
| Requirement ID | REQ-TEN-005 |
| Application | [05 Customer / Tenant Management](../../../01-business/roadmap/applications/05-customer-tenant-management.md) |
| Application code | `APP-TEN` |
| Priority | P0 |
| AI required | No |

## Source functions
| Function ID | Function | Covered here |
|---|---|---|
| 05.03.02.01 | Assign role | Yes: role selector in the member access panel (existing REQ-IAM-002 behaviour kept) |
| 05.03.02.02 | Assign group | No change: groups stay on the existing groups card (REQ-TEN-003); whether groups carry access is Open question 5 |
| 05.03.02.03 | Review access | Yes: "Mark as reviewed" and "last reviewed" (existing REQ-TEN-002.6 stamp) (.7) |
| 06.02.01.03 | Assign role | Yes, as 05.03.02.01 |
| 06.02.01.04 | Evaluate permission | Yes: enforcement through effective access (.10) |
| 09.02.01.02 | Configure service | Yes, *who* may change plan and seats of an organization subscription (.4) |
| 09.02.01.04 | Suspend service | Yes, *who* may suspend or reactivate an organization subscription (.4) |
| 09.02.01.05 | Deprovision service | Yes, *who* may cancel an organization subscription (.4). Provisioning itself is [REQ-ORD-002](../provisioning-contract/requirement.md) |

## Summary
Organization admins decide what each member can do in EIS (**feature permissions**) and which of the organization's subscribed products each member can open (**product access**). Every organization role has **role defaults**; an admin or a **delegated administrator** can give one member an **individual override**. What is enforced is the **effective access**: role defaults with overrides applied. Organization admins manage the organization's shared subscriptions by default, and can delegate this with the new *Manage subscriptions* permission ([C65](../../../01-business/roadmap/open-decisions.md#c65)). Everything is managed on one **Access management** screen; the platform's separate Roles and Permissions pages become one **Roles & permissions** screen.

## Inventory (as of 2026-10-03, `dev`)

### Organization-scope permissions today
Seeded by `RbacSeeder` on the `ORG_ADMIN` role (the `MEMBER` role has none). A permission is organization-scope because it is attached to an organization-scope role; the `permission` table has no scope column. Checked by `AuthorizationService.hasOrganizationPermission` / `evaluate`, and also satisfied by an active organization privileged-access grant (REQ-IAM-004).

| Code | What it allows today | Where it is checked |
|---|---|---|
| `MANAGE_ORGANIZATION` | Organization settings and running the organization: MFA policy; SAML and OIDC sign-in providers and claim mapping; business dashboard; organization audit log; organization billing (details, invoices, payments — [C47](../../../01-business/roadmap/open-decisions.md#c47) default); organization subscriptions list and **seat changes** (REQ-SUB-003) | `OrganizationSelfService.requireOrganizationManagement`, `BillingOwnerResolver`, `OrganizationSamlProviderController`, `OrganizationOidcProviderController`, `OrganizationAuditLogController`, `BusinessDashboardController`, `SubscriptionSeatService` |
| `MANAGE_USERS` | List members; change a member's role (with the last-admin guard); suspend, reactivate, remove members; mark access reviewed; reset a member's two-factor authentication; create and delete groups and change group members | `OrganizationSelfService` |
| `MANAGE_PRODUCT_ACCESS` | See, grant and revoke one member's access to one of the organization's products (with a product role) | `OrganizationSelfService` (`/organization/me/members/{id}/products`) |
| `MANAGE_PRIVILEGED_ACCESS` | Approve, reject and revoke members' temporary privileged-access requests in the organization. It can never itself be requested (the self-escalation guard); nobody approves their own request | `OrganizationSelfService`, `PrivilegedAccessService` |
| `MANAGE_ORDERS` | Approve and reject members' orders (REQ-ORD-001) | `OrderService` |

Notes found during the inventory:
- **Organization subscriptions** have **no** suspend, reactivate, cancel or change-plan action today (only individual subscriptions do, REQ-SUB-001 — the gap [C38](../../../01-business/roadmap/open-decisions.md#c38)/[C39](../../../01-business/roadmap/open-decisions.md#c39) carry). Seat changes exist and use `MANAGE_ORGANIZATION`.
- **Self-change:** the existing self-escalation guard covers privileged access (cannot request `MANAGE_PRIVILEGED_ACCESS`, cannot approve one's own request). Changing one's **own role** is not blocked today except by the last-admin rule; this FRD extends the guard to the caller's own role and access (BR-3).
- `MANAGE_ORDERS` is not in `PermissionAdminService`'s list of protected (undeletable) permissions, unlike the other four.
- The code `MANAGE_BILLING` already exists as a **platform** permission (platform billing administration). The organization *Manage billing* permission therefore needs a code that cannot be confused with it (Open question 2).

### Screens today
| Screen / component | Where | What it does with roles, permissions or access |
|---|---|---|
| `OrganizationMembersCard` | Organization settings (`/organization/settings`; on the business dashboard until C69) | Member list with a role selector (REQ-IAM-002), status actions, Mark reviewed, Reset 2FA (`MANAGE_USERS`) |
| `OrganizationGroupsCard` | Organization settings | Groups and their members (REQ-TEN-003, `MANAGE_USERS`) |
| `OrganizationPrivilegedAccessCard` | Organization settings | Pending and active organization privileged-access requests (REQ-IAM-004, `MANAGE_PRIVILEGED_ACCESS`) |
| `PrivilegedAccessRequestsCard` | Account → Security | A member requests temporary elevated access |
| `RolesAdminPage` | `/admin/roles` | Platform admin: create, edit, delete roles and their permissions (REQ-IAM-003) |
| `PermissionsAdminPage` | `/admin/permissions` | Platform admin: permission catalogue — create, edit, delete (REQ-IAM-003) |
| `AdminPrivilegedAccessPage` | `/admin/privileged-access` | Platform-scope privileged access |
| Sidebar (`appNavigation.ts`) | — | Shows organization items from the caller's organization permissions (`/me/permissions`) |

No screen uses the per-member product access API today: product access can be granted only through the API.

### Product access today
- **Organization level:** the organization's subscriptions (`product_subscription`, owner = organization). Entitlements are derived from ACTIVE subscriptions ([C52](../../../01-business/roadmap/open-decisions.md#c52)).
- **Member level:** `organization_product_access` rows (member, product, **product role** — a free-text, product-specific role such as `PMS_USER`, status ACTIVE/INACTIVE). A row is what "assigned" means; membership alone never implies access. Grants are refused for products the organization does not subscribe to and for products the catalogue marks inactive. Revoking flips the row to INACTIVE.
- **Use:** the "my products" two-tier view and the business dashboard (unused product access alerts). There is no catalogue of valid product roles per product (Open question 7).
- **Seats:** seats in use = the organization's ACTIVE members (pool default, [REQ-SUB-003](../subscription-seats/requirement.md) Open question 3); product access does not consume seats today.

## Concepts and terms
These words are used everywhere: UI, documents and i18n keys (`access.*`).

| Term | Definition |
|---|---|
| **Organization role** | The existing roles *Organization admin* (`ORG_ADMIN`) and *Member* (`MEMBER`). Custom organization roles are not added (Open question 4). |
| **Feature permissions** | What a member can do in EIS, grouped by area. Shown with plain-language names and one-line descriptions; the code appears only in a "technical name" tooltip. |
| **Product access** | Which of the organization's entitled products (ACTIVE subscriptions, [C52](../../../01-business/roadmap/open-decisions.md#c52)) a member can open. |
| **Role defaults** | The feature permissions and product access every member of a role gets automatically. |
| **Individual override** | A grant or removal for one member that differs from their role defaults. |
| **Effective access** | Role defaults with individual overrides applied. This is what is enforced. |
| **Delegated administrator** | A member who holds *Manage access* (and is not an organization admin). |

### Feature permission catalogue (organization scope)
Existing permissions keep their codes. New permissions are only the three named in [C65](../../../01-business/roadmap/open-decisions.md#c65).

| Area | Name | Code | Description | Status |
|---|---|---|---|---|
| Members & access | Manage members | `MANAGE_USERS` | Change roles, suspend, reactivate, remove, review access, reset two-factor, manage groups | Existing |
| Members & access | Manage product access | `MANAGE_PRODUCT_ACCESS` | Give or remove members' access to the organization's products | Existing |
| Members & access | **Manage access** | `MANAGE_ACCESS` | Manage other members' feature permissions, product access and role defaults (delegation) | **New** |
| Subscriptions | **Manage subscriptions** | `MANAGE_SUBSCRIPTIONS` | Suspend, reactivate, cancel, change plan and change seats of the organization's subscriptions | **New** (D16) |
| Orders | Approve orders | `MANAGE_ORDERS` | Approve or reject members' orders | Existing |
| Billing | **Manage billing** | Open question 2 | The organization's billing details, invoices and payments | **New — proposed, confirm** |
| Security & sign-in | Approve privileged access | `MANAGE_PRIVILEGED_ACCESS` | Approve, reject and revoke members' temporary elevated access | Existing |
| Organization settings | Manage organization | `MANAGE_ORGANIZATION` | Two-factor policy, single sign-on providers, business dashboard, organization audit log (and, until *Manage billing* is confirmed, billing) | Existing |

Whether `MANAGE_ORGANIZATION` should be split into smaller permissions (sign-in, audit, dashboard) is Not specified (Open question 8). Seat changes move from `MANAGE_ORGANIZATION` to *Manage subscriptions* (.4).

## Actors
- **Organization admin** (`ORG_ADMIN`): always holds every organization feature permission.
- **Delegated administrator:** a member holding *Manage access*.
- **Member:** has their effective access.
- **Platform administrator** (`MANAGE_ROLES`, `MANAGE_PERMISSIONS`): defines platform and organization roles and the permission catalogue (REQ-IAM-003).

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-TEN-005.1 | **Automatic access from role:** when a member joins, or their role changes, their effective access is recalculated automatically from the role defaults. Existing individual overrides are kept. The change appears in the audit log. | Must |
| REQ-TEN-005.2 | **Role defaults:** an organization admin or delegated administrator can set, for each organization role, which feature permissions and which products are on by default. *Organization admin* always has all organization feature permissions, shown locked ON. Initial role defaults: Open questions 1 and 9. | Must |
| REQ-TEN-005.3 | **Individual overrides:** for one member, an organization admin or delegated administrator can turn any feature permission or product on or off, overriding the role default, and can **Reset to role default** for one item or all items. | Must |
| REQ-TEN-005.4 | **D16:** organization subscription actions — suspend, reactivate, cancel, change plan, change seats — require *Manage subscriptions*. By default only organization admins hold it. Anyone holding *Manage access* can grant it to another member (within the guard rails). | Must |
| REQ-TEN-005.5 | **Product access limits:** only products in the organization's ACTIVE subscriptions can be granted ([C52](../../../01-business/roadmap/open-decisions.md#c52)). If granting product access consumes a seat (Open question 3, [C63](../../../01-business/roadmap/open-decisions.md#c63)), a grant is refused when no seat is free, and the toggle is disabled with the reason. | Must |
| REQ-TEN-005.6 | **Guard rails** (each shown as a disabled toggle with the reason): (a) a delegated administrator cannot grant a feature permission they do not hold; (b) only organization admins can change another organization admin's access or role; (c) nobody can change their own permissions, product access or role; (d) the last organization admin cannot be demoted (existing rule); (e) organization admins' feature permissions are locked ON. | Must |
| REQ-TEN-005.7 | **Review access:** a member's access can be marked as reviewed (existing REQ-TEN-002.6 stamp); the screen shows when each member was last reviewed and by whom. | Must |
| REQ-TEN-005.8 | **Bulk changes:** select several members and grant or remove one product or one feature permission, with a preview and a summary confirmation ("Grant Product X to 8 members — 2 skipped: no seats"). Each skipped member shows the reason. | Should |
| REQ-TEN-005.9 | **Audit:** every change records who, whom, what, from what value to what value, and why it was allowed (role default or override, and the actor's authority). Role-default changes record the role and the number of affected members. | Must |
| REQ-TEN-005.10 | **Enforcement:** every organization permission check uses effective access (and, as today, an active privileged-access grant also satisfies it). Members with no overrides keep exactly today's behaviour while role defaults equal today's role permissions. | Must |
| REQ-TEN-005.11 | **Platform Roles & permissions:** the platform admin's Roles and Permissions pages become one screen with the same functions and validation messages (REQ-IAM-003). | Must |

## Out of scope
- Custom organization roles (Open question 4).
- Access carried by groups (Open question 5; groups carry none today, [C35](../../../01-business/roadmap/open-decisions.md#c35)).
- Notifications to members about access changes (Open question 6) — the existing role-change notification stays.
- Product-internal permissions (what a member can do inside Valam.ai etc.); only which products they can open.
- Invitations (05.03.01): [REQ-TEN-008](../invite-user/requirement.md) (C84) builds one slice of this FRD for `INVITE_USERS`: the table `member_access_override` with permission items only.

## Dependencies
REQ-IAM-002 (member role assignment), REQ-IAM-003 (role and permission administration), REQ-IAM-004 (privileged access), REQ-TEN-002 (member lifecycle, review stamp), REQ-TEN-003 (groups), REQ-SUB-001 (subscription lifecycle), REQ-SUB-002 (entitlements), REQ-SUB-003 (seats), REQ-BIL-001 (organization billing), audit.

## Where each part of this FRD lives
| Part | Location |
|---|---|
| Requirement, business rules, workflow, acceptance criteria | this folder |
| Cross-feature rule | [BR-ACC-001 Effective access](../../../03-business-rules/BR-ACC-001-effective-access.md) |
| Screens | [ui-requirements.md](ui-requirements.md) |
| API | [api-requirements.md](api-requirements.md) → [access-management.md](../../../06-api/api-requirements/access-management.md) |
| Data model | [access-management.md](../../../07-database/data-model/access-management.md) |
| UAT scripts | [test-cases/UAT/access-management](../../../../test-cases/UAT/access-management/README.md) |

## Answers applied on 2026-10-09 ([C86](../../../01-business/roadmap/open-decisions.md#c86))
The product owner approved this FRD "as drafted" (question Q11). Every open question that has a **proposed** answer below takes that proposal; the two blocking questions that had no proposal take the defaults in the table. The product owner can change any of them.

| Open question | Applied answer |
|---|---|
| 1 Member role defaults | As proposed: all entitled products ON, no management permissions. |
| 2 Manage billing | As proposed: a separate permission `MANAGE_ORGANIZATION_BILLING`. |
| 3 Does product access consume a seat? | **Default (no proposal existed): no.** A seat is used by each ACTIVE member (the pool model of [REQ-SUB-003](../subscription-seats/requirement.md)); granting product access does not use a second seat. |
| 7 Product role | **Default (no proposal existed):** each product's catalog entry lists its valid product roles (for example `PMS_ADMIN`, `PMS_MANAGER`, `PMS_USER`) with one **default role** (the least-privileged); turning a product ON grants the default role, and an administrator may choose another valid role on the screen. The platform stores the role; each tool maps it to its own roles ([REQ-INT-003](../platform-tool-sync/requirement.md).14). |
| 4, 5, 6, 8, 9, 10 | As proposed where a proposal is written; otherwise unchanged (they did not block approval). |

## Open questions (answered above where marked)
| # | Question | Blocks approval |
|---|---|---|
| 1 | **Member role defaults:** what product access and feature permissions do *Members* get by default? Proposed — confirm: all entitled products ON, no management permissions. | Yes |
| 2 | **Manage billing:** a separate permission, or part of *Manage subscriptions*? If separate, its code (the platform already uses `MANAGE_BILLING`; proposed — confirm: `MANAGE_ORGANIZATION_BILLING`). | Yes |
| 3 | Does granting **product access** consume a **seat** (pool vs named seats, [REQ-SUB-003](../subscription-seats/requirement.md) Open question 3)? | Yes |
| 4 | Are **custom organization roles** (for example "Billing manager") wanted later, or are role defaults plus overrides enough? | No — confirm in review |
| 5 | Should **groups** (REQ-TEN-003) carry product access or permissions? Today they carry none ([C35](../../../01-business/roadmap/open-decisions.md#c35)). | No — confirm in review |
| 6 | Should members be **notified** (email or in-app) when their access changes? | No — confirm in review |
| 7 | **Product role:** each product access row carries a product-specific role (for example `PMS_USER`), but no list of valid product roles exists. Which product role does turning a product ON grant, and can an admin choose it on this screen? | Yes |
| 8 | Should `MANAGE_ORGANIZATION` be split into smaller feature permissions (sign-in and security, audit log, business dashboard), or stay one permission? | No — confirm in review |
| 9 | **Organization vs platform role definitions:** the platform admin defines which permissions the `ORG_ADMIN` and `MEMBER` roles have (REQ-IAM-003). Proposed — confirm: those are the starting role defaults of every organization, and an organization's own role defaults then apply to that organization only. | Yes |
| 10 | Does *Manage access* include *Manage product access* and *Manage members*, or are they granted separately? Proposed — confirm: separately. | No — confirm in review |
