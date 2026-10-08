# TC-TEN-047: Invite user — token and email

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-047 |
| Requirement ID (required) | [REQ-TEN-008](../../../docs/02-requirements/FRD/invite-user/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/invite-user/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-008](TESTPLAN-TEN-008.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Steps
1. Send an invitation and read the stored row.
2. Resend and open the old link.

## Expected Result
Only a 64-character hash is stored; the old link is invalid after resend; the email carries the link.

## Automated coverage
- `backend/.../invitation/service/InvitationServiceTest.java` — `theTokenIsStoredOnlyAsAHashAndNeverReturned`, `aSecondPendingInvitationIsRefusedAndResendReplacesTheLink`

## Actual Result
The automated tests passed on 2026-10-08.

## Status
Passed
