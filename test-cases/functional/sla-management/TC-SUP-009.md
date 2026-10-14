# TC-SUP-009: First response: the first time a person takes the ticket out of Open

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-009 |
| Requirement ID (required) | [REQ-SUP-002](../../../docs/02-requirements/FRD/sla-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/sla-management/acceptance-criteria.md) |
| Business rule(s) | [BR-SLA-006, BR-SLA-007](../../../docs/02-requirements/FRD/sla-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-002](TESTPLAN-SUP-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Two OPEN tickets.

## Steps
1. Categorize the first ticket only.
2. Assign it to an agent.
3. Escalate it.
4. Resolve the second ticket straight away.

## Expected Result
Step 1: no first response. Step 2: the first response is stored. Step 3: its time does not change. Step 4: the second ticket has a first response and its resolution state is MET.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/support/service/SlaServiceTest.java` — `theFirstResponseIsTheFirstTimeAPersonTakesTheTicketOutOfOpen`

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
