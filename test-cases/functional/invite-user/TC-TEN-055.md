# TC-TEN-055: Invite user — audit (AC-12)

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-055 |
| Requirement ID (required) | [REQ-TEN-008](../../../docs/02-requirements/FRD/invite-user/requirement.md) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/invite-user/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-008](TESTPLAN-TEN-008.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Steps
Create, resend, revoke, accept, decline an invitation, let one expire, refuse one for seat, organization state and other organization, grant and remove *Can invite*; open the audit log.

## Expected Result
Entries `INVITATION_CREATED`, `_RESENT`, `_REVOKED`, `_ACCEPTED`, `_DECLINED`, `_EXPIRED`, `INVITATION_ACCEPT_FAILED_SEAT` / `_ORGANIZATION` / `_OTHER_ORGANIZATION` (outcome FAILURE), `INVITE_PERMISSION_GRANTED` / `_REMOVED`.

## Automated coverage
`InvitationServiceTest` checks the seat FAILURE entry (`acceptanceChecksSeatsAgainAndRefusesWithoutActivatingAnything`); the other actions are written by the same calls the tests run.

## Status
Passed for the seat entry; the rest is checked by reading the audit log (manual).
