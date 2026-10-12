# TC-TEN-059: Invite user — a refused acceptance sends no notice

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-059 |
| Requirement ID (required) | [REQ-TEN-008](../../../docs/02-requirements/FRD/invite-user/requirement.md) (REQ-TEN-008.19, BR-INV-018) |
| Acceptance Criterion | [AC-13](../../../docs/02-requirements/FRD/invite-user/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-008](TESTPLAN-TEN-008.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes (`InvitationServiceTest.aRefusedAcceptanceSendsNoNotice`) |

## Steps
1. An administrator invites an address.
2. A different signed-in account opens the link and tries to accept.
3. Open the administrator's notification center.

## Expected Result
The acceptance is refused (403, different email) and the inviter has no notification.

## Status
Passed (automated, 2026-10-12).
