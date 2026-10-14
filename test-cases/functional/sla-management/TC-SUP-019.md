# TC-SUP-019: SLA changes, warnings and breaches are audited

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-019 |
| Requirement ID (required) | [REQ-SUP-002](../../../docs/02-requirements/FRD/sla-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-14](../../../docs/02-requirements/FRD/sla-management/acceptance-criteria.md) |
| Business rule(s) | [BR-SLA-011, BR-SLA-018](../../../docs/02-requirements/FRD/sla-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-002](TESTPLAN-SUP-002.md) |
| Priority | P1 |
| Type | Functional (manual) |
| Automated | No |

## Preconditions
A platform administrator; a ticket that warns and breaches.

## Steps
1. Save targets, then settings.
2. Let a ticket pass its warning point, then its deadline.
3. Read the audit log.

## Expected Result
`SLA_POLICY_UPDATED`, `SLA_SETTINGS_UPDATED`, `TICKET_SLA_WARNING` and `TICKET_SLA_BREACHED` exist for the ticket and the actions.

## Automated coverage
None. This case is manual only.

Manual only: no automated test asserts these audit entries. **Not Run**.

## Actual Result
Not Run. The case has not been run by hand yet.

## Status
Not Run

## Linked Defect (if failed)
None.
