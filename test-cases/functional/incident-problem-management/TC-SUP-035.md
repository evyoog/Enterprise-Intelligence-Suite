# TC-SUP-035: Incident and problem changes are audited

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-035 |
| Requirement ID (required) | [REQ-SUP-003](../../../docs/02-requirements/FRD/incident-problem-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-15](../../../docs/02-requirements/FRD/incident-problem-management/acceptance-criteria.md) |
| Business rule(s) | [BR-INC-017](../../../docs/02-requirements/FRD/incident-problem-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-003](TESTPLAN-SUP-003.md) |
| Priority | P1 |
| Type | Functional (manual) |
| Automated | No |

## Preconditions
A platform administrator; the audit log.

## Steps
1. Log, update, link a ticket to and unlink a ticket from an incident.
2. Create a problem, update it, link and unlink an incident, add, update and delete an action.
3. Read the audit log.

## Expected Result
The entries `INCIDENT_POSTED`, `INCIDENT_UPDATED`, `INCIDENT_TICKET_LINKED`, `INCIDENT_TICKET_UNLINKED`, `PROBLEM_CREATED`, `PROBLEM_UPDATED`, `PROBLEM_INCIDENT_LINKED`, `PROBLEM_INCIDENT_UNLINKED`, `PROBLEM_ACTION_ADDED`, `PROBLEM_ACTION_UPDATED` and `PROBLEM_ACTION_DELETED` exist.

## Automated coverage
None. This case is manual only.

Manual only: no automated test asserts these audit entries. **Not Run**.

## Actual Result
Not Run. The case has not been run by hand yet.

## Status
Not Run

## Linked Defect (if failed)
None.
