# TC-SUP-014: New targets do not move the deadlines of existing tickets

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-014 |
| Requirement ID (required) | [REQ-SUP-002](../../../docs/02-requirements/FRD/sla-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/sla-management/acceptance-criteria.md) |
| Business rule(s) | [BR-SLA-016](../../../docs/02-requirements/FRD/sla-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-002](TESTPLAN-SUP-002.md) |
| Priority | P1 |
| Type | Functional (manual) |
| Automated | No |

## Preconditions
An open ticket with deadlines; a platform administrator.

## Steps
1. Note the ticket's two deadlines.
2. Save new targets for its priority.
3. Read the ticket again.
4. Create a new ticket of the same priority, and re-prioritise the first ticket.

## Expected Result
Step 3: both deadlines are unchanged. Step 4: the new ticket gets the new targets; the re-prioritised ticket's deadlines are recalculated from its creation time with the new targets.

## Automated coverage
None. This case is manual only.

Manual only: no automated test saves targets and then reads an existing ticket. **Not Run**.

## Actual Result
Not Run. The case has not been run by hand yet.

## Status
Not Run

## Linked Defect (if failed)
None.
