# REQ-TEN-002 — Member Lifecycle & Access Review

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-26 ([C34](../../../01-business/roadmap/open-decisions.md#c34))

| Field | Value |
|---|---|
| Sprint | [2026.4.1](../../../01-business/roadmap/sprints/SPRINT-2026.4.1.md) |
| Requirement ID | REQ-TEN-002 |
| Application | [05 Customer / Tenant Management](../../../01-business/roadmap/applications/05-customer-tenant-management.md) |
| Application code | `APP-TEN` |
| Priority | P0 ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source functions
| Function ID | Function | Application page |
|---|---|---|
| 05.03.01.03 | Activate user | [05 Customer / Tenant Management](../../../01-business/roadmap/applications/05-customer-tenant-management.md#feature-050301-user-lifecycle) |
| 05.03.01.04 | Suspend user | same |
| 05.03.01.05 | Remove user | same |
| 05.03.02.03 | Review access | [same page](../../../01-business/roadmap/applications/05-customer-tenant-management.md#feature-050302-role-assignment) |

05.03.01.01 Invite user is now [REQ-TEN-008](../invite-user/requirement.md) (C84). 05.03.01.02 Create user is **not** in this FRD — carried (originally to sprint 2026.4.2 ([C34](../../../01-business/roadmap/open-decisions.md#c34); needs a new identity-creation flow)). 05.03.02.01 Assign role and 05.03.02.02 Assign group were already built (member-role-assignment FRD; `group-management` FRD respectively).

## Summary
An organization admin (or platform admin) can suspend a member (frees their seat, blocks sign-in, reversible), reactivate a suspended member (subject to the seat limit), and remove a member (the existing one-way action, now with its own endpoint and UI). They can also record that they have reviewed a member's current role and access, stamped with who and when.

## Actors
- Organization admin (holding `MANAGE_USERS`, same-organization only)
- Platform admin (via a privileged-access grant for `MANAGE_USERS`, same as the existing role-change action)

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-TEN-002.1 | An admin can suspend an active member of their own organization. | P0 |
| REQ-TEN-002.2 | An admin can reactivate a suspended member, subject to the organization's seat limit. | P0 |
| REQ-TEN-002.3 | An admin can remove a member (existing one-way action), now reachable from the members table. | P0 |
| REQ-TEN-002.4 | None of these three actions may be performed on the organization's last active administrator. | P0 |
| REQ-TEN-002.5 | A suspended member cannot sign in or use any self-service endpoint, immediately. | P0 |
| REQ-TEN-002.6 | An admin can mark a member's access as reviewed; the member record shows who reviewed it and when. | P0 |
| REQ-TEN-002.7 | Every status change and access review is audited and the affected member is notified (status changes only — reviews are silent). | P0 |

## Out of scope
- Invite user / Create user (identity creation) — carried to 2026.4.2 (C34)
- A scheduled or forced periodic access-review workflow — this is a manual, on-demand stamp only (C34)
- Assign group — see the separate `group-management` FRD

## Dependencies
- Existing `OrganizationMemberService`, `OrganizationSelfService`, `MembershipStatus`.
- New enum value `MembershipStatus.SUSPENDED`; new columns `organization_member.last_reviewed_at`, `.last_reviewed_by_customer_id` ([V006](../../../../database/migrations/V006__product_structure_plan_pricing_member_lifecycle_groups.sql)).
