# TC-SUP-012: A resolved ticket is no longer watched

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-012 |
| Requirement ID (required) | [REQ-SUP-002](../../../docs/02-requirements/FRD/sla-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/sla-management/acceptance-criteria.md) |
| Business rule(s) | [BR-SLA-012](../../../docs/02-requirements/FRD/sla-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-002](TESTPLAN-SUP-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An URGENT ticket that has been resolved.

## Steps
1. Run the check three days later.

## Expected Result
The resolution is not breached and the status stays RESOLVED.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/support/service/SlaServiceTest.java` — `aResolvedTicketIsNotWatchedAnyMore`

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
