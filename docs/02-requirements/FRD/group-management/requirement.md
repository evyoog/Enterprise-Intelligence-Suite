# REQ-TEN-003 — Group Management

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-26 ([C35](../../../01-business/roadmap/open-decisions.md#c35))

| Field | Value |
|---|---|
| Sprint | [2026.4.1](../../../01-business/roadmap/sprints/SPRINT-2026.4.1.md) |
| Requirement ID | REQ-TEN-003 |
| Application | [05 Customer / Tenant Management](../../../01-business/roadmap/applications/05-customer-tenant-management.md) |
| Application code | `APP-TEN` |
| Priority | P0 ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source functions
| Function ID | Function | Application page |
|---|---|---|
| 05.04.01.01 | Create group | [05 Customer / Tenant Management](../../../01-business/roadmap/applications/05-customer-tenant-management.md#feature-050401-groups) |
| 05.04.01.02 | Add member | same |
| 05.04.01.03 | Remove member | same |

05.04.02 Projects is **not** in this FRD — carried to sprint 2026.4.2 ([C35](../../../01-business/roadmap/open-decisions.md#c35)).

## Summary
An organization admin can create named groups (e.g. "Engineering") and add or remove their own organization's members from them, on the business dashboard. A group carries no permissions or product access of its own.

## Actors
- Organization admin (holding `MANAGE_USERS`, same-organization only)

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-TEN-003.1 | An admin can create a group, named, in their own organization. | P0 |
| REQ-TEN-003.2 | An admin can delete a group from their own organization. | P0 |
| REQ-TEN-003.3 | An admin can add a member of their own organization to a group in that organization. | P0 |
| REQ-TEN-003.4 | An admin can remove a member from a group. | P0 |
| REQ-TEN-003.5 | Adding a member already in the group is a no-op (no duplicate membership). | P0 |
| REQ-TEN-003.6 | Every group action is scoped to the caller's own organization; acting on another organization's group or member is refused. | P0 |

## Out of scope
- Projects (05.04.02), including "assign resources" — no resource model exists yet outside per-product access (C35)
- Any permission or product-access effect from group membership — a group is purely organizational grouping
- Nested groups, or a member belonging to more than one group's worth of restriction (a member may belong to any number of groups; there is no exclusivity rule)

## Dependencies
- Existing `OrganizationMember`, `OrganizationSelfService`, `MANAGE_USERS` permission.
- New tables `organization_group`, `organization_group_member` ([V006](../../../../database/migrations/V006__product_structure_plan_pricing_member_lifecycle_groups.sql)).
