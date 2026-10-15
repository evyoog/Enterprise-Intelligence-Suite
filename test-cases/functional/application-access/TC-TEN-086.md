# TC-TEN-086: Requester cancels their own pending request

| Field | Value |
|---|---|
| Test Case ID (required) | TC-TEN-086 |
| Requirement ID (required) | [REQ-TEN-010](../../../docs/02-requirements/FRD/application-access/requirement.md) (.3, BR-AAC-004) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/application-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-TEN-010](TESTPLAN-TEN-010.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes (`AccessRequestServiceTest.aMemberCanCancelTheirOwnPendingRequest`) |

## Preconditions
A member with a PENDING request.

## Steps
1. The member calls `cancel(memberId, requestId)`.
2. The member calls `cancel(memberId, requestId)` again on the same (now CANCELLED) request.

## Expected Result
Step 1 succeeds: the request becomes CANCELLED. Step 2 is refused (`IllegalArgumentException`, only a pending request can be cancelled) — a cancelled request is a historical record.

## Status
Passed (automated, 2026-10-15).

## Linked Defect (if failed)
None.
