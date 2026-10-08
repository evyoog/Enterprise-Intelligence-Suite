# TC-TEN-048: Invite user — duplicates and resend

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-048 |
| Requirement ID (required) | [REQ-TEN-008](../../../docs/02-requirements/FRD/invite-user/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/invite-user/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-008](TESTPLAN-TEN-008.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Steps
1. Invite an email, then invite it again.
2. Resend.

## Expected Result
The second invitation is refused ("already pending"); resend keeps one row, counts the send and issues a new link.

## Automated coverage
- `backend/.../invitation/service/InvitationServiceTest.java` — `aSecondPendingInvitationIsRefusedAndResendReplacesTheLink`
- `frontend/src/components/organization/OrganizationInvitationsCard.test.tsx`

## Actual Result
The automated tests passed on 2026-10-08.

## Status
Passed
