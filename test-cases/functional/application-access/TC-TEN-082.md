# TC-TEN-082: Admin rejects a request with a required note

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-082 |
| Requirement ID (required) | [REQ-TEN-010](../../../docs/02-requirements/FRD/application-access/requirement.md) (.6, .7, BR-AAC-006, BR-AAC-007) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/application-access/acceptance-criteria.md), [AC-7](../../../docs/02-requirements/FRD/application-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-010](TESTPLAN-TEN-010.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`AccessRequestServiceTest.anAdminRejectsARequestWithAReason`, `aDecidedRequestCannotBeDecidedAgain`) |

## Preconditions
A PENDING request from a member.

## Steps
1. The admin calls `reject(adminId, requestId, { note: " " })` (blank note).
2. The admin calls `reject(adminId, requestId, { note: "Not needed for your role" })`.
3. The admin calls `reject` (or `approve`) again on the same request.

## Expected Result
Step 1 is refused (`IllegalArgumentException`, a reason is required). Step 2 succeeds: the request is REJECTED with `decisionNote` = "Not needed for your role", and the member is notified. Step 3 is refused — a decided request cannot be decided again.

## Status
Passed (automated, 2026-10-15).

## Linked Defect (if failed)
None.
