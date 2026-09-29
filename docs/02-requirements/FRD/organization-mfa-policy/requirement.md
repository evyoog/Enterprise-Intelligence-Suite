# REQ-IAM-001 — Organization MFA Policy

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-25

| Field | Value |
|---|---|
| Sprint | [2026.3.3](../../../01-business/roadmap/sprints/SPRINT-2026.3.3.md) |
| Requirement ID | REQ-IAM-001 |
| Application | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md) |
| Application code | `APP-IAM` ([DN-5](../../../01-business/roadmap/open-decisions.md#dn-5-application-codes)) |
| Priority | P0: the application is MVP scope ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No. This feature makes no use of AI (proposed; confirmed when the FRD is approved, C12) |

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
| REQ-IAM-001.1 | The administrator can see whether MFA is currently required for their organization. | P0 |
| REQ-IAM-001.2 | The administrator can turn the MFA requirement on or off for their own organization. | P0 |
| REQ-IAM-001.3 | When MFA is required, a member's password sign-in that did not use a one-time password is not turned into a session until the member passes the platform authenticator step. Changed 2026-09-26 ([C29](../../../01-business/roadmap/open-decisions.md#c29)): the sign-in is no longer refused. | P0 |
| REQ-IAM-001.4 | The policy also applies to SAML (and OIDC) sign-in: a member of an organization that requires MFA passes the platform authenticator step after the identity provider ([C29](../../../01-business/roadmap/open-decisions.md#c29)). | P0 |
| REQ-IAM-001.5 | A member with no authenticator sets one up during sign-in (QR code, first code, recovery codes shown once). No password is asked, because the user has just signed in ([C29](../../../01-business/roadmap/open-decisions.md#c29)). | P0 |

## Out of scope
- A general policy engine or any policy other than the MFA requirement. Under [C21](../../../01-business/roadmap/open-decisions.md#c21) it is deferred to sprint 2027.2.2 with Policy Management (15.02)
- Changing MFA enrolment for individual users (06.01.02, already implemented)

## Dependencies
- Existing endpoints `GET /organization/me` and `PATCH /organization/me/mfa-policy`.
- Login enforcement in `SignInMfaGate`, used by `AuthController#login` and `SamlAuthenticationService#handleAcs`; policy lookup in `MfaPolicyService`.
