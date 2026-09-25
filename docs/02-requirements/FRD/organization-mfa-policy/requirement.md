# REQ-<APP-CODE>-<NNN> — Organization MFA Policy

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
| 06.02.02.01 | Create policy (MFA policy only) | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md#feature-060202-policy) |
| 06.02.02.02 | Assign policy (MFA policy only) | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md#feature-060202-policy) |
| 06.02.02.03 | Evaluate policy (MFA policy enforcement, already implemented) | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md#feature-060202-policy) |

## Summary
An organization administrator can require multi-factor authentication for every member of their organization. The setting is the organization's `mfaRequired` flag. The backend already stores it and enforces it at login; this feature adds the administrator control to set it. It covers the MFA policy only. A general policy engine is out of scope.

## Actors
- Organization administrator (standing `ORG_ADMIN` role, or a member holding an active privileged-access grant for `MANAGE_ORGANIZATION`)
- Organization member (subject to the policy at login)

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-<APP-CODE>-<NNN>.1 | The administrator can see whether MFA is currently required for their organization. | Not specified |
| REQ-<APP-CODE>-<NNN>.2 | The administrator can turn the MFA requirement on or off for their own organization. | Not specified |
| REQ-<APP-CODE>-<NNN>.3 | When MFA is required, a member's login that did not use a one-time password is refused (existing behaviour). | Not specified |

## Out of scope
- A general policy engine or any policy other than the MFA requirement (App 15 Policy Management, sprint 2027.2.2; see [open-decisions.md](../../../01-business/roadmap/open-decisions.md))
- Changing MFA enrolment for individual users (06.01.02, already implemented)

## Dependencies
- Existing endpoints `GET /organization/me` and `PATCH /organization/me/mfa-policy`.
- Existing login enforcement in `AuthController#login` and `MfaPolicyService`.
