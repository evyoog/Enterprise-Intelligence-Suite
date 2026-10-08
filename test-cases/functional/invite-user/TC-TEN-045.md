# TC-TEN-045: Invite user — who can invite

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-045 |
| Requirement ID (required) | [REQ-TEN-008](../../../docs/02-requirements/FRD/invite-user/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/invite-user/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-008](TESTPLAN-TEN-008.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Steps
1. As an organization administrator send an invitation.
2. As a plain member try to send one.

## Expected Result
The administrator's invitation is created and emailed; the member gets 403 "You do not have permission to invite users."

## Automated coverage
- `backend/.../invitation/service/InvitationServiceTest.java` — `anAdministratorInvitesAndAPlainMemberCannot`

## Actual Result
The automated tests passed on 2026-10-08.

## Status
Passed
