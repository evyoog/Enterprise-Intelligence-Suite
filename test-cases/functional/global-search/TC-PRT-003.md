# TC-PRT-003: A customer's own ticket appears only when a customer id is given

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-003 |
| Requirement ID (required) | [REQ-PRT-002](../../../docs/02-requirements/FRD/global-search/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/global-search/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PRT-002](TESTPLAN-PRT-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A ticket created by a customer.

## Steps
1. Arrange: a customer creates a ticket with a distinctive subject.
2. Act: call search(subject, null, customerId).
3. Act: call search(subject, null, null).
4. Observe both results.

## Expected Result
Step 2 includes the ticket; step 3 returns an empty tickets list.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/search/service/GlobalSearchServiceTest.java` — `findsTheCallersOwnMatchingTicketButNotWithoutACustomerId`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
