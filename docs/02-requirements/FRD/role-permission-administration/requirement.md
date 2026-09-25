# REQ-IAM-003 — Role and Permission Administration

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-25

| Field | Value |
|---|---|
| Sprint | [2026.3.3](../../../01-business/roadmap/sprints/SPRINT-2026.3.3.md) |
| Requirement ID | REQ-IAM-003 |
| Application | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md) |
| Application code | `APP-IAM` ([DN-5](../../../01-business/roadmap/open-decisions.md#dn-5-application-codes)) |
| Priority | P0: the application is MVP scope ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No. This feature makes no use of AI (proposed; confirmed when the FRD is approved, C12) |

## Source functions
Workbook functions from the sprint and application pages that this FRD covers:

| Function ID | Function | Application page |
|---|---|---|
| 06.02.01.01 | Create role | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md#feature-060201-rbac) |
| 06.02.01.02 | Define permission | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md#feature-060201-rbac) |

## Summary
A platform administrator can create, view, edit and delete roles, and create, view, edit and delete permissions, including which permissions each role grants. The backend endpoints exist; this feature adds the admin pages under the existing `/admin` layout.

## Actors
- Platform administrator (holding `MANAGE_ROLES` and/or `MANAGE_PERMISSIONS`)

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-IAM-003.1 | The administrator can list roles with name, scope, description, permissions and whether the role is system-managed. | P0 |
| REQ-IAM-003.2 | The administrator can create a role with a name, scope (`PLATFORM` or `ORGANIZATION`), description and permissions. | P0 |
| REQ-IAM-003.3 | The administrator can edit a role's description and permission set. | P0 |
| REQ-IAM-003.4 | The administrator can delete a role that is not system-managed. | P0 |
| REQ-IAM-003.5 | The administrator can list permissions with name, description, whether it is system-managed, and how many roles grant it. | P0 |
| REQ-IAM-003.6 | The administrator can create a permission with a name and description, edit its description, and delete it when allowed. | P0 |
| REQ-IAM-003.7 | When the backend refuses an action, its message is shown. | P0 |

## Out of scope
- Deciding who holds a PLATFORM role (controlled by the Keycloak client-role claim)
- Organization roles other than `ORG_ADMIN` and `MEMBER`
- Assigning roles to organization members (see `member-role-assignment`)

## Dependencies
- Existing endpoints under `/admin/roles` and `/admin/permissions`.
- Existing admin layout (`AdminLayout`, `RequireAdmin`).
