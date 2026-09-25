# REQ-<APP-CODE>-<NNN> — Role and Permission Administration

**Status:** Draft
**BRD:** Not specified
**Owner:** Not specified
**Approved by / on:** Not specified / Not specified

| Field | Value |
|---|---|
| Sprint | [2026.3.3](../../../01-business/roadmap/sprints/SPRINT-2026.3.3.md) |
| Application | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md) |
| Application code | Not assigned. `<APP-CODE>` is left as-is; application codes are an open decision ([open-decisions.md](../../../01-business/roadmap/open-decisions.md)) |

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
| REQ-<APP-CODE>-<NNN>.1 | The administrator can list roles with name, scope, description, permissions and whether the role is system-managed. | Not specified |
| REQ-<APP-CODE>-<NNN>.2 | The administrator can create a role with a name, scope (`PLATFORM` or `ORGANIZATION`), description and permissions. | Not specified |
| REQ-<APP-CODE>-<NNN>.3 | The administrator can edit a role's description and permission set. | Not specified |
| REQ-<APP-CODE>-<NNN>.4 | The administrator can delete a role that is not system-managed. | Not specified |
| REQ-<APP-CODE>-<NNN>.5 | The administrator can list permissions with name, description, whether it is system-managed, and how many roles grant it. | Not specified |
| REQ-<APP-CODE>-<NNN>.6 | The administrator can create a permission with a name and description, edit its description, and delete it when allowed. | Not specified |
| REQ-<APP-CODE>-<NNN>.7 | When the backend refuses an action, its message is shown. | Not specified |

## Out of scope
- Deciding who holds a PLATFORM role (controlled by the Keycloak client-role claim)
- Organization roles other than `ORG_ADMIN` and `MEMBER`
- Assigning roles to organization members (see `member-role-assignment`)

## Dependencies
- Existing endpoints under `/admin/roles` and `/admin/permissions`.
- Existing admin layout (`AdminLayout`, `RequireAdmin`).
