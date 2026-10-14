# TC-SUP-016: Administrators see SLA chips, the breach flag and the SLA filter

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-016 |
| Requirement ID (required) | [REQ-SUP-002](../../../docs/02-requirements/FRD/sla-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-11](../../../docs/02-requirements/FRD/sla-management/acceptance-criteria.md), [AC-16](../../../docs/02-requirements/FRD/sla-management/acceptance-criteria.md) |
| Business rule(s) | [BR-SLA-013](../../../docs/02-requirements/FRD/sla-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-002](TESTPLAN-SUP-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A platform administrator; tickets that are on track, at risk and breached.

## Steps
1. Open Support → Tickets.
2. Filter by "At risk (warning)", then "Breached", then a filter that matches nothing.

## Expected Result
Each row shows a first-response chip and a resolution chip with state and due time, and "SLA breached" for a breached ticket; the filter shows only matching tickets and, for no match, "No tickets match this SLA filter"; no axe violations.

## Automated coverage
- `frontend/src/pages/admin/AdminSupportTicketsPage.test.tsx` — "shows both SLA chips with due times, and flags a breached ticket", "filters by SLA state", "shows an empty state when nothing matches the SLA filter", "has no detectable a11y violations"
- `frontend/src/components/support/SlaChip.test.tsx` — "shows the caption and the due time", "shows only the due time when there is no state, and nothing without data", "has no detectable a11y violations"
- `backend/src/test/java/com/vyoog/eisplatform/modules/support/service/SlaServiceTest.java` — `aWarningGoesOutOnceAtTheWarningPointToTheAssigneeAndTheAlertList (state WARNING)`, `aBreachEscalatesTheTicketKeepsThePriorityAndFlagsIt (BREACHED, MET_LATE)`

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
