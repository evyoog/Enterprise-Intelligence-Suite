# REQ-IAM-004 — Privileged Access (User and Organization Administrator)

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-25

| Field | Value |
|---|---|
| Sprint | [2026.3.3](../../../01-business/roadmap/sprints/SPRINT-2026.3.3.md) |
| Requirement ID | REQ-IAM-004 |
| Application | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md) |
| Application code | `APP-IAM` ([DN-5](../../../01-business/roadmap/open-decisions.md#dn-5-application-codes)) |
| Priority | P0: the application is MVP scope ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No. This feature makes no use of AI (proposed; confirmed when the FRD is approved, C12) |

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
| REQ-IAM-004.1 | A user can submit a request with a permission name, a justification and a duration in minutes. | P0 |
| REQ-IAM-004.2 | A user can see their own requests, with status, effective status (including EXPIRED), duration, decision and expiry. | P0 |
| REQ-IAM-004.3 | A user can withdraw their own request while it is pending, or while the grant is active. | P0 |
| REQ-IAM-004.4 | An organization administrator can list pending ORGANIZATION-scope requests for their own organization. | P0 |
| REQ-IAM-004.5 | An organization administrator can approve, reject or revoke those requests, with an optional note. | P0 |
| REQ-IAM-004.6 | An approved request grants the permission until it expires. | P0 |
| REQ-IAM-004.7 | The request form offers the permissions the user may request as a dropdown, from a new read-only endpoint `GET /me/privileged-access/requestable-permissions` ([C24](../../../01-business/roadmap/open-decisions.md#c24)). | P0 |
| REQ-IAM-004.8 | When the backend refuses an action, its message is shown. | P0 |
| REQ-IAM-004.9 | Organization administrators and platform administrators can list the active (approved, unexpired) grants of their scope, see who holds each one, and revoke one early with an optional note (sprint audit 2026-09-26). | P0 |

## Out of scope
- Platform-administrator approve and reject (already built: `AdminPrivilegedAccessPage`). Correction 2026-09-26: this FRD said platform revoke was also built; the page had no revoke. It is added by REQ-IAM-004.9.
- Any duration option, permission or scope beyond what the backend accepts

## Dependencies
- Existing endpoints under `/me/privileged-access` and `/organization/me/privileged-access`.
- Backend endpoint added under [C24](../../../01-business/roadmap/open-decisions.md#c24): `GET /me/privileged-access/requestable-permissions` (`PrivilegedAccessController`). It adds no new rules.
- Existing types in `frontend/src/api/platformPrivilegedAccessApi.ts` (reuse, do not duplicate the platform calls).
