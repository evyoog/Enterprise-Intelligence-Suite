# REQ-TEN-004 — Tenant Lifecycle

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-27 ([C37](../../../01-business/roadmap/open-decisions.md#c37))

| Field | Value |
|---|---|
| Sprint | [2026.4.2](../../../01-business/roadmap/sprints/SPRINT-2026.4.2.md), carried from [2026.4.1](../../../01-business/roadmap/sprints/SPRINT-2026.4.1.md) |
| Requirement ID | REQ-TEN-004 |
| Application | [05 Customer / Tenant Management](../../../01-business/roadmap/applications/05-customer-tenant-management.md) |
| Application code | `APP-TEN` |
| Priority | P0 ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source functions
| Function ID | Function | Application page |
|---|---|---|
| 05.02.01.03 | Assign region | [05 Customer / Tenant Management](../../../01-business/roadmap/applications/05-customer-tenant-management.md#feature-050201-tenant-lifecycle) |
| 05.02.01.05 | Configure tenant policies | same |

05.02.01.01 Create tenant, 05.02.01.02 Configure tenant and 05.02.01.04 Configure isolation are **not** built as new code — see C37: each is satisfied by existing organization registration/edit flows and the platform's existing tenant-isolation architecture.

## Summary
On the same admin-edit form used for REQ-TEN-001 (organization details), a platform administrator can assign an organization to one of the regions defined in [platform-administration](../platform-administration/requirement.md), and toggle whether that organization's seat limit can be exceeded.

## Actors
- Platform administrator (holding `MANAGE_REGISTRATIONS`, the existing gate on `/admin/registrations/**`)

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-TEN-004.1 | An admin can assign an organization to an existing region, or clear the assignment (unassigned). | P0 |
| REQ-TEN-004.2 | Assigning a region that does not exist is refused. | P0 |
| REQ-TEN-004.3 | An admin can set `allowSeatOverage` on an organization. | P0 |
| REQ-TEN-004.4 | When `allowSeatOverage` is true, adding a member to that organization is never blocked by its licensed seat count. | P0 |
| REQ-TEN-004.5 | `isOverLimit` still reports the organization as over its licensed seats when applicable, even with the override on — the override changes what is *blocked*, not what is *reported*. | P0 |

## Out of scope
- A separate Tenant entity distinct from Organization (C37 — "tenant" is Organization)
- Any new tenant-isolation mechanism (already satisfied by existing `organization_id` scoping, C37)
- A fixed region list, region hierarchy, or per-region configuration beyond name and enabled state (belongs to [platform-administration](../platform-administration/requirement.md))
- Any further tenant policy beyond seat overage (Not specified; not invented)

## Dependencies
- Existing `Organization`, `OrganizationMemberService#assertSeatAvailable`, `AdminRegistrationService#updateOrganization`.
- [platform-administration](../platform-administration/requirement.md)'s `PlatformRegion`.
- New columns `organization.region_id`, `organization.allow_seat_overage` ([V007](../../../../database/migrations/V007__platform_administration_tenant_lifecycle.sql)).
