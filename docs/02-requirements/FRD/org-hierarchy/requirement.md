# REQ-TEN-006 — Organization hierarchy

**Status:** Approved (2026-10-07, [C82](../../../01-business/roadmap/open-decisions.md#c82))
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-10-07 ("Approved, use your recommendations")
**Decisions:** [C82](../../../01-business/roadmap/open-decisions.md#c82)

| Field | Value |
|---|---|
| Sprint | [2026.4.2](../../../01-business/roadmap/sprints/SPRINT-2026.4.2.md) (added by C82; dates unchanged) |
| Requirement ID | REQ-TEN-006 |
| Application | [05 Customer & Tenant Management](../../../01-business/roadmap/applications/05-customer-tenant-management.md) |
| Origin | The org-hierarchy of the Thittam (Vyoog PMS) product, adapted to EIS (organization isolation by `organization_id`, MUI, i18n) |
| Priority | P1 |

## Summary
An organization administrator models how the organization is structured: a single tree of **nodes** (for example Organization, Division, Business unit, Department, Location, Cost center, Team). The tree enforces a configurable **level order** (a child is always on a lower level than its parent), keeps a **history** of every move, can be loaded in bulk from a **CSV** file, and lets members be **placed** on a node. It is a structural layer only: it does not grant permissions, change subscriptions or replace groups (REQ-TEN-003).

## Actors
- **Organization administrator** (`MANAGE_ORGANIZATION`): the only actor; sees and changes the whole tree of their own organization.

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-TEN-006.1 | Each organization has exactly one tree. Its single **root** node (type Organization) is created automatically the first time the tree is opened, named after the organization. | Must |
| REQ-TEN-006.2 | A node has a name (max 150), a type, an optional code (max 50) and description (max 1000), a sort order and an active flag. Names are unique among the children of one parent (case-insensitive). | Must |
| REQ-TEN-006.3 | **Level order:** the organization has an ordered list of level types (default Organization, Division, Business unit, Department, Location, Cost center, Team). A child must be on a lower level than its parent; changing a node's type must keep it above its children. Types outside the list are refused. | Must |
| REQ-TEN-006.4 | **Level configuration:** the administrator can rename, add, remove and reorder the level types. A type used by a node cannot be removed; the first level (the root's) stays Organization. | Must |
| REQ-TEN-006.5 | **Move:** a node (not the root) can be moved under another node. A node cannot become its own parent or move under one of its descendants; the level order must still hold for the node and its whole subtree. Each move is written to the node's history (previous parent, new parent, who, when). | Must |
| REQ-TEN-006.6 | **Deactivate:** a node with children or placed members is deactivated only after the administrator confirms ("force"). Deactivating does not change children or members. The root cannot be deactivated. | Must |
| REQ-TEN-006.7 | **Delete:** a node can be deleted only when it has no children and no placed members. The root cannot be deleted. | Must |
| REQ-TEN-006.8 | **Place members:** an administrator places an active or inactive member of the same organization on one node (a member has at most one home node) and can remove the placement. A member of another organization is never accepted. | Must |
| REQ-TEN-006.9 | **CSV import:** a CSV with columns `name`, `type` (required), `code`, `description`, `parentName` (optional; blank = directly under the root). Rows are processed top to bottom through the same rules as creating a node; a bad row is skipped and reported (row number and reason) while good rows are created. Maximum 1,000 rows and 1 MB. | Should |
| REQ-TEN-006.10 | **History:** the administrator can open a node's move history, newest first. | Should |
| REQ-TEN-006.11 | Every change (create, update, move, activate/deactivate, delete, import, level change, member placement) is audited. | Must |
| REQ-TEN-006.12 | Everything is limited to the caller's organization; another organization's node answers "not found" (NFR-003). | Must |
| REQ-TEN-006.13 | The screen (People & structure → Org structure) shows the hierarchy as an org chart (cards with connector lines, expand/collapse badges, zoom, pan and minimap) with search, type filter and active-only filter, a details panel (type, code, path, members, history), actions per node, a level-configuration dialog and CSV import with the file format and a downloadable sample. All text uses i18n (English and Spanish), has keyboard access and passes the accessibility checks. | Must |

## Out of scope (not built)
See "Not specified" in [C82](../../../01-business/roadmap/open-decisions.md#c82): role assignments at a node, the org-chart canvas, teams and allocations, project hierarchy, member read access, multiple roots, the working calendar per node.

## Dependencies
- REQ-TEN-001 organization and members; REQ-TEN-003 groups (unchanged, independent).
- Audit module; permission `MANAGE_ORGANIZATION`.
