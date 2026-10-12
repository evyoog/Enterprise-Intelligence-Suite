# REQ-TEN-009 — Projects

**Status:** Approved (2026-10-12, [C88](../../../01-business/roadmap/open-decisions.md#c88))
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-10-12 (answers PR-1 A and PR-2 A, then "start develop")

| Field | Value |
|---|---|
| Sprint | [2026.4.1](../../../01-business/roadmap/sprints/SPRINT-2026.4.1.md) (carried to 2026.4.2 by [C35](../../../01-business/roadmap/open-decisions.md#c35), pulled back by C88) |
| Requirement ID | REQ-TEN-009 |
| Application | [05 Customer & Tenant Management](../../../01-business/roadmap/applications/05-customer-tenant-management.md#feature-050402-projects) |
| Functions | 05.04.02.01 Create project, 05.04.02.02 Assign users, 05.04.02.03 Assign resources |
| Priority | P0 |

## Summary
A **project** is a named workspace inside one organization. It is a **group** of the organization's members (PR-2 A) **plus products** (PR-1 A): the only "resource" a project can own is access to a product the organization already subscribes to. Every member of the project can use the project's products with the role the project gives. Organization administrators and members with `MANAGE_USERS` (people) or `MANAGE_PRODUCT_ACCESS` (products) manage projects.

## Actors
- **Organization administrator** (`ORG_ADMIN`): everything below.
- **Member with `MANAGE_USERS`**: creates, renames and deletes projects, adds and removes members.
- **Member with `MANAGE_PRODUCT_ACCESS`**: adds products to a project, changes the role, removes products.
- **Project member**: gains the project's products as product access (the existing two-tier view shows it).

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-TEN-009.1 | **Create project (05.04.02.01):** name (required, up to 150 characters, unique among the organization's projects, ignoring case) and optional description (up to 500 characters). A project is stored as an organization group of kind `PROJECT`. | Must |
| REQ-TEN-009.2 | **Assign users (05.04.02.02):** add and remove active members of the same organization. Adding twice does nothing. Requires `MANAGE_USERS`. | Must |
| REQ-TEN-009.3 | **Assign resources (05.04.02.03):** add a product with a product role, change the role, remove the product. The product must be Active in the catalog and the organization must hold an Active subscription for it (the same rule as a direct grant). Requires `MANAGE_PRODUCT_ACCESS`. | Must |
| REQ-TEN-009.4 | **Effective access:** every Active member of a project holds product access (`organization_product_access`) for each product of the project, with the project's role, recorded as coming from that project. Joining a project grants it, leaving or removing the product or deleting the project takes it back. | Must |
| REQ-TEN-009.5 | **Direct access wins:** an Active direct assignment (made by an administrator for that member and product) is never changed by a project. A direct assignment made over project-granted access turns it into a direct assignment. A direct revoke of access that comes from a project is refused with a pointer to the project. | Must |
| REQ-TEN-009.6 | **Several projects:** when a member gets the same product from more than one project, one access row is kept; removing one source leaves the access in place from another source, with that source's role. | Must |
| REQ-TEN-009.7 | **Notice:** a member who newly gains access to a product through a project gets an in-app notification (and an email unless muted): "Access granted: <product>" naming the project. | Should |
| REQ-TEN-009.8 | **Delete project:** removes its member list and product list and takes back the access it granted. History stays in the audit log. | Must |
| REQ-TEN-009.9 | **Plain groups unchanged:** the Groups card and its endpoints list and change only groups of kind `GROUP`; a project is managed only through Projects. | Must |
| REQ-TEN-009.10 | **Screens:** Projects tab in People & structure (list, create, rename, delete), a project panel with Members and Products, English and Spanish, accessible. | Must |
| REQ-TEN-009.11 | **Audit:** project created, renamed, deleted, member added and removed, product added, changed and removed; product access granted or revoked because of a project. | Must |
| REQ-TEN-009.12 | Several organizations per person, project-level roles other than the product role, project-owned data and project billing are not part of this requirement. | Out of scope |

## Out of scope (this sprint)
Resources other than product access (files, workspaces, data), project-specific permissions, nested projects, project owners with their own authority, project billing or cost centres, bulk import, a member-facing "My projects" screen, platform-administrator editing.

## Dependencies
REQ-TEN-003 (groups), REQ-TEN-002 (member lifecycle), REQ-TEN-005 slice (`MANAGE_PRODUCT_ACCESS`), REQ-SUB-001/003 (organization subscriptions), catalog (products), notifications, audit. REQ-INT-003 will carry the effective product access to the tools; nothing special is needed for projects.
