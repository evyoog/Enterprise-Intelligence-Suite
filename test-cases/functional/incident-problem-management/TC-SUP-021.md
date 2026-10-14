# TC-SUP-021: A new incident is Logged, and the internal fields stay off the customer record

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-021 |
| Requirement ID (required) | [REQ-SUP-003](../../../docs/02-requirements/FRD/incident-problem-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/incident-problem-management/acceptance-criteria.md) |
| Business rule(s) | [BR-INC-001, BR-INC-002, BR-INC-003](../../../docs/02-requirements/FRD/incident-problem-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-003](TESTPLAN-SUP-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An active product; a platform administrator.

## Steps
1. Log an incident with severity MAJOR and the internal notes "Queue backed up on node 3".
2. Read it in the administrator list.
3. Inspect the customer incident record (`IncidentDto`).

## Expected Result
The incident is LOGGED, open, MAJOR, with those notes, and is in the administrator list. The customer record has none of severity, phase, internalNotes, assignedToCustomerId, resolutionSummary, problemId or tickets.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/servicestatus/service/IncidentAndProblemTest.java` — `aNewIncidentIsLoggedAndTheInternalFieldsStayOffTheStatusPage`

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
