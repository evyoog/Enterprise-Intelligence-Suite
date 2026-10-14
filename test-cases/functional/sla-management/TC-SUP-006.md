# TC-SUP-006: A new ticket gets both deadlines from its creation time

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-006 |
| Requirement ID (required) | [REQ-SUP-002](../../../docs/02-requirements/FRD/sla-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/sla-management/acceptance-criteria.md) |
| Business rule(s) | [BR-SLA-001, BR-SLA-005](../../../docs/02-requirements/FRD/sla-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-002](TESTPLAN-SUP-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A customer; the seeded SLA policies.

## Steps
1. The customer creates a ticket.
2. Read the ticket as an administrator.
3. Read the SLA configuration.

## Expected Result
The ticket has a first-response deadline after its creation time, a later resolution deadline, no first response and both states ON_TRACK; the configuration lists LOW, MEDIUM, HIGH and URGENT with URGENT around the clock.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/support/service/SlaServiceTest.java` — `aNewTicketGetsBothDeadlinesFromItsCreationTime`, `theTargetsAndBusinessHoursCanBeEditedAndAreValidated`

The seeded minute values other than Urgent (60 / 480) are not asserted by a test; they are in migration V041 and the FRD.

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
