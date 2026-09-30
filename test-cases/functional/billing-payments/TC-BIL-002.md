# TC-BIL-002: A customer cannot see another customer's invoice

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-002 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Two customers; customer A has one invoice.

## Steps
1. Act: customer B requests customer A's invoice by id.

## Expected Result
A `ResourceNotFoundException` (generic 404) is thrown — never a 403 that would confirm the invoice exists.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/BillingServiceTest.java` — `aCustomerCannotSeeAnotherCustomersInvoice`

## Actual Result
The automated test passed on 2026-09-30.

## Status
Passed (automated run 2026-09-30)

## Linked Defect (if failed)
None.
