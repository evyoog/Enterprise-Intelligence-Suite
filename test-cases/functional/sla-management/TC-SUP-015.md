# TC-SUP-015: Customers see the deadlines but never the SLA states

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-015 |
| Requirement ID (required) | [REQ-SUP-002](../../../docs/02-requirements/FRD/sla-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/sla-management/acceptance-criteria.md), [AC-16](../../../docs/02-requirements/FRD/sla-management/acceptance-criteria.md) |
| Business rule(s) | [BR-SLA-014](../../../docs/02-requirements/FRD/sla-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-002](TESTPLAN-SUP-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A customer with an open ticket, a ticket someone has responded to, a resolved ticket and a ticket without deadlines.

## Steps
1. The customer reads their ticket through the API; an administrator reads the same ticket.
2. Open My tickets.

## Expected Result
Step 1: the deadlines are returned and both state fields are null for the customer; the administrator gets ON_TRACK and ON_TRACK. Step 2: an open ticket shows "We aim to respond by … and resolve by …"; once responded only "We aim to resolve by …"; no target for a resolved or closed ticket or a ticket without deadlines; no SLA state anywhere; no axe violations.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/support/service/SlaServiceTest.java` — `theRequesterSeesTheDeadlinesButNotTheSlaState`
- `frontend/src/pages/MyTicketsPage.test.tsx` — "shows the response and resolution targets for an open ticket, without SLA states", "shows only the resolution target once someone has responded", "shows no targets for resolved or closed tickets or tickets without deadlines", "has no detectable a11y violations"

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
