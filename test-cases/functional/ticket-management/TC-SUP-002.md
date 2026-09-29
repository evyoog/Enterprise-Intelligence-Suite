# TC-SUP-002: Another customer's ticket is invisible

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-002 |
| Requirement ID (required) | [REQ-SUP-001](../../../docs/02-requirements/FRD/ticket-management/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/ticket-management/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUP-001](TESTPLAN-SUP-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A ticket owned by one customer.

## Steps
1. Arrange: customer A creates a ticket.
2. Act: customer B (a stranger) calls getMyTicket with A's ticket id.
3. Observe the response.

## Expected Result
The request is refused with a generic 404 (`ResourceNotFoundException`).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/support/service/SupportTicketServiceTest.java` — `anotherCustomersTicketIsInvisible`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
