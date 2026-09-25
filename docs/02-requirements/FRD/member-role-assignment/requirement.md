# REQ-IAM-002 — Member Role Assignment

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-25

| Field | Value |
|---|---|
| Sprint | [2026.3.3](../../../01-business/roadmap/sprints/SPRINT-2026.3.3.md) |
| Requirement ID | REQ-IAM-002 |
| Application | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md) |
| Application code | `APP-IAM` ([DN-5](../../../01-business/roadmap/open-decisions.md#dn-5-application-codes)) |
| Priority | P0: the application is MVP scope ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No. This feature makes no use of AI (proposed; confirmed when the FRD is approved, C12) |

## Source functions
Workbook functions from the sprint and application pages that this FRD covers:

| Function ID | Function | Application page |
|---|---|---|
| 06.02.01.03 | Assign role | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md#feature-060201-rbac) |

## Summary
An organization administrator can see the members of their organization and change a member's organization role between `ORG_ADMIN` and `MEMBER`. The backend endpoints exist; this feature adds the member list and role selector in the UI.

## Actors
- Organization administrator (holding `MANAGE_USERS`)
- Organization member (the target whose role changes)

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-IAM-002.1 | The administrator can list the members of their own organization with name, email, role and status. | P0 |
| REQ-IAM-002.2 | The administrator can change a member's organization role to `ORG_ADMIN` or `MEMBER`. | P0 |
| REQ-IAM-002.3 | When the backend refuses a change (for example, demoting the last administrator), its message is shown. | P0 |

## Out of scope
- Any organization role other than `ORG_ADMIN` and `MEMBER`
- Inviting, suspending or removing members (App 05 Customer / Tenant Management, sprint 2026.4.2)
- Platform `ADMIN` role assignment, which is controlled in Keycloak

## Dependencies
- Existing endpoints `GET /organization/me/users` and `PATCH /organization/me/members/{memberId}/role`.
