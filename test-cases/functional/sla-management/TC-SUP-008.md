# TC-SUP-008: A priority change recalculates both deadlines from the creation time

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-008 |
| Requirement ID (required) | [REQ-SUP-002](../../../docs/02-requirements/FRD/sla-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/sla-management/acceptance-criteria.md) |
| Business rule(s) | [BR-SLA-008](../../../docs/02-requirements/FRD/sla-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-002](TESTPLAN-SUP-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A new MEDIUM ticket.

## Steps
1. An administrator changes the priority to URGENT.
2. Compare the deadlines with the creation time.

## Expected Result
First-response deadline = creation + 60 minutes, resolution deadline = creation + 480 minutes (24x7); the first-response deadline differs from the MEDIUM one.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/support/service/SlaServiceTest.java` — `changingThePriorityRecalculatesTheDeadlinesFromTheCreationTime`

Not covered by an automated test: recalculation by escalation, and clearing of the warning marks.

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
