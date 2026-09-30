# TC-BIL-005: A refund over the refundable amount is refused; a valid partial refund updates payment and invoice

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-005 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A CAPTURED payment of 1999 (minor units) against a PAID invoice.

## Steps
1. Act: an admin refunds 999999 with a reason.
2. Observe the result.
3. Act: an admin refunds 500 with a reason.
4. Observe the payment and invoice.

## Expected Result
Step 2: `BillingConflictException` (HTTP 409, code `INVALID_STATE`) — refused, nothing changes. Step 4: the payment is PARTIALLY_REFUNDED; the invoice is PARTIALLY_REFUNDED.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/BillingServiceTest.java` — `refundingMoreThanCapturedIsRefused`

## Actual Result
The automated test passed on 2026-09-30.

## Status
Passed (automated run 2026-09-30)

## Linked Defect (if failed)
None.
