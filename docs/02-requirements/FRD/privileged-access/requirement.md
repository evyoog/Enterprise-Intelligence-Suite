# REQ-<APP-CODE>-<NNN> — Privileged Access (User and Organization Administrator)

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
| 06.03.01.01 | Request elevated access | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md#feature-060301-administrative-access) |
| 06.03.01.02 | Approve access | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md#feature-060301-administrative-access) |
| 06.03.01.03 | Grant temporary access | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md#feature-060301-administrative-access) |
| 06.03.01.04 | Revoke access | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md#feature-060301-administrative-access) |

## Summary
A signed-in user can request temporary, time-boxed access to a permission they do not hold, see their own requests, and withdraw them. An organization administrator can review pending ORGANIZATION-scope requests from their organization and approve, reject or revoke them. The backend flow exists. Platform-administrator approval is already built (`AdminPrivilegedAccessPage`). This feature adds the requester view and the organization-administrator view.

## Actors
- Requester (any signed-in user)
- Organization administrator (standing `MANAGE_PRIVILEGED_ACCESS` in their organization)

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-<APP-CODE>-<NNN>.1 | A user can submit a request with a permission name, a justification and a duration in minutes. | Not specified |
| REQ-<APP-CODE>-<NNN>.2 | A user can see their own requests, with status, effective status (including EXPIRED), duration, decision and expiry. | Not specified |
| REQ-<APP-CODE>-<NNN>.3 | A user can withdraw their own request while it is pending, or while the grant is active. | Not specified |
| REQ-<APP-CODE>-<NNN>.4 | An organization administrator can list pending ORGANIZATION-scope requests for their own organization. | Not specified |
| REQ-<APP-CODE>-<NNN>.5 | An organization administrator can approve, reject or revoke those requests, with an optional note. | Not specified |
| REQ-<APP-CODE>-<NNN>.6 | An approved request grants the permission until it expires. | Not specified |
| REQ-<APP-CODE>-<NNN>.7 | When the backend refuses an action, its message is shown. | Not specified |

## Out of scope
- Platform-administrator approve, reject and revoke (already built: `AdminPrivilegedAccessPage`, `/admin/privileged-access/**`)
- Any duration option, permission or scope beyond what the backend accepts

## Dependencies
- Existing endpoints under `/me/privileged-access` and `/organization/me/privileged-access`.
- Existing types in `frontend/src/api/platformPrivilegedAccessApi.ts` (reuse, do not duplicate the platform calls).

## Open questions
- How the requester chooses `permissionName` in the UI is **Not specified**. The backend accepts any permission name that some role grants. There is no endpoint that lists requestable permissions for a regular user (`GET /me/permissions` returns only the caller's own permissions). Recorded as C24 in [open-decisions.md](../../../01-business/roadmap/open-decisions.md).
