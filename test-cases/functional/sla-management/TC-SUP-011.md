# TC-SUP-011: A breach escalates the ticket, keeps the priority and flags it

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-011 |
| Requirement ID (required) | [REQ-SUP-002](../../../docs/02-requirements/FRD/sla-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/sla-management/acceptance-criteria.md) |
| Business rule(s) | [BR-SLA-010, BR-SLA-011, BR-SLA-013](../../../docs/02-requirements/FRD/sla-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-002](TESTPLAN-SUP-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An OPEN URGENT ticket; person B on the alert list.

## Steps
1. Run the check 70 minutes after creation.
2. Read the ticket as an administrator.
3. Assign the ticket to an agent and read it again.

## Expected Result
Step 1: the first-response breach is flagged; the ticket is ESCALATED, priority URGENT, first response not recorded; B is told "…breached…"; the state is BREACHED. Step 3: the response is recorded and the state is MET_LATE.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/support/service/SlaServiceTest.java` — `aBreachEscalatesTheTicketKeepsThePriorityAndFlagsIt`

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
