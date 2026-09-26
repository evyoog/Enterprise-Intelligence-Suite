# REQ-IAM-008 — MFA Recovery (administrator reset)

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-26 ([C30](../../../01-business/roadmap/open-decisions.md#c30))

| Field | Value |
|---|---|
| Sprint | [2026.3.3](../../../01-business/roadmap/sprints/SPRINT-2026.3.3.md) |
| Requirement ID | REQ-IAM-008 |
| Application | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md) |
| Application code | `APP-IAM` ([DN-5](../../../01-business/roadmap/open-decisions.md#dn-5-application-codes)) |
| Priority | P0: the application is MVP scope ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No |

## Source functions
| Function ID | Function | Application page |
|---|---|---|
| 06.01.02.03 | Recover MFA | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md#feature-060102-mfa) |

## Summary
Users already recover with their own recovery codes. When a user has lost both their authenticator and their codes, an administrator can reset their two-factor authentication: organization administrators for members of their own organization, platform administrators for anyone. Every reset is audited and the user is notified. If the organization requires MFA, the user sets up a new authenticator at their next sign-in ([C29](../../../01-business/roadmap/open-decisions.md#c29)).

## Actors
- Organization administrator (`MANAGE_USERS`)
- Platform administrator (`MANAGE_REGISTRATIONS`, the gate on `/admin/registrations/**`)
- The user whose MFA is reset

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-IAM-008.1 | An organization administrator can reset the two-factor authentication of a member of their own organization from the members card. | P0 |
| REQ-IAM-008.2 | A platform administrator can reset any account's two-factor authentication by email from the registrations page. | P0 |
| REQ-IAM-008.3 | A reset removes the authenticator, the recovery codes and any sign-in waiting on a code; the user is notified and the reset is audited. | P0 |
| REQ-IAM-008.4 | Nobody can reset their own two-factor authentication this way. | P0 |

## Out of scope
- Resetting a Keycloak-native OTP credential (the platform authenticator only)
- Self-service recovery beyond the existing recovery codes

## Dependencies
- `PlatformMfaService`, `OrganizationSelfService#requirePermissionOnMember`, `NotificationService`, `AuditService`.
